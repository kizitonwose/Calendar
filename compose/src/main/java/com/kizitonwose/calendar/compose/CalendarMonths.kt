package com.kizitonwose.calendar.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.CalendarMonth

@Suppress("FunctionName")
internal fun LazyListScope.CalendarMonths(
    monthCount: Int,
    monthData: (offset: Int) -> CalendarMonth,
    contentVerticalMode: ContentVerticalMode,
    dayContent: @Composable BoxScope.(CalendarDay) -> Unit,
    monthHeader: (@Composable ColumnScope.(CalendarMonth) -> Unit)? = null,
    monthBody: @Composable ColumnScope.(CalendarMonth, content: @Composable () -> Unit) -> Unit,
    monthFooter: (@Composable ColumnScope.(CalendarMonth) -> Unit)? = null,
    monthContainer: @Composable LazyItemScope.(CalendarMonth, container: @Composable () -> Unit) -> Unit,
) {
    items(
        count = monthCount,
        key = { offset -> monthData(offset).yearMonth },
    ) { offset ->
        val month = monthData(offset)
        val hasFooter = monthFooter != null
        val hasHeader = monthHeader != null
        val fillParentHeight = when (contentVerticalMode) {
            ContentVerticalMode.Wrap -> false
            ContentVerticalMode.Fill -> true
        }
        monthContainer(month) {
            if (fillParentHeight && hasFooter) {
                ColumnFilledWithFooter(
                    modifier = Modifier
                        .fillParentMaxSize(),
                    hasHeader = hasHeader,
                ) {
                    MonthContent(
                        month = month,
                        contentVerticalMode = contentVerticalMode,
                        dayContent = dayContent,
                        monthHeader = monthHeader,
                        monthBody = monthBody,
                        monthFooter = monthFooter,
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillParentMaxWidth()
                        .wrapContentHeight(),
                ) {
                    MonthContent(
                        month = month,
                        contentVerticalMode = contentVerticalMode,
                        dayContent = dayContent,
                        monthHeader = monthHeader,
                        monthBody = monthBody,
                        monthFooter = monthFooter,
                    )
                }
            }
        }
    }
}

@Composable
private fun ColumnScope.MonthContent(
    month: CalendarMonth,
    contentVerticalMode: ContentVerticalMode,
    dayContent: @Composable BoxScope.(CalendarDay) -> Unit,
    monthHeader: (@Composable ColumnScope.(CalendarMonth) -> Unit)? = null,
    monthBody: @Composable ColumnScope.(CalendarMonth, content: @Composable () -> Unit) -> Unit,
    monthFooter: (@Composable ColumnScope.(CalendarMonth) -> Unit)? = null,
) {
    monthHeader?.invoke(this, month)
    monthBody(month) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .calendarParentHeight(contentVerticalMode),
        ) {
            for (week in month.weekDays) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weekRowHeight(contentVerticalMode, this),
                ) {
                    for (day in week) {
                        Box(modifier = Modifier.weight(1f)) {
                            dayContent(day)
                        }
                    }
                }
            }
        }
    }
    monthFooter?.invoke(this, month)
}
