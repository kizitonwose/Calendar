package com.kizitonwose.calendar.core.deprecated

import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth

@Deprecated(
    message = "Please use the `firstDay` property instead. " +
        "This is only available for migration purposes.",
    level = DeprecationLevel.ERROR,
    replaceWith = ReplaceWith("firstDay"),
)
/**
 * Returns the first day of the year-month.
 */
public fun YearMonth.atStartOfMonth(): LocalDate = firstDay

@Deprecated(
    message = "Please use the `lastDay` property instead. " +
        "This is only available for migration purposes.",
    level = DeprecationLevel.ERROR,
    replaceWith = ReplaceWith("lastDay"),
)
/**
 * Returns the last day of the year-month.
 */
public fun YearMonth.atEndOfMonth(): LocalDate = lastDay

// public fun YearMonth.lengthOfMonth(): Int = numberOfDays
// public fun YearMonth.atDay(day: Int): LocalDate = onDay(day)
// public fun Year.atDay(dayOfYear: Int): LocalDate = onDay(dayOfYear)
// public fun Year.atMonth(month: Month): YearMonth = onMonth(month)
// public fun Year.atMonthDay(month: Month, day: Int): LocalDate = onMonthDay(month, day)
