package com.kizitonwose.calendar.core

import androidx.compose.runtime.Immutable

/**
 * Represents a year on the calendar.
 *
 * @param year the calendar year value.
 * @param months the months in this year.
 */
@Immutable
public data class CalendarYear(
    val year: Year,
    val months: List<CalendarMonth>,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as CalendarYear

        if (year != other.year) return false
        if (months.first() != other.months.first()) return false
        if (months.last() != other.months.last()) return false

        return true
    }

    override fun hashCode(): Int {
        var result = year.hashCode()
        result = 31 * result + months.first().hashCode()
        result = 31 * result + months.last().hashCode()
        return result
    }

    override fun toString(): String {
        return "CalendarYear { " +
            "year = $year, " +
            "firstMonth = ${months.first()}, " +
            "lastMonth = ${months.last()} " +
            "} "
    }
}
