package com.kizitonwose.calendar.view.internal

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout
import androidx.core.view.children
import androidx.core.view.isGone
import com.kizitonwose.calendar.view.DaySize

private class WidthDivisorLinearLayout : LinearLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) :
        super(context, attrs, defStyle)

    var widthDivisor: Int = 0

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        @Suppress("NAME_SHADOWING")
        val heightMeasureSpec = if (widthDivisor > 0) {
            val width = MeasureSpec.getSize(widthMeasureSpec)
            MeasureSpec.makeMeasureSpec(width / widthDivisor, MeasureSpec.EXACTLY)
        } else {
            heightMeasureSpec
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        check(children.none { it.isGone }) {
            "Use `View.INVISIBLE` to hide any unneeded day content instead of `View.GONE`"
        }
    }
}

internal class WeekHolder<Day>(
    private val daySize: DaySize,
    private val dayHolders: List<DayHolder<Day>>,
) {

    private lateinit var weekContainer: LinearLayout

    fun inflateWeekView(parent: LinearLayout): View {
        return WidthDivisorLinearLayout(parent.context).apply {
            weekContainer = this
            val width = if (daySize.parentDecidesWidth) MATCH_PARENT else WRAP_CONTENT
            val height = if (daySize.parentDecidesHeight) MATCH_PARENT else WRAP_CONTENT
            val weight = if (daySize.parentDecidesHeight) 1f else 0f
            layoutParams = LinearLayout.LayoutParams(width, height, weight)
            orientation = LinearLayout.HORIZONTAL
            weightSum = dayHolders.count().toFloat()
            widthDivisor = if (daySize == DaySize.Square) dayHolders.count() else 0
            for (holder in dayHolders) {
                addView(holder.inflateDayView(this))
            }
        }
    }

    fun bindWeekView(daysOfWeek: List<Day>) {
        // The last week row can be empty if out date style is not `EndOfGrid`
        weekContainer.isGone = daysOfWeek.isEmpty()
        daysOfWeek.forEachIndexed { index, day ->
            dayHolders[index].bindDayView(day)
        }
    }

    fun reloadDay(day: Day): Boolean = dayHolders.any { it.reloadViewIfNecessary(day) }
}
