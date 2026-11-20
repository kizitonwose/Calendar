package io.github.ceniorpomidor.workcalendar.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.ceniorpomidor.workcalendar.AppContainer
import io.github.ceniorpomidor.workcalendar.domain.model.AppSettings
import io.github.ceniorpomidor.workcalendar.domain.model.ExternalEarning
import io.github.ceniorpomidor.workcalendar.domain.model.Money
import io.github.ceniorpomidor.workcalendar.domain.model.PayoutAmountMode
import io.github.ceniorpomidor.workcalendar.domain.model.PayoutKind
import io.github.ceniorpomidor.workcalendar.domain.model.PayoutRule
import io.github.ceniorpomidor.workcalendar.domain.model.RatePeriod
import io.github.ceniorpomidor.workcalendar.domain.model.SickPayMethod
import io.github.ceniorpomidor.workcalendar.domain.model.VacationPayMethod
import io.github.ceniorpomidor.workcalendar.domain.model.WeekendShift
import io.github.ceniorpomidor.workcalendar.domain.pay.Formula
import io.github.ceniorpomidor.workcalendar.domain.time.DateRange
import io.github.ceniorpomidor.workcalendar.domain.util.Formats
import io.github.ceniorpomidor.workcalendar.ui.components.ConfirmDialog
import io.github.ceniorpomidor.workcalendar.ui.components.DateField
import io.github.ceniorpomidor.workcalendar.ui.components.EmptyState
import io.github.ceniorpomidor.workcalendar.ui.components.LocalSnackbar
import io.github.ceniorpomidor.workcalendar.ui.components.NumberField
import io.github.ceniorpomidor.workcalendar.ui.components.ScrollColumn
import io.github.ceniorpomidor.workcalendar.ui.components.SectionCard
import io.github.ceniorpomidor.workcalendar.ui.components.StepperField
import io.github.ceniorpomidor.workcalendar.ui.components.SubScreen
import io.github.ceniorpomidor.workcalendar.ui.components.SwitchRow
import io.github.ceniorpomidor.workcalendar.ui.components.TextInput
import io.github.ceniorpomidor.workcalendar.ui.components.ThinDivider
import io.github.ceniorpomidor.workcalendar.ui.components.ValueRow
import io.github.ceniorpomidor.workcalendar.ui.components.launchSafely
import io.github.ceniorpomidor.workcalendar.ui.components.money
import io.github.ceniorpomidor.workcalendar.ui.theme.AppIcons
import java.time.LocalDate

@Composable
fun RatesScreen(container: AppContainer, onBack: () -> Unit) {
    val rates by container.finance.rates.collectAsStateWithLifecycle(initialValue = emptyList())
    var editing by remember { mutableStateOf<RatePeriod?>(null) }
    var recalc by remember { mutableStateOf<Triple<LocalDate, Int, Money>?>(null) }
    var deleting by remember { mutableStateOf<RatePeriod?>(null) }
    val snackbar = LocalSnackbar.current
    val scope = rememberCoroutineScope()
    val today = container.clock.today()
    SubScreen(
        title = "Ставки и надбавки",
        onBack = onBack,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    editing = (rates.lastOrNull() ?: RatePeriod(effectiveFrom = today, hourlyRate = Money.ZERO)).copy(id = 0, effectiveFrom = today)
                },
                icon = { Icon(AppIcons.Add, contentDescription = null) },
                text = { Text("Новая ставка") },
            )
        },
    ) { padding ->
        ScrollColumn(padding) {
            Text(
                "Ставка действует с указанной даты до начала следующей. Подтверждённые смены сохраняют ставку на момент подтверждения — пересчёт выполняется только по вашему выбору.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (rates.isEmpty()) EmptyState(AppIcons.Ruble, "Ставка не задана", "Без ставки заработок не рассчитывается")
            rates.sortedByDescending { it.effectiveFrom }.forEach { r ->
                SectionCard(
                    modifier = Modifier.clickable { editing = r },
                    title = "${money(r.hourlyRate)} в час",
                    icon = AppIcons.Ruble,
                    action = { IconButton(onClick = { deleting = r }) { Icon(AppIcons.Delete, contentDescription = "Удалить") } },
                ) {
                    Text("Действует с ${Formats.date(r.effectiveFrom)}", style = MaterialTheme.typography.bodyMedium)
                    val parts = listOfNotNull(
                        r.holidayBonusPercent.takeIf { it > 0 }?.let { "праздничные +$it%" },
                        r.overtimeBonusPercent.takeIf { it > 0 }?.let { "сверхурочные +$it%" },
                        r.extraShiftBonusPercent.takeIf { it > 0 }?.let { "доп. смены +$it%" },
                    )
                    Text(if (parts.isEmpty()) "Без надбавок" else parts.joinToString(", "), style = MaterialTheme.typography.bodySmall)
                    if (r.note.isNotBlank()) Text(r.note, style = MaterialTheme.typography.bodySmall)
                }
            }
            Spacer(Modifier.padding(bottom = 72.dp))
        }
    }
    editing?.let { rate ->
        RateDialog(rate, onDismiss = { editing = null }) { saved ->
            scope.launchSafely(snackbar, success = "Ставка сохранена") {
                container.finance.saveRate(saved)
                editing = null
                // Offer recalculation when the rate applies to the past and confirmed shifts exist.
                if (saved.effectiveFrom <= today) {
                    val (count, diff) = container.shifts.previewRecalculation(DateRange(saved.effectiveFrom, today))
                    if (count > 0) recalc = Triple(saved.effectiveFrom, count, diff)
                }
            }
        }
    }
    recalc?.let { (from, count, diff) ->
        AlertDialog(
            onDismissRequest = { recalc = null },
            title = { Text("Пересчитать прошлые смены?") },
            text = {
                Text(
                    "С ${Formats.date(from)} есть ${Formats.shifts(count)} с подтверждёнными часами по прежней ставке. " +
                        "Пересчёт изменит начисления на ${money(diff)}. Если не пересчитывать, они сохранят прежнюю ставку.",
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    scope.launchSafely(snackbar, success = "Смены пересчитаны") { container.shifts.recalculatePay(DateRange(from, today)) }
                    recalc = null
                }) { Text("Пересчитать") }
            },
            dismissButton = { TextButton(onClick = { recalc = null }) { Text("Оставить как есть") } },
        )
    }
    deleting?.let { r ->
        ConfirmDialog(
            title = "Удалить ставку?",
            text = "Ставка ${money(r.hourlyRate)} с ${Formats.date(r.effectiveFrom)} будет удалена. Подтверждённые смены сохранят свои суммы.",
            confirmText = "Удалить",
            destructive = true,
            onConfirm = { scope.launchSafely(snackbar, success = "Удалено") { container.finance.deleteRate(r) } },
            onDismiss = { deleting = null },
        )
    }
}

@Composable
private fun RateDialog(rate: RatePeriod, onDismiss: () -> Unit, onSave: (RatePeriod) -> Unit) {
    var date by remember { mutableStateOf(rate.effectiveFrom) }
    var rateText by remember { mutableStateOf(if (rate.hourlyRate.isZero) "" else rate.hourlyRate.toString().replace('.', ',')) }
    var holiday by remember { mutableStateOf(rate.holidayBonusPercent.toString()) }
    var overtime by remember { mutableStateOf(rate.overtimeBonusPercent.toString()) }
    var extra by remember { mutableStateOf(rate.extraShiftBonusPercent.toString()) }
    var note by remember { mutableStateOf(rate.note) }
    val parsed = Money.parse(rateText)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (rate.id == 0L) "Новая ставка" else "Ставка") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                DateField("Действует с", date, { date = it })
                NumberField("Ставка в час", rateText, { rateText = it }, suffix = "₽", isError = rateText.isNotBlank() && parsed == null)
                Text("Надбавки, % к часовой ставке (по ТК РФ: праздник — не менее 100%, сверхурочные — 50–100%)", style = MaterialTheme.typography.bodySmall)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NumberField("Праздничные", holiday, { holiday = it.filter { c -> c.isDigit() } }, suffix = "%", decimal = false, modifier = Modifier.weight(1f))
                    NumberField("Сверхурочные", overtime, { overtime = it.filter { c -> c.isDigit() } }, suffix = "%", decimal = false, modifier = Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NumberField("Доп. смены", extra, { extra = it.filter { c -> c.isDigit() } }, suffix = "%", decimal = false, modifier = Modifier.weight(1f))
                    Spacer(Modifier.weight(1f))
                }
                TextInput("Комментарий", note, { note = it })
            }
        },
        confirmButton = {
            TextButton(
                enabled = parsed != null && parsed.isPositive,
                onClick = {
                    onSave(
                        rate.copy(
                            effectiveFrom = date,
                            hourlyRate = parsed ?: Money.ZERO,
                            holidayBonusPercent = holiday.toIntOrNull()?.coerceIn(0, 1000) ?: 0,
                            overtimeBonusPercent = overtime.toIntOrNull()?.coerceIn(0, 1000) ?: 0,
                            extraShiftBonusPercent = extra.toIntOrNull()?.coerceIn(0, 1000) ?: 0,
                            note = note.trim(),
                        ),
                    )
                },
            ) { Text("Сохранить") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } },
    )
}

private fun PayoutRule.describe(): String {
    val month = when (payMonthOffset) {
        0 -> "того же месяца"
        1 -> "следующего месяца"
        else -> "через $payMonthOffset мес."
    }
    val amount = when (amountMode) {
        PayoutAmountMode.FIXED -> "фиксированная сумма"
        PayoutAmountMode.PERCENT_OF_PERIOD -> "$percent% заработка за $periodStartDay–${if (periodEndDay >= 31) "конец" else periodEndDay.toString()}"
        PayoutAmountMode.MONTH_REMAINDER -> "остаток за месяц"
        PayoutAmountMode.FORMULA -> "по формуле"
    }
    return "$payDay число $month · $amount"
}

@Composable
fun PayoutRulesScreen(container: AppContainer, onBack: () -> Unit, onEdit: (Long) -> Unit) {
    val rules by container.finance.rules.collectAsStateWithLifecycle(initialValue = emptyList())
    SubScreen(
        title = "Аванс и зарплата",
        onBack = onBack,
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = { onEdit(0) }, icon = { Icon(AppIcons.Add, contentDescription = null) }, text = { Text("Выплата") })
        },
    ) { padding ->
        ScrollColumn(padding) {
            Text(
                "По этим правилам приложение рассчитывает ожидаемые суммы и напоминает о выплатах. Полученный аванс учитывается при расчёте остатка зарплаты.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            rules.forEach { r ->
                SectionCard(modifier = Modifier.clickable { onEdit(r.id) }, title = r.name, icon = AppIcons.Wallet) {
                    Text(r.describe(), style = MaterialTheme.typography.bodyMedium)
                    Text(
                        listOfNotNull(
                            if (!r.enabled) "выключено" else null,
                            if (r.notify) "напоминание за ${Formats.days(r.notifyDaysBefore)}" else "без напоминания",
                        ).joinToString(" · "),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Spacer(Modifier.padding(bottom = 72.dp))
        }
    }
}

@Composable
fun PayoutRuleEditScreen(container: AppContainer, ruleId: Long?, onBack: () -> Unit) {
    val snackbar = LocalSnackbar.current
    val scope = rememberCoroutineScope()
    var rule by remember { mutableStateOf<PayoutRule?>(if (ruleId == null) PayoutRule.defaultAdvance().copy(name = "Выплата", kind = PayoutKind.OTHER, sortOrder = 5) else null) }
    var fixedText by rememberSaveable { mutableStateOf("") }
    var askDelete by remember { mutableStateOf(false) }
    LaunchedEffect(ruleId) {
        if (ruleId != null) {
            val r = container.finance.getRule(ruleId)
            rule = r
            fixedText = r?.fixedAmount?.takeIf { it.isPositive }?.toString()?.replace('.', ',') ?: ""
        }
    }
    val r = rule
    SubScreen(
        title = r?.name ?: "Выплата",
        onBack = onBack,
        actions = { if (ruleId != null) IconButton(onClick = { askDelete = true }) { Icon(AppIcons.Delete, contentDescription = "Удалить") } },
    ) { padding ->
        ScrollColumn(padding) {
            if (r == null) {
                Text("Загрузка…")
                return@ScrollColumn
            }
            fun set(transform: (PayoutRule) -> PayoutRule) {
                rule = runCatching { transform(r) }.getOrDefault(r)
            }
            SectionCard {
                TextInput("Название", r.name, { v -> set { it.copy(name = v) } })
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(PayoutKind.ADVANCE to "Аванс", PayoutKind.SALARY to "Зарплата", PayoutKind.OTHER to "Другое").forEach { (k, label) ->
                        FilterChip(selected = r.kind == k, onClick = { set { it.copy(kind = k) } }, label = { Text(label) })
                    }
                }
                SwitchRow("Учитывать эту выплату", "Выключите, чтобы скрыть её из расчётов", r.enabled) { v -> set { it.copy(enabled = v) } }
            }
            SectionCard(title = "Когда платят", icon = AppIcons.Event) {
                StepperField("День выплаты", r.payDay, { v -> set { it.copy(payDay = v) } }, 1..31)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(0 to "в том же месяце", 1 to "в следующем месяце").forEach { (offset, label) ->
                        FilterChip(selected = r.payMonthOffset == offset, onClick = { set { it.copy(payMonthOffset = offset) } }, label = { Text(label) })
                    }
                }
                Text("Если день выплаты выпадает на выходной или праздник:", style = MaterialTheme.typography.bodySmall)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(WeekendShift.BEFORE to "накануне", WeekendShift.AFTER to "после", WeekendShift.NONE to "в тот же день").forEach { (w, label) ->
                        FilterChip(selected = r.weekendShift == w, onClick = { set { it.copy(weekendShift = w) } }, label = { Text(label) })
                    }
                }
            }
            SectionCard(title = "За какой период", icon = AppIcons.DateRange) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StepperField("С числа", r.periodStartDay, { v -> set { it.copy(periodStartDay = v, periodEndDay = maxOf(v, it.periodEndDay)) } }, 1..31, modifier = Modifier.weight(1f))
                }
                StepperField("По число (31 — до конца месяца)", r.periodEndDay, { v -> set { it.copy(periodEndDay = maxOf(v, it.periodStartDay)) } }, 1..31)
            }
            SectionCard(title = "Сумма", icon = AppIcons.Calculate) {
                Column {
                    listOf(
                        PayoutAmountMode.PERCENT_OF_PERIOD to "Процент от заработка за период",
                        PayoutAmountMode.MONTH_REMAINDER to "Остаток: заработок за месяц минус другие выплаты",
                        PayoutAmountMode.FIXED to "Фиксированная сумма",
                        PayoutAmountMode.FORMULA to "Своя формула",
                    ).forEach { (mode, label) ->
                        Row(Modifier.fillMaxWidth().clickable { set { it.copy(amountMode = mode) } }, verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = r.amountMode == mode, onClick = { set { it.copy(amountMode = mode) } })
                            Text(label, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
                when (r.amountMode) {
                    PayoutAmountMode.PERCENT_OF_PERIOD -> StepperField("Процент", r.percent, { v -> set { it.copy(percent = v) } }, 1..100, suffix = "%", step = 5)
                    PayoutAmountMode.FIXED -> NumberField("Сумма", fixedText, { t ->
                        fixedText = t
                        Money.parse(t)?.let { m -> set { it.copy(fixedAmount = m) } }
                    }, suffix = "₽")
                    PayoutAmountMode.FORMULA -> {
                        val error = if (r.formula.isBlank()) "Введите формулу" else Formula.validate(r.formula)
                        TextInput("Формула", r.formula, { v -> set { it.copy(formula = v) } }, isError = error != null, supportingText = error ?: "Формула корректна")
                        Text("Переменные (в рублях и часах):", style = MaterialTheme.typography.bodySmall)
                        Formula.VARIABLES.forEach { (name, description) -> Text("$name — $description", style = MaterialTheme.typography.bodySmall) }
                        Text("Пример: ЗАРАБОТОК * 40% или min(ЗАРАБОТОК; 30000). Аргументы функций разделяются «;».", style = MaterialTheme.typography.bodySmall)
                    }
                    PayoutAmountMode.MONTH_REMAINDER -> Text(
                        "Сумма = заработок за весь месяц (с премиями и удержаниями) минус другие выплаты этого месяца. Если аванс уже получен, учитывается фактически полученная сумма.",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
            SectionCard(title = "Напоминание", icon = AppIcons.NotificationsActive) {
                SwitchRow("Напоминать о выплате", "Расчёт работает и без напоминаний", r.notify) { v -> set { it.copy(notify = v) } }
                if (r.notify) StepperField("За сколько дней", r.notifyDaysBefore, { v -> set { it.copy(notifyDaysBefore = v) } }, 0..30)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f)) { Text("Отмена") }
                Button(
                    modifier = Modifier.weight(1f),
                    enabled = r.name.isNotBlank() && (r.amountMode != PayoutAmountMode.FORMULA || (r.formula.isNotBlank() && Formula.validate(r.formula) == null)),
                    onClick = { scope.launchSafely(snackbar, success = "Сохранено", onSuccess = onBack) { container.finance.saveRule(r) } },
                ) { Text("Сохранить") }
            }
        }
    }
    if (askDelete && r != null) {
        ConfirmDialog(
            title = "Удалить «${r.name}»?",
            text = "Правило будет удалено. Отметки о полученных выплатах сохранятся.",
            confirmText = "Удалить",
            destructive = true,
            onConfirm = { scope.launchSafely(snackbar, success = "Удалено", onSuccess = onBack) { container.finance.deleteRule(r) } },
            onDismiss = { askDelete = false },
        )
    }
}

@Composable
fun AbsenceRulesScreen(container: AppContainer, settings: AppSettings, onBack: () -> Unit) {
    val snackbar = LocalSnackbar.current
    val scope = rememberCoroutineScope()
    val external by container.finance.external.collectAsStateWithLifecycle(initialValue = emptyList())
    var editingExternal by remember { mutableStateOf<ExternalEarning?>(null) }
    val rules = settings.absence
    fun update(transform: (AppSettings) -> AppSettings) = scope.launchSafely(snackbar) { container.settings.update(transform) }
    var mrotText by rememberSaveable { mutableStateOf(rules.minimumWage.toString().replace('.', ',')) }
    SubScreen(title = "Отпуск и больничный", onBack = onBack) { padding ->
        ScrollColumn(padding) {
            SectionCard(title = "Отпускные", icon = AppIcons.Vacation) {
                listOf(
                    VacationPayMethod.PLANNED_SHIFTS to "По запланированным сменам: оплата смен по графику, попавших на отпуск",
                    VacationPayMethod.AVERAGE_EARNINGS to "По среднему заработку (ст. 139 ТК РФ): заработок за 12 мес. / 29,3 × дни отпуска",
                    VacationPayMethod.NONE to "Не рассчитывать — вводить сумму вручную",
                ).forEach { (method, label) ->
                    Row(Modifier.fillMaxWidth().clickable { update { it.copy(absence = it.absence.copy(vacationMethod = method)) } }, verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = rules.vacationMethod == method, onClick = { update { it.copy(absence = it.absence.copy(vacationMethod = method)) } })
                        Text(label, style = MaterialTheme.typography.bodyMedium)
                    }
                }
                SwitchRow("Праздники не входят в дни отпуска", "ст. 120 ТК РФ", rules.excludeHolidaysFromVacation) { v ->
                    update { it.copy(absence = it.absence.copy(excludeHolidaysFromVacation = v)) }
                }
                StepperField("Напоминание за рабочих дней до отпуска", rules.vacationNotifyBusinessDaysBefore, { v ->
                    update { it.copy(absence = it.absence.copy(vacationNotifyBusinessDaysBefore = v)) }
                }, 0..10)
            }
            SectionCard(title = "Больничный", icon = AppIcons.Medical) {
                listOf(
                    SickPayMethod.BENEFIT_255FZ to "По формуле: среднедневной заработок × процент по стажу × дни болезни (255-ФЗ)",
                    SickPayMethod.NONE to "Не рассчитывать — вводить сумму вручную",
                ).forEach { (method, label) ->
                    Row(Modifier.fillMaxWidth().clickable { update { it.copy(absence = it.absence.copy(sickMethod = method)) } }, verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = rules.sickMethod == method, onClick = { update { it.copy(absence = it.absence.copy(sickMethod = method)) } })
                        Text(label, style = MaterialTheme.typography.bodyMedium)
                    }
                }
                Text(
                    "Среднедневной заработок = заработок за 2 предыдущих года / 730 (с учётом предельной базы и минимума от МРОТ). Процент: стаж до 5 лет — 60%, 5–8 лет — 80%, от 8 лет — 100%.",
                    style = MaterialTheme.typography.bodySmall,
                )
                StepperField("Дней за счёт работодателя", rules.employerSickDays, { v -> update { it.copy(absence = it.absence.copy(employerSickDays = v)) } }, 0..10)
                StepperField("Выплата Соцфонда через рабочих дней", rules.fundPaymentBusinessDays, { v -> update { it.copy(absence = it.absence.copy(fundPaymentBusinessDays = v)) } }, 1..30)
                NumberField("МРОТ (для минимального пособия)", mrotText, { t ->
                    mrotText = t
                    Money.parse(t)?.let { m -> if (m.isPositive) update { it.copy(absence = it.absence.copy(minimumWage = m)) } }
                }, suffix = "₽")
                Text("Предельная база взносов по годам:", style = MaterialTheme.typography.bodySmall)
                rules.yearlyCaps.toSortedMap().forEach { (year, cap) -> ValueRow(year.toString(), money(cap)) }
            }
            SectionCard(
                title = "Заработок прошлых периодов",
                icon = AppIcons.History,
                action = { TextButton(onClick = { editingExternal = ExternalEarning(year = container.clock.today().year - 1, amount = Money.ZERO) }) { Text("Добавить") } },
            ) {
                Text(
                    "Укажите заработок за годы (для больничного) или месяцы (для отпускных), за которые нет данных в приложении — например, из справки о заработке.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (external.isEmpty()) Text("Нет записей", style = MaterialTheme.typography.bodyMedium)
                external.forEach { e ->
                    Row(Modifier.fillMaxWidth().clickable { editingExternal = e }.padding(vertical = 4.dp)) {
                        Text(if (e.month == null) "${e.year} год" else Formats.monthTitle(java.time.YearMonth.of(e.year, e.month!!)), modifier = Modifier.weight(1f))
                        Text(money(e.amount))
                    }
                    ThinDivider()
                }
            }
        }
    }
    editingExternal?.let { e ->
        var year by remember(e) { mutableStateOf(e.year) }
        var month by remember(e) { mutableStateOf(e.month ?: 0) }
        var amount by remember(e) { mutableStateOf(if (e.amount.isZero) "" else e.amount.toString().replace('.', ',')) }
        AlertDialog(
            onDismissRequest = { editingExternal = null },
            title = { Text("Заработок") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    StepperField("Год", year, { year = it }, 2000..2100)
                    StepperField("Месяц (0 — весь год)", month, { month = it }, 0..12)
                    NumberField("Сумма", amount, { amount = it }, suffix = "₽")
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val value = Money.parse(amount) ?: return@TextButton
                    scope.launchSafely(snackbar, success = "Сохранено") {
                        container.finance.saveExternal(e.copy(year = year, month = month.takeIf { it in 1..12 }, amount = value))
                    }
                    editingExternal = null
                }) { Text("Сохранить") }
            },
            dismissButton = {
                Row {
                    if (e.id != 0L) {
                        TextButton(onClick = {
                            scope.launchSafely(snackbar, success = "Удалено") { container.finance.deleteExternal(e) }
                            editingExternal = null
                        }) { Text("Удалить") }
                    }
                    TextButton(onClick = { editingExternal = null }) { Text("Отмена") }
                }
            },
        )
    }
}
