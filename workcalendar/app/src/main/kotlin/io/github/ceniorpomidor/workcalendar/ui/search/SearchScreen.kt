package io.github.ceniorpomidor.workcalendar.ui.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import io.github.ceniorpomidor.workcalendar.AppContainer
import io.github.ceniorpomidor.workcalendar.domain.export.CsvExport
import io.github.ceniorpomidor.workcalendar.domain.model.Money
import io.github.ceniorpomidor.workcalendar.domain.util.Formats
import io.github.ceniorpomidor.workcalendar.ui.components.EmptyState
import io.github.ceniorpomidor.workcalendar.ui.components.SectionTitle
import io.github.ceniorpomidor.workcalendar.ui.components.SubScreen
import io.github.ceniorpomidor.workcalendar.ui.components.ThinDivider
import io.github.ceniorpomidor.workcalendar.ui.theme.AppIcons
import kotlinx.coroutines.delay
import java.time.LocalDate

private sealed interface SearchResult {
    val title: String
    val subtitle: String

    data class Date(val date: LocalDate) : SearchResult {
        override val title get() = "Перейти к дате ${Formats.date(date)}"
        override val subtitle get() = Formats.dayTitle(date, withYear = true)
    }

    data class ShiftItem(val id: Long, override val title: String, override val subtitle: String) : SearchResult

    data class AbsenceItem(val id: Long, override val title: String, override val subtitle: String) : SearchResult

    data class PaymentItem(val id: Long, override val title: String, override val subtitle: String) : SearchResult

    data class AccrualItem(val id: Long, override val title: String, override val subtitle: String) : SearchResult

    data class NoteItem(val date: LocalDate, override val title: String, override val subtitle: String) : SearchResult
}

private fun parseDate(text: String, today: LocalDate): LocalDate? {
    val parts = text.trim().split('.', '/', '-').filter { it.isNotBlank() }
    return runCatching {
        when (parts.size) {
            3 -> {
                val year = parts[2].toInt().let { if (it < 100) 2000 + it else it }
                LocalDate.of(year, parts[1].toInt(), parts[0].toInt())
            }
            2 -> LocalDate.of(today.year, parts[1].toInt(), parts[0].toInt())
            else -> null
        }
    }.getOrNull()
}

/** Search in shift notes and titles, absences, payments, accruals, day notes; or jump to a date. */
@Composable
fun SearchScreen(
    container: AppContainer,
    onBack: (() -> Unit)?,
    onShift: (Long) -> Unit,
    onDate: (LocalDate) -> Unit,
    onAbsence: (Long) -> Unit,
    onPayment: (Long) -> Unit,
    onAccrual: (Long) -> Unit,
) {
    var query by rememberSaveable { mutableStateOf("") }
    var results by remember { mutableStateOf<List<Pair<String, List<SearchResult>>>>(emptyList()) }
    val focus = remember { FocusRequester() }
    LaunchedEffect(Unit) { runCatching { focus.requestFocus() } }
    LaunchedEffect(query) {
        val q = query.trim()
        if (q.length < 2) {
            results = emptyList()
            return@LaunchedEffect
        }
        delay(250)
        val groups = ArrayList<Pair<String, List<SearchResult>>>()
        parseDate(q, container.clock.today())?.let { groups += "Дата" to listOf(SearchResult.Date(it)) }
        val shifts = container.shifts.search(q).map {
            SearchResult.ShiftItem(
                it.id,
                "${Formats.date(it.date)} · ${Formats.timeRange(it.plannedStart, it.plannedEnd)}",
                listOf(it.title, it.note).filter { s ->
                    s.isNotBlank()
                }.joinToString(" · "),
            )
        }
        if (shifts.isNotEmpty()) groups += "Смены" to shifts
        val notes = container.db.miscDao().searchNotes("%$q%").map { SearchResult.NoteItem(it.date, Formats.date(it.date), it.text) }
        if (notes.isNotEmpty()) groups += "Заметки" to notes
        val absences = container.absences.search(q).map { SearchResult.AbsenceItem(it.id, it.displayTitle(), "${Formats.period(it.startDate, it.endDate)} ${it.note}") }
        if (absences.isNotEmpty()) groups += "Отсутствия" to absences
        val payments = container.finance.searchPayments(q).map {
            SearchResult.PaymentItem(it.id, "${CsvExport.paymentTypeName(it.type)} · ${Formats.money(it.amount)}", "${Formats.date(it.date)} ${it.note}")
        }
        if (payments.isNotEmpty()) groups += "Выплаты" to payments
        val accruals = container.finance.searchAccruals(q).map {
            SearchResult.AccrualItem(it.id, "${it.title} · ${Formats.money(it.amount)}", "${Formats.date(it.date)} ${it.note}")
        }
        if (accruals.isNotEmpty()) groups += "Начисления" to accruals
        // Amount search: payments and accruals with the same sum.
        Money.parse(q)?.takeIf { it.isPositive }?.let { amount ->
            val byAmount = container.finance.getPayments().filter { it.amount == amount }.map {
                SearchResult.PaymentItem(it.id, "${CsvExport.paymentTypeName(it.type)} · ${Formats.money(it.amount)}", Formats.date(it.date))
            }
            if (byAmount.isNotEmpty()) groups += "Выплаты на сумму ${Formats.money(amount)}" to byAmount
        }
        results = groups
    }
    SubScreen(title = "Поиск", onBack = onBack) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(16.dp)) {
            item {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text("Заметка, название, сумма или дата (дд.мм.гггг)") },
                    leadingIcon = { Icon(AppIcons.Search, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().focusRequester(focus),
                )
            }
            if (query.trim().length >= 2 && results.isEmpty()) {
                item { EmptyState(AppIcons.Search, "Ничего не найдено") }
            }
            results.forEach { (group, items) ->
                item(key = "g-$group") { SectionTitle(group) }
                items(items) { r ->
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .clickable {
                                when (r) {
                                    is SearchResult.Date -> onDate(r.date)
                                    is SearchResult.ShiftItem -> onShift(r.id)
                                    is SearchResult.AbsenceItem -> onAbsence(r.id)
                                    is SearchResult.PaymentItem -> onPayment(r.id)
                                    is SearchResult.AccrualItem -> onAccrual(r.id)
                                    is SearchResult.NoteItem -> onDate(r.date)
                                }
                            }
                            .padding(vertical = 10.dp, horizontal = 8.dp),
                    ) {
                        Text(r.title, style = MaterialTheme.typography.bodyLarge)
                        if (r.subtitle.isNotBlank()) Text(r.subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    ThinDivider()
                }
            }
        }
    }
}
