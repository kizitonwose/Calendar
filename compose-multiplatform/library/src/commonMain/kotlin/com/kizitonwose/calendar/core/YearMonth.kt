package com.kizitonwose.calendar.core

import androidx.compose.runtime.Immutable
import kotlinx.datetime.Month
import kotlinx.datetime.number

@Immutable
data class YearMonth(val year: Int, val month: Month) : Comparable<YearMonth>, JvmSerializable {
    constructor(year: Int, monthNumber: Int) :
        this(year = year, month = Month(monthNumber))

    init {
        try {
            atStartOfMonth()
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("Year value $year is out of range", e)
        }
    }

    /**
     * Same as java.time.YearMonth.compareTo()
     */
    override fun compareTo(other: YearMonth): Int {
        var cmp = (year - other.year)
        if (cmp == 0) {
            cmp = (month.number - other.month.number)
        }
        return cmp
    }

    companion object
}
