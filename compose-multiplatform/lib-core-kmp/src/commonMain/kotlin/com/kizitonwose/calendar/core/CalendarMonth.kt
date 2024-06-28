package com.kizitonwose.calendar.core

class CalendarMonth(
    override val yearMonth: YearMonth,
    override val weekDays: List<List<CalendarDay3>>,
) : CalendarMonthWithDays<YearMonth, CalendarDay3>(
    yearMonth = yearMonth,
    weekDays = weekDays,
)
