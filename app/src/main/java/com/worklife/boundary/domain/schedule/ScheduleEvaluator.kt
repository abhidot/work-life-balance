package com.worklife.boundary.domain.schedule

import java.time.DayOfWeek
import java.time.LocalTime
import java.time.ZonedDateTime

class ScheduleEvaluator {

    fun isAllowed(
        now: ZonedDateTime,
        global: GlobalSchedule,
        overrides: EntryScheduleOverrides,
    ): Boolean {
        val activeDays = effectiveDays(global, overrides)
        if (now.dayOfWeek !in activeDays) return false

        val (start, end) = effectiveTimes(global, overrides)
        val current = now.toLocalTime()
        return !current.isBefore(start) && current.isBefore(end)
    }

    private fun effectiveDays(
        global: GlobalSchedule,
        overrides: EntryScheduleOverrides,
    ): Set<DayOfWeek> = when (overrides.daysMode) {
        OverrideMode.INHERIT_GLOBAL -> global.activeDays
        OverrideMode.CUSTOM -> overrides.customDays
    }

    private fun effectiveTimes(
        global: GlobalSchedule,
        overrides: EntryScheduleOverrides,
    ): Pair<LocalTime, LocalTime> = when (overrides.timesMode) {
        OverrideMode.INHERIT_GLOBAL -> global.startTime to global.endTime
        OverrideMode.CUSTOM -> {
            val start = overrides.customStartTime ?: global.startTime
            val end = overrides.customEndTime ?: global.endTime
            start to end
        }
    }
}
