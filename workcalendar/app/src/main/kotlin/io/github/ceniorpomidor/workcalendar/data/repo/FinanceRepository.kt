package io.github.ceniorpomidor.workcalendar.data.repo

import androidx.room.withTransaction
import io.github.ceniorpomidor.workcalendar.data.db.AppDatabase
import io.github.ceniorpomidor.workcalendar.data.db.toDomain
import io.github.ceniorpomidor.workcalendar.data.db.toEntity
import io.github.ceniorpomidor.workcalendar.domain.model.ChangeCategory
import io.github.ceniorpomidor.workcalendar.domain.model.ExternalEarning
import io.github.ceniorpomidor.workcalendar.domain.model.ManualAccrual
import io.github.ceniorpomidor.workcalendar.domain.model.Money
import io.github.ceniorpomidor.workcalendar.domain.model.Payment
import io.github.ceniorpomidor.workcalendar.domain.model.PaymentType
import io.github.ceniorpomidor.workcalendar.domain.model.PayoutRule
import io.github.ceniorpomidor.workcalendar.domain.model.RatePeriod
import io.github.ceniorpomidor.workcalendar.domain.notify.AbsencePayment
import io.github.ceniorpomidor.workcalendar.domain.pay.PayoutCalculator
import io.github.ceniorpomidor.workcalendar.domain.pay.PayoutInstance
import io.github.ceniorpomidor.workcalendar.domain.time.DateRange
import io.github.ceniorpomidor.workcalendar.domain.util.Formats
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class FinanceRepository(
    private val db: AppDatabase,
    private val calc: CalcRepository,
    private val log: ChangeLogger,
    private val changes: DataChanges,
    private val clock: AppClock,
) {
    private val dao get() = db.financeDao()

    val rates: Flow<List<RatePeriod>> = db.rateDao().observeAll().map { list -> list.map { it.toDomain() } }
    val rules: Flow<List<PayoutRule>> = dao.observeRules().map { list -> list.map { it.toDomain() } }
    val payments: Flow<List<Payment>> = dao.observePayments().map { list -> list.map { it.toDomain() } }
    val accruals: Flow<List<ManualAccrual>> = dao.observeAccruals().map { list -> list.map { it.toDomain() } }
    val external: Flow<List<ExternalEarning>> = dao.observeExternal().map { list -> list.map { it.toDomain() } }

    suspend fun getRules(): List<PayoutRule> = dao.getRules().map { it.toDomain() }

    suspend fun getRule(id: Long): PayoutRule? = dao.getRule(id)?.toDomain()

    suspend fun getPayments(): List<Payment> = dao.getPayments().map { it.toDomain() }

    suspend fun getPayment(id: Long): Payment? = dao.getPayment(id)?.toDomain()

    suspend fun getAccruals(): List<ManualAccrual> = dao.getAccruals().map { it.toDomain() }

    suspend fun getAccrual(id: Long): ManualAccrual? = dao.getAccrual(id)?.toDomain()

    /** Creates the default advance and salary rules on the first start. */
    suspend fun ensureDefaultRules() {
        if (dao.getRules().isEmpty()) {
            dao.insertRules(listOf(PayoutRule.defaultAdvance().toEntity(), PayoutRule.defaultSalary().toEntity()))
        }
    }

    suspend fun saveRate(rate: RatePeriod): Long {
        if (!rate.hourlyRate.isPositive) throw ValidationException("Укажите ставку больше нуля")
        val existing = db.rateDao().getAll().map { it.toDomain() }
        if (existing.any { it.id != rate.id && it.effectiveFrom == rate.effectiveFrom }) {
            throw ValidationException("Ставка с ${Formats.date(rate.effectiveFrom)} уже есть — измените её")
        }
        val id = if (rate.id == 0L) db.rateDao().insert(rate.toEntity()) else rate.id.also { db.rateDao().update(rate.toEntity()) }
        log.log(
            ChangeCategory.RATE,
            if (rate.id == 0L) "Новая ставка" else "Ставка изменена",
            "С ${Formats.date(rate.effectiveFrom)}: ${Formats.money(rate.hourlyRate)}/ч" +
                surchargeText(rate),
            id,
        )
        changes.notifyChanged()
        return id
    }

    private fun surchargeText(rate: RatePeriod): String {
        val parts = ArrayList<String>()
        if (rate.holidayBonusPercent > 0) parts += "праздничные +${rate.holidayBonusPercent}%"
        if (rate.overtimeBonusPercent > 0) parts += "сверхурочные +${rate.overtimeBonusPercent}%"
        if (rate.extraShiftBonusPercent > 0) parts += "доп. смены +${rate.extraShiftBonusPercent}%"
        return if (parts.isEmpty()) "" else " (${parts.joinToString()})"
    }

    suspend fun deleteRate(rate: RatePeriod) {
        db.rateDao().delete(rate.toEntity())
        log.log(ChangeCategory.RATE, "Ставка удалена", "С ${Formats.date(rate.effectiveFrom)}: ${Formats.money(rate.hourlyRate)}/ч. Подтверждённые смены сохранили свои суммы", rate.id)
        changes.notifyChanged()
    }

    suspend fun saveRule(rule: PayoutRule): Long {
        if (rule.name.isBlank()) throw ValidationException("Укажите название выплаты")
        val id = if (rule.id == 0L) dao.insertRule(rule.toEntity()) else rule.id.also { dao.updateRule(rule.toEntity()) }
        log.log(ChangeCategory.SETTINGS, "Правило выплаты сохранено", "«${rule.name}»: ${rule.payDay} число", id)
        changes.notifyChanged()
        return id
    }

    suspend fun deleteRule(rule: PayoutRule) {
        dao.deleteRule(rule.toEntity())
        log.log(ChangeCategory.SETTINGS, "Правило выплаты удалено", "«${rule.name}»", rule.id)
        changes.notifyChanged()
    }

    suspend fun findPayout(key: String): PayoutInstance? = findPayout(key, calc.current().payouts)

    private suspend fun findPayout(key: String, calculator: PayoutCalculator): PayoutInstance? {
        val (_, month) = PayoutInstance.parseKey(key) ?: return null
        val range = DateRange.month(month)
        val shifts = db.shiftDao().getRange(range.start, range.endInclusive).map { it.toDomain() }
        return calculator.forMonth(month, getRules(), shifts, getAccruals(), getPayments(), clock.now()).firstOrNull { it.key == key }
    }

    /** A reply to an old notification may arrive after the same payout was recorded elsewhere. */
    suspend fun receivePayoutReply(key: String, amount: Money): Boolean = db.withTransaction {
        val context = calc.current()
        if (context.settings.security.financeLock) throw ValidationException("Откройте выплату и разблокируйте финансы")
        val payout = findPayout(key, context.payouts) ?: throw ValidationException("Правило выплаты больше недоступно")
        if (payout.isReceived) return@withTransaction false
        markPayoutReceived(payout, amount, clock.today(), "")
        true
    }

    /** Registers money received for an expected payout; repeated calls update the same record. */
    suspend fun markPayoutReceived(payout: PayoutInstance, amount: Money, date: LocalDate, note: String): Long = db.withTransaction {
        val existing = dao.getPaymentByKey(payout.key)?.toDomain()
        val now = clock.now()
        val payment = Payment(
            id = existing?.id ?: 0,
            type = PayoutCalculator.paymentTypeFor(payout.rule.kind),
            amount = amount,
            date = date,
            payoutKey = payout.key,
            periodStart = payout.period.start,
            periodEnd = payout.period.endInclusive,
            expectedAmount = payout.expectedNet,
            note = note,
            createdAt = existing?.createdAt ?: now,
            updatedAt = now,
        )
        savePayment(payment)
    }

    suspend fun markAbsencePaymentReceived(item: AbsencePayment, amount: Money, date: LocalDate, note: String): Long {
        val existing = dao.getPaymentByKey(item.key)?.toDomain()
        val now = clock.now()
        val payment = Payment(
            id = existing?.id ?: 0,
            type = if (item.part == AbsencePayment.Part.VACATION) PaymentType.VACATION_PAY else PaymentType.SICK_PAY,
            amount = amount,
            date = date,
            payoutKey = item.key,
            periodStart = item.absence.startDate,
            periodEnd = item.absence.endDate,
            expectedAmount = item.amount,
            absenceId = item.absence.id,
            note = note,
            createdAt = existing?.createdAt ?: now,
            updatedAt = now,
        )
        return savePayment(payment)
    }

    suspend fun savePayment(payment: Payment): Long {
        if (payment.amount.isNegative) throw ValidationException("Сумма не может быть отрицательной")
        val now = clock.now()
        val id = if (payment.id == 0L) {
            dao.insertPayment(payment.copy(createdAt = now, updatedAt = now).toEntity())
        } else {
            dao.updatePayment(payment.copy(updatedAt = now).toEntity())
            payment.id
        }
        log.log(ChangeCategory.PAYMENT, if (payment.id == 0L) "Выплата получена" else "Выплата изменена", "${Formats.date(payment.date)}: ${Formats.money(payment.amount)}", id)
        changes.notifyChanged()
        return id
    }

    suspend fun deletePayment(payment: Payment) {
        dao.deletePayment(payment.toEntity())
        log.log(ChangeCategory.PAYMENT, "Выплата удалена", "${Formats.date(payment.date)}: ${Formats.money(payment.amount)}", payment.id)
        changes.notifyChanged()
    }

    suspend fun saveAccrual(accrual: ManualAccrual): Long {
        if (accrual.title.isBlank()) throw ValidationException("Укажите название")
        if (!accrual.amount.isPositive) throw ValidationException("Сумма должна быть больше нуля")
        val now = clock.now()
        val id = if (accrual.id == 0L) {
            dao.insertAccrual(accrual.copy(createdAt = now, updatedAt = now).toEntity())
        } else {
            dao.updateAccrual(accrual.copy(updatedAt = now).toEntity())
            accrual.id
        }
        log.log(
            ChangeCategory.ACCRUAL,
            if (accrual.id ==
                0L
            ) {
                "Начисление добавлено"
            } else {
                "Начисление изменено"
            },
            "${Formats.date(accrual.date)} «${accrual.title}»: ${Formats.money(accrual.signedAmount)}",
            id,
        )
        changes.notifyChanged()
        return id
    }

    suspend fun deleteAccrual(accrual: ManualAccrual) {
        dao.deleteAccrual(accrual.toEntity())
        log.log(ChangeCategory.ACCRUAL, "Начисление удалено", "${Formats.date(accrual.date)} «${accrual.title}»", accrual.id)
        changes.notifyChanged()
    }

    suspend fun saveExternal(item: ExternalEarning) {
        if (item.amount.isNegative) throw ValidationException("Сумма не может быть отрицательной")
        val existing = dao.getExternal().map { it.toDomain() }.firstOrNull { it.id != item.id && it.year == item.year && it.month == item.month }
        val toSave = if (existing != null) item.copy(id = existing.id) else item
        if (toSave.id == 0L) dao.insertExternal(toSave.toEntity()) else dao.updateExternal(toSave.toEntity())
        changes.notifyChanged()
    }

    suspend fun deleteExternal(item: ExternalEarning) {
        dao.deleteExternal(item.toEntity())
        changes.notifyChanged()
    }

    suspend fun searchPayments(query: String): List<Payment> = dao.searchPayments("%$query%").map { it.toDomain() }

    suspend fun searchAccruals(query: String): List<ManualAccrual> = dao.searchAccruals("%$query%").map { it.toDomain() }
}
