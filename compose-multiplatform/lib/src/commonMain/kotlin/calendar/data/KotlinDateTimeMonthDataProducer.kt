package calendar.data

import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import calendar.compose.CalendarState
import calendar.core.CalendarMonth
import calendar.core.OutDateStyle
import calendar.core.YearMonth
import kotlinx.datetime.DayOfWeek

internal object KotlinDateTimeMonthDataProducer : MonthDataProducer<YearMonth, CalendarMonth> {
    override fun getCalendarMonthData(
        startMonth: YearMonth,
        offset: Int,
        firstDayOfWeek: DayOfWeek,
        outDateStyle: OutDateStyle,
    ): CalendarMonth {
        return calendar.data.getCalendarMonthData(
            startMonth = startMonth,
            offset = offset,
            firstDayOfWeek = firstDayOfWeek,
            outDateStyle = outDateStyle,
        ).calendarMonth
    }

    override fun getMonthIndex(startMonth: YearMonth, month: YearMonth): Int {
        return calendar.data.getMonthIndex(
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
