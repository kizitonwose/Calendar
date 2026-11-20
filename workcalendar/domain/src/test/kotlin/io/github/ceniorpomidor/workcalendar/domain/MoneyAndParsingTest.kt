package io.github.ceniorpomidor.workcalendar.domain

import io.github.ceniorpomidor.workcalendar.domain.model.Money
import io.github.ceniorpomidor.workcalendar.domain.pay.Formula
import io.github.ceniorpomidor.workcalendar.domain.util.Formats
import io.github.ceniorpomidor.workcalendar.domain.util.HoursParser
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class MoneyAndParsingTest {
    @Test
    fun `money parsing accepts russian formats`() {
        assertEquals(Money(123456), Money.parse("1 234,56"))
        assertEquals(Money(123450), Money.parse("1234.5"))
        assertEquals(Money(100000), Money.parse("1000 ₽"))
        assertEquals(Money(-5000), Money.parse("-50"))
        assertNull(Money.parse("abc"))
        assertNull(Money.parse("1,2,3"))
        assertNull(Money.parse("10.123"))
        assertNull(Money.parse(""))
    }

    @Test
    fun `pay for minutes is rounded half up`() {
        // 7.5 hours at 333.33 per hour = 2499.975 -> 2499.98
        assertEquals(Money(249998), Money.forMinutes(450, Money(33333)))
        // 20% surcharge for 2 hours at 300 = 120
        assertEquals(Money.ofRubles(120), Money.forMinutes(120, Money.ofRubles(300), 20))
        assertEquals(-3L, Money.divRound(-5, 2))
        assertEquals(3L, Money.divRound(5, 2))
        assertEquals(Money(1300), Money.ofRubles(100).percent(13))
    }

    @Test
    fun `money formatting`() {
        assertEquals("12\u00A0345,67\u00A0₽", Formats.money(Money(1234567)))
        assertEquals("1\u00A0000\u00A0₽", Formats.money(Money.ofRubles(1000)))
        assertEquals("−5,50\u00A0₽", Formats.money(Money(-550)))
        assertEquals("7,5", Formats.hoursDecimal(450))
        assertEquals("8", Formats.hoursDecimal(480))
        assertEquals("7,33", Formats.hoursDecimal(440))
        assertEquals("7\u00A0ч 20\u00A0мин", Formats.hoursMinutes(440))
        assertEquals("2 смены", Formats.shifts(2))
        assertEquals("11 смен", Formats.shifts(11))
        assertEquals("21 смена", Formats.shifts(21))
    }

    @Test
    fun `hours parser understands common inputs`() {
        assertEquals(480, HoursParser.parse("8"))
        assertEquals(450, HoursParser.parse("7,5"))
        assertEquals(450, HoursParser.parse("7.5"))
        assertEquals(450, HoursParser.parse("7:30"))
        assertEquals(450, HoursParser.parse("7ч30м"))
        assertEquals(450, HoursParser.parse("7 ч 30 мин"))
        assertEquals(450, HoursParser.parse("450 мин"))
        assertEquals(465, HoursParser.parse("7,75"))
        assertEquals(480, HoursParser.parse("8 часов"))
        assertNull(HoursParser.parse("7:75"))
        assertNull(HoursParser.parse("много"))
        assertNull(HoursParser.parse("99"))
    }

    @Test
    fun `formula evaluation`() {
        val vars = mapOf("ЗАРАБОТОК" to 50000.0, "ЧАСЫ" to 80.0)
        assertEquals(20000.0, Formula.evaluate("ЗАРАБОТОК * 40%", vars), 0.001)
        assertEquals(30000.0, Formula.evaluate("min(ЗАРАБОТОК; 30000)", vars), 0.001)
        assertEquals(43500.0, Formula.evaluate("earned * 0,87", vars), 0.001)
        assertEquals(-10.0, Formula.evaluate("-(4+6)", vars), 0.001)
        assertEquals(625.0, Formula.evaluate("ЗАРАБОТОК / ЧАСЫ", vars), 0.001)
        assertThrows<Formula.FormulaException> { Formula.evaluate("ЗАРАБОТОК *", vars) }
        assertThrows<Formula.FormulaException> { Formula.evaluate("НЕИЗВЕСТНО + 1", vars) }
        assertThrows<Formula.FormulaException> { Formula.evaluate("1/0", vars) }
        assertNull(Formula.validate("ЗАРАБОТОК*0.5 + СТАВКА"))
        assertNotNull(Formula.validate("ЗАРАБОТОК*(0.5"))
    }
}
