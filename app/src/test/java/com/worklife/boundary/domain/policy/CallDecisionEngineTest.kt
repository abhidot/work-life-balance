package com.worklife.boundary.domain.policy

import com.worklife.boundary.domain.schedule.EntryScheduleOverrides
import com.worklife.boundary.domain.schedule.GlobalSchedule
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

class CallDecisionEngineTest {

    private val engine = CallDecisionEngine()
    private val zone = ZoneId.systemDefault()

    private val global = GlobalSchedule(
        activeDays = DayOfWeek.entries.toSet(),
        startTime = LocalTime.of(9, 0),
        endTime = LocalTime.of(18, 0),
    )

    private val entry = BlocklistEntry(
        id = 1L,
        displayName = "Boss",
        contactId = null,
        enabled = true,
        numbers = listOf("+14155552671"),
        scheduleOverrides = EntryScheduleOverrides(),
    )

    @Test
    fun end_to_end_blocks_outside_window() {
        val now = ZonedDateTime.of(LocalDate.now(), LocalTime.of(22, 0), zone)
        val decision = engine.evaluate(
            CallDecisionEngine.Input(
                incomingNumber = "+14155552671",
                now = now,
                masterBlockingEnabled = true,
                globalSchedule = global,
                entries = listOf(entry),
            ),
        )
        assertTrue(decision.shouldBlock)
    }

    @Test
    fun end_to_end_allows_unknown_caller() {
        val now = ZonedDateTime.of(LocalDate.now(), LocalTime.of(22, 0), zone)
        val decision = engine.evaluate(
            CallDecisionEngine.Input(
                incomingNumber = "+14155559999",
                now = now,
                masterBlockingEnabled = true,
                globalSchedule = global,
                entries = listOf(entry),
            ),
        )
        assertFalse(decision.shouldBlock)
    }
}
