package com.kizitonwose.calendarview.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import com.kizitonwose.calendarview.CalendarView
import com.kizitonwose.calendarview.model.CalendarDay
import com.kizitonwose.calendarview.model.DayOwner
import com.kizitonwose.calendarview.model.ScrollMode
import com.kizitonwose.calendarview.utils.yearMonth
import org.threeten.bp.LocalDate
import org.threeten.bp.YearMonth

class CalendarLayoutManager(private val recyclerView: CalendarView, private val config: CalendarConfig) :
    LinearLayoutManager(recyclerView.context, config.orientation, false) {

    val adapter: CalendarAdapter
        get() = recyclerView.adapter as CalendarAdapter

    val context: Context
        get() = recyclerView.context

    fun scrollToMonth(month: YearMonth) {
        scrollToPosition(adapter.getAdapterPosition(month))
    }

    fun smoothScrollToMonth(month: YearMonth) {
        val position = adapter.getAdapterPosition(month)
        if (position != -1) {
            startSmoothScroll(CalendarSmoothScroller(position, null))
        }
    }

    fun smoothScrollToDate(date: LocalDate) {
        val position = adapter.getAdapterPosition(date.yearMonth)
        if (position != -1) {
            startSmoothScroll(CalendarSmoothScroller(position, date))
        }
    }

    fun scrollToDate(date: LocalDate) {
        scrollToMonth(date.yearMonth)
        if (config.scrollMode == ScrollMode.PAGED) return
        recyclerView.post {
            val day = CalendarDay(date, DayOwner.THIS_MONTH)
            val monthPosition = adapter.getAdapterPosition(date.yearMonth)
            if (monthPosition != -1) {
                // We already scrolled to this position so findViewHolder should not return null.
                val viewHolder = recyclerView.findViewHolderForAdapterPosition(monthPosition) as MonthViewHolder
                val offset = calculateOffset(day, monthPosition, viewHolder.itemView)
                scrollToPositionWithOffset(monthPosition, -offset)
            }
        }
    }

    private fun calculateOffset(day: CalendarDay, position: Int, itemView: View): Int {
        var offset = 0

        // Add header view height to offset if this is a vertical calendar with a header view.
        if (config.orientation == RecyclerView.VERTICAL) {
            itemView.findViewById<View?>(adapter.headerViewId)?.let {
                offset += it.height
            }
        }
        val bodyLayout = itemView.findViewById<LinearLayout>(adapter.bodyViewId)
        val weekLayout = bodyLayout.getChildAt(0) as LinearLayout
        val dayLayout = weekLayout.getChildAt(0) as ViewGroup

        val weekDays: List<List<CalendarDay>> = adapter.getMonthAtPosition(position).weekDays
        // Get the row for this date in the month.
        val weekOfMonthRow = weekDays.indexOfFirst { it.contains(day) }
        // Get the column for this date in the month.
        val dayInWeekColumn = weekDays[weekOfMonthRow].indexOf(day)
        offset += if (orientation == RecyclerView.VERTICAL) {
            // Multiply the height by the number of weeks before the target week.
            dayLayout.height * weekOfMonthRow
        } else {
            // Multiply the width by the number of days before the target day.
            dayLayout.width * dayInWeekColumn
        }
        return offset
    }

    private inner class CalendarSmoothScroller(val position: Int, val date: LocalDate?) :
        LinearSmoothScroller(context) {

        init {
            targetPosition = position
        }

        override fun getVerticalSnapPreference(): Int {
            return LinearSmoothScroller.SNAP_TO_START
        }

        override fun getHorizontalSnapPreference(): Int {
            return LinearSmoothScroller.SNAP_TO_START
        }

        override fun calculateDyToMakeVisible(view: View, snapPreference: Int): Int {
            val dy = super.calculateDyToMakeVisible(view, snapPreference)
            if (date == null) {
                return dy
            }
            val offset = calculateOffset(CalendarDay(date, DayOwner.THIS_MONTH), position, view)
            return dy - offset
        }

        override fun calculateDxToMakeVisible(view: View, snapPreference: Int): Int {
            val dx = super.calculateDxToMakeVisible(view, snapPreference)
            if (date == null) {
                return dx
            }
            val offset = calculateOffset(CalendarDay(date, DayOwner.THIS_MONTH), position, view)
            return dx - offset
        }
    }
}