package com.kizitonwose.calendar.core.format

import com.kizitonwose.calendar.core.Year
import com.kizitonwose.calendar.core.YearMonth
import com.kizitonwose.calendar.core.atMonth
import com.kizitonwose.calendar.core.atStartOfMonth
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format.char

internal val ISO_YEAR_MONTH by lazy {
    LocalDate.Format { year(); char('-'); monthNumber() }
}

internal val ISO_YEAR by lazy {
    LocalDate.Format { year() }
}

internal fun Year.toIso8601String() = ISO_YEAR.format(atMonth(1).atStartOfMonth())

internal fun YearMonth.toIso8601String() = ISO_YEAR_MONTH.format(atStartOfMonth())
