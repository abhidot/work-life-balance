package com.worklife.boundary.domain.policy

import com.worklife.boundary.domain.phone.PhoneNormalizer

class CallMatchResolver(
    private val phoneNormalizer: PhoneNormalizer = PhoneNormalizer(defaultRegion = "US"),
) {
    fun resolve(incomingNumber: String, entries: List<BlocklistEntry>): BlocklistEntry? {
        val normalizedIncoming = phoneNormalizer.normalize(incomingNumber) ?: return null
        return entries.firstOrNull { entry ->
            entry.numbers.any { stored -> stored == normalizedIncoming }
        }
    }
}
