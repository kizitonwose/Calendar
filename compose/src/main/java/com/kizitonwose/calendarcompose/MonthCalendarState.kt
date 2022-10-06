package com.kizitonwose.calendarcompose

import android.util.Log
import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.gestures.ScrollScope
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.LazyListLayoutInfo
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.kizitonwose.calendarcore.CalendarMonth
import com.kizitonwose.calendardata.*
import java.io.Serializable
import java.time.DayOfWeek
import java.time.YearMonth

internal class VisibleItemState(
    val firstVisibleItemIndex: Int = 0,
    val firstVisibleItemScrollOffset: Int = 0,
) : Serializable

abstract class MonthCalendarState internal constructor(
    startMonth: YearMonth,
    endMonth: YearMonth,
    firstVisibleMonth: YearMonth,
    firstDayOfWeek: DayOfWeek,
    visibleItemState: VisibleItemState?,
) : ScrollableState {

    private var _startMonth by mutableStateOf(startMonth)
    var startMonth: YearMonth
        get() = _startMonth
        set(value) {
            if (value != startMonth) {
                _startMonth = value
                monthDataChanged()
            }
        }

    private var _endMonth by mutableStateOf(endMonth)
    var endMonth: YearMonth
        get() = _endMonth
        set(value) {
            if (value != endMonth) {
                _endMonth = value
                monthDataChanged()
            }
        }

    private var _firstDayOfWeek by mutableStateOf(firstDayOfWeek)
    var firstDayOfWeek: DayOfWeek
        get() = _firstDayOfWeek
        set(value) {
            if (value != firstDayOfWeek) {
                _firstDayOfWeek = value
                monthDataChanged()
            }
        }

    internal val listState = LazyListState(
        firstVisibleItemIndex = visibleItemState?.firstVisibleItemIndex
            ?: getScrollIndex(firstVisibleMonth) ?: 0,
        firstVisibleItemScrollOffset = visibleItemState?.firstVisibleItemScrollOffset ?: 0
    )

    internal var monthIndexCount by mutableStateOf(0)

    internal val store = DataStore { offset ->
        getMonthData(startMonth, offset, firstDayOfWeek).calendarMonth
    }

    val firstVisibleMonth: CalendarMonth
        get() = store[listState.firstVisibleItemIndex]

    val lastVisibleMonth: CalendarMonth
        get() = store[listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0]

    val layoutInfo: CalendarLayoutInfo
        get() = CalendarLayoutInfo(listState.layoutInfo) { index -> store[index] }

    init {
        monthDataChanged() // Update monthIndexCount initially.
    }

    protected fun monthDataChanged() {
        store.clear()
        checkDateRange(startMonth, endMonth)
        monthIndexCount = getMonthIndicesCount(startMonth, endMonth)
    }

    abstract fun getMonthData(
        startMonth: YearMonth,
        offset: Int,
        firstDayOfWeek: DayOfWeek,
    ): MonthData

    suspend fun scrollToMonth(month: YearMonth) {
        listState.scrollToItem(getScrollIndex(month) ?: return)
    }

    suspend fun animateScrollToMonth(month: YearMonth) {
        listState.animateScrollToItem(getScrollIndex(month) ?: return)
    }

    private fun getScrollIndex(month: YearMonth): Int? {
        if (month !in startMonth..endMonth) {
            Log.d("CalendarState", "Attempting to scroll out of range: $month")
            return null
        }
        return getMonthIndex(startMonth, month)
    }

    override val isScrollInProgress: Boolean get() = listState.isScrollInProgress

    override fun dispatchRawDelta(delta: Float): Float = listState.dispatchRawDelta(delta)

    override suspend fun scroll(
        scrollPriority: MutatePriority,
        block: suspend ScrollScope.() -> Unit,
    ) = listState.scroll(scrollPriority, block)

}

class CalendarLayoutInfo(info: LazyListLayoutInfo, private val month: (Int) -> CalendarMonth) :
    LazyListLayoutInfo by info {
    val visibleMonthsInfo: List<CalendarItemInfo>
        get() = visibleItemsInfo.map {
            CalendarItemInfo(it, month(it.index))
        }
}

class CalendarItemInfo(info: LazyListItemInfo, val month: CalendarMonth) : LazyListItemInfo by info