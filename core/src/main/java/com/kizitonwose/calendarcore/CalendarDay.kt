package com.kizitonwose.calendarcore

import java.io.Serializable
import java.time.LocalDate

data class CalendarDay(val date: LocalDate, val position: DayPosition) : Serializable {
    val day = date.dayOfMonth
}
