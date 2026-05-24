package com.vaastuverse.app.data

data class CommunicationPreferences(
    val pushEnabled: Boolean = true,
    val smsEnabled: Boolean = true,
    val emailEnabled: Boolean = true,
    val orderUpdatesEnabled: Boolean = true,
    val partnerAlertsEnabled: Boolean = true,
    val marketingEnabled: Boolean = false,
)
