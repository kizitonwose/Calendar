package io.github.ceniorpomidor.workcalendar.domain.export

import io.github.ceniorpomidor.workcalendar.domain.model.Absence
import io.github.ceniorpomidor.workcalendar.domain.model.AbsenceType
import io.github.ceniorpomidor.workcalendar.domain.model.AccrualType
import io.github.ceniorpomidor.workcalendar.domain.model.ManualAccrual
import io.github.ceniorpomidor.workcalendar.domain.model.Payment
import io.github.ceniorpomidor.workcalendar.domain.model.PaymentType
import io.github.ceniorpomidor.workcalendar.domain.model.Shift
import io.github.ceniorpomidor.workcalendar.domain.model.ShiftKind
import io.github.ceniorpomidor.workcalendar.domain.model.ShiftStatus
import io.github.ceniorpomidor.workcalendar.domain.pay.PayCalculator
import io.github.ceniorpomidor.workcalendar.domain.util.Formats
import java.time.LocalDateTime

/** Minimal CSV builder compatible with Excel in Russian locale (`;` separator, UTF-8 BOM). */
class CsvWriter(private val separator: Char = ';') {
    private val sb = StringBuilder()

    fun row(vararg cells: Any?): CsvWriter {
        sb.append(cells.joinToString(separator.toString()) { escape(it?.toString() ?: "") })
        sb.append("\r\n")
        return this
    }

    fun row(cells: List<Any?>): CsvWriter = row(*cells.toTypedArray())

    private fun escape(value: String): String {
        val needsQuotes = value.any { it == separator || it == '"' || it == '\n' || it == '\r' } || value.startsWith(" ") || value.endsWith(" ")
        return if (needsQuotes) "\"" + value.replace("\"", "\"\"") + "\"" else value
    }

    /** Content with a UTF-8 BOM so that Excel detects the encoding. */
    fun build(): String = "\uFEFF" + sb.toString()
}

object CsvExport {
    fun shifts(shifts: List<Shift>, pay: PayCalculator): String {
        val w = CsvWriter()
        w.row(
            "Дата",
            "День недели",
            "Начало (план)",
            "Окончание (план)",
            "Перерыв, мин",
            "Часы (план)",
            "Статус",
            "Тип",
            "Начало (факт)",
            "Окончание (факт)",
            "Часы (факт)",
            "Ставка, руб/ч",
            "Начислено, руб",
            "Праздничные, ч",
            "Сверхурочные, ч",
            "Название",
            "Заметка",
        )
        for (s in shifts.filter { it.isVisible }.sortedWith(compareBy<Shift> { it.date }.thenBy { it.plannedStart })) {
            val snapshot = s.pay ?: if (s.status == ShiftStatus.CONFIRMED) pay.calculate(s, s.workedMinutes ?: 0) else null
            val rate = snapshot?.hourlyRate ?: pay.hourlyRateFor(s)
            w.row(
                Formats.date(s.date),
                Formats.weekdayShort(s.date.dayOfWeek),
                Formats.dateTime(s.plannedStart),
                Formats.dateTime(s.plannedEnd),
                s.plannedBreakMinutes,
                Formats.hoursDecimal(s.plannedPaidMinutes(pay.zone)),
                statusName(s.status),
                if (s.kind == ShiftKind.EXTRA) "Дополнительная" else "По графику",
                s.actualStart?.let { Formats.dateTime(it) },
                s.actualEnd?.let { Formats.dateTime(it) },
                s.workedMinutes?.let { Formats.hoursDecimal(it.toLong()) },
                rate?.let { Formats.moneyPlain(it) },
                snapshot?.let { if (s.status == ShiftStatus.CONFIRMED) Formats.moneyPlain(it.total) else null },
                snapshot?.let { Formats.hoursDecimal(it.holidayMinutes.toLong()) },
                snapshot?.let { Formats.hoursDecimal(it.overtimeMinutes.toLong()) },
                s.title,
                s.note,
            )
        }
        return w.build()
    }

    fun absences(absences: List<Absence>): String {
        val w = CsvWriter()
        w.row("Тип", "Название", "Начало", "Окончание", "Дней", "Оплачиваемый", "Сумма, руб", "Способ расчёта", "Заметка")
        for (a in absences.sortedBy { it.startDate }) {
            w.row(
                absenceTypeName(a.type),
                a.displayTitle(),
                Formats.date(a.startDate),
                Formats.date(a.endDate),
                a.calendarDays,
                if (a.paid) "да" else "нет",
                a.amount?.let { Formats.moneyPlain(it) },
                when {
                    a.manualAmount != null -> "вручную"
                    a.calculation != null -> "автоматически"
                    else -> ""
                },
                a.note,
            )
        }
        return w.build()
    }

    fun payments(payments: List<Payment>): String {
        val w = CsvWriter()
        w.row("Дата получения", "Тип", "Сумма, руб", "Ожидалось, руб", "Период с", "Период по", "Комментарий")
        for (p in payments.sortedBy { it.date }) {
            w.row(
                Formats.date(p.date),
                paymentTypeName(p.type),
                Formats.moneyPlain(p.amount),
                p.expectedAmount?.let { Formats.moneyPlain(it) },
                p.periodStart?.let { Formats.date(it) },
                p.periodEnd?.let { Formats.date(it) },
                p.note,
            )
        }
        return w.build()
    }

    fun accruals(accruals: List<ManualAccrual>): String {
        val w = CsvWriter()
        w.row("Дата", "Тип", "Название", "Сумма, руб", "Комментарий")
        for (a in accruals.sortedBy { it.date }) {
            w.row(Formats.date(a.date), accrualTypeName(a.type), a.title, Formats.moneyPlain(a.signedAmount), a.note)
        }
        return w.build()
    }

    fun statusName(status: ShiftStatus): String = when (status) {
        ShiftStatus.PLANNED -> "Запланирована"
        ShiftStatus.CONFIRMED -> "Часы подтверждены"
        ShiftStatus.MISSED -> "Не состоялась"
        ShiftStatus.CANCELLED -> "Отменена"
        ShiftStatus.MOVED -> "Перенесена"
        ShiftStatus.COVERED -> "Отсутствие"
        ShiftStatus.DELETED -> "Удалена"
    }

    fun absenceTypeName(type: AbsenceType): String = Absence.defaultTitle(type)

    fun paymentTypeName(type: PaymentType): String = when (type) {
        PaymentType.ADVANCE -> "Аванс"
        PaymentType.SALARY -> "Зарплата"
        PaymentType.VACATION_PAY -> "Отпускные"
        PaymentType.SICK_PAY -> "Больничный"
        PaymentType.BONUS -> "Премия"
        PaymentType.OTHER -> "Другое"
    }

    fun accrualTypeName(type: AccrualType): String = when (type) {
        AccrualType.BONUS -> "Премия"
        AccrualType.ALLOWANCE -> "Надбавка"
        AccrualType.COMPENSATION -> "Компенсация"
        AccrualType.DEDUCTION -> "Удержание"
        AccrualType.TAX -> "Налог"
        AccrualType.OTHER -> "Другое"
    }
}

/** iCalendar (.ics) export of shifts for importing into other calendars. */
object IcsExport {
    fun shifts(shifts: List<Shift>, generatedAt: LocalDateTime, title: String = "Работа"): String {
        val lines = ArrayList<String>()
        lines += "BEGIN:VCALENDAR"
        lines += "VERSION:2.0"
        lines += "PRODID:-//WorkCalendar//RU"
        lines += "CALSCALE:GREGORIAN"
        lines += "X-WR-CALNAME:${escape(title)}"
        val stamp = format(generatedAt)
        for (s in shifts.filter { it.status == ShiftStatus.PLANNED || it.status == ShiftStatus.CONFIRMED }.sortedBy { it.plannedStart }) {
            val start = s.actualStart?.takeIf { s.status == ShiftStatus.CONFIRMED } ?: s.plannedStart
            val end = s.actualEnd?.takeIf { s.status == ShiftStatus.CONFIRMED } ?: s.plannedEnd
            val summary = buildString {
                append(if (s.kind == ShiftKind.EXTRA) "Доп. смена" else "Смена")
                if (s.title.isNotBlank()) append(" · ").append(s.title)
            }
            lines += "BEGIN:VEVENT"
            lines += "UID:shift-${s.id}-${s.date}@workcalendar"
            lines += "DTSTAMP:$stamp"
            lines += "DTSTART:${format(start)}"
            lines += "DTEND:${format(end)}"
            lines += "SUMMARY:${escape(summary)}"
            if (s.note.isNotBlank()) lines += "DESCRIPTION:${escape(s.note)}"
            lines += "END:VEVENT"
        }
        lines += "END:VCALENDAR"
        return lines.joinToString("\r\n") { fold(it) } + "\r\n"
    }

    private fun format(dt: LocalDateTime): String =
        "%04d%02d%02dT%02d%02d%02d".format(dt.year, dt.monthValue, dt.dayOfMonth, dt.hour, dt.minute, dt.second)

    private fun escape(text: String): String = text
        .replace("\\", "\\\\")
        .replace(";", "\\;")
        .replace(",", "\\,")
        .replace("\r\n", "\\n")
        .replace("\n", "\\n")

    /** Folds lines longer than 75 octets (RFC 5545). */
    private fun fold(line: String): String {
        val bytes = line.toByteArray(Charsets.UTF_8)
        if (bytes.size <= 75) return line
        val sb = StringBuilder()
        var current = 0
        var limit = 75
        for (ch in line) {
            val size = ch.toString().toByteArray(Charsets.UTF_8).size
            if (current + size > limit) {
                sb.append("\r\n ")
                current = 0
                limit = 74
            }
            sb.append(ch)
            current += size
        }
        return sb.toString()
    }
}
