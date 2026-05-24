package com.vaastuverse.app.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.vaastuverse.app.data.CommunicationPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.commPrefsDataStore by preferencesDataStore("communication_preferences")

class CommunicationPreferencesRepository(private val context: Context) {

    private fun key(userId: String, field: String) = booleanPreferencesKey("${userId}_$field")

    fun preferencesFlow(userId: String): Flow<CommunicationPreferences> =
        context.commPrefsDataStore.data.map { prefs ->
            CommunicationPreferences(
                pushEnabled = prefs[key(userId, "push")] ?: true,
                smsEnabled = prefs[key(userId, "sms")] ?: true,
                emailEnabled = prefs[key(userId, "email")] ?: true,
                orderUpdatesEnabled = prefs[key(userId, "order_updates")] ?: true,
                partnerAlertsEnabled = prefs[key(userId, "partner_alerts")] ?: true,
                marketingEnabled = prefs[key(userId, "marketing")] ?: false,
            )
        }

    suspend fun save(userId: String, prefs: CommunicationPreferences) {
        context.commPrefsDataStore.edit { store ->
            store[key(userId, "push")] = prefs.pushEnabled
            store[key(userId, "sms")] = prefs.smsEnabled
            store[key(userId, "email")] = prefs.emailEnabled
            store[key(userId, "order_updates")] = prefs.orderUpdatesEnabled
            store[key(userId, "partner_alerts")] = prefs.partnerAlertsEnabled
            store[key(userId, "marketing")] = prefs.marketingEnabled
        }
    }
}
