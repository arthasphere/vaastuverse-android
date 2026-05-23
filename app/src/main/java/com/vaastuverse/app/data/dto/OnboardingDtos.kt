package com.vaastuverse.app.data.dto

data class ApplyRequest(
    val requestedRole: String,
    val documents: List<DocumentEntry> = emptyList(),
) {
    data class DocumentEntry(val docType: String, val fileUrl: String)
}

data class ApplicationResponse(
    val id: String,
    val userId: String,
    val requestedRole: String,
    val status: String,
    val reviewerNotes: String?,
    val submittedAt: String?,
    val documents: List<ApplyRequest.DocumentEntry>?,
)
