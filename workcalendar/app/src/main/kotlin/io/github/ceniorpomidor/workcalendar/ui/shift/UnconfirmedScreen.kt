package io.github.ceniorpomidor.workcalendar.ui.shift

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.ceniorpomidor.workcalendar.AppContainer
import io.github.ceniorpomidor.workcalendar.domain.util.Formats
import io.github.ceniorpomidor.workcalendar.ui.components.ConfirmDialog
import io.github.ceniorpomidor.workcalendar.ui.components.EmptyState
import io.github.ceniorpomidor.workcalendar.ui.components.LocalSnackbar
import io.github.ceniorpomidor.workcalendar.ui.components.SubScreen
import io.github.ceniorpomidor.workcalendar.ui.components.launchSafely
import io.github.ceniorpomidor.workcalendar.ui.components.money
import io.github.ceniorpomidor.workcalendar.ui.theme.AppIcons
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow

/** Shifts that have ended but have no hours; quick confirmation one by one or all at once. */
@Composable
fun UnconfirmedScreen(container: AppContainer, onOpen: (Long) -> Unit, onBack: (() -> Unit)?) {
    val snackbar = LocalSnackbar.current
    val scope = rememberCoroutineScope()
    val flow = remember {
        flow {
            while (true) {
                emit(container.clock.now())
                delay(30_000)
            }
        }.flatMapLatest { now -> container.shifts.observeUnconfirmed(now) }
    }
    val shifts by flow.collectAsStateWithLifecycle(initialValue = emptyList())
    val calc by container.calc.context.collectAsStateWithLifecycle(initialValue = null)
    var askAll by remember { mutableStateOf(false) }
    val loaded by produceState(false) {
        delay(600)
        value = true
    }

    SubScreen(title = "Неподтверждённые смены", onBack = onBack) { padding ->
        if (shifts.isEmpty()) {
            if (loaded) {
                EmptyState(
                    AppIcons.DoneAll,
                    "Все часы отмечены",
                    "Когда смена закончится, приложение напомнит внести отработанное время.",
                    modifier = Modifier.padding(padding),
                )
            }
            return@SubScreen
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Заработок по этим сменам не учитывается, пока часы не внесены. Неотмеченные смены не засчитываются автоматически.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (shifts.size > 1) {
                        FilledTonalButton(onClick = { askAll = true }, modifier = Modifier.fillMaxWidth()) {
                            Text("Отметить все по плану (${shifts.size})")
                        }
                    }
                }
            }
            items(shifts, key = { it.id }) { shift ->
                val zone = calc?.zone ?: java.time.ZoneId.systemDefault()
                val planned = shift.plannedPaidMinutes(zone)
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                    shape = RoundedCornerShape(18.dp),
                    onClick = { onOpen(shift.id) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(Formats.dayTitle(shift.date).replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.titleSmall)
                        Text("${Formats.timeRange(shift.plannedStart, shift.plannedEnd)} · план ${Formats.hours(planned)}", style = MaterialTheme.typography.bodyMedium)
                        calc?.pay?.estimate(shift)?.let {
                            Text("≈ ${money(it.total)} после подтверждения", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = {
                                scope.launchSafely(snackbar, success = "Отмечено: ${Formats.hours(planned)}") { container.shifts.confirmAsPlanned(shift.id) }
                            }) { Text("Полностью") }
                            TextButton(onClick = { onOpen(shift.id) }) { Text("Другое время") }
                        }
                    }
                }
            }
        }
    }
    if (askAll) {
        ConfirmDialog(
            title = "Отметить все смены по плану?",
            text = "${Formats.shifts(shifts.size)} будут отмечены как отработанные полностью. Любую из них можно исправить позже.",
            confirmText = "Отметить",
            onConfirm = {
                val ids = shifts.map { it.id }
                scope.launchSafely(snackbar, success = "Готово") { container.shifts.confirmAllAsPlanned(ids) }
            },
            onDismiss = { askAll = false },
        )
    }
}
