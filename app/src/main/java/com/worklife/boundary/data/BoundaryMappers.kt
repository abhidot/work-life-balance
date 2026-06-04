package com.worklife.boundary.data

import com.worklife.boundary.data.local.DayOfWeekBitmask
import com.worklife.boundary.data.local.entity.AppSettingsEntity
import com.worklife.boundary.data.local.entity.BlocklistEntryEntity
import com.worklife.boundary.data.local.entity.BlocklistNumberEntity
import com.worklife.boundary.domain.policy.BlocklistEntry
import com.worklife.boundary.domain.schedule.EntryScheduleOverrides
import com.worklife.boundary.domain.schedule.GlobalSchedule
import com.worklife.boundary.domain.schedule.OverrideMode
import java.time.DayOfWeek
import java.time.LocalTime

object BoundaryMappers {
    fun toGlobalSchedule(entity: AppSettingsEntity): GlobalSchedule = GlobalSchedule(
        activeDays = DayOfWeekBitmask.decode(entity.activeDaysMask),
        startTime = minutesToLocalTime(entity.startMinutes),
        endTime = minutesToLocalTime(entity.endMinutes),
    )

    fun toBlocklistEntry(
        entity: BlocklistEntryEntity,
        numbers: List<BlocklistNumberEntity>,
    ): BlocklistEntry = BlocklistEntry(
        id = entity.id,
        displayName = entity.displayName,
        contactId = entity.contactId,
        enabled = entity.enabled,
        numbers = numbers.map { it.normalizedNumber },
        scheduleOverrides = EntryScheduleOverrides(
            daysMode = entity.daysMode.toOverrideMode(),
            customDays = entity.customDaysMask?.let { DayOfWeekBitmask.decode(it) } ?: emptySet(),
            timesMode = entity.timesMode.toOverrideMode(),
            customStartTime = entity.customStartMinutes?.let { minutesToLocalTime(it) },
            customEndTime = entity.customEndMinutes?.let { minutesToLocalTime(it) },
        ),
    )

    fun defaultSettings(): AppSettingsEntity = AppSettingsEntity(
        masterBlockingEnabled = false,
        onboardingComplete = false,
        activeDaysMask = DayOfWeekBitmask.encode(DayOfWeekBitmask.weekdays),
        startMinutes = localTimeToMinutes(LocalTime.of(9, 0)),
        endMinutes = localTimeToMinutes(LocalTime.of(18, 0)),
    )

    fun localTimeToMinutes(time: LocalTime): Int = time.hour * 60 + time.minute

    fun minutesToLocalTime(minutes: Int): LocalTime =
        LocalTime.of(minutes / 60, minutes % 60)

    private fun String.toOverrideMode(): OverrideMode = when (this) {
        "CUSTOM" -> OverrideMode.CUSTOM
        else -> OverrideMode.INHERIT_GLOBAL
    }

}

fun OverrideMode.toStorage(): String = when (this) {
    OverrideMode.INHERIT_GLOBAL -> "INHERIT"
    OverrideMode.CUSTOM -> "CUSTOM"
}
