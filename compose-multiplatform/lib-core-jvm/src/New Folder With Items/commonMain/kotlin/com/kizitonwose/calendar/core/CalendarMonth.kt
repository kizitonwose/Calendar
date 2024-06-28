package com.kizitonwose.calendar.core

import androidx.compose.runtime.Immutable

@Immutable
expect class CalendarMonth : CalendarMonthWithDays<YearMonth, CalendarDay>
expect class YearMonth : Any, Comparable<YearMonth>
