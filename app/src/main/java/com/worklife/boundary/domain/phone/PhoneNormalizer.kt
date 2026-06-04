package com.worklife.boundary.domain.phone

import com.google.i18n.phonenumbers.PhoneNumberUtil

class PhoneNormalizer(
    private val defaultRegion: String,
    private val util: PhoneNumberUtil = PhoneNumberUtil.getInstance(),
) {
    fun normalize(raw: String): String? {
        val trimmed = raw.trim()
        if (trimmed.isEmpty()) return null
        return try {
            val parsed = util.parse(trimmed, defaultRegion)
            if (!util.isValidNumber(parsed)) return null
            util.format(parsed, PhoneNumberUtil.PhoneNumberFormat.E164)
        } catch (_: Exception) {
            null
        }
    }
}
