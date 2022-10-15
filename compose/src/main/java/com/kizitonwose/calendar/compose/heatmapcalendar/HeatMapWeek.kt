package com.kizitonwose.calendar.compose.heatmapcalendar

import androidx.compose.runtime.Immutable
import com.kizitonwose.calendar.compose.HeatMapCalendar
import com.kizitonwose.calendar.core.CalendarDay
import java.io.Serializable

/**
 * Represents a week on the heatmap calendar.
 *
 * This model exists only as a wrapper class with the [Immutable] annotation for compose.
 * The alternative would be to use the `kotlinx.ImmutableList` type for the `days` value
 * which is used ONLY in the dayContent parameter of the [HeatMapCalendar] but then we
 * would force that dependency on the library consumers.
 *
 * @param days the days in this week.
 */
@Immutable
data class HeatMapWeek(val days: List<CalendarDay>) : Serializable
