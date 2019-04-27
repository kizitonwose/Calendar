package com.kizitonwose.calendarview.adapter

import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
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

    // Note: We don't set IDs for the header and footer views
    // because it would overwrite the ID set by the user for
    // the root view in the provided resource.
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

    fun smoothScrollToMonth(month: YearMonth) {
        val position = getAdapterPosition(month)
        if (position != -1) {
            val smoothScroller = object : LinearSmoothScroller(rv.context) {
                override fun getVerticalSnapPreference(): Int {
                    return LinearSmoothScroller.SNAP_TO_START
                }

                override fun getHorizontalSnapPreference(): Int {
                    return LinearSmoothScroller.SNAP_TO_START
                }
            }
            smoothScroller.targetPosition = position
            rv.layoutManager?.startSmoothScroll(smoothScroller)
        }
    }

    fun smoothScrollToDate(date: LocalDate) {
        val position = getAdapterPosition(date.yearMonth)
        if (position != -1) {
            val smoothScroller = object : LinearSmoothScroller(rv.context) {
                override fun getVerticalSnapPreference(): Int {
                    return LinearSmoothScroller.SNAP_TO_START
                }
                override fun getHorizontalSnapPreference(): Int {
                    return LinearSmoothScroller.SNAP_TO_START
                }
                override fun calculateDyToMakeVisible(view: View, snapPreference: Int): Int {
                    val dy = super.calculateDyToMakeVisible(view, snapPreference)
                    val offset = getDateOffset(CalendarDay(date, DayOwner.THIS_MONTH), position, view)
                    return dy - offset
                }
                override fun calculateDxToMakeVisible(view: View, snapPreference: Int): Int {
                    val dx = super.calculateDxToMakeVisible(view, snapPreference)
                    val offset = getDateOffset(CalendarDay(date, DayOwner.THIS_MONTH), position, view)
                    return dx - offset
                }
            }
            smoothScroller.targetPosition = position
            rv.layoutManager?.startSmoothScroll(smoothScroller)
        }
    }

    fun scrollToDate(date: LocalDate) {
        scrollToMonth(date.yearMonth)
        if (config.scrollMode == ScrollMode.PAGED) return
        rv.post {
            val day = CalendarDay(date, DayOwner.THIS_MONTH)
            val layoutManager = rv.layoutManager as LinearLayoutManager
            val monthPosition = getAdapterPosition(date.yearMonth)
            if (monthPosition != -1) {
                // We already scrolled to this position so findViewHolder should not return null.
                val viewHolder = rv.findViewHolderForAdapterPosition(monthPosition) as MonthViewHolder
                val offset = getDateOffset(day, monthPosition, viewHolder.itemView)
                layoutManager.scrollToPositionWithOffset(monthPosition, -offset)
            }
        }
    }

    private fun getDateOffset(day: CalendarDay, targetPosition: Int, itemView: View): Int {
        var offset = 0
        val orientation = (rv.layoutManager as LinearLayoutManager).orientation
        if (orientation == RecyclerView.VERTICAL) {
            // Add header view height to offset if this is a vertical calendar with a header view.
            // See why we don't set IDs for header/footer views in the comment on the `bodyViewId`
            // field in this class.
            val rootView = itemView.findViewById<LinearLayout>(rootViewId)
            if (rootView.childCount >= 2 && rootView.getChildAt(1).id == bodyViewId) {
                offset += rootView.getChildAt(0).height
            }
        }
        val bodyLayout = itemView.findViewById<LinearLayout>(bodyViewId)
        val weekLayout = bodyLayout.getChildAt(0) as LinearLayout
        val dayLayout = weekLayout.getChildAt(0) as ViewGroup

        val weekDays: List<List<CalendarDay>> = months[targetPosition].weekDays
        // Get the row for this date in the month.
        val weekOfMonthRow = weekDays.indexOfFirst { it.contains(day) }
        // Get the column for this date in the month.
        val dayInWeekColumn = weekDays[weekOfMonthRow].indexOf(day)
        offset += if (orientation == RecyclerView.VERTICAL) {
            // Multiply the height by the number of weeks before the target week.
            dayLayout.height * weekOfMonthRow
        } else {
            // Multiply the width by the number of days before the target day.
            dayLayout.width * dayInWeekColumn
        }
        return offset
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

    private fun getAdapterPosition(month: YearMonth): Int {
        return months.indexOfFirst { it.yearMonth == month }
    }

    fun getFirstVisibleMonth(): CalendarMonth? {
        val visibleItemPos = (rv.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
        if (visibleItemPos != RecyclerView.NO_POSITION) {
            return months[visibleItemPos]
        }
        return null
    }

}
