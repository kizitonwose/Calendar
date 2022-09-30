package com.kizitonwose.calendarcompose

import java.io.Serializable
import java.time.LocalDate
import java.time.Month
import java.time.YearMonth

enum class OutDateStyle {
    EndOfRow, EndOfGrid
}

enum class DayPosition {
    InDate, MonthDate, OutDate
}

data class CalendarDay(val date: LocalDate, val position: DayPosition) : Serializable

data class CalendarMonth internal constructor(
    val yearMonth: YearMonth,
    val weekDays: List<List<CalendarDay>>,
) : Serializable {
    val year: Int = yearMonth.year
    val month: Month = yearMonth.month
    val monthValue: Int = yearMonth.monthValue

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

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
