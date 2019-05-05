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
import com.kizitonwose.calendarview.utils.*
import org.threeten.bp.DayOfWeek
import org.threeten.bp.YearMonth


open class ViewContainer(val view: View)

interface DayBinder<T : ViewContainer> {
    fun create(view: View): T
    fun bind(container: T, day: CalendarDay)
}

interface MonthHeaderFooterBinder<T : ViewContainer> {
    fun create(view: View): T
    fun bind(container: T, month: CalendarMonth)
}

typealias DateClickListener = (CalendarDay) -> Unit

typealias MonthScrollListener = (CalendarMonth) -> Unit

open class CalendarAdapter(
    @LayoutRes private val dayViewRes: Int,
    @LayoutRes private val monthHeaderRes: Int,
    @LayoutRes private val monthFooterRes: Int,
    private val config: CalendarConfig,
    private val calView: CalendarView,
    startMonth: YearMonth,
    endMonth: YearMonth,
    firstDayOfWeek: DayOfWeek
) : RecyclerView.Adapter<MonthViewHolder>() {

    private val months = mutableListOf<CalendarMonth>()

    val bodyViewId = View.generateViewId()
    val rootViewId = View.generateViewId()

    // Values of headerViewId & footerViewId will be
    // replaced with IDs set in the XML if present.
    var headerViewId = View.generateViewId()
    var footerViewId = View.generateViewId()

    init {
        val startCalMonth = CalendarMonth(startMonth, config, firstDayOfWeek)
        val endCalMonth = CalendarMonth(endMonth, config, firstDayOfWeek)
        var lastCalMonth = startCalMonth
        while (lastCalMonth < endCalMonth) {
            months.add(lastCalMonth)
            lastCalMonth = lastCalMonth.next
        }
        months.add(endCalMonth)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        calView.post { findVisibleMonthAndNotify() }
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
                calView.monthPaddingStart, calView.monthPaddingTop,
                calView.monthPaddingEnd, calView.monthPaddingBottom
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

        // We create an internal click listener instead of directly passing
        // the one in the CalendarView so we can always call the updated
        // instance in the CalenderView if it changes.
        @Suppress("UNCHECKED_CAST")
        val dayConfig = DayConfig(
            calView.dayWidth, calView.dayHeight, dayViewRes,
            { calView.dateClickListener?.invoke(it) },
            calView.dayBinder as DayBinder<ViewContainer>
        )
        @Suppress("UNCHECKED_CAST")
        return MonthViewHolder(
            this, rootLayout, dayConfig,
            calView.monthHeaderBinder as MonthHeaderFooterBinder<ViewContainer>?,
            calView.monthFooterBinder as MonthHeaderFooterBinder<ViewContainer>?
        )
    }

    override fun onBindViewHolder(holder: MonthViewHolder, position: Int, payloads: List<Any>) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads)
        } else {
            payloads.forEach {
                holder.reloadDay(it as CalendarDay)
            }
        }
    }

    override fun onBindViewHolder(holder: MonthViewHolder, position: Int) {
        holder.bindMonth(getItem(position))
    }

    fun reloadDay(day: CalendarDay) {
        val yearMonth = when (day.owner) {
            DayOwner.THIS_MONTH -> day.date.yearMonth
            DayOwner.PREVIOUS_MONTH -> day.date.yearMonth.next
            DayOwner.NEXT_MONTH -> day.date.yearMonth.previous
        }
        val position = getAdapterPosition(yearMonth)
        if (position != -1) {
            notifyItemChanged(position, day)
        }
    }

    fun reloadMonth(month: YearMonth) {
        notifyItemChanged(getAdapterPosition(month))
    }

    private var visibleMonth: CalendarMonth? = null
    private var calWrapsHeight: Boolean? = null
    fun findVisibleMonthAndNotify() {
        val visibleItemPos = (calView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
        if (visibleItemPos != RecyclerView.NO_POSITION) {
            val visibleMonth = months[visibleItemPos]
            if (visibleMonth != this.visibleMonth) {
                this.visibleMonth = visibleMonth
                calView.monthScrollListener?.invoke(visibleMonth)

                // Fixes issue where the calendar does not resize its height when in horizontal, paged mode and
                // the `outDateStyle` is not `endOfGrid` hence the last row of a 5-row visible month is empty.
                // We set such week row's container visibility to GONE in the WeekHolder but it seems the
                // RecyclerView accounts for the items in the immediate previous and next indices when
                // calculating height and uses the tallest one of the three meaning that the current index's
                // view will end up having a blank space at the bottom unless the immediate previous and next
                // indices are also missing the last row. There should be a better way to fix this I think.
                if (config.orientation == RecyclerView.HORIZONTAL && config.scrollMode == ScrollMode.PAGED) {
                    if (calWrapsHeight == null) {
                        // We modify the layoutParams so we save the initial value set by the user.
                        calWrapsHeight = calView.layoutParams.height == ViewGroup.LayoutParams.WRAP_CONTENT
                    }
                    if (calWrapsHeight!!.not()) return // Bug only happens when the CalenderView wraps its height.
                    val visibleVH = calView.findViewHolderForAdapterPosition(visibleItemPos) as MonthViewHolder
                    val newHeight = visibleVH.headerView?.height.orZero() +
                            // For some reason `visibleVH.bodyLayout.height` does not give us the updated height.
                            // So we calculate it again by checking the number of visible(non-empty) rows.
                            visibleMonth.weekDays.takeWhile { it.isNotEmpty() }.size * calView.dayHeight +
                            visibleVH.footerView?.height.orZero()
                    if (calView.layoutParams.height != newHeight)
                        calView.layoutParams = calView.layoutParams.apply {
                            this.height = newHeight
                        }
                }
            }
        }
    }

    internal fun getAdapterPosition(month: YearMonth): Int {
        return months.indexOfFirst { it.yearMonth == month }
    }

    fun getFirstVisibleMonth(): CalendarMonth? {
        val visibleItemPos = (calView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
        if (visibleItemPos != RecyclerView.NO_POSITION) {
            return months[visibleItemPos]
        }
        return null
    }

    fun getMonthAtPosition(position: Int): CalendarMonth {
        return months[position]
    }
}
