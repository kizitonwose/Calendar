package io.github.ceniorpomidor.workcalendar.domain

import io.github.ceniorpomidor.workcalendar.domain.TestData.date
import io.github.ceniorpomidor.workcalendar.domain.TestData.dateTime
import io.github.ceniorpomidor.workcalendar.domain.TestData.shift
import io.github.ceniorpomidor.workcalendar.domain.alarm.AlarmPlanner
import io.github.ceniorpomidor.workcalendar.domain.alarm.AlarmTexts
import io.github.ceniorpomidor.workcalendar.domain.model.AlarmItem
import io.github.ceniorpomidor.workcalendar.domain.model.AlarmRepeat
import io.github.ceniorpomidor.workcalendar.domain.model.AlarmSettings
import io.github.ceniorpomidor.workcalendar.domain.model.AppSettings
import io.github.ceniorpomidor.workcalendar.domain.model.ShiftKind
import io.github.ceniorpomidor.workcalendar.domain.model.ShiftOrigin
import io.github.ceniorpomidor.workcalendar.domain.model.ShiftStatus
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.DayOfWeek

class AlarmPlannerTest {
    private val beforeShift = AlarmItem(id = 1, repeat = AlarmRepeat.WORK_DAYS, minutesBefore = 90)

    // Mon 05.10 and Tue 06.10 08:00–20:00, Wed 07.10 off, Thu 08.10 extra shift 10:00–18:00.
    private val shifts = listOf(
        shift(1, "2026-10-05", start = "08:00", end = "20:00"),
        shift(2, "2026-10-06", start = "08:00", end = "20:00"),
        shift(3, "2026-10-08", start = "10:00", end = "18:00", kind = ShiftKind.EXTRA, origin = ShiftOrigin.MANUAL),
    )

    private fun settings(vararg items: AlarmItem) = AlarmSettings(items = items.toList())

    @Test
    fun `several alarms before every working day`() {
        val second = AlarmItem(id = 2, repeat = AlarmRepeat.WORK_DAYS, minute = 6 * 60 + 45)
        val rings = AlarmPlanner.upcoming(settings(beforeShift, second), shifts, dateTime("2026-10-05T05:00"), days = 7)
        assertEquals(
            listOf("2026-10-05T06:30", "2026-10-05T06:45", "2026-10-06T06:30", "2026-10-06T06:45", "2026-10-08T06:45", "2026-10-08T08:30").map { dateTime(it) },
            rings.map { it.at },
        )
        assertEquals(1L, rings.first().shift?.id)
        assertEquals("Смена 08:00–20:00", rings.first().description())
        assertEquals("Подъём · смена 08:00–20:00", rings.first().copy(alarm = beforeShift.copy(label = "Подъём")).description())
    }

    @Test
    fun `days off, days of the week and one date`() {
        val daysOff = AlarmItem(id = 1, repeat = AlarmRepeat.DAYS_OFF, minute = 9 * 60)
        val gym = AlarmItem(id = 2, repeat = AlarmRepeat.WEEKDAYS, minute = 19 * 60, weekdays = setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY))
        val once = AlarmItem(id = 3, repeat = AlarmRepeat.ONCE, minute = 5 * 60, date = date("2026-10-06"))
        // Up to Thu 08.10 (the extra shift): Fri 09.10 would be a day off too.
        val rings = AlarmPlanner.upcoming(settings(daysOff, gym, once), shifts, dateTime("2026-10-05T05:00"), days = 2)
        assertEquals(
            listOf("2026-10-05T19:00", "2026-10-06T05:00", "2026-10-07T09:00", "2026-10-07T19:00").map { dateTime(it) },
            rings.map { it.at },
        )
        assertEquals("Выходные по графику", rings[2].description())
        assertEquals("Пн, Ср", AlarmTexts.repeat(gym))
    }

    @Test
    fun `switched off alarms and days, the ring that has just happened and past rings are skipped`() {
        val off = beforeShift.copy(id = 2, enabled = false)
        val skipping = beforeShift.copy(skipDates = setOf(date("2026-10-06")))
        val now = dateTime("2026-10-05T06:30")
        val rings = AlarmPlanner.upcoming(settings(skipping, off), shifts, now, days = 7)
        assertEquals(listOf(dateTime("2026-10-08T08:30")), rings.map { it.at })
        val first = AlarmPlanner.upcoming(settings(beforeShift), shifts, now.minusMinutes(1), days = 7).first()
        assertEquals(dateTime("2026-10-06T06:30"), AlarmPlanner.upcoming(settings(beforeShift), shifts, now.minusSeconds(30), days = 7, skipKey = first.key).first().at)
    }

    @Test
    fun `no working day alarm for cancelled, moved, covered and confirmed shifts or without extra shifts`() {
        val list = listOf(
            shift(1, "2026-10-05", status = ShiftStatus.CANCELLED),
            shift(2, "2026-10-06", status = ShiftStatus.MOVED),
            shift(3, "2026-10-07", status = ShiftStatus.COVERED),
            shift(4, "2026-10-08", status = ShiftStatus.CONFIRMED, worked = 480),
        )
        assertTrue(AlarmPlanner.upcoming(settings(beforeShift), list, dateTime("2026-10-04T12:00"), days = 7).isEmpty())
        val noExtra = beforeShift.copy(includeExtraShifts = false)
        assertEquals(listOf(date("2026-10-05"), date("2026-10-06")), AlarmPlanner.upcoming(settings(noExtra), shifts, dateTime("2026-10-05T05:00"), days = 7).map { it.date })
    }

    @Test
    fun `alarm of an early shift rings the evening before`() {
        val night = listOf(shift(1, "2026-10-06", start = "00:30", end = "08:30", breakMinutes = 0))
        val rings = AlarmPlanner.upcoming(settings(beforeShift), night, dateTime("2026-10-05T12:00"), days = 0)
        assertEquals(listOf(dateTime("2026-10-05T23:00")), rings.map { it.at })
        assertEquals(date("2026-10-06"), rings.single().date)
    }

    @Test
    fun `day panel shows switched off rings of the day`() {
        val once = AlarmItem(id = 2, enabled = false, repeat = AlarmRepeat.ONCE, minute = 5 * 60, date = date("2026-10-05"))
        val hidden = AlarmItem(id = 3, enabled = false, repeat = AlarmRepeat.WORK_DAYS)
        val day = AlarmPlanner.forDay(settings(beforeShift.copy(skipDates = setOf(date("2026-10-05"))), once, hidden), date("2026-10-05"), shifts)
        assertEquals(listOf(2L, 1L), day.map { it.ring.alarm.id })
        assertTrue(day.none { it.on })
    }

    @Test
    fun `saving, switching off a day and cleaning up`() {
        val today = date("2026-10-07")
        var s = AlarmSettings().save(beforeShift.copy(id = 0)).save(AlarmItem(repeat = AlarmRepeat.ONCE, date = date("2026-10-01")))
        assertEquals(listOf(1L, 2L), s.items.map { it.id })
        s = s.skip(1, date("2026-10-02"), true).skip(1, date("2026-10-09"), true).skip(1, date("2026-10-09"), false).skip(1, date("2026-10-08"), true)
        s = s.cleaned(today)
        assertEquals(listOf(1L), s.items.map { it.id })
        assertEquals(setOf(date("2026-10-08")), s.items.single().skipDates)
        assertEquals("Включено: 1 из 1", AlarmTexts.summary(s))
        assertEquals("за 1 ч 30 мин до смены", AlarmTexts.time(s.items.single()).replace('\u00A0', ' '))
    }

    @Test
    fun `alarm of version 1_1_0 becomes the list of alarms`() {
        val json = Json { ignoreUnknownKeys = true }
        val old = """{"alarm":{"workDays":true,"mode":"FIXED_TIME","fixedMinute":400,"includeExtraShifts":false,""" +
            """"snoozeMinutes":5,"days":[{"date":"2026-10-06","minute":null},{"date":"2026-10-07","minute":345}]}}"""
        val alarm = json.decodeFromString(AppSettings.serializer(), old).alarm.migrated()
        assertFalse(alarm.workDays)
        assertTrue(alarm.days.isEmpty())
        assertEquals(5, alarm.snoozeMinutes)
        val (work, once) = alarm.items
        assertEquals(AlarmRepeat.WORK_DAYS, work.repeat)
        assertEquals(null, work.minutesBefore)
        assertEquals(400, work.minute)
        assertFalse(work.includeExtraShifts)
        assertEquals(setOf(date("2026-10-06"), date("2026-10-07")), work.skipDates)
        assertEquals(AlarmItem(id = 2, repeat = AlarmRepeat.ONCE, minute = 345, date = date("2026-10-07")), once)
        assertEquals(alarm, alarm.migrated())
        val before = json.decodeFromString(AppSettings.serializer(), """{"alarm":{"workDays":true}}""").alarm.migrated()
        assertEquals(90, before.items.single().minutesBefore)
    }
}
