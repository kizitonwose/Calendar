package com.kizitonwose.calendarview.model

import com.kizitonwose.calendarview.adapter.CalendarConfig
import org.threeten.bp.LocalDate
import org.threeten.bp.YearMonth
import java.io.Serializable


class CalendarMonth internal constructor(val yearMonth: YearMonth, private val config: CalendarConfig) :
    Comparable<CalendarMonth>, Serializable {

    private val year: Int = yearMonth.year
    private val month: Int = yearMonth.month.value

    private val ownedDays: List<CalendarDay> by lazy {
        weekDays.flatten().filter { it.owner == DayOwner.THIS_MONTH }
    }

    internal val weekDays: List<List<CalendarDay>> by lazy {
        val thisMonthDays = (1..yearMonth.lengthOfMonth()).map {
            CalendarDay(LocalDate.of(year, month, it), DayOwner.THIS_MONTH)
        }

        // Group days by week of month
        val weekOfMonthField = config.weekFields.weekOfMonth()
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
                val newRowDates = if (lastDay.owner == DayOwner.THIS_MONTH) {
                    (1..7).map {
                        CalendarDay(LocalDate.of(next.year, next.month, it), DayOwner.NEXT_MONTH)
                    }
                } else {
                    (1..7).map {
                        CalendarDay(LocalDate.of(next.year, next.month, it + lastDay.day), DayOwner.NEXT_MONTH)
                    }
                }
                weekDaysGroup.add(newRowDates)
            } else {
                weekDaysGroup.add(emptyList())
            }
        }
        weekDaysGroup
    }

    internal val ownedDates: List<LocalDate>
        get() = ownedDays.map { it.date }

    val previous: CalendarMonth
        get() = CalendarMonth(yearMonth.minusMonths(1), config)

    val next: CalendarMonth
        get() = CalendarMonth(yearMonth.plusMonths(1), config)


    override fun compareTo(other: CalendarMonth): Int {
        return yearMonth.compareTo(other.yearMonth)
    }
}
