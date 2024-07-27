package com.kizitonwose.calendar.data

import java.time.DayOfWeek
import java.time.YearMonth
import java.time.temporal.WeekFields

fun YearMonth.weeksInMonth(firstDayOfWeek: DayOfWeek) = atEndOfMonth().get(WeekFields.of(firstDayOfWeek, 1).weekOfMonth())
