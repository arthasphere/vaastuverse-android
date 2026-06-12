package com.vaastuverse.app.data.dto

data class CustomerProfileRequest(
    val displayName: String,
    val city: String? = null,
    val profilePicUrl: String? = null,
    val dateOfBirth: String? = null,
)

data class CustomerProfileResponse(
    val id: String,
    val userId: String,
    val displayName: String,
    val city: String?,
    val profilePicUrl: String?,
    val dateOfBirth: String? = null,
    val createdAt: String?,
)

fun CustomerProfileResponse.hasDateOfBirth(): Boolean = !dateOfBirth.isNullOrBlank()

data class PartnerProfileResponse(
    val id: String,
    val userId: String,
    val partnerType: String,
    val businessName: String?,
    val kycStatus: String?,
    val createdAt: String?,
)
