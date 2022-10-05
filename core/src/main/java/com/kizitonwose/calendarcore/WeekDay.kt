package com.kizitonwose.calendarcore

import java.io.Serializable
import java.time.LocalDate

data class WeekDay(val date: LocalDate, val position: WeekDayPosition) : Serializable {
    val day = date.dayOfMonth
}
