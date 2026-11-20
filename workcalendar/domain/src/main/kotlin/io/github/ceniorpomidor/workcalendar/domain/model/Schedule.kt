@file:UseSerializers(LocalDateSerializer::class, LocalDateTimeSerializer::class)

package io.github.ceniorpomidor.workcalendar.domain.model

import io.github.ceniorpomidor.workcalendar.domain.time.TimeMath
import io.github.ceniorpomidor.workcalendar.domain.util.LocalDateSerializer
import io.github.ceniorpomidor.workcalendar.domain.util.LocalDateTimeSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

/** Working time of one day in a schedule pattern. */
@Serializable
data class ShiftSpec(
    /** Start, minutes since midnight. */
    val startMinute: Int,
    /** Gross duration including the break, minutes (up to 48 hours). */
    val durationMinutes: Int,
    /** Unpaid break, minutes. */
    val breakMinutes: Int = 0,
    val title: String = "",
) {
    init {
        require(startMinute in 0 until TimeMath.MINUTES_PER_DAY) { "startMinute out of range: $startMinute" }
        require(durationMinutes in 1..MAX_DURATION_MINUTES) { "durationMinutes out of range: $durationMinutes" }
        require(breakMinutes in 0 until durationMinutes) { "breakMinutes out of range: $breakMinutes" }
    }

    val endMinuteOfDay: Int get() = (startMinute + durationMinutes) % TimeMath.MINUTES_PER_DAY

    /** The shift ends on a later calendar day than it starts. */
    val crossesMidnight: Boolean get() = startMinute + durationMinutes > TimeMath.MINUTES_PER_DAY

    val paidMinutes: Int get() = durationMinutes - breakMinutes

    fun startOn(date: LocalDate): LocalDateTime = TimeMath.atMinute(date, startMinute)

    fun endOn(date: LocalDate): LocalDateTime = startOn(date).plusMinutes(durationMinutes.toLong())

    companion object {
        const val MAX_DURATION_MINUTES: Int = 48 * 60

        /** Creates a spec from start and end minutes of day; end <= start means the next day. */
        fun fromTimes(startMinute: Int, endMinute: Int, breakMinutes: Int = 0, title: String = ""): ShiftSpec {
            var duration = endMinute - startMinute
            if (duration <= 0) duration += TimeMath.MINUTES_PER_DAY
            return ShiftSpec(startMinute, duration, breakMinutes, title)
        }
    }
}

/** Repeating schedule. */
@Serializable
sealed interface SchedulePattern {
    /** Do not create shifts on public holidays. */
    val skipHolidays: Boolean

    /** Working time for [date] or null for a day off (holidays are handled by the generator). */
    fun specFor(date: LocalDate): ShiftSpec?

    /** Fixed days of week, possibly with different times. */
    @Serializable
    @SerialName("weekly")
    data class Weekly(
        val days: Map<DayOfWeek, ShiftSpec>,
        override val skipHolidays: Boolean = false,
    ) : SchedulePattern {
        override fun specFor(date: LocalDate): ShiftSpec? = days[date.dayOfWeek]
    }

    /**
     * Repeating cycle such as 2/2, 3/3, "24 hours on, 3 days off". [days] lists the cycle,
     * a null element is a day off; [anchorDate] is the date of the first element.
     */
    @Serializable
    @SerialName("cycle")
    data class Cycle(
        val days: List<ShiftSpec?>,
        val anchorDate: LocalDate,
        override val skipHolidays: Boolean = false,
    ) : SchedulePattern {
        init {
            require(days.isNotEmpty()) { "Cycle must contain at least one day" }
        }

        override fun specFor(date: LocalDate): ShiftSpec? {
            val offset = ChronoUnit.DAYS.between(anchorDate, date)
            val index = Math.floorMod(offset, days.size.toLong()).toInt()
            return days[index]
        }

        val workDays: Int get() = days.count { it != null }

        val offDays: Int get() = days.count { it == null }
    }
}

/** User-defined schedule preset. Changing it does not change the calendar until it is applied. */
@Serializable
data class ScheduleTemplate(
    val id: Long = 0,
    val name: String,
    val pattern: SchedulePattern,
    val colorIndex: Int = 0,
    val archived: Boolean = false,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null,
)

/**
 * A template applied to the calendar for a period. Keeps its own copy of the pattern so that
 * later edits of the template never change dates generated before.
 */
@Serializable
data class ScheduleAssignment(
    val id: Long = 0,
    val templateId: Long? = null,
    val templateName: String,
    val pattern: SchedulePattern,
    val startDate: LocalDate,
    /** Inclusive end, null means open-ended. */
    val endDate: LocalDate? = null,
    val createdAt: LocalDateTime? = null,
) {
    init {
        require(endDate == null || !endDate.isBefore(startDate)) { "Assignment end $endDate is before start $startDate" }
    }

    fun covers(date: LocalDate): Boolean = !date.isBefore(startDate) && (endDate == null || !date.isAfter(endDate))
}
