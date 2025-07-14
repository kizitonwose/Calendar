package com.kizitonwose.calendar.compose.yearcalendar

import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.ScrollScope
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.lazy.LazyListLayoutInfo
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.kizitonwose.calendar.compose.CalendarInfo
import com.kizitonwose.calendar.compose.CalendarLayoutInfo
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.CalendarMonth
import com.kizitonwose.calendar.core.CalendarYear
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.ExperimentalCalendarApi
import com.kizitonwose.calendar.core.OutDateStyle
import com.kizitonwose.calendar.core.Year
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import com.kizitonwose.calendar.data.DataStore
import com.kizitonwose.calendar.data.VisibleItemState
import com.kizitonwose.calendar.data.checkRange
import com.kizitonwose.calendar.data.daysUntil
import com.kizitonwose.calendar.data.getCalendarYearData
import com.kizitonwose.calendar.data.getYearIndex
import com.kizitonwose.calendar.data.getYearIndicesCount
import com.kizitonwose.calendar.data.indexOfFirstOrNull
import com.kizitonwose.calendar.data.log
import com.kizitonwose.calendar.data.positionYearMonth
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth

/**
 * Creates a [YearCalendarState] that is remembered across compositions.
 *
 * @param startYear the initial value for [YearCalendarState.startYear]
 * @param endYear the initial value for [YearCalendarState.endYear]
 * @param firstDayOfWeek the initial value for [YearCalendarState.firstDayOfWeek]
 * @param firstVisibleYear the initial value for [YearCalendarState.firstVisibleYear]
 * @param outDateStyle the initial value for [YearCalendarState.outDateStyle]
 */
@ExperimentalCalendarApi
@Composable
public fun rememberYearCalendarState(
    startYear: Year = Year.now(),
    endYear: Year = startYear,
    firstVisibleYear: Year = startYear,
    firstDayOfWeek: DayOfWeek = firstDayOfWeekFromLocale(),
    outDateStyle: OutDateStyle = OutDateStyle.EndOfRow,
): YearCalendarState {
    return rememberSaveable(
        inputs = arrayOf(
            startYear,
            endYear,
            firstVisibleYear,
            firstDayOfWeek,
            outDateStyle,
        ),
        saver = YearCalendarState.Saver,
    ) {
        YearCalendarState(
            startYear = startYear,
            endYear = endYear,
            firstDayOfWeek = firstDayOfWeek,
            firstVisibleYear = firstVisibleYear,
            outDateStyle = outDateStyle,
            visibleItemState = null,
        )
    }
}

/**
 * A state object that can be hoisted to control and observe calendar properties.
 *
 * This should be created via [rememberYearCalendarState].
 *
 * @param startYear the first month on the calendar.
 * @param endYear the last month on the calendar.
 * @param firstDayOfWeek the first day of week on the calendar.
 * @param firstVisibleYear the initial value for [YearCalendarState.firstVisibleYear]
 * @param outDateStyle the preferred style for out date generation.
 */
@Stable
public class YearCalendarState internal constructor(
    startYear: Year,
    endYear: Year,
    firstDayOfWeek: DayOfWeek,
    firstVisibleYear: Year,
    outDateStyle: OutDateStyle,
    visibleItemState: VisibleItemState?,
) : ScrollableState {
    /** Backing state for [startYear] */
    private var _startYear by mutableStateOf(startYear)

    /** The first year on the calendar. */
    public var startYear: Year
        get() = _startYear
        set(value) {
            if (value != startYear) {
                _startYear = value
                yearDataChanged()
            }
        }

    /** Backing state for [endYear] */
    private var _endYear by mutableStateOf(endYear)

    /** The last year on the calendar. */
    public var endYear: Year
        get() = _endYear
        set(value) {
            if (value != endYear) {
                _endYear = value
                yearDataChanged()
            }
        }

    /** Backing state for [firstDayOfWeek] */
    private var _firstDayOfWeek by mutableStateOf(firstDayOfWeek)

    /** The first day of week on the calendar. */
    public var firstDayOfWeek: DayOfWeek
        get() = _firstDayOfWeek
        set(value) {
            if (value != firstDayOfWeek) {
                _firstDayOfWeek = value
                yearDataChanged()
            }
        }

    /** Backing state for [outDateStyle] */
    private var _outDateStyle by mutableStateOf(outDateStyle)

    /** The preferred style for out date generation. */
    public var outDateStyle: OutDateStyle
        get() = _outDateStyle
        set(value) {
            if (value != outDateStyle) {
                _outDateStyle = value
                yearDataChanged()
            }
        }

    /**
     * The first year that is visible.
     *
     * @see [lastVisibleYear]
     */
    public val firstVisibleYear: CalendarYear by derivedStateOf {
        store[listState.firstVisibleItemIndex]
    }

    /**
     * The last year that is visible.
     *
     * @see [firstVisibleYear]
     */
    public val lastVisibleYear: CalendarYear by derivedStateOf {
        store[listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0]
    }

    /**
     * The object of [CalendarLayoutInfo] calculated during the last layout pass. For example,
     * you can use it to calculate what items are currently visible.
     *
     * Note that this property is observable and is updated after every scroll or remeasure.
     * If you use it in the composable function it will be recomposed on every change causing
     * potential performance issues including infinity recomposition loop.
     * Therefore, avoid using it in the composition.
     *
     * If you need to use it in the composition then consider wrapping the calculation into a
     * derived state in order to only have recompositions when the derived value changes.
     * See Example6Page in the sample app for usage.
     *
     * If you want to run some side effects like sending an analytics event or updating a state
     * based on this value consider using "snapshotFlow".
     *
     * see [LazyListLayoutInfo]
     */
    public val layoutInfo: YearCalendarLayoutInfo
        get() = YearCalendarLayoutInfo(listState.layoutInfo) { index -> store[index] }

    /**
     * [InteractionSource] that will be used to dispatch drag events when this
     * calendar is being dragged. If you want to know whether the fling (or animated scroll) is in
     * progress, use [isScrollInProgress].
     */
    public val interactionSource: InteractionSource
        get() = listState.interactionSource

    internal val listState = LazyListState(
        firstVisibleItemIndex = visibleItemState?.firstVisibleItemIndex
            ?: getScrollIndex(firstVisibleYear) ?: 0,
        firstVisibleItemScrollOffset = visibleItemState?.firstVisibleItemScrollOffset ?: 0,
    )

    internal val placementInfo = YearItemPlacementInfo()

    internal var calendarInfo by mutableStateOf(CalendarInfo(indexCount = 0))

    internal val store = DataStore { offset ->
        getCalendarYearData(
            startYear = this.startYear,
            offset = offset,
            firstDayOfWeek = this.firstDayOfWeek,
            outDateStyle = this.outDateStyle,
        )
    }

    init {
        yearDataChanged() // Update indexCount initially.
    }

    private fun yearDataChanged() {
        store.clear()
        checkRange(startYear, endYear)
        // Read the firstDayOfWeek and outDateStyle properties to ensure recomposition
        // even though they are unused in the CalendarInfo. Alternatively, we could use
        // mutableStateMapOf() as the backing store for DataStore() to ensure recomposition
        // but not sure how compose handles recomposition of a lazy list that reads from
        // such map when an entry unrelated to the visible indices changes.
        calendarInfo = CalendarInfo(
            indexCount = getYearIndicesCount(startYear, endYear),
            firstDayOfWeek = firstDayOfWeek,
            outDateStyle = outDateStyle,
        )
    }

    /**
     * Instantly brings the [year] to the top of the viewport.
     *
     * @param year the year to which to scroll. Must be within the
     * range of [startYear] and [endYear].
     *
     * @see [animateScrollToYear]
     */
    public suspend fun scrollToYear(year: Year) {
        listState.scrollToItem(getScrollIndex(year) ?: return)
    }

    /**
     * Animate (smooth scroll) to the given [year].
     *
     * @param year the year to which to scroll. Must be within the
     * range of [startYear] and [endYear].
     *
     * @see [scrollToYear]
     */
    public suspend fun animateScrollToYear(year: Year) {
        listState.animateScrollToItem(getScrollIndex(year) ?: return)
    }

    /**
     * Instantly brings the [month] to the top of the viewport.
     *
     * @param month the month to which to scroll. Must be within the
     * range of [startYear] and [endYear].
     *
     * @see [animateScrollToMonth]
     */
    public suspend fun scrollToMonth(month: YearMonth): Unit =
        scrollToMonth(month, animate = false)

    /**
     * Animate (smooth scroll) to the given [month].
     *
     * @param month the month to which to scroll. Must be within the
     * range of [startYear] and [endYear].
     *
     * @see [scrollToMonth]
     */
    public suspend fun animateScrollToMonth(month: YearMonth): Unit =
        scrollToMonth(month, animate = true)

    /**
     * Instantly brings the [date] to the top of the viewport.
     *
     * @param date the date to which to scroll. Must be within the
     * range of [startYear] and [endYear].
     * @param position the position of the date in the month.
     *
     * @see [animateScrollToDate]
     */
    public suspend fun scrollToDate(
        date: LocalDate,
        position: DayPosition = DayPosition.MonthDate,
    ): Unit = scrollToDay(CalendarDay(date, position))

    /**
     * Animate (smooth scroll) to the given [date].
     *
     * @param date the date to which to scroll. Must be within the
     * range of [startYear] and [endYear].
     * @param position the position of the date in the month.
     *
     * @see [scrollToDate]
     */
    public suspend fun animateScrollToDate(
        date: LocalDate,
        position: DayPosition = DayPosition.MonthDate,
    ): Unit = animateScrollToDay(CalendarDay(date, position))

    /**
     * Instantly brings the [day] to the top of the viewport.
     *
     * @param day the day to which to scroll. Must be within the
     * range of [startYear] and [endYear].
     *
     * @see [animateScrollToDay]
     */
    public suspend fun scrollToDay(day: CalendarDay): Unit =
        scrollToDay(day, animate = false)

    /**
     * Animate (smooth scroll) to the given [day].
     *
     * @param day the day to which to scroll. Must be within the
     * range of [startYear] and [endYear].
     *
     * @see [scrollToDay]
     */
    public suspend fun animateScrollToDay(day: CalendarDay): Unit =
        scrollToDay(day, animate = true)

    private suspend fun scrollToDay(day: CalendarDay, animate: Boolean) {
        val yearMonth = day.positionYearMonth
        val yearIndex = getScrollIndex(Year(yearMonth.year)) ?: return
        val year = store[yearIndex]
        val visibleMonths = placementInfo.isMonthVisible.apply(year.months)
        val monthIndex = visibleMonths.indexOfFirstOrNull { it.yearMonth == yearMonth } ?: return
        val weeksOfMonth = visibleMonths[monthIndex].weekDays
        val dayIndex = when (layoutInfo.orientation) {
            Orientation.Vertical -> weeksOfMonth.indexOfFirstOrNull { it.contains(day) }
            Orientation.Horizontal -> firstDayOfWeek.daysUntil(day.date.dayOfWeek)
        } ?: return
        val monthDayInfo = placementInfo.awaitFistMonthDayOffsetAndSize(layoutInfo.orientation) ?: return
        val monthGridOffset = monthDayInfo.monthGridOffset(
            monthIndex = monthIndex,
            columnCount = placementInfo.monthColumns,
            visibleMonths = visibleMonths,
        )
        val scrollOffset = monthGridOffset +
            monthDayInfo.monthOffsetInContainer +
            monthDayInfo.dayOffsetInMonth +
            (monthDayInfo.daySize * dayIndex)
        if (animate) {
            listState.animateScrollToItem(yearIndex, scrollOffset)
        } else {
            listState.scrollToItem(yearIndex, scrollOffset)
        }
    }

    private suspend fun scrollToMonth(yearMonth: YearMonth, animate: Boolean) {
        val yearIndex = getScrollIndex(Year(yearMonth.year)) ?: return
        val year = store[yearIndex]
        val visibleMonths = placementInfo.isMonthVisible.apply(year.months)
        val monthIndex = visibleMonths.indexOfFirstOrNull {
            it.yearMonth == yearMonth
        } ?: return
        val monthDayInfo = placementInfo.awaitFistMonthDayOffsetAndSize(layoutInfo.orientation) ?: return
        val monthGridOffset = monthDayInfo.monthGridOffset(
            monthIndex = monthIndex,
            columnCount = placementInfo.monthColumns,
            visibleMonths = visibleMonths,
        )
        val scrollOffset = monthGridOffset + monthDayInfo.monthOffsetInContainer
        if (animate) {
            listState.animateScrollToItem(yearIndex, scrollOffset)
        } else {
            listState.scrollToItem(yearIndex, scrollOffset)
        }
    }

    private fun YearItemPlacementInfo.OffsetSize.monthGridOffset(
        monthIndex: Int,
        columnCount: Int,
        visibleMonths: List<CalendarMonth>,
    ): Int {
        val (monthRow, monthColumn) = rowColumn(
            monthIndex = monthIndex,
            monthColumns = columnCount,
        )
        val orientation = layoutInfo.orientation
        val isUniformOrientationGrid = when (orientation) {
            Orientation.Vertical -> when (placementInfo.contentHeightMode) {
                // Variable month height, we need to do some more work.
                YearContentHeightMode.Wrap -> false
                // Equal month height, we can multiply reliably.
                YearContentHeightMode.Fill,
                YearContentHeightMode.Stretch,
                -> true
            }

            // Equal month width, we can multiply reliably.
            Orientation.Horizontal -> true
        }
        val spacingMultiplier = when (orientation) {
            Orientation.Vertical -> monthRow
            Orientation.Horizontal -> monthColumn
        }
        return if (isUniformOrientationGrid) {
            spacingMultiplier * (monthSize + monthSpacing)
        } else {
            val offset = visibleMonths
                .take(monthIndex + 1) // remove non relevant rows
                .chunked(columnCount)
                .dropLast(1) // remove the month row
                .sumOf { row ->
                    row.maxOf {
                        // dayBodyCount * daySize is fine because this is only relevant
                        // in vertical wrap mode so the extra space will be the month
                        // decorations (header, week day name, footer) as the month
                        // container is not stretched in this case.
                        val monthSizeWithoutDays = monthSize - (dayBodyCount * daySize)
                        monthSizeWithoutDays + (it.weekDays.count() * daySize)
                    }
                }
            offset + (spacingMultiplier * monthSpacing)
        }
    }

    private fun getScrollIndex(year: Year): Int? {
        if (year !in startYear..endYear) {
            log("YearCalendarState", "Attempting to scroll out of range: $year")
            return null
        }
        return getYearIndex(startYear, year)
    }

    /**
     * Whether this [ScrollableState] is currently scrolling by gesture, fling or programmatically.
     */
    override val isScrollInProgress: Boolean
        get() = listState.isScrollInProgress

    override fun dispatchRawDelta(delta: Float): Float = listState.dispatchRawDelta(delta)

    override suspend fun scroll(
        scrollPriority: MutatePriority,
        block: suspend ScrollScope.() -> Unit,
    ): Unit = listState.scroll(scrollPriority, block)

    public companion object {
        internal val Saver: Saver<YearCalendarState, Any> = listSaver(
            save = {
                listOf(
                    it.startYear.value,
                    it.endYear.value,
                    it.firstVisibleYear.year.value,
                    it.firstDayOfWeek.ordinal,
                    it.outDateStyle.ordinal,
                    it.listState.firstVisibleItemIndex,
                    it.listState.firstVisibleItemScrollOffset,
                )
            },
            restore = {
                YearCalendarState(
                    startYear = Year(it[0]),
                    endYear = Year(it[1]),
                    firstVisibleYear = Year(it[2]),
                    firstDayOfWeek = DayOfWeek.entries[it[3]],
                    outDateStyle = OutDateStyle.entries[it[4]],
                    visibleItemState = VisibleItemState(
                        firstVisibleItemIndex = it[5],
                        firstVisibleItemScrollOffset = it[6],
                    ),
                )
            },
        )
    }
}
