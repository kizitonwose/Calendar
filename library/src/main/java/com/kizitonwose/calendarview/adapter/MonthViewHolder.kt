package com.kizitonwose.calendarview.adapter

import android.view.View
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.kizitonwose.calendarview.model.CalendarDay
import com.kizitonwose.calendarview.model.CalendarMonth
import com.kizitonwose.calendarview.model.OutDateStyle
import com.kizitonwose.calendarview.model.ScrollMode

data class CalendarConfig(
    val outDateStyle: OutDateStyle,
    val scrollMode: ScrollMode,
    @RecyclerView.Orientation val orientation: Int
)

class MonthViewHolder constructor(
    adapter: CalendarAdapter,
    rootLayout: LinearLayout,
    dayConfig: DayConfig,
    private var monthHeaderBinder: MonthHeaderFooterBinder?,
    private var monthFooterBinder: MonthHeaderFooterBinder?
) : RecyclerView.ViewHolder(rootLayout) {

    private val weekHolders = (1..6).map { WeekHolder(dayConfig) }

    var headerView: View? = rootLayout.findViewById(adapter.headerViewId)
    var footerView: View? = rootLayout.findViewById(adapter.footerViewId)
    var bodyLayout: LinearLayout = rootLayout.findViewById(adapter.bodyViewId)

    lateinit var month: CalendarMonth

    init {
        // Add week rows.
        weekHolders.forEach {
            bodyLayout.addView(it.inflateWeekView(bodyLayout))
        }
    }

    fun bindMonth(month: CalendarMonth) {
        this.month = month
        headerView?.let { header ->
            monthHeaderBinder?.invoke(header, month)
        }
        footerView?.let { footer ->
            monthFooterBinder?.invoke(footer, month)
        }
        weekHolders.forEachIndexed { index, week ->
            week.bindWeekView(month.weekDays[index])
        }
    }

    fun reloadDay(day: CalendarDay) {
        weekHolders.map { it.dayHolders }.flatten().firstOrNull { it.currentDay == day }?.reloadView()
    }

}
