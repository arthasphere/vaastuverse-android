package com.vaastuverse.app.data.dto

data class CustomerPropertyRequest(
    val type: String,
    val label: String,
    val details: Map<String, String>,
)

data class CustomerPropertyResponse(
    val id: String,
    val userId: String,
    val type: String,
    val label: String,
    val details: Map<String, String>? = null,
    val createdAt: String?,
    val updatedAt: String?,
)
