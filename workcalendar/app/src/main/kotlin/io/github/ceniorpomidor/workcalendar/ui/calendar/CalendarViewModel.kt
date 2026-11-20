package io.github.ceniorpomidor.workcalendar.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.ceniorpomidor.workcalendar.AppContainer
import io.github.ceniorpomidor.workcalendar.data.db.DayNoteEntity
import io.github.ceniorpomidor.workcalendar.data.repo.CalcContext
import io.github.ceniorpomidor.workcalendar.domain.model.Absence
import io.github.ceniorpomidor.workcalendar.domain.model.AbsenceType
import io.github.ceniorpomidor.workcalendar.domain.model.AppSettings
import io.github.ceniorpomidor.workcalendar.domain.model.ManualAccrual
import io.github.ceniorpomidor.workcalendar.domain.model.Money
import io.github.ceniorpomidor.workcalendar.domain.model.Payment
import io.github.ceniorpomidor.workcalendar.domain.model.PayoutRule
import io.github.ceniorpomidor.workcalendar.domain.model.Shift
import io.github.ceniorpomidor.workcalendar.domain.model.ShiftDisplayState
import io.github.ceniorpomidor.workcalendar.domain.model.ShiftKind
import io.github.ceniorpomidor.workcalendar.domain.model.ShiftStatus
import io.github.ceniorpomidor.workcalendar.domain.pay.PayoutInstance
import io.github.ceniorpomidor.workcalendar.domain.pay.PeriodSummary
import io.github.ceniorpomidor.workcalendar.domain.time.DateRange
import io.github.ceniorpomidor.workcalendar.domain.util.Formats
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth

/** Aggregated state of a calendar day; defines its color. */
enum class DayState {
    OFF,
    PLANNED,
    IN_PROGRESS,
    AWAITING,
    CONFIRMED,
    MISSED,
    CANCELLED,
    MOVED,
    EXTRA,
    VACATION,
    SICK,
    OTHER_ABSENCE,
}

data class DayInfo(
    val date: LocalDate,
    val shifts: List<Shift>,
    val absence: Absence?,
    val holidayName: String?,
    val specialDayOff: Boolean,
    val publicHoliday: Boolean,
    val note: String?,
    val state: DayState,
    val lines: List<String>,
    val earned: Money?,
    val hasExtra: Boolean,
    /** Advances and salaries paid on this day. */
    val payouts: List<PayoutInstance> = emptyList(),
)

data class CalendarUiState(
    val month: YearMonth,
    val now: LocalDateTime,
    val settings: AppSettings = AppSettings(),
    val calc: CalcContext? = null,
    val days: Map<LocalDate, DayInfo> = emptyMap(),
    /** Summaries of the loaded month and its neighbours: shown at once while swiping. */
    val summaries: Map<YearMonth, PeriodSummary> = emptyMap(),
    val unconfirmedTotal: Int = 0,
    val hasSchedule: Boolean = true,
    val loading: Boolean = true,
)

class CalendarViewModel(private val c: AppContainer) : ViewModel() {
    private val month = MutableStateFlow(YearMonth.from(c.clock.today()))
    val selectedDate = MutableStateFlow(c.clock.today())

    private val ticker: Flow<LocalDateTime> = flow {
        while (true) {
            emit(c.clock.now())
            delay(30_000)
        }
    }

    private val unconfirmed: Flow<Int> = ticker.flatMapLatest { now -> c.shifts.observeUnconfirmed(now).map { it.size } }

    private val payoutInputs: Flow<PayoutInputs> = combine(c.finance.rules, c.finance.accruals, c.finance.payments) { rules, accruals, payments ->
        PayoutInputs(rules, accruals, payments)
    }

    val state: StateFlow<CalendarUiState> = month.flatMapLatest { m ->
        val range = DateRange(m.minusMonths(1).atDay(1), m.plusMonths(1).atEndOfMonth())
        // Salaries paid in the first loaded month are for the month before it.
        val shiftRange = DateRange(m.minusMonths(2).atDay(1), range.endInclusive)
        combine(
            c.shifts.observeRange(shiftRange),
            c.absences.observeRange(range),
            c.db.miscDao().observeNotes(range.start, range.endInclusive),
            c.calc.context,
            ticker,
        ) { shifts, absences, notes, calc, now -> Loaded(shifts, absences, notes, calc, now) }
            .combine(payoutInputs) { d, p -> build(m, range, d, p) }
    }.combine(unconfirmed) { s, count -> s.copy(unconfirmedTotal = count) }
        .combine(c.schedule.assignments) { s, assignments -> s.copy(hasSchedule = assignments.isNotEmpty() || s.days.values.any { it.shifts.isNotEmpty() }) }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CalendarUiState(month.value, c.clock.now()))

    /**
     * The selected day, observed on its own: after swiping away the selection stays where it was
     * and may lie outside the loaded months.
     */
    val selectedDay: StateFlow<DayInfo?> = selectedDate.flatMapLatest { date ->
        val range = DateRange(date, date)
        combine(
            c.shifts.observeRange(range),
            c.absences.observeRange(range),
            c.db.miscDao().observeNotes(date, date),
            c.calc.context,
            ticker,
        ) { shifts, absences, notes, calc, now ->
            val dayShifts = shifts.filter { it.isVisible && it.date == date }.sortedBy { it.plannedStart }
            dayInfo(date, dayShifts, absences.firstOrNull { date in it.range }, notes.firstOrNull()?.text, calc, now)
        }
    }.flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun onMonthVisible(m: YearMonth) {
        if (month.value == m && !state.value.loading) return
        month.value = m
        viewModelScope.launch {
            try {
                c.schedule.ensureGenerated(DateRange(m.minusMonths(1).atDay(1), m.plusMonths(1).atEndOfMonth()))
            } catch (e: Exception) {
                android.util.Log.e("WorkCalendar", "Generation failed", e)
            }
        }
    }

    fun select(date: LocalDate) {
        selectedDate.value = date
    }

    suspend fun saveNote(date: LocalDate, text: String) {
        if (text.isBlank()) {
            c.db.miscDao().deleteNote(date)
        } else {
            c.db.miscDao().upsertNote(DayNoteEntity(date, text.trim(), c.clock.now()))
        }
    }

    private class Loaded(
        val shifts: List<Shift>,
        val absences: List<Absence>,
        val notes: List<DayNoteEntity>,
        val calc: CalcContext,
        val now: LocalDateTime,
    )

    private class PayoutInputs(val rules: List<PayoutRule>, val accruals: List<ManualAccrual>, val payments: List<Payment>)

    private fun build(m: YearMonth, range: DateRange, d: Loaded, p: PayoutInputs): CalendarUiState {
        val calc = d.calc
        val now = d.now
        val shifts = d.shifts
        val absences = d.absences
        val settings = calc.settings
        val byDate = shifts.filter { it.isVisible }.groupBy { it.date }
        val notesByDate = d.notes.associateBy { it.date }
        val payouts = calc.payouts.between(range.start, range.endInclusive, p.rules, shifts, p.accruals, p.payments, now).groupBy { it.payDate }
        val days = HashMap<LocalDate, DayInfo>()
        for (date in range) {
            val dayShifts = byDate[date].orEmpty().sortedBy { it.plannedStart }
            val absence = absences.firstOrNull { date in it.range }
            days[date] = dayInfo(date, dayShifts, absence, notesByDate[date]?.text, calc, now).copy(payouts = payouts[date].orEmpty())
        }
        val summaries = listOf(m.minusMonths(1), m, m.plusMonths(1)).associateWith { month ->
            calc.summaries.summarize(DateRange.month(month), shifts, absences, emptyList(), now)
        }
        return CalendarUiState(month = m, now = now, settings = settings, calc = calc, days = days, summaries = summaries, loading = false)
    }

    private fun dayInfo(date: LocalDate, shifts: List<Shift>, absence: Absence?, note: String?, calc: CalcContext, now: LocalDateTime): DayInfo {
        val active = shifts.filter { it.isActive }
        val settings = calc.settings.calendar
        val states = active.map { it.displayState(now) }
        val state = when {
            absence != null -> when (absence.type) {
                AbsenceType.VACATION, AbsenceType.UNPAID -> DayState.VACATION
                AbsenceType.SICK -> DayState.SICK
                AbsenceType.OTHER -> DayState.OTHER_ABSENCE
            }
            ShiftDisplayState.AWAITING_CONFIRMATION in states -> DayState.AWAITING
            ShiftDisplayState.IN_PROGRESS in states -> DayState.IN_PROGRESS
            ShiftDisplayState.UPCOMING in states -> if (active.all { it.kind == ShiftKind.EXTRA }) DayState.EXTRA else DayState.PLANNED
            ShiftDisplayState.CONFIRMED in states -> DayState.CONFIRMED
            ShiftDisplayState.MISSED in states -> DayState.MISSED
            shifts.any { it.status == ShiftStatus.MOVED } -> DayState.MOVED
            shifts.any { it.status == ShiftStatus.CANCELLED } -> DayState.CANCELLED
            else -> DayState.OFF
        }
        val lines = ArrayList<String>()
        when (state) {
            DayState.VACATION -> lines += if (absence?.type == AbsenceType.UNPAID) "б/с" else "отпуск"
            DayState.SICK -> lines += "больн."
            DayState.OTHER_ABSENCE -> lines += (absence?.displayTitle() ?: "").take(7).lowercase()
            DayState.CONFIRMED -> {
                val worked = active.sumOf { (it.workedMinutes ?: 0).toLong() }
                if (settings.showWorkedHours) {
                    lines += "${Formats.hoursDecimal(worked)} ч"
                } else {
                    active.firstOrNull()?.let { lines += Formats.time(it.plannedStart) }
                }
            }
            DayState.MISSED -> lines += "не был"
            DayState.CANCELLED -> lines += "отмена"
            DayState.MOVED -> shifts.firstOrNull { it.status == ShiftStatus.MOVED }?.movedToDate?.let { lines += "→${Formats.shortDate(it)}" }
            DayState.OFF -> Unit
            else -> {
                val first = active.firstOrNull()
                if (first != null && settings.showShiftTimes) {
                    lines += Formats.time(first.plannedStart)
                    lines += Formats.time(first.plannedEnd)
                } else if (first != null) {
                    lines += "${Formats.hoursDecimal(first.plannedPaidMinutes(calc.zone))} ч"
                }
                if (active.size > 1) lines += "+${active.size - 1}"
            }
        }
        val earned = if (calc.settings.calendar.showDayEarnings) {
            val confirmed = active.mapNotNull { calc.pay.earned(it) }
            if (confirmed.isNotEmpty()) confirmed.fold(Money.ZERO) { a, b -> a + b } else null
        } else {
            null
        }
        return DayInfo(
            date = date,
            shifts = shifts,
            absence = absence,
            holidayName = calc.holidays.holidayName(date),
            specialDayOff = calc.holidays.isSpecialDayOff(date),
            publicHoliday = calc.holidays.isPublicHoliday(date),
            note = note,
            state = state,
            lines = lines,
            earned = earned,
            hasExtra = active.any { it.kind == ShiftKind.EXTRA },
        )
    }
}
