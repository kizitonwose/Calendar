package com.kizitonwose.calendar.data

import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import com.kizitonwose.calendar.compose.CalendarState
import com.kizitonwose.calendar.core.OutDateStyle
import kotlinx.datetime.DayOfWeek

//@RestrictTo(LIBRARY_GROUP)
interface MonthDataProducer<YearMonth, CalendarMonth> {
    fun getMonthData(
        startMonth: YearMonth,
        offset: Int,
        firstDayOfWeek: DayOfWeek,
        outDateStyle: OutDateStyle,
    ): CalendarMonth

    fun getMonthIndex(startMonth: YearMonth, month: YearMonth): Int

    fun getMonthIndicesCount(startMonth: YearMonth, endMonth: YearMonth): Int {
        // Add one to include the start month itself!
        return getMonthIndex(startMonth, endMonth) + 1
    }

    fun CalendarMonth.yearMonth(): YearMonth

    fun isInRange(startMonth: YearMonth, endMonth: YearMonth, month: YearMonth): Boolean

    val saver: Saver<CalendarState<YearMonth, CalendarMonth>, Any>

    @Suppress("UNCHECKED_CAST")
    fun getSaverInstance(): Saver<CalendarState<YearMonth, CalendarMonth>, Any> = listSaver(
        save = {
            listOf(
                it.startMonth,
                it.endMonth,
                it.firstVisibleMonth.yearMonth(),
                it.firstDayOfWeek,
                it.outDateStyle,
                it.listState.firstVisibleItemIndex,
                it.listState.firstVisibleItemScrollOffset,
            )
        },
        restore = {
            CalendarState(
                startMonth = it[0] as YearMonth,
                endMonth = it[1] as YearMonth,
                firstVisibleMonth = it[2] as YearMonth,
                firstDayOfWeek = it[3] as DayOfWeek,
                outDateStyle = it[4] as OutDateStyle,
                visibleItemState = VisibleItemState(
                    firstVisibleItemIndex = it[5] as Int,
                    firstVisibleItemScrollOffset = it[6] as Int,
                ),
                data = this,
            )
        },
    )
}
