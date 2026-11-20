package io.github.ceniorpomidor.workcalendar.domain.alarm

import io.github.ceniorpomidor.workcalendar.domain.model.AlarmItem
import io.github.ceniorpomidor.workcalendar.domain.model.AlarmRepeat
import io.github.ceniorpomidor.workcalendar.domain.model.AlarmSettings
import io.github.ceniorpomidor.workcalendar.domain.model.Shift
import io.github.ceniorpomidor.workcalendar.domain.model.ShiftKind
import io.github.ceniorpomidor.workcalendar.domain.model.ShiftStatus
import io.github.ceniorpomidor.workcalendar.domain.time.TimeMath
import io.github.ceniorpomidor.workcalendar.domain.util.Formats
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime

/** One ring of an alarm. */
data class PlannedAlarm(
    val at: LocalDateTime,
    /** Day the ring belongs to: for an alarm before a shift, the date of the shift. */
    val date: LocalDate,
    val alarm: AlarmItem,
    /** First shift of the day, if any. */
    val shift: Shift?,
) {
    /** Identifies the ring, e.g. to remember that it has already rung. */
    val key: String get() = "${alarm.id}@$at"

    /** Text under the time: the name of the alarm and the shift it wakes up for. */
    fun description(): String {
        val shiftText = shift?.let { "смена ${Formats.timeRange(it.plannedStart, it.plannedEnd)}" }
        val text = listOfNotNull(alarm.label.trim().ifBlank { null }, shiftText).joinToString(" · ")
        return text.ifBlank { AlarmTexts.repeat(alarm) }.replaceFirstChar { it.uppercase() }
    }
}

/** An alarm in the day panel: [on] is false when it is switched off for this day. */
data class AlarmOfDay(val ring: PlannedAlarm, val on: Boolean)

/** Plans the rings of the app's alarms by the schedule of shifts. */
object AlarmPlanner {
    /** How many days ahead the next ring is looked for (covers vacations). */
    const val LOOKAHEAD_DAYS = 62

    /** First planned shift of the day. */
    fun workShift(date: LocalDate, shifts: List<Shift>, includeExtraShifts: Boolean = true): Shift? = shifts
        .filter { it.date == date && it.status == ShiftStatus.PLANNED && (includeExtraShifts || it.kind == ShiftKind.REGULAR) }
        .minByOrNull { it.plannedStart }

    /** When [item] rings for [date], whether it is switched on or not; null if it does not ring that day. */
    fun occurrence(item: AlarmItem, date: LocalDate, shifts: List<Shift>): PlannedAlarm? {
        val atTime = date.atTime(TimeMath.timeOfMinute(item.minute))
        return when (item.repeat) {
            AlarmRepeat.WORK_DAYS -> workShift(date, shifts, item.includeExtraShifts)?.let { shift ->
                val at = item.minutesBefore?.let { shift.plannedStart.minusMinutes(it.toLong()) } ?: atTime
                PlannedAlarm(at, date, item, shift)
            }
            AlarmRepeat.DAYS_OFF -> if (shifts.none { it.date == date && it.isActive }) PlannedAlarm(atTime, date, item, null) else null
            AlarmRepeat.WEEKDAYS -> if (date.dayOfWeek in item.weekdays) PlannedAlarm(atTime, date, item, workShift(date, shifts)) else null
            AlarmRepeat.ONCE -> if (date == item.date) PlannedAlarm(atTime, date, item, workShift(date, shifts)) else null
        }
    }

    /**
     * Alarms of [date] for the day panel, by time. Repeating alarms that are switched off in the
     * list are not shown; a one-time alarm is shown switched off.
     */
    fun forDay(settings: AlarmSettings, date: LocalDate, shifts: List<Shift>): List<AlarmOfDay> = settings.items
        .filter { it.enabled || it.repeat == AlarmRepeat.ONCE }
        .mapNotNull { item -> occurrence(item, date, shifts)?.let { AlarmOfDay(it, item.enabled && date !in item.skipDates) } }
        .sortedBy { it.ring.at }

    /**
     * Rings after [now] for the next [days] days, by time. [shifts] must contain the shifts from
     * today to today + [days] + 1: an alarm before an early shift can ring the evening before.
     * [skipKey] is the ring that has just happened.
     */
    fun upcoming(settings: AlarmSettings, shifts: List<Shift>, now: LocalDateTime, days: Int = LOOKAHEAD_DAYS, skipKey: String? = null): List<PlannedAlarm> {
        val today = now.toLocalDate()
        val result = ArrayList<PlannedAlarm>()
        for (item in settings.items) {
            if (!item.enabled) continue
            for (i in 0..days + 1) {
                val date = today.plusDays(i.toLong())
                if (date in item.skipDates) continue
                val ring = occurrence(item, date, shifts) ?: continue
                if (ring.at.isAfter(now) && ring.key != skipKey) result += ring
            }
        }
        return result.sortedWith(compareBy<PlannedAlarm> { it.at }.thenBy { it.alarm.id })
    }
}

/** Texts about alarms for lists and settings. */
object AlarmTexts {
    /** `06:30` or `за 1 ч 30 мин до смены`. */
    fun time(item: AlarmItem): String {
        val before = item.minutesBefore
        return if (item.beforeShift && before != null) {
            "за ${Formats.hoursMinutes(before.toLong())} до смены"
        } else {
            Formats.time(TimeMath.timeOfMinute(item.minute))
        }
    }

    /** `Рабочие дни по графику`, `Пн, Ср, Пт`, `Пятница, 9 октября`… */
    fun repeat(item: AlarmItem): String = when (item.repeat) {
        AlarmRepeat.WORK_DAYS -> if (item.includeExtraShifts) "Рабочие дни по графику" else "Рабочие дни, кроме доп. смен"
        AlarmRepeat.DAYS_OFF -> "Выходные по графику"
        AlarmRepeat.WEEKDAYS -> weekdays(item.weekdays)
        AlarmRepeat.ONCE -> item.date?.let { Formats.dayTitle(it).replaceFirstChar { c -> c.uppercase() } } ?: "Один раз"
    }

    fun weekdays(days: Set<DayOfWeek>): String = when {
        days.isEmpty() -> "Дни не выбраны"
        days.size == 7 -> "Каждый день"
        days == DayOfWeek.entries.filter { it.value <= 5 }.toSet() -> "Пн–Пт"
        else -> days.sorted().joinToString(", ") { Formats.weekdayShort(it) }
    }

    /** One line about all alarms for the settings list. */
    fun summary(settings: AlarmSettings): String {
        val on = settings.items.count { it.enabled }
        return when {
            settings.items.isEmpty() -> "Не заведены"
            on == 0 -> "Все выключены"
            else -> "Включено: $on из ${settings.items.size}"
        }
    }
}
