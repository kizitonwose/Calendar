package com.kizitonwose.calendarview.model

import org.threeten.bp.YearMonth
import java.io.Serializable

data class CalendarMonth(
    val yearMonth: YearMonth,
    val weekDays: List<List<CalendarDay>>,
    internal val indexInSameMonth: Int,
    internal val numberOfSameMonth: Int
) : Comparable<CalendarMonth>, Serializable {

    val year: Int = yearMonth.year
    val month: Int = yearMonth.monthValue

    override fun hashCode(): Int {
        return 31 * yearMonth.hashCode() +
            weekDays.first().first().hashCode() +
            weekDays.last().last().hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        (other as CalendarMonth)
        return yearMonth == other.yearMonth &&
            weekDays.first().first() == other.weekDays.first().first() &&
            weekDays.last().last() == other.weekDays.last().last()
    }

    override fun compareTo(other: CalendarMonth): Int {
        val monthResult = yearMonth.compareTo(other.yearMonth)
        if (monthResult == 0) { // Same yearMonth
            return indexInSameMonth.compareTo(other.indexInSameMonth)
        }
        return monthResult
    }

    override fun toString(): String {
        return "CalendarMonth { first = ${weekDays.first().first()}, last = ${weekDays.last().last()}} " +
            "indexInSameMonth = $indexInSameMonth, numberOfSameMonth = $numberOfSameMonth"
    }
}
