package calendar.core

import androidx.compose.runtime.Immutable
import kotlinx.datetime.Month

@Immutable
data class YearMonth(val year: Int, val month: Month)
