package com.kizitonwose.calendarcompose.shared

import java.time.DayOfWeek

// E.g DayOfWeek.SATURDAY.daysUntil(DayOfWeek.TUESDAY) = 3
internal fun DayOfWeek.daysUntil(other: DayOfWeek) = (7 + (other.value - value)) % 7
