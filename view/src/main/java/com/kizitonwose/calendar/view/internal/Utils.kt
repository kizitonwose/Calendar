package com.kizitonwose.calendar.view.internal

import android.content.Context
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.ViewGroup.MarginLayoutParams
import android.widget.LinearLayout
import com.kizitonwose.calendar.view.Binder
import com.kizitonwose.calendar.view.DaySize
import com.kizitonwose.calendar.view.MarginValues
import com.kizitonwose.calendar.view.ViewContainer
import java.time.LocalDate

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
    } else {
        null
    }

    @Suppress("UNCHECKED_CAST")
    val dayConfig = DayConfig(
        daySize = daySize,
        dayViewRes = dayViewResource,
        dayBinder = dayBinder as Binder<Day, ViewContainer>,
    )

    val weekHolders = List(weekSize) {
        WeekHolder(
            daySize = dayConfig.daySize,
            dayHolders = List(size = 7) { DayHolder(dayConfig) },
        )
    }.onEach { weekHolder ->
        rootLayout.addView(weekHolder.inflateWeekView(rootLayout))
    }

    val itemFooterView = if (itemFooterResource != 0) {
        rootLayout.inflate(itemFooterResource).also { footerView ->
            rootLayout.addView(footerView)
        }
    } else {
        null
    }

    fun setupRoot(root: ViewGroup) {
        val width = if (daySize.parentDecidesWidth) MATCH_PARENT else WRAP_CONTENT
        val height = if (daySize.parentDecidesHeight) MATCH_PARENT else WRAP_CONTENT
        root.layoutParams = MarginLayoutParams(width, height).apply {
            bottomMargin = itemMargins.bottom
            topMargin = itemMargins.top
            marginStart = itemMargins.start
            marginEnd = itemMargins.end
        }
    }

    val itemView = itemViewClass?.let {
        val customLayout = runCatching {
            Class.forName(it)
                .getDeclaredConstructor(Context::class.java)
                .newInstance(rootLayout.context) as ViewGroup
        }.onFailure {
            Log.e(
                "CalendarView",
                "Failure loading custom class $itemViewClass, " +
                    "check that $itemViewClass is a ViewGroup and the " +
                    "single argument context constructor is available. " +
                    "For an example on how to use a custom class, see: $EXAMPLE_CUSTOM_CLASS_URL",
                it,
            )
        }.getOrNull()

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

internal fun dayTag(date: LocalDate): Int = date.hashCode()
internal const val EXAMPLE_CUSTOM_CLASS_URL =
    "https://github.com/kizitonwose/Calendar/blob/3dfb2d2e91d5e443b540ff411113a05268e4b8d2/sample/src/main/java/com/kizitonwose/calendar/sample/view/Example6Fragment.kt#L29"
