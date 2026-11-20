package io.github.ceniorpomidor.workcalendar.domain

import io.github.ceniorpomidor.workcalendar.domain.TestData.date
import io.github.ceniorpomidor.workcalendar.domain.TestData.dateTime
import io.github.ceniorpomidor.workcalendar.domain.TestData.shift
import io.github.ceniorpomidor.workcalendar.domain.backup.BackupCodec
import io.github.ceniorpomidor.workcalendar.domain.backup.BackupException
import io.github.ceniorpomidor.workcalendar.domain.backup.BackupFile
import io.github.ceniorpomidor.workcalendar.domain.export.CsvExport
import io.github.ceniorpomidor.workcalendar.domain.export.CsvWriter
import io.github.ceniorpomidor.workcalendar.domain.export.IcsExport
import io.github.ceniorpomidor.workcalendar.domain.model.Absence
import io.github.ceniorpomidor.workcalendar.domain.model.AbsenceType
import io.github.ceniorpomidor.workcalendar.domain.model.AppSettings
import io.github.ceniorpomidor.workcalendar.domain.model.Money
import io.github.ceniorpomidor.workcalendar.domain.model.PayoutRule
import io.github.ceniorpomidor.workcalendar.domain.model.RatePeriod
import io.github.ceniorpomidor.workcalendar.domain.model.ScheduleTemplate
import io.github.ceniorpomidor.workcalendar.domain.model.ShiftStatus
import io.github.ceniorpomidor.workcalendar.domain.util.PinHasher
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class BackupAndExportTest {
    @Test
    fun `scenario 12 - backup round trip keeps all data`() {
        val backup = BackupFile(
            appVersion = "1.0",
            createdAt = "2026-10-04T12:00",
            settings = AppSettings(onboardingDone = true, employmentStartDate = date("2020-01-01")),
            templates = listOf(ScheduleTemplate(id = 1, name = "5/2", pattern = TestData.fiveTwo())),
            assignments = listOf(TestData.assignment(1, TestData.twoTwo(date("2026-10-01")), "2026-10-01")),
            shifts = listOf(
                shift(1, "2026-10-05", status = ShiftStatus.CONFIRMED, worked = 450).copy(pay = TestData.payCalculator().calculate(shift(1, "2026-10-05"), 450)),
                shift(2, "2026-10-06", start = "20:00", end = "08:00"),
            ),
            rates = listOf(RatePeriod(id = 1, effectiveFrom = date("2026-01-01"), hourlyRate = Money.ofRubles(300), holidayBonusPercent = 100)),
            absences = listOf(Absence(id = 1, type = AbsenceType.VACATION, startDate = date("2026-11-09"), endDate = date("2026-11-22"), manualAmount = Money.ofRubles(40_000))),
            payoutRules = listOf(PayoutRule.defaultAdvance().copy(id = 1), PayoutRule.defaultSalary().copy(id = 2)),
        )
        val text = BackupCodec.encode(backup)
        val restored = BackupCodec.decode(text)
        assertEquals(backup, restored)
    }

    @Test
    fun `invalid backups are rejected with a message`() {
        assertThrows<BackupException> { BackupCodec.decode("not json") }
        assertThrows<BackupException> { BackupCodec.decode("{\"format\":\"other\"}") }
        assertThrows<BackupException> { BackupCodec.decode("{\"format\":\"workcalendar-backup\",\"version\":99}") }
        val duplicates = BackupFile(shifts = listOf(shift(1, "2026-10-05"), shift(2, "2026-10-05")))
        assertThrows<BackupException> { BackupCodec.decode(BackupCodec.encode(duplicates)) }
    }

    @Test
    fun `csv escaping and bom`() {
        val csv = CsvWriter().row("a;b", "say \"hi\"", 5).build()
        assertTrue(csv.startsWith("\uFEFF"))
        assertEquals("\uFEFF\"a;b\";\"say \"\"hi\"\"\";5\r\n", csv)
        val shifts = CsvExport.shifts(listOf(shift(1, "2026-10-05", status = ShiftStatus.CONFIRMED, worked = 450)), TestData.payCalculator())
        assertTrue(shifts.contains("05.10.2026"))
        assertTrue(shifts.contains("2250,00"))
    }

    @Test
    fun `ics export`() {
        val ics = IcsExport.shifts(listOf(shift(1, "2026-10-05")), dateTime("2026-10-04T12:00"))
        assertTrue(ics.contains("DTSTART:20261005T090000"))
        assertTrue(ics.contains("DTEND:20261005T180000"))
        assertTrue(ics.startsWith("BEGIN:VCALENDAR"))
    }

    @Test
    fun `pin hashing`() {
        val salt = PinHasher.newSalt()
        val hash = PinHasher.hash("1234", salt)
        assertTrue(PinHasher.verify("1234", salt, hash))
        assertFalse(PinHasher.verify("4321", salt, hash))
        assertTrue(PinHasher.isValidPin("0000"))
        assertFalse(PinHasher.isValidPin("12a4"))
    }
}
