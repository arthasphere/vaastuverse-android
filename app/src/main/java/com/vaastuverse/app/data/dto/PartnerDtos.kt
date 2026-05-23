package com.vaastuverse.app.data.dto

data class DiscoverablePartnerResponse(
    val userId: String,
    val partnerType: String,
    val businessName: String?,
    val displayName: String?,
    val city: String?,
    val kycStatus: String,
    val serviceSummary: String,
)

data class EarningsSummaryResponse(
    val partnerId: String?,
    val month: String?,
    val totalEarnings: Double?,
    val entries: List<EarningsEntryDto>?,
) {
    data class EarningsEntryDto(
        val entryType: String?,
        val amount: Double?,
        val description: String?,
        val createdAt: String?,
    )
}

data class QualityRatingResponse(
    val partnerId: String?,
    val averageRating: Double?,
    val reviewCount: Int?,
)

data class KnowledgeEntryResponse(
    val id: String?,
    val title: String?,
    val status: String?,
)

data class ConflictCaseResponse(
    val id: String?,
    val status: String?,
)

data class ReportSummaryResponse(
    val id: String?,
    val status: String?,
    val customerId: String?,
)

data class BookingResponse(
    val id: String?,
    val status: String?,
)
