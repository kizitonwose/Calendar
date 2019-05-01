package com.kizitonwose.calendarview.adapter

import android.view.View
import android.widget.LinearLayout
import androidx.annotation.LayoutRes
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
    rootContainer: LinearLayout,
    @LayoutRes dayViewRes: Int,
    daySize: DaySize,
    dateClickListener: DateClickListener,
    dateViewBinder: DateViewBinder,
    private var monthHeaderBinder: MonthHeaderFooterBinder?,
    private var monthFooterBinder: MonthHeaderFooterBinder?,
    private var calendarConfig: CalendarConfig
) : RecyclerView.ViewHolder(rootContainer) {

    private val weekHolders =
        (1..6).map { WeekHolder(dayViewRes, daySize, dateClickListener, dateViewBinder, calendarConfig) }

    private var headerView: View? = rootContainer.findViewById(adapter.headerViewId)
    private var footerView: View? = rootContainer.findViewById(adapter.footerViewId)
    private var bodyLayout: LinearLayout = rootContainer.findViewById(adapter.bodyViewId)

    init {
        // Add week rows.
        weekHolders.forEach {
            bodyLayout.addView(it.inflateWeekView(bodyLayout))
        }
    }

    fun bindMonth(month: CalendarMonth) {
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
