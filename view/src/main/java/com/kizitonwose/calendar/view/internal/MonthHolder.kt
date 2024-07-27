package com.kizitonwose.calendar.view.internal

import android.view.View
import android.widget.LinearLayout
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.CalendarMonth
import com.kizitonwose.calendar.view.DaySize
import com.kizitonwose.calendar.view.MarginValues
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.MonthHeaderFooterBinder
import com.kizitonwose.calendar.view.ViewContainer
import java.time.YearMonth

internal class MonthHolder(
    private val daySize: DaySize,
    private val dayViewResource: Int,
    private var dayBinder: MonthDayBinder<ViewContainer>?,
    private val monthHeaderResource: Int,
    private val monthFooterResource: Int,
    private val monthViewClass: String?,
    private var monthHeaderBinder: MonthHeaderFooterBinder<ViewContainer>?,
    private var monthFooterBinder: MonthHeaderFooterBinder<ViewContainer>?,
) {
    private lateinit var monthContainer: ItemContent<CalendarDay>
    private var headerContainer: ViewContainer? = null
    private var footerContainer: ViewContainer? = null
    private lateinit var month: CalendarMonth

    fun inflateMonthView(parent: LinearLayout): View {
        return setupItemRoot(
            // TODO - YEAR
            itemMargins = MarginValues(),
            daySize = daySize,
            context = parent.context,
            dayViewResource = dayViewResource,
            itemHeaderResource = monthHeaderResource,
            itemFooterResource = monthFooterResource,
            weekSize = 6,
            // TODO - YEAR
            itemViewClass = null,
            dayBinder = dayBinder as MonthDayBinder,
        ).also { monthContainer = it }.itemView

//        return LinearLayout(parent.context).apply {
//            monthContainer = this
//            // TODO - YEAR
////            val width = if (daySize.parentDecidesWidth) ViewGroup.LayoutParams.MATCH_PARENT else ViewGroup.LayoutParams.WRAP_CONTENT
////            val height = if (daySize.parentDecidesHeight) ViewGroup.LayoutParams.MATCH_PARENT else ViewGroup.LayoutParams.WRAP_CONTENT
////            val weight = if (daySize.parentDecidesHeight) 1f else 0f
//            layoutParams = GridLayout.LayoutParams(
//                GridLayout.spec(
//                    /* start = */ GridLayout.UNDEFINED,
//                    /* size = */ 1,
//                    /* alignment = */ GridLayout.FILL,
//                ),
//                GridLayout.spec(
//                    /* start = */ GridLayout.UNDEFINED,
//                    /* size = */ 1,
//                    /* alignment = */
//                    if (daySize.parentDecidesHeight) {
//                        GridLayout.FILL
//                    } else {
//                        GridLayout.TOP
//                    },
//                ),
//            )
//            orientation = LinearLayout.VERTICAL
//            for (holder in weekHolders) {
//                addView(holder.inflateWeekView(this))
//            }
//        }
    }

    fun bindMonthView(month: CalendarMonth) {
        monthContainer.itemView.isGone = false
        // The last week row can be empty if out date style is not `EndOfGrid`
        this.month = month
        monthContainer.headerView?.let { view ->
            val headerContainer = headerContainer ?: monthHeaderBinder!!.create(view).also {
                headerContainer = it
            }
            monthHeaderBinder?.bind(headerContainer, month)
        }
        monthContainer.weekHolders.forEachIndexed { index, week ->
            week.bindWeekView(month.weekDays.getOrNull(index).orEmpty())
        }
        monthContainer.footerView?.let { view ->
            val footerContainer = footerContainer ?: monthFooterBinder!!.create(view).also {
                footerContainer = it
            }
            monthFooterBinder?.bind(footerContainer, month)
        }
    }

    fun hide() {
        monthContainer.itemView.isGone = true
    }

    fun isShown(): Boolean = monthContainer.itemView.isVisible

    fun reloadMonth(yearMonth: YearMonth): Boolean {
        return if (yearMonth == this.month.yearMonth) {
            bindMonthView(this.month)
            true
        } else {
            false
        }
    }

    fun reloadDay(day: CalendarDay): Boolean = monthContainer.weekHolders.any { it.reloadDay(day) }

}
