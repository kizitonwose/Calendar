package com.kizitonwose.calendar.view.internal

import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import com.kizitonwose.calendar.view.MarginValues

internal abstract class CalendarLayoutManager<IndexData, DayData>(
    private val calView: RecyclerView,
    @RecyclerView.Orientation orientation: Int,
) : LinearLayoutManager(calView.context, orientation, false) {

    abstract fun getaItemAdapterPosition(data: IndexData): Int
    abstract fun getaDayAdapterPosition(data: DayData): Int
    abstract fun getDayTag(data: DayData): Int
    abstract fun getItemMargins(): MarginValues
    abstract fun scrollPaged(): Boolean
    abstract fun notifyScrollListenerIfNeeded()

    fun scrollToIndex(indexData: IndexData) {
        val position = getaItemAdapterPosition(indexData)
        if (position == NO_INDEX) return
        scrollToPositionWithOffset(position, 0)
        calView.post { notifyScrollListenerIfNeeded() }
    }

    fun smoothScrollToIndex(indexData: IndexData) {
        val position = getaItemAdapterPosition(indexData)
        if (position == NO_INDEX) return
        startSmoothScroll(CalendarSmoothScroller(position, null))
    }

    fun smoothScrollToDay(day: DayData) {
        val indexPosition = getaDayAdapterPosition(day)
        if (indexPosition == NO_INDEX) return
        // Can't target a specific day in a paged calendar.
        startSmoothScroll(CalendarSmoothScroller(indexPosition, if (scrollPaged()) null else day))
    }

    fun scrollToDay(day: DayData) {
        val indexPosition = getaDayAdapterPosition(day)
        if (indexPosition == NO_INDEX) return
        scrollToPositionWithOffset(indexPosition, 0)
        // Can't target a specific day in a paged calendar.
        if (scrollPaged()) {
            calView.post { notifyScrollListenerIfNeeded() }
        } else {
            calView.post {
                val itemView = calView.findViewHolderForAdapterPosition(indexPosition)?.itemView
                    ?: return@post
                val offset = calculateDayViewOffsetInParent(day, itemView)
                scrollToPositionWithOffset(indexPosition, -offset)
                calView.post { notifyScrollListenerIfNeeded() }
            }
        }
    }

    private fun calculateDayViewOffsetInParent(day: DayData, itemView: View): Int {
        val dayView = itemView.findViewWithTag<View>(getDayTag(day)) ?: return 0
        val rect = Rect()
        dayView.getDrawingRect(rect)
        (itemView as ViewGroup).offsetDescendantRectToMyCoords(dayView, rect)
        val isVertical = orientation == VERTICAL
        val margins = getItemMargins()
        return if (isVertical) rect.top + margins.top else rect.left + margins.start
    }

    private inner class CalendarSmoothScroller(position: Int, val day: DayData?) :
        LinearSmoothScroller(calView.context) {

        init {
            targetPosition = position
        }

        override fun getVerticalSnapPreference(): Int = SNAP_TO_START

        override fun getHorizontalSnapPreference(): Int = SNAP_TO_START

        override fun calculateDyToMakeVisible(view: View, snapPreference: Int): Int {
            val dy = super.calculateDyToMakeVisible(view, snapPreference)
            if (day == null) {
                return dy
            }
            val offset = calculateDayViewOffsetInParent(day, view)
            return dy - offset
        }

        override fun calculateDxToMakeVisible(view: View, snapPreference: Int): Int {
            val dx = super.calculateDxToMakeVisible(view, snapPreference)
            if (day == null) {
                return dx
            }
            val offset = calculateDayViewOffsetInParent(day, view)
            return dx - offset
        }
    }
}
