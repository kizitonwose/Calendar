@file:Suppress("NewApi")

package com.kizitonwose.calendar.core

import kotlinx.datetime.toJavaMonth
import kotlinx.datetime.toKotlinMonth
import java.time.Year as JavaYear
import java.time.YearMonth as JavaYearMonth

public fun YearMonth.toJavaYearMonth(): JavaYearMonth = JavaYearMonth.of(year, month.toJavaMonth())

public fun JavaYearMonth.toKotlinYearMonth(): YearMonth = YearMonth(year, month.toKotlinMonth())

public fun Year.toJavaYear(): JavaYear = JavaYear.of(value)

public fun JavaYear.toKotlinYear(): Year = Year(value)
