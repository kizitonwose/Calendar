package com.kizitonwose.calendar.view.internal

import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout
import androidx.core.view.isGone
import com.kizitonwose.calendar.view.DaySize

internal class WeekHolder<Day>(
    private val daySize: DaySize,
    private val dayHolders: List<DayHolder<Day>>,
) {

    private lateinit var container: LinearLayout

    fun inflateWeekView(parent: LinearLayout): View {
        container = LinearLayout(parent.context).apply {
            val width = if (daySize.parentDecidesWidth) MATCH_PARENT else WRAP_CONTENT
            layoutParams = LinearLayout.LayoutParams(width, WRAP_CONTENT)
            orientation = LinearLayout.HORIZONTAL
            weightSum = dayHolders.count().toFloat()
            for (holder in dayHolders) {
                addView(holder.inflateDayView(this))
            }
        }
        return container
    }

    fun bindWeekView(daysOfWeek: List<Day>) {
        // The last week row can be empty if out date style is not `EndOfGrid`
        container.isGone = daysOfWeek.isEmpty()
        if (daysOfWeek.isNotEmpty()) {
            dayHolders.forEachIndexed { index, holder ->
                holder.bindDayView(daysOfWeek[index])
            }
        }
    }

    fun reloadDay(day: Day): Boolean = dayHolders.any { it.reloadViewIfNecessary(day) }
}
