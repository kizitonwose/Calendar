package com.kizitonwose.calendarview.ui

import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.kizitonwose.calendarview.model.CalendarDay
import java.lang.RuntimeException

internal class WeekHolder(
    dayConfig: DayConfig,
    private val rightToLeft: Boolean = false
) {

    val dayHolders = (1..7).map { DayHolder(dayConfig) }

    private lateinit var container: LinearLayout

    fun inflateWeekView(parent: LinearLayout): View {
        container = LinearLayout(parent.context).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            orientation = LinearLayout.HORIZONTAL
            weightSum = dayHolders.count().toFloat()
            clipChildren = false //#ClipChildrenFix
            for (holder in dayHolders) {
                addView(holder.inflateDayView(this))
            }

            if(rightToLeft) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    layoutDirection = View.LAYOUT_DIRECTION_RTL
                }else{
                    throw RuntimeException("Right to left layout is only supported in sdk >= 17")
                }
            }
        }
        return container
    }

    fun bindWeekView(daysOfWeek: List<CalendarDay>) {
        container.visibility = if (daysOfWeek.isEmpty()) View.GONE else View.VISIBLE
        dayHolders.forEachIndexed { index, holder ->
            // Indices can be null if OutDateStyle is NONE. We set the
            // visibility for the views at these indices to INVISIBLE.
            holder.bindDayView(daysOfWeek.getOrNull(index))
        }
    }

    fun reloadDay(day: CalendarDay) {
        dayHolders.first { it.day == day }.reloadView()
    }
}
