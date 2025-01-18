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
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.unit.Dp
import com.kizitonwose.calendar.compose.or
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.CalendarMonth
import com.kizitonwose.calendar.core.CalendarYear
import kotlin.math.min

@Suppress("FunctionName")
internal fun LazyListScope.YearCalendarMonths(
    yearCount: Int,
    yearData: (offset: Int) -> CalendarYear,
    monthColumns: Int,
    monthVerticalSpacing: Dp,
    monthHorizontalSpacing: Dp,
    yearBodyContentPadding: PaddingValues,
    contentHeightMode: YearContentHeightMode,
    isMonthVisible: ((month: CalendarMonth) -> Boolean)?,
    dayContent: @Composable BoxScope.(CalendarDay) -> Unit,
    monthHeader: (@Composable ColumnScope.(CalendarMonth) -> Unit)?,
    monthBody: (@Composable ColumnScope.(CalendarMonth, content: @Composable () -> Unit) -> Unit)?,
    monthFooter: (@Composable ColumnScope.(CalendarMonth) -> Unit)?,
    monthContainer: (@Composable BoxScope.(CalendarMonth, container: @Composable () -> Unit) -> Unit)?,
    yearHeader: (@Composable ColumnScope.(CalendarYear) -> Unit)?,
    yearBody: (@Composable ColumnScope.(CalendarYear, content: @Composable () -> Unit) -> Unit)?,
    yearFooter: (@Composable ColumnScope.(CalendarYear) -> Unit)?,
    yearContainer: (@Composable LazyItemScope.(CalendarYear, container: @Composable () -> Unit) -> Unit)?,
    onItemPlaced: (itemCoordinates: YearItemCoordinates) -> Unit,
) {
    items(
        count = yearCount,
        key = { offset -> yearData(offset).year.value },
    ) { yearOffset ->
        val year = yearData(yearOffset)
        val fillHeight = when (contentHeightMode) {
            YearContentHeightMode.Wrap -> false
            YearContentHeightMode.Fill,
            YearContentHeightMode.Stretch,
            -> true
        }
        val months = isMonthVisible.apply(year.months)
        val hasYearContainer = yearContainer != null
        val currentOnItemPlaced by rememberUpdatedState(onItemPlaced)
        val itemCoordinatesStore = remember(year.year) {
            YearItemCoordinatesStore(months.first(), currentOnItemPlaced)
        }
        Box(Modifier.onPlaced(itemCoordinatesStore::onItemRootPlaced)) {
            yearContainer.or(defaultYearContainer)(year) {
                Column(
                    modifier = Modifier
                        .then(
                            if (hasYearContainer) {
                                Modifier.fillMaxWidth()
                            } else {
                                Modifier.fillParentMaxWidth()
                            },
                        )
                        .then(
                            if (fillHeight) {
                                if (hasYearContainer) {
                                    Modifier.fillMaxHeight()
                                } else {
                                    Modifier.fillParentMaxHeight()
                                }
                            } else {
                                Modifier.wrapContentHeight()
                            },
                        ),
                ) {
                    yearHeader?.invoke(this, year)
                    yearBody.or(defaultYearBody)(year) {
                        CalendarGrid(
                            modifier = Modifier
                                .fillMaxWidth()
                                .then(if (fillHeight) Modifier.weight(1f) else Modifier.wrapContentHeight())
                                .padding(yearBodyContentPadding),
                            monthColumns = monthColumns,
                            monthCount = months.count(),
                            fillHeight = fillHeight,
                            monthVerticalSpacing = monthVerticalSpacing,
                            monthHorizontalSpacing = monthHorizontalSpacing,
                            onFirstMonthPlaced = itemCoordinatesStore::onFirstMonthPlaced,
                        ) { monthOffset ->
                            val month = months[monthOffset]
                            val hasMonthContainer = monthContainer != null
                            monthContainer.or(defaultMonthContainer)(month) {
                                Column(
                                    modifier = Modifier
                                        .then(if (hasMonthContainer) Modifier.fillMaxWidth() else Modifier)
                                        .then(
                                            if (fillHeight) {
                                                if (hasMonthContainer) Modifier.fillMaxHeight() else Modifier
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
                                                            if (contentHeightMode == YearContentHeightMode.Stretch) {
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
                                                                    monthIndex = monthOffset,
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
                    yearFooter?.invoke(this, year)
                }
            }
        }
    }
}

@Composable
private inline fun CalendarGrid(
    monthColumns: Int,
    fillHeight: Boolean,
    monthVerticalSpacing: Dp,
    monthHorizontalSpacing: Dp,
    monthCount: Int,
    modifier: Modifier = Modifier,
    noinline onFirstMonthPlaced: (coordinates: LayoutCoordinates) -> Unit,
    crossinline content: @Composable BoxScope.(Int) -> Unit,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(monthVerticalSpacing),
    ) {
        val rows = (monthCount / monthColumns) + min(monthCount % monthColumns, 1)
        for (rowId in 0 until rows) {
            val firstIndex = rowId * monthColumns
            Row(
                modifier = Modifier.then(
                    if (fillHeight) Modifier.weight(1f) else Modifier,
                ),
                horizontalArrangement = Arrangement.spacedBy(monthHorizontalSpacing),
            ) {
                for (columnId in 0 until monthColumns) {
                    val index = firstIndex + columnId
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .onFirstMonthPlaced(index, onFirstMonthPlaced),
                    ) {
                        if (index < monthCount) {
                            content(index)
                        }
                    }
                }
            }
        }
    }
}

@Stable
internal class YearItemCoordinatesStore(
    private val firstMonth: CalendarMonth,
    private val onItemPlaced: (itemCoordinates: YearItemCoordinates) -> Unit,
) {
    private var itemRootCoordinates: LayoutCoordinates? = null
    private var firstDayCoordinates: LayoutCoordinates? = null
    private var firstMonthCoordinates: LayoutCoordinates? = null

    fun onItemRootPlaced(coordinates: LayoutCoordinates) {
        itemRootCoordinates = coordinates
        check()
    }

    fun onFirstMonthPlaced(coordinates: LayoutCoordinates) {
        firstMonthCoordinates = coordinates
        check()
    }

    fun onFirstDayPlaced(coordinates: LayoutCoordinates) {
        firstDayCoordinates = coordinates
        check()
    }

    private fun check() {
        val itemRootCoordinates = itemRootCoordinates ?: return
        val firstMonthCoordinates = firstMonthCoordinates ?: return
        val firstDayCoordinates = firstDayCoordinates ?: return
        val itemCoordinates = YearItemCoordinates(
            firstMonth = firstMonth,
            itemRootCoordinates = itemRootCoordinates,
            firstMonthCoordinates = firstMonthCoordinates,
            firstDayCoordinates = firstDayCoordinates,
        )
        onItemPlaced(itemCoordinates)
    }
}

private inline fun Modifier.onFirstDayPlaced(
    monthIndex: Int,
    dateRow: Int,
    dateColumn: Int,
    noinline onFirstDayPlaced: (coordinates: LayoutCoordinates) -> Unit,
) = if (monthIndex == 0 && dateRow == 0 && dateColumn == 0) {
    onPlaced(onFirstDayPlaced)
} else {
    this
}

private inline fun Modifier.onFirstMonthPlaced(
    monthIndex: Int,
    noinline onFirstMonthPlaced: (coordinates: LayoutCoordinates) -> Unit,
) = if (monthIndex == 0) {
    onPlaced(onFirstMonthPlaced)
} else {
    this
}

internal inline fun ((month: CalendarMonth) -> Boolean)?.apply(months: List<CalendarMonth>) = if (this != null) {
    months.filter(this).also {
        check(it.isNotEmpty()) {
            "Cannot remove all the months in a year, " +
                "use the startYear and endYear parameters to remove full years."
        }
    }
} else {
    months
}

internal fun rowColumn(monthIndex: Int, monthColumns: Int): Pair<Int, Int> {
    val row = monthIndex / monthColumns
    val column = monthIndex % monthColumns
    // val index = row * monthColumns + column
    return row to column
}

private val defaultYearContainer: (@Composable LazyItemScope.(CalendarYear, container: @Composable () -> Unit) -> Unit) =
    { _, container -> container() }

private val defaultYearBody: (@Composable ColumnScope.(CalendarYear, content: @Composable () -> Unit) -> Unit) =
    { _, content -> content() }

private val defaultMonthContainer: (@Composable BoxScope.(CalendarMonth, container: @Composable () -> Unit) -> Unit) =
    { _, container -> container() }

private val defaultMonthBody: (@Composable ColumnScope.(CalendarMonth, content: @Composable () -> Unit) -> Unit) =
    { _, content -> content() }
