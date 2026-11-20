package io.github.ceniorpomidor.workcalendar.ui.finance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.ceniorpomidor.workcalendar.AppContainer
import io.github.ceniorpomidor.workcalendar.data.repo.CalcContext
import io.github.ceniorpomidor.workcalendar.domain.model.Absence
import io.github.ceniorpomidor.workcalendar.domain.model.AppSettings
import io.github.ceniorpomidor.workcalendar.domain.model.ManualAccrual
import io.github.ceniorpomidor.workcalendar.domain.model.Money
import io.github.ceniorpomidor.workcalendar.domain.model.Payment
import io.github.ceniorpomidor.workcalendar.domain.model.PayoutRule
import io.github.ceniorpomidor.workcalendar.domain.model.Shift
import io.github.ceniorpomidor.workcalendar.domain.notify.AbsencePayment
import io.github.ceniorpomidor.workcalendar.domain.pay.PayoutInstance
import io.github.ceniorpomidor.workcalendar.domain.pay.PeriodSummary
import io.github.ceniorpomidor.workcalendar.domain.time.DateRange
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

enum class PeriodMode {
    MONTH,
    RANGE,
    YEAR,
}

data class Period(val mode: PeriodMode, val range: DateRange, val month: YearMonth)

data class AbsencePaymentRow(val item: AbsencePayment, val received: Payment?)

/** Expected payouts for a coming month, by the shifts already in the calendar. */
data class ForecastMonth(val month: YearMonth, val summary: PeriodSummary, val payouts: List<PayoutInstance>) {
    /** Sum of the payouts for the month; null when one of them cannot be calculated. */
    val total: Money? get() = if (payouts.any { it.effectiveNet == null }) null else payouts.fold(Money.ZERO) { a, p -> a + (p.effectiveNet ?: Money.ZERO) }
}

data class FinanceUiState(
    val period: Period,
    val settings: AppSettings = AppSettings(),
    val summary: PeriodSummary? = null,
    val previous: PeriodSummary? = null,
    val payouts: List<PayoutInstance> = emptyList(),
    val upcoming: List<PayoutInstance> = emptyList(),
    val absencePayments: List<AbsencePaymentRow> = emptyList(),
    val accruals: List<ManualAccrual> = emptyList(),
    val payments: List<Payment> = emptyList(),
    val forecast: List<ForecastMonth> = emptyList(),
    val hasRates: Boolean = true,
    val today: LocalDate = LocalDate.now(),
    val loading: Boolean = true,
) {
    /** Sum of expected payouts of the month; null when one of them cannot be calculated. */
    val expectedTotal: Money?
        get() = if (payouts.any { it.effectiveNet == null }) null else payouts.fold(Money.ZERO) { a, p -> a + (p.effectiveNet ?: Money.ZERO) }

    val receivedTotal: Money get() = payouts.mapNotNull { it.payment?.amount }.fold(Money.ZERO) { a, b -> a + b }

    /** Expected but not yet received. */
    val remaining: Money?
        get() = if (payouts.any { !it.isReceived && it.expectedNet == null }) {
            null
        } else {
            payouts.filter { !it.isReceived }.fold(Money.ZERO) { a, p -> a + (p.expectedNet ?: Money.ZERO) }
        }
}

private data class Loaded(
    val shifts: List<Shift>,
    val absences: List<Absence>,
    val accruals: List<ManualAccrual>,
    val payments: List<Payment>,
    val rules: List<PayoutRule> = emptyList(),
)

class FinanceViewModel(private val c: AppContainer) : ViewModel() {
    private val today = c.clock.today()
    val period = MutableStateFlow(Period(PeriodMode.MONTH, DateRange.month(YearMonth.from(today)), YearMonth.from(today)))

    fun setMonth(month: YearMonth) {
        period.value = Period(PeriodMode.MONTH, DateRange.month(month), month)
    }

    fun setRange(range: DateRange) {
        period.value = Period(PeriodMode.RANGE, range, YearMonth.from(range.start))
    }

    fun setYear(year: Int) {
        period.value = Period(PeriodMode.YEAR, DateRange.year(year), YearMonth.of(year, 1))
    }

    fun setMode(mode: PeriodMode) {
        val p = period.value
        when (mode) {
            PeriodMode.MONTH -> setMonth(p.month)
            PeriodMode.RANGE -> setRange(p.range)
            PeriodMode.YEAR -> setYear(p.range.start.year)
        }
    }

    private val upcomingRange = DateRange(YearMonth.from(today).minusMonths(2).atDay(1), YearMonth.from(today).plusMonths(1).atEndOfMonth())

    /** Coming months shown in the forecast of the month [p]: none for past months. */
    private fun forecastMonths(p: Period): List<YearMonth> =
        if (p.mode == PeriodMode.MONTH && p.month >= YearMonth.from(today)) (1L..FORECAST_MONTHS).map { p.month.plusMonths(it) } else emptyList()

    init {
        // The forecast needs the shifts of the coming months: create them by the schedule.
        viewModelScope.launch {
            period.collect { p ->
                val months = forecastMonths(p)
                if (months.isNotEmpty()) {
                    try {
                        c.schedule.ensureGenerated(DateRange(months.first().atDay(1), months.last().atEndOfMonth()))
                    } catch (e: Exception) {
                        android.util.Log.e("WorkCalendar", "Generation failed", e)
                    }
                }
            }
        }
    }

    val state: StateFlow<FinanceUiState> = period.flatMapLatest { p ->
        val loadStart = minOf(p.range.start, p.month.minusMonths(1).atDay(1), upcomingRange.start)
        val loadEnd = maxOf(p.range.endInclusive, upcomingRange.endInclusive, forecastMonths(p).lastOrNull()?.atEndOfMonth() ?: p.range.endInclusive)
        combine(
            c.shifts.observeRange(DateRange(loadStart, loadEnd)),
            c.absences.all,
            c.finance.accruals,
            c.finance.payments,
        ) { shifts, absences, accruals, payments -> Loaded(shifts, absences, accruals, payments) }
            .combine(c.finance.rules) { loaded, rules -> loaded.copy(rules = rules) }
            .combine(c.calc.context) { loaded, calc -> build(p, loaded, calc) }
    }.flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FinanceUiState(period.value))

    private suspend fun build(p: Period, d: Loaded, calc: CalcContext): FinanceUiState {
        val now = c.clock.now()
        val today = now.toLocalDate()
        val summary = calc.summaries.summarize(p.range, d.shifts, d.absences, d.accruals, now)
        val previous = if (p.mode == PeriodMode.MONTH) {
            calc.summaries.summarize(DateRange.month(p.month.minusMonths(1)), d.shifts, d.absences, d.accruals, now)
        } else {
            null
        }
        val payouts = if (p.mode == PeriodMode.MONTH) calc.payouts.forMonth(p.month, d.rules, d.shifts, d.accruals, d.payments, now) else emptyList()
        val upcoming = calc.payouts.between(today, today.plusDays(45), d.rules, d.shifts, d.accruals, d.payments, now).filter { !it.isReceived }
        val paydays = calc.payouts.between(p.range.start.minusDays(40), p.range.endInclusive.plusDays(90), d.rules, d.shifts, d.accruals, d.payments, now).map { it.payDate }
        val relevant = d.absences.filter { it.range.overlaps(DateRange(p.range.start.minusDays(31), p.range.endInclusive.plusDays(31))) }
        val absencePayments = c.absences.payments(relevant, calc, paydays)
            .filter { it.date in p.range || it.absence.range.overlaps(p.range) }
            .map { item -> AbsencePaymentRow(item, d.payments.firstOrNull { it.payoutKey == item.key }) }
        val forecast = forecastMonths(p).map { month ->
            ForecastMonth(
                month = month,
                summary = calc.summaries.summarize(DateRange.month(month), d.shifts, d.absences, d.accruals, now),
                payouts = calc.payouts.forMonth(month, d.rules, d.shifts, d.accruals, d.payments, now),
            )
        }
        return FinanceUiState(
            period = p,
            forecast = forecast,
            settings = calc.settings,
            summary = summary,
            previous = previous,
            payouts = payouts,
            upcoming = upcoming,
            absencePayments = absencePayments,
            accruals = d.accruals.filter { it.date in p.range }.sortedBy { it.date },
            payments = d.payments.filter { it.date in p.range }.sortedBy { it.date },
            hasRates = !calc.rates.isEmpty,
            today = today,
            loading = false,
        )
    }
}

/** How many coming months the forecast shows. */
private const val FORECAST_MONTHS = 3L
