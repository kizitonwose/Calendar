package com.kizitonwose.calendar.core

import androidx.compose.runtime.Immutable

/**
 * Represents a month on the calendar.
 *
 * @param yearMonth the calendar month value.
 * @param weekDays the weeks in this month.
 */
@Immutable
open class CalendarMonthWithDays<YearMonth : Any, CalendarDay : Any> internal constructor(
    open val yearMonth: YearMonth,
    open val weekDays: List<List<CalendarDay>>,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as CalendarMonthWithDays<*, *>

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

class CalendarMonth(
    override val yearMonth: YearMonth,
    override val weekDays: List<List<CalendarDay>>,
) : CalendarMonthWithDays<YearMonth, CalendarDay>(
    yearMonth = yearMonth,
    weekDays = weekDays,
)
