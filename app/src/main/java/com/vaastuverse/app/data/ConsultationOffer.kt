package com.vaastuverse.app.data

enum class ConsultationOffer(
    val apiType: String,
    val label: String,
    val subtitle: String,
    val priceLabel: String,
    val durationMinutes: Int,
) {
    QUICK(
        apiType = "QUICK",
        label = "Quick consultation",
        subtitle = "20-minute call with Guruji",
        priceLabel = "₹499",
        durationMinutes = 20,
    ),
    FULL(
        apiType = "FULL",
        label = "Full consultation",
        subtitle = "60-minute deep-dive with Guruji",
        priceLabel = "₹1,399",
        durationMinutes = 60,
    ),
}
