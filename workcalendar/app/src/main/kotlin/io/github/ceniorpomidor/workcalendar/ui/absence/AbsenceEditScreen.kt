package io.github.ceniorpomidor.workcalendar.ui.absence

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.ceniorpomidor.workcalendar.AppContainer
import io.github.ceniorpomidor.workcalendar.data.repo.AbsenceCheck
import io.github.ceniorpomidor.workcalendar.domain.absence.AbsencePaymentDates
import io.github.ceniorpomidor.workcalendar.domain.model.Absence
import io.github.ceniorpomidor.workcalendar.domain.model.AbsencePayCalculation
import io.github.ceniorpomidor.workcalendar.domain.model.AbsenceType
import io.github.ceniorpomidor.workcalendar.domain.model.Money
import io.github.ceniorpomidor.workcalendar.domain.model.Payment
import io.github.ceniorpomidor.workcalendar.domain.model.SickPayMethod
import io.github.ceniorpomidor.workcalendar.domain.model.VacationPayMethod
import io.github.ceniorpomidor.workcalendar.domain.notify.AbsencePayment
import io.github.ceniorpomidor.workcalendar.domain.util.Formats
import io.github.ceniorpomidor.workcalendar.ui.components.Banner
import io.github.ceniorpomidor.workcalendar.ui.components.BannerKind
import io.github.ceniorpomidor.workcalendar.ui.components.ConfirmDialog
import io.github.ceniorpomidor.workcalendar.ui.components.DateField
import io.github.ceniorpomidor.workcalendar.ui.components.DateRangePickerDialog
import io.github.ceniorpomidor.workcalendar.ui.components.LocalSnackbar
import io.github.ceniorpomidor.workcalendar.ui.components.NumberField
import io.github.ceniorpomidor.workcalendar.ui.components.ScrollColumn
import io.github.ceniorpomidor.workcalendar.ui.components.SectionCard
import io.github.ceniorpomidor.workcalendar.ui.components.SubScreen
import io.github.ceniorpomidor.workcalendar.ui.components.SwitchRow
import io.github.ceniorpomidor.workcalendar.ui.components.TextInput
import io.github.ceniorpomidor.workcalendar.ui.components.ValueRow
import io.github.ceniorpomidor.workcalendar.ui.components.launchSafely
import io.github.ceniorpomidor.workcalendar.ui.components.money
import io.github.ceniorpomidor.workcalendar.ui.theme.AppIcons
import kotlinx.coroutines.flow.map
import java.time.LocalDate

private fun typeTitle(type: AbsenceType): String = when (type) {
    AbsenceType.VACATION -> "Отпуск"
    AbsenceType.SICK -> "Больничный"
    AbsenceType.UNPAID -> "Без оплаты"
    AbsenceType.OTHER -> "Другое"
}

@Composable
fun AbsenceEditScreen(container: AppContainer, absenceId: Long?, type: AbsenceType, start: LocalDate, onDone: () -> Unit) {
    val snackbar = LocalSnackbar.current
    val scope = rememberCoroutineScope()
    val calc by container.calc.context.collectAsStateWithLifecycle(initialValue = null)
    var existing by remember { mutableStateOf<Absence?>(null) }
    var loaded by rememberSaveable { mutableStateOf(absenceId == null) }

    var absenceType by rememberSaveable { mutableStateOf(type) }
    var startDate by rememberSaveable { mutableStateOf(start) }
    var endDate by rememberSaveable { mutableStateOf(if (type == AbsenceType.VACATION) start.plusDays(13) else start) }
    var paid by rememberSaveable { mutableStateOf(type != AbsenceType.UNPAID) }
    var manual by rememberSaveable { mutableStateOf(false) }
    var amountText by rememberSaveable { mutableStateOf("") }
    var title by rememberSaveable { mutableStateOf("") }
    var note by rememberSaveable { mutableStateOf("") }
    var keepShifts by rememberSaveable { mutableStateOf(true) }
    var paymentDate by rememberSaveable { mutableStateOf<LocalDate?>(null) }
    var daysText by rememberSaveable { mutableStateOf("") }
    var pickRange by remember { mutableStateOf(false) }
    var askDelete by remember { mutableStateOf(false) }
    var preview by remember { mutableStateOf<AbsencePayCalculation?>(null) }
    var check by remember { mutableStateOf<AbsenceCheck?>(null) }
    var receive by remember { mutableStateOf<AbsencePayment?>(null) }

    LaunchedEffect(absenceId) {
        if (absenceId != null && !loaded) {
            val a = container.absences.get(absenceId)
            existing = a
            if (a != null) {
                absenceType = a.type
                startDate = a.startDate
                endDate = a.endDate
                paid = a.paid
                manual = a.manualAmount != null
                amountText = a.manualAmount?.toString()?.replace('.', ',') ?: ""
                title = a.title
                note = a.note
                paymentDate = a.paymentDate
            }
            loaded = true
        } else if (absenceId != null) {
            existing = container.absences.get(absenceId)
        }
    }

    val manualAmount = if (manual) Money.parse(amountText) else null
    val current = Absence(
        id = existing?.id ?: 0,
        type = absenceType,
        title = title.trim(),
        startDate = startDate,
        endDate = if (endDate < startDate) startDate else endDate,
        paid = paid && absenceType != AbsenceType.UNPAID,
        manualAmount = manualAmount,
        calculation = existing?.calculation,
        paymentDate = paymentDate,
        employerPaymentDate = existing?.employerPaymentDate,
        note = note.trim(),
        createdAt = existing?.createdAt,
    )

    // Live check of overlaps, affected shifts and the automatic amount.
    LaunchedEffect(current.type, current.startDate, current.endDate, current.paid, calc, loaded) {
        if (!loaded) return@LaunchedEffect
        try {
            container.schedule.ensureGenerated(current.range)
            check = container.absences.check(current)
            preview = if (current.paid) container.absences.calculate(current) else null
        } catch (e: Exception) {
            preview = null
        }
    }

    val payments by remember(absenceId) {
        container.finance.payments.map { list -> list.filter { absenceId != null && it.absenceId == absenceId } }
    }.collectAsStateWithLifecycle(initialValue = emptyList<Payment>())

    val methodEnabled = when (absenceType) {
        AbsenceType.VACATION -> calc?.settings?.absence?.vacationMethod != VacationPayMethod.NONE
        AbsenceType.SICK -> calc?.settings?.absence?.sickMethod != SickPayMethod.NONE
        else -> false
    }

    SubScreen(
        title = if (existing == null) "Новое отсутствие" else existing!!.displayTitle(),
        onBack = onDone,
        actions = {
            if (existing != null) IconButton(onClick = { askDelete = true }) { Icon(AppIcons.Delete, contentDescription = "Удалить") }
        },
    ) { padding ->
        ScrollColumn(padding) {
            SectionCard(title = "Вид отсутствия") {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    AbsenceType.entries.forEach { t ->
                        FilterChip(
                            selected = absenceType == t,
                            onClick = {
                                absenceType = t
                                if (t == AbsenceType.UNPAID) {
                                    paid = false
                                } else if (absenceType != AbsenceType.OTHER) {
                                    paid = true
                                }
                            },
                            label = { Text(typeTitle(t)) },
                        )
                    }
                }
                if (absenceType == AbsenceType.OTHER || absenceType == AbsenceType.UNPAID) {
                    TextInput("Название (например, «Учебный отпуск», «Донорский день»)", title, { title = it })
                }
            }
            SectionCard(title = "Период", icon = AppIcons.DateRange) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DateField("С", startDate, {
                        startDate = it
                        if (endDate < it) endDate = it
                    }, modifier = Modifier.weight(1f))
                    DateField("По", endDate, { endDate = if (it < startDate) startDate else it }, modifier = Modifier.weight(1f))
                }
                TextButton(onClick = { pickRange = true }) { Text("Выбрать диапазон в календаре") }
                if (absenceType == AbsenceType.VACATION) {
                    NumberField(
                        "Количество дней отпуска",
                        daysText,
                        { text ->
                            daysText = text.filter { it.isDigit() }
                            val days = daysText.toIntOrNull()
                            if (days != null && days in 1..365 && calc != null) {
                                endDate = endForDays(startDate, days, calc!!.settings.absence.excludeHolidaysFromVacation) { calc!!.holidays.isPublicHoliday(it) }
                            }
                        },
                        decimal = false,
                        supportingText = "Праздники внутри отпуска не входят в число дней и продлевают отпуск",
                    )
                }
                val holidaysInside = calc?.let { c -> current.range.count { c.holidays.isPublicHoliday(it) } } ?: 0
                ValueRow("Календарных дней", current.calendarDays.toString(), emphasize = true)
                if (absenceType == AbsenceType.VACATION && holidaysInside > 0) {
                    ValueRow("Из них праздничных (не оплачиваются как отпуск)", holidaysInside.toString())
                }
            }

            check?.let { c ->
                c.overlap?.let { Banner("Пересекается: ${it.displayTitle()} ${Formats.period(it.startDate, it.endDate)}. Измените даты.", BannerKind.ERROR) }
                if (c.coverage.toCover.isNotEmpty()) {
                    SectionCard {
                        Text(
                            "На эти даты по графику ${Formats.shifts(c.coverage.toCover.size)}. Они не войдут в учёт часов и заработка.",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        SwitchRow("Сохранить смены в истории", "Смены останутся на датах как «заменены отсутствием»", keepShifts) { keepShifts = it }
                    }
                }
                if (c.coverage.conflicts.isNotEmpty()) {
                    Banner(
                        "На даты ${c.coverage.conflicts.joinToString { Formats.shortDate(it.date) }} уже подтверждены часы. Они не будут изменены — проверьте период, чтобы избежать двойного учёта.",
                        BannerKind.WARNING,
                    )
                }
            }

            if (absenceType != AbsenceType.UNPAID) {
                SectionCard(title = "Оплата", icon = AppIcons.Payments) {
                    SwitchRow("Оплачиваемый", null, paid) { paid = it }
                    if (paid) {
                        SwitchRow(
                            "Указать сумму вручную",
                            if (methodEnabled) "Иначе сумма рассчитывается автоматически" else "Автоматический расчёт выключен в настройках",
                            manual || !methodEnabled,
                            enabled = methodEnabled,
                        ) { manual = it }
                        if (manual || !methodEnabled) {
                            NumberField(
                                if (absenceType == AbsenceType.SICK) "Сумма пособия" else "Сумма выплаты",
                                amountText,
                                { amountText = it },
                                suffix = "₽",
                                isError = amountText.isNotBlank() && manualAmount == null,
                            )
                        } else {
                            CalculationView(preview)
                        }
                        val c = calc
                        if (c != null) {
                            when (absenceType) {
                                AbsenceType.VACATION -> {
                                    val auto = AbsencePaymentDates.vacationPayDate(current.copy(paymentDate = null), c.holidays, c.settings.absence.vacationNotifyBusinessDaysBefore)
                                    DateField(
                                        "Дата выплаты отпускных",
                                        paymentDate ?: auto,
                                        { paymentDate = it },
                                        supportingText = if (paymentDate ==
                                            null
                                        ) {
                                            "Ожидается за ${c.settings.absence.vacationNotifyBusinessDaysBefore} раб. дня до отпуска — напоминание придёт в этот день"
                                        } else {
                                            null
                                        },
                                    )
                                }
                                AbsenceType.SICK -> {
                                    val fund = AbsencePaymentDates.sickFundDate(current.copy(paymentDate = null), c.holidays, c.settings.absence.fundPaymentBusinessDays)
                                    DateField("Выплата от Соцфонда (примерно)", paymentDate ?: fund, {
                                        paymentDate = it
                                    }, supportingText = "Часть работодателя — в ближайший день выплаты зарплаты после закрытия больничного")
                                }
                                else -> DateField("Дата выплаты (если ожидается)", paymentDate, { paymentDate = it })
                            }
                        }
                    }
                }
            }

            if (existing != null && paid) {
                SectionCard(title = "Полученные выплаты", icon = AppIcons.Savings) {
                    if (payments.isEmpty()) Text("Пока не отмечены", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    payments.forEach { p -> ValueRow(Formats.date(p.date), money(p.amount)) }
                    TextButton(onClick = {
                        scope.launchSafely(snackbar) {
                            val c = calc ?: return@launchSafely
                            val items = container.absences.payments(listOf(existing!!), c, emptyList())
                            receive = items.firstOrNull { item -> payments.none { it.payoutKey == item.key } } ?: items.firstOrNull()
                        }
                    }) { Text("Отметить получение") }
                }
            }

            SectionCard(title = "Заметка") {
                TextInput("Комментарий", note, { note = it }, singleLine = false, minLines = 2)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(onClick = onDone, modifier = Modifier.weight(1f)) { Text("Отмена") }
                Button(
                    onClick = {
                        scope.launchSafely(snackbar, success = "Сохранено", onSuccess = onDone) {
                            container.absences.save(current, keepShifts)
                        }
                    },
                    enabled = check?.overlap == null && (!manual || manualAmount != null) && loaded &&
                        (absenceType != AbsenceType.OTHER || title.isNotBlank()),
                    modifier = Modifier.weight(1f),
                ) { Text("Сохранить") }
            }
            if (existing != null && existing!!.manualAmount == null && existing!!.paid) {
                TextButton(onClick = {
                    scope.launchSafely(snackbar, success = "Пересчитано") { existing = container.absences.recalculate(existing!!.id) }
                }) { Text("Пересчитать сумму по текущим данным") }
            }
        }
    }

    if (pickRange) {
        DateRangePickerDialog(startDate, endDate, "Период отсутствия", onDismiss = { pickRange = false }) { s, e ->
            startDate = s
            endDate = e
            pickRange = false
        }
    }
    if (askDelete && existing != null) {
        ConfirmDialog(
            title = "Удалить ${existing!!.displayTitle().lowercase()}?",
            text = "Смены по графику на эти даты вернутся в расписание. История смен не удаляется.",
            confirmText = "Удалить",
            destructive = true,
            onConfirm = { scope.launchSafely(snackbar, success = "Удалено", onSuccess = onDone) { container.absences.delete(existing!!) } },
            onDismiss = { askDelete = false },
        )
    }
    receive?.let { item ->
        var amount by remember(item) { mutableStateOf(item.amount?.toString()?.replace('.', ',') ?: "") }
        var date by remember(item) { mutableStateOf(container.clock.today()) }
        AlertDialog(
            onDismissRequest = { receive = null },
            title = { Text("Выплата получена") },
            text = {
                androidx.compose.foundation.layout.Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    NumberField("Сумма", amount, { amount = it }, suffix = "₽")
                    DateField("Дата получения", date, { date = it })
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val value = Money.parse(amount)
                    if (value != null) {
                        scope.launchSafely(snackbar, success = "Выплата отмечена") { container.finance.markAbsencePaymentReceived(item, value, date, "") }
                        receive = null
                    }
                }) { Text("Сохранить") }
            },
            dismissButton = { TextButton(onClick = { receive = null }) { Text("Отмена") } },
        )
    }
}

/** End date for [days] vacation days starting at [start]; holidays may extend the vacation. */
private fun endForDays(start: LocalDate, days: Int, skipHolidays: Boolean, isHoliday: (LocalDate) -> Boolean): LocalDate {
    var counted = 0
    var date = start
    while (true) {
        if (!skipHolidays || !isHoliday(date)) counted++
        if (counted >= days) return date
        date = date.plusDays(1)
    }
}

@Composable
private fun CalculationView(calculation: AbsencePayCalculation?) {
    if (calculation == null) {
        Text("Расчёт…", style = MaterialTheme.typography.bodyMedium)
        return
    }
    ValueRow("Расчётная сумма", money(calculation.total), emphasize = true)
    calculation.employerPart?.let { if (it.isPositive) ValueRow("Работодатель", money(it)) }
    calculation.fundPart?.let { if (it.isPositive && calculation.employerPart?.isPositive == true) ValueRow("Соцфонд", money(it)) }
    calculation.explanation.forEach { Text("• $it", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
    calculation.warnings.forEach { Banner(it, BannerKind.WARNING) }
    Text(
        "Расчёт ориентировочный и выполняется по правилам из настроек (Настройки → Отпуск и больничный). При необходимости укажите сумму вручную.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    if (calculation.insufficientData) {
        Banner("Данных недостаточно — сумма может быть неточной.", BannerKind.WARNING)
    }
}
