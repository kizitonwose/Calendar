package com.kizitonwose.calendar.view.internal.weekcalendar

import com.kizitonwose.calendar.view.MarginValues
import com.kizitonwose.calendar.view.WeekCalendarView
import com.kizitonwose.calendar.view.internal.CalendarLayoutManager
import com.kizitonwose.calendar.view.internal.dayTag
import java.time.LocalDate

internal class WeekCalendarLayoutManager(private val calView: WeekCalendarView) :
    CalendarLayoutManager<LocalDate, LocalDate>(calView, HORIZONTAL) {

    private val adapter: WeekCalendarAdapter
        get() = calView.adapter as WeekCalendarAdapter

    override fun getaItemAdapterPosition(data: LocalDate): Int = adapter.getAdapterPosition(data)
    override fun getaDayAdapterPosition(data: LocalDate): Int = adapter.getAdapterPosition(data)
    override fun getDayTag(data: LocalDate): Int = dayTag(data)
    override fun getItemMargins(): MarginValues = calView.weekMargins
    override fun scrollPaged(): Boolean = calView.scrollPaged
    override fun notifyScrollListenerIfNeeded() = adapter.notifyWeekScrollListenerIfNeeded()
}
