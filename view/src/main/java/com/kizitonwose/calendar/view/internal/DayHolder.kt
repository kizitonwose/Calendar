package com.kizitonwose.calendar.view.internal

import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.LinearLayout
import androidx.annotation.LayoutRes
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.WeekDay
import com.kizitonwose.calendar.view.Binder
import com.kizitonwose.calendar.view.DaySize
import com.kizitonwose.calendar.view.ViewContainer
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

    fun inflateDayView(parent: LinearLayout): View {
        return parent.inflate(config.dayViewRes).apply {
            dayView = this
            layoutParams = DayLinearLayoutParams(layoutParams).apply {
                if (config.daySize.parentDecidesWidth) {
                    width = 0
                    weight = 1f
                }
                when (config.daySize) {
                    DaySize.Square,
                    DaySize.Rectangle,
                    -> {
                        height = MATCH_PARENT
                    }

                    DaySize.SeventhWidth,
                    DaySize.FreeForm,
                    -> Unit
                }
            }
        }
    }

    fun bindDayView(currentDay: Day) {
        this.day = currentDay
        if (!::viewContainer.isInitialized) {
            viewContainer = config.dayBinder.create(dayView)
        }
        dayView.tag = dayTag(findDate(currentDay))
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

@Suppress("FunctionName")
internal fun DayLinearLayoutParams(layoutParams: ViewGroup.LayoutParams): LinearLayout.LayoutParams =
    if (layoutParams is ViewGroup.MarginLayoutParams) {
        // Ensure we call the correct constructor.
        LinearLayout.LayoutParams(layoutParams)
    } else {
        LinearLayout.LayoutParams(layoutParams)
    }
