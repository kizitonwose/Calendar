package com.kizitonwose.calendarview.adapter

import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kizitonwose.calendarview.CalendarView
import com.kizitonwose.calendarview.model.CalendarDay
import com.kizitonwose.calendarview.model.CalendarMonth
import com.kizitonwose.calendarview.utils.inflate
import org.threeten.bp.DayOfWeek
import org.threeten.bp.YearMonth


typealias DateClickListener = (CalendarDay) -> Unit

typealias DateViewBinder = (view: View, currentDay: CalendarDay) -> Unit

typealias MonthHeaderFooterBinder = (view: View, calendarMonth: CalendarMonth) -> Unit

typealias MonthScrollListener = (calendarMonth: CalendarMonth) -> Unit

open class CalendarAdapter(
    @LayoutRes private val dayViewRes: Int,
    @LayoutRes private val monthHeaderRes: Int,
    @LayoutRes private val monthFooterRes: Int,
    private val config: CalendarConfig
) : RecyclerView.Adapter<MonthViewHolder>() {

    private lateinit var rv: CalendarView

    private lateinit var firstDayOfWeek: DayOfWeek

    private val months = mutableListOf<CalendarMonth>()

    val bodyViewId = View.generateViewId()
    val rootViewId = View.generateViewId()

    // Values of headerViewId & footerViewId will be
    // replaced with IDs set in the XML if present.
    var headerViewId = View.generateViewId()
    var footerViewId = View.generateViewId()

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        rv = recyclerView as CalendarView
        rv.post { findVisibleMonthAndNotify() }
    }

    private fun getItem(position: Int): CalendarMonth = months[position]

    override fun getItemCount(): Int = months.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MonthViewHolder {
        val context = parent.context
        val rootLayout = LinearLayout(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            orientation = LinearLayout.VERTICAL
            setPaddingRelative(
                rv.monthPaddingStart, rv.monthPaddingTop,
                rv.monthPaddingEnd, rv.monthPaddingBottom
            )
            id = rootViewId
        }

        if (monthHeaderRes != 0) {
            val monthHeaderView = rootLayout.inflate(monthHeaderRes)
            // Don't overwrite ID set by the user.
            if (monthHeaderView.id == View.NO_ID) {
                monthHeaderView.id = headerViewId
            } else {
                headerViewId = monthHeaderView.id
            }
            rootLayout.addView(monthHeaderView)
        }

        val monthBodyLayout = LinearLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            orientation = LinearLayout.VERTICAL
            id = bodyViewId
        }
        rootLayout.addView(monthBodyLayout)

        if (monthFooterRes != 0) {
            val monthFooterView = rootLayout.inflate(monthFooterRes)
            // Don't overwrite ID set by the user.
            if (monthFooterView.id == View.NO_ID) {
                monthFooterView.id = footerViewId
            } else {
                footerViewId = monthFooterView.id
            }
            rootLayout.addView(monthFooterView)
        }

        return MonthViewHolder(
            this, rootLayout, dayViewRes, rv.daySize,
            { rv.dateClickListener?.invoke(it) },
            { view, day -> rv.dateViewBinder?.invoke(view, day) },
            rv.monthHeaderBinder, rv.monthFooterBinder, config
        )
    }

    override fun onBindViewHolder(holder: MonthViewHolder, position: Int) {
        holder.bindMonth(getItem(position))
    }

    fun reloadDay(day: CalendarDay) {
        val adapterPos = months.indexOfFirst { it.weekDays.flatten().contains(day) }
        if (adapterPos != -1) {
            // Notify the adapter to reload the month if we cannot find the ViewHolder.
            // `findViewHolderForAdapterPosition` can return null if the month is not
            // currently visible on the screen.
            val viewHolder = rv.findViewHolderForAdapterPosition(adapterPos)
            if (viewHolder != null) {
                (viewHolder as MonthViewHolder).reloadDay(day)
            } else {
                notifyItemChanged(adapterPos)
            }
        }
    }

    fun reloadMonth(month: YearMonth) {
        notifyItemChanged(getAdapterPosition(month))
    }

    fun setupDates(startMonth: YearMonth, endMonth: YearMonth, firstDayOfWeek: DayOfWeek) {
        this.firstDayOfWeek = firstDayOfWeek
        val startCalMonth = CalendarMonth(startMonth, config, firstDayOfWeek)
        val endCalMonth = CalendarMonth(endMonth, config, firstDayOfWeek)
        var lastCalMonth = startCalMonth
        months.clear()
        while (lastCalMonth < endCalMonth) {
            months.add(lastCalMonth)
            lastCalMonth = lastCalMonth.next
        }
        months.add(endCalMonth)
        notifyDataSetChanged()
    }

    private var visibleMonth: CalendarMonth? = null
    fun findVisibleMonthAndNotify() {
        val visibleItemPos = (rv.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
        if (visibleItemPos != RecyclerView.NO_POSITION) {
            val visibleMonth = months[visibleItemPos]
            if (visibleMonth != this.visibleMonth) {
                rv.monthScrollListener?.invoke(visibleMonth)
                this.visibleMonth = visibleMonth
            }
        }
    }

    internal fun getAdapterPosition(month: YearMonth): Int {
        return months.indexOfFirst { it.yearMonth == month }
    }

    fun getFirstVisibleMonth(): CalendarMonth? {
        val visibleItemPos = (rv.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
        if (visibleItemPos != RecyclerView.NO_POSITION) {
            return months[visibleItemPos]
        }
        return null
    }

    fun getMonthAtPosition(position: Int): CalendarMonth {
        return months[position]
    }
}
