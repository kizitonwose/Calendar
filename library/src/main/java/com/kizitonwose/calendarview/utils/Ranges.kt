package com.kizitonwose.calendarview.utils

import java.time.LocalDate

class LocalDateRange(
   first: LocalDate,
   last: LocalDate
) : LocalDateProgression(first, last), ClosedRange<LocalDate> {
    override val endInclusive: LocalDate
        get() = last

    override val start: LocalDate
        get() = first

    override fun isEmpty(): Boolean = first > last

    override fun equals(other: Any?): Boolean {
        if (other !is LocalDateRange) {
            return false
        } else if (isEmpty() && other.isEmpty()) {
            return true
        }

        return first == other.first && last == other.last
    }

    override fun hashCode(): Int =
        if (isEmpty()) -1 else (31 * first.hashCode() + last.hashCode())

    override fun toString(): String = "$first..$last"
}

operator fun LocalDate.rangeTo(other: LocalDate): LocalDateRange {
    return LocalDateRange(this, other)
}
