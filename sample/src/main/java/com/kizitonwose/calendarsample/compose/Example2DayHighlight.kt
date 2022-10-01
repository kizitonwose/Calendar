package com.kizitonwose.calendarsample.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kizitonwose.calendarcompose.CalendarDay
import com.kizitonwose.calendarcompose.DayPosition
import com.kizitonwose.calendarsample.ContinuousSelectionHelper
import com.kizitonwose.calendarsample.DateSelection
import com.kizitonwose.calendarsample.R
import java.time.LocalDate

fun Modifier.backgroundHighlight(
    day: CalendarDay,
    today: LocalDate,
    selection: DateSelection,
    padding: Dp,
    color: Color,
    textColor: (Color) -> Unit,
): Modifier = composed {
    val (startDate, endDate) = selection
    when (day.position) {
        DayPosition.MonthDate -> {
            when {
                day.date.isBefore(today) -> {
                    textColor(colorResource(R.color.inactive_text_color))
                    this
                }
                day.date == startDate -> {
                    textColor(Color.White)
                    padding(padding)
                        .background(color = color, shape = CircleShape)
                }
                day.date == endDate -> {
                    textColor(Color.White)
                    padding(padding)
                        .background(color = color, shape = CircleShape)

                }
                day.date == today -> {
                    textColor(colorResource(R.color.example_4_grey))
                    padding(padding)
                        .border(
                            width = 1.dp,
                            shape = CircleShape,
                            color = colorResource(R.color.inactive_text_color))
                }
                else -> {
                    textColor(colorResource(R.color.example_4_grey))
                    this
                }
            }
        }
        DayPosition.InDate, DayPosition.OutDate -> this
    }
}

fun Modifier.continuousBackgroundHighlight(
    day: CalendarDay,
    selection: DateSelection,
    color: Color,
    padding: Dp,
): Modifier {
    val (startDate, endDate) = selection
    return when (day.position) {
        DayPosition.MonthDate -> {
            when {
                day.date == startDate && endDate != null -> {
                    padding(vertical = padding)
                        .clipHalf(start = true)
                        .background(color = color)
                }
                startDate != null && endDate != null && (day.date > startDate && day.date < endDate) -> {
                    padding(vertical = padding)
                        .background(color = color)
                }
                day.date == endDate && startDate != null -> {
                    padding(vertical = padding)
                        .clipHalf(start = false)
                        .background(color = color)
                }
                else -> this
            }
        }
        DayPosition.InDate -> {
            if (startDate != null && endDate != null &&
                ContinuousSelectionHelper.isInDateBetween(day.date, startDate, endDate)
            ) {
                padding(vertical = padding)
                    .background(color = color)
            } else this
        }
        DayPosition.OutDate -> {
            if (startDate != null && endDate != null &&
                ContinuousSelectionHelper.isOutDateBetween(day.date, startDate, endDate)
            ) {
                padding(vertical = padding)
                    .background(color = color)
            } else this
        }
    }
}


/**
 * Old AirBnb highlight style, like in the View example 4.
 */
fun Modifier.backgroundHighlightLegacy(
    day: CalendarDay,
    today: LocalDate,
    selection: DateSelection,
    color: Color,
    textColor: (Color) -> Unit,
): Modifier = composed {
    val (startDate, endDate) = selection
    val padding = 4.dp
    when (day.position) {
        DayPosition.MonthDate -> {
            when {
                day.date.isBefore(today) -> {
                    textColor(colorResource(R.color.inactive_text_color))
                    this
                }
                startDate == day.date && endDate == null -> {
                    textColor(Color.White)
                    padding(padding)
                        .background(color = color, shape = CircleShape)
                }
                day.date == startDate -> {
                    textColor(Color.White)
                    padding(vertical = padding)
                        .background(
                            color = color,
                            shape = RoundedCornerShape(
                                topStartPercent = 50,
                                bottomStartPercent = 50
                            )
                        )
                }
                startDate != null && endDate != null && (day.date > startDate && day.date < endDate) -> {
                    textColor(Color.White)
                    padding(vertical = padding)
                        .background(color = color)
                }
                day.date == endDate -> {
                    textColor(Color.White)
                    padding(vertical = padding)
                        .background(
                            color = color,
                            shape = RoundedCornerShape(topEndPercent = 50, bottomEndPercent = 50)
                        )
                }
                day.date == today -> {
                    textColor(colorResource(R.color.example_4_grey))
                    padding(padding)
                        .border(
                            width = 1.dp,
                            shape = CircleShape,
                            color = colorResource(R.color.inactive_text_color))
                }
                else -> {
                    textColor(colorResource(R.color.example_4_grey))
                    this
                }
            }
        }
        DayPosition.InDate -> {
            textColor(Color.Transparent)
            if (startDate != null &&
                endDate != null &&
                ContinuousSelectionHelper.isInDateBetween(day.date, startDate, endDate)
            ) {
                padding(vertical = padding)
                    .background(color = color)
            } else this
        }
        DayPosition.OutDate -> {
            textColor(Color.Transparent)
            if (startDate != null &&
                endDate != null &&
                ContinuousSelectionHelper.isOutDateBetween(day.date, startDate, endDate)
            ) {
                padding(vertical = padding)
                    .background(color = color)
            } else this
        }
    }
}