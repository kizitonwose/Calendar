package com.kizitonwose.calendarcompose

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kizitonwose.calendarcompose.CalendarDefaults.flingBehavior
import com.kizitonwose.calendarcompose.boxcalendar.BoxCalendarInternal
import com.kizitonwose.calendarcompose.boxcalendar.WeekHeaderPosition
import com.kizitonwose.calendarcompose.shared.CalendarDataStore
import com.kizitonwose.calendarcompose.shared.getCalendarMonthData
import com.kizitonwose.calendarcompose.shared.getMonthIndicesCount
import com.kizitonwose.calendarcompose.weekcalendar.WeekCalendarInternal
import com.kizitonwose.calendarcompose.weekcalendar.WeekCalendarState
import com.kizitonwose.calendarcompose.weekcalendar.rememberWeekCalendarState
import java.time.DayOfWeek
import java.time.LocalDate

@Composable
fun HorizontalCalendar(
    modifier: Modifier = Modifier,
    state: CalendarState = rememberCalendarState(),
    outDateStyle: OutDateStyle = OutDateStyle.EndOfRow,
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
    outDateStyle = outDateStyle,
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
    outDateStyle: OutDateStyle = OutDateStyle.EndOfRow,
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
    outDateStyle = outDateStyle,
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
    outDateStyle: OutDateStyle,
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
    val startMonth = state.startMonth
    val endMonth = state.endMonth
    val firstDayOfWeek = state.firstDayOfWeek
    val itemsCount = remember(startMonth, endMonth) {
        getMonthIndicesCount(startMonth, endMonth)
    }
    val dataStore = remember(startMonth, firstDayOfWeek, outDateStyle) {
        CalendarDataStore { offset ->
            getCalendarMonthData(startMonth, offset, firstDayOfWeek, outDateStyle)
        }
    }

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
                itemsCount = itemsCount,
                monthData = { offset -> dataStore[offset] },
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
                itemsCount = itemsCount,
                monthData = { offset -> dataStore[offset] },
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
    dayContent: @Composable BoxScope.(LocalDate) -> Unit = { },
    weekHeader: @Composable ColumnScope.(List<LocalDate>) -> Unit = { },
    weekFooter: @Composable ColumnScope.(List<LocalDate>) -> Unit = { },
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
fun BoxCalendar(
    modifier: Modifier = Modifier,
    state: CalendarState = rememberCalendarState(),
    weekHeaderPosition: WeekHeaderPosition = WeekHeaderPosition.Start,
    userScrollEnabled: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    dayContent: @Composable ColumnScope.(CalendarDay) -> Unit = { },
    weekHeader: @Composable ColumnScope.(DayOfWeek) -> Unit = { },
    monthHeader: @Composable ColumnScope.(CalendarMonth) -> Unit = { },
) = BoxCalendarInternal(
    modifier = modifier,
    state = state,
    weekHeaderPosition = weekHeaderPosition,
    userScrollEnabled = userScrollEnabled,
    dayContent = dayContent,
    weekHeader = weekHeader,
    monthHeader = monthHeader,
    contentPadding = contentPadding,
)
