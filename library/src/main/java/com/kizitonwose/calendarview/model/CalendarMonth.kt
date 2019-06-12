package com.kizitonwose.calendarview.model

import org.threeten.bp.YearMonth
import java.io.Serializable

data class CalendarMonth(
    val yearMonth: YearMonth,
    internal val weekDays: List<List<CalendarDay>>,
    internal val indexInSameMonth: Int,
    internal val numberOfSameMonth: Int
) : Comparable<CalendarMonth>, Serializable {

    val year: Int = yearMonth.year
    val month: Int = yearMonth.monthValue

    override fun hashCode(): Int {
        var result = yearMonth.hashCode()
        result = 31 * result + indexInSameMonth
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        (other as CalendarMonth)
        return yearMonth == other.yearMonth && indexInSameMonth == other.indexInSameMonth
    }

    override fun compareTo(other: CalendarMonth): Int {
        val monthResult = yearMonth.compareTo(other.yearMonth)
        if (monthResult == 0) { // Same yearMonth
            return indexInSameMonth.compareTo(other.indexInSameMonth)
        }
        return monthResult
    }

}