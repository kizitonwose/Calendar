package com.kizitonwose.calendarview.adapter

import android.view.View
import android.widget.LinearLayout
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import com.kizitonwose.calendarview.model.CalendarDay
import com.kizitonwose.calendarview.model.CalendarMonth
import org.threeten.bp.DayOfWeek
import org.threeten.bp.temporal.WeekFields

data class MonthViews(val header: View?, val body: LinearLayout, val footer: View?)

data class CalendarConfig(val firstDayOfWeek: DayOfWeek) {
    val weekFields: WeekFields by lazy { WeekFields.of(firstDayOfWeek, 1) }
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

    fun reloadDate(day: CalendarDay) {
        val date = day.date
        val weekOfMonthField = calendarConfig.weekFields.weekOfMonth()
        val field = date.get(weekOfMonthField)
        weekHolders[field.dec()].reloadDate(day)
    }

}
