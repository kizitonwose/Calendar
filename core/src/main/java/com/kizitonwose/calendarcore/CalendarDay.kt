package com.kizitonwose.calendarcore

import java.io.Serializable
import java.time.LocalDate

/**
 * Represents a day on the calendar.
 *
 * @param date the date for this day.
 * @param position the [DayPosition] for this day.
 */
data class CalendarDay(val date: LocalDate, val position: DayPosition) : Serializable