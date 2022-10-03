package com.kizitonwose.calendarview2.utils

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.WeekFields
import java.util.*

internal fun ViewGroup.inflate(@LayoutRes layoutRes: Int, attachToRoot: Boolean = false): View {
    return LayoutInflater.from(context).inflate(layoutRes, this, attachToRoot)
}

internal fun Int?.orZero(): Int = this ?: 0

internal const val NO_INDEX = -1

internal val CoroutineScope.job: Job
    get() = requireNotNull(coroutineContext[Job])

internal fun View.getVerticalMargins(): Int {
    val marginParams = layoutParams as? ViewGroup.MarginLayoutParams
    return marginParams?.topMargin.orZero() + marginParams?.bottomMargin.orZero()
}

val LocalDate.yearMonth: YearMonth
    get() = YearMonth.of(year, month)

val YearMonth.next: YearMonth
    get() = this.plusMonths(1)

val YearMonth.previous: YearMonth
    get() = this.minusMonths(1)

fun daysOfWeek(firstDayOfWeek: DayOfWeek = firstDayOfWeekFromLocale()): List<DayOfWeek> {
    val pivot = 7 - firstDayOfWeek.ordinal
    val daysOfWeek = DayOfWeek.values()
    // Order `daysOfWeek` array so that firstDayOfWeek is at index 0.
    return (daysOfWeek.takeLast(pivot) + daysOfWeek.dropLast(pivot))
}

fun firstDayOfWeekFromLocale(): DayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek

