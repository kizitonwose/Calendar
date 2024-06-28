package com.kizitonwose.calendar.core

fun YearMonth.toJavaYearMonth() = java.time.YearMonth.of(year, month)
