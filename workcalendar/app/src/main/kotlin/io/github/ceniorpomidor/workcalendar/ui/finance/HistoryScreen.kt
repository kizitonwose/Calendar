package io.github.ceniorpomidor.workcalendar.ui.finance

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.ceniorpomidor.workcalendar.AppContainer
import io.github.ceniorpomidor.workcalendar.domain.export.CsvExport
import io.github.ceniorpomidor.workcalendar.domain.model.AbsenceType
import io.github.ceniorpomidor.workcalendar.domain.model.Money
import io.github.ceniorpomidor.workcalendar.domain.model.ShiftStatus
import io.github.ceniorpomidor.workcalendar.domain.time.DateRange
import io.github.ceniorpomidor.workcalendar.domain.util.Formats
import io.github.ceniorpomidor.workcalendar.ui.components.EmptyState
import io.github.ceniorpomidor.workcalendar.ui.components.SectionTitle
import io.github.ceniorpomidor.workcalendar.ui.components.SubScreen
import io.github.ceniorpomidor.workcalendar.ui.components.ThinDivider
import io.github.ceniorpomidor.workcalendar.ui.components.money
import io.github.ceniorpomidor.workcalendar.ui.theme.AppIcons
import io.github.ceniorpomidor.workcalendar.ui.theme.AppTheme
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import java.time.LocalDate
import java.time.YearMonth

enum class HistoryFilter(val title: String) {
    ALL("Все"),
    WORK("Смены"),
    VACATION("Отпускные"),
    SICK("Больничные"),
    ACCRUALS("Начисления"),
    PAYMENTS("Выплаты"),
}

private enum class EntryKind { SHIFT, ABSENCE, ACCRUAL, PAYMENT }

private data class HistoryEntry(
    val date: LocalDate,
    val kind: EntryKind,
    val id: Long,
    val title: String,
    val subtitle: String,
    val amount: Money?,
    val negative: Boolean = false,
    val filter: HistoryFilter,
)

/** Accruals (per shift, absences, manual) and received payments with filters by year, month and type. */
@Composable
fun HistoryScreen(
    container: AppContainer,
    onBack: (() -> Unit)?,
    onShift: (Long) -> Unit,
    onPayment: (Long) -> Unit,
    onAccrual: (Long) -> Unit,
    onAbsence: (Long) -> Unit,
) {
    var year by rememberSaveable { mutableStateOf(container.clock.today().year) }
    var month by rememberSaveable { mutableStateOf(0) }
    var filter by rememberSaveable { mutableStateOf(HistoryFilter.ALL) }
    val entries by remember {
        snapshotFlow { year }.flatMapLatest { y ->
            val range = DateRange.year(y)
            combine(
                container.shifts.observeRange(range),
                container.absences.observeRange(range),
                container.finance.accruals,
                container.finance.payments,
                container.calc.context,
            ) { shifts, absences, accruals, payments, calc ->
                val list = ArrayList<HistoryEntry>()
                shifts.filter { it.status == ShiftStatus.CONFIRMED || it.status == ShiftStatus.MISSED }.forEach { s ->
                    val earned = calc.pay.earned(s)
                    list += HistoryEntry(
                        date = s.date,
                        kind = EntryKind.SHIFT,
                        id = s.id,
                        title = if (s.status == ShiftStatus.MISSED) "Смена не состоялась" else "Смена · ${Formats.hours((s.workedMinutes ?: 0).toLong())}",
                        subtitle = Formats.timeRange(s.plannedStart, s.plannedEnd),
                        amount = earned,
                        filter = HistoryFilter.WORK,
                    )
                }
                absences.filter { it.paid }.forEach { a ->
                    list += HistoryEntry(
                        date = a.startDate,
                        kind = EntryKind.ABSENCE,
                        id = a.id,
                        title = a.displayTitle(),
                        subtitle = "${Formats.period(a.startDate, a.endDate)}, ${Formats.days(a.calendarDays)}",
                        amount = a.amount,
                        filter = if (a.type == AbsenceType.SICK) HistoryFilter.SICK else HistoryFilter.VACATION,
                    )
                }
                accruals.filter { it.date.year == y }.forEach { a ->
                    list += HistoryEntry(a.date, EntryKind.ACCRUAL, a.id, a.title, CsvExport.accrualTypeName(a.type), a.amount, a.type.isNegative, HistoryFilter.ACCRUALS)
                }
                payments.filter { it.date.year == y }.forEach { p ->
                    list += HistoryEntry(
                        p.date,
                        EntryKind.PAYMENT,
                        p.id,
                        "Получено: ${CsvExport.paymentTypeName(p.type)}",
                        p.note.ifBlank { p.expectedAmount?.let { "ожидалось ${Formats.money(it)}" } ?: "" },
                        p.amount,
                        filter = HistoryFilter.PAYMENTS,
                    )
                }
                list.sortedWith(compareByDescending<HistoryEntry> { it.date }.thenBy { it.kind })
            }
        }
    }.collectAsStateWithLifecycle(initialValue = emptyList())

    val visible = entries.filter { (filter == HistoryFilter.ALL || it.filter == filter) && (month == 0 || it.date.monthValue == month) }

    SubScreen(title = "История операций", onBack = onBack) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(bottom = 24.dp)) {
            item {
                Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { year-- }) { Icon(AppIcons.ChevronLeft, contentDescription = "Предыдущий год") }
                    Text("$year год", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    IconButton(onClick = { year++ }) { Icon(AppIcons.ChevronRight, contentDescription = "Следующий год") }
                }
            }
            item {
                Row(Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 12.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(selected = month == 0, onClick = { month = 0 }, label = { Text("Весь год") })
                    for (m in 1..12) {
                        FilterChip(selected = month == m, onClick = { month = m }, label = { Text(Formats.monthShort(java.time.Month.of(m))) })
                    }
                }
            }
            item {
                Row(Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 12.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    HistoryFilter.entries.forEach { f -> FilterChip(selected = filter == f, onClick = { filter = f }, label = { Text(f.title) }) }
                }
            }
            if (visible.isEmpty()) {
                item { EmptyState(AppIcons.Receipt, "Нет операций", "Подтверждённые смены, отпускные, больничные, начисления и выплаты появятся здесь.") }
            }
            val grouped = visible.groupBy { YearMonth.from(it.date) }
            grouped.forEach { (ym, list) ->
                item(key = "header-$ym") {
                    val accrued = list.filter { it.kind != EntryKind.PAYMENT }.fold(Money.ZERO) { acc, e -> acc + (e.amount?.let { if (e.negative) -it else it } ?: Money.ZERO) }
                    val paid = list.filter { it.kind == EntryKind.PAYMENT }.fold(Money.ZERO) { acc, e -> acc + (e.amount ?: Money.ZERO) }
                    Column(Modifier.padding(horizontal = 8.dp)) {
                        SectionTitle(Formats.monthTitle(ym))
                        Text(
                            "Начислено ${money(accrued)} · получено ${money(paid)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 8.dp, bottom = 4.dp),
                        )
                    }
                }
                items(list, key = { "${it.kind}-${it.id}-${it.date}" }) { e ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable {
                                when (e.kind) {
                                    EntryKind.SHIFT -> onShift(e.id)
                                    EntryKind.ABSENCE -> onAbsence(e.id)
                                    EntryKind.ACCRUAL -> onAccrual(e.id)
                                    EntryKind.PAYMENT -> onPayment(e.id)
                                }
                            }
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(e.title, style = MaterialTheme.typography.bodyLarge)
                            Text("${Formats.date(e.date)} · ${e.subtitle}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        val color = when {
                            e.kind == EntryKind.PAYMENT -> MaterialTheme.colorScheme.primary
                            e.negative -> AppTheme.status.negative
                            else -> Color.Unspecified
                        }
                        Text(
                            e.amount?.let {
                                (
                                    if (e.negative) {
                                        "−"
                                    } else if (e.kind == EntryKind.PAYMENT) {
                                        ""
                                    } else {
                                        "+"
                                    }
                                ) + money(it)
                            } ?: "—",
                            color = color,
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                    ThinDivider()
                }
            }
        }
    }
}
