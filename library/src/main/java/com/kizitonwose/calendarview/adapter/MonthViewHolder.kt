package com.kizitonwose.calendarview.adapter

import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.kizitonwose.calendarview.model.CalendarDay
import com.kizitonwose.calendarview.model.CalendarMonth
import com.kizitonwose.calendarview.model.OutDateStyle
import com.kizitonwose.calendarview.model.ScrollMode

data class CalendarConfig(
    val outDateStyle: OutDateStyle,
    val scrollMode: ScrollMode,
    @RecyclerView.Orientation val orientation: Int,
    val monthViewClass: String?
)

class MonthViewHolder constructor(
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
        headerView?.let {
            if (headerContainer == null) {
                headerContainer = monthHeaderBinder?.create(it)
            }
            monthHeaderBinder?.bind(headerContainer!!, month)
        }
        footerView?.let {
            if (footerContainer == null) {
                footerContainer = monthFooterBinder?.create(it)
            }
            monthFooterBinder?.bind(footerContainer!!, month)
        }
        weekHolders.forEachIndexed { index, week ->
            week.bindWeekView(month.weekDays[index])
        }
    }

    fun reloadDay(day: CalendarDay) {
        weekHolders.map { it.dayHolders }.flatten().firstOrNull { it.currentDay == day }?.reloadView()
    }

}
