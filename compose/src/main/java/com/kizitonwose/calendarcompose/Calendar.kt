package com.kizitonwose.calendarcompose

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kizitonwose.calendarcompose.CalendarDefaults.flingBehavior
import com.kizitonwose.calendarcompose.heatmapcalendar.HeatMapCalendarInternal
import com.kizitonwose.calendarcompose.heatmapcalendar.HeatMapCalendarState
import com.kizitonwose.calendarcompose.heatmapcalendar.HeatMapWeekHeaderPosition
import com.kizitonwose.calendarcompose.heatmapcalendar.rememberHeatMapCalendarState
import com.kizitonwose.calendarcompose.weekcalendar.WeekCalendarInternal
import com.kizitonwose.calendarcompose.weekcalendar.WeekCalendarState
import com.kizitonwose.calendarcompose.weekcalendar.rememberWeekCalendarState
import com.kizitonwose.calendarcore.CalendarDay
import com.kizitonwose.calendarcore.CalendarMonth
import com.kizitonwose.calendarcore.WeekDay
import java.time.DayOfWeek

@Composable
fun HorizontalCalendar(
    modifier: Modifier = Modifier,
    state: CalendarState = rememberCalendarState(),
    calendarScrollPaged: Boolean = true,
    userScrollEnabled: Boolean = true,
    reverseLayout: Boolean = false,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    dayContent: @Composable BoxScope.(CalendarDay) -> Unit = { },
    monthHeader: @Composable ColumnScope.(CalendarMonth) -> Unit = { },
    monthContent: @Composable ColumnScope.(CalendarMonth, content: @Composable () -> Unit) -> Unit = { _, content -> content() },
    monthFooter: @Composable ColumnScope.(CalendarMonth) -> Unit = { },
    monthContainer: @Composable LazyItemScope.(CalendarMonth, container: @Composable () -> Unit) -> Unit = { _, container -> container() },
) = Calendar(
    modifier = modifier,
    state = state,
    calendarScrollPaged = calendarScrollPaged,
    userScrollEnabled = userScrollEnabled,
    isVertical = false,
    reverseLayout = reverseLayout,
    dayContent = dayContent,
    monthHeader = monthHeader,
    monthContent = monthContent,
    monthFooter = monthFooter,
    monthContainer = monthContainer,
    contentPadding = contentPadding,
)

@Composable
fun VerticalCalendar(
    modifier: Modifier = Modifier,
    state: CalendarState = rememberCalendarState(),
    calendarScrollPaged: Boolean = false,
    userScrollEnabled: Boolean = true,
    reverseLayout: Boolean = false,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    dayContent: @Composable BoxScope.(CalendarDay) -> Unit = { },
    monthHeader: @Composable ColumnScope.(CalendarMonth) -> Unit = { },
    monthContent: @Composable ColumnScope.(CalendarMonth, content: @Composable () -> Unit) -> Unit = { _, content -> content() },
    monthFooter: @Composable ColumnScope.(CalendarMonth) -> Unit = { },
    monthContainer: @Composable LazyItemScope.(CalendarMonth, container: @Composable () -> Unit) -> Unit = { _, container -> container() },
) = Calendar(
    modifier = modifier,
    state = state,
    calendarScrollPaged = calendarScrollPaged,
    userScrollEnabled = userScrollEnabled,
    isVertical = true,
    reverseLayout = reverseLayout,
    dayContent = dayContent,
    monthHeader = monthHeader,
    monthContent = monthContent,
    monthFooter = monthFooter,
    monthContainer = monthContainer,
    contentPadding = contentPadding,
)

@Composable
private fun Calendar(
    modifier: Modifier,
    state: CalendarState,
    calendarScrollPaged: Boolean,
    userScrollEnabled: Boolean,
    isVertical: Boolean,
    reverseLayout: Boolean,
    contentPadding: PaddingValues,
    dayContent: @Composable BoxScope.(CalendarDay) -> Unit,
    monthHeader: @Composable ColumnScope.(CalendarMonth) -> Unit,
    monthContent: @Composable ColumnScope.(CalendarMonth, content: @Composable () -> Unit) -> Unit,
    monthFooter: @Composable ColumnScope.(CalendarMonth) -> Unit,
    monthContainer: @Composable LazyItemScope.(CalendarMonth, container: @Composable () -> Unit) -> Unit,
) {
    if (isVertical) {
        LazyColumn(
            modifier = modifier.fillMaxHeight(),
            state = state.listState,
            flingBehavior = flingBehavior(calendarScrollPaged, state.listState),
            userScrollEnabled = userScrollEnabled,
            reverseLayout = reverseLayout,
            contentPadding = contentPadding,
        ) {
            CalendarItems(
                itemsCount = state.monthIndexCount,
                monthData = { offset -> state.store[offset] },
                dayContent = dayContent,
                monthHeader = monthHeader,
                monthContent = monthContent,
                monthFooter = monthFooter,
                monthContainer = monthContainer,
            )
        }
    } else {
        LazyRow(
            modifier = modifier.wrapContentHeight(),
            state = state.listState,
            flingBehavior = flingBehavior(calendarScrollPaged, state.listState),
            userScrollEnabled = userScrollEnabled,
            reverseLayout = reverseLayout,
            contentPadding = contentPadding,
        ) {
            CalendarItems(
                itemsCount = state.monthIndexCount,
                monthData = { offset -> state.store[offset] },
                dayContent = dayContent,
                monthHeader = monthHeader,
                monthContent = monthContent,
                monthFooter = monthFooter,
                monthContainer = monthContainer,
            )
        }
    }
}

@Composable
fun WeekCalendar(
    modifier: Modifier = Modifier,
    state: WeekCalendarState = rememberWeekCalendarState(),
    calendarScrollPaged: Boolean = true,
    userScrollEnabled: Boolean = true,
    reverseLayout: Boolean = false,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    dayContent: @Composable BoxScope.(WeekDay) -> Unit = { },
    weekHeader: @Composable ColumnScope.(List<WeekDay>) -> Unit = { },
    weekFooter: @Composable ColumnScope.(List<WeekDay>) -> Unit = { },
) = WeekCalendarInternal(
    modifier = modifier,
    state = state,
    calendarScrollPaged = calendarScrollPaged,
    userScrollEnabled = userScrollEnabled,
    reverseLayout = reverseLayout,
    dayContent = dayContent,
    weekHeader = weekHeader,
    weekFooter = weekFooter,
    contentPadding = contentPadding,
)

@Composable
fun HeatMapCalendar(
    modifier: Modifier = Modifier,
    state: HeatMapCalendarState = rememberHeatMapCalendarState(),
    weekHeaderPosition: HeatMapWeekHeaderPosition = HeatMapWeekHeaderPosition.Start,
    userScrollEnabled: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    dayContent: @Composable ColumnScope.(day: CalendarDay, week: List<CalendarDay>) -> Unit,
    weekHeader: @Composable ColumnScope.(DayOfWeek) -> Unit = { },
    monthHeader: @Composable ColumnScope.(CalendarMonth) -> Unit = { },
) = HeatMapCalendarInternal(
    modifier = modifier,
    state = state,
    weekHeaderPosition = weekHeaderPosition,
    userScrollEnabled = userScrollEnabled,
    dayContent = dayContent,
    weekHeader = weekHeader,
    monthHeader = monthHeader,
    contentPadding = contentPadding,
)
