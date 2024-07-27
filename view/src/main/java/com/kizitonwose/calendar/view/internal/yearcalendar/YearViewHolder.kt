package com.kizitonwose.calendar.view.internal.yearcalendar

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.CalendarYear
import com.kizitonwose.calendar.view.ViewContainer
import com.kizitonwose.calendar.view.YearHeaderFooterBinder
import com.kizitonwose.calendar.view.internal.MonthHolder
import java.time.YearMonth

internal class YearViewHolder(
    rootLayout: ViewGroup,
    private val headerView: View?,
    private val footerView: View?,
    private val monthHolders: List<List<MonthHolder>>,
    private val yearHeaderBinder: YearHeaderFooterBinder<ViewContainer>?,
    private val yearFooterBinder: YearHeaderFooterBinder<ViewContainer>?,
) : RecyclerView.ViewHolder(rootLayout) {
    private var yearHeaderContainer: ViewContainer? = null
    private var yearFooterContainer: ViewContainer? = null

    lateinit var year: CalendarYear

    fun bindYear(year: CalendarYear) {
        this.year = year
        headerView?.let { view ->
            val headerContainer = yearHeaderContainer ?: yearHeaderBinder!!.create(view).also {
                yearHeaderContainer = it
            }
            yearHeaderBinder?.bind(headerContainer, year)
        }
        val months = year.months.filter { true }
        monthHolders.flatten().forEachIndexed { index, month ->
            if (months.size > index) {
                month.bindMonthView(months[index])
            } else {
                month.hide()
            }
        }
        footerView?.let { view ->
            val footerContainer = yearFooterContainer ?: yearFooterBinder!!.create(view).also {
                yearFooterContainer = it
            }
            yearFooterBinder?.bind(footerContainer, year)
        }
    }

    fun reloadMonth(yearMonth: YearMonth) {
        monthHolders.flatten()
            .filter { it.isShown() }
            .firstOrNull { it.reloadMonth(yearMonth) }
    }

    fun reloadDay(day: CalendarDay) {
        monthHolders.flatten()
            .filter { it.isShown() }
            .firstOrNull { it.reloadDay(day) }
    }
}
