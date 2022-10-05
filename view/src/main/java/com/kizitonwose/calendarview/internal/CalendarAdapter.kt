package com.kizitonwose.calendarview.internal

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Rect
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.kizitonwose.calendarcore.*
import com.kizitonwose.calendarinternal.DataStore
import com.kizitonwose.calendarinternal.getCalendarMonthData
import com.kizitonwose.calendarinternal.getMonthIndex
import com.kizitonwose.calendarinternal.getMonthIndicesCount
import com.kizitonwose.calendarview.CalendarView
import com.kizitonwose.calendarview.DayBinder
import com.kizitonwose.calendarview.MonthHeaderFooterBinder
import com.kizitonwose.calendarview.ViewContainer
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

internal class CalendarAdapter(
    private val calView: CalendarView,
    private var outDateStyle: OutDateStyle,
    private var startMonth: YearMonth,
    private var endMonth: YearMonth,
    private var firstDayOfWeek: DayOfWeek,
) : RecyclerView.Adapter<MonthViewHolder>() {

    private var itemCount = getMonthIndicesCount(startMonth, endMonth)
    private val dataStore = DataStore { offset ->
        getCalendarMonthData(startMonth, offset, firstDayOfWeek, outDateStyle).calendarMonth
    }

    // Values of headerViewId & footerViewId will be
    // replaced with IDs set in the XML if present.
    private var headerViewId = ViewCompat.generateViewId()
    private var footerViewId = ViewCompat.generateViewId()

    init {
        setHasStableIds(true)
    }

    private val isAttached: Boolean
        get() = calView.adapter === this

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        calView.post { notifyMonthScrollListenerIfNeeded() }
    }

    private fun getItem(position: Int): CalendarMonth = dataStore[position]

    override fun getItemId(position: Int): Long = getItem(position).hashCode().toLong()

    override fun getItemCount(): Int = itemCount

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MonthViewHolder {
        val context = parent.context
        val rootLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
        }

        if (calView.monthHeaderResource != 0) {
            val monthHeaderView = rootLayout.inflate(calView.monthHeaderResource)
            // Don't overwrite ID set by the user.
            if (monthHeaderView.id == View.NO_ID) {
                monthHeaderView.id = headerViewId
            } else {
                headerViewId = monthHeaderView.id
            }
            rootLayout.addView(monthHeaderView)
        }

        @Suppress("UNCHECKED_CAST")
        val dayConfig = DayConfig(
            daySizeSquare = calView.daySizeSquare,
            calView.dayViewResource,
            calView.dayBinder as DayBinder<ViewContainer>
        )

        val weekHolders = (1..6)
            .map { WeekHolder(dayConfig.daySizeSquare, (1..7).map { DayHolder(dayConfig) }) }
            .onEach { weekHolder -> rootLayout.addView(weekHolder.inflateWeekView(rootLayout)) }

        if (calView.monthFooterResource != 0) {
            val monthFooterView = rootLayout.inflate(calView.monthFooterResource)
            // Don't overwrite ID set by the user.
            if (monthFooterView.id == View.NO_ID) {
                monthFooterView.id = footerViewId
            } else {
                footerViewId = monthFooterView.id
            }
            rootLayout.addView(monthFooterView)
        }

        fun setupRoot(root: ViewGroup) {
            ViewCompat.setPaddingRelative(
                root,
                calView.monthPaddingStart,
                calView.monthPaddingTop,
                calView.monthPaddingEnd,
                calView.monthPaddingBottom
            )
            val width = if (calView.daySizeSquare) MATCH_PARENT else WRAP_CONTENT
            root.layoutParams = ViewGroup.MarginLayoutParams(width, WRAP_CONTENT).apply {
                bottomMargin = calView.monthMarginBottom
                topMargin = calView.monthMarginTop

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    marginStart = calView.monthMarginStart
                    marginEnd = calView.monthMarginEnd
                } else {
                    leftMargin = calView.monthMarginStart
                    rightMargin = calView.monthMarginEnd
                }
            }
        }

        val userRoot = calView.monthViewClass?.let {
            val customLayout = Class.forName(it)
                .getDeclaredConstructor(Context::class.java)
                .newInstance(context) as ViewGroup

            customLayout.apply {
                setupRoot(this)
                addView(rootLayout)
            }
        } ?: rootLayout.apply { setupRoot(this) }

        @Suppress("UNCHECKED_CAST")
        return MonthViewHolder(
            rootLayout = userRoot,
            headerViewId = headerViewId,
            footerViewId = footerViewId,
            weekHolders = weekHolders,
            monthHeaderBinder = calView.monthHeaderBinder as MonthHeaderFooterBinder<ViewContainer>?,
            monthFooterBinder = calView.monthFooterBinder as MonthHeaderFooterBinder<ViewContainer>?
        )
    }

    override fun onBindViewHolder(holder: MonthViewHolder, position: Int, payloads: List<Any>) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads)
        } else {
            payloads.forEach {
                holder.reloadDay(it as CalendarDay)
            }
        }
    }

    override fun onBindViewHolder(holder: MonthViewHolder, position: Int) {
        holder.bindMonth(getItem(position))
    }

    fun reloadDay(day: CalendarDay) {
        val position = getAdapterPosition(day)
        if (position != NO_INDEX) {
            notifyItemChanged(position, day)
        }
    }

    fun reloadMonth(month: YearMonth) {
        notifyItemChanged(getAdapterPosition(month))
    }

    fun reloadCalendar() {
        notifyItemRangeChanged(0, itemCount)
    }

    private var visibleMonth: CalendarMonth? = null
    fun notifyMonthScrollListenerIfNeeded() {
        // Guard for cv.post() calls and other callbacks which use this method.
        if (!isAttached) return

        if (calView.isAnimating) {
            // Fixes an issue where findFirstVisibleMonthPosition() returns
            // zero if called when the RecyclerView is animating. This can be
            // replicated in Example 1 when switching from week to month mode.
            // The property changes when switching modes in Example 1 cause
            // notifyDataSetChanged() to be called, hence the animation.
            calView.itemAnimator?.isRunning {
                notifyMonthScrollListenerIfNeeded()
            }
            return
        }
        val visibleItemPos = findFirstVisibleMonthPosition()
        if (visibleItemPos != RecyclerView.NO_POSITION) {
            val visibleMonth = dataStore[visibleItemPos]

            if (visibleMonth != this.visibleMonth) {
                this.visibleMonth = visibleMonth
                calView.monthScrollListener?.invoke(visibleMonth)

                // Fixes issue where the calendar does not resize its height when in horizontal, paged mode and
                // the `outDateStyle` is not `endOfGrid` hence the last row of a 5-row visible month is empty.
                // We set such week row's container visibility to GONE in the WeekHolder but it seems the
                // RecyclerView accounts for the items in the immediate previous and next indices when
                // calculating height and uses the tallest one of the three meaning that the current index's
                // view will end up having a blank space at the bottom unless the immediate previous and next
                // indices are also missing the last row. I think there should be a better way to fix this.
                // New: Also fixes issue where the calendar does not wrap each month's height when in vertical,
                // paged mode and just matches parent's height instead.
                // Only happens when the CalenderView wraps its height.
                if (calView.scrollPaged && calView.layoutParams.height == WRAP_CONTENT) {
                    val visibleVH = calView.findViewHolderForAdapterPosition(visibleItemPos)
                            as? MonthViewHolder ?: return
                    // Fixes #199, #266
                    visibleVH.itemView.requestLayout()
                }
            }
        }
    }

    internal fun getAdapterPosition(month: YearMonth): Int {
        return getMonthIndex(startMonth, month)
    }

    internal fun getAdapterPosition(date: LocalDate): Int {
        return getAdapterPosition(CalendarDay(date, DayPosition.MonthDate))
    }

    internal fun getAdapterPosition(day: CalendarDay): Int {
        val index = getAdapterPosition(day.positionYearMonth)
        if (!indexInCount(index, itemCount)) return NO_INDEX
        return index
    }

    private val layoutManager: CalendarLayoutManager
        get() = calView.layoutManager as CalendarLayoutManager

    fun findFirstVisibleMonth(): CalendarMonth? =
        dataStore.getOrNull(findFirstVisibleMonthPosition())

    fun findLastVisibleMonth(): CalendarMonth? =
        dataStore.getOrNull(findLastVisibleMonthPosition())

    fun findFirstVisibleDay(): CalendarDay? = findVisibleDay(true)

    fun findLastVisibleDay(): CalendarDay? = findVisibleDay(false)

    private fun findFirstVisibleMonthPosition(): Int = layoutManager.findFirstVisibleItemPosition()

    private fun findLastVisibleMonthPosition(): Int = layoutManager.findLastVisibleItemPosition()

    private fun findVisibleDay(isFirst: Boolean): CalendarDay? {
        val visibleIndex =
            if (isFirst) findFirstVisibleMonthPosition() else findLastVisibleMonthPosition()
        if (visibleIndex == NO_INDEX) return null

        val visibleItemView = layoutManager.findViewByPosition(visibleIndex) ?: return null
        val monthRect = Rect()
        visibleItemView.getGlobalVisibleRect(monthRect)

        val dayRect = Rect()
        return dataStore[visibleIndex].weekDays.flatten()
            .run { if (isFirst) this else reversed() }
            .firstOrNull {
                val dayView = visibleItemView.findViewWithTag<View>(it.date.hashCode())
                    ?: return@firstOrNull false
                dayView.getGlobalVisibleRect(dayRect)
                dayRect.intersect(monthRect)
            }
    }

    @SuppressLint("NotifyDataSetChanged")
    internal fun updateData(
        startMonth: YearMonth,
        endMonth: YearMonth,
        outDateStyle: OutDateStyle,
        firstDayOfWeek: DayOfWeek,
    ) {
        this.startMonth = startMonth
        this.endMonth = endMonth
        this.outDateStyle = outDateStyle
        this.firstDayOfWeek = firstDayOfWeek
        this.itemCount = getMonthIndicesCount(startMonth, endMonth)
        dataStore.clear()
        notifyDataSetChanged()
    }
}

private fun indexInCount(index: Int, count: Int): Boolean {
    return index < count
}

// Find the actual month on the calendar that owns this date.
internal val CalendarDay.positionYearMonth: YearMonth
    get() = when (position) {
        DayPosition.InDate -> date.yearMonth.nextMonth
        DayPosition.MonthDate -> date.yearMonth
        DayPosition.OutDate -> date.yearMonth.previousMonth
    }
