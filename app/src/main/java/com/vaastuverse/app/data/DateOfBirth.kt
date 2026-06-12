package com.vaastuverse.app.data

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

private val ISO = DateTimeFormatter.ISO_LOCAL_DATE
private val DISPLAY = DateTimeFormatter.ofPattern("dd/MM/yyyy")

/** Parses DD/MM/YYYY or YYYY-MM-DD into ISO yyyy-MM-dd for the API. */
fun parseDateOfBirthInput(raw: String): String? {
    val trimmed = raw.trim()
    if (trimmed.isBlank()) return null
    return runCatching { LocalDate.parse(trimmed, DISPLAY).format(ISO) }
        .recoverCatching { LocalDate.parse(trimmed, ISO).format(ISO) }
        .getOrNull()
}

fun formatDateOfBirthForDisplay(iso: String?): String {
    if (iso.isNullOrBlank()) return ""
    return runCatching { LocalDate.parse(iso, ISO).format(DISPLAY) }
        .getOrElse { iso }
}

fun isValidDateOfBirthInput(raw: String): Boolean = parseDateOfBirthInput(raw) != null
