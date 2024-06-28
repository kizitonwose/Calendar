package com.kizitonwose.calendar.core

import androidx.compose.runtime.Immutable
import java.time.YearMonth

@Immutable
class CalendarMonth3(
    override val yearMonth: YearMonth,
    override val weekDays: List<List<CalendarDay>>,
) : CalendarMonthWithDays<YearMonth, CalendarDay>(
    yearMonth = yearMonth,
    weekDays = weekDays,
)
