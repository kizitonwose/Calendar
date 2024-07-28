package com.kizitonwose.calendar.view.internal.yearcalendar

import android.annotation.SuppressLint
import android.graphics.Rect
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.recyclerview.widget.RecyclerView
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.CalendarMonth
import com.kizitonwose.calendar.core.CalendarYear
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.OutDateStyle
import com.kizitonwose.calendar.core.nextMonth
import com.kizitonwose.calendar.core.previousMonth
import com.kizitonwose.calendar.core.yearMonth
import com.kizitonwose.calendar.data.DataStore
import com.kizitonwose.calendar.data.getCalendarYearData
import com.kizitonwose.calendar.data.getYearIndex
import com.kizitonwose.calendar.data.getYearIndicesCount
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.MonthHeaderFooterBinder
import com.kizitonwose.calendar.view.ViewContainer
import com.kizitonwose.calendar.view.YearCalendarView
import com.kizitonwose.calendar.view.YearHeaderFooterBinder
import com.kizitonwose.calendar.view.internal.NO_INDEX
import java.time.DayOfWeek
import java.time.Month
import java.time.Year
import java.time.YearMonth

internal class YearCalendarAdapter(
    private val calView: YearCalendarView,
    private var outDateStyle: OutDateStyle,
    private var startYear: Year,
    private var endYear: Year,
    private var firstDayOfWeek: DayOfWeek,
) : RecyclerView.Adapter<YearViewHolder>() {
    private var itemCount = getYearIndicesCount(startYear, endYear)
    private val dataStore = DataStore { offset ->
        getCalendarYearData(startYear, offset, firstDayOfWeek, outDateStyle)
    }

    init {
        setHasStableIds(true)
    }

    private val isAttached: Boolean
        get() = calView.adapter === this

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        calView.post { notifyYearScrollListenerIfNeeded() }
    }

    private fun getItem(position: Int): CalendarYear = dataStore[position]

    override fun getItemId(position: Int): Long = getItem(position).year.value.toLong()

    override fun getItemCount(): Int = itemCount

    @Suppress("UNCHECKED_CAST")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): YearViewHolder {
        val content = setupYearItemRoot(
            daySize = calView.daySize,
            context = calView.context,
            dayViewResource = calView.dayViewResource,
            dayBinder = calView.dayBinder as MonthDayBinder<ViewContainer>,
            monthColumns = calView.monthColumns,
            monthHorizontalSpacing = calView.monthHorizontalSpacing,
            monthVerticalSpacing = calView.monthVerticalSpacing,
            yearItemMargins = calView.yearMargins,
            monthHeaderResource = calView.monthHeaderResource,
            monthFooterResource = calView.monthFooterResource,
            monthViewClass = calView.monthViewClass,
            monthHeaderBinder = calView.monthHeaderBinder as MonthHeaderFooterBinder<ViewContainer>?,
            monthFooterBinder = calView.monthFooterBinder as MonthHeaderFooterBinder<ViewContainer>?,
            yearItemViewClass = calView.yearViewClass,
            yearItemHeaderResource = calView.yearHeaderResource,
            yearItemFooterResource = calView.yearFooterResource,
        )

        @Suppress("UNCHECKED_CAST")
        return YearViewHolder(
            rootLayout = content.itemView,
            headerView = content.headerView,
            footerView = content.footerView,
            monthRowHolders = content.monthRowHolders,
            isMonthVisible = calView.isMonthVisible,
            yearHeaderBinder = calView.yearHeaderBinder as YearHeaderFooterBinder<ViewContainer>?,
            yearFooterBinder = calView.yearFooterBinder as YearHeaderFooterBinder<ViewContainer>?,
        )
    }

    override fun onBindViewHolder(holder: YearViewHolder, position: Int, payloads: List<Any>) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads)
        } else {
            payloads.forEach {
                when (it) {
                    is CalendarDay -> holder.reloadDay(it)
                    is YearMonth -> holder.reloadMonth(it)
                    else -> {}
                }
            }
        }
    }

    override fun onBindViewHolder(holder: YearViewHolder, position: Int) {
        holder.bindYear(getItem(position))
    }

    fun reloadDay(vararg day: CalendarDay) {
        day.forEach { day ->
            val position = getAdapterPosition(day)
            if (position != NO_INDEX) {
                notifyItemChanged(position, day)
            }
        }
    }

    fun reloadMonth(month: YearMonth) {
        val position = getAdapterPosition(Year.of(month.year))
        if (position != NO_INDEX) {
            notifyItemChanged(position, month)
        }
    }

    fun reloadYear(month: Year) {
        notifyItemChanged(getAdapterPosition(month))
    }

    fun reloadCalendar() {
        notifyItemRangeChanged(0, itemCount)
    }

    private var visibleYear: CalendarYear? = null
    fun notifyYearScrollListenerIfNeeded() {
        // Guard for cv.post() calls and other callbacks which use this method.
        if (!isAttached) return

        if (calView.isAnimating) {
            // Fixes an issue where findFirstVisibleMonthPosition() returns
            // zero if called when the RecyclerView is animating. This can be
            // replicated in Example 1 when switching from week to month mode.
            // The property changes when switching modes in Example 1 cause
            // notifyDataSetChanged() to be called, hence the animation.
            calView.itemAnimator?.isRunning {
                notifyYearScrollListenerIfNeeded()
            }
            return
        }
        val visibleItemPos = findFirstVisibleYearPosition()
        if (visibleItemPos != RecyclerView.NO_POSITION) {
            val visibleYear = dataStore[visibleItemPos]

            if (visibleYear != this.visibleYear) {
                this.visibleYear = visibleYear
                calView.yearScrollListener?.invoke(visibleYear)

                // TODO - YEAR
                // Fixes issue where the calendar does not resize its height when in horizontal, paged mode and
                // the `outDateStyle` is not `endOfGrid` hence the last row of a 5-row visible month is empty.
                // We set such week row's container visibility to GONE in the WeekHolder but it seems the
                // RecyclerView accounts for the items in the immediate previous and next indices when
                // calculating height and uses the tallest one of the three meaning that the current index's
                // view will end up having a blank space at the bottom unless the immediate previous and next
                // indices are also missing the last row. I think there should be a better way to fix this.
                // New: Also fixes issue where the calendar does not wrap each month's height when in vertical,
                // paged mode and just matches parent's height instead.
                // Only happens when the CalendarView wraps its height.
                if (calView.scrollPaged && calView.layoutParams.height == WRAP_CONTENT) {
                    val visibleVH =
                        calView.findViewHolderForAdapterPosition(visibleItemPos) ?: return
                    // Fixes #199, #266
                    visibleVH.itemView.requestLayout()
                }
            }
        }
    }

    internal fun getAdapterPosition(year: Year): Int {
        return getYearIndex(startYear, year)
    }

    internal fun getAdapterPosition(day: CalendarDay): Int {
        return getAdapterPosition(day.positionYear)
    }

    private val layoutManager: YearCalendarLayoutManager
        get() = calView.layoutManager as YearCalendarLayoutManager

    fun findFirstVisibleYear(): CalendarYear? {
        val index = findFirstVisibleYearPosition()
        return if (index == NO_INDEX) null else dataStore[index]
    }

    fun findLastVisibleYear(): CalendarYear? {
        val index = findLastVisibleYearPosition()
        return if (index == NO_INDEX) null else dataStore[index]
    }

    fun findFirstVisibleMonth(): CalendarMonth? = findVisibleMonth(isFirst = true)

    fun findLastVisibleMonth(): CalendarMonth? = findVisibleMonth(isFirst = false)

    fun findFirstVisibleDay(): CalendarDay? = findVisibleDay(isFirst = true)

    fun findLastVisibleDay(): CalendarDay? = findVisibleDay(isFirst = false)

    private fun findFirstVisibleYearPosition(): Int = layoutManager.findFirstVisibleItemPosition()

    private fun findLastVisibleYearPosition(): Int = layoutManager.findLastVisibleItemPosition()

    private fun findVisibleDay(isFirst: Boolean): CalendarDay? {
        val visibleIndex = if (isFirst) {
            findFirstVisibleYearPosition()
        } else {
            findLastVisibleYearPosition()
        }
        if (visibleIndex == NO_INDEX) return null

        val visibleItemView = layoutManager.findViewByPosition(visibleIndex) ?: return null
        val monthRect = Rect()
        visibleItemView.getGlobalVisibleRect(monthRect)
        // TODO - YEAR

//        val dayRect = Rect()
//        return dataStore[visibleIndex].weekDays.flatten()
//            .run { if (isFirst) this else reversed() }
//            .firstOrNull {
//                val dayView = visibleItemView.findViewWithTag<View>(dayTag(it.date))
//                    ?: return@firstOrNull false
//                dayView.getGlobalVisibleRect(dayRect)
//                dayRect.intersect(monthRect)
//            }
        return null
    }

    private fun findVisibleMonth(isFirst: Boolean): CalendarMonth? {
        val visibleIndex = if (isFirst) {
            findFirstVisibleYearPosition()
        } else {
            findLastVisibleYearPosition()
        }
        if (visibleIndex == NO_INDEX) return null

        val visibleItemView = layoutManager.findViewByPosition(visibleIndex) ?: return null
        val monthRect = Rect()
        visibleItemView.getGlobalVisibleRect(monthRect)
        // TODO - YEAR

//        val dayRect = Rect()
//        return dataStore[visibleIndex].weekDays.flatten()
//            .run { if (isFirst) this else reversed() }
//            .firstOrNull {
//                val dayView = visibleItemView.findViewWithTag<View>(dayTag(it.date))
//                    ?: return@firstOrNull false
//                dayView.getGlobalVisibleRect(dayRect)
//                dayRect.intersect(monthRect)
//            }
        return null
    }

    @SuppressLint("NotifyDataSetChanged")
    internal fun updateData(
        startYear: Year,
        endYear: Year,
        outDateStyle: OutDateStyle,
        firstDayOfWeek: DayOfWeek,
    ) {
        this.startYear = startYear
        this.endYear = endYear
        this.outDateStyle = outDateStyle
        this.firstDayOfWeek = firstDayOfWeek
        this.itemCount = getYearIndicesCount(startYear, endYear)
        dataStore.clear()
        notifyDataSetChanged()
    }
}

// Find the actual year on the calendar where this date is shown.
internal val CalendarDay.positionYear: Year
    get() = when (position) {
        DayPosition.InDate -> if (date.month == Month.DECEMBER) {
            date.yearMonth.nextMonth.year
        } else {
            date.yearMonth.year
        }

        DayPosition.MonthDate -> date.year
        DayPosition.OutDate -> if (date.month == Month.JANUARY) {
            date.yearMonth.previousMonth.year
        } else {
            date.yearMonth.year
        }
    }.let(Year::of)

