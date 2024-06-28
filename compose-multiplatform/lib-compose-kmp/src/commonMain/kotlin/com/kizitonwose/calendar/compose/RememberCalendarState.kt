package com.kizitonwose.calendar.compose

import androidx.compose.runtime.Composable
import com.kizitonwose.calendar.core.OutDateStyle
import com.kizitonwose.calendar.core.YearMonth
import com.kizitonwose.calendar.core.now
import com.kizitonwose.calendar.data.KotlinDateTimeMonthDataProducer
import kotlinx.datetime.DayOfWeek

/**
 * Creates a [CalendarState] that is remembered across compositions.
 *
 * @param startMonth the initial value for [CalendarState.startMonth]
 * @param endMonth the initial value for [CalendarState.endMonth]
 * @param firstDayOfWeek the initial value for [CalendarState.firstDayOfWeek]
 * @param firstVisibleMonth the initial value for [CalendarState.firstVisibleMonth]
 * @param outDateStyle the initial value for [CalendarState.outDateStyle]
 */
@Composable
fun rememberCalendarState(
    startMonth: YearMonth = YearMonth.now(),
    endMonth: YearMonth = startMonth,
    firstVisibleMonth: YearMonth = startMonth,
    firstDayOfWeek: DayOfWeek = DayOfWeek.SUNDAY,
    outDateStyle: OutDateStyle = OutDateStyle.EndOfRow,
) = __rememberCalendarStateImpl(
    startMonth = startMonth,
    endMonth = endMonth,
    firstVisibleMonth = firstVisibleMonth,
    firstDayOfWeek = firstDayOfWeek,
    outDateStyle = outDateStyle,
    monthDataProducer = KotlinDateTimeMonthDataProducer,
)
