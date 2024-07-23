package com.kizitonwose.calendar.core

import java.time.Year as jtYear
import java.time.YearMonth as jtYearMonth

public fun YearMonth.toJavaYearMonth(): jtYearMonth = jtYearMonth.of(year, month)

public fun jtYearMonth.toKotlinYearMonth(): YearMonth = YearMonth(year, month)

public fun Year.toJavaYear(): jtYear = jtYear.of(year)

public fun jtYear.toKotlinYear(): Year = Year(value)
