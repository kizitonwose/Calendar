package com.kizitonwose.calendar.compose.heatmapcalendar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.CalendarMonth
import com.kizitonwose.calendar.core.daysOfWeek
import java.time.DayOfWeek

@Composable
internal fun HeatMapCalendarImpl(
    modifier: Modifier,
    state: HeatMapCalendarState,
    userScrollEnabled: Boolean,
    weekHeaderPosition: HeatMapWeekHeaderPosition,
    contentPadding: PaddingValues,
    dayContent: @Composable ColumnScope.(day: CalendarDay, week: HeatMapWeek) -> Unit,
    weekHeader: (@Composable ColumnScope.(DayOfWeek) -> Unit)? = null,
    monthHeader: (@Composable ColumnScope.(CalendarMonth) -> Unit)? = null,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.Bottom,
    ) {
        if (weekHeaderPosition == HeatMapWeekHeaderPosition.Start && weekHeader != null) {
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
                key = { offset -> state.store[offset].yearMonth },
            ) { offset ->
                val calendarMonth = state.store[offset]
                Column(modifier = Modifier.width(IntrinsicSize.Max)) {
                    monthHeader?.invoke(this, calendarMonth)
                    Row {
                        for (week in calendarMonth.weekDays) {
                            Column {
                                for (day in week) {
                                    dayContent(day, HeatMapWeek(week))
                                }
                            }
                        }
                    }
                }
            }
        }
        if (weekHeaderPosition == HeatMapWeekHeaderPosition.End && weekHeader != null) {
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
        horizontalAlignment = horizontalAlignment,
    ) {
        for (dayOfWeek in daysOfWeek(firstDayOfWeek)) {
            weekHeader(dayOfWeek)
        }
    }
}
