package io.github.ceniorpomidor.workcalendar.domain

import io.github.ceniorpomidor.workcalendar.domain.TestData.date
import io.github.ceniorpomidor.workcalendar.domain.TestData.dateTime
import io.github.ceniorpomidor.workcalendar.domain.model.HolidayOverride
import io.github.ceniorpomidor.workcalendar.domain.model.HolidayOverrideType
import io.github.ceniorpomidor.workcalendar.domain.time.DateRange
import io.github.ceniorpomidor.workcalendar.domain.time.HolidayCalendar
import io.github.ceniorpomidor.workcalendar.domain.time.TimeMath
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.ZoneId

class TimeAndHolidaysTest {
    @Test
    fun `elapsed time respects daylight saving changes`() {
        val berlin = ZoneId.of("Europe/Berlin")
        // Night of 29.03.2026 clocks go forward: 20:00–08:00 lasts 11 real hours.
        assertEquals(11 * 60L, TimeMath.elapsedMinutes(dateTime("2026-03-28T20:00"), dateTime("2026-03-29T08:00"), berlin))
        // Night of 25.10.2026 clocks go back: 13 real hours.
        assertEquals(13 * 60L, TimeMath.elapsedMinutes(dateTime("2026-10-24T20:00"), dateTime("2026-10-25T08:00"), berlin))
        // Moscow has no DST.
        assertEquals(12 * 60L, TimeMath.elapsedMinutes(dateTime("2026-10-24T20:00"), dateTime("2026-10-25T08:00"), TestData.zone))
    }

    @Test
    fun `russian holidays and transfers`() {
        val cal = HolidayCalendar()
        assertTrue(cal.isPublicHoliday(date("2026-01-07")))
        assertTrue(cal.isPublicHoliday(date("2026-11-04")))
        assertFalse(cal.isPublicHoliday(date("2026-11-05")))
        // 2025 decree: 02.05 and 08.05 are days off, 01.11 (Saturday) is a working day.
        assertTrue(cal.isNonWorkingDay(date("2025-05-02")))
        assertTrue(cal.isNonWorkingDay(date("2025-05-08")))
        assertTrue(cal.isBusinessDay(date("2025-11-01")))
        assertTrue(cal.isNonWorkingDay(date("2025-11-03")))
        // Automatic rule for a year without built-in decree: 2027-05-09 is Sunday -> 10.05 off.
        assertTrue(cal.isNonWorkingDay(date("2027-05-10")))
        // User overrides.
        val custom = HolidayCalendar(listOf(HolidayOverride(date("2026-10-07"), HolidayOverrideType.DAY_OFF, "Корпоратив")))
        assertTrue(custom.isNonWorkingDay(date("2026-10-07")))
        assertEquals("Корпоратив", custom.holidayName(date("2026-10-07")))
    }

    @Test
    fun `production calendar 2027`() {
        val cal = HolidayCalendar()
        // Decree No. 1187 of 17.09.2026: 247 working days in a five-day week.
        assertEquals(247, DateRange.year(2027).count { cal.isBusinessDay(it) })
        for (day in listOf("2027-01-08", "2027-02-22", "2027-02-23", "2027-05-03", "2027-05-10", "2027-06-14", "2027-11-05", "2027-12-31")) {
            assertTrue(cal.isNonWorkingDay(date(day)), day)
        }
        assertTrue(cal.isBusinessDay(date("2027-02-20")))
        assertTrue(cal.isWorkingWeekend(date("2027-02-20")))
        assertTrue(cal.isBusinessDay(date("2027-01-11")))
        assertEquals("Выходной (перенос)", cal.holidayName(date("2027-11-05")))
    }

    @Test
    fun `business day arithmetic skips weekends and holidays`() {
        val cal = HolidayCalendar()
        // Monday 09.11.2026 minus 2 business days = Thursday 05.11 (04.11 is a holiday).
        assertEquals(date("2026-11-05"), cal.minusBusinessDays(date("2026-11-09"), 2))
        assertEquals(date("2026-10-09"), cal.businessDayOnOrBefore(date("2026-10-10")))
        assertEquals(date("2026-10-12"), cal.plusBusinessDays(date("2026-10-09"), 1))
    }
}
