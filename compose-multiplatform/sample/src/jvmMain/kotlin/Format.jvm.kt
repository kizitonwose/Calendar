import androidx.compose.ui.text.intl.Locale
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Month
import java.time.format.TextStyle
import java.util.Locale as JavaLocale

actual fun Month.getDisplayName(short: Boolean, locale: Locale): String {
    val style = if (short) TextStyle.SHORT else TextStyle.FULL
    return getDisplayName(style, JavaLocale.forLanguageTag(locale.toLanguageTag()))
}

actual fun DayOfWeek.getShortDisplayName(locale: Locale): String {
    return getDisplayName(TextStyle.SHORT, JavaLocale.forLanguageTag(locale.toLanguageTag()))
}
