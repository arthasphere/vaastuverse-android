package com.vaastuverse.app.data.dto

data class AccountMeResponse(
    val userId: String,
    val phone: String?,
    val email: String?,
    val phoneVerified: Boolean,
    val emailVerified: Boolean,
)
