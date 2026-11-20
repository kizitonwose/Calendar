package io.github.ceniorpomidor.workcalendar.domain.absence

import java.time.LocalDate
import java.time.Period

/** Insurance experience (страховой стаж) used for the sick benefit percent. */
object InsuranceExperience {
    /** Total experience in full months on [onDate]. */
    fun months(employmentStart: LocalDate?, priorMonths: Int, onDate: LocalDate): Int {
        val current = if (employmentStart != null && !employmentStart.isAfter(onDate)) {
            val p = Period.between(employmentStart, onDate)
            p.years * 12 + p.months
        } else {
            0
        }
        return (current + priorMonths).coerceAtLeast(0)
    }

    /** Benefit percent by experience (art. 7 of Federal law 255-FZ). */
    fun sickPercent(months: Int): Int = when {
        months < 5 * 12 -> 60
        months < 8 * 12 -> 80
        else -> 100
    }

    fun describe(months: Int): String {
        val years = months / 12
        val rest = months % 12
        val y = when {
            years % 100 in 11..19 -> "лет"
            years % 10 == 1 -> "год"
            years % 10 in 2..4 -> "года"
            else -> "лет"
        }
        return when {
            years == 0 -> "$rest мес."
            rest == 0 -> "$years $y"
            else -> "$years $y $rest мес."
        }
    }
}
