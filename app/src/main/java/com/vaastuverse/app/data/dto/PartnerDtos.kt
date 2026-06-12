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
    val id: String? = null,
    val customerId: String? = null,
    val gurujiId: String? = null,
    val status: String? = null,
    val consultationType: String? = null,
    val scheduledAt: String? = null,
    val amount: Double? = null,
    val durationMinutes: Int? = null,
)

data class GurujiAvailabilityResponse(
    val id: Long? = null,
    val gurujiId: String? = null,
    val dayOfWeek: Int? = null,
    val slotDate: String? = null,
    val startTime: String? = null,
    val endTime: String? = null,
    val active: Boolean? = null,
)

data class AvailabilitySlotRequest(
    val date: String,
    val startTime: String,
    val endTime: String,
)

data class TwoWeekAvailabilityRequest(
    val slots: List<AvailabilitySlotRequest>,
)

data class BookConsultationRequest(
    val gurujiId: String,
    val consultationType: String,
    val scheduledAt: String,
    val reportId: String? = null,
)
