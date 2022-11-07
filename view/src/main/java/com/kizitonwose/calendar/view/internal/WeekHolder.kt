package com.kizitonwose.calendar.view.internal

import android.view.View
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.MATCH_CONSTRAINT
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID
import androidx.core.view.isGone
import com.kizitonwose.calendar.view.DaySize
import com.kizitonwose.calendar.view.internal.constraints.DayCellHorizontalChain

internal class WeekHolder<Day>(
    private val daySize: DaySize,
    private val dayHolders: List<DayHolder<Day>>,
) {

    private lateinit var weekContainer: ConstraintLayout
    private val dayConstraintChain = DayCellHorizontalChain()

    @Suppress("KotlinConstantConditions")
    fun inflateWeekView(parent: ConstraintLayout, id: Int, topId: Int, bottomId: Int): View {
        return ConstraintLayout(parent.context).also { weekContainer ->
            this.weekContainer = weekContainer
            weekContainer.id = id
            val width = if (daySize.parentDecidesWidth) MATCH_CONSTRAINT else WRAP_CONTENT
            val height = if (daySize.parentDecidesHeight) MATCH_CONSTRAINT else WRAP_CONTENT
            weekContainer.layoutParams = ConstraintLayout.LayoutParams(width, height).apply {
                if (topId == PARENT_ID) topToTop = topId else topToBottom = topId
                startToStart = PARENT_ID
                endToEnd = PARENT_ID
                if (bottomId == PARENT_ID) {
                    // Only create a complete vertical chain if the content should
                    // match the parent's height, otherwise adjacent months with
                    // more weeks will shrink items to fit the size of that was
                    // calculated for the currently visible month.
                    if (daySize.parentDecidesHeight) bottomToBottom = bottomId
                } else {
                    bottomToTop = bottomId
                }
            }
            dayHolders.forEach { dayHolder ->
                val link = dayConstraintChain.getNextLink()
                weekContainer.addView(
                    dayHolder.inflateDayView(
                        parent = weekContainer,
                        id = link.id,
                        startId = link.previousId,
                        endId = link.nextId,
                    ),
                )
            }
        }
    }

    fun bindWeekView(daysOfWeek: List<Day>) {
        // The last week row can be empty if out date style is not `EndOfGrid`
        weekContainer.isGone = daysOfWeek.isEmpty()
        if (daysOfWeek.isNotEmpty()) {
            dayHolders.forEachIndexed { index, holder ->
                holder.bindDayView(daysOfWeek[index])
            }
        }
    }

    fun reloadDay(day: Day): Boolean = dayHolders.any { it.reloadViewIfNecessary(day) }
}
