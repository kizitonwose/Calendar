package com.kizitonwose.calendarcompose.shared

import java.time.DayOfWeek
import java.time.YearMonth
import java.time.temporal.ChronoUnit

// E.g DayOfWeek.SATURDAY.daysUntil(DayOfWeek.TUESDAY) = 3
internal fun DayOfWeek.daysUntil(other: DayOfWeek) = (7 + (other.value - value)) % 7

internal fun getMonthIndicesCount(startMonth: YearMonth, endMonth: YearMonth): Int {
    // Add one to include the start month itself!
    return ChronoUnit.MONTHS.between(startMonth, endMonth).toInt() + 1
}
