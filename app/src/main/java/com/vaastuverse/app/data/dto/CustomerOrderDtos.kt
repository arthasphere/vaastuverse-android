package com.vaastuverse.app.data.dto

data class CustomerOrderRequest(
    val id: String? = null,
    val useCaseId: String,
    val packageTitle: String,
    val priceLabel: String,
    val kind: String = "REPORT",
    val linkedReportOrderId: String? = null,
    val guruTier: String? = null,
    val propertyId: String? = null,
    val propertyLabel: String? = null,
    val propertySubmittedAt: Long? = null,
    val buyerDifferentFromUser: Boolean? = null,
    val buyerFullName: String? = null,
    val buyerDateOfBirth: String? = null,
    val status: String,
    val createdAt: Long? = null,
    val lastUpdatedAt: Long? = null,
)

data class LinkPropertyRequest(
    val propertyId: String,
    val buyerDifferentFromUser: Boolean? = null,
    val buyerFullName: String? = null,
    val buyerDateOfBirth: String? = null,
)

data class CustomerOrderResponse(
    val id: String,
    val userId: String,
    val useCaseId: String,
    val packageTitle: String,
    val priceLabel: String,
    val kind: String,
    val guruTier: String? = null,
    val upgradedFromOrderId: String? = null,
    val linkedReportOrderId: String? = null,
    val upgradeEligible: Boolean? = null,
    val assignedGurujiId: String? = null,
    val assignedGurujiName: String? = null,
    val propertyId: String? = null,
    val propertyLabel: String? = null,
    val propertySubmittedAt: Long? = null,
    val buyerDifferentFromUser: Boolean? = null,
    val buyerFullName: String? = null,
    val buyerDateOfBirth: String? = null,
    val status: String,
    val publishedReportId: String? = null,
    val reportStatus: String? = null,
    val reportPdfUrl: String? = null,
    val reportDeliveredAt: Long? = null,
    val reportExpiresAt: Long? = null,
    val createdAt: Long,
    val lastUpdatedAt: Long,
)

data class DeliverReportRequest(
    val pdfUrl: String,
)

data class ReportAccessUrlResponse(
    val url: String,
    val mode: String? = null,
    val expiresAt: Long? = null,
)

data class SubmitGuruRatingRequest(
    val rating: Int,
)

data class GuruRatingResponse(
    val orderId: String? = null,
    val gurujiId: String? = null,
    val rating: Int? = null,
    val createdAt: Long? = null,
    val updatedAt: Long? = null,
)
