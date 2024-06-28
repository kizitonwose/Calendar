package com.kizitonwose.calendar.core

class CalendarMonth(
    override val yearMonth: YearMonth,
    override val weekDays: List<List<CalendarDay>>,
) : CalendarMonthWithDays<YearMonth, CalendarDay>(
    yearMonth = yearMonth,
    weekDays = weekDays,
)
