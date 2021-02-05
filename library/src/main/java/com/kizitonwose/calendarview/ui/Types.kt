package com.kizitonwose.calendarview.ui

import android.view.View
import com.kizitonwose.calendarview.model.CalendarDay
import com.kizitonwose.calendarview.model.CalendarMonth
import com.kizitonwose.calendarview.model.Event

open class ViewContainer(val view: View)

interface DayBinder<T : ViewContainer> {
    fun create(view: View): T
    fun bind(container: T, day: CalendarDay)
    fun recycle(container: T) {}
}

interface MonthHeaderFooterBinder<T : ViewContainer> {
    fun create(view: View): T
    fun bind(container: T, month: CalendarMonth)
    fun recycle(container: T) {}
}

interface EventCellBinder<T : ViewContainer> {
    fun create(view: View): T
    fun bind(container: T, event: Event, leftBoundaryStart: Boolean, rightBoundaryEnd: Boolean)
    fun recycle(container: T) {}
}

typealias MonthScrollListener = (CalendarMonth) -> Unit
