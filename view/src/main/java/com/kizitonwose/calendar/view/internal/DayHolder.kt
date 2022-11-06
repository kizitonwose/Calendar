package com.kizitonwose.calendar.view.internal

import android.view.View
import androidx.annotation.LayoutRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.*
import androidx.core.view.*
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.WeekDay
import com.kizitonwose.calendar.view.Binder
import com.kizitonwose.calendar.view.DaySize
import com.kizitonwose.calendar.view.ViewContainer
import com.kizitonwose.calendar.view.internal.constraints.ConstraintLayoutParams
import java.time.LocalDate

internal data class DayConfig<Day>(
    val daySize: DaySize,
    @LayoutRes val dayViewRes: Int,
    val dayBinder: Binder<Day, ViewContainer>,
)

internal class DayHolder<Day>(private val config: DayConfig<Day>) {

    private lateinit var dayView: View
    private lateinit var viewContainer: ViewContainer
    private var day: Day? = null

    @Suppress("KotlinConstantConditions")
    fun inflateDayView(parent: ConstraintLayout, id: Int, startId: Int, endId: Int): View {
        return parent.inflate(config.dayViewRes).also { dayView ->
            this.dayView = dayView
            val daySize = config.daySize
            dayView.id = id
            dayView.layoutParams = ConstraintLayoutParams(dayView.layoutParams).apply {
                if (startId == PARENT_ID) startToStart = startId else startToEnd = startId
                topToTop = PARENT_ID
                bottomToBottom = PARENT_ID
                if (endId == PARENT_ID) endToEnd = endId else endToStart = endId
                when (daySize) {
                    DaySize.Square -> {
                        width = MATCH_CONSTRAINT
                        height = MATCH_CONSTRAINT
                        dimensionRatio = "1:1"
                    }
                    DaySize.SeventhWidth -> {
                        width = MATCH_CONSTRAINT
                    }
                    DaySize.FreeForm -> {}
                    DaySize.Rectangle -> {
                        width = MATCH_CONSTRAINT
                        height = MATCH_CONSTRAINT
                    }
                }
            }
        }
    }

    fun bindDayView(currentDay: Day) {
        this.day = currentDay
        if (!::viewContainer.isInitialized) {
            viewContainer = config.dayBinder.create(dayView)
        }

        val dayTag = dayTag(findDate(currentDay))
        if (dayView.tag != dayTag) {
            dayView.tag = dayTag
        }

        config.dayBinder.bind(viewContainer, currentDay)
    }

    fun reloadViewIfNecessary(day: Day): Boolean {
        return if (day == this.day) {
            bindDayView(day)
            true
        } else {
            false
        }
    }
}

private fun findDate(day: Any?): LocalDate {
    return when (day) {
        is CalendarDay -> day.date
        is WeekDay -> day.date
        else -> throw IllegalArgumentException("Invalid day type: $day")
    }
}
