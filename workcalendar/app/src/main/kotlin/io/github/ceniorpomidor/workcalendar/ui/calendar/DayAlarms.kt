package io.github.ceniorpomidor.workcalendar.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import io.github.ceniorpomidor.workcalendar.domain.alarm.AlarmOfDay
import io.github.ceniorpomidor.workcalendar.domain.alarm.AlarmTexts
import io.github.ceniorpomidor.workcalendar.domain.model.AlarmRepeat
import io.github.ceniorpomidor.workcalendar.domain.util.Formats
import io.github.ceniorpomidor.workcalendar.ui.theme.AppIcons
import java.time.LocalDate

/** Alarms of the selected day and what can be done with them in the day panel. */
class DayAlarmsUi(
    val alarms: List<AlarmOfDay>,
    /** Time offered for a new alarm on this day, minute of the day. */
    val suggestedMinute: Int,
    /** Switches an alarm on or off for this day only. */
    val onToggle: (AlarmOfDay, Boolean) -> Unit,
    val onEdit: (Long) -> Unit,
    val onAdd: (minute: Int) -> Unit,
)

@Composable
fun DayAlarms(date: LocalDate, ui: DayAlarmsUi) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        for (alarm in ui.alarms) {
            val ring = alarm.ring
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    .clickable { ui.onEdit(ring.alarm.id) }
                    .padding(start = 12.dp, end = 8.dp, top = 6.dp, bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    if (alarm.on) AppIcons.Alarm else AppIcons.AlarmOff,
                    contentDescription = null,
                    tint = if (alarm.on) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        "Будильник ${Formats.time(ring.at)}" + if (ring.at.toLocalDate() != date) " накануне" else "",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    val once = ring.alarm.repeat == AlarmRepeat.ONCE
                    val label = ring.alarm.label.trim().ifBlank { null }
                    Text(
                        when {
                            !alarm.on && once -> "Выключен"
                            !alarm.on -> "Выключен в этот день"
                            once -> listOfNotNull("Только в этот день", label).joinToString(" · ")
                            else -> listOfNotNull(AlarmTexts.repeat(ring.alarm), label).joinToString(" · ")
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(checked = alarm.on, onCheckedChange = { ui.onToggle(alarm, it) })
            }
        }
        AssistChip(
            onClick = { ui.onAdd(ui.suggestedMinute) },
            label = { Text("Будильник на этот день") },
            leadingIcon = { Icon(AppIcons.AlarmAdd, contentDescription = null, modifier = Modifier.size(18.dp)) },
        )
    }
}
