package com.vaastuverse.app.data

enum class GuruTier {
    AI,
    GURUJI_VALIDATED,
}

fun GuruTier.isAi(): Boolean = this == GuruTier.AI
