package io.github.ceniorpomidor.workcalendar.ui.finance

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.ceniorpomidor.workcalendar.AppContainer
import io.github.ceniorpomidor.workcalendar.domain.model.Money
import io.github.ceniorpomidor.workcalendar.domain.notify.AbsencePayment
import io.github.ceniorpomidor.workcalendar.domain.pay.PayoutAmount
import io.github.ceniorpomidor.workcalendar.domain.pay.PayoutInstance
import io.github.ceniorpomidor.workcalendar.domain.pay.PeriodSummary
import io.github.ceniorpomidor.workcalendar.domain.time.DateRange
import io.github.ceniorpomidor.workcalendar.domain.util.Formats
import io.github.ceniorpomidor.workcalendar.ui.components.Banner
import io.github.ceniorpomidor.workcalendar.ui.components.BannerKind
import io.github.ceniorpomidor.workcalendar.ui.components.DateField
import io.github.ceniorpomidor.workcalendar.ui.components.DateRangePickerDialog
import io.github.ceniorpomidor.workcalendar.ui.components.LocalSnackbar
import io.github.ceniorpomidor.workcalendar.ui.components.NumberField
import io.github.ceniorpomidor.workcalendar.ui.components.ScrollColumn
import io.github.ceniorpomidor.workcalendar.ui.components.SectionCard
import io.github.ceniorpomidor.workcalendar.ui.components.SubScreen
import io.github.ceniorpomidor.workcalendar.ui.components.Tag
import io.github.ceniorpomidor.workcalendar.ui.components.ThinDivider
import io.github.ceniorpomidor.workcalendar.ui.components.ValueRow
import io.github.ceniorpomidor.workcalendar.ui.components.launchSafely
import io.github.ceniorpomidor.workcalendar.ui.components.money
import io.github.ceniorpomidor.workcalendar.ui.theme.AppIcons
import io.github.ceniorpomidor.workcalendar.ui.theme.AppTheme

@Composable
fun FinanceScreen(
    container: AppContainer,
    onHistory: () -> Unit,
    onStats: () -> Unit,
    onPayout: (String) -> Unit,
    onPayment: (Long) -> Unit,
    onNewPayment: () -> Unit,
    onAccrual: (Long) -> Unit,
    onNewAccrual: () -> Unit,
    onOpenAbsence: (Long) -> Unit,
    onOpenUnconfirmed: () -> Unit,
    onRates: () -> Unit,
    onPayoutRules: () -> Unit,
    onBack: (() -> Unit)? = null,
) {
    val vm: FinanceViewModel = viewModel { FinanceViewModel(container) }
    val state by vm.state.collectAsStateWithLifecycle()
    var pickRange by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<PayoutInstance?>(null) }
    val snackbar = LocalSnackbar.current
    val scope = rememberCoroutineScope()
    val period = state.period

    SubScreen(
        title = "Финансы",
        onBack = onBack,
        actions = {
            IconButton(onClick = onStats) { Icon(AppIcons.BarChart, contentDescription = "Статистика") }
            IconButton(onClick = onHistory) { Icon(AppIcons.Receipt, contentDescription = "История") }
        },
    ) { padding ->
        ScrollColumn(padding) {
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                val modes = listOf(PeriodMode.MONTH to "Месяц", PeriodMode.RANGE to "Период", PeriodMode.YEAR to "Год")
                modes.forEachIndexed { index, (mode, label) ->
                    SegmentedButton(
                        selected = period.mode == mode,
                        onClick = {
                            vm.setMode(mode)
                            if (mode == PeriodMode.RANGE) pickRange = true
                        },
                        shape = SegmentedButtonDefaults.itemShape(index, modes.size),
                    ) { Text(label) }
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = {
                        when (period.mode) {
                            PeriodMode.MONTH -> vm.setMonth(period.month.minusMonths(1))
                            PeriodMode.YEAR -> vm.setYear(period.range.start.year - 1)
                            PeriodMode.RANGE -> pickRange = true
                        }
                    },
                    enabled = period.mode != PeriodMode.RANGE,
                ) { Icon(AppIcons.ChevronLeft, contentDescription = "Назад") }
                Text(
                    when (period.mode) {
                        PeriodMode.MONTH -> Formats.monthTitle(period.month)
                        PeriodMode.YEAR -> "${period.range.start.year} год"
                        PeriodMode.RANGE -> Formats.period(period.range.start, period.range.endInclusive)
                    },
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f).clickable(enabled = period.mode == PeriodMode.RANGE) { pickRange = true },
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
                IconButton(
                    onClick = {
                        when (period.mode) {
                            PeriodMode.MONTH -> vm.setMonth(period.month.plusMonths(1))
                            PeriodMode.YEAR -> vm.setYear(period.range.start.year + 1)
                            PeriodMode.RANGE -> pickRange = true
                        }
                    },
                    enabled = period.mode != PeriodMode.RANGE,
                ) { Icon(AppIcons.ChevronRight, contentDescription = "Вперёд") }
            }

            val summary = state.summary
            if (summary == null) {
                Text("Загрузка…")
                return@ScrollColumn
            }
            if (!state.hasRates) {
                Banner("Не задана почасовая ставка — заработок не рассчитывается.", BannerKind.WARNING, action = { TextButton(onClick = onRates) { Text("Указать") } })
            }
            if (summary.unconfirmedShifts > 0) {
                Banner(
                    "${Formats.shifts(summary.unconfirmedShifts)} без отмеченных часов — они не входят в подтверждённый заработок.",
                    BannerKind.WARNING,
                    action = { TextButton(onClick = onOpenUnconfirmed) { Text("Отметить") } },
                )
            }
            HeroCard(summary, state.previous, monthMode = period.mode == PeriodMode.MONTH)
            AccrualsCard(summary)
            HoursCard(summary)
            if (period.mode == PeriodMode.MONTH) {
                PayoutsCard(state, onEdit = { editing = it }, onRules = onPayoutRules)
                if (state.forecast.isNotEmpty()) ForecastCard(state.forecast, onMonth = { vm.setMonth(it) })
            }
            if (state.upcoming.isNotEmpty() && period.mode != PeriodMode.MONTH) {
                SectionCard(title = "Ближайшие выплаты", icon = AppIcons.Event) {
                    state.upcoming.take(4).forEach { PayoutRow(it, state.today, onClick = { onPayout(it.key) }) }
                }
            }
            if (state.absencePayments.isNotEmpty()) {
                SectionCard(title = "Отпускные и больничные", icon = AppIcons.Vacation) {
                    state.absencePayments.forEach { row ->
                        val title = when (row.item.part) {
                            AbsencePayment.Part.VACATION -> "Отпускные"
                            AbsencePayment.Part.SICK_EMPLOYER -> "Больничный (работодатель)"
                            AbsencePayment.Part.SICK_FUND -> "Больничный (Соцфонд)"
                            AbsencePayment.Part.OTHER -> row.item.absence.displayTitle()
                        }
                        Row(
                            Modifier.fillMaxWidth().clickable { onOpenAbsence(row.item.absence.id) }.padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(title, style = MaterialTheme.typography.bodyLarge)
                                Text(
                                    "${Formats.period(row.item.absence.startDate, row.item.absence.endDate)} · выплата ${Formats.shortDate(row.item.date)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(row.item.amount?.let { money(it) } ?: "не рассчитано", style = MaterialTheme.typography.bodyLarge)
                                row.received?.let { Text("получено ${money(it.amount)}", style = MaterialTheme.typography.bodySmall, color = AppTheme.status.positive) }
                            }
                        }
                    }
                }
            }
            SectionCard(
                title = "Доп. начисления и удержания",
                icon = AppIcons.Calculate,
                action = { TextButton(onClick = onNewAccrual) { Text("Добавить") } },
            ) {
                if (state.accruals.isEmpty()) {
                    Text("Премии, надбавки, удержания и налоги, внесённые вручную", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                state.accruals.forEach { a ->
                    Row(Modifier.fillMaxWidth().clickable { onAccrual(a.id) }.padding(vertical = 4.dp)) {
                        Column(Modifier.weight(1f)) {
                            Text(a.title, style = MaterialTheme.typography.bodyLarge)
                            Text(Formats.date(a.date), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text(
                            (if (a.type.isNegative) "−" else "+") + money(a.amount),
                            color = if (a.type.isNegative) AppTheme.status.negative else AppTheme.status.positive,
                        )
                    }
                }
            }
            SectionCard(
                title = "Полученные деньги",
                icon = AppIcons.Savings,
                action = { TextButton(onClick = onNewPayment) { Text("Добавить") } },
            ) {
                if (state.payments.isEmpty()) {
                    Text("За период выплат не отмечено", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                state.payments.forEach { p ->
                    Row(Modifier.fillMaxWidth().clickable { onPayment(p.id) }.padding(vertical = 4.dp)) {
                        Column(Modifier.weight(1f)) {
                            Text(io.github.ceniorpomidor.workcalendar.domain.export.CsvExport.paymentTypeName(p.type), style = MaterialTheme.typography.bodyLarge)
                            Text(Formats.date(p.date) + if (p.note.isNotBlank()) " · ${p.note}" else "", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text(money(p.amount), style = MaterialTheme.typography.bodyLarge)
                    }
                }
                if (state.payments.isNotEmpty()) {
                    ThinDivider()
                    ValueRow("Всего получено", money(state.payments.fold(Money.ZERO) { acc, p -> acc + p.amount }), emphasize = true)
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }

    editing?.let { payout ->
        ReceivedDialog(
            payout = payout,
            today = state.today,
            onSave = { amount, date ->
                editing = null
                scope.launchSafely(snackbar, success = "Сохранено: пришло ${Formats.money(amount)}") {
                    container.finance.markPayoutReceived(payout, amount, date, payout.payment?.note.orEmpty())
                }
            },
            onClear = payout.payment?.let { payment ->
                {
                    editing = null
                    scope.launchSafely(snackbar, success = "Отметка о получении снята") { container.finance.deletePayment(payment) }
                }
            },
            onMore = {
                editing = null
                onPayout(payout.key)
            },
            onDismiss = { editing = null },
        )
    }

    if (pickRange) {
        DateRangePickerDialog(period.range.start, period.range.endInclusive, "Период", onDismiss = { pickRange = false }) { s, e ->
            vm.setRange(DateRange(s, e))
            pickRange = false
        }
    }
}

@Composable
private fun HeroCard(summary: PeriodSummary, previous: PeriodSummary?, monthMode: Boolean) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("За отработанные часы", style = MaterialTheme.typography.labelLarge)
            Text(money(summary.confirmedEarnings), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text("${Formats.hours(summary.workedMinutes)} подтверждено из ${Formats.hours(summary.plannedMinutes)} по графику", style = MaterialTheme.typography.bodyMedium)
            if (monthMode) {
                summary.forecastEarnings?.takeIf { it != summary.confirmedEarnings }?.let {
                    Text("С учётом оставшихся смен: ≈ ${money(it)}", style = MaterialTheme.typography.bodyMedium)
                }
            }
            if (summary.grossAccrued != summary.confirmedEarnings) {
                Text("Всего начислено (с отпускными, премиями, удержаниями): ${money(summary.grossAccrued)}", style = MaterialTheme.typography.bodySmall)
            }
            if (previous != null) {
                val prev = previous.confirmedEarnings
                val diffText = if (prev.isPositive) {
                    val pct = ((summary.confirmedEarnings.kopecks - prev.kopecks) * 100.0 / prev.kopecks)
                    " (${if (pct >= 0) "+" else ""}${"%.0f".format(pct)}%)"
                } else {
                    ""
                }
                Text("Предыдущий месяц: ${money(prev)}$diffText", style = MaterialTheme.typography.bodySmall)
            }
            Text("Без учёта налогов — это не сумма на руки.", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun AccrualsCard(s: PeriodSummary) {
    SectionCard(title = "Начисления", icon = AppIcons.Payments) {
        ValueRow("Работа (подтверждённые часы)", money(s.confirmedEarnings))
        if (!s.surcharges.isZero) ValueRow("  в т.ч. надбавки (праздники, сверхурочные, доп. смены)", money(s.surcharges))
        if (!s.vacationPay.isZero || s.vacationDays > 0) ValueRow("Отпускные", money(s.vacationPay))
        if (!s.sickPay.isZero || s.sickDays > 0) ValueRow("Больничные", money(s.sickPay))
        if (!s.otherAbsencePay.isZero) ValueRow("Другие отсутствия", money(s.otherAbsencePay))
        if (!s.bonuses.isZero) ValueRow("Премии и доп. начисления", "+" + money(s.bonuses))
        if (!s.deductions.isZero) ValueRow("Удержания", "−" + money(s.deductions))
        ThinDivider()
        ValueRow("Итого начислено", money(s.grossAccrued), emphasize = true)
        if (s.absencesWithoutAmount > 0) {
            Text("Для ${s.absencesWithoutAmount} отсутствий сумма не указана", style = MaterialTheme.typography.bodySmall, color = AppTheme.status.onAwaiting)
        }
    }
}

@Composable
private fun HoursCard(s: PeriodSummary) {
    SectionCard(title = "Часы и смены", icon = AppIcons.Schedule) {
        ValueRow("Отработано / по плану", "${Formats.hours(s.workedMinutes)} / ${Formats.hours(s.plannedMinutes)}", emphasize = true)
        if (s.extraMinutes > 0) ValueRow("Дополнительные и сверхурочные", Formats.hours(s.extraMinutes))
        if (s.holidayMinutes > 0) ValueRow("Праздничные", Formats.hours(s.holidayMinutes))
        ValueRow("Смены: отработано", "${s.workedShifts} из ${s.scheduledShifts}")
        if (s.extraShifts > 0) ValueRow("  в т.ч. дополнительных", s.extraShifts.toString())
        if (s.unconfirmedShifts > 0) ValueRow("Без отмеченных часов", s.unconfirmedShifts.toString(), valueColor = AppTheme.status.onAwaiting)
        if (s.upcomingShifts > 0) ValueRow("Впереди", s.upcomingShifts.toString())
        if (s.missedShifts > 0) ValueRow("Не состоялись (пропущены)", s.missedShifts.toString())
        if (s.cancelledShifts > 0) ValueRow("Отменены", s.cancelledShifts.toString())
        ValueRow("Выходных дней", s.daysOff.toString())
        if (s.vacationDays > 0) ValueRow("Дней отпуска", s.vacationDays.toString())
        if (s.sickDays > 0) ValueRow("Дней больничного", s.sickDays.toString())
        s.averagePerShift?.let { ValueRow("Средний заработок за смену", money(it)) }
        s.averagePerHour?.let { ValueRow("Средний заработок за час", money(it)) }
    }
}

@Composable
private fun PayoutsCard(state: FinanceUiState, onEdit: (PayoutInstance) -> Unit, onRules: () -> Unit) {
    SectionCard(title = "Аванс и зарплата за месяц", icon = AppIcons.Wallet, action = { TextButton(onClick = onRules) { Text("Правила") } }) {
        if (state.payouts.isEmpty()) {
            Text("Правила выплат не настроены", style = MaterialTheme.typography.bodyMedium)
            return@SectionCard
        }
        Text(
            "Нажмите на выплату, чтобы внести или исправить сумму, которая пришла.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        state.payouts.forEach { PayoutAmountsRow(it, state.today, onClick = { onEdit(it) }) }
        ThinDivider()
        ValueRow("Ожидается за месяц", state.expectedTotal?.let { money(it) } ?: "недостаточно данных")
        ValueRow("Пришло", money(state.receivedTotal), valueColor = AppTheme.status.positive)
        ValueRow("Ещё должны выплатить", state.remaining?.let { money(it) } ?: "недостаточно данных", emphasize = true)
    }
}

/** A payout with the calculated amount and the amount that came, side by side. */
@Composable
private fun PayoutAmountsRow(payout: PayoutInstance, today: java.time.LocalDate, onClick: () -> Unit) {
    val status = AppTheme.status
    Column(Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 6.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(payout.rule.name, style = MaterialTheme.typography.titleSmall)
                Text(
                    "${Formats.weekdayShort(payout.payDate.dayOfWeek)}, ${Formats.date(payout.payDate)} · за ${Formats.period(payout.period.start, payout.period.endInclusive)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Icon(AppIcons.Edit, contentDescription = "Внести сумму", tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Row {
            Column(Modifier.weight(1f)) {
                Text("Рассчитано", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                when (val amount = payout.amount) {
                    is PayoutAmount.Calculated -> Text(
                        (if (amount.isEstimate) "≈ " else "") + money(amount.net),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    is PayoutAmount.Insufficient -> Text("нет данных", style = MaterialTheme.typography.bodyLarge)
                }
            }
            Column(Modifier.weight(1f)) {
                Text("Пришло", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                val payment = payout.payment
                when {
                    payment != null -> Text(
                        "${money(payment.amount)} · ${Formats.shortDate(payment.date)}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = status.positive,
                    )
                    payout.payDate.isAfter(today) -> Tag("ожидается", status.planned, status.onPlanned)
                    else -> Tag("внесите сумму", status.awaiting, status.onAwaiting)
                }
            }
        }
        (payout.amount as? PayoutAmount.Insufficient)?.let {
            Text(it.reason, style = MaterialTheme.typography.bodySmall, color = status.onAwaiting)
        }
    }
}

/** Coming months: payouts expected by the shifts already in the calendar. */
@Composable
private fun ForecastCard(forecast: List<ForecastMonth>, onMonth: (java.time.YearMonth) -> Unit) {
    SectionCard(title = "Прогноз выплат", icon = AppIcons.TrendingUp) {
        Text(
            "По сменам в календаре, до вычета налога. Изменится, если поменять смены, график или ставку.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        forecast.forEachIndexed { index, f ->
            if (index > 0) ThinDivider()
            Column(Modifier.fillMaxWidth().clickable { onMonth(f.month) }.padding(vertical = 4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(Formats.monthTitle(f.month), style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                    Text(f.total?.let { "≈ ${money(it)}" } ?: "нет данных", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                }
                Text(
                    "${Formats.shifts(f.summary.scheduledShifts)} · ${Formats.hours(f.summary.plannedMinutes)}" +
                        (f.summary.plannedEarnings?.let { " · заработок ≈ ${money(it)}" } ?: ""),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                f.payouts.forEach { payout ->
                    Row {
                        Text(
                            "${payout.rule.name} ${Formats.shortDate(payout.payDate)}",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f),
                        )
                        Text(payout.effectiveNet?.let { "≈ ${money(it)}" } ?: "нет данных", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}

/** Quick entry of the amount that came for a payout. */
@Composable
private fun ReceivedDialog(
    payout: PayoutInstance,
    today: java.time.LocalDate,
    onSave: (Money, java.time.LocalDate) -> Unit,
    onClear: (() -> Unit)?,
    onMore: () -> Unit,
    onDismiss: () -> Unit,
) {
    var amountText by rememberSaveable { mutableStateOf((payout.payment?.amount ?: payout.expectedNet)?.toString()?.replace('.', ',').orEmpty()) }
    var date by rememberSaveable { mutableStateOf(payout.payment?.date ?: if (payout.payDate.isAfter(today)) today else payout.payDate) }
    val amount = Money.parse(amountText)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(payout.title.replaceFirstChar { it.uppercase() }) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Рассчитано: " + (payout.expectedNet?.let { money(it) } ?: "нет данных") + ", выплата по правилу ${Formats.date(payout.payDate)}",
                    style = MaterialTheme.typography.bodyMedium,
                )
                NumberField("Сколько пришло", amountText, { amountText = it }, suffix = "₽", isError = amountText.isNotBlank() && amount == null)
                DateField("Дата получения", date, { date = it })
                TextButton(onClick = onMore) { Text("Комментарий и подробный расчёт") }
            }
        },
        confirmButton = {
            TextButton(enabled = amount != null && amount.isPositive, onClick = { amount?.let { onSave(it, date) } }) { Text("Сохранить") }
        },
        dismissButton = {
            Row {
                if (onClear != null) TextButton(onClick = onClear) { Text("Не пришло") }
                TextButton(onClick = onDismiss) { Text("Отмена") }
            }
        },
    )
}

@Composable
fun PayoutRow(payout: PayoutInstance, today: java.time.LocalDate, onClick: () -> Unit) {
    val status = AppTheme.status
    Row(Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(payout.rule.name, style = MaterialTheme.typography.bodyLarge)
            Text(
                "${Formats.weekdayShort(payout.payDate.dayOfWeek)}, ${Formats.date(payout.payDate)} · за ${Formats.period(payout.period.start, payout.period.endInclusive)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            when {
                payout.isReceived -> Tag("получено ${Formats.shortDate(payout.payment!!.date)}", status.confirmed, status.onConfirmed)
                payout.payDate.isBefore(today) -> Tag("не отмечено получение", status.awaiting, status.onAwaiting)
                else -> Tag("ожидается", status.planned, status.onPlanned)
            }
        }
        Spacer(Modifier.width(8.dp))
        Column(horizontalAlignment = Alignment.End) {
            when (val amount = payout.amount) {
                is PayoutAmount.Calculated -> {
                    Text((if (amount.isEstimate && !payout.isReceived) "≈ " else "") + money(amount.net), style = MaterialTheme.typography.titleSmall)
                    if (payout.isReceived && payout.payment!!.amount != amount.net) {
                        Text("получено ${money(payout.payment!!.amount)}", style = MaterialTheme.typography.bodySmall, color = status.positive)
                    }
                }
                is PayoutAmount.Insufficient -> Text("нет данных", style = MaterialTheme.typography.bodySmall, color = Color.Unspecified)
            }
        }
    }
    if (payout.amount is PayoutAmount.Insufficient) {
        Text((payout.amount as PayoutAmount.Insufficient).reason, style = MaterialTheme.typography.bodySmall, color = status.onAwaiting)
    }
}
