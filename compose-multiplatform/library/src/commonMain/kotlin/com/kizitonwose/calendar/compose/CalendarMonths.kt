package com.kizitonwose.calendar.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onPlaced
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.CalendarMonth
import com.kizitonwose.calendar.core.format.toIso8601String

@Suppress("FunctionName")
internal fun LazyListScope.CalendarMonths(
    monthCount: Int,
    monthData: (offset: Int) -> CalendarMonth,
    contentHeightMode: ContentHeightMode,
    dayContent: @Composable BoxScope.(CalendarDay) -> Unit,
    monthHeader: (@Composable ColumnScope.(CalendarMonth) -> Unit)?,
    monthBody: (@Composable ColumnScope.(CalendarMonth, content: @Composable () -> Unit) -> Unit)?,
    monthFooter: (@Composable ColumnScope.(CalendarMonth) -> Unit)?,
    monthContainer: (@Composable LazyItemScope.(CalendarMonth, container: @Composable () -> Unit) -> Unit)?,
    onItemPlaced: (itemCoordinates: ItemCoordinates) -> Unit,
) {
    items(
        count = monthCount,
        key = { offset -> monthData(offset).yearMonth.toIso8601String() },
    ) { offset ->
        val month = monthData(offset)
        val fillHeight = when (contentHeightMode) {
            ContentHeightMode.Wrap -> false
            ContentHeightMode.Fill -> true
        }
        val hasMonthContainer = monthContainer != null
        val currentOnItemPlaced by rememberUpdatedState(onItemPlaced)
        val itemCoordinatesStore = remember(month.yearMonth) {
            ItemCoordinatesStore(currentOnItemPlaced)
        }
        Box(Modifier.onPlaced(itemCoordinatesStore::onItemRootPlaced)) {
            monthContainer.or(defaultMonthContainer)(month) {
                Column(
                    modifier = Modifier
                        .then(
                            if (hasMonthContainer) {
                                Modifier.fillMaxWidth()
                            } else {
                                Modifier.fillParentMaxWidth()
                            },
                        )
                        .then(
                            if (fillHeight) {
                                if (hasMonthContainer) {
                                    Modifier.fillMaxHeight()
                                } else {
                                    Modifier.fillParentMaxHeight()
                                }
                            } else {
                                Modifier.wrapContentHeight()
                            },
                        ),
                ) {
                    monthHeader?.invoke(this, month)
                    monthBody.or(defaultMonthBody)(month) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .then(
                                    if (fillHeight) {
                                        Modifier.weight(1f)
                                    } else {
                                        Modifier.wrapContentHeight()
                                    },
                                ),
                        ) {
                            for ((row, week) in month.weekDays.withIndex()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .then(
                                            if (fillHeight) {
                                                Modifier.weight(1f)
                                            } else {
                                                Modifier.wrapContentHeight()
                                            },
                                        ),
                                ) {
                                    for ((column, day) in week.withIndex()) {
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clipToBounds()
                                                .onFirstDayPlaced(
                                                    dateRow = row,
                                                    dateColumn = column,
                                                    onFirstDayPlaced = itemCoordinatesStore::onFirstDayPlaced,
                                                ),
                                        ) {
                                            dayContent(day)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    monthFooter?.invoke(this, month)
                }
            }
        }
    }
}

@Stable
internal class ItemCoordinatesStore(
    private val onItemPlaced: (itemCoordinates: ItemCoordinates) -> Unit,
) {
    private var itemRootCoordinates: LayoutCoordinates? = null
    private var firstDayCoordinates: LayoutCoordinates? = null

    fun onItemRootPlaced(coordinates: LayoutCoordinates) {
        itemRootCoordinates = coordinates
        check()
    }

    fun onFirstDayPlaced(coordinates: LayoutCoordinates) {
        firstDayCoordinates = coordinates
        check()
    }

    private fun check() {
        val itemRootCoordinates = itemRootCoordinates ?: return
        val firstDayCoordinates = firstDayCoordinates ?: return
        val itemCoordinates = ItemCoordinates(
            itemRootCoordinates = itemRootCoordinates,
            firstDayCoordinates = firstDayCoordinates,
        )
        onItemPlaced(itemCoordinates)
    }
}

private inline fun Modifier.onFirstDayPlaced(
    dateRow: Int,
    dateColumn: Int,
    noinline onFirstDayPlaced: (coordinates: LayoutCoordinates) -> Unit,
) = if (dateRow == 0 && dateColumn == 0) {
    onPlaced(onFirstDayPlaced)
} else {
    this
}

private val defaultMonthContainer: (@Composable LazyItemScope.(CalendarMonth, container: @Composable () -> Unit) -> Unit) =
    { _, container -> container() }

private val defaultMonthBody: (@Composable ColumnScope.(CalendarMonth, content: @Composable () -> Unit) -> Unit) =
    { _, content -> content() }

internal inline fun <T> T?.or(default: T) = this ?: default
