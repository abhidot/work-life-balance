package com.worklife.boundary.domain.schedule

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

class ScheduleEvaluatorTest {

    private val evaluator = ScheduleEvaluator()
    private val zone = ZoneId.of("America/New_York")

    private val weekdayGlobal = GlobalSchedule(
        activeDays = setOf(
            DayOfWeek.MONDAY,
            DayOfWeek.TUESDAY,
            DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY,
            DayOfWeek.FRIDAY,
        ),
        startTime = LocalTime.of(9, 0),
        endTime = LocalTime.of(18, 0),
    )

    @Test
    fun allows_when_now_is_inside_global_window_on_active_day() {
        val now = ZonedDateTime.of(LocalDate.of(2025, 6, 4), LocalTime.of(10, 0), zone) // Wed
        assertTrue(evaluator.isAllowed(now, weekdayGlobal, EntryScheduleOverrides()))
    }

    @Test
    fun blocks_when_now_is_outside_global_time_on_active_day() {
        val now = ZonedDateTime.of(LocalDate.of(2025, 6, 4), LocalTime.of(20, 0), zone)
        assertFalse(evaluator.isAllowed(now, weekdayGlobal, EntryScheduleOverrides()))
    }

    @Test
    fun blocks_when_today_is_not_in_global_active_days() {
        val now = ZonedDateTime.of(LocalDate.of(2025, 6, 7), LocalTime.of(10, 0), zone) // Sat
        assertFalse(evaluator.isAllowed(now, weekdayGlobal, EntryScheduleOverrides()))
    }

    @Test
    fun uses_custom_days_when_overridden() {
        val now = ZonedDateTime.of(LocalDate.of(2025, 6, 7), LocalTime.of(10, 0), zone) // Sat
        val overrides = EntryScheduleOverrides(
            daysMode = OverrideMode.CUSTOM,
            customDays = setOf(DayOfWeek.SATURDAY),
        )
        assertTrue(evaluator.isAllowed(now, weekdayGlobal, overrides))
    }

    @Test
    fun uses_custom_times_when_overridden() {
        val now = ZonedDateTime.of(LocalDate.of(2025, 6, 4), LocalTime.of(11, 0), zone)
        val overrides = EntryScheduleOverrides(
            timesMode = OverrideMode.CUSTOM,
            customStartTime = LocalTime.of(10, 0),
            customEndTime = LocalTime.of(10, 30),
        )
        assertTrue(evaluator.isAllowed(now, weekdayGlobal, overrides))
    }

    @Test
    fun blocks_when_outside_custom_time_window() {
        val now = ZonedDateTime.of(LocalDate.of(2025, 6, 4), LocalTime.of(11, 0), zone)
        val overrides = EntryScheduleOverrides(
            timesMode = OverrideMode.CUSTOM,
            customStartTime = LocalTime.of(10, 0),
            customEndTime = LocalTime.of(10, 30),
        )
        assertFalse(evaluator.isAllowed(now, weekdayGlobal, overrides))
    }
}
