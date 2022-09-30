package com.kizitonwose.calendarcompose.weekcalendar

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.kizitonwose.calendarcompose.CalendarDefaults.flingBehavior
import com.kizitonwose.calendarcompose.shared.CalendarDataStore
import java.time.LocalDate

@Composable
internal fun WeekCalendarInternal(
    modifier: Modifier,
    state: WeekCalendarState,
    calendarScrollPaged: Boolean,
    userScrollEnabled: Boolean,
    reverseLayout: Boolean,
    contentPadding: PaddingValues,
    dayContent: @Composable RowScope.(LocalDate) -> Unit,
    weekHeader: @Composable ColumnScope.(List<LocalDate>) -> Unit,
    weekFooter: @Composable ColumnScope.(List<LocalDate>) -> Unit,
) {
    val weekIndexData = remember(state.startDate, state.endDate, state.firstDayOfWeek) {
        getWeekCalendarIndexData(state.startDate, state.endDate, state.firstDayOfWeek)
    }
    state.startDateAdjusted = weekIndexData.startDateAdjusted
    state.endDateAdjusted = weekIndexData.endDateAdjusted
    val dataStore = remember(weekIndexData.startDateAdjusted) {
        CalendarDataStore { offset ->
            getWeekCalendarData(weekIndexData.startDateAdjusted, offset)
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
            count = weekIndexData.weekCount,
            key = { offset -> dataStore[offset].firstDayInWeek }) { offset ->
            val columnModifier = if (calendarScrollPaged) {
                Modifier.fillParentMaxWidth()
            } else Modifier.width(IntrinsicSize.Max)
            val data = dataStore[offset]
            Column(modifier = columnModifier) {
                weekHeader(data.days)
                Row {
                    for (date in data.days) {
                        dayContent(date)
                    }
                }
                weekFooter(data.days)
            }
        }
    }
}
