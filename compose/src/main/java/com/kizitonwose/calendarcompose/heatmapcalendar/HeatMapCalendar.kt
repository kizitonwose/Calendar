package com.kizitonwose.calendarcompose.heatmapcalendar

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.kizitonwose.calendarcore.CalendarDay
import com.kizitonwose.calendarcore.CalendarMonth
import com.kizitonwose.calendarcore.daysOfWeek
import java.time.DayOfWeek

@Composable
internal fun HeatMapCalendarInternal(
    modifier: Modifier,
    state: HeatMapCalendarState,
    userScrollEnabled: Boolean,
    weekHeaderPosition: HeatMapWeekHeaderPosition,
    contentPadding: PaddingValues,
    dayContent: @Composable ColumnScope.(day: CalendarDay, week: List<CalendarDay>) -> Unit,
    weekHeader: @Composable ColumnScope.(DayOfWeek) -> Unit,
    monthHeader: @Composable ColumnScope.(CalendarMonth) -> Unit,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.Bottom
    ) {
        if (weekHeaderPosition == HeatMapWeekHeaderPosition.Start) {
            WeekHeaderColumn(
                horizontalAlignment = Alignment.End,
                firstDayOfWeek = state.firstDayOfWeek,
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
                count = state.monthIndexCount,
                key = { offset -> state.store[offset].yearMonth }) { offset ->
                val calendarMonth = state.store[offset]
                Column(modifier = Modifier.width(IntrinsicSize.Max)) {
                    monthHeader(calendarMonth)
                    Row {
                        for (week in calendarMonth.weekDays) {
                            Column {
                                for (day in week) {
                                    dayContent(day, week)
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
                firstDayOfWeek = state.firstDayOfWeek,
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
