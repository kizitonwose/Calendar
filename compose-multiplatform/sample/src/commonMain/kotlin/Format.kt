import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toUpperCase
import com.kizitonwose.calendar.core.Week
import com.kizitonwose.calendar.core.YearMonth
import com.kizitonwose.calendar.core.yearMonth
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Month

fun YearMonth.displayText(short: Boolean = false): String {
    return "${month.displayText(short = short)} $year"
}

fun Month.displayText(short: Boolean = true): String {
    return getDisplayName(short, EnLocale)
}

fun DayOfWeek.displayText(uppercase: Boolean = false): String {
    return getShortDisplayName(EnLocale).let { value ->
        if (uppercase) value.toUpperCase(EnLocale) else value
    }
}

expect fun Month.getDisplayName(short: Boolean, locale: Locale): String

expect fun DayOfWeek.getShortDisplayName(locale: Locale): String

private val EnLocale = Locale("en-US")

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
