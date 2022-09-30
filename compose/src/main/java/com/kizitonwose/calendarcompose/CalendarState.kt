package com.kizitonwose.calendarcompose

import android.os.Parcelable
import android.util.Log
import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.gestures.ScrollScope
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.LazyListLayoutInfo
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import kotlinx.parcelize.Parcelize
import java.time.DayOfWeek
import java.time.YearMonth
import java.time.temporal.ChronoUnit

@Parcelize
internal class VisibleItemState(
    val firstVisibleItemIndex: Int = 0,
    val firstVisibleItemScrollOffset: Int = 0,
) : Parcelable

@Stable
class CalendarState internal constructor(
    startMonth: YearMonth,
    endMonth: YearMonth,
    firstDayOfWeek: DayOfWeek,
    firstVisibleMonth: YearMonth,
    visibleItemState: VisibleItemState?,
) : ScrollableState {

    var startMonth by mutableStateOf(startMonth)

    var endMonth by mutableStateOf(endMonth)

    var firstDayOfWeek by mutableStateOf(firstDayOfWeek)

    internal val listState = LazyListState(
        firstVisibleItemIndex = visibleItemState?.firstVisibleItemIndex
            ?: getScrollIndex(firstVisibleMonth) ?: 0,
        firstVisibleItemScrollOffset = visibleItemState?.firstVisibleItemScrollOffset ?: 0
    )

    val firstVisibleMonth: YearMonth
        get() = startMonth.plusMonths(listState.firstVisibleItemIndex.toLong())

    val lastVisibleMonth: YearMonth
        get() = startMonth.plusMonths(
            listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index?.toLong() ?: 0
        )

    val layoutInfo: CalendarLayoutInfo
        get() = CalendarLayoutInfo(listState.layoutInfo, startMonth)

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
        return ChronoUnit.MONTHS.between(startMonth, month).toInt()
    }

    override val isScrollInProgress: Boolean get() = listState.isScrollInProgress

    override fun dispatchRawDelta(delta: Float): Float = listState.dispatchRawDelta(delta)

    override suspend fun scroll(
        scrollPriority: MutatePriority,
        block: suspend ScrollScope.() -> Unit
    ) = listState.scroll(scrollPriority, block)

    companion object {
        val Saver: Saver<CalendarState, *> = listSaver(
            save = {
                val visibleItemState = VisibleItemState(
                    firstVisibleItemIndex = it.listState.firstVisibleItemIndex,
                    firstVisibleItemScrollOffset = it.listState.firstVisibleItemScrollOffset
                )
                listOf(
                    it.startMonth,
                    it.endMonth,
                    it.firstVisibleMonth,
                    it.firstDayOfWeek,
                    visibleItemState,
                )
            },
            restore = {
                CalendarState(
                    startMonth = it[0] as YearMonth,
                    endMonth = it[1] as YearMonth,
                    firstVisibleMonth = it[2] as YearMonth,
                    firstDayOfWeek = it[3] as DayOfWeek,
                    visibleItemState = it[4] as VisibleItemState,
                )
            }
        )
    }
}

class CalendarLayoutInfo(info: LazyListLayoutInfo, private val startMonth: YearMonth) :
    LazyListLayoutInfo by info {
    val visibleMonthsInfo: List<CalendarItemInfo>
        get() = visibleItemsInfo.map {
            CalendarItemInfo(it, startMonth.plusMonths(it.index.toLong()))
        }
}

class CalendarItemInfo(info: LazyListItemInfo, val month: YearMonth) : LazyListItemInfo by info


@Composable
fun rememberCalendarState(
    startMonth: YearMonth = YearMonth.now(),
    endMonth: YearMonth = startMonth,
    firstVisibleMonth: YearMonth = startMonth,
    firstDayOfWeek: DayOfWeek = firstDayOfWeekFromLocale(),
): CalendarState {
    return rememberSaveable(saver = CalendarState.Saver) {
        CalendarState(
            startMonth = startMonth,
            endMonth = endMonth,
            firstDayOfWeek = firstDayOfWeek,
            firstVisibleMonth = firstVisibleMonth,
            visibleItemState = null
        )
    }
}
