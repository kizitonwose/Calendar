package com.kizitonwose.calendarview.adapter

import android.view.View
import android.widget.LinearLayout
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import com.kizitonwose.calendarview.model.CalendarDay
import com.kizitonwose.calendarview.model.CalendarMonth
import com.kizitonwose.calendarview.model.OutDateStyle
import com.kizitonwose.calendarview.model.ScrollMode

data class MonthViews(val header: View?, val body: LinearLayout, val footer: View?)

data class CalendarConfig(
    val outDateStyle: OutDateStyle,
    val scrollMode: ScrollMode,
    @RecyclerView.Orientation val orientation: Int
) {
    // Note: We don't set IDs for the header and footer views
    // because it would overwrite the ID set by the user for
    // the root view in the provided resource.
    val bodyViewId = View.generateViewId()
    val rootViewId = View.generateViewId()
}

class MonthViewHolder constructor(
    rootContainer: LinearLayout,
    private var monthViews: MonthViews,
    @LayoutRes dayViewRes: Int,
    dateClickListener: DateClickListener,
    dateViewBinder: DateViewBinder,
    private var monthHeaderBinder: MonthHeaderFooterBinder?,
    private var monthFooterBinder: MonthHeaderFooterBinder?,
    private var calendarConfig: CalendarConfig
) : RecyclerView.ViewHolder(rootContainer) {

    private val weekHolders = (1..6).map { WeekHolder(dayViewRes, dateClickListener, dateViewBinder, calendarConfig) }

    init {
        weekHolders.forEach {
            val monthBodyLayout = monthViews.body
            monthBodyLayout.addView(it.inflateWeekView(monthBodyLayout))
        }
    }

    fun bindMonth(month: CalendarMonth) {
        monthViews.header?.let { header ->
            monthHeaderBinder?.invoke(header, month)
        }
        monthViews.footer?.let { footer ->
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
