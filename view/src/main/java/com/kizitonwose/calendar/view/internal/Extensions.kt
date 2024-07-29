package com.kizitonwose.calendar.view.internal

import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.nextMonth
import com.kizitonwose.calendar.core.previousMonth
import com.kizitonwose.calendar.core.yearMonth
import java.time.YearMonth

internal fun ViewGroup.inflate(@LayoutRes layoutRes: Int, attachToRoot: Boolean = false): View {
    return LayoutInflater.from(context).inflate(layoutRes, this, attachToRoot)
}

internal const val NO_INDEX = -1

// Find the actual month on the calendar where this date is shown.
internal val CalendarDay.positionYearMonth: YearMonth
    get() = when (position) {
        DayPosition.InDate -> date.yearMonth.nextMonth
        DayPosition.MonthDate -> date.yearMonth
        DayPosition.OutDate -> date.yearMonth.previousMonth
    }

internal fun Rect.intersects(other: Rect): Boolean {
    return if (this.isEmpty || other.isEmpty) {
        false
    } else {
        Rect.intersects(this, other)
    }
}
