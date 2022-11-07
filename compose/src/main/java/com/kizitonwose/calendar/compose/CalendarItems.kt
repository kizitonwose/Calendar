package com.kizitonwose.calendar.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.CalendarMonth

@Suppress("FunctionName")
internal fun LazyListScope.CalendarItems(
    itemsCount: Int,
    monthData: (offset: Int) -> CalendarMonth,
    dayContent: @Composable BoxScope.(CalendarDay) -> Unit,
    monthHeader: @Composable ColumnScope.(CalendarMonth) -> Unit,
    monthBody: @Composable ColumnScope.(CalendarMonth, content: @Composable () -> Unit) -> Unit,
    monthFooter: @Composable ColumnScope.(CalendarMonth) -> Unit,
    monthContainer: @Composable LazyItemScope.(CalendarMonth, container: @Composable () -> Unit) -> Unit,
) {
    items(
        count = itemsCount,
        key = { offset -> monthData(offset).yearMonth },
    ) { offset ->
        val calendarMonth = monthData(offset)
        monthContainer(calendarMonth) {
            Column(modifier = Modifier.fillParentMaxWidth()) {
                monthHeader(calendarMonth)
                monthBody(calendarMonth) {
                    Column {
                        for (week in calendarMonth.weekDays) {
                            Row {
                                for (day in week) {
                                    Box(modifier = Modifier.weight(1f)) {
                                        dayContent(day)
                                    }
                                }
                            }
                        }
                    }
                }
                monthFooter(calendarMonth)
            }
        }
    }
}
