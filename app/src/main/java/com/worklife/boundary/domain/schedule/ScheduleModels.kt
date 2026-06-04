package com.worklife.boundary.domain.schedule

import java.time.DayOfWeek
import java.time.LocalTime

data class GlobalSchedule(
    val activeDays: Set<DayOfWeek>,
    val startTime: LocalTime,
    val endTime: LocalTime,
)

enum class OverrideMode {
    INHERIT_GLOBAL,
    CUSTOM,
}

data class EntryScheduleOverrides(
    val daysMode: OverrideMode = OverrideMode.INHERIT_GLOBAL,
    val customDays: Set<DayOfWeek> = emptySet(),
    val timesMode: OverrideMode = OverrideMode.INHERIT_GLOBAL,
    val customStartTime: LocalTime? = null,
    val customEndTime: LocalTime? = null,
)
