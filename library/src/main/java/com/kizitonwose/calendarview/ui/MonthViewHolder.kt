package com.kizitonwose.calendarview.ui

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kizitonwose.calendarview.model.CalendarDay
import com.kizitonwose.calendarview.model.CalendarMonth
import com.kizitonwose.calendarview.model.InternalEvent

internal class MonthViewHolder constructor(
    adapter: CalendarAdapter,
    rootLayout: ViewGroup,
    private val weekHolders: List<WeekHolder>,
    private var monthHeaderBinder: MonthHeaderFooterBinder<ViewContainer>?,
    private var monthFooterBinder: MonthHeaderFooterBinder<ViewContainer>?
) : RecyclerView.ViewHolder(rootLayout) {

    val headerView: View? = rootLayout.findViewById(adapter.headerViewId)
    val footerView: View? = rootLayout.findViewById(adapter.footerViewId)

    private var headerContainer: ViewContainer? = null
    private var footerContainer: ViewContainer? = null

    fun bindMonth(month: CalendarMonth, events: List<InternalEvent>) {
        headerView?.let { view ->
            val headerContainer = headerContainer ?: monthHeaderBinder!!.create(view).also {
                headerContainer = it
            }
            monthHeaderBinder?.bind(headerContainer, month)
        }
        footerView?.let { view ->
            val footerContainer = footerContainer ?: monthFooterBinder!!.create(view).also {
                footerContainer = it
            }
            monthFooterBinder?.bind(footerContainer, month)
        }
        weekHolders.forEachIndexed { index, week ->
            val weekDays: List<CalendarDay> = month.weekDays.getOrNull(index).orEmpty()
            val weekEvents: List<InternalEvent> =
                if (weekDays.isNotEmpty()) {
                    events.filter { event ->
                        when (event) {
                            is InternalEvent.Single -> weekDays.any { it.date.isEqual(event.start) }
                            is InternalEvent.AllDay.Original -> weekDays.any { it.date.isEqual(event.start) }
                            is InternalEvent.AllDay.Partial -> weekDays.any { it.date.isEqual(event.start) }
                        }
                    }
                } else {
                    emptyList()
                }
            week.bindWeekView(weekDays, weekEvents, month)
        }
    }

    fun reloadDay(day: CalendarDay) {
        weekHolders.find { it.reloadDay(day) }
    }

    fun recycle() {
        weekHolders.forEach { it.recycle() }
        headerContainer?.let { monthHeaderBinder?.recycle(it) }
        footerContainer?.let { monthFooterBinder?.recycle(it) }
    }
}
