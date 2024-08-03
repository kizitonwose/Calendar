package com.kizitonwose.calendar.view.internal.yearcalendar

import android.view.View
import android.widget.LinearLayout
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.CalendarMonth
import com.kizitonwose.calendar.view.DaySize
import com.kizitonwose.calendar.view.MarginValues
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.MonthHeaderFooterBinder
import com.kizitonwose.calendar.view.ViewContainer
import com.kizitonwose.calendar.view.internal.ItemContent
import com.kizitonwose.calendar.view.internal.setupItemRoot
import java.time.YearMonth

internal class YearMonthHolder(
    private val daySize: DaySize,
    private val dayViewResource: Int,
    private var dayBinder: MonthDayBinder<ViewContainer>?,
    private val monthHeaderResource: Int,
    private val monthFooterResource: Int,
    private val monthViewClass: String?,
    private val monthHeaderBinder: MonthHeaderFooterBinder<ViewContainer>?,
    private val monthFooterBinder: MonthHeaderFooterBinder<ViewContainer>?,
) {
    private lateinit var monthContainer: ItemContent<CalendarDay>
    private var headerContainer: ViewContainer? = null
    private var footerContainer: ViewContainer? = null
    private lateinit var month: CalendarMonth

    fun inflateMonthView(parent: LinearLayout): View {
        return setupItemRoot(
            itemMargins = MarginValues.ZERO,
            daySize = daySize,
            context = parent.context,
            dayViewResource = dayViewResource,
            itemHeaderResource = monthHeaderResource,
            itemFooterResource = monthFooterResource,
            weekSize = 6,
            itemViewClass = monthViewClass,
            dayBinder = dayBinder as MonthDayBinder,
        ).also { monthContainer = it }.itemView
    }

    fun bindMonthView(month: CalendarMonth) {
        this.month = month
        monthContainer.itemView.apply {
            tag = monthTag(month.yearMonth)
            isVisible = true
        }
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

    fun makeInvisible() {
        monthContainer.itemView.apply {
            tag = null
            isInvisible = true
        }
    }

    fun isVisible(): Boolean = monthContainer.itemView.isVisible

    fun reloadMonth(yearMonth: YearMonth): Boolean {
        return if (yearMonth == month.yearMonth) {
            bindMonthView(month)
            true
        } else {
            false
        }
    }

    fun reloadDay(day: CalendarDay): Boolean = monthContainer.weekHolders.any { it.reloadDay(day) }
}
