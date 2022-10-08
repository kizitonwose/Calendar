package com.kizitonwose.calendarview.internal.monthcalendar

import androidx.recyclerview.widget.RecyclerView
import com.kizitonwose.calendarcore.CalendarDay
import com.kizitonwose.calendarview.CalendarView
import com.kizitonwose.calendarview.MarginValues
import com.kizitonwose.calendarview.internal.CalendarLayoutManager
import com.kizitonwose.calendarview.internal.dayTag
import java.time.YearMonth

internal class MonthCalendarLayoutManager(
    private val calView: CalendarView,
    @RecyclerView.Orientation orientation: Int,
) : CalendarLayoutManager<YearMonth, CalendarDay>(calView, orientation) {

    private val adapter: MonthCalendarAdapter
        get() = calView.adapter as MonthCalendarAdapter

    override fun getaItemAdapterPosition(data: YearMonth): Int = adapter.getAdapterPosition(data)
    override fun getaDayAdapterPosition(data: CalendarDay): Int = adapter.getAdapterPosition(data)
    override fun getDayTag(data: CalendarDay): Int = dayTag(data.date)
    override fun getItemMargins(): MarginValues = calView.monthMargins
    override fun scrollPaged(): Boolean = calView.scrollPaged
    override fun notifyScrollListenerIfNeeded() = adapter.notifyMonthScrollListenerIfNeeded()
}
