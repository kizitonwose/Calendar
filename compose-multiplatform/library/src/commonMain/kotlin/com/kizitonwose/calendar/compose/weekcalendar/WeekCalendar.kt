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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onPlaced
import com.kizitonwose.calendar.compose.CalendarDefaults.flingBehavior
import com.kizitonwose.calendar.compose.ItemCoordinates
import com.kizitonwose.calendar.compose.ItemCoordinatesStore
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
    onItemPlaced: (itemCoordinates: ItemCoordinates) -> Unit,
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
            val currentOnItemPlaced by rememberUpdatedState(onItemPlaced)
            val itemCoordinatesStore = remember(week.days.first().date) {
                ItemCoordinatesStore(currentOnItemPlaced)
            }
            Column(
                modifier = Modifier
                    .then(
                        if (calendarScrollPaged) {
                            Modifier.fillParentMaxWidth()
                        } else {
                            Modifier.width(IntrinsicSize.Max)
                        },
                    )
                    .onPlaced(itemCoordinatesStore::onItemRootPlaced),
            ) {
                weekHeader?.invoke(this, week)
                Row {
                    for ((column, day) in week.days.withIndex()) {
                        Box(
                            modifier = Modifier
                                .then(if (calendarScrollPaged) Modifier.weight(1f) else Modifier)
                                .clipToBounds()
                                .onFirstDayPlaced(column, itemCoordinatesStore::onFirstDayPlaced),
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
    column: Int,
    noinline onFirstDayPlaced: (coordinates: LayoutCoordinates) -> Unit,
) = if (column == 0) {
    onPlaced(onFirstDayPlaced)
} else {
    this
}
