package com.kizitonwose.calendarview.utils

import java.time.LocalDate

open class LocalDateProgression(
    protected val first: LocalDate,
    protected val end: LocalDate
) : Iterable<LocalDate> {

    protected val last: LocalDate = getProgressionLastElement(first, end)

    override fun iterator(): Iterator<LocalDate> = LocalDateProgressionIterator(first, last)

    open fun isEmpty(): Boolean = first > last

    override fun equals(other: Any?): Boolean {
        if (other !is LocalDateProgression) {
            return false
        } else if (isEmpty() && other.isEmpty()) {
            return true
        }

        return first == other.first && last == other.last
    }

    override fun hashCode(): Int {
        return if (isEmpty()) {
            -1
        } else {
            (31 * (31 * first.hashCode() + last.hashCode()))
        }
    }

    override fun toString(): String = "$first..$last"
}

private fun getProgressionLastElement(first: LocalDate, last: LocalDate): LocalDate {
    return if (first <= last) {
        last
    } else {
        first
    }
}
