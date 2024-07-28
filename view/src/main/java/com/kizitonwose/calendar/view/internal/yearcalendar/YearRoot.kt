package com.kizitonwose.calendar.view.internal.yearcalendar

import android.content.Context
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.ViewGroup.MarginLayoutParams
import android.widget.LinearLayout
import com.kizitonwose.calendar.view.DaySize
import com.kizitonwose.calendar.view.MarginValues
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.MonthHeaderFooterBinder
import com.kizitonwose.calendar.view.ViewContainer
import com.kizitonwose.calendar.view.internal.EXAMPLE_CUSTOM_CLASS_URL
import com.kizitonwose.calendar.view.internal.MonthHolder
import com.kizitonwose.calendar.view.internal.inflate
import java.time.LocalDate
import kotlin.math.min

internal data class YearItemContent(
    val itemView: ViewGroup,
    val headerView: View?,
    val footerView: View?,
    val monthHolders: List<List<MonthHolder>>,
)

internal fun setupYearItemRoot(
    columns: Int,
    itemCount: Int,
    yearItemMargins: MarginValues,
    daySize: DaySize,
    context: Context,
    dayViewResource: Int,
    dayBinder: MonthDayBinder<ViewContainer>?,
    monthHeaderResource: Int,
    monthFooterResource: Int,
    monthViewClass: String?,
    monthHeaderBinder: MonthHeaderFooterBinder<ViewContainer>?,
    monthFooterBinder: MonthHeaderFooterBinder<ViewContainer>?,
    yearItemViewClass: String?,
    yearItemHeaderResource: Int,
    yearItemFooterResource: Int,
): YearItemContent {
    val rootLayout = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
    }

    val itemHeaderView = if (yearItemHeaderResource != 0) {
        rootLayout.inflate(yearItemHeaderResource).also { headerView ->
            rootLayout.addView(headerView)
        }
    } else {
        null
    }
    val rows = (itemCount / columns) + min(1, itemCount.rem(columns))
    val monthHolders = List(rows) {
        val rowLayout = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
        }
        // TODO - YEAR optimize size ignore unused index after filter
        val row = List(columns) {
            MonthHolder(
                daySize = daySize,
                dayViewResource = dayViewResource,
                dayBinder = dayBinder,
                monthHeaderResource = monthHeaderResource,
                monthFooterResource = monthFooterResource,
                monthViewClass = monthViewClass,
                monthHeaderBinder = monthHeaderBinder,
                monthFooterBinder = monthFooterBinder,
            )
        }.onEach { monthHolder ->
            // todo weight
            val width = if (daySize.parentDecidesWidth) MATCH_PARENT else WRAP_CONTENT
            val height = if (daySize.parentDecidesHeight) MATCH_PARENT else WRAP_CONTENT
            rowLayout.addView(
                monthHolder.inflateMonthView(rowLayout),
                LinearLayout.LayoutParams(0, height, 1f),
            )
        }
        // todo weight
        val width = if (daySize.parentDecidesWidth) MATCH_PARENT else WRAP_CONTENT
        val height = if (daySize.parentDecidesHeight) 0 else WRAP_CONTENT
        val weight = if (daySize.parentDecidesHeight) 1f else 0f
        rootLayout.addView(
            rowLayout,
            LinearLayout.LayoutParams(width, height, weight),
        )
        return@List row
    }

    val itemFooterView = if (yearItemFooterResource != 0) {
        rootLayout.inflate(yearItemFooterResource).also { footerView ->
            rootLayout.addView(footerView)
        }
    } else {
        null
    }

    fun setupRoot(root: ViewGroup) {
        val width = if (daySize.parentDecidesWidth) MATCH_PARENT else WRAP_CONTENT
        val height = if (daySize.parentDecidesHeight) MATCH_PARENT else WRAP_CONTENT
        root.layoutParams = MarginLayoutParams(width, height).apply {
            bottomMargin = yearItemMargins.bottom
            topMargin = yearItemMargins.top
            marginStart = yearItemMargins.start
            marginEnd = yearItemMargins.end
        }
    }

    val itemView = yearItemViewClass?.let {
        val customLayout = runCatching {
            Class.forName(it)
                .getDeclaredConstructor(Context::class.java)
                .newInstance(rootLayout.context) as ViewGroup
        }.onFailure {
            Log.e(
                "YearCalendarView",
                "Failure loading custom class $yearItemViewClass, " +
                    "check that $yearItemViewClass is a ViewGroup and the " +
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

    return YearItemContent(
        itemView = itemView,
        headerView = itemHeaderView,
        footerView = itemFooterView,
        monthHolders = monthHolders,
    )
}

internal fun dayTag(date: LocalDate): Int = date.hashCode()
