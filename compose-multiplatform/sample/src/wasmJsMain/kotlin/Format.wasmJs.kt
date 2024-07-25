import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toLowerCase
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Month

actual fun Month.getDisplayName(short: Boolean, locale: Locale): String {
    val name = name.toLowerCase(enLocale).capitalize(enLocale)
    return if (short) name.take(3) else name
}

actual fun DayOfWeek.getDisplayName(narrow: Boolean, locale: Locale): String {
    return name.toLowerCase(enLocale).capitalize(enLocale).take(if (narrow) 1 else 3)
}

private val enLocale = Locale("en-US")
