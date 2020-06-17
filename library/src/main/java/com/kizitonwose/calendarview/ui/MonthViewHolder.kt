package com.kizitonwose.calendarview.ui

import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.kizitonwose.calendarview.model.CalendarDay
import com.kizitonwose.calendarview.model.CalendarMonth

internal class MonthViewHolder constructor(
    adapter: CalendarAdapter,
    rootLayout: ViewGroup,
    dayConfig: DayConfig,
    private var monthHeaderBinder: MonthHeaderFooterBinder<ViewContainer>?,
    private var monthFooterBinder: MonthHeaderFooterBinder<ViewContainer>?
) : RecyclerView.ViewHolder(rootLayout) {

    private val weekHolders = (1..6).map { WeekHolder(dayConfig) }

    val headerView: View? = rootLayout.findViewById(adapter.headerViewId)
    val footerView: View? = rootLayout.findViewById(adapter.footerViewId)
    val bodyLayout: LinearLayout = rootLayout.findViewById(adapter.bodyViewId)

    private var headerContainer: ViewContainer? = null
    private var footerContainer: ViewContainer? = null

    lateinit var month: CalendarMonth

    init {
        // Add week rows.
        weekHolders.forEach {
            bodyLayout.addView(it.inflateWeekView(bodyLayout))
        }
    }

    fun bindMonth(month: CalendarMonth) {
        this.month = month
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
            week.bindWeekView(month.weekDays.getOrNull(index).orEmpty())
        }
    }

    fun reloadDay(day: CalendarDay) {
        weekHolders.map { it.dayHolders }.flatten().firstOrNull { it.day == day }?.reloadView()
    }
}
