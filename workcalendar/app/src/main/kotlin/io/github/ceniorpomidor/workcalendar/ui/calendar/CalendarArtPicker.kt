package io.github.ceniorpomidor.workcalendar.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.ceniorpomidor.workcalendar.domain.model.CalendarArt
import io.github.ceniorpomidor.workcalendar.ui.theme.AppIcons

@Composable
internal fun CalendarArtPicker(current: CalendarArt, onDismiss: () -> Unit, onSelect: (CalendarArt) -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Тема календаря") },
        text = {
            Column(
                Modifier.fillMaxWidth().selectableGroup().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                for (themes in CalendarArt.entries.chunked(2)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        for (theme in themes) {
                            CalendarThemeOption(theme, current == theme, Modifier.weight(1f)) { onSelect(theme) }
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Закрыть") } },
    )
}

@Composable
private fun CalendarThemeOption(art: CalendarArt, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val style = calendarStyle(art)
    val colors = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(14.dp)
    Column(
        modifier
            .clip(shape)
            .background(colors.surface)
            .border(if (selected) 2.dp else 1.dp, if (selected) colors.primary else colors.outlineVariant, shape)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .padding(5.dp),
    ) {
        Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).clearAndSetSemantics { }) {
            CalendarThemeSample(style)
            if (selected) {
                Box(
                    Modifier.align(Alignment.TopEnd).padding(3.dp).size(16.dp).background(colors.primary, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(AppIcons.Check, contentDescription = null, tint = colors.onPrimary, modifier = Modifier.size(12.dp))
                }
            }
        }
        Box(
            Modifier.fillMaxWidth().heightIn(min = 34.dp).padding(horizontal = 3.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            Text(art.title, style = MaterialTheme.typography.labelMedium, color = colors.onSurface)
        }
    }
}

@Composable
private fun CalendarThemeSample(style: CalendarStyle) {
    Column(Modifier.fillMaxWidth().calendarBackdrop(style).padding(4.dp)) {
        Text(
            "Ноябрь",
            style = style.titleStyle.copy(fontSize = if (style.art == CalendarArt.OFFICE) 20.sp else 13.sp),
            color = style.ink,
            maxLines = 1,
        )
        Row(Modifier.fillMaxWidth().calendarSheet(style).padding(vertical = 3.dp)) {
            val marks = listOf(CalendarMark.SHIFT, CalendarMark.CONFIRMED, CalendarMark.VACATION)
            for ((index, mark) in marks.withIndex()) {
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    CalendarDate(
                        number = 17 + index,
                        style = style,
                        mark = mark,
                        variant = index,
                        today = false,
                        holiday = false,
                        small = false,
                        modifier = Modifier.fillMaxWidth().height(22.dp),
                        joinsLeft = index == 1,
                        joinsRight = index == 0,
                    )
                    if (index == 2) PayoutTag("А", received = false, style = style)
                    else CalendarMarkTag(mark, style, symbolOnly = true)
                }
            }
        }
    }
}
