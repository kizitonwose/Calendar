package com.kizitonwose.calendarcompose

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import com.kizitonwose.calendarcore.OutDateStyle
import com.kizitonwose.calendarcore.firstDayOfWeekFromLocale
import com.kizitonwose.calendardata.MonthData
import com.kizitonwose.calendardata.getCalendarMonthData
import java.time.DayOfWeek
import java.time.YearMonth

@Stable
class CalendarState internal constructor(
    startMonth: YearMonth,
    endMonth: YearMonth,
    firstDayOfWeek: DayOfWeek,
    firstVisibleMonth: YearMonth,
    outDateStyle: OutDateStyle,
    visibleItemState: VisibleItemState?,
) : MonthCalendarState(
    startMonth = startMonth,
    endMonth = endMonth,
    firstDayOfWeek = firstDayOfWeek,
    firstVisibleMonth = firstVisibleMonth,
    visibleItemState = visibleItemState,
) {
    private var _outDateStyle by mutableStateOf(outDateStyle)
    internal var outDateStyle: OutDateStyle
        get() = _outDateStyle
        set(value) {
            if (value != outDateStyle) {
                _outDateStyle = value
                monthDataChanged()
            }
        }

    override fun getMonthData(
        startMonth: YearMonth,
        offset: Int,
        firstDayOfWeek: DayOfWeek,
    ): MonthData = getCalendarMonthData(startMonth, offset, firstDayOfWeek, outDateStyle)

    companion object {
        internal val Saver: Saver<CalendarState, *> = listSaver(
            save = {
                val visibleItemState = VisibleItemState(
                    firstVisibleItemIndex = it.listState.firstVisibleItemIndex,
                    firstVisibleItemScrollOffset = it.listState.firstVisibleItemScrollOffset
                )
                listOf(
                    it.startMonth,
                    it.endMonth,
                    it.firstVisibleMonth.yearMonth,
                    it.firstDayOfWeek,
                    it.outDateStyle,
                    visibleItemState,
                )
            },
            restore = {
                CalendarState(
                    startMonth = it[0] as YearMonth,
                    endMonth = it[1] as YearMonth,
                    firstVisibleMonth = it[2] as YearMonth,
                    firstDayOfWeek = it[3] as DayOfWeek,
                    outDateStyle = it[4] as OutDateStyle,
                    visibleItemState = it[5] as VisibleItemState,
                )
            }
        )
    }
}

@Composable
fun rememberCalendarState(
    startMonth: YearMonth = YearMonth.now(),
    endMonth: YearMonth = startMonth,
    firstVisibleMonth: YearMonth = startMonth,
    firstDayOfWeek: DayOfWeek = firstDayOfWeekFromLocale(),
    outDateStyle: OutDateStyle = OutDateStyle.EndOfRow,
): CalendarState {
    return rememberSaveable(saver = CalendarState.Saver) {
        CalendarState(
            startMonth = startMonth,
            endMonth = endMonth,
            firstDayOfWeek = firstDayOfWeek,
            firstVisibleMonth = firstVisibleMonth,
            outDateStyle = outDateStyle,
            visibleItemState = null
        )
    }
}

