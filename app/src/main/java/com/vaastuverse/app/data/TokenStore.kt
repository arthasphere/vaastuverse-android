package com.vaastuverse.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.tokenDataStore: DataStore<Preferences> by preferencesDataStore("auth_tokens")

data class StoredSession(
    val accessToken: String,
    val refreshToken: String,
    val userId: String,
    val roles: List<String>,
) {
    val hasPartnerRole: Boolean
        get() = roles.any { role ->
            role.startsWith("GURUJI") || role == "DESIGNER" || role == "CHANNEL_PARTNER"
        }

    val partnerRoles: List<String>
        get() = roles.filter { it != "CUSTOMER" }
}

class TokenStore(private val context: Context) {
    private val keyAccess = stringPreferencesKey("access_token")
    private val keyRefresh = stringPreferencesKey("refresh_token")
    private val keyUserId = stringPreferencesKey("user_id")
    private val keyRoles = stringPreferencesKey("roles")

    val sessionFlow: Flow<StoredSession?> = context.tokenDataStore.data.map { prefs ->
        val access = prefs[keyAccess] ?: return@map null
        val refresh = prefs[keyRefresh] ?: return@map null
        val userId = prefs[keyUserId] ?: return@map null
        val roles = prefs[keyRoles]?.split(",")?.filter { it.isNotBlank() } ?: emptyList()
        StoredSession(access, refresh, userId, roles)
    }

    suspend fun currentSession(): StoredSession? = sessionFlow.first()

    suspend fun save(session: StoredSession) {
        context.tokenDataStore.edit { prefs ->
            prefs[keyAccess] = session.accessToken
            prefs[keyRefresh] = session.refreshToken
            prefs[keyUserId] = session.userId
            prefs[keyRoles] = session.roles.joinToString(",")
        }
    }

    suspend fun clear() {
        context.tokenDataStore.edit { it.clear() }
    }
}
