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
import com.kizitonwose.calendarview.model.DayOwner
import com.kizitonwose.calendarview.model.ScrollMode
import com.kizitonwose.calendarview.utils.inflate
import com.kizitonwose.calendarview.utils.yearMonth
import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalDate
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

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        rv = recyclerView as CalendarView
        rv.post { findVisibleMonthAndNotify() }
    }

    private fun getItem(position: Int): CalendarMonth = months[position]

    override fun getItemCount(): Int = months.size

    private val bodyViewId = View.generateViewId()
    private val rootViewId = View.generateViewId()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MonthViewHolder {
        val context = parent.context
        val rootLayout = LinearLayout(context).apply {
            layoutParams = ViewGroup.LayoutParams(rv.monthWidth, rv.monthHeight)
            orientation = LinearLayout.VERTICAL
            setPaddingRelative(
                rv.monthPaddingStart, rv.monthPaddingTop,
                rv.monthPaddingEnd, rv.monthPaddingBottom
            )
            id = rootViewId
        }

        var monthHeaderView: View? = null
        if (monthHeaderRes != 0) {
            monthHeaderView = rootLayout.inflate(monthHeaderRes)
            rootLayout.addView(monthHeaderView)
        }

        val monthBodyLayout = LinearLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            orientation = LinearLayout.VERTICAL
            id = bodyViewId
        }
        rootLayout.addView(monthBodyLayout)

        var monthFooterView: View? = null
        if (monthFooterRes != 0) {
            monthFooterView = rootLayout.inflate(monthFooterRes)
            rootLayout.addView(monthFooterView)
        }

        return MonthViewHolder(rootLayout, MonthViews(monthHeaderView, monthBodyLayout, monthFooterView), dayViewRes, {
            rv.dateClickListener?.invoke(it)
        }, { view, day ->
            rv.dateViewBinder?.invoke(view, day)
        }, rv.monthHeaderBinder, rv.monthFooterBinder, config)
    }

    override fun onBindViewHolder(holder: MonthViewHolder, position: Int) {
        holder.bindMonth(getItem(position))
    }

    fun scrollToMonth(month: YearMonth) {
        rv.scrollToPosition(getAdapterPosition(month))
    }

    fun scrollToDate(date: LocalDate) {
        scrollToMonth(date.yearMonth)
        if (config.scrollMode == ScrollMode.PAGED) return
        rv.post {
            val day = CalendarDay(date, DayOwner.THIS_MONTH)
            val layoutManager = rv.layoutManager as LinearLayoutManager
            val monthPosition = getAdapterPosition(date.yearMonth)
            if (monthPosition != -1) {
                // Get the weekOfMonth for this date using its position in the weekDays array.
                val weekOfMonth = months[monthPosition].weekDays.indexOfFirst { it.contains(day) }
                // We already scrolled to this position to findViewHolder should not return null.
                val viewHolder = rv.findViewHolderForAdapterPosition(monthPosition) as MonthViewHolder
                var offset = 0
                if (layoutManager.orientation == RecyclerView.VERTICAL) {
                    // Add header view height to offset if this is a vertical calendar with a header view.
                    val rootView = viewHolder.itemView.findViewById<LinearLayout>(rootViewId)
                    if (rootView.childCount >= 2 && rootView.getChildAt(1).id == bodyViewId) {
                        offset += rootView.getChildAt(0).height
                    }
                }
                val bodyLayout = viewHolder.itemView.findViewById<LinearLayout>(bodyViewId)
                val weekLayout = bodyLayout.getChildAt(0) as LinearLayout
                // Multiply the height by the number of weeks before the target week.
                offset += weekLayout.height * weekOfMonth
                layoutManager.scrollToPositionWithOffset(monthPosition, -offset)
            }
        }
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
        val visibleItemPos = (rv.layoutManager as LinearLayoutManager).findFirstCompletelyVisibleItemPosition()
        if (visibleItemPos != RecyclerView.NO_POSITION) {
            val visibleMonth = months[visibleItemPos]
            if (visibleMonth != this.visibleMonth) {
                rv.monthScrollListener?.invoke(visibleMonth)
                this.visibleMonth = visibleMonth
            }
        }
    }

    private fun getAdapterPosition(month: YearMonth): Int {
        return months.indexOfFirst { it.yearMonth == month }
    }
}
