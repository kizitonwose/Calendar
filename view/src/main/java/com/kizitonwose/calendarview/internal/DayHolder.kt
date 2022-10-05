package com.kizitonwose.calendarview.internal

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.annotation.LayoutRes
import androidx.core.view.*
import com.kizitonwose.calendarcore.CalendarDay
import com.kizitonwose.calendarview.DayBinder
import com.kizitonwose.calendarview.ViewContainer

internal data class DayConfig(
    val daySizeSquare: Boolean,
    @LayoutRes val dayViewRes: Int,
    val viewBinder: DayBinder<ViewContainer>,
)

private class DaySquareFrameLayout : FrameLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) :
            super(context, attrs, defStyle)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec)
    }
}

internal class DayHolder(private val config: DayConfig) {

    private lateinit var dateView: View
    private lateinit var dateViewParent: FrameLayout
    private lateinit var viewContainer: ViewContainer
    private var day: CalendarDay? = null

    fun inflateDayView(parent: LinearLayout): View {
        // This will be placed in the WeekLayout(A LinearLayout) hence we
        // use LinearLayout.LayoutParams and set the weight appropriately.
        // The parent's wightSum is already set to 7 to accommodate seven week days.
        dateViewParent = if (config.daySizeSquare) {
            DaySquareFrameLayout(parent.context).apply {
                layoutParams = LinearLayout.LayoutParams(0, MATCH_PARENT, 1f)
            }
        } else {
            FrameLayout(parent.context).apply {
                layoutParams = LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
            }
        }
        dateView = dateViewParent.inflate(config.dayViewRes).also { view ->
            if (config.daySizeSquare) {
                val lp = FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
                lp.topMargin = view.marginTop
                lp.bottomMargin = view.marginBottom
                lp.leftMargin = view.marginLeft
                lp.rightMargin = view.marginRight
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    lp.marginEnd = view.marginEnd
                    lp.marginStart = view.marginStart
                }
                view.layoutParams = lp
            }
        }
        dateViewParent.addView(dateView)
        return dateViewParent
    }

    fun bindDayView(currentDay: CalendarDay) {
        this.day = currentDay
        if (!::viewContainer.isInitialized) {
            viewContainer = config.viewBinder.create(dateView)
        }

        val dayHash = currentDay.date.hashCode()
        if (dateViewParent.tag != dayHash) {
            dateViewParent.tag = dayHash
        }

        config.viewBinder.bind(viewContainer, currentDay)
    }

    fun reloadViewIfNecessary(day: CalendarDay): Boolean {
        return if (day == this.day) {
            bindDayView(day)
            true
        } else {
            false
        }
    }
}
