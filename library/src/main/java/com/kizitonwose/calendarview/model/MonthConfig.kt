package com.kizitonwose.calendarview.model

import com.kizitonwose.calendarview.utils.next
import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalDate
import org.threeten.bp.YearMonth
import org.threeten.bp.temporal.WeekFields

internal data class MonthConfig(
    val outDateStyle: OutDateStyle,
    val inDateStyle: InDateStyle,
    val maxRowCount: Int,
    val startMonth: YearMonth,
    val endMonth: YearMonth,
    val firstDayOfWeek: DayOfWeek,
    val hasBoundaries: Boolean
) {

    internal val months: List<CalendarMonth> by lazy lazy@{
        return@lazy if (hasBoundaries) {
            generateBoundedMonths(
                startMonth, endMonth, firstDayOfWeek,
                maxRowCount, inDateStyle, outDateStyle
            )
        } else {
            generateUnboundedMonths(
                startMonth, endMonth, firstDayOfWeek,
                maxRowCount, inDateStyle, outDateStyle
            )
        }
    }

    internal companion object {

        /**
         * A [YearMonth] will have multiple [CalendarMonth] instances if the [maxRowCount] is
         * less than 6. Each [CalendarMonth] will hold just enough [CalendarDay] instances(weekDays)
         * to fit in the [maxRowCount].
         */
        internal fun generateBoundedMonths(
            startMonth: YearMonth,
            endMonth: YearMonth,
            firstDayOfWeek: DayOfWeek,
            maxRowCount: Int,
            inDateStyle: InDateStyle,
            outDateStyle: OutDateStyle
        ): List<CalendarMonth> {
            val months = mutableListOf<CalendarMonth>()
            var nextMonth = startMonth
            while (nextMonth <= endMonth) {
                val generateInDates = when (inDateStyle) {
                    InDateStyle.ALL_MONTHS -> true
                    InDateStyle.FIRST_MONTH -> nextMonth == startMonth
                    InDateStyle.NONE -> false
                }
                val weekDaysGroup =
                    generateWeekDays(nextMonth, firstDayOfWeek, generateInDates, outDateStyle).toMutableList()

                // Group rows by maxRowCount into CalendarMonth classes.
                val calendarMonths = mutableListOf<CalendarMonth>()
                val div = weekDaysGroup.size / maxRowCount
                val rem = weekDaysGroup.size % maxRowCount
                // Add the last month dropped from div if rem is not zero
                val numberOfSameMonth = if (rem == 0) div else div + 1
                while (weekDaysGroup.isNotEmpty()) {
                    val monthDays = weekDaysGroup.take(maxRowCount)
                    calendarMonths.add(CalendarMonth(nextMonth, monthDays, calendarMonths.size, numberOfSameMonth))
                    weekDaysGroup.removeAll(monthDays)
                }

                months.addAll(calendarMonths)

                nextMonth = nextMonth.next
            }

            return months
        }

        internal fun generateUnboundedMonths(
            startMonth: YearMonth,
            endMonth: YearMonth,
            firstDayOfWeek: DayOfWeek,
            maxRowCount: Int,
            inDateStyle: InDateStyle,
            outDateStyle: OutDateStyle
        ): List<CalendarMonth> {

            // Generate a flat list of all days in the given month range
            val allDays = mutableListOf<CalendarDay>()
            var nextMonth = startMonth
            while (nextMonth <= endMonth) {
                val generateInDates = when (inDateStyle) {
                    InDateStyle.ALL_MONTHS -> true
                    InDateStyle.FIRST_MONTH -> nextMonth == startMonth
                    InDateStyle.NONE -> false
                }
                allDays.addAll(generateWeekDays(nextMonth, firstDayOfWeek, generateInDates, outDateStyle).flatten())
                nextMonth = nextMonth.next
            }

            // Regroup data into 7 days.
            val daysGroup = mutableListOf<List<CalendarDay>>()
            while (allDays.isNotEmpty()) {
                val sevenDays = allDays.take(7)
                daysGroup.add(sevenDays)
                allDays.removeAll(sevenDays)
            }

            val calendarMonths = mutableListOf<CalendarMonth>()
            val div = daysGroup.size / maxRowCount
            val rem = daysGroup.size % maxRowCount
            // Add the last month dropped from div if rem is not zero
            val num = if (rem == 0) div else div + 1
            while (daysGroup.isNotEmpty()) {
                val monthDays = daysGroup.take(maxRowCount)
                calendarMonths.add(
                    // numberOfSameMonth is the total number of all months and
                    // indexInSameMonth is basically this item's index in the entire month list.
                    CalendarMonth(startMonth, monthDays, calendarMonths.size, num)
                )
                daysGroup.removeAll(monthDays)
            }

            return calendarMonths
        }

        /**
         * Generates the necessary number of weeks for a [YearMonth].
         */
        internal fun generateWeekDays(
            yearMonth: YearMonth,
            firstDayOfWeek: DayOfWeek,
            generateInDates: Boolean,
            outDateStyle: OutDateStyle
        ): List<List<CalendarDay>> {
            val year = yearMonth.year
            val month = yearMonth.monthValue

            val thisMonthDays = (1..yearMonth.lengthOfMonth()).map {
                CalendarDay(LocalDate.of(year, month, it), DayOwner.THIS_MONTH)
            }.toMutableList()

            val weekDaysGroup = if (generateInDates) {
                // Group days by week of month so we can add the in dates if necessary.
                val weekOfMonthField = WeekFields.of(firstDayOfWeek, 1).weekOfMonth()
                val groupByWeekOfMonth = thisMonthDays.groupBy { it.date.get(weekOfMonthField) }.values.toMutableList()

                // Add in-dates if necessary
                val firstWeek = groupByWeekOfMonth.first()
                if (firstWeek.size < 7) {
                    val previousMonth = yearMonth.minusMonths(1)
                    val inDates = (1..previousMonth.lengthOfMonth()).toList()
                        .takeLast(7 - firstWeek.size).map {
                            CalendarDay(
                                LocalDate.of(previousMonth.year, previousMonth.month, it),
                                DayOwner.PREVIOUS_MONTH
                            )
                        }
                    groupByWeekOfMonth[0] = inDates + firstWeek
                }
                groupByWeekOfMonth
            } else {
                // Group days by 7, first day shown on the month will be day 1.
                val groupBySeven = mutableListOf<List<CalendarDay>>()
                while (thisMonthDays.isNotEmpty()) {
                    val nextRow = thisMonthDays.take(7)
                    groupBySeven.add(nextRow)
                    thisMonthDays.removeAll(nextRow)
                }
                groupBySeven
            }


            if (outDateStyle == OutDateStyle.END_OF_ROW || outDateStyle == OutDateStyle.END_OF_GRID) {
                // Add out-dates for the last row.
                val nextMonth = yearMonth.plusMonths(1)
                val lastWeek = weekDaysGroup.last()
                if (lastWeek.size < 7) {
                    val outDates = (1..7 - lastWeek.size).map {
                        CalendarDay(LocalDate.of(nextMonth.year, nextMonth.month, it), DayOwner.NEXT_MONTH)
                    }
                    weekDaysGroup[weekDaysGroup.lastIndex] = lastWeek + outDates
                }

                // Add more rows to form a 6 x 7 grid
                if (outDateStyle == OutDateStyle.END_OF_GRID) {
                    while (weekDaysGroup.size < 6) {
                        val lastDay = weekDaysGroup.last().last()
                        val nextRowDates = (1..7).map {
                            val dayValue = if (lastDay.owner == DayOwner.THIS_MONTH) it else it + lastDay.day
                            CalendarDay(LocalDate.of(nextMonth.year, nextMonth.month, dayValue), DayOwner.NEXT_MONTH)
                        }
                        weekDaysGroup.add(nextRowDates)
                    }
                }
            }

            return weekDaysGroup
        }
    }
}
