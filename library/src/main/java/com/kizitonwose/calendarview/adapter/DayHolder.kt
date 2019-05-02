package com.kizitonwose.calendarview.adapter

import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.annotation.LayoutRes
import androidx.annotation.Px
import com.kizitonwose.calendarview.model.CalendarDay
import com.kizitonwose.calendarview.utils.inflate

data class DayConfig(
    @Px val width: Int,
    @Px val height: Int,
    @LayoutRes val dayViewRes: Int,
    val dateClickListener: DateClickListener?,
    val dateViewBinder: DateViewBinder?
)

class DayHolder(private val config: DayConfig) {
    private lateinit var dateView: View
    private lateinit var containerView: FrameLayout

    var currentDay: CalendarDay? = null

    fun inflateDayView(parent: LinearLayout): View {
        if (::dateView.isInitialized.not()) {
            dateView = parent.inflate(config.dayViewRes).apply {
                // We ensure the layout params of the supplied child view is
                // MATCH_PARENT so it fills the parent container.
                layoutParams = layoutParams.apply {
                    height = ViewGroup.LayoutParams.MATCH_PARENT
                    width = ViewGroup.LayoutParams.MATCH_PARENT
                }
            }
            containerView = FrameLayout(parent.context).apply {
                setOnClickListener {
                    currentDay?.let { day ->
                        config.dateClickListener?.invoke(day)
                    }
                }
                // We return this Layout as DayView which will be place in the WeekLayout(A LinearLayout)
                // hence we use LinearLayout.LayoutParams and set the weight appropriately.
                // The parent's wightSum is already set to 7 to accommodate seven week days.
                layoutParams = LinearLayout.LayoutParams(config.width, config.height, 1F)
                addView(dateView)
            }
        }
        return containerView
    }

    fun bindDayView(currentDay: CalendarDay) {
        this.currentDay = currentDay
        config.dateViewBinder?.invoke(dateView, currentDay)
    }

    fun reloadView() {
        currentDay?.let { bindDayView(it) }
    }

}
