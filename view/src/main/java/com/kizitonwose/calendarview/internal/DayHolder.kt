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
import com.kizitonwose.calendarview.Binder
import com.kizitonwose.calendarview.DaySize
import com.kizitonwose.calendarview.ViewContainer

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

        val dayHash = currentDay.hashCode()
        if (dateViewParent.tag != dayHash) {
            dateViewParent.tag = dayHash
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
