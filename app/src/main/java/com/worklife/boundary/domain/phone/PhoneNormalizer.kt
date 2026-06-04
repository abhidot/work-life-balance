package com.worklife.boundary.domain.phone

/**
 * Lightweight E.164-style normalizer (no libphonenumber metadata — saves several MB in APK).
 */
class PhoneNormalizer(
    private val defaultRegion: String = "US",
) {
    fun normalize(raw: String): String? {
        val trimmed = raw.trim()
        if (trimmed.isEmpty()) return null

        val digits = trimmed.filter { it.isDigit() }
        if (digits.length < 7) return null

        val e164 = when {
            trimmed.startsWith("+") -> "+$digits"
            defaultRegion == "US" && digits.length == 10 -> "+1$digits"
            defaultRegion == "US" && digits.length == 11 && digits.startsWith("1") -> "+$digits"
            digits.length >= 10 -> "+$digits"
            else -> return null
        }
        return e164
    }
}
