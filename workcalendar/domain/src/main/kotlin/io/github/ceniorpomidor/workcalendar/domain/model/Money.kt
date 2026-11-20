package io.github.ceniorpomidor.workcalendar.domain.model

import kotlinx.serialization.Serializable
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.abs

/**
 * Amount of money in minor units (kopecks). All calculations are done with
 * integer arithmetic to avoid floating point errors.
 */
@Serializable
@JvmInline
value class Money(val kopecks: Long) : Comparable<Money> {
    operator fun plus(other: Money): Money = Money(kopecks + other.kopecks)

    operator fun minus(other: Money): Money = Money(kopecks - other.kopecks)

    operator fun unaryMinus(): Money = Money(-kopecks)

    operator fun times(factor: Int): Money = Money(kopecks * factor)

    operator fun times(factor: Long): Money = Money(kopecks * factor)

    override fun compareTo(other: Money): Int = kopecks.compareTo(other.kopecks)

    val isZero: Boolean get() = kopecks == 0L

    val isPositive: Boolean get() = kopecks > 0L

    val isNegative: Boolean get() = kopecks < 0L

    /** Returns [percent] percents of this amount, rounded half-up to kopecks. */
    fun percent(percent: Int): Money = Money(divRound(kopecks * percent, 100))

    /** Returns this amount multiplied by [numerator] / [denominator], rounded half-up. */
    fun scale(numerator: Long, denominator: Long): Money {
        require(denominator != 0L) { "denominator must not be zero" }
        return Money(divRound(kopecks * numerator, denominator))
    }

    fun toBigDecimal(): BigDecimal = BigDecimal.valueOf(kopecks, 2)

    fun toRubles(): Double = kopecks / 100.0

    fun coerceAtLeast(min: Money): Money = if (this < min) min else this

    fun coerceAtMost(max: Money): Money = if (this > max) max else this

    override fun toString(): String = toBigDecimal().toPlainString()

    companion object {
        val ZERO: Money = Money(0)

        fun ofRubles(rubles: Long): Money = Money(rubles * 100)

        fun of(value: BigDecimal): Money = Money(value.setScale(2, RoundingMode.HALF_UP).movePointRight(2).longValueExact())

        fun ofRubles(rubles: Double): Money = of(BigDecimal.valueOf(rubles))

        /** Payment for [minutes] of work at [hourlyRate] per hour. */
        fun forMinutes(minutes: Long, hourlyRate: Money): Money = Money(divRound(minutes * hourlyRate.kopecks, 60))

        /** Payment for [minutes] of work at [hourlyRate] with a [percent] surcharge. */
        fun forMinutes(minutes: Long, hourlyRate: Money, percent: Int): Money =
            Money(divRound(minutes * hourlyRate.kopecks * percent, 60L * 100L))

        /**
         * Parses user input such as `1 234,56`, `1234.5`, `1234 ₽`.
         * Returns null when the text is not a valid amount.
         */
        fun parse(text: String): Money? {
            val cleaned = text.trim()
                .replace("₽", "")
                .replace("руб.", "")
                .replace("руб", "")
                .replace("р.", "")
                .replace("\u00A0", "")
                .replace("\u202F", "")
                .replace(" ", "")
                .replace(',', '.')
            if (cleaned.isEmpty()) return null
            if (cleaned.count { it == '.' } > 1) return null
            val decimal = cleaned.toBigDecimalOrNull() ?: return null
            if (decimal.scale() > 2 && decimal.stripTrailingZeros().scale() > 2) return null
            return try {
                of(decimal)
            } catch (e: ArithmeticException) {
                null
            }
        }

        /** Integer division rounded half away from zero. */
        fun divRound(numerator: Long, denominator: Long): Long {
            require(denominator != 0L)
            val negative = (numerator < 0) xor (denominator < 0)
            val n = abs(numerator)
            val d = abs(denominator)
            val q = (n / d) + if ((n % d) * 2 >= d) 1 else 0
            return if (negative) -q else q
        }
    }
}

fun Iterable<Money>.sum(): Money {
    var total = 0L
    for (money in this) total += money.kopecks
    return Money(total)
}

inline fun <T> Iterable<T>.sumOfMoney(selector: (T) -> Money): Money {
    var total = 0L
    for (item in this) total += selector(item).kopecks
    return Money(total)
}
