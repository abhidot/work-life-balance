package com.worklife.boundary.data.local

import java.time.DayOfWeek

object DayOfWeekBitmask {
    private val order = listOf(
        DayOfWeek.MONDAY,
        DayOfWeek.TUESDAY,
        DayOfWeek.WEDNESDAY,
        DayOfWeek.THURSDAY,
        DayOfWeek.FRIDAY,
        DayOfWeek.SATURDAY,
        DayOfWeek.SUNDAY,
    )

    fun encode(days: Set<DayOfWeek>): Int =
        days.fold(0) { acc, day -> acc or (1 shl order.indexOf(day)) }

    fun decode(mask: Int): Set<DayOfWeek> =
        order.filterIndexed { index, _ -> mask and (1 shl index) != 0 }.toSet()

    val weekdays: Set<DayOfWeek> = setOf(
        DayOfWeek.MONDAY,
        DayOfWeek.TUESDAY,
        DayOfWeek.WEDNESDAY,
        DayOfWeek.THURSDAY,
        DayOfWeek.FRIDAY,
    )
}
