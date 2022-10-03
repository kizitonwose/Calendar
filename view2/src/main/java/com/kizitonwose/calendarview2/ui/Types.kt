package com.kizitonwose.calendarview2.ui

import android.view.View
import com.kizitonwose.calendarview2.model.CalendarDay
import com.kizitonwose.calendarview2.model.CalendarMonth

open class ViewContainer(val view: View)

interface DayBinder<T : ViewContainer> {
    fun create(view: View): T
    fun bind(container: T, day: CalendarDay)
}

interface MonthHeaderFooterBinder<T : ViewContainer> {
    fun create(view: View): T
    fun bind(container: T, month: CalendarMonth)
}

typealias MonthScrollListener = (CalendarMonth) -> Unit
