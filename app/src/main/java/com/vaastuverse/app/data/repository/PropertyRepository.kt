package com.vaastuverse.app.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.vaastuverse.app.data.SavedProperty
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.propertyDataStore: DataStore<Preferences> by preferencesDataStore("saved_properties")

class PropertyRepository(private val context: Context) {
    private val gson = Gson()

    fun propertiesFlow(userId: String): Flow<List<SavedProperty>> =
        context.propertyDataStore.data.map { prefs ->
            decode(prefs[stringPreferencesKey(key(userId))])
        }

    suspend fun listProperties(userId: String): List<SavedProperty> =
        propertiesFlow(userId).first()

    suspend fun saveProperty(userId: String, property: SavedProperty) {
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

    suspend fun deleteProperty(userId: String, propertyId: String) {
        context.propertyDataStore.edit { prefs ->
            val current = decode(prefs[stringPreferencesKey(key(userId))])
                .filterNot { it.id == propertyId }
            prefs[stringPreferencesKey(key(userId))] = gson.toJson(current)
        }
    }

    private fun key(userId: String) = "properties_$userId"

    private fun decode(raw: String?): List<SavedProperty> {
        if (raw.isNullOrBlank()) return emptyList()
        return runCatching {
            val type = object : TypeToken<List<SavedProperty>>() {}.type
            gson.fromJson<List<SavedProperty>>(raw, type)
        }.getOrElse { emptyList() }
    }
}
