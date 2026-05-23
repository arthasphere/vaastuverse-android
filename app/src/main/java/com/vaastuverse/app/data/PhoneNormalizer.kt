package com.vaastuverse.app.data

object PhoneNormalizer {
    /**
     * Normalizes to 10-digit Indian mobile (6–9 prefix).
     * Strips +91, leading 0, spaces, dashes.
     */
    fun normalizeIndianMobile(input: String): String {
        var digits = input.filter { it.isDigit() }
        when {
            digits.length == 12 && digits.startsWith("91") -> digits = digits.drop(2)
            digits.length == 11 && digits.startsWith("0") -> digits = digits.drop(1)
        }
        require(digits.length == 10 && digits.first() in '6'..'9') {
            "Enter a valid 10-digit Indian mobile (e.g. 9876543210 or +91 9876543210)"
        }
        return digits
    }
}
