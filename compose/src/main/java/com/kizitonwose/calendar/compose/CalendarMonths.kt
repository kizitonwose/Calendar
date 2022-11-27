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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.CalendarMonth

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
) {
    items(
        count = monthCount,
        key = { offset -> monthData(offset).yearMonth },
    ) { offset ->
        val month = monthData(offset)
        val fillHeight = when (contentHeightMode) {
            ContentHeightMode.Wrap -> false
            ContentHeightMode.Fill -> true
        }
        val hasContainer = monthContainer != null
        monthContainer.or(defaultMonthContainer)(month) {
            Column(
                modifier = Modifier
                    .then(if (hasContainer) Modifier.fillMaxWidth() else Modifier.fillParentMaxWidth())
                    .then(
                        if (fillHeight) {
                            if (hasContainer) Modifier.fillMaxHeight() else Modifier.fillParentMaxHeight()
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
                            .then(if (fillHeight) Modifier.weight(1f) else Modifier.wrapContentHeight()),
                    ) {
                        for (week in month.weekDays) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .then(if (fillHeight) Modifier.weight(1f) else Modifier.wrapContentHeight()),
                            ) {
                                for (day in week) {
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clipToBounds(),
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

private val defaultMonthContainer: (@Composable LazyItemScope.(CalendarMonth, container: @Composable () -> Unit) -> Unit) =
    { _, container -> container() }

private val defaultMonthBody: (@Composable ColumnScope.(CalendarMonth, content: @Composable () -> Unit) -> Unit) =
    { _, content -> content() }

private fun <T> T?.or(default: T) = this ?: default
