package com.kizitonwose.calendar.core

import java.time.YearMonth as jtYearMonth

public fun YearMonth.toJavaYearMonth(): jtYearMonth = jtYearMonth.of(year, month)

public fun jtYearMonth.toKotlinYearMonth(): YearMonth = YearMonth(year, month)
