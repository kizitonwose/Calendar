@file:UseSerializers(LocalDateSerializer::class)

package io.github.ceniorpomidor.workcalendar.domain.model

import io.github.ceniorpomidor.workcalendar.domain.util.LocalDateSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import java.time.DayOfWeek
import java.time.LocalDate

/** Days an alarm rings on. */
@Serializable
enum class AlarmRepeat {
    /** Days with a planned shift. */
    WORK_DAYS,

    /** Days without shifts: days off by the schedule, vacation, sick leave. */
    DAYS_OFF,

    /** Chosen days of the week, whatever the schedule. */
    WEEKDAYS,

    /** One date. */
    ONCE,
}

/** One alarm of the app. The app's alarms ring by themselves and do not touch the system clock app. */
@Serializable
data class AlarmItem(
    val id: Long = 0,
    val enabled: Boolean = true,
    val label: String = "",
    val repeat: AlarmRepeat = AlarmRepeat.WORK_DAYS,
    /** Working days: minutes before the start of the first shift of the day; null — at [minute]. */
    val minutesBefore: Int? = null,
    /** Time of day, minutes since midnight. */
    val minute: Int = DEFAULT_MINUTE,
    /** [AlarmRepeat.WEEKDAYS]: days of the week. */
    val weekdays: Set<DayOfWeek> = emptySet(),
    /** [AlarmRepeat.ONCE]: the date. */
    val date: LocalDate? = null,
    /** Working days: also days with extra shifts only. */
    val includeExtraShifts: Boolean = true,
    /** Days when the user switched this alarm off. */
    val skipDates: Set<LocalDate> = emptySet(),
) {
    /** Rings a set time before the shift rather than at a time of day. */
    val beforeShift: Boolean get() = repeat == AlarmRepeat.WORK_DAYS && minutesBefore != null

    companion object {
        const val DEFAULT_MINUTE = 6 * 60 + 30
        const val DEFAULT_MINUTES_BEFORE = 90

        /** The alarm can ring at most 12 hours before the shift. */
        const val MAX_MINUTES_BEFORE = 12 * 60
    }
}

/** How the time of the working day alarm was chosen in version 1.1.0. */
@Serializable
enum class AlarmTimeMode {
    BEFORE_SHIFT,
    FIXED_TIME,
}

/** Alarm of one day in version 1.1.0: replaced the working day alarm or turned it off (null minute). */
@Serializable
data class DayAlarm(
    val date: LocalDate,
    val minute: Int?,
)

@Serializable
data class AlarmSettings(
    val items: List<AlarmItem> = emptyList(),
    val snoozeMinutes: Int = 10,
    /** The alarm stops ringing by itself after this time, minutes. */
    val ringMinutes: Int = 10,
    // Version 1.1.0 had one alarm for working days plus changes for single days. These fields are
    // only read: [migrated] turns them into [items].
    val workDays: Boolean = false,
    val mode: AlarmTimeMode = AlarmTimeMode.BEFORE_SHIFT,
    val minutesBefore: Int = AlarmItem.DEFAULT_MINUTES_BEFORE,
    val fixedMinute: Int = AlarmItem.DEFAULT_MINUTE,
    val includeExtraShifts: Boolean = true,
    val days: List<DayAlarm> = emptyList(),
) {
    fun item(id: Long): AlarmItem? = items.firstOrNull { it.id == id }

    /** Adds the alarm (with a new id when it has none) or replaces the alarm with the same id. */
    fun save(item: AlarmItem): AlarmSettings {
        val saved = if (item.id == 0L) item.copy(id = (items.maxOfOrNull { it.id } ?: 0L) + 1) else item
        val list = if (items.any { it.id == saved.id }) items.map { if (it.id == saved.id) saved else it } else items + saved
        return copy(items = list)
    }

    fun remove(id: Long): AlarmSettings = copy(items = items.filter { it.id != id })

    fun setEnabled(id: Long, enabled: Boolean): AlarmSettings = copy(items = items.map { if (it.id == id) it.copy(enabled = enabled) else it })

    /** Switches an alarm off for one day, or back on. */
    fun skip(id: Long, date: LocalDate, skipped: Boolean): AlarmSettings = copy(
        items = items.map { if (it.id != id) it else it.copy(skipDates = if (skipped) it.skipDates + date else it.skipDates - date) },
    )

    /** Drops one-time alarms and switched off days that are in the past. */
    fun cleaned(today: LocalDate): AlarmSettings {
        val limit = today.minusDays(1)
        return copy(
            items = items
                .filter { it.repeat != AlarmRepeat.ONCE || it.date?.isBefore(limit) != true }
                .map { item -> item.copy(skipDates = item.skipDates.filterTo(LinkedHashSet()) { !it.isBefore(limit) }) },
        )
    }

    /** Moves the alarm of version 1.1.0 to [items]. */
    fun migrated(): AlarmSettings {
        if (!workDays && days.isEmpty()) return this
        var result = copy(workDays = false, days = emptyList())
        if (workDays) {
            // A single day with its own time or without an alarm: the working day alarm skips it.
            result = result.save(
                AlarmItem(
                    repeat = AlarmRepeat.WORK_DAYS,
                    minutesBefore = minutesBefore.takeIf { mode == AlarmTimeMode.BEFORE_SHIFT },
                    minute = fixedMinute,
                    includeExtraShifts = includeExtraShifts,
                    skipDates = days.mapTo(LinkedHashSet()) { it.date },
                ),
            )
        }
        for (day in days) {
            val minute = day.minute ?: continue
            result = result.save(AlarmItem(repeat = AlarmRepeat.ONCE, minute = minute, date = day.date))
        }
        return result
    }
}
