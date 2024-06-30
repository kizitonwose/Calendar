import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.intl.Locale
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Month

actual fun Month.getDisplayName(short: Boolean, locale: Locale): String {
    val name = name.capitalize(EnLocale)
    return if (short) name.take(3) else name
}

actual fun DayOfWeek.getShortDisplayName(locale: Locale): String {
    return name.capitalize(EnLocale).first().toString()
}

private val EnLocale = Locale("en-US")
