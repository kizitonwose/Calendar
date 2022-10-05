package com.kizitonwose.calendarcompose.weekcalendar

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.kizitonwose.calendarcompose.CalendarDefaults.flingBehavior
import com.kizitonwose.calendarcore.WeekDay

@Composable
internal fun WeekCalendarInternal(
    modifier: Modifier,
    state: WeekCalendarState,
    calendarScrollPaged: Boolean,
    userScrollEnabled: Boolean,
    reverseLayout: Boolean,
    contentPadding: PaddingValues,
    dayContent: @Composable BoxScope.(WeekDay) -> Unit,
    weekHeader: @Composable ColumnScope.(List<WeekDay>) -> Unit,
    weekFooter: @Composable ColumnScope.(List<WeekDay>) -> Unit,
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
            key = { offset -> state.store[offset].firstDayInWeek }) { offset ->
            val columnModifier = if (calendarScrollPaged) {
                Modifier.fillParentMaxWidth()
            } else Modifier.width(IntrinsicSize.Max)
            val weekData = state.store[offset]
            Column(modifier = columnModifier) {
                weekHeader(weekData.days)
                Row {
                    for (date in weekData.days) {
                        val boxModifier = if (calendarScrollPaged) {
                            Modifier.weight(1f)
                        } else Modifier
                        Box(modifier = boxModifier) {
                            dayContent(date)
                        }
                    }
                }
                weekFooter(weekData.days)
            }
        }
    }
}
