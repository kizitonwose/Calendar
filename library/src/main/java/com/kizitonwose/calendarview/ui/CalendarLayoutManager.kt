package com.kizitonwose.calendarview.ui

import android.content.Context
import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
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


class CalendarLayoutManager(private val calView: CalendarView, private val config: CalendarConfig) :
    LinearLayoutManager(calView.context, config.orientation, false) {

    val adapter: CalendarAdapter
        get() = calView.adapter as CalendarAdapter

    val context: Context
        get() = calView.context

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
        calView.post {
            val day = CalendarDay(date, DayOwner.THIS_MONTH)
            val monthPosition = adapter.getAdapterPosition(date.yearMonth)
            if (monthPosition != -1) {
                // We already scrolled to this position so findViewHolder should not return null.
                val viewHolder = calView.findViewHolderForAdapterPosition(monthPosition) as MonthViewHolder
                val offset = calculateOffset(day, viewHolder.itemView)
                scrollToPositionWithOffset(monthPosition, -offset)
            }
        }
    }

    private fun calculateOffset(day: CalendarDay, itemView: View): Int {
        val dayView = itemView.findViewById<View?>(day.date.hashCode()) ?: return 0
        val rect = Rect()
        dayView.getDrawingRect(rect)
        (itemView as ViewGroup).offsetDescendantRectToMyCoords(dayView, rect)
        return if (orientation == RecyclerView.VERTICAL) rect.top + calView.monthMarginTop else rect.left + calView.monthMarginStart
    }

    private inner class CalendarSmoothScroller(position: Int, val date: LocalDate?) :
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
            val offset = calculateOffset(CalendarDay(date, DayOwner.THIS_MONTH), view)
            return dy - offset
        }

        override fun calculateDxToMakeVisible(view: View, snapPreference: Int): Int {
            val dx = super.calculateDxToMakeVisible(view, snapPreference)
            if (date == null) {
                return dx
            }
            val offset = calculateOffset(CalendarDay(date, DayOwner.THIS_MONTH), view)
            return dx - offset
        }
    }
}