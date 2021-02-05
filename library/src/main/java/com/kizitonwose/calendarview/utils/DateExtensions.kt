@file:Suppress("NOTHING_TO_INLINE")

package com.kizitonwose.calendarview.utils

import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.util.*

inline fun LocalDate.isSameYearMonth(rhs: YearMonth): Boolean {
    val sameYear = this.year == rhs.year
    val sameMonth = this.monthValue == rhs.monthValue
    return sameMonth && sameYear
}

inline fun Date.isSameYearMonth(rhs: YearMonth): Boolean {
    return this.toCalendar().isSameYearMonth(rhs)
}

inline fun Date.isSameDay(rhs: Date): Boolean {
    return this.toCalendar().isSameDay(rhs.toCalendar())
}

inline fun Calendar.isSameDay(rhs: Calendar): Boolean {
    val sameYear = this.get(Calendar.YEAR) == rhs.get(Calendar.YEAR)
    val sameMonth = this.get(Calendar.MONTH) == rhs.get(Calendar.MONTH)
    val sameDay = this.get(Calendar.DAY_OF_MONTH) == rhs.get(Calendar.DAY_OF_MONTH)
    return sameDay && sameMonth && sameYear
}

inline fun Calendar.isSameYearMonth(rhs: YearMonth): Boolean {
    val sameYear = this.get(Calendar.YEAR) == rhs.year
    val sameMonth = this.get(Calendar.MONTH) == rhs.monthValue
    return sameMonth && sameYear
}

inline fun Date.toCalendar(): Calendar {
    return Calendar.getInstance().apply { time = this@toCalendar }
}

inline fun Date.toLocalDate(): LocalDate {
    return Instant.ofEpochMilli(time)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
}