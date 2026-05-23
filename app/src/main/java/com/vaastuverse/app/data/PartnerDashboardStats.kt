package com.vaastuverse.app.data

data class PartnerDashboardStats(
    val knowledgeEntries: Int = 0,
    val reportsPendingReview: Int = 0,
    val openConflicts: Int = 0,
    val consultationBookings: Int = 0,
    val monthlyEarningsInr: Double = 0.0,
    val qualityRating: Double? = null,
    val reviewCount: Int = 0,
) {
    val reportsInfluenced: Int
        get() = reportsPendingReview + knowledgeEntries

    fun formattedMonthlyEarnings(): String {
        if (monthlyEarningsInr <= 0.0) return "₹0"
        return if (monthlyEarningsInr >= 1000) {
            "₹${"%.1f".format(monthlyEarningsInr / 1000)}K"
        } else {
            "₹${monthlyEarningsInr.toInt()}"
        }
    }
}
