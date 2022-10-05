package com.kizitonwose.calendarcompose.heatmapcalendar

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import com.kizitonwose.calendarcompose.MonthCalendarState
import com.kizitonwose.calendarcompose.VisibleItemState
import com.kizitonwose.calendarcore.firstDayOfWeekFromLocale
import com.kizitonwose.calendarinternal.MonthData
import com.kizitonwose.calendarinternal.getHeatMapCalendarMonthData
import java.time.DayOfWeek
import java.time.YearMonth

@Stable
class HeatMapCalendarState internal constructor(
    startMonth: YearMonth,
    endMonth: YearMonth,
    firstVisibleMonth: YearMonth,
    firstDayOfWeek: DayOfWeek,
    visibleItemState: VisibleItemState?,
) : MonthCalendarState(
    startMonth = startMonth,
    endMonth = endMonth,
    firstDayOfWeek = firstDayOfWeek,
    firstVisibleMonth = firstVisibleMonth,
    visibleItemState = visibleItemState,
) {

    override fun getMonthData(
        startMonth: YearMonth,
        offset: Int,
        firstDayOfWeek: DayOfWeek,
    ): MonthData = getHeatMapCalendarMonthData(startMonth, offset, firstDayOfWeek)


    companion object {
        internal val Saver: Saver<HeatMapCalendarState, *> = listSaver(
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
                    visibleItemState,
                )
            },
            restore = {
                HeatMapCalendarState(
                    startMonth = it[0] as YearMonth,
                    endMonth = it[1] as YearMonth,
                    firstVisibleMonth = it[2] as YearMonth,
                    firstDayOfWeek = it[3] as DayOfWeek,
                    visibleItemState = it[4] as VisibleItemState,
                )
            }
        )
    }
}

@Composable
fun rememberHeatMapCalendarState(
    startMonth: YearMonth = YearMonth.now(),
    endMonth: YearMonth = startMonth,
    firstVisibleMonth: YearMonth = startMonth,
    firstDayOfWeek: DayOfWeek = firstDayOfWeekFromLocale(),
): HeatMapCalendarState {
    return rememberSaveable(saver = HeatMapCalendarState.Saver) {
        HeatMapCalendarState(
            startMonth = startMonth,
            endMonth = endMonth,
            firstDayOfWeek = firstDayOfWeek,
            firstVisibleMonth = firstVisibleMonth,
            visibleItemState = null
        )
    }
}
