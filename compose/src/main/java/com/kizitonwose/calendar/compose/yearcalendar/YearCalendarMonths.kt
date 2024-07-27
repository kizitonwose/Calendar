package com.kizitonwose.calendar.compose.yearcalendar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.unit.Dp
import com.kizitonwose.calendar.compose.or
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.CalendarMonth
import com.kizitonwose.calendar.core.CalendarYear

@Suppress("FunctionName")
internal fun LazyListScope.YearCalendarMonths(
    yearCount: Int,
    yearData: (offset: Int) -> CalendarYear,
    columns: Int,
    monthVerticalSpacing: Dp,
    monthHorizontalSpacing: Dp,
    yearBodyContentPadding: PaddingValues,
    contentHeightMode: YearContentHeightMode,
    isMonthVisible: (month: CalendarMonth) -> Boolean,
    dayContent: @Composable BoxScope.(CalendarDay) -> Unit,
    monthHeader: (@Composable ColumnScope.(CalendarMonth) -> Unit)?,
    monthBody: (@Composable ColumnScope.(CalendarMonth, content: @Composable () -> Unit) -> Unit)?,
    monthFooter: (@Composable ColumnScope.(CalendarMonth) -> Unit)?,
    monthContainer: (@Composable BoxScope.(CalendarMonth, container: @Composable () -> Unit) -> Unit)?,
    yearHeader: (@Composable ColumnScope.(CalendarYear) -> Unit)?,
    yearBody: (@Composable ColumnScope.(CalendarYear, content: @Composable () -> Unit) -> Unit)?,
    yearFooter: (@Composable ColumnScope.(CalendarYear) -> Unit)?,
    yearContainer: (@Composable LazyItemScope.(CalendarYear, container: @Composable () -> Unit) -> Unit)?,
) {
    items(
        count = yearCount,
        key = { offset -> yearData(offset).year },
    ) { yearOffset ->
        val year = yearData(yearOffset)
        val fillHeight = when (contentHeightMode) {
            YearContentHeightMode.Wrap -> false
            YearContentHeightMode.Fill,
            YearContentHeightMode.Stretch,
            -> true
        }
        val hasYearContainer = yearContainer != null
        yearContainer.or(defaultYearContainer)(year) {
            Column(
                modifier = Modifier
                    .then(if (hasYearContainer) Modifier.fillMaxWidth() else Modifier.fillParentMaxWidth())
                    .then(
                        if (fillHeight) {
                            if (hasYearContainer) Modifier.fillMaxHeight() else Modifier.fillParentMaxHeight()
                        } else {
                            Modifier.wrapContentHeight()
                        },
                    ),
            ) {
                val months = year.months.filter(isMonthVisible)
                yearHeader?.invoke(this, year)
                yearBody.or(defaultYearBody)(year) {
                    CalendarGrid(
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(if (fillHeight) Modifier.weight(1f) else Modifier.wrapContentHeight())
                            .padding(yearBodyContentPadding),
                        columns = columns,
                        itemCount = months.count(),
                        fillHeight = fillHeight,
                        monthVerticalSpacing = monthVerticalSpacing,
                        monthHorizontalSpacing = monthHorizontalSpacing,
                    ) { monthOffset ->
                        val month = months[monthOffset]
                        val hasContainer = monthContainer != null
                        monthContainer.or(defaultMonthContainer)(month) {
                            Column(
                                modifier = Modifier
                                    .then(if (hasContainer) Modifier.fillMaxWidth() else Modifier)
                                    .then(
                                        if (fillHeight) {
                                            if (hasContainer) Modifier.fillMaxHeight() else Modifier
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
                                                    .then(
                                                        if (contentHeightMode == YearContentHeightMode.Stretch) {
                                                            Modifier.weight(1f)
                                                        } else {
                                                            Modifier.wrapContentHeight()
                                                        },
                                                    ),
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
                yearFooter?.invoke(this, year)
            }
        }
    }
}

@Composable
private fun CalendarGrid(
    columns: Int,
    fillHeight: Boolean,
    monthVerticalSpacing: Dp,
    monthHorizontalSpacing: Dp,
    itemCount: Int,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.(Int) -> Unit,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(monthVerticalSpacing),
    ) {
        var rows = (itemCount / columns)
        if (itemCount.mod(columns) > 0) {
            rows += 1
        }

        for (rowId in 0 until rows) {
            val firstIndex = rowId * columns

            Row(
                modifier = Modifier.then(
                    if (fillHeight) Modifier.weight(1f) else Modifier,
                ),
                horizontalArrangement = Arrangement.spacedBy(monthHorizontalSpacing),
            ) {
                for (columnId in 0 until columns) {
                    val index = firstIndex + columnId
                    Box(
                        modifier = Modifier
                            .weight(1f),
                    ) {
                        if (index < itemCount) {
                            content(index)
                        }
                    }
                }
            }
        }
    }
}

private val defaultYearContainer: (@Composable LazyItemScope.(CalendarYear, container: @Composable () -> Unit) -> Unit) =
    { _, container -> container() }

private val defaultYearBody: (@Composable ColumnScope.(CalendarYear, content: @Composable () -> Unit) -> Unit) =
    { _, content -> content() }

private val defaultMonthContainer: (@Composable BoxScope.(CalendarMonth, container: @Composable () -> Unit) -> Unit) =
    { _, container -> container() }

private val defaultMonthBody: (@Composable ColumnScope.(CalendarMonth, content: @Composable () -> Unit) -> Unit) =
    { _, content -> content() }
