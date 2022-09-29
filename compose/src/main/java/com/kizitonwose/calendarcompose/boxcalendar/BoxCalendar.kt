package com.kizitonwose.calendarcompose.boxcalendar

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.kizitonwose.calendarcompose.CalendarDay
import com.kizitonwose.calendarcompose.CalendarMonth
import com.kizitonwose.calendarcompose.CalendarState
import com.kizitonwose.calendarcompose.internal.MonthData
import com.kizitonwose.calendarcompose.internal.getBoxCalendarMonthData
import com.kizitonwose.calendarcompose.internal.getMonthIndicesCount
import com.kizitonwose.calendarcompose.rememberCalendarState
import com.kizitonwose.calendarcore.daysOfWeek
import java.time.DayOfWeek

@Composable
internal fun BoxCalendar(
    modifier: Modifier = Modifier,
    state: CalendarState = rememberCalendarState(),
    userScrollEnabled: Boolean,
    weekHeaderPosition: WeekHeaderPosition = WeekHeaderPosition.Start,
    dayContent: @Composable ColumnScope.(CalendarDay) -> Unit = { },
    weekHeader: @Composable ColumnScope.(DayOfWeek) -> Unit = { },
    monthHeader: @Composable ColumnScope.(CalendarMonth) -> Unit = { },
) {
    val startMonth = state.startMonth
    val endMonth = state.endMonth
    val firstDayOfWeek = state.firstDayOfWeek
    val itemsCount = remember(startMonth, endMonth) {
        getMonthIndicesCount(startMonth, endMonth)
    }
    val dataStore = remember(startMonth, endMonth, firstDayOfWeek) {
        mutableMapOf<Int, MonthData>()
    }

    fun getMonthData(offset: Int) = dataStore.getOrPut(offset) {
        getBoxCalendarMonthData(startMonth, offset, firstDayOfWeek)
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.Bottom
    ) {
        if (weekHeaderPosition == WeekHeaderPosition.Start) {
            WeekHeaderColumn(
                horizontalAlignment = Alignment.End,
                firstDayOfWeek = firstDayOfWeek,
                weekHeader = weekHeader,
            )
        }
        LazyRow(
            modifier = Modifier.weight(1f),
            state = state.listState,
            userScrollEnabled = userScrollEnabled,
        ) {
            items(
                count = itemsCount,
                key = { offset -> getMonthData(offset).month }) { offset ->
                val data = getMonthData(offset)
                Column(modifier = Modifier.width(IntrinsicSize.Max)) {
                    monthHeader(data.calendarMonth)
                    Row {
                        for (week in data.calendarMonth.weekDays) {
                            Column {
                                for (day in week) {
                                    dayContent(day)
                                }
                            }
                        }
                    }
                }
            }
        }
        if (weekHeaderPosition == WeekHeaderPosition.End) {
            WeekHeaderColumn(
                horizontalAlignment = Alignment.Start,
                firstDayOfWeek = firstDayOfWeek,
                weekHeader = weekHeader,
            )
        }
    }
}

@Composable
private fun WeekHeaderColumn(
    horizontalAlignment: Alignment.Horizontal,
    firstDayOfWeek: DayOfWeek,
    weekHeader: @Composable (ColumnScope.(DayOfWeek) -> Unit),
) {
    Column(
        modifier = Modifier.width(IntrinsicSize.Max),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = horizontalAlignment
    ) {
        for (dayOfWeek in daysOfWeek(firstDayOfWeek)) {
            weekHeader(dayOfWeek)
        }
    }
}
