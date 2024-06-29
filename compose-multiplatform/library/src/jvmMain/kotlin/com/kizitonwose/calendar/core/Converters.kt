package com.kizitonwose.calendar.core

import java.time.YearMonth as jtYearMonth

fun YearMonth.toJavaYearMonth(): jtYearMonth = jtYearMonth.of(year, month)
