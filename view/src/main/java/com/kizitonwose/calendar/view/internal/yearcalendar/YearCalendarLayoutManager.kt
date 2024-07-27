package com.kizitonwose.calendar.view.internal.yearcalendar

import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.view.MarginValues
import com.kizitonwose.calendar.view.YearCalendarView
import com.kizitonwose.calendar.view.internal.CalendarLayoutManager
import com.kizitonwose.calendar.view.internal.dayTag
import java.time.Year

internal class YearCalendarLayoutManager(private val calView: YearCalendarView) :
    CalendarLayoutManager<Year, CalendarDay>(calView, calView.orientation) {
    private val adapter: YearCalendarAdapter
        get() = calView.adapter as YearCalendarAdapter

    override fun getaItemAdapterPosition(data: Year): Int = adapter.getAdapterPosition(data)
    override fun getaDayAdapterPosition(data: CalendarDay): Int = adapter.getAdapterPosition(data)
    override fun getDayTag(data: CalendarDay): Int = dayTag(data.date)
    override fun getItemMargins(): MarginValues = calView.yearMargins
    override fun scrollPaged(): Boolean = calView.scrollPaged
    override fun notifyScrollListenerIfNeeded() = adapter.notifyYearScrollListenerIfNeeded()
}
