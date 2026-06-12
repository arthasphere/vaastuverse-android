package com.vaastuverse.app.data.dto

data class GuruMatchingFeaturesResponse(
    val aiGuruEnabled: Boolean = false,
    val upgradePlanEnabled: Boolean = false,
    val aiGuruId: String = "AI",
    val aiGuruName: String = "AI",
    val aiPriceLabel: String = "₹100",
)
