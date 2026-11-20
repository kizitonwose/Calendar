package io.github.ceniorpomidor.workcalendar.domain.time

import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit

/** Inclusive range of dates. */
data class DateRange(val start: LocalDate, val endInclusive: LocalDate) : Iterable<LocalDate> {
    init {
        require(!endInclusive.isBefore(start)) { "Range end $endInclusive is before start $start" }
    }

    operator fun contains(date: LocalDate): Boolean = !date.isBefore(start) && !date.isAfter(endInclusive)

    /** Number of days in the range, both ends included. */
    val days: Int get() = ChronoUnit.DAYS.between(start, endInclusive).toInt() + 1

    fun overlaps(other: DateRange): Boolean = !other.endInclusive.isBefore(start) && !other.start.isAfter(endInclusive)

    fun intersect(other: DateRange): DateRange? {
        if (!overlaps(other)) return null
        val s = if (start.isAfter(other.start)) start else other.start
        val e = if (endInclusive.isBefore(other.endInclusive)) endInclusive else other.endInclusive
        return DateRange(s, e)
    }

    fun dates(): Sequence<LocalDate> = generateSequence(start) { if (it < endInclusive) it.plusDays(1) else null }

    override fun iterator(): Iterator<LocalDate> = dates().iterator()

    override fun toString(): String = "$start..$endInclusive"

    companion object {
        fun of(start: LocalDate, endInclusive: LocalDate): DateRange = DateRange(start, endInclusive)

        fun single(date: LocalDate): DateRange = DateRange(date, date)

        fun month(month: YearMonth): DateRange = DateRange(month.atDay(1), month.atEndOfMonth())

        fun year(year: Int): DateRange = DateRange(LocalDate.of(year, 1, 1), LocalDate.of(year, 12, 31))

        /** Creates a range from two dates in any order. */
        fun between(a: LocalDate, b: LocalDate): DateRange = if (a <= b) DateRange(a, b) else DateRange(b, a)
    }
}

fun YearMonth.toDateRange(): DateRange = DateRange.month(this)

/** Months touched by the range, in order. */
fun DateRange.months(): List<YearMonth> {
    val result = mutableListOf<YearMonth>()
    var m = YearMonth.from(start)
    val last = YearMonth.from(endInclusive)
    while (m <= last) {
        result += m
        m = m.plusMonths(1)
    }
    return result
}
