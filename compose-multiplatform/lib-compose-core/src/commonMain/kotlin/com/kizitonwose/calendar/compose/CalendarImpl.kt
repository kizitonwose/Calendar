package com.kizitonwose.calendar.compose

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.kizitonwose.calendar.compose.CalendarDefaults.flingBehavior
import com.kizitonwose.calendar.core.CalendarMonthWithDays

@Composable
//@RestrictTo(LIBRARY_GROUP)
fun <YearMonth, CalendarDay, CalendarMonth : CalendarMonthWithDays<YearMonth, CalendarDay>> __CalendarImpl(
    modifier: Modifier,
    state: CalendarState<YearMonth, CalendarMonth>,
    calendarScrollPaged: Boolean,
    userScrollEnabled: Boolean,
    isHorizontal: Boolean,
    reverseLayout: Boolean,
    contentPadding: PaddingValues,
    contentHeightMode: ContentHeightMode,
    dayContent: @Composable BoxScope.(CalendarDay) -> Unit,
    monthHeader: (@Composable ColumnScope.(CalendarMonth) -> Unit)? = null,
    monthBody: (@Composable ColumnScope.(CalendarMonth, content: @Composable () -> Unit) -> Unit)? = null,
    monthFooter: (@Composable ColumnScope.(CalendarMonth) -> Unit)? = null,
    monthContainer: (@Composable LazyItemScope.(CalendarMonth, container: @Composable () -> Unit) -> Unit)? = null,
) {
    if (isHorizontal) {
        LazyRow(
            modifier = modifier,
            state = state.listState,
            flingBehavior = flingBehavior(calendarScrollPaged, state.listState),
            userScrollEnabled = userScrollEnabled,
            reverseLayout = reverseLayout,
            contentPadding = contentPadding,
        ) {
            CalendarMonths(
                monthCount = state.calendarInfo.indexCount,
                monthData = { offset -> state.store[offset] },
                contentHeightMode = contentHeightMode,
                dayContent = dayContent,
                monthHeader = monthHeader,
                monthBody = monthBody,
                monthFooter = monthFooter,
                monthContainer = monthContainer,
            )
        }
    } else {
        LazyColumn(
            modifier = modifier,
            state = state.listState,
            flingBehavior = flingBehavior(calendarScrollPaged, state.listState),
            userScrollEnabled = userScrollEnabled,
            reverseLayout = reverseLayout,
            contentPadding = contentPadding,
        ) {
            CalendarMonths(
                monthCount = state.calendarInfo.indexCount,
                monthData = { offset -> state.store[offset] },
                contentHeightMode = contentHeightMode,
                dayContent = dayContent,
                monthHeader = monthHeader,
                monthBody = monthBody,
                monthFooter = monthFooter,
                monthContainer = monthContainer,
            )
        }
    }
}
