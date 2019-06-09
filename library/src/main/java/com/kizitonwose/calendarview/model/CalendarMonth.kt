package com.kizitonwose.calendarview.model

import com.kizitonwose.calendarview.ui.CalendarConfig
import com.kizitonwose.calendarview.utils.next
import com.kizitonwose.calendarview.utils.previous
import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalDate
import org.threeten.bp.YearMonth
import org.threeten.bp.temporal.WeekFields
import java.io.Serializable

data class CalendarSubMonth(val yearMonth: YearMonth, val weekDays: List<List<CalendarDay>>, val indexInMonth: Int)

object CalendarMonthGenerator {

    fun generateMonths(
        startMonth: YearMonth,
        endMonth: YearMonth,
        firstDayOfWeek: DayOfWeek,
        config: CalendarConfig
    ): List<CalendarSubMonth> {
        val months = mutableListOf<CalendarSubMonth>()
        var lastMonth = startMonth
        while (lastMonth <= endMonth) {
            months.addAll(generateSubMonths(lastMonth, firstDayOfWeek, config))
            lastMonth = lastMonth.next
        }
        return months
    }

    fun generateSubMonths(
        yearMonth: YearMonth,
        firstDayOfWeek: DayOfWeek,
        config: CalendarConfig
    ): List<CalendarSubMonth> {
        val year = yearMonth.year
        val month = yearMonth.monthValue

        val thisMonthDays = (1..yearMonth.lengthOfMonth()).map {
            CalendarDay(LocalDate.of(year, month, it), DayOwner.THIS_MONTH)
        }

        // Group days by week of month
        val weekOfMonthField = WeekFields.of(firstDayOfWeek, 1).weekOfMonth()
        val weekDaysGroup = thisMonthDays.groupBy { it.date.get(weekOfMonthField) }.values.toMutableList()

        // Add in-dates if necessary
        val firstWeek = weekDaysGroup.first()
        if (firstWeek.size < 7) {
            val previousMonth = yearMonth.minusMonths(1)
            val inDates = (1..previousMonth.lengthOfMonth()).toList()
                .takeLast(7 - firstWeek.size).map {
                    CalendarDay(LocalDate.of(previousMonth.year, previousMonth.month, it), DayOwner.PREVIOUS_MONTH)
                }
            weekDaysGroup[0] = inDates + firstWeek
        }

        // Add out-dates if necessary.
        val nextMonth = yearMonth.plusMonths(1)
        val lastWeek = weekDaysGroup.last()
        if (lastWeek.size < 7) {
            val outDates = (1..7 - lastWeek.size).map {
                CalendarDay(LocalDate.of(nextMonth.year, nextMonth.month, it), DayOwner.NEXT_MONTH)
            }
            weekDaysGroup[weekDaysGroup.lastIndex] = lastWeek + outDates
        }

        // Ensure we have a representation of all 6 week rows
        while (weekDaysGroup.size < 6) {
            if (config.outDateStyle == OutDateStyle.END_OF_GRID) {
                val lastDay = weekDaysGroup.last().last()
                val nextRowDates = (1..7).map {
                    val dayValue = if (lastDay.owner == DayOwner.THIS_MONTH) it else it + lastDay.day
                    CalendarDay(LocalDate.of(nextMonth.year, nextMonth.month, dayValue), DayOwner.NEXT_MONTH)
                }
                weekDaysGroup.add(nextRowDates)
            } else {
                weekDaysGroup.add(emptyList())
            }
        }


        val months = mutableListOf<CalendarSubMonth>()
        var startIndex = 0
        val maxRowCount = config.maxRowCount
        while (startIndex < 6) {
            val monthDays = weekDaysGroup.subList(startIndex, maxRowCount)
            months.add(CalendarSubMonth(yearMonth, monthDays, months.count()))
            startIndex += maxRowCount
        }
        return months
    }
}


class CalendarMonth internal constructor(
    val yearMonth: YearMonth,
    private val config: CalendarConfig,
    private val firstDayOfWeek: DayOfWeek
) : Comparable<CalendarMonth>, Serializable {

    val year: Int = yearMonth.year
    val month: Int = yearMonth.monthValue

    private val ownedDays: List<CalendarDay> by lazy {
        weekDays.flatten().filter { it.owner == DayOwner.THIS_MONTH }
    }

    internal val weekDays: List<List<CalendarDay>> by lazy {
        val thisMonthDays = (1..yearMonth.lengthOfMonth()).map {
            CalendarDay(LocalDate.of(year, month, it), DayOwner.THIS_MONTH)
        }

        // Group days by week of month
        val weekOfMonthField = WeekFields.of(firstDayOfWeek, 1).weekOfMonth()
        val weekDaysGroup = thisMonthDays.groupBy { it.date.get(weekOfMonthField) }.values.toMutableList()

        // Add in-dates if necessary
        val firstWeek = weekDaysGroup.first()
        if (firstWeek.size < 7) {
            val previousMonth = yearMonth.minusMonths(1)
            val inDates = (1..previousMonth.lengthOfMonth()).toList()
                .takeLast(7 - firstWeek.size).map {
                    CalendarDay(LocalDate.of(previous.year, previous.month, it), DayOwner.PREVIOUS_MONTH)
                }
            weekDaysGroup[0] = inDates + firstWeek
        }

        // Add out-dates if necessary.
        val lastWeek = weekDaysGroup.last()
        if (lastWeek.size < 7) {
            val outDates = (1..7 - lastWeek.size).map {
                CalendarDay(LocalDate.of(next.year, next.month, it), DayOwner.NEXT_MONTH)
            }
            weekDaysGroup[weekDaysGroup.lastIndex] = lastWeek + outDates
        }

        // Ensure we have a representation of all 6 week rows
        while (weekDaysGroup.size < 6) {
            if (config.outDateStyle == OutDateStyle.END_OF_GRID) {
                val lastDay = weekDaysGroup.last().last()
                val nextRowDates = (1..7).map {
                    val dayValue = if (lastDay.owner == DayOwner.THIS_MONTH) it else it + lastDay.day
                    CalendarDay(LocalDate.of(next.year, next.month, dayValue), DayOwner.NEXT_MONTH)
                }
                weekDaysGroup.add(nextRowDates)
            } else {
                weekDaysGroup.add(emptyList())
            }
        }
        weekDaysGroup
    }

    internal val ownedDates: List<LocalDate>
        get() = ownedDays.map { it.date }

    val previous: CalendarMonth
        get() = CalendarMonth(yearMonth.previous, config, firstDayOfWeek)

    val next: CalendarMonth
        get() = CalendarMonth(yearMonth.next, config, firstDayOfWeek)


    override fun hashCode(): Int = yearMonth.hashCode()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        return yearMonth == (other as CalendarMonth).yearMonth
    }

    override fun compareTo(other: CalendarMonth): Int = yearMonth.compareTo(other.yearMonth)
}
