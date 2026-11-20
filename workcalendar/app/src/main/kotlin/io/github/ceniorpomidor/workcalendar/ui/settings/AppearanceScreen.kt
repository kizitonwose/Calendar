package io.github.ceniorpomidor.workcalendar.ui.settings

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.ceniorpomidor.workcalendar.AppContainer
import io.github.ceniorpomidor.workcalendar.domain.color.ThemeColors
import io.github.ceniorpomidor.workcalendar.domain.model.AppSettings
import io.github.ceniorpomidor.workcalendar.domain.model.ColorPalette
import io.github.ceniorpomidor.workcalendar.domain.model.NavItem
import io.github.ceniorpomidor.workcalendar.domain.model.NavLabels
import io.github.ceniorpomidor.workcalendar.domain.model.ThemeMode
import io.github.ceniorpomidor.workcalendar.domain.util.Formats
import io.github.ceniorpomidor.workcalendar.ui.components.ScrollColumn
import io.github.ceniorpomidor.workcalendar.ui.components.SectionCard
import io.github.ceniorpomidor.workcalendar.ui.components.SubScreen
import io.github.ceniorpomidor.workcalendar.ui.components.SwitchRow
import io.github.ceniorpomidor.workcalendar.ui.components.ThinDivider
import io.github.ceniorpomidor.workcalendar.ui.navigation.info
import io.github.ceniorpomidor.workcalendar.ui.navigation.title
import io.github.ceniorpomidor.workcalendar.ui.theme.AppIcons
import java.time.DayOfWeek
import kotlin.math.roundToInt

private val paletteNames = listOf(
    ColorPalette.BLUE to "Синий",
    ColorPalette.INDIGO to "Индиго",
    ColorPalette.VIOLET to "Фиолетовый",
    ColorPalette.PINK to "Розовый",
    ColorPalette.RED to "Красный",
    ColorPalette.ORANGE to "Оранжевый",
    ColorPalette.AMBER to "Янтарный",
    ColorPalette.OLIVE to "Оливковый",
    ColorPalette.GREEN to "Зелёный",
    ColorPalette.TEAL to "Бирюзовый",
    ColorPalette.GRAPHITE to "Графит",
    ColorPalette.CUSTOM to "Свой",
)

private val rainbow = Brush.sweepGradient(listOf(Color.Red, Color.Yellow, Color.Green, Color.Cyan, Color.Blue, Color.Magenta, Color.Red))

@Composable
fun AppearanceScreen(container: AppContainer, settings: AppSettings, onBack: () -> Unit) {
    val update = rememberSettingsUpdater(container)
    val c = settings.calendar
    SubScreen(title = "Оформление", onBack = onBack) { padding ->
        ScrollColumn(padding) {
            SectionCard(title = "Тема", icon = AppIcons.Contrast) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(ThemeMode.SYSTEM to "Как в системе", ThemeMode.LIGHT to "Светлая", ThemeMode.DARK to "Тёмная").forEach { (mode, label) ->
                        FilterChip(selected = settings.theme == mode, onClick = { update { it.copy(theme = mode) } }, label = { Text(label) })
                    }
                }
                SwitchRow("Чёрный фон в тёмной теме", "Экономит заряд на OLED-экранах", settings.appearance.pureBlack) { v ->
                    update { it.copy(appearance = it.appearance.copy(pureBlack = v)) }
                }
            }
            SectionCard(title = "Цветовая палитра", icon = AppIcons.Palette) {
                PalettePicker(settings, update)
            }
            SectionCard(title = "Нижнее меню", icon = AppIcons.Tune) {
                NavItemsEditor(settings, update)
            }
            SectionCard(title = "Календарь", icon = AppIcons.CalendarMonth) {
                Text("Первый день недели", style = MaterialTheme.typography.bodyMedium)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(DayOfWeek.MONDAY, DayOfWeek.SUNDAY, DayOfWeek.SATURDAY).forEach { day ->
                        FilterChip(
                            selected = c.firstDayOfWeek == day,
                            onClick = { update { it.copy(calendar = it.calendar.copy(firstDayOfWeek = day)) } },
                            label = { Text(Formats.weekdayFull(day).replaceFirstChar { ch -> ch.uppercase() }) },
                        )
                    }
                }
                SwitchRow("Время смен в ячейках", null, c.showShiftTimes) { v -> update { it.copy(calendar = it.calendar.copy(showShiftTimes = v)) } }
                SwitchRow("Отработанные часы после подтверждения", null, c.showWorkedHours) { v -> update { it.copy(calendar = it.calendar.copy(showWorkedHours = v)) } }
                SwitchRow("Заработок за день в ячейках", null, c.showDayEarnings) { v -> update { it.copy(calendar = it.calendar.copy(showDayEarnings = v)) } }
                SwitchRow("Выделять праздники", null, c.showHolidays) { v -> update { it.copy(calendar = it.calendar.copy(showHolidays = v)) } }
                SwitchRow("Сводка месяца над календарём", null, c.showMonthSummary) { v -> update { it.copy(calendar = it.calendar.copy(showMonthSummary = v)) } }
            }
        }
    }
}

@Composable
private fun PalettePicker(settings: AppSettings, update: ((AppSettings) -> AppSettings) -> Unit) {
    val dark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val context = LocalContext.current
    val dynamicAvailable = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val dynamic = settings.dynamicColor && dynamicAvailable
    val appearance = settings.appearance
    val swatches = remember(dark, appearance.customHue) {
        paletteNames.associate { (palette, _) -> palette to Color(ThemeColors.swatch(palette, appearance.customHue, dark)) }
    }
    FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        if (dynamicAvailable) {
            val wallpaper = if (dark) dynamicDarkColorScheme(context).primary else dynamicLightColorScheme(context).primary
            Swatch("Обои", Modifier.background(wallpaper), selected = dynamic, dark = dark, icon = AppIcons.Wallpaper) {
                update { it.copy(dynamicColor = true) }
            }
        }
        for ((palette, name) in paletteNames) {
            val selected = !dynamic && appearance.palette == palette
            val fill = if (palette == ColorPalette.CUSTOM && !selected) Modifier.background(rainbow) else Modifier.background(swatches.getValue(palette))
            Swatch(name, fill, selected = selected, dark = dark) {
                update { it.copy(dynamicColor = false, appearance = it.appearance.copy(palette = palette)) }
            }
        }
    }
    if (dynamicAvailable) {
        Text(
            "«Обои» — цвета Material You из обоев телефона. Остальные палитры одинаково выглядят в светлой и тёмной теме.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
    if (!dynamic && appearance.palette == ColorPalette.CUSTOM) {
        var hue by remember(appearance.customHue) { mutableFloatStateOf(appearance.customHue.toFloat()) }
        val hues = remember(dark) { (0..360 step 30).map { Color(ThemeColors.swatch(ColorPalette.CUSTOM, it, dark)) } }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Оттенок", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
            Box(Modifier.size(24.dp).clip(CircleShape).background(Color(ThemeColors.swatch(ColorPalette.CUSTOM, hue.roundToInt(), dark))))
        }
        Box(Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)).background(Brush.horizontalGradient(hues)))
        Slider(
            value = hue,
            onValueChange = { hue = it },
            valueRange = 0f..359f,
            onValueChangeFinished = { update { it.copy(appearance = it.appearance.copy(customHue = hue.roundToInt())) } },
        )
    }
}

@Composable
private fun Swatch(label: String, fill: Modifier, selected: Boolean, dark: Boolean, icon: ImageVector? = null, onClick: () -> Unit) {
    val mark = if (dark) Color(0xFF111318) else Color.White
    Column(
        Modifier
            .width(68.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            Modifier
                .size(44.dp)
                .clip(CircleShape)
                .then(fill)
                .then(if (selected) Modifier.border(3.dp, MaterialTheme.colorScheme.onSurface, CircleShape) else Modifier),
            contentAlignment = Alignment.Center,
        ) {
            when {
                selected -> Icon(AppIcons.Check, contentDescription = "Выбрано", tint = mark)
                icon != null -> Icon(icon, contentDescription = null, tint = mark)
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun NavItemsEditor(settings: AppSettings, update: ((AppSettings) -> AppSettings) -> Unit) {
    val items = NavItem.normalize(settings.appearance.navItems)
    val labels = settings.appearance.navLabels
    fun save(list: List<NavItem>) = update { it.copy(appearance = it.appearance.copy(navItems = NavItem.normalize(list))) }

    // Preview of the bar as it will look.
    NavigationBar(windowInsets = WindowInsets(0), modifier = Modifier.clip(RoundedCornerShape(16.dp))) {
        items.forEachIndexed { index, item ->
            NavigationBarItem(
                selected = index == 0,
                onClick = {},
                icon = { Icon(if (index == 0) item.info.selectedIcon else item.info.icon, contentDescription = null) },
                label = if (labels == NavLabels.NEVER) null else ({ Text(item.info.label, maxLines = 1, overflow = TextOverflow.Ellipsis) }),
                alwaysShowLabel = labels == NavLabels.ALWAYS,
            )
        }
    }
    Text(
        "До ${NavItem.MAX} пунктов. Календарь и настройки убрать нельзя, но их можно переставить.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    items.forEachIndexed { index, item ->
        NavItemRow(
            item = item,
            checked = true,
            enabled = item !in NavItem.REQUIRED,
            onToggle = { save(items - item) },
            onUp = if (index > 0) ({ save(items.toMutableList().apply { add(index - 1, removeAt(index)) }) }) else null,
            onDown = if (index < items.lastIndex) ({ save(items.toMutableList().apply { add(index + 1, removeAt(index)) }) }) else null,
        )
    }
    val rest = NavItem.entries.filter { it !in items }
    if (rest.isNotEmpty()) {
        ThinDivider()
        for (item in rest) {
            NavItemRow(
                item = item,
                checked = false,
                enabled = items.size < NavItem.MAX,
                onToggle = {
                    // A new item goes before the settings when they close the bar.
                    val list = items.toMutableList()
                    list.add(if (list.lastOrNull() == NavItem.SETTINGS) list.lastIndex else list.size, item)
                    save(list)
                },
            )
        }
    }
    Text("Подписи пунктов", style = MaterialTheme.typography.bodyMedium)
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf(NavLabels.ALWAYS to "Всегда", NavLabels.SELECTED to "У выбранного", NavLabels.NEVER to "Без подписей").forEach { (mode, label) ->
            FilterChip(
                selected = labels == mode,
                onClick = { update { it.copy(appearance = it.appearance.copy(navLabels = mode)) } },
                label = { Text(label) },
            )
        }
    }
}

@Composable
private fun NavItemRow(item: NavItem, checked: Boolean, enabled: Boolean, onToggle: () -> Unit, onUp: (() -> Unit)? = null, onDown: (() -> Unit)? = null) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .toggleable(value = checked, enabled = enabled, role = Role.Checkbox, onValueChange = { onToggle() }),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(checked = checked, onCheckedChange = null, enabled = enabled, modifier = Modifier.padding(12.dp))
        Icon(item.info.icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.width(12.dp))
        Text(
            item.title,
            style = MaterialTheme.typography.bodyLarge,
            color = if (checked || enabled) Color.Unspecified else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        if (checked) {
            IconButton(onClick = { onUp?.invoke() }, enabled = onUp != null) { Icon(AppIcons.ExpandLess, contentDescription = "Выше") }
            IconButton(onClick = { onDown?.invoke() }, enabled = onDown != null) { Icon(AppIcons.ExpandMore, contentDescription = "Ниже") }
        }
    }
}
