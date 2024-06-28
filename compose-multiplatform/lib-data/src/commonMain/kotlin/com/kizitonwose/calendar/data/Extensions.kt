package com.kizitonwose.calendar.data

import com.kizitonwose.calendar.core.YearMonth
import com.kizitonwose.calendar.core.atStartOfMonth
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.monthsUntil

internal fun YearMonth.monthsUntil(other: YearMonth): Int =
    atStartOfMonth().monthsUntil(other.atStartOfMonth())

// E.g DayOfWeek.SATURDAY.daysUntil(DayOfWeek.TUESDAY) = 3
internal fun DayOfWeek.daysUntil(other: DayOfWeek) = (7 + (other.isoDayNumber - isoDayNumber)) % 7
