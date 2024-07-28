package com.kizitonwose.calendar.view.internal.yearcalendar

import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.CalendarMonth
import com.kizitonwose.calendar.core.CalendarYear
import com.kizitonwose.calendar.view.ViewContainer
import com.kizitonwose.calendar.view.YearHeaderFooterBinder
import java.time.YearMonth

internal class YearViewHolder(
    rootLayout: ViewGroup,
    private val headerView: View?,
    private val footerView: View?,
    private val monthRowHolders: List<Pair<LinearLayout, List<YearMonthHolder>>>,
    private val yearHeaderBinder: YearHeaderFooterBinder<ViewContainer>?,
    private val yearFooterBinder: YearHeaderFooterBinder<ViewContainer>?,
    private val isMonthVisible: (month: CalendarMonth) -> Boolean,
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
        val months = year.months.filter(isMonthVisible)
        var index = 0
        for ((rowLayout, row) in monthRowHolders) {
            for (monthHolder in row) {
                if (months.size > index) {
                    monthHolder.bindMonthView(months[index])
                } else {
                    monthHolder.makeInvisible()
                }
                index += 1
            }
            rowLayout.isVisible = row.any(YearMonthHolder::isVisible)
        }
        footerView?.let { view ->
            val footerContainer = yearFooterContainer ?: yearFooterBinder!!.create(view).also {
                yearFooterContainer = it
            }
            yearFooterBinder?.bind(footerContainer, year)
        }
    }

    fun reloadMonth(yearMonth: YearMonth) {
        visibleItems().firstOrNull {
            it.reloadMonth(yearMonth)
        }
    }

    fun reloadDay(day: CalendarDay) {
        visibleItems().firstOrNull {
            it.reloadDay(day)
        }
    }

    private fun visibleItems() = monthRowHolders
        .map { it.second }
        .flatten()
        .filter { it.isVisible() }
}
