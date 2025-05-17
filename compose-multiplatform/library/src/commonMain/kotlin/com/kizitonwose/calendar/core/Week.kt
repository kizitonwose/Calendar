package com.kizitonwose.calendar.core

import androidx.compose.runtime.Immutable

/**
 * Represents a week on the week-based calendar.
 *
 * @param days the days in this week.
 */
@Immutable
@ConsistentCopyVisibility
public data class Week internal constructor(val days: List<WeekDay>) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as Week

        if (days.first() != other.days.first()) return false
        if (days.last() != other.days.last()) return false

        return true
    }

    override fun hashCode(): Int {
        var result = days.first().hashCode()
        result = 31 * result + days.last().hashCode()
        return result
    }

    override fun toString(): String {
        return "Week { " +
            "first = ${days.first()}, " +
            "last = ${days.last()} " +
            "} "
    }
}
