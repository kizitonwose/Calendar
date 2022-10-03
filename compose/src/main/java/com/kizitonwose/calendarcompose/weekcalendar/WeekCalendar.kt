package com.kizitonwose.calendarcompose.weekcalendar

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.kizitonwose.calendarcompose.CalendarDefaults.flingBehavior
import com.kizitonwose.calendarinternal.CalendarDataStore
import com.kizitonwose.calendarinternal.getWeekCalendarData
import com.kizitonwose.calendarinternal.getWeekIndicesCount
import java.time.LocalDate

@Composable
internal fun WeekCalendarInternal(
    modifier: Modifier,
    state: WeekCalendarState,
    calendarScrollPaged: Boolean,
    userScrollEnabled: Boolean,
    reverseLayout: Boolean,
    contentPadding: PaddingValues,
    dayContent: @Composable BoxScope.(LocalDate) -> Unit,
    weekHeader: @Composable ColumnScope.(List<LocalDate>) -> Unit,
    weekFooter: @Composable ColumnScope.(List<LocalDate>) -> Unit,
) {
    val weekIndexCount = remember(state.startDate, state.endDate) {
        getWeekIndicesCount(state.startDate, state.endDate)
    }
    val dataStore = remember(state.startDate) {
        CalendarDataStore { offset ->
            getWeekCalendarData(state.startDate, offset)
        }
    }
    LazyRow(
        modifier = modifier,
        state = state.listState,
        flingBehavior = flingBehavior(calendarScrollPaged, state.listState),
        userScrollEnabled = userScrollEnabled,
        reverseLayout = reverseLayout,
        contentPadding = contentPadding
    ) {
        items(
            count = weekIndexCount,
            key = { offset -> dataStore[offset].firstDayInWeek }) { offset ->
            val columnModifier = if (calendarScrollPaged) {
                Modifier.fillParentMaxWidth()
            } else Modifier.width(IntrinsicSize.Max)
            val data = dataStore[offset]
            Column(modifier = columnModifier) {
                weekHeader(data.days)
                Row {
                    for (date in data.days) {
                        val boxModifier = if (calendarScrollPaged) {
                            Modifier.weight(1f)
                        } else Modifier
                        Box(modifier = boxModifier) {
                            dayContent(date)
                        }
                    }
                }
                weekFooter(data.days)
            }
        }
    }
}
