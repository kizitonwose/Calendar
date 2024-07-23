package com.kizitonwose.calendar.core.format

import com.kizitonwose.calendar.core.Year
import com.kizitonwose.calendar.core.YearMonth
import com.kizitonwose.calendar.core.atMonth
import com.kizitonwose.calendar.core.atStartOfMonth
import com.kizitonwose.calendar.core.yearMonth
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format.char

internal val ISO_YEAR_MONTH by lazy {
    LocalDate.Format { year(); char('-'); monthNumber() }
}

internal val ISO_YEAR by lazy {
    LocalDate.Format { year() }
}

internal fun LocalDate.toIso8601String() = LocalDate.Formats.ISO.format(this)

internal fun YearMonth.toIso8601String() = ISO_YEAR_MONTH.format(atStartOfMonth())

internal fun Year.toIso8601String() = ISO_YEAR.format(atMonth(1).atStartOfMonth())

internal fun String.fromIso8601LocalDate(): LocalDate =
    LocalDate.parse(this, LocalDate.Formats.ISO)

internal fun String.fromIso8601YearMonth(): YearMonth =
    LocalDate.parse("$this-01", LocalDate.Formats.ISO).yearMonth

internal fun String.fromIso8601Year(): Year =
    Year(LocalDate.parse("$this-01-01", LocalDate.Formats.ISO).year)
