package com.kizitonwose.calendarview.internal

import android.content.Context
import android.os.Build
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.ViewGroup.MarginLayoutParams
import android.widget.LinearLayout
import com.kizitonwose.calendarview.Binder
import com.kizitonwose.calendarview.DaySize
import com.kizitonwose.calendarview.ViewContainer

internal data class ItemContent<Day>(
    val itemView: ViewGroup,
    val headerView: View?,
    val footerView: View?,
    val weekHolders: List<WeekHolder<Day>>,
)

internal fun <Day, Container : ViewContainer> setupItemRoot(
    itemMargins: MarginValues,
    daySize: DaySize,
    context: Context,
    dayViewResource: Int,
    itemHeaderResource: Int,
    itemFooterResource: Int,
    weekSize: Int,
    itemViewClass: String?,
    dayBinder: Binder<Day, Container>,
): ItemContent<Day> {
    val rootLayout = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
    }

    val itemHeaderView = if (itemHeaderResource != 0) {
        rootLayout.inflate(itemHeaderResource).also { headerView ->
            rootLayout.addView(headerView)
        }
    } else null

    @Suppress("UNCHECKED_CAST")
    val dayConfig = DayConfig(
        daySize = daySize,
        dayViewRes = dayViewResource,
        dayBinder = dayBinder as Binder<Day, ViewContainer>
    )

    val weekHolders = (1..weekSize)
        .map { WeekHolder(dayConfig.daySize, (1..7).map { DayHolder(dayConfig) }) }
        .onEach { weekHolder -> rootLayout.addView(weekHolder.inflateWeekView(rootLayout)) }

    val itemFooterView = if (itemFooterResource != 0) {
        rootLayout.inflate(itemFooterResource).also { footerView ->
            rootLayout.addView(footerView)
        }
    } else null

    fun setupRoot(root: ViewGroup) {
        val width = if (daySize.parentDecidesWidth) MATCH_PARENT else WRAP_CONTENT
        root.layoutParams = MarginLayoutParams(width, WRAP_CONTENT).apply {
            bottomMargin = itemMargins.bottom
            topMargin = itemMargins.top
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                marginStart = itemMargins.start
                marginEnd = itemMargins.end
            } else {
                leftMargin = itemMargins.start
                rightMargin = itemMargins.end
            }
        }
    }

    val itemView = itemViewClass?.let {
        val customLayout = runCatching {
            Class.forName(it)
                .getDeclaredConstructor(Context::class.java)
                .newInstance(rootLayout.context) as ViewGroup
        }.onFailure { Log.e("CalendarView", "failure loading custom class", it) }
            .getOrNull()

        customLayout?.apply {
            setupRoot(this)
            addView(rootLayout)
        }
    } ?: rootLayout.apply { setupRoot(this) }

    return ItemContent(
        itemView = itemView,
        headerView = itemHeaderView,
        footerView = itemFooterView,
        weekHolders = weekHolders,
    )
}