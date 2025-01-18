package com.kizitonwose.calendar.compose.weekcalendar

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onPlaced
import com.kizitonwose.calendar.compose.CalendarDefaults.flingBehavior
import com.kizitonwose.calendar.core.Week
import com.kizitonwose.calendar.core.WeekDay
import com.kizitonwose.calendar.core.format.toIso8601String

@Composable
internal fun WeekCalendarImpl(
    modifier: Modifier,
    state: WeekCalendarState,
    calendarScrollPaged: Boolean,
    userScrollEnabled: Boolean,
    reverseLayout: Boolean,
    contentPadding: PaddingValues,
    dayContent: @Composable BoxScope.(WeekDay) -> Unit,
    weekHeader: (@Composable ColumnScope.(Week) -> Unit)? = null,
    weekFooter: (@Composable ColumnScope.(Week) -> Unit)? = null,
    onFirstDayPlaced: (coordinates: LayoutCoordinates) -> Unit,
) {
    LazyRow(
        modifier = modifier,
        state = state.listState,
        flingBehavior = flingBehavior(calendarScrollPaged, state.listState),
        userScrollEnabled = userScrollEnabled,
        reverseLayout = reverseLayout,
        contentPadding = contentPadding,
    ) {
        items(
            count = state.weekIndexCount,
            key = { offset -> state.store[offset].days.first().date.toIso8601String() },
        ) { offset ->
            val week = state.store[offset]
            Column(
                modifier = Modifier
                    .then(
                        if (calendarScrollPaged) {
                            Modifier.fillParentMaxWidth()
                        } else {
                            Modifier.width(IntrinsicSize.Max)
                        },
                    ),
            ) {
                weekHeader?.invoke(this, week)
                Row {
                    for (day in week.days) {
                        Box(
                            modifier = Modifier
                                .then(if (calendarScrollPaged) Modifier.weight(1f) else Modifier)
                                .clipToBounds()
                                .onFirstDayPlaced(day, week, onFirstDayPlaced),
                        ) {
                            dayContent(day)
                        }
                    }
                }
                weekFooter?.invoke(this, week)
            }
        }
    }
}

private inline fun Modifier.onFirstDayPlaced(
    day: WeekDay,
    week: Week,
    noinline onFirstDayPlaced: (coordinates: LayoutCoordinates) -> Unit,
) = if (day == week.days.first()) {
    onPlaced(onFirstDayPlaced)
} else {
    this
}
