package com.kizitonwose.calendarcompose.weekcalendar

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
import com.kizitonwose.calendarcompose.VisibleItemState
import com.kizitonwose.calendarcompose.atStartOfMonth
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit
import java.time.temporal.WeekFields
import java.util.*

// TODO: Implement first visible date constructor property.
@Stable
class WeekCalendarState internal constructor(
    startDate: LocalDate,
    endDate: LocalDate,
    firstDayOfWeek: DayOfWeek,
    visibleItemState: VisibleItemState,
) : ScrollableState {
    internal val listState = LazyListState(
        firstVisibleItemIndex = visibleItemState.firstVisibleItemIndex,
        firstVisibleItemScrollOffset = visibleItemState.firstVisibleItemScrollOffset
    )

    var startDate by mutableStateOf(startDate)

    var endDate by mutableStateOf(endDate)

    var startDateAdjusted by mutableStateOf(startDate)
        internal set

    var endDateAdjusted by mutableStateOf(endDate)
        internal set

    var firstDayOfWeek by mutableStateOf(firstDayOfWeek)

    val firstVisibleDate: LocalDate
        get() = startDateAdjusted.plusWeeks(listState.firstVisibleItemIndex.toLong())

    val lastVisibleDate: LocalDate
        get() = startDateAdjusted.plusWeeks(
            listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index?.toLong() ?: 0
        )

    val layoutInfo: WeekCalendarLayoutInfo
        get() = WeekCalendarLayoutInfo(listState.layoutInfo, startDateAdjusted)

    suspend fun scrollToDate(date: LocalDate) {
        listState.scrollToItem(getScrollIndex(date) ?: return)
    }

    suspend fun animateScrollToDate(date: LocalDate) {
        listState.animateScrollToItem(getScrollIndex(date) ?: return)
    }

    override val isScrollInProgress: Boolean get() = listState.isScrollInProgress

    override fun dispatchRawDelta(delta: Float): Float = listState.dispatchRawDelta(delta)

    override suspend fun scroll(
        scrollPriority: MutatePriority,
        block: suspend ScrollScope.() -> Unit,
    ) = listState.scroll(scrollPriority, block)

    private fun getScrollIndex(date: LocalDate): Int? {
        if (date !in startDate..endDate) {
            Log.d("WeekCalendarState", "Attempting to scroll out of range; $date")
            return null
        }
        return ChronoUnit.WEEKS.between(startDateAdjusted, date).toInt()
    }

    companion object {
        val Saver: Saver<WeekCalendarState, *> = listSaver(
            save = {
                val visibleItemState = VisibleItemState(
                    firstVisibleItemIndex = it.listState.firstVisibleItemIndex,
                    firstVisibleItemScrollOffset = it.listState.firstVisibleItemScrollOffset
                )
                listOf(it.startDate, it.endDate, it.firstDayOfWeek, visibleItemState)
            },
            restore = {
                WeekCalendarState(
                    startDate = it[0] as LocalDate,
                    endDate = it[1] as LocalDate,
                    firstDayOfWeek = it[2] as DayOfWeek,
                    visibleItemState = it[3] as VisibleItemState,
                )
            }
        )
    }
}

class WeekCalendarLayoutInfo(info: LazyListLayoutInfo, private val startDate: LocalDate) :
    LazyListLayoutInfo by info {
    val visibleWeeksInfo: List<WeekCalendarItemInfo>
        get() = visibleItemsInfo.map { info ->
            val start = startDate.plusWeeks(info.index.toLong())
            WeekCalendarItemInfo(info, (0 until 7).map { start.plusDays(it.toLong()) })
        }
}

class WeekCalendarItemInfo(info: LazyListItemInfo, val dates: List<LocalDate>) :
    LazyListItemInfo by info

@Composable
fun rememberWeekCalendarState(
    startMonth: LocalDate = YearMonth.now().atStartOfMonth(),
    endMonth: LocalDate = YearMonth.now().atEndOfMonth(),
    firstDayOfWeek: DayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek,
): WeekCalendarState {
    return rememberSaveable(saver = WeekCalendarState.Saver) {
        WeekCalendarState(
            startDate = startMonth,
            endDate = endMonth,
            firstDayOfWeek = firstDayOfWeek,
            visibleItemState = VisibleItemState(),
        )
    }
}
