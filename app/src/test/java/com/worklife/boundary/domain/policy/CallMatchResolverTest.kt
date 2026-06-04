package com.worklife.boundary.domain.policy

import com.worklife.boundary.domain.schedule.EntryScheduleOverrides
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CallMatchResolverTest {

    private val resolver = CallMatchResolver()

    @Test
    fun resolves_entry_when_incoming_matches_any_number() {
        val entry = sampleEntry(id = 1L, numbers = listOf("+14155552671", "+14155550002"))
        val match = resolver.resolve("+1 (415) 555-2671", listOf(entry))
        assertEquals(entry, match)
    }

    @Test
    fun returns_null_when_no_match() {
        val entry = sampleEntry(id = 1L, numbers = listOf("+14155552671"))
        assertNull(resolver.resolve("+14155559999", listOf(entry)))
    }

    @Test
    fun returns_null_for_invalid_incoming() {
        val entry = sampleEntry(id = 1L, numbers = listOf("+14155552671"))
        assertNull(resolver.resolve("invalid", listOf(entry)))
    }

    private fun sampleEntry(id: Long, numbers: List<String>) = BlocklistEntry(
        id = id,
        displayName = "Alex",
        contactId = null,
        enabled = true,
        numbers = numbers,
        scheduleOverrides = EntryScheduleOverrides(),
    )
}
