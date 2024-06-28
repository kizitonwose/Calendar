package com.kizitonwose.calendar.compose

import androidx.compose.runtime.Composable
import com.kizitonwose.calendar.core.OutDateStyle
import com.kizitonwose.calendar.data.JdkMonthDataProducer
import java.time.DayOfWeek
import java.time.YearMonth

/**
 * Creates a [CalendarState] that is remembered across compositions.
 *
 * @param startMonth the initial value for [CalendarState.startMonth]
 * @param endMonth the initial value for [CalendarState.endMonth]
 * @param firstDayOfWeek the initial value for [CalendarState.firstDayOfWeek]
 * @param firstVisibleMonth the initial value for [CalendarState.firstVisibleMonth]
 * @param outDateStyle the initial value for [CalendarState.outDateStyle]
 */
@Suppress("NewApi")
@Composable
fun rememberCalendarState(
    startMonth: YearMonth = YearMonth.now(),
    endMonth: YearMonth = startMonth,
    firstVisibleMonth: YearMonth = startMonth,
    firstDayOfWeek: DayOfWeek = DayOfWeek.SUNDAY, // TODO MULTIPLATFORM
    outDateStyle: OutDateStyle = OutDateStyle.EndOfRow,
) = __rememberCalendarStateImpl(
    startMonth = startMonth,
    endMonth = endMonth,
    firstVisibleMonth = firstVisibleMonth,
    firstDayOfWeek = firstDayOfWeek,
    outDateStyle = outDateStyle,
    monthDataProducer = JdkMonthDataProducer,
)
