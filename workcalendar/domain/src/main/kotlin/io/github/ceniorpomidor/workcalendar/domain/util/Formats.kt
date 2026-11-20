package io.github.ceniorpomidor.workcalendar.domain.util

import io.github.ceniorpomidor.workcalendar.domain.model.Money
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.Month
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import kotlin.math.abs

/** Locale-independent Russian formatting used across the app, notifications and exports. */
object Formats {
    private const val NBSP = '\u00A0'

    private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    private val shortDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM")
    private val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    private val monthsNominative = listOf(
        "Январь",
        "Февраль",
        "Март",
        "Апрель",
        "Май",
        "Июнь",
        "Июль",
        "Август",
        "Сентябрь",
        "Октябрь",
        "Ноябрь",
        "Декабрь",
    )
    private val monthsGenitive = listOf(
        "января",
        "февраля",
        "марта",
        "апреля",
        "мая",
        "июня",
        "июля",
        "августа",
        "сентября",
        "октября",
        "ноября",
        "декабря",
    )
    private val monthsShort = listOf(
        "янв",
        "фев",
        "мар",
        "апр",
        "май",
        "июн",
        "июл",
        "авг",
        "сен",
        "окт",
        "ноя",
        "дек",
    )
    private val weekdaysShort = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")
    private val weekdaysFull = listOf("понедельник", "вторник", "среда", "четверг", "пятница", "суббота", "воскресенье")

    /** `12 345,67 ₽`; kopecks are omitted for whole amounts when [alwaysKopecks] is false. */
    fun money(money: Money, currency: String = "₽", alwaysKopecks: Boolean = false): String {
        val negative = money.kopecks < 0
        val absolute = abs(money.kopecks)
        val rubles = absolute / 100
        val kopecks = absolute % 100
        val grouped = groupThousands(rubles)
        val body = if (kopecks != 0L || alwaysKopecks) "$grouped,${kopecks.toString().padStart(2, '0')}" else grouped
        val sign = if (negative) "−" else ""
        return if (currency.isEmpty()) "$sign$body" else "$sign$body$NBSP$currency"
    }

    /** Plain decimal for CSV export: `12345,67` (Excel with Russian locale expects comma). */
    fun moneyPlain(money: Money): String {
        val negative = money.kopecks < 0
        val absolute = abs(money.kopecks)
        val body = "${absolute / 100},${(absolute % 100).toString().padStart(2, '0')}"
        return if (negative) "-$body" else body
    }

    private fun groupThousands(value: Long): String {
        val digits = value.toString()
        val sb = StringBuilder()
        digits.forEachIndexed { index, c ->
            if (index > 0 && (digits.length - index) % 3 == 0) sb.append(NBSP)
            sb.append(c)
        }
        return sb.toString()
    }

    /** `7,5`, `8`, `7,25` — hours with up to two decimals. */
    fun hoursDecimal(minutes: Long): String {
        val negative = minutes < 0
        val m = abs(minutes)
        val hundredths = Money.divRound(m * 100, 60)
        val whole = hundredths / 100
        val frac = hundredths % 100
        val body = when {
            frac == 0L -> whole.toString()
            frac % 10 == 0L -> "$whole,${frac / 10}"
            else -> "$whole,${frac.toString().padStart(2, '0')}"
        }
        return if (negative) "-$body" else body
    }

    /** `7,5 ч` */
    fun hours(minutes: Long): String = "${hoursDecimal(minutes)}${NBSP}ч"

    /** `7 ч 30 мин`, `8 ч`, `45 мин`. */
    fun hoursMinutes(minutes: Long): String {
        val m = abs(minutes)
        val h = m / 60
        val rest = m % 60
        val sign = if (minutes < 0) "−" else ""
        return when {
            h == 0L -> "$sign$rest${NBSP}мин"
            rest == 0L -> "$sign$h${NBSP}ч"
            else -> "$sign$h${NBSP}ч $rest${NBSP}мин"
        }
    }

    /** `7:30` */
    fun hoursClock(minutes: Long): String {
        val m = abs(minutes)
        return "${if (minutes < 0) "-" else ""}${m / 60}:${(m % 60).toString().padStart(2, '0')}"
    }

    fun time(time: LocalTime): String = time.format(timeFormatter)

    fun time(dateTime: LocalDateTime): String = dateTime.format(timeFormatter)

    fun date(date: LocalDate): String = date.format(dateFormatter)

    fun shortDate(date: LocalDate): String = date.format(shortDateFormatter)

    fun dateTime(dateTime: LocalDateTime): String = "${date(dateTime.toLocalDate())} ${time(dateTime)}"

    /** `4 октября 2026` */
    fun longDate(date: LocalDate, withYear: Boolean = true): String {
        val base = "${date.dayOfMonth} ${monthsGenitive[date.monthValue - 1]}"
        return if (withYear) "$base ${date.year}" else base
    }

    /** `суббота, 4 октября` */
    fun dayTitle(date: LocalDate, withYear: Boolean = false): String = "${weekdaysFull[date.dayOfWeek.value - 1]}, ${longDate(date, withYear)}"

    /** `Октябрь 2026` */
    fun monthTitle(month: YearMonth): String = "${monthsNominative[month.monthValue - 1]} ${month.year}"

    fun monthName(month: Month): String = monthsNominative[month.value - 1]

    fun monthShort(month: Month): String = monthsShort[month.value - 1]

    fun weekdayShort(day: DayOfWeek): String = weekdaysShort[day.value - 1]

    fun weekdayFull(day: DayOfWeek): String = weekdaysFull[day.value - 1]

    /** `09:00–18:00` with `(+1)` when the end is on the next day. */
    fun timeRange(start: LocalDateTime, end: LocalDateTime): String {
        val days = java.time.temporal.ChronoUnit.DAYS.between(start.toLocalDate(), end.toLocalDate())
        val suffix = if (days > 0) " (+$days)" else ""
        return "${time(start)}–${time(end)}$suffix"
    }

    /** `01.10–15.10.2026` or `28.12.2026–10.01.2027`. */
    fun period(start: LocalDate, end: LocalDate): String = when {
        start == end -> date(start)
        start.year == end.year -> "${shortDate(start)}–${date(end)}"
        else -> "${date(start)}–${date(end)}"
    }

    /** Russian plural form: 1 смена, 2 смены, 5 смен. */
    fun plural(count: Long, one: String, few: String, many: String): String {
        val n = abs(count) % 100
        val n1 = n % 10
        val word = when {
            n in 11..19 -> many
            n1 == 1L -> one
            n1 in 2..4 -> few
            else -> many
        }
        return "$count $word"
    }

    fun shifts(count: Int): String = plural(count.toLong(), "смена", "смены", "смен")

    fun days(count: Int): String = plural(count.toLong(), "день", "дня", "дней")
}
