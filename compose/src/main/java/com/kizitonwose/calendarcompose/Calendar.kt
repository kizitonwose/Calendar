package com.kizitonwose.calendarcompose

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.kizitonwose.calendarcompose.CalendarDefaults.flingBehavior
import com.kizitonwose.calendarcompose.boxcalendar.WeekHeaderPosition
import com.kizitonwose.calendarcompose.internal.MonthData
import com.kizitonwose.calendarcompose.internal.getCalendarMonthData
import com.kizitonwose.calendarcompose.internal.getMonthIndicesCount
import java.time.DayOfWeek

@Composable
fun HorizontalCalendar(
    modifier: Modifier = Modifier,
    state: CalendarState = rememberCalendarState(),
    outDateStyle: OutDateStyle = OutDateStyle.EndOfRow,
    calendarScrollPaged: Boolean = true,
    userScrollEnabled: Boolean = true,
    reverseLayout: Boolean = false,
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
)

@Composable
fun VerticalCalendar(
    modifier: Modifier = Modifier,
    state: CalendarState = rememberCalendarState(),
    outDateStyle: OutDateStyle = OutDateStyle.EndOfRow,
    calendarScrollPaged: Boolean = true,
    userScrollEnabled: Boolean = true,
    reverseLayout: Boolean = false,
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
        mutableMapOf<Int, MonthData>()
    }

    fun getMonthData(offset: Int) = dataStore.getOrPut(offset) {
        getCalendarMonthData(startMonth, offset, firstDayOfWeek, outDateStyle)
    }

    if (isVertical) {
        LazyColumn(
            modifier = modifier.fillMaxHeight(),
            state = state.listState,
            flingBehavior = flingBehavior(calendarScrollPaged, state.listState),
            userScrollEnabled = userScrollEnabled,
            reverseLayout = reverseLayout,
        ) {
            CalendarItems(
                itemsCount = itemsCount,
                monthData = { offset -> getMonthData(offset) },
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
        ) {
            CalendarItems(
                itemsCount = itemsCount,
                monthData = { offset -> getMonthData(offset) },
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
fun BoxCalendar(
    modifier: Modifier = Modifier,
    state: CalendarState = rememberCalendarState(),
    weekHeaderPosition: WeekHeaderPosition = WeekHeaderPosition.Start,
    userScrollEnabled: Boolean,
    dayContent: @Composable ColumnScope.(CalendarDay) -> Unit = { },
    weekHeader: @Composable ColumnScope.(DayOfWeek) -> Unit = { },
    monthHeader: @Composable ColumnScope.(CalendarMonth) -> Unit = { },
) = com.kizitonwose.calendarcompose.boxcalendar.BoxCalendar(
    modifier = modifier,
    state = state,
    weekHeaderPosition = weekHeaderPosition,
    userScrollEnabled = userScrollEnabled,
    dayContent = dayContent,
    weekHeader = weekHeader,
    monthHeader = monthHeader,
)