package com.kizitonwose.calendarview.adapter

import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import com.kizitonwose.calendarview.CalendarView
import com.kizitonwose.calendarview.model.CalendarDay
import com.kizitonwose.calendarview.model.CalendarMonth
import com.kizitonwose.calendarview.utils.inflate
import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalDate


typealias DateClickListener = (CalendarDay) -> Unit

typealias DateViewBinder = (view: View, currentDay: CalendarDay) -> Unit

typealias MonthHeaderFooterBinder = (view: View, calendarMonth: CalendarMonth) -> Unit

open class CalendarAdapter(
    @LayoutRes private val dayViewRes: Int,
    @LayoutRes private val monthHeaderRes: Int,
    @LayoutRes private val monthFooterRes: Int
) : RecyclerView.Adapter<MonthViewHolder>() {

    private lateinit var rv: CalendarView

    private val months = mutableListOf<CalendarMonth>()

    private val config = CalendarConfig(DayOfWeek.MONDAY)

    init {
        months.add(CalendarMonth.now())
        months.add(months.first().next)
        months.add(months.last().next)
        months.add(months.last().next)

    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        rv = recyclerView as CalendarView
    }

    private fun getItem(position: Int): CalendarMonth = months[position]

    override fun getItemCount(): Int = months.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MonthViewHolder {
        val context = parent.context
        val rootLayout = LinearLayout(context).apply {
            layoutParams = ViewGroup.LayoutParams(rv.monthWidth, rv.monthHeight)
            orientation = LinearLayout.VERTICAL
            setPaddingRelative(
                rv.monthPaddingStart, rv.monthPaddingTop,
                rv.monthPaddingEnd, rv.monthPaddingBottom
            )
        }
        val monthBodyLayout = LinearLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            orientation = LinearLayout.VERTICAL
        }

        var monthHeaderView: View? = null
        if (monthHeaderRes != 0) {
            monthHeaderView = rootLayout.inflate(monthHeaderRes)
            rootLayout.addView(monthHeaderView)
        }

        rootLayout.addView(monthBodyLayout)

        var monthFooterView: View? = null
        if (monthFooterRes != 0) {
            monthFooterView = rootLayout.inflate(monthFooterRes)
            rootLayout.addView(monthFooterView)
        }

        return MonthViewHolder(rootLayout, MonthViews(monthHeaderView, monthBodyLayout, monthFooterView), dayViewRes, {
            rv.onDateClick?.invoke(it)
        }, { view, day ->
            rv.dateViewBinder?.invoke(view, day)
        }, rv.monthHeaderBinder, rv.monthFooterBinder, config)
    }

    override fun onBindViewHolder(holder: MonthViewHolder, position: Int) {
        holder.bindMonth(getItem(position))
    }

    private fun refreshMonth(date: LocalDate) {
        notifyItemChanged(months.indexOfFirst { it.ownedDates.contains(date) })
    }

    fun scrollToMonth(date: LocalDate) {
        rv.scrollToPosition(getAdapterPosition(date))
    }

    fun scrollToDate(date: LocalDate) {

    }

    private fun getAdapterPosition(date: LocalDate): Int {
        return months.indexOfFirst { it.ownedDates.contains(date) }
    }

    fun reloadDate(date: LocalDate) {
        val adapterPos = months.indexOfFirst { it.ownedDates.contains(date) }
        val viewHolder = rv.findViewHolderForAdapterPosition(adapterPos) as? MonthViewHolder
        viewHolder?.reloadDate(getItem(adapterPos).ownedDays[date.dayOfMonth.dec()])
    }
}
