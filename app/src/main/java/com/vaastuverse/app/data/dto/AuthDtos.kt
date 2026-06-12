package com.vaastuverse.app.data.dto

data class RegisterRequest(
    val phone: String,
    val email: String? = null,
    val password: String? = null,
    val displayName: String? = null,
)

data class OtpSendRequest(val phone: String)

data class OtpVerifyRequest(val phone: String, val code: String)

data class LoginRequest(val email: String, val password: String)

data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val userId: String,
    val roles: List<String>,
    val tokenType: String? = null,
    val accountPersona: String? = null,
)

data class MessageResponse(val message: String)
