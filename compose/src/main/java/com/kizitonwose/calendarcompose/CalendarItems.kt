package com.kizitonwose.calendarcompose

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.kizitonwose.calendarcompose.shared.MonthData

@Suppress("FunctionName")
internal fun LazyListScope.CalendarItems(
    itemsCount: Int,
    monthData: (offset: Int) -> MonthData,
    dayContent: @Composable BoxScope.(CalendarDay) -> Unit,
    monthHeader: @Composable ColumnScope.(CalendarMonth) -> Unit,
    monthContent: @Composable ColumnScope.(CalendarMonth, content: @Composable () -> Unit) -> Unit,
    monthFooter: @Composable ColumnScope.(CalendarMonth) -> Unit,
    monthContainer: @Composable LazyItemScope.(CalendarMonth, container: @Composable () -> Unit) -> Unit,
) {
    items(
        count = itemsCount,
        key = { offset -> monthData(offset).month }) { offset ->
        val data = monthData(offset)
        monthContainer(data.calendarMonth) {
            Column(modifier = Modifier.fillParentMaxWidth()) {
                monthHeader(data.calendarMonth)
                monthContent(data.calendarMonth) {
                    Column {
                        for (week in data.calendarMonth.weekDays) {
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
                monthFooter(data.calendarMonth)
            }
        }
    }
}
