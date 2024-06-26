package calendar.compose

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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import calendar.core.OutDateStyle
import calendar.data.DataStore
import calendar.data.MonthDataProducer
import calendar.data.VisibleItemState
import calendar.data.checkDateRange
import kotlinx.datetime.DayOfWeek

@Composable
internal fun <YearMonth : Comparable<YearMonth>, CalendarMonth> rememberCalendarStateImpl(
    startMonth: YearMonth,
    endMonth: YearMonth,
    firstVisibleMonth: YearMonth,
    firstDayOfWeek: DayOfWeek,
    outDateStyle: OutDateStyle,
    monthDataProducer: MonthDataProducer<YearMonth, CalendarMonth>,
): CalendarState<YearMonth, CalendarMonth> {
    return rememberSaveable(
        inputs = arrayOf(
            startMonth,
            endMonth,
            firstVisibleMonth,
            firstDayOfWeek,
            outDateStyle,
        ),
        saver = monthDataProducer.saver,
    ) {
        CalendarState(
            startMonth = startMonth,
            endMonth = endMonth,
            firstDayOfWeek = firstDayOfWeek,
            firstVisibleMonth = firstVisibleMonth,
            outDateStyle = outDateStyle,
            visibleItemState = null,
            data = monthDataProducer,
        )
    }
}

/**
 * A state object that can be hoisted to control and observe calendar properties.
 *
 * This should be created via [rememberCalendarState].
 *
 * @param startMonth the first month on the calendar.
 * @param endMonth the last month on the calendar.
 * @param firstDayOfWeek the first day of week on the calendar.
 * @param firstVisibleMonth the initial value for [CalendarState.firstVisibleMonth]
 * @param outDateStyle the preferred style for out date generation.
 */
@Stable
class CalendarState<YearMonth, CalendarMonth> internal constructor(
    startMonth: YearMonth,
    endMonth: YearMonth,
    firstDayOfWeek: DayOfWeek,
    firstVisibleMonth: YearMonth,
    outDateStyle: OutDateStyle,
    visibleItemState: VisibleItemState?,
    private val data: MonthDataProducer<YearMonth, CalendarMonth>,
) : ScrollableState where YearMonth : Comparable<YearMonth> {
    /** Backing state for [startMonth] */
    private var _startMonth by mutableStateOf(startMonth)

    /** The first month on the calendar. */
    var startMonth: YearMonth
        get() = _startMonth
        set(value) {
            if (value != startMonth) {
                _startMonth = value
                monthDataChanged()
            }
        }

    /** Backing state for [endMonth] */
    private var _endMonth by mutableStateOf(endMonth)

    /** The last month on the calendar. */
    var endMonth: YearMonth
        get() = _endMonth
        set(value) {
            if (value != endMonth) {
                _endMonth = value
                monthDataChanged()
            }
        }

    /** Backing state for [firstDayOfWeek] */
    private var _firstDayOfWeek by mutableStateOf(firstDayOfWeek)

    /** The first day of week on the calendar. */
    var firstDayOfWeek: DayOfWeek
        get() = _firstDayOfWeek
        set(value) {
            if (value != firstDayOfWeek) {
                _firstDayOfWeek = value
                monthDataChanged()
            }
        }

    /** Backing state for [outDateStyle] */
    private var _outDateStyle by mutableStateOf(outDateStyle)

    /** The preferred style for out date generation. */
    var outDateStyle: OutDateStyle
        get() = _outDateStyle
        set(value) {
            if (value != outDateStyle) {
                _outDateStyle = value
                monthDataChanged()
            }
        }

    /**
     * The first month that is visible.
     *
     * @see [lastVisibleMonth]
     */
    val firstVisibleMonth: CalendarMonth by derivedStateOf {
        store[listState.firstVisibleItemIndex]
    }

    /**
     * The last month that is visible.
     *
     * @see [firstVisibleMonth]
     */
    val lastVisibleMonth: CalendarMonth by derivedStateOf {
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
    val layoutInfo: CalendarLayoutInfo<CalendarMonth>
        get() = CalendarLayoutInfo(listState.layoutInfo) { index -> store[index] }

    /**
     * [InteractionSource] that will be used to dispatch drag events when this
     * calendar is being dragged. If you want to know whether the fling (or animated scroll) is in
     * progress, use [isScrollInProgress].
     */
    val interactionSource: InteractionSource
        get() = listState.interactionSource

    internal val listState = LazyListState(
        firstVisibleItemIndex = visibleItemState?.firstVisibleItemIndex
            ?: getScrollIndex(firstVisibleMonth) ?: 0,
        firstVisibleItemScrollOffset = visibleItemState?.firstVisibleItemScrollOffset ?: 0,
    )

    internal var calendarInfo by mutableStateOf(CalendarInfo(indexCount = 0))

    internal val store = DataStore { offset ->
        data.getCalendarMonthData(
            startMonth = this.startMonth,
            offset = offset,
            firstDayOfWeek = this.firstDayOfWeek,
            outDateStyle = this.outDateStyle,
        )
    }

    init {
        monthDataChanged() // Update monthIndexCount initially.
    }

    private fun monthDataChanged() {
        store.clear()
        checkDateRange(startMonth, endMonth)
        // Read the firstDayOfWeek and outDateStyle properties to ensure recomposition
        // even though they are unused in the CalendarInfo. Alternatively, we could use
        // mutableStateMapOf() as the backing store for DataStore() to ensure recomposition
        // but not sure how compose handles recomposition of a lazy list that reads from
        // such map when an entry unrelated to the visible indices changes.
        calendarInfo = CalendarInfo(
            indexCount = data.getMonthIndicesCount(startMonth, endMonth),
            firstDayOfWeek = firstDayOfWeek,
            outDateStyle = outDateStyle,
        )
    }

    /**
     * Instantly brings the [month] to the top of the viewport.
     *
     * @param month the month to which to scroll. Must be within the
     * range of [startMonth] and [endMonth].
     *
     * @see [animateScrollToMonth]
     */
    suspend fun scrollToMonth(month: YearMonth) {
        listState.scrollToItem(getScrollIndex(month) ?: return)
    }

    /**
     * Animate (smooth scroll) to the given [month].
     *
     * @param month the month to which to scroll. Must be within the
     * range of [startMonth] and [endMonth].
     */
    suspend fun animateScrollToMonth(month: YearMonth) {
        listState.animateScrollToItem(getScrollIndex(month) ?: return)
    }

    private fun getScrollIndex(month: YearMonth): Int? {
        if (month !in startMonth..endMonth) {
            // TODO KMP
//            Log.d("CalendarState", "Attempting to scroll out of range: $month")
            return null
        }
        return data.getMonthIndex(startMonth, month)
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
    ) = listState.scroll(scrollPriority, block)
}
