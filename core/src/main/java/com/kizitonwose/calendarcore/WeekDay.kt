package com.kizitonwose.calendarcore

import java.io.Serializable
import java.time.LocalDate

/**
 * Represents a day on the week calendar.
 *
 * @param date the date for this day.
 * @param position the [WeekDayPosition] for this day.
 */
data class WeekDay(val date: LocalDate, val position: WeekDayPosition) : Serializable