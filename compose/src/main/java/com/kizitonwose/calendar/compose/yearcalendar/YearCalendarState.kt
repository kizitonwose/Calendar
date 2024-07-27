package com.kizitonwose.calendar.compose.yearcalendar

import android.util.Log
import androidx.compose.foundation.MutatePriority
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
import com.kizitonwose.calendar.compose.VisibleItemState
import com.kizitonwose.calendar.core.CalendarYear
import com.kizitonwose.calendar.core.ExperimentalCalendarApi
import com.kizitonwose.calendar.core.OutDateStyle
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import com.kizitonwose.calendar.data.DataStore
import com.kizitonwose.calendar.data.checkRange
import com.kizitonwose.calendar.data.getCalendarYearData
import com.kizitonwose.calendar.data.getYearIndex
import com.kizitonwose.calendar.data.getYearIndicesCount
import java.time.DayOfWeek
import java.time.Year

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
     */
    public suspend fun animateScrollToYear(year: Year) {
        listState.animateScrollToItem(getScrollIndex(year) ?: return)
    }

    private fun getScrollIndex(year: Year): Int? {
        if (year !in startYear..endYear) {
            Log.d("YearCalendarState", "Attempting to scroll out of range: $year")
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
                    it.startYear,
                    it.endYear,
                    it.firstVisibleYear.year,
                    it.firstDayOfWeek,
                    it.outDateStyle,
                    it.listState.firstVisibleItemIndex,
                    it.listState.firstVisibleItemScrollOffset,
                )
            },
            restore = {
                YearCalendarState(
                    startYear = it[0] as Year,
                    endYear = it[1] as Year,
                    firstVisibleYear = it[2] as Year,
                    firstDayOfWeek = it[3] as DayOfWeek,
                    outDateStyle = it[4] as OutDateStyle,
                    visibleItemState = VisibleItemState(
                        firstVisibleItemIndex = it[5] as Int,
                        firstVisibleItemScrollOffset = it[6] as Int,
                    ),
                )
            },
        )
    }
}
