package com.kizitonwose.calendar.core

import androidx.compose.runtime.Immutable
import kotlinx.datetime.YearMonth

/**
 * Represents a month on the calendar.
 *
 * @param yearMonth the calendar month value.
 * @param weekDays the weeks in this month.
 */
@Immutable
@ConsistentCopyVisibility
public data class CalendarMonth internal constructor(
    val yearMonth: YearMonth,
    val weekDays: List<List<CalendarDay>>,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as CalendarMonth

        if (yearMonth != other.yearMonth) return false
        if (weekDays.first().first() != other.weekDays.first().first()) return false
        if (weekDays.last().last() != other.weekDays.last().last()) return false

        return true
    }

    override fun hashCode(): Int {
        var result = yearMonth.hashCode()
        result = 31 * result + weekDays.first().first().hashCode()
        result = 31 * result + weekDays.last().last().hashCode()
        return result
    }

    override fun toString(): String {
        return "CalendarMonth { " +
            "first = ${weekDays.first().first()}, " +
            "last = ${weekDays.last().last()} " +
            "} "
    }
}
