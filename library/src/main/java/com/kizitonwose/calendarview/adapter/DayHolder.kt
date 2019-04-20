package com.kizitonwose.calendarview.adapter

import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.annotation.LayoutRes
import com.kizitonwose.calendarview.SquareFrameLayout
import com.kizitonwose.calendarview.model.CalendarDay
import com.kizitonwose.calendarview.utils.inflate

class DayHolder(
    @LayoutRes private val dayViewRes: Int,
    private val dateClickListener: DateClickListener,
    private val dateViewBinder: DateViewBinder
) {
    private lateinit var dateView: View
    private lateinit var containerView: SquareFrameLayout

    private var currentDay: CalendarDay? = null

    fun inflateDayView(parent: LinearLayout): View {
        if (::dateView.isInitialized.not()) {
            dateView = parent.inflate(dayViewRes).apply {
                // We ensure the layout params of the child view is MATCH PARENT
                // so it fills the SquareFrameLayout.
                layoutParams = layoutParams.apply {
                    height = ViewGroup.LayoutParams.MATCH_PARENT
                    width = ViewGroup.LayoutParams.MATCH_PARENT
                }
            }
            containerView = SquareFrameLayout(parent.context).apply {
                setOnClickListener {
                    currentDay?.let { day ->
                        dateClickListener.invoke(day)
                    }
                }
                // We wrap the view in a SquareFrameLayout for use in the Week row layout which is
                // a LinearLayout hence we use LinearLayout.LayoutParams for the  SquareFrameLayout
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1F)
                addView(dateView)
            }
        }
        return containerView
    }

    fun bindDayView(currentDay: CalendarDay) {
        this.currentDay = currentDay
        dateViewBinder.invoke(dateView, currentDay)
    }

}
