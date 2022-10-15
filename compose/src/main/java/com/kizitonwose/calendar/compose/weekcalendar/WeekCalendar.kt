package com.kizitonwose.calendar.compose.weekcalendar

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.kizitonwose.calendar.compose.CalendarDefaults.flingBehavior
import com.kizitonwose.calendar.core.Week
import com.kizitonwose.calendar.core.WeekDay

@Composable
internal fun WeekCalendarInternal(
    modifier: Modifier,
    state: WeekCalendarState,
    calendarScrollPaged: Boolean,
    userScrollEnabled: Boolean,
    reverseLayout: Boolean,
    contentPadding: PaddingValues,
    dayContent: @Composable BoxScope.(WeekDay) -> Unit,
    weekHeader: @Composable ColumnScope.(Week) -> Unit,
    weekFooter: @Composable ColumnScope.(Week) -> Unit,
) {
    LazyRow(
        modifier = modifier,
        state = state.listState,
        flingBehavior = flingBehavior(calendarScrollPaged, state.listState),
        userScrollEnabled = userScrollEnabled,
        reverseLayout = reverseLayout,
        contentPadding = contentPadding
    ) {
        items(
            count = state.weekIndexCount,
            key = { offset -> state.store[offset].days.first().date }
        ) { offset ->
            val columnModifier = if (calendarScrollPaged) {
                Modifier.fillParentMaxWidth()
            } else Modifier.width(IntrinsicSize.Max)
            val week = state.store[offset]
            Column(modifier = columnModifier) {
                weekHeader(week)
                Row {
                    for (date in week.days) {
                        val boxModifier = if (calendarScrollPaged) {
                            Modifier.weight(1f)
                        } else Modifier
                        Box(modifier = boxModifier) {
                            dayContent(date)
                        }
                    }
                }
                weekFooter(week)
            }
        }
    }
}
