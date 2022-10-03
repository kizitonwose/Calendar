package com.kizitonwose.calendarcompose.heatmapcalendar

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.kizitonwose.calendarcompose.CalendarState
import com.kizitonwose.calendarcore.CalendarDay
import com.kizitonwose.calendarcore.CalendarMonth
import com.kizitonwose.calendarcore.daysOfWeek
import com.kizitonwose.calendarinternal.CalendarDataStore
import com.kizitonwose.calendarinternal.getBoxCalendarMonthData
import com.kizitonwose.calendarinternal.getMonthIndicesCount
import java.time.DayOfWeek

@Composable
internal fun HeatMapCalendarInternal(
    modifier: Modifier,
    state: CalendarState,
    userScrollEnabled: Boolean,
    weekHeaderPosition: HeatMapWeekHeaderPosition,
    contentPadding: PaddingValues,
    dayContent: @Composable ColumnScope.(CalendarDay) -> Unit,
    weekHeader: @Composable ColumnScope.(DayOfWeek) -> Unit,
    monthHeader: @Composable ColumnScope.(CalendarMonth) -> Unit,
) {
    val startMonth = state.startMonth
    val endMonth = state.endMonth
    val firstDayOfWeek = state.firstDayOfWeek
    val itemsCount = remember(startMonth, endMonth) {
        getMonthIndicesCount(startMonth, endMonth)
    }
    val dataStore = remember(startMonth, endMonth, firstDayOfWeek) {
        CalendarDataStore { offset ->
            getBoxCalendarMonthData(startMonth, offset, firstDayOfWeek)
        }
    }
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.Bottom
    ) {
        if (weekHeaderPosition == HeatMapWeekHeaderPosition.Start) {
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
            contentPadding = contentPadding,
        ) {
            items(
                count = itemsCount,
                key = { offset -> dataStore[offset].month }) { offset ->
                val data = dataStore[offset]
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
        if (weekHeaderPosition == HeatMapWeekHeaderPosition.End) {
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
