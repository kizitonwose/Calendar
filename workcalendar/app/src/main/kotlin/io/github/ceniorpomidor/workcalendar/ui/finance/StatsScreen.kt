package io.github.ceniorpomidor.workcalendar.ui.finance

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.ceniorpomidor.workcalendar.AppContainer
import io.github.ceniorpomidor.workcalendar.domain.model.Money
import io.github.ceniorpomidor.workcalendar.domain.pay.PeriodSummary
import io.github.ceniorpomidor.workcalendar.domain.time.DateRange
import io.github.ceniorpomidor.workcalendar.domain.util.Formats
import io.github.ceniorpomidor.workcalendar.ui.components.ScrollColumn
import io.github.ceniorpomidor.workcalendar.ui.components.SectionCard
import io.github.ceniorpomidor.workcalendar.ui.components.SubScreen
import io.github.ceniorpomidor.workcalendar.ui.components.ThinDivider
import io.github.ceniorpomidor.workcalendar.ui.components.ValueRow
import io.github.ceniorpomidor.workcalendar.ui.components.money
import io.github.ceniorpomidor.workcalendar.ui.theme.AppIcons
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import java.time.YearMonth

/** Monthly comparison of earnings and hours, plan versus fact, averages and counts. */
@Composable
fun StatsScreen(container: AppContainer, onBack: (() -> Unit)?) {
    var monthsCount by rememberSaveable { mutableStateOf(6) }
    val current = YearMonth.from(container.clock.today())
    val data by remember {
        snapshotFlow { monthsCount }.flatMapLatest { count ->
            val months = (count - 1 downTo 0).map { current.minusMonths(it.toLong()) }
            val range = DateRange(months.first().atDay(1), months.last().atEndOfMonth())
            combine(
                container.shifts.observeRange(range),
                container.absences.observeRange(range),
                container.finance.accruals,
                container.calc.context,
            ) { shifts, absences, accruals, calc ->
                months.zip(calc.summaries.monthly(months, shifts, absences, accruals, container.clock.now()))
            }
        }
    }.collectAsStateWithLifecycle(initialValue = emptyList())

    SubScreen(title = "Статистика", onBack = onBack) { padding ->
        ScrollColumn(padding) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(3, 6, 12).forEach { n ->
                    FilterChip(selected = monthsCount == n, onClick = { monthsCount = n }, label = { Text("$n мес.") })
                }
            }
            if (data.isEmpty()) {
                Text("Загрузка…")
                return@ScrollColumn
            }
            val primary = MaterialTheme.colorScheme.primary
            SectionCard(title = "Заработок: факт и план", icon = AppIcons.BarChart) {
                BarChart(
                    labels = data.map { Formats.monthShort(it.first.month) },
                    actual = data.map { it.second.grossAccrued.toRubles() },
                    planned = data.map { (it.second.plannedEarnings ?: Money.ZERO).toRubles() },
                    color = primary,
                    valueLabel = { Formats.money(Money.ofRubles(it.toLong()), "") },
                )
                Legend(primary)
            }
            SectionCard(title = "Часы: факт и план", icon = AppIcons.Schedule) {
                BarChart(
                    labels = data.map { Formats.monthShort(it.first.month) },
                    actual = data.map { it.second.workedMinutes / 60.0 },
                    planned = data.map { it.second.plannedMinutes / 60.0 },
                    color = MaterialTheme.colorScheme.tertiary,
                    valueLabel = { "${it.toLong()} ч" },
                )
                Legend(MaterialTheme.colorScheme.tertiary)
            }
            val summaries = data.map { it.second }
            SectionCard(title = "Итого за ${data.size} мес.", icon = AppIcons.Insights) {
                val total = summaries.fold(Money.ZERO) { a, s -> a + s.grossAccrued }
                val work = summaries.fold(Money.ZERO) { a, s -> a + s.confirmedEarnings }
                val minutes = summaries.sumOf { it.workedMinutes }
                val shifts = summaries.sumOf { it.workedShifts }
                ValueRow("Начислено всего", money(total), emphasize = true)
                ValueRow("  за работу", money(work))
                ValueRow("  отпускные", money(summaries.fold(Money.ZERO) { a, s -> a + s.vacationPay }))
                ValueRow("  больничные", money(summaries.fold(Money.ZERO) { a, s -> a + s.sickPay }))
                ValueRow("  доп. начисления − удержания", money(summaries.fold(Money.ZERO) { a, s -> a + s.bonuses - s.deductions }))
                ValueRow("В среднем за месяц", money(total.scale(1, data.size.toLong())))
                if (shifts > 0) ValueRow("Средний заработок за смену", money(work.scale(1, shifts.toLong())))
                if (minutes > 0) ValueRow("Средний заработок за час", money(work.scale(60, minutes)))
                ThinDivider()
                ValueRow("Отработано смен", shifts.toString())
                ValueRow("Пропущено смен", summaries.sumOf { it.missedShifts }.toString())
                ValueRow("Отменено смен", summaries.sumOf { it.cancelledShifts }.toString())
                ValueRow("Выходных дней", summaries.sumOf { it.daysOff }.toString())
                ValueRow("Отработано часов", Formats.hours(minutes))
                ValueRow("Доп. и сверхурочные часы", Formats.hours(summaries.sumOf { it.extraMinutes }))
            }
            SectionCard(title = "По месяцам", icon = AppIcons.Receipt) {
                data.reversed().forEach { (month, s) -> MonthLine(month, s) }
            }
        }
    }
}

@Composable
private fun MonthLine(month: YearMonth, s: PeriodSummary) {
    Column(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row {
            Text(Formats.monthTitle(month), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
            Text(money(s.grossAccrued), style = MaterialTheme.typography.bodyLarge)
        }
        val plan = s.plannedEarnings?.let { " · план ${money(it)}" } ?: ""
        Text(
            "${Formats.hours(s.workedMinutes)} из ${Formats.hours(s.plannedMinutes)} · ${Formats.shifts(s.workedShifts)}$plan",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun Legend(color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(Modifier.size(12.dp).clip(RoundedCornerShape(3.dp)).padding(0.dp)) { Canvas(Modifier.size(12.dp)) { drawRect(color) } }
        Text("Факт", style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.width(8.dp))
        Box(Modifier.size(12.dp).clip(RoundedCornerShape(3.dp))) { Canvas(Modifier.size(12.dp)) { drawRect(color.copy(alpha = 0.28f)) } }
        Text("План", style = MaterialTheme.typography.bodySmall)
    }
}

/** Grouped bar chart: planned (light) and actual (solid) values per label. */
@Composable
private fun BarChart(labels: List<String>, actual: List<Double>, planned: List<Double>, color: Color, valueLabel: (Double) -> String) {
    val max = (actual + planned).maxOrNull()?.takeIf { it > 0 } ?: 1.0
    val gridColor = MaterialTheme.colorScheme.outlineVariant
    Column {
        Text("макс. ${valueLabel(max)}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Canvas(Modifier.fillMaxWidth().height(160.dp).padding(top = 4.dp)) {
            val n = labels.size.coerceAtLeast(1)
            val groupWidth = size.width / n
            val barWidth = groupWidth * 0.32f
            for (i in 1..3) {
                val y = size.height * i / 4f
                drawLine(gridColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
            }
            for (i in 0 until n) {
                val x0 = i * groupWidth + groupWidth * 0.15f
                val plannedH = (planned.getOrElse(i) { 0.0 } / max * size.height).toFloat()
                val actualH = (actual.getOrElse(i) { 0.0 } / max * size.height).toFloat()
                drawRoundRect(color.copy(alpha = 0.28f), Offset(x0, size.height - plannedH), Size(barWidth, plannedH), CornerRadius(6f, 6f))
                drawRoundRect(color, Offset(x0 + barWidth + groupWidth * 0.04f, size.height - actualH), Size(barWidth, actualH), CornerRadius(6f, 6f))
            }
        }
        Row(Modifier.fillMaxWidth()) {
            labels.forEach { Text(it, style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(1f), textAlign = TextAlign.Center) }
        }
    }
}
