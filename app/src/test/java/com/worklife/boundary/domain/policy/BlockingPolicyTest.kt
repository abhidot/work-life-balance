package com.worklife.boundary.domain.policy

import com.worklife.boundary.domain.schedule.EntryScheduleOverrides
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BlockingPolicyTest {

    private val policy = BlockingPolicy()

    private val entry = BlocklistEntry(
        id = 1L,
        displayName = "Manager",
        contactId = "42",
        enabled = true,
        numbers = listOf("+14155552671"),
        scheduleOverrides = EntryScheduleOverrides(),
    )

    @Test
    fun does_not_block_when_master_blocking_off() {
        assertFalse(
            policy.shouldBlock(
                BlockingContext(masterBlockingEnabled = false, matchedEntry = entry, scheduleAllowsCall = false),
            ),
        )
    }

    @Test
    fun does_not_block_when_no_matching_entry() {
        assertFalse(
            policy.shouldBlock(
                BlockingContext(masterBlockingEnabled = true, matchedEntry = null, scheduleAllowsCall = false),
            ),
        )
    }

    @Test
    fun does_not_block_when_entry_disabled() {
        assertFalse(
            policy.shouldBlock(
                BlockingContext(
                    masterBlockingEnabled = true,
                    matchedEntry = entry.copy(enabled = false),
                    scheduleAllowsCall = false,
                ),
            ),
        )
    }

    @Test
    fun does_not_block_when_inside_allowed_schedule() {
        assertFalse(
            policy.shouldBlock(
                BlockingContext(masterBlockingEnabled = true, matchedEntry = entry, scheduleAllowsCall = true),
            ),
        )
    }

    @Test
    fun blocks_when_master_on_entry_enabled_and_outside_schedule() {
        assertTrue(
            policy.shouldBlock(
                BlockingContext(masterBlockingEnabled = true, matchedEntry = entry, scheduleAllowsCall = false),
            ),
        )
    }
}
