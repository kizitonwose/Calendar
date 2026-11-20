package io.github.ceniorpomidor.workcalendar.ui.onboarding

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.ceniorpomidor.workcalendar.AppContainer
import io.github.ceniorpomidor.workcalendar.data.repo.ApplyRequest
import io.github.ceniorpomidor.workcalendar.domain.model.Money
import io.github.ceniorpomidor.workcalendar.domain.model.PayoutKind
import io.github.ceniorpomidor.workcalendar.domain.model.RatePeriod
import io.github.ceniorpomidor.workcalendar.domain.model.ScheduleTemplate
import io.github.ceniorpomidor.workcalendar.domain.schedule.ScheduleGenerator
import io.github.ceniorpomidor.workcalendar.domain.schedule.SchedulePresets
import io.github.ceniorpomidor.workcalendar.domain.time.DateRange
import io.github.ceniorpomidor.workcalendar.domain.time.HolidayCalendar
import io.github.ceniorpomidor.workcalendar.domain.time.TimeMath
import io.github.ceniorpomidor.workcalendar.domain.util.Formats
import io.github.ceniorpomidor.workcalendar.ui.components.DateField
import io.github.ceniorpomidor.workcalendar.ui.components.LocalSnackbar
import io.github.ceniorpomidor.workcalendar.ui.components.NumberField
import io.github.ceniorpomidor.workcalendar.ui.components.SectionCard
import io.github.ceniorpomidor.workcalendar.ui.components.StepperField
import io.github.ceniorpomidor.workcalendar.ui.components.SwitchRow
import io.github.ceniorpomidor.workcalendar.ui.components.TimeField
import io.github.ceniorpomidor.workcalendar.ui.components.launchSafely
import io.github.ceniorpomidor.workcalendar.ui.theme.AppIcons
import java.time.LocalDate

private const val STEPS = 5

/** First start: employment date, rate, schedule, paydays and notification permission. */
@Composable
fun OnboardingScreen(container: AppContainer, onDone: () -> Unit) {
    val snackbar = LocalSnackbar.current
    val scope = rememberCoroutineScope()
    val today = container.clock.today()
    var step by rememberSaveable { mutableStateOf(0) }
    var employment by rememberSaveable { mutableStateOf<LocalDate?>(null) }
    var priorYears by rememberSaveable { mutableStateOf(0) }
    var rateText by rememberSaveable { mutableStateOf("") }
    var preset by rememberSaveable { mutableStateOf("5x2") }
    var startMinute by rememberSaveable { mutableStateOf(9 * 60) }
    var endMinute by rememberSaveable { mutableStateOf(18 * 60) }
    var breakMinutes by rememberSaveable { mutableStateOf(60) }
    var anchor by rememberSaveable { mutableStateOf(today) }
    var applyFrom by rememberSaveable { mutableStateOf(today) }
    var useSchedule by rememberSaveable { mutableStateOf(true) }
    var advanceDay by rememberSaveable { mutableStateOf(25) }
    var salaryDay by rememberSaveable { mutableStateOf(10) }
    var payoutNotify by rememberSaveable { mutableStateOf(true) }
    var saving by remember { mutableStateOf(false) }
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }

    val duration = run {
        var d = endMinute - startMinute
        if (d <= 0) d += TimeMath.MINUTES_PER_DAY
        d
    }
    val fullDay = preset == "1x3" || preset == "1x2"
    val pattern = runCatching {
        SchedulePresets.create(preset, anchor, startMinute, if (fullDay) 24 * 60 else duration, breakMinutes)
    }.getOrNull()
    val rate = Money.parse(rateText)?.takeIf { it.isPositive }

    fun finish() {
        saving = true
        scope.launchSafely(snackbar, onSuccess = onDone) {
            try {
                container.settings.update {
                    it.copy(
                        employmentStartDate = employment,
                        priorExperienceMonths = priorYears * 12,
                        trackingStartDate = if (useSchedule) applyFrom else today,
                    )
                }
                if (rate != null) {
                    val from = listOfNotNull(employment, applyFrom.withDayOfMonth(1)).minOrNull() ?: today
                    container.finance.saveRate(RatePeriod(effectiveFrom = from, hourlyRate = rate))
                }
                container.finance.ensureDefaultRules()
                for (rule in container.finance.getRules()) {
                    val updated = when (rule.kind) {
                        PayoutKind.ADVANCE -> rule.copy(payDay = advanceDay, notify = payoutNotify)
                        PayoutKind.SALARY -> rule.copy(payDay = salaryDay, notify = payoutNotify)
                        PayoutKind.OTHER -> rule
                    }
                    if (updated != rule) container.finance.saveRule(updated)
                }
                if (useSchedule && pattern != null) {
                    val name = SchedulePresets.all.firstOrNull { it.id == preset }?.name ?: "Мой график"
                    val id = container.schedule.saveTemplate(ScheduleTemplate(name = name, pattern = pattern))
                    val preview = container.schedule.preview(ApplyRequest(id, name, pattern, applyFrom, null, overwriteUserChanges = false))
                    container.schedule.apply(preview)
                }
                container.settings.update { it.copy(onboardingDone = true) }
            } finally {
                saving = false
            }
        }
    }

    Surface(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().imePadding().padding(16.dp)) {
            LinearProgressIndicator(progress = { (step + 1f) / STEPS }, modifier = Modifier.fillMaxWidth())
            Column(
                Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(top = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                when (step) {
                    0 -> {
                        Icon(AppIcons.CalendarMonth, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(56.dp))
                        Text("Рабочий календарь", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                        Text(
                            "Планируйте смены, отмечайте отработанные часы, следите за заработком, авансом и зарплатой. Всё хранится только на телефоне.",
                            style = MaterialTheme.typography.bodyLarge,
                        )
                        SectionCard(title = "О работе") {
                            DateField("Дата трудоустройства", employment, { employment = it }, supportingText = "Нужна для стажа и расчёта отпускных/больничных. Можно указать позже")
                            StepperField("Стаж до этой работы, лет", priorYears, { priorYears = it }, 0..60)
                        }
                    }
                    1 -> {
                        Text("Почасовая ставка", style = MaterialTheme.typography.headlineSmall)
                        Text("Заработок считается по подтверждённым часам. Ставку можно менять с любой даты, прошлые смены сохранят прежнюю.", style = MaterialTheme.typography.bodyMedium)
                        NumberField("Ставка в час", rateText, { rateText = it }, suffix = "₽", isError = rateText.isNotBlank() && rate == null)
                        Text(
                            "Надбавки за праздники и сверхурочные можно задать позже в Настройки → Ставки.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    2 -> {
                        Text("График работы", style = MaterialTheme.typography.headlineSmall)
                        SwitchRow("Заполнить календарь по графику", "Смены будут созданы автоматически", useSchedule) { useSchedule = it }
                        if (useSchedule) {
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                SchedulePresets.all.filter { it.id != "custom" }.forEach { p ->
                                    FilterChip(selected = preset == p.id, onClick = {
                                        preset = p.id
                                        when (p.id) {
                                            "1x3", "1x2" -> {
                                                startMinute = 8 * 60
                                                breakMinutes = 120
                                            }
                                            "day-night" -> {
                                                startMinute = 8 * 60
                                                breakMinutes = 60
                                            }
                                            "2x2", "3x3" -> {
                                                startMinute = 8 * 60
                                                endMinute = 20 * 60
                                                breakMinutes = 60
                                            }
                                            else -> {
                                                startMinute = 9 * 60
                                                endMinute = 18 * 60
                                                breakMinutes = 60
                                            }
                                        }
                                    }, label = { Text(p.name) })
                                }
                            }
                            Text(SchedulePresets.all.first { it.id == preset }.description, style = MaterialTheme.typography.bodySmall)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                TimeField("Начало", startMinute, { startMinute = it }, modifier = Modifier.weight(1f))
                                if (!fullDay && preset != "day-night") {
                                    TimeField("Окончание", endMinute, { endMinute = it }, modifier = Modifier.weight(1f))
                                }
                            }
                            NumberField("Перерыв, мин", breakMinutes.toString(), { t -> breakMinutes = t.filter { it.isDigit() }.toIntOrNull() ?: 0 }, decimal = false)
                            if (preset != "5x2") {
                                DateField("Первый рабочий день цикла", anchor, { anchor = it })
                            }
                            DateField("Заполнять календарь с", applyFrom, { applyFrom = it })
                            pattern?.let { p ->
                                val shifts = ScheduleGenerator.generate(p, DateRange(applyFrom, applyFrom.plusDays(6)), HolidayCalendar()).associateBy { it.date }
                                Text("Ближайшая неделя:", style = MaterialTheme.typography.labelLarge)
                                for (i in 0..6) {
                                    val d = applyFrom.plusDays(i.toLong())
                                    val s = shifts[d]
                                    Text(
                                        "${Formats.weekdayShort(d.dayOfWeek)} ${Formats.shortDate(d)} — ${s?.let { Formats.timeRange(it.start, it.end) } ?: "выходной"}",
                                        style = MaterialTheme.typography.bodySmall,
                                    )
                                }
                            }
                        }
                    }
                    3 -> {
                        Text("Аванс и зарплата", style = MaterialTheme.typography.headlineSmall)
                        Text(
                            "Обычно аванс платят за первую половину месяца, а зарплату — за весь месяц в следующем месяце. Правила можно настроить подробнее позже.",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        StepperField("День аванса", advanceDay, { advanceDay = it }, 1..31)
                        StepperField("День зарплаты (следующего месяца)", salaryDay, { salaryDay = it }, 1..31)
                        SwitchRow("Напоминать о выплатах за день", "С расчётной суммой", payoutNotify) { payoutNotify = it }
                    }
                    else -> {
                        Icon(AppIcons.NotificationsActive, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(48.dp))
                        Text("Уведомления", style = MaterialTheme.typography.headlineSmall)
                        Text(
                            "После окончания смены приложение предложит отметить часы — прямо из уведомления. Также напомнит об авансе, зарплате, отпускных и больничных.",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            OutlinedButton(onClick = { permission.launch(Manifest.permission.POST_NOTIFICATIONS) }) { Text("Разрешить уведомления") }
                        }
                    }
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (step > 0) {
                    OutlinedButton(onClick = { step-- }) { Text("Назад") }
                } else {
                    TextButton(onClick = {
                        scope.launchSafely(snackbar, onSuccess = onDone) {
                            container.finance.ensureDefaultRules()
                            container.settings.update { it.copy(onboardingDone = true, trackingStartDate = today) }
                        }
                    }) { Text("Пропустить") }
                }
                Spacer(Modifier.weight(1f))
                if (step < STEPS - 1) {
                    Button(onClick = { step++ }, enabled = step != 2 || !useSchedule || pattern != null) { Text("Далее") }
                } else {
                    Button(onClick = { finish() }, enabled = !saving) { Text(if (saving) "Сохранение…" else "Готово") }
                }
            }
        }
    }
}
