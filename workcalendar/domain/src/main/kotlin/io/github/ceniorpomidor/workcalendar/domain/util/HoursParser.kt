package io.github.ceniorpomidor.workcalendar.domain.util

/**
 * Parses worked time entered by the user (e.g. from a notification reply):
 * `8`, `7,5`, `7.5`, `7:30`, `7ч30м`, `7 ч 30 мин`, `450 мин`, `7h30m`.
 * Returns minutes or null when the input is not understood or out of range.
 */
object HoursParser {
    const val MAX_MINUTES: Int = 48 * 60

    private val clock = Regex("""^(\d{1,2})[:.](\d{2})$""")
    private val decimal = Regex("""^(\d{1,2})(?:[.,](\d{1,2}))?\s*(?:ч|час|часа|часов|h|hr|hrs)?\.?$""")
    private val hoursAndMinutes = Regex("""^(\d{1,2})\s*(?:ч|час|часа|часов|h)\.?\s*(\d{1,2})\s*(?:м|мин|минут|минуты|m|min)?\.?$""")
    private val minutesOnly = Regex("""^(\d{1,4})\s*(?:м|мин|минут|минуты|m|min)\.?$""")

    fun parse(input: String): Int? {
        val text = input.trim().lowercase().replace('\u00A0', ' ')
        if (text.isEmpty()) return null
        val minutes = parseInternal(text) ?: return null
        return minutes.takeIf { it in 0..MAX_MINUTES }
    }

    private fun parseInternal(text: String): Int? {
        // "7:30" is a clock-like value; "7.30" is ambiguous, treat as decimal hours below unless 2 digits after dot >= 60.
        clock.matchEntire(text)?.let { m ->
            val h = m.groupValues[1].toInt()
            val min = m.groupValues[2].toInt()
            if (text.contains(':')) {
                return if (min < 60) h * 60 + min else null
            }
        }
        hoursAndMinutes.matchEntire(text)?.let { m ->
            val h = m.groupValues[1].toInt()
            val min = m.groupValues[2].toInt()
            return if (min < 60) h * 60 + min else null
        }
        minutesOnly.matchEntire(text)?.let { m ->
            return m.groupValues[1].toInt()
        }
        decimal.matchEntire(text)?.let { m ->
            val h = m.groupValues[1].toInt()
            val fracText = m.groupValues[2]
            val fracMinutes = if (fracText.isEmpty()) {
                0
            } else {
                // "7,5" -> 0.5 h; "7,25" -> 0.25 h
                val numerator = fracText.toInt()
                val denominator = if (fracText.length == 1) 10 else 100
                Math.round(numerator * 60.0 / denominator).toInt()
            }
            return h * 60 + fracMinutes
        }
        return null
    }
}
