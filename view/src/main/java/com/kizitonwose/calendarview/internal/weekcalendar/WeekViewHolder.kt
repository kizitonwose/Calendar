package com.kizitonwose.calendarview.internal.weekcalendar

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kizitonwose.calendarcore.WeekDay
import com.kizitonwose.calendarview.ViewContainer
import com.kizitonwose.calendarview.WeekHeaderFooterBinder
import com.kizitonwose.calendarview.internal.WeekHolder

internal class WeekViewHolder constructor(
    rootLayout: ViewGroup,
    private val headerView: View?,
    private val footerView: View?,
    private val weekHolders: WeekHolder<WeekDay>,
    private var weekHeaderBinder: WeekHeaderFooterBinder<ViewContainer>?,
    private var weekFooterBinder: WeekHeaderFooterBinder<ViewContainer>?,
) : RecyclerView.ViewHolder(rootLayout) {

    private var headerContainer: ViewContainer? = null
    private var footerContainer: ViewContainer? = null

    lateinit var week: List<WeekDay>

    fun bindWeek(week: List<WeekDay>) {
        this.week = week
        headerView?.let { view ->
            val headerContainer = headerContainer ?: weekHeaderBinder!!.create(view).also {
                headerContainer = it
            }
            weekHeaderBinder?.bind(headerContainer, week)
        }
        footerView?.let { view ->
            val footerContainer = footerContainer ?: weekFooterBinder!!.create(view).also {
                footerContainer = it
            }
            weekFooterBinder?.bind(footerContainer, week)
        }
        weekHolders.bindWeekView(week)
    }

    fun reloadDay(day: WeekDay) {
        weekHolders.reloadDay(day)
    }
}
