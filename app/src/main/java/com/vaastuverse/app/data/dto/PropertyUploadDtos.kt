package com.vaastuverse.app.data.dto

data class PropertyUploadResponse(
    val id: String,
    val userId: String,
    val fieldKey: String,
    val status: String,
    val fileUrl: String?,
    val originalFileName: String?,
    val failureReason: String?,
    val expiresAt: Long?,
)

data class PropertyFieldSuggestionsResponse(
    val uploadId: String,
    val fieldKey: String,
    val suggestedDetails: Map<String, String>?,
)
