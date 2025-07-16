import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toUpperCase
import com.kizitonwose.calendar.core.Week
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Month
import kotlinx.datetime.YearMonth
import kotlinx.datetime.yearMonth

fun YearMonth.displayText(short: Boolean = false): String {
    return "${month.displayText(short = short)} $year"
}

fun Month.displayText(short: Boolean = true): String {
    return getDisplayName(short, enLocale)
}

fun DayOfWeek.displayText(uppercase: Boolean = false, narrow: Boolean = false): String {
    return getDisplayName(narrow, enLocale).let { value ->
        if (uppercase) value.toUpperCase(enLocale) else value
    }
}

expect fun Month.getDisplayName(short: Boolean, locale: Locale): String

expect fun DayOfWeek.getDisplayName(narrow: Boolean = false, locale: Locale): String

private val enLocale = Locale("en-US")

fun getWeekPageTitle(week: Week): String {
    val firstDate = week.days.first().date
    val lastDate = week.days.last().date
    return when {
        firstDate.yearMonth == lastDate.yearMonth -> {
            firstDate.yearMonth.displayText()
        }

        firstDate.year == lastDate.year -> {
            "${firstDate.month.displayText(short = false)} - ${lastDate.yearMonth.displayText()}"
        }

        else -> {
            "${firstDate.yearMonth.displayText()} - ${lastDate.yearMonth.displayText()}"
        }
    }
}
