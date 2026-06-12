package com.vaastuverse.app.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.vaastuverse.app.data.PropertyFieldKeys
import com.vaastuverse.app.data.PropertyValidationException
import com.vaastuverse.app.data.SavedProperty
import com.vaastuverse.app.data.SavedPropertyType
import com.vaastuverse.app.data.isLocalOnly
import com.vaastuverse.app.data.newLocalPropertyId
import com.vaastuverse.app.data.StoredSession
import com.vaastuverse.app.data.buildPropertyLabel
import com.vaastuverse.app.data.dto.CustomerPropertyRequest
import com.vaastuverse.app.data.dto.CustomerPropertyResponse
import com.vaastuverse.app.data.network.ApiClient
import com.vaastuverse.app.data.toDetailsMap
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import retrofit2.HttpException

private val Context.propertyDataStore: DataStore<Preferences> by preferencesDataStore("saved_properties")

class PropertyRepository(private val context: Context) {
    private val gson = Gson()
    private val api get() = ApiClient.api

    fun propertiesFlow(userId: String): Flow<List<SavedProperty>> =
        context.propertyDataStore.data.map { prefs ->
            runCatching { decode(prefs[stringPreferencesKey(key(userId))]) }
                .getOrElse { emptyList() }
        }

    suspend fun listProperties(userId: String): List<SavedProperty> =
        propertiesFlow(userId).first()

    suspend fun syncFromServer(session: StoredSession): List<SavedProperty> {
        return try {
            val remote = api.listCustomerProperties(
                session.userId,
                ApiClient.bearer(session.accessToken),
                type = null,
            ).map(::fromResponse)
            replaceLocal(session.userId, remote)
            remote
        } catch (_: Exception) {
            listProperties(session.userId)
        }
    }

    suspend fun fetchProperty(session: StoredSession, propertyId: String): SavedProperty? {
        if (propertyId.startsWith("local-")) {
            return listProperties(session.userId).find { it.id == propertyId }
        }
        return try {
            val response = api.getCustomerProperty(
                session.userId,
                propertyId,
                ApiClient.bearer(session.accessToken),
            )
            fromResponse(response).also { upsertLocal(session.userId, it) }
        } catch (_: Exception) {
            listProperties(session.userId).find { it.id == propertyId }
        }
    }

    suspend fun saveProperty(session: StoredSession, property: SavedProperty): SavedProperty {
        val payload = property.copy(
            label = buildPropertyLabel(property.type, property.details),
        )
        val saved = try {
            val request = CustomerPropertyRequest(
                type = payload.type.name,
                label = payload.label,
                details = payload.toDetailsMap(),
            )
            val response = if (payload.isLocalOnly()) {
                api.createCustomerProperty(
                    session.userId,
                    ApiClient.bearer(session.accessToken),
                    request,
                )
            } else {
                api.updateCustomerProperty(
                    session.userId,
                    payload.id,
                    ApiClient.bearer(session.accessToken),
                    request,
                )
            }
            fromResponse(response)
        } catch (e: HttpException) {
            val body = e.response()?.errorBody()?.string().orEmpty()
            val message = parseApiError(body) ?: "Could not save property"
            if (e.code() in 400..499) throw PropertyValidationException(message)
            throw IllegalStateException(message)
        }
        upsertLocal(session.userId, saved)
        return saved
    }

    suspend fun deleteProperty(session: StoredSession, propertyId: String) {
        if (!propertyId.startsWith("local-")) {
            runCatching {
                api.deleteCustomerProperty(
                    session.userId,
                    propertyId,
                    ApiClient.bearer(session.accessToken),
                )
            }
        }
        deleteLocal(session.userId, propertyId)
    }

    private suspend fun replaceLocal(userId: String, properties: List<SavedProperty>) {
        context.propertyDataStore.edit { prefs ->
            prefs[stringPreferencesKey(key(userId))] = gson.toJson(properties)
        }
    }

    private suspend fun upsertLocal(userId: String, property: SavedProperty) {
        context.propertyDataStore.edit { prefs ->
            val current = decode(prefs[stringPreferencesKey(key(userId))]).toMutableList()
            val index = current.indexOfFirst { it.id == property.id }
            if (index >= 0) {
                current[index] = property
            } else {
                current.add(property)
            }
            prefs[stringPreferencesKey(key(userId))] = gson.toJson(current)
        }
    }

    private suspend fun deleteLocal(userId: String, propertyId: String) {
        context.propertyDataStore.edit { prefs ->
            val current = decode(prefs[stringPreferencesKey(key(userId))])
                .filterNot { it.id == propertyId }
            prefs[stringPreferencesKey(key(userId))] = gson.toJson(current)
        }
    }

    private fun key(userId: String) = "properties_$userId"

    private fun fromResponse(response: CustomerPropertyResponse): SavedProperty {
        val type = runCatching { SavedPropertyType.valueOf(response.type) }
            .getOrDefault(SavedPropertyType.HOME)
        return SavedProperty(
            id = response.id,
            type = type,
            label = response.label,
            details = response.details.orEmpty(),
        )
    }

    private data class SavedPropertyDto(
        val id: String? = null,
        val type: SavedPropertyType? = null,
        val label: String? = null,
        val details: Map<String, String>? = null,
        val address: String? = null,
        val city: String? = null,
        val notes: String? = null,
    )

    private fun decode(raw: String?): List<SavedProperty> {
        if (raw.isNullOrBlank()) return emptyList()
        val type = object : TypeToken<List<SavedPropertyDto>>() {}.type
        val dtos = runCatching { gson.fromJson<List<SavedPropertyDto>>(raw, type) }
            .getOrElse { emptyList() }
            .orEmpty()
        return dtos.mapNotNull { dto ->
            runCatching {
                val propertyType = dto.type ?: return@runCatching null
                migrateLegacy(
                    SavedProperty(
                        id = dto.id?.trim().orEmpty().ifBlank { newLocalPropertyId() },
                        type = propertyType,
                        label = dto.label?.trim().orEmpty(),
                        details = dto.details.orEmpty(),
                        address = dto.address,
                        city = dto.city,
                        notes = dto.notes,
                    ),
                )
            }.getOrNull()
        }
    }

    private fun migrateLegacy(property: SavedProperty): SavedProperty {
        val existingDetails = property.details
        if (existingDetails.isNotEmpty()) {
            return property.copy(
                label = buildPropertyLabel(property.type, existingDetails).ifBlank { property.label },
            )
        }
        if (property.address.isNullOrBlank() && property.label.isBlank()) return property
        val details = mutableMapOf<String, String>()
        when (property.type) {
            SavedPropertyType.HOME -> {
                details[PropertyFieldKeys.PROJECT_NAME] = property.label
                details[PropertyFieldKeys.EXACT_ADDRESS] = property.address.orEmpty()
            }
            SavedPropertyType.OFFICE -> {
                details[PropertyFieldKeys.BUSINESS_NAME] = property.label
                details[PropertyFieldKeys.OFFICE_ADDRESS] = property.address.orEmpty()
            }
            SavedPropertyType.SHOP -> {
                details[PropertyFieldKeys.SHOP_NAME] = property.label
                details[PropertyFieldKeys.LOCATION] = property.address.orEmpty()
            }
            SavedPropertyType.FACTORY -> {
                details[PropertyFieldKeys.FACILITY_NAME] = property.label
                details[PropertyFieldKeys.LOCATION] = property.address.orEmpty()
            }
        }
        property.city?.takeIf { it.isNotBlank() }?.let { city ->
            val locationKey = when (property.type) {
                SavedPropertyType.HOME -> PropertyFieldKeys.EXACT_ADDRESS
                SavedPropertyType.OFFICE -> PropertyFieldKeys.OFFICE_ADDRESS
                else -> PropertyFieldKeys.LOCATION
            }
            val existing = details[locationKey].orEmpty()
            details[locationKey] = listOf(existing, city).filter { it.isNotBlank() }.joinToString(", ")
        }
        property.notes?.takeIf { it.isNotBlank() }?.let {
            details[PropertyFieldKeys.INTERIOR_LAYOUT_NOTES] = it
        }
        return property.copy(
            label = buildPropertyLabel(property.type, details).ifBlank { property.label },
            details = details,
        )
    }

    private fun parseApiError(body: String): String? = runCatching {
        val json = gson.fromJson(body, JsonObject::class.java)
        json?.get("message")?.asString ?: json?.get("error")?.asString
    }.getOrNull()
}
