package com.kizitonwose.calendar.view.internal

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

private class DaySquareFrameLayout : FrameLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) :
            super(context, attrs, defStyle)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec)
    }
}

internal class DayHolder<Day>(private val config: DayConfig<Day>) {

    private lateinit var dateView: View
    private lateinit var dateViewParent: FrameLayout
    private lateinit var viewContainer: ViewContainer
    private var day: Day? = null

    fun inflateDayView(parent: LinearLayout): View {
        // This will be placed in the WeekLayout(A LinearLayout) hence we
        // use LinearLayout.LayoutParams and set the weight appropriately.
        // The parent's wightSum is already set to 7 to accommodate seven week days.
        dateViewParent = when (config.daySize) {
            DaySize.Square -> DaySquareFrameLayout(parent.context).apply {
                layoutParams = LinearLayout.LayoutParams(0, MATCH_PARENT, 1f)
            }
            DaySize.SeventhWidth -> FrameLayout(parent.context).apply {
                layoutParams = LinearLayout.LayoutParams(0, WRAP_CONTENT, 1f)
            }
            DaySize.FreeForm -> FrameLayout(parent.context).apply {
                layoutParams = LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
            }
        }
        dateView = dateViewParent.inflate(config.dayViewRes).also { view ->
            if (config.daySize.parentDecidesWidth) {
                val height = if (config.daySize.parentDecidesHeight) MATCH_PARENT else WRAP_CONTENT
                val lp = FrameLayout.LayoutParams(MATCH_PARENT, height)
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

    fun bindDayView(currentDay: Day) {
        this.day = currentDay
        if (!::viewContainer.isInitialized) {
            viewContainer = config.dayBinder.create(dateView)
        }

        val dayTag = dayTag(findDate(currentDay))
        if (dateViewParent.tag != dayTag) {
            dateViewParent.tag = dayTag
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
