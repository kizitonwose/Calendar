package io.github.ceniorpomidor.workcalendar.ui.finance

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
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
import io.github.ceniorpomidor.workcalendar.AppContainer
import io.github.ceniorpomidor.workcalendar.domain.export.CsvExport
import io.github.ceniorpomidor.workcalendar.domain.model.AccrualType
import io.github.ceniorpomidor.workcalendar.domain.model.ManualAccrual
import io.github.ceniorpomidor.workcalendar.domain.model.Money
import io.github.ceniorpomidor.workcalendar.domain.model.Payment
import io.github.ceniorpomidor.workcalendar.domain.model.PaymentType
import io.github.ceniorpomidor.workcalendar.domain.pay.PayoutAmount
import io.github.ceniorpomidor.workcalendar.domain.pay.PayoutCalculator
import io.github.ceniorpomidor.workcalendar.domain.pay.PayoutInstance
import io.github.ceniorpomidor.workcalendar.domain.util.Formats
import io.github.ceniorpomidor.workcalendar.ui.components.Banner
import io.github.ceniorpomidor.workcalendar.ui.components.BannerKind
import io.github.ceniorpomidor.workcalendar.ui.components.ConfirmDialog
import io.github.ceniorpomidor.workcalendar.ui.components.DateField
import io.github.ceniorpomidor.workcalendar.ui.components.LocalCurrency
import io.github.ceniorpomidor.workcalendar.ui.components.LocalSnackbar
import io.github.ceniorpomidor.workcalendar.ui.components.NumberField
import io.github.ceniorpomidor.workcalendar.ui.components.ScrollColumn
import io.github.ceniorpomidor.workcalendar.ui.components.SectionCard
import io.github.ceniorpomidor.workcalendar.ui.components.SubScreen
import io.github.ceniorpomidor.workcalendar.ui.components.TextInput
import io.github.ceniorpomidor.workcalendar.ui.components.ValueRow
import io.github.ceniorpomidor.workcalendar.ui.components.launchSafely
import io.github.ceniorpomidor.workcalendar.ui.components.money
import io.github.ceniorpomidor.workcalendar.ui.theme.AppIcons

/** Registers money received: for an expected payout (advance/salary) or any other payment. */
@Composable
fun PaymentEditScreen(container: AppContainer, paymentId: Long?, payoutKey: String, onDone: () -> Unit) {
    val snackbar = LocalSnackbar.current
    val scope = rememberCoroutineScope()
    var existing by remember { mutableStateOf<Payment?>(null) }
    var payout by remember { mutableStateOf<PayoutInstance?>(null) }
    var loaded by remember { mutableStateOf(false) }
    var initialized by rememberSaveable { mutableStateOf(false) }
    var type by rememberSaveable { mutableStateOf(PaymentType.SALARY) }
    var amountText by rememberSaveable { mutableStateOf("") }
    var date by rememberSaveable { mutableStateOf(container.clock.today()) }
    var note by rememberSaveable { mutableStateOf("") }
    var askDelete by remember { mutableStateOf(false) }

    LaunchedEffect(paymentId, payoutKey) {
        loaded = false
        val instance = payoutKey.takeIf { it.isNotBlank() }?.let { container.finance.findPayout(it) }
        val payment = paymentId?.let { container.finance.getPayment(it) } ?: instance?.payment
        payout = instance
        existing = payment
        if (!initialized) {
            type = payment?.type ?: instance?.rule?.let { PayoutCalculator.paymentTypeFor(it.kind) } ?: PaymentType.SALARY
            amountText = (payment?.amount ?: instance?.expectedNet)?.toString()?.replace('.', ',').orEmpty()
            date = payment?.date ?: minOf(instance?.payDate ?: container.clock.today(), container.clock.today())
            note = payment?.note.orEmpty()
            initialized = true
        }
        loaded = true
    }
    val amount = Money.parse(amountText)

    SubScreen(
        title = payout?.title?.replaceFirstChar { it.uppercase() } ?: if (existing != null) "Выплата" else "Полученная выплата",
        onBack = onDone,
        actions = { if (existing != null) IconButton(onClick = { askDelete = true }) { Icon(AppIcons.Delete, contentDescription = "Удалить") } },
    ) { padding ->
        ScrollColumn(padding) {
            if (!loaded) return@ScrollColumn
            if ((payoutKey.isNotBlank() && payout == null) || (paymentId != null && existing == null)) {
                Banner("Эта выплата больше недоступна", BannerKind.WARNING)
                return@ScrollColumn
            }
            SectionCard(title = "Получено", icon = AppIcons.Savings) {
                if (payout == null) {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        PaymentType.entries.forEach { t ->
                            FilterChip(selected = type == t, onClick = { type = t }, label = { Text(CsvExport.paymentTypeName(t)) })
                        }
                    }
                }
                NumberField("Фактическая сумма", amountText, { amountText = it }, suffix = LocalCurrency.current, isError = amountText.isNotBlank() && amount == null)
                DateField("Дата получения", date, { date = it })
                TextInput("Комментарий", note, { note = it }, singleLine = false)
                Text(
                    "Выплаты хранятся отдельно от начислений: отметка о получении не увеличивает заработок, а уменьшает остаток к получению.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(onClick = onDone, modifier = Modifier.weight(1f)) { Text("Отмена") }
                Button(
                    enabled = amount != null && loaded,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        val value = amount ?: return@Button
                        scope.launchSafely(snackbar, success = "Выплата сохранена", onSuccess = onDone) {
                            val p = payout
                            if (p != null) {
                                container.finance.markPayoutReceived(p, value, date, note.trim())
                            } else {
                                val base = existing ?: Payment(type = type, amount = value, date = date)
                                container.finance.savePayment(base.copy(type = type, amount = value, date = date, note = note.trim()))
                            }
                        }
                    },
                ) { Text("Сохранить") }
            }
            payout?.let { p ->
                SectionCard(title = "Ожидаемая выплата", icon = AppIcons.Event) {
                    ValueRow("Дата по правилу", "${Formats.weekdayShort(p.payDate.dayOfWeek)}, ${Formats.date(p.payDate)}")
                    ValueRow("Период", Formats.period(p.period.start, p.period.endInclusive))
                    when (val a = p.amount) {
                        is PayoutAmount.Calculated -> {
                            ValueRow("Расчётная сумма", (if (a.isEstimate) "≈ " else "") + money(a.net), emphasize = true)
                            a.explanation.forEach { Text("• $it", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                        }
                        is PayoutAmount.Insufficient -> Banner(a.reason, BannerKind.WARNING)
                    }
                }
            }
        }
    }
    if (askDelete && existing != null) {
        ConfirmDialog(
            title = "Удалить выплату?",
            text = "Запись о получении ${money(existing!!.amount)} будет удалена. Начисления не изменятся.",
            confirmText = "Удалить",
            destructive = true,
            onConfirm = { scope.launchSafely(snackbar, success = "Удалено", onSuccess = onDone) { container.finance.deletePayment(existing!!) } },
            onDismiss = { askDelete = false },
        )
    }
}

/** Manual accrual or deduction: bonus, allowance, alimony, tax and so on. */
@Composable
fun AccrualEditScreen(container: AppContainer, accrualId: Long?, onDone: () -> Unit) {
    val snackbar = LocalSnackbar.current
    val scope = rememberCoroutineScope()
    var existing by remember { mutableStateOf<ManualAccrual?>(null) }
    var loaded by rememberSaveable { mutableStateOf(accrualId == null) }
    var type by rememberSaveable { mutableStateOf(AccrualType.BONUS) }
    var title by rememberSaveable { mutableStateOf("") }
    var amountText by rememberSaveable { mutableStateOf("") }
    var date by rememberSaveable { mutableStateOf(container.clock.today()) }
    var note by rememberSaveable { mutableStateOf("") }
    var askDelete by remember { mutableStateOf(false) }

    LaunchedEffect(accrualId) {
        if (accrualId != null) {
            val a = container.finance.getAccrual(accrualId)
            existing = a
            if (a != null && !loaded) {
                type = a.type
                title = a.title
                amountText = a.amount.toString().replace('.', ',')
                date = a.date
                note = a.note
            }
            loaded = true
        }
    }
    val amount = Money.parse(amountText)
    SubScreen(
        title = if (existing == null) "Начисление или удержание" else existing!!.title,
        onBack = onDone,
        actions = { if (existing != null) IconButton(onClick = { askDelete = true }) { Icon(AppIcons.Delete, contentDescription = "Удалить") } },
    ) { padding ->
        ScrollColumn(padding) {
            SectionCard(title = "Тип") {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    AccrualType.entries.forEach { t ->
                        FilterChip(
                            selected = type == t,
                            onClick = {
                                type = t
                                if (title.isBlank() || AccrualType.entries.any { CsvExport.accrualTypeName(it) == title }) title = CsvExport.accrualTypeName(t)
                            },
                            label = { Text(CsvExport.accrualTypeName(t)) },
                        )
                    }
                }
                Text(
                    if (type.isNegative) "Уменьшает начисленную сумму" else "Увеличивает начисленную сумму",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            SectionCard {
                TextInput("Название", title, { title = it })
                NumberField("Сумма", amountText, { amountText = it }, suffix = "₽", isError = amountText.isNotBlank() && amount == null)
                DateField("Дата (месяц, к которому относится)", date, { date = it })
                TextInput("Комментарий", note, { note = it }, singleLine = false)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(onClick = onDone, modifier = Modifier.weight(1f)) { Text("Отмена") }
                Button(
                    enabled = amount != null && title.isNotBlank() && loaded,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        val value = amount ?: return@Button
                        scope.launchSafely(snackbar, success = "Сохранено", onSuccess = onDone) {
                            val base = existing ?: ManualAccrual(date = date, type = type, title = title, amount = value)
                            container.finance.saveAccrual(base.copy(date = date, type = type, title = title.trim(), amount = value, note = note.trim()))
                        }
                    },
                ) { Text("Сохранить") }
            }
        }
    }
    if (askDelete && existing != null) {
        ConfirmDialog(
            title = "Удалить запись?",
            text = "«${existing!!.title}» на ${money(existing!!.amount)} будет удалена.",
            confirmText = "Удалить",
            destructive = true,
            onConfirm = { scope.launchSafely(snackbar, success = "Удалено", onSuccess = onDone) { container.finance.deleteAccrual(existing!!) } },
            onDismiss = { askDelete = false },
        )
    }
}
