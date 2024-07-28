package com.kizitonwose.calendar.view.internal.yearcalendar

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RectShape
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
import com.kizitonwose.calendar.view.internal.MonthHolder
import com.kizitonwose.calendar.view.internal.customViewOrRoot
import com.kizitonwose.calendar.view.internal.inflate
import kotlin.math.min

internal data class YearItemContent(
    val itemView: ViewGroup,
    val headerView: View?,
    val footerView: View?,
    val monthRowHolders: List<Pair<LinearLayout, List<MonthHolder>>>,
)

internal fun setupYearItemRoot(
    monthColumns: Int,
    monthHorizontalSpacing: Int,
    monthVerticalSpacing: Int,
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
    // Put the months in a separate layout so we can have
    // dividers that ignore the year headers and footers.
    val monthsLayout = DividerLinearLayout(
        context = context,
        orientation = LinearLayout.VERTICAL,
        axisSpacing = monthVerticalSpacing,
    )

    val itemHeaderView = if (yearItemHeaderResource != 0) {
        rootLayout.inflate(yearItemHeaderResource).also { headerView ->
            rootLayout.addView(headerView)
        }
    } else {
        null
    }
    val monthCount = 12
    val rows = (monthCount / monthColumns) + min(1, monthCount % monthColumns)
    val monthHolders = List(rows) {
        val rowLayout = DividerLinearLayout(
            context = context,
            orientation = LinearLayout.HORIZONTAL,
            axisSpacing = monthHorizontalSpacing,
        )
        val row = List(monthColumns) {
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
            val height = if (daySize.parentDecidesHeight) MATCH_PARENT else WRAP_CONTENT
            rowLayout.addView(
                monthHolder.inflateMonthView(rowLayout),
                LinearLayout.LayoutParams(0, height, 1f),
            )
        }
        val width = if (daySize.parentDecidesWidth) MATCH_PARENT else WRAP_CONTENT
        val height = if (daySize.parentDecidesHeight) 0 else WRAP_CONTENT
        val weight = if (daySize.parentDecidesHeight) 1f else 0f
        monthsLayout.addView(
            rowLayout,
            LinearLayout.LayoutParams(width, height, weight),
        )
        return@List rowLayout to row
    }

    run {
        val width = if (daySize.parentDecidesWidth) MATCH_PARENT else WRAP_CONTENT
        val height = if (daySize.parentDecidesHeight) 0 else WRAP_CONTENT
        val weight = if (daySize.parentDecidesHeight) 1f else 0f
        rootLayout.addView(
            monthsLayout,
            LinearLayout.LayoutParams(width, height, weight),
        )
    }

    val itemFooterView = if (yearItemFooterResource != 0) {
        rootLayout.inflate(yearItemFooterResource).also { footerView ->
            rootLayout.addView(footerView)
        }
    } else {
        null
    }

    val itemView = customViewOrRoot(
        customViewClass = yearItemViewClass,
        rootLayout = rootLayout,
    ) { root: ViewGroup ->
        val width = if (daySize.parentDecidesWidth) MATCH_PARENT else WRAP_CONTENT
        val height = if (daySize.parentDecidesHeight) MATCH_PARENT else WRAP_CONTENT
        root.layoutParams = MarginLayoutParams(width, height).apply {
            bottomMargin = yearItemMargins.bottom
            topMargin = yearItemMargins.top
            marginStart = yearItemMargins.start
            marginEnd = yearItemMargins.end
        }
    }

    return YearItemContent(
        itemView = itemView,
        headerView = itemHeaderView,
        footerView = itemFooterView,
        monthRowHolders = monthHolders,
    )
}

@Suppress("FunctionName")
private fun DividerLinearLayout(
    context: Context,
    orientation: Int,
    axisSpacing: Int,
) = LinearLayout(context).apply {
    this.orientation = orientation
    if (axisSpacing > 0) {
        showDividers = LinearLayout.SHOW_DIVIDER_MIDDLE
        dividerDrawable = ShapeDrawable(RectShape()).apply {
            if (orientation == LinearLayout.VERTICAL) {
                intrinsicHeight = axisSpacing
            } else {
                intrinsicWidth = axisSpacing
            }
            paint.color = Color.TRANSPARENT
        }
    }
}
