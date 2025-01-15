package com.kizitonwose.calendar.view.internal.yearcalendar

import android.annotation.SuppressLint
import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.recyclerview.widget.RecyclerView
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.CalendarMonth
import com.kizitonwose.calendar.core.CalendarYear
import com.kizitonwose.calendar.core.OutDateStyle
import com.kizitonwose.calendar.data.DataStore
import com.kizitonwose.calendar.data.getCalendarYearData
import com.kizitonwose.calendar.data.getYearIndex
import com.kizitonwose.calendar.data.getYearIndicesCount
import com.kizitonwose.calendar.data.positionYearMonth
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.MonthHeaderFooterBinder
import com.kizitonwose.calendar.view.ViewContainer
import com.kizitonwose.calendar.view.YearCalendarView
import com.kizitonwose.calendar.view.YearHeaderFooterBinder
import com.kizitonwose.calendar.view.internal.NO_INDEX
import com.kizitonwose.calendar.view.internal.dayTag
import com.kizitonwose.calendar.view.internal.intersects
import java.time.DayOfWeek
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
            context = calView.context,
            daySize = calView.daySize,
            monthHeight = calView.monthHeight,
            dayViewResource = calView.dayViewResource,
            dayBinder = calView.dayBinder as MonthDayBinder<ViewContainer>,
            monthColumns = calView.monthColumns,
            monthHorizontalSpacing = calView.monthHorizontalSpacing,
            monthVerticalSpacing = calView.monthVerticalSpacing,
            yearItemMargins = calView.yearMargins,
            yearBodyMargins = calView.yearBodyMargins,
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
        val position = getAdapterPosition(month)
        if (position != NO_INDEX) {
            notifyItemChanged(position, month)
        }
    }

    fun reloadYear(year: Year) {
        notifyItemChanged(getAdapterPosition(year))
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

                // See reason in MonthCalendarAdapter
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

    internal fun getAdapterPosition(month: YearMonth): Int {
        return getAdapterPosition(Year.of(month.year))
    }

    internal fun getAdapterPosition(day: CalendarDay): Int {
        return getAdapterPosition(day.positionYearMonth)
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

    /**
     * In a vertically scrolling calendar, year and month headers/footers can cause the
     * visible day rect to not be found in the returned visible year index from a call to
     * findFirstVisibleItemPosition/findLastVisibleItemPosition if only the header or
     * footer of the year or month in that index is visible. So we check adjacent indices too.
     */
    private fun findVisibleDay(isFirst: Boolean): CalendarDay? {
        return visibleMonthInfo(isFirst = isFirst)?.visibleDay(isFirst)
            ?: visibleMonthInfo(isFirst, yearIncrement = -1)?.visibleDay(isFirst)
            ?: visibleMonthInfo(isFirst, yearIncrement = 1)?.visibleDay(isFirst)
    }

    private fun Triple<CalendarMonth, View, Rect>.visibleDay(isFirst: Boolean): CalendarDay? {
        val (visibleMonth, visibleMonthView, visibleMonthRect) = this
        val dayRect = Rect()
        return visibleMonth.weekDays.flatten()
            .run { if (isFirst) this else reversed() }
            .firstOrNull {
                val dayView = visibleMonthView.findViewWithTag<View>(dayTag(it.date))
                    ?: return@firstOrNull false
                dayView.getGlobalVisibleRect(dayRect) &&
                    dayRect.intersects(visibleMonthRect)
            }
    }

    /**
     * In a vertically scrolling calendar, year headers/footers can cause the
     * visible month rect to not be found in the returned visible year index from a call to
     * findFirstVisibleItemPosition/findLastVisibleItemPosition if only the header or footer
     * of the year in that index is visible. So we check adjacent indices too.
     */
    private fun findVisibleMonth(isFirst: Boolean): CalendarMonth? {
        return visibleMonthInfo(isFirst = isFirst)?.first
            ?: visibleMonthInfo(isFirst, yearIncrement = -1)?.first
            ?: visibleMonthInfo(isFirst, yearIncrement = 1)?.first
    }

    private fun visibleMonthInfo(isFirst: Boolean, yearIncrement: Int = 0): Triple<CalendarMonth, View, Rect>? {
        var visibleIndex = if (isFirst) {
            findFirstVisibleYearPosition()
        } else {
            findLastVisibleYearPosition()
        }
        if (visibleIndex == NO_INDEX) return null
        visibleIndex += yearIncrement

        val visibleItemView = layoutManager.findViewByPosition(visibleIndex) ?: return null
        val yearRect = Rect()
        if (!visibleItemView.getGlobalVisibleRect(yearRect) || yearRect.isEmpty) return null

        val monthRect = Rect()
        return dataStore[visibleIndex].months
            .run { if (isFirst) this else reversed() }
            .firstNotNullOfOrNull {
                val monthView = visibleItemView.findViewWithTag<View>(monthTag(it.yearMonth))
                    ?: return@firstNotNullOfOrNull null
                if (
                    monthView.getGlobalVisibleRect(monthRect) &&
                    monthRect.intersects(yearRect)
                ) {
                    Triple(it, monthView, monthRect)
                } else {
                    null
                }
            }
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
