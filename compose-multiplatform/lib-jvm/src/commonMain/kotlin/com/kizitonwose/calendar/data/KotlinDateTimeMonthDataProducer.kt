package com.kizitonwose.calendar.data

import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import com.kizitonwose.calendar.compose.CalendarState
import com.kizitonwose.calendar.core.CalendarMonth
import com.kizitonwose.calendar.core.OutDateStyle
import com.kizitonwose.calendar.core.YearMonth
import kotlinx.datetime.DayOfWeek

internal object KotlinDateTimeMonthDataProducer : MonthDataProducer<YearMonth, CalendarMonth> {
    override fun getCalendarMonthData(
        startMonth: YearMonth,
        offset: Int,
        firstDayOfWeek: DayOfWeek,
        outDateStyle: OutDateStyle,
    ): CalendarMonth {
        return com.kizitonwose.calendar.data.getCalendarMonthData(
            startMonth = startMonth,
            offset = offset,
            firstDayOfWeek = firstDayOfWeek,
            outDateStyle = outDateStyle,
        ).calendarMonth
    }

    override fun getMonthIndex(startMonth: YearMonth, month: YearMonth): Int {
        return com.kizitonwose.calendar.data.getMonthIndex(
            startMonth = startMonth,
            targetMonth = month,
        )
    }

    override val saver: Saver<CalendarState<YearMonth, CalendarMonth>, Any> = listSaver(
        save = {
            listOf(
                it.startMonth,
                it.endMonth,
                it.firstVisibleMonth.yearMonth,
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
