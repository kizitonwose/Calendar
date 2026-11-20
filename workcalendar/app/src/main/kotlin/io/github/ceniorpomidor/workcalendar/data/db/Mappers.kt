package io.github.ceniorpomidor.workcalendar.data.db

import io.github.ceniorpomidor.workcalendar.domain.model.Absence
import io.github.ceniorpomidor.workcalendar.domain.model.ChangeLogEntry
import io.github.ceniorpomidor.workcalendar.domain.model.DayNote
import io.github.ceniorpomidor.workcalendar.domain.model.ExternalEarning
import io.github.ceniorpomidor.workcalendar.domain.model.HolidayOverride
import io.github.ceniorpomidor.workcalendar.domain.model.ManualAccrual
import io.github.ceniorpomidor.workcalendar.domain.model.Money
import io.github.ceniorpomidor.workcalendar.domain.model.Payment
import io.github.ceniorpomidor.workcalendar.domain.model.PayoutRule
import io.github.ceniorpomidor.workcalendar.domain.model.RatePeriod
import io.github.ceniorpomidor.workcalendar.domain.model.ScheduleAssignment
import io.github.ceniorpomidor.workcalendar.domain.model.ScheduleTemplate
import io.github.ceniorpomidor.workcalendar.domain.model.Shift

fun ShiftEntity.toDomain(): Shift = Shift(
    id = id,
    date = date,
    plannedStart = plannedStart,
    plannedEnd = plannedEnd,
    plannedBreakMinutes = plannedBreakMinutes,
    kind = kind,
    origin = origin,
    status = status,
    userModified = userModified,
    assignmentId = assignmentId,
    title = title,
    note = note,
    actualStart = actualStart,
    actualEnd = actualEnd,
    actualBreakMinutes = actualBreakMinutes,
    workedMinutes = workedMinutes,
    hourlyRateOverride = hourlyRateOverride?.let { Money(it) },
    pay = pay,
    absenceId = absenceId,
    movedToDate = movedToDate,
    movedFromShiftId = movedFromShiftId,
    movedFromDate = movedFromDate,
    confirmedAt = confirmedAt,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

fun Shift.toEntity(): ShiftEntity = ShiftEntity(
    id = id,
    date = date,
    plannedStart = plannedStart,
    plannedEnd = plannedEnd,
    plannedBreakMinutes = plannedBreakMinutes,
    kind = kind,
    origin = origin,
    status = status,
    userModified = userModified,
    templateKey = templateKey,
    assignmentId = assignmentId,
    title = title,
    note = note,
    actualStart = actualStart,
    actualEnd = actualEnd,
    actualBreakMinutes = actualBreakMinutes,
    workedMinutes = workedMinutes,
    hourlyRateOverride = hourlyRateOverride?.kopecks,
    pay = pay,
    payTotal = pay?.total?.kopecks,
    absenceId = absenceId,
    movedToDate = movedToDate,
    movedFromShiftId = movedFromShiftId,
    movedFromDate = movedFromDate,
    confirmedAt = confirmedAt,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

fun TemplateEntity.toDomain(): ScheduleTemplate = ScheduleTemplate(id, name, pattern, colorIndex, archived, createdAt, updatedAt)

fun ScheduleTemplate.toEntity(): TemplateEntity = TemplateEntity(id, name, pattern, colorIndex, archived, createdAt, updatedAt)

fun AssignmentEntity.toDomain(): ScheduleAssignment = ScheduleAssignment(id, templateId, templateName, pattern, startDate, endDate, createdAt)

fun ScheduleAssignment.toEntity(): AssignmentEntity = AssignmentEntity(id, templateId, templateName, pattern, startDate, endDate, createdAt)

fun RateEntity.toDomain(): RatePeriod = RatePeriod(
    id = id,
    effectiveFrom = effectiveFrom,
    hourlyRate = Money(hourlyRate),
    holidayBonusPercent = holidayBonusPercent,
    overtimeBonusPercent = overtimeBonusPercent,
    extraShiftBonusPercent = extraShiftBonusPercent,
    note = note,
)

fun RatePeriod.toEntity(): RateEntity = RateEntity(
    id = id,
    effectiveFrom = effectiveFrom,
    hourlyRate = hourlyRate.kopecks,
    holidayBonusPercent = holidayBonusPercent,
    overtimeBonusPercent = overtimeBonusPercent,
    extraShiftBonusPercent = extraShiftBonusPercent,
    note = note,
)

fun AbsenceEntity.toDomain(): Absence = Absence(
    id = id,
    type = type,
    title = title,
    startDate = startDate,
    endDate = endDate,
    paid = paid,
    manualAmount = manualAmount?.let { Money(it) },
    calculation = calculation,
    paymentDate = paymentDate,
    employerPaymentDate = employerPaymentDate,
    note = note,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

fun Absence.toEntity(): AbsenceEntity = AbsenceEntity(
    id = id,
    type = type,
    title = title,
    startDate = startDate,
    endDate = endDate,
    paid = paid,
    manualAmount = manualAmount?.kopecks,
    calculation = calculation,
    paymentDate = paymentDate,
    employerPaymentDate = employerPaymentDate,
    note = note,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

fun PayoutRuleEntity.toDomain(): PayoutRule = PayoutRule(
    id = id,
    name = name,
    kind = kind,
    enabled = enabled,
    payDay = payDay.coerceIn(1, 31),
    payMonthOffset = payMonthOffset.coerceIn(0, 2),
    periodStartDay = periodStartDay.coerceIn(1, 31),
    periodEndDay = periodEndDay.coerceIn(periodStartDay.coerceIn(1, 31), 31),
    amountMode = amountMode,
    fixedAmount = Money(fixedAmount),
    percent = percent,
    formula = formula,
    weekendShift = weekendShift,
    notify = notify,
    notifyDaysBefore = notifyDaysBefore.coerceIn(0, 30),
    sortOrder = sortOrder,
)

fun PayoutRule.toEntity(): PayoutRuleEntity = PayoutRuleEntity(
    id = id,
    name = name,
    kind = kind,
    enabled = enabled,
    payDay = payDay,
    payMonthOffset = payMonthOffset,
    periodStartDay = periodStartDay,
    periodEndDay = periodEndDay,
    amountMode = amountMode,
    fixedAmount = fixedAmount.kopecks,
    percent = percent,
    formula = formula,
    weekendShift = weekendShift,
    notify = notify,
    notifyDaysBefore = notifyDaysBefore,
    sortOrder = sortOrder,
)

fun PaymentEntity.toDomain(): Payment = Payment(
    id = id,
    type = type,
    amount = Money(amount),
    date = date,
    payoutKey = payoutKey,
    periodStart = periodStart,
    periodEnd = periodEnd,
    expectedAmount = expectedAmount?.let { Money(it) },
    absenceId = absenceId,
    note = note,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

fun Payment.toEntity(): PaymentEntity = PaymentEntity(
    id = id,
    type = type,
    amount = amount.kopecks,
    date = date,
    payoutKey = payoutKey,
    periodStart = periodStart,
    periodEnd = periodEnd,
    expectedAmount = expectedAmount?.kopecks,
    absenceId = absenceId,
    note = note,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

fun AccrualEntity.toDomain(): ManualAccrual = ManualAccrual(id, date, type, title, Money(amount), note, createdAt, updatedAt)

fun ManualAccrual.toEntity(): AccrualEntity = AccrualEntity(id, date, type, title, amount.kopecks, note, createdAt, updatedAt)

fun ExternalEarningEntity.toDomain(): ExternalEarning = ExternalEarning(id, year, month, Money(amount), note)

fun ExternalEarning.toEntity(): ExternalEarningEntity = ExternalEarningEntity(id, year, month, amount.kopecks, note)

fun DayNoteEntity.toDomain(): DayNote = DayNote(date, text, updatedAt)

fun DayNote.toEntity(): DayNoteEntity = DayNoteEntity(date, text, updatedAt)

fun HolidayOverrideEntity.toDomain(): HolidayOverride = HolidayOverride(date, type, title)

fun HolidayOverride.toEntity(): HolidayOverrideEntity = HolidayOverrideEntity(date, type, title)

fun ChangeLogEntity.toDomain(): ChangeLogEntry = ChangeLogEntry(id, timestamp, category, action, description, entityId)

fun ChangeLogEntry.toEntity(): ChangeLogEntity = ChangeLogEntity(id, timestamp, category, action, description, entityId)
