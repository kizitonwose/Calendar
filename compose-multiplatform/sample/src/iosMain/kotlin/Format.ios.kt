import androidx.compose.ui.text.intl.Locale
import com.kizitonwose.calendar.core.daysOfWeek
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Month
import platform.Foundation.NSCalendar
import platform.Foundation.NSLocale

actual fun Month.getDisplayName(short: Boolean, locale: Locale): String =
    NSCalendar.currentCalendar.let {
        it.setLocale(NSLocale(locale.toLanguageTag()))
        it.monthSymbols[Month.entries.indexOf(this)] as String
    }

actual fun DayOfWeek.getDisplayName(narrow: Boolean, locale: Locale): String =
    NSCalendar.currentCalendar.let {
        it.setLocale(NSLocale(locale.toLanguageTag()))
        val values = if (narrow) {
            it.veryShortWeekdaySymbols
        } else {
            it.shortWeekdaySymbols
        }
        values[sundayBasedWeek.indexOf(this)] as String
    }

private val sundayBasedWeek = daysOfWeek(firstDayOfWeek = DayOfWeek.SUNDAY)
