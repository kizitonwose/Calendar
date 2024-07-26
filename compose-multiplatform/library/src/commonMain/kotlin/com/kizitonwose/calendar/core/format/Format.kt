package com.kizitonwose.calendar.core.format

import com.kizitonwose.calendar.core.Year
import com.kizitonwose.calendar.core.YearMonth
import com.kizitonwose.calendar.core.atMonth
import com.kizitonwose.calendar.core.atStartOfMonth
import com.kizitonwose.calendar.core.yearMonth
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format.char

private val ISO_YEAR_MONTH by lazy {
    LocalDate.Format {
        year()
        char('-')
        monthNumber()
    }
}

private val ISO_YEAR by lazy {
    LocalDate.Format { year() }
}

private val ISO_LOCAL_DATE by lazy {
    LocalDate.Formats.ISO
}

internal fun LocalDate.toIso8601String() = ISO_LOCAL_DATE.format(this)

internal fun YearMonth.toIso8601String() = ISO_YEAR_MONTH.format(atStartOfMonth())

internal fun Year.toIso8601String() = ISO_YEAR.format(atMonth(1).atStartOfMonth())

internal fun String.fromIso8601LocalDate(): LocalDate =
    LocalDate.parse(this, ISO_LOCAL_DATE)

internal fun String.fromIso8601YearMonth(): YearMonth =
    LocalDate.parse("$this-01", ISO_LOCAL_DATE).yearMonth

internal fun String.fromIso8601Year(): Year =
    Year(LocalDate.parse("$this-01-01", ISO_LOCAL_DATE).year)
