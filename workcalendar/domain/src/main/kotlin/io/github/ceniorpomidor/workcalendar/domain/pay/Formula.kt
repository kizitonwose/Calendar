package io.github.ceniorpomidor.workcalendar.domain.pay

import kotlin.math.max
import kotlin.math.min
import kotlin.math.round

/**
 * Small arithmetic language for user-defined payout rules, e.g. `ЗАРАБОТОК * 40%` or
 * `min(ЗАРАБОТОК; 30000) - 1500`.
 *
 * Supports numbers (`0.87`, `0,87`), variables, `+ - * /`, parentheses, postfix `%`
 * and functions `min`, `max`, `round` (arguments are separated by `;`).
 */
object Formula {
    /** Variables available in payout formulas (values in rubles / hours). */
    val VARIABLES: Map<String, String> = linkedMapOf(
        "ЗАРАБОТОК" to "Заработок за период выплаты",
        "ЧАСЫ" to "Отработанные часы за период",
        "СМЕНЫ" to "Количество отработанных смен за период",
        "ПЛАН" to "Плановый заработок за период",
        "ПЛАН_ЧАСЫ" to "Плановые часы за период",
        "СТАВКА" to "Почасовая ставка",
        "МЕСЯЦ" to "Заработок за весь месяц",
        "ВЫПЛАЧЕНО" to "Другие выплаты за этот месяц",
    )

    private val ALIASES: Map<String, String> = mapOf(
        "EARNED" to "ЗАРАБОТОК",
        "HOURS" to "ЧАСЫ",
        "SHIFTS" to "СМЕНЫ",
        "PLANNED" to "ПЛАН",
        "PLANNED_HOURS" to "ПЛАН_ЧАСЫ",
        "RATE" to "СТАВКА",
        "MONTH" to "МЕСЯЦ",
        "PAID" to "ВЫПЛАЧЕНО",
    )

    class FormulaException(message: String) : Exception(message)

    /** Evaluates the expression; throws [FormulaException] with a Russian message on errors. */
    fun evaluate(expression: String, variables: Map<String, Double>): Double {
        val normalized = variables.mapKeys { it.key.uppercase() }
        val parser = Parser(tokenize(expression), normalized)
        val value = parser.parseExpression()
        parser.expectEnd()
        if (value.isNaN() || value.isInfinite()) throw FormulaException("Результат не является числом (деление на ноль?)")
        return value
    }

    /** Returns an error message, or null if the formula is valid. */
    fun validate(expression: String): String? = try {
        evaluate(expression, VARIABLES.keys.associateWith { 1.0 })
        null
    } catch (e: FormulaException) {
        e.message
    }

    private sealed interface Token {
        data class Number(val value: Double) : Token

        data class Identifier(val name: String) : Token

        data class Symbol(val char: Char) : Token
    }

    private fun tokenize(text: String): List<Token> {
        val tokens = ArrayList<Token>()
        var i = 0
        while (i < text.length) {
            val c = text[i]
            when {
                c.isWhitespace() -> i++
                c.isDigit() -> {
                    val start = i
                    while (i < text.length && text[i].isDigit()) i++
                    if (i + 1 < text.length && (text[i] == '.' || text[i] == ',') && text[i + 1].isDigit()) {
                        i++
                        while (i < text.length && text[i].isDigit()) i++
                    }
                    tokens += Token.Number(text.substring(start, i).replace(',', '.').toDouble())
                }
                c.isLetter() || c == '_' -> {
                    val start = i
                    while (i < text.length && (text[i].isLetterOrDigit() || text[i] == '_')) i++
                    tokens += Token.Identifier(text.substring(start, i).uppercase())
                }
                c in "+-*/()%;" -> {
                    tokens += Token.Symbol(c)
                    i++
                }
                c == '×' -> {
                    tokens += Token.Symbol('*')
                    i++
                }
                c == '−' -> {
                    tokens += Token.Symbol('-')
                    i++
                }
                else -> throw FormulaException("Недопустимый символ «$c»")
            }
        }
        return tokens
    }

    private class Parser(private val tokens: List<Token>, private val variables: Map<String, Double>) {
        private var pos = 0

        private fun peek(): Token? = tokens.getOrNull(pos)

        private fun isSymbol(c: Char): Boolean = (peek() as? Token.Symbol)?.char == c

        fun expectEnd() {
            if (pos < tokens.size) throw FormulaException("Лишние символы в конце формулы")
        }

        fun parseExpression(): Double {
            var value = parseTerm()
            while (true) {
                value = when {
                    isSymbol('+') -> {
                        pos++
                        value + parseTerm()
                    }
                    isSymbol('-') -> {
                        pos++
                        value - parseTerm()
                    }
                    else -> return value
                }
            }
        }

        private fun parseTerm(): Double {
            var value = parseUnary()
            while (true) {
                value = when {
                    isSymbol('*') -> {
                        pos++
                        value * parseUnary()
                    }
                    isSymbol('/') -> {
                        pos++
                        val divisor = parseUnary()
                        if (divisor == 0.0) throw FormulaException("Деление на ноль")
                        value / divisor
                    }
                    else -> return value
                }
            }
        }

        private fun parseUnary(): Double {
            if (isSymbol('-')) {
                pos++
                return -parseUnary()
            }
            if (isSymbol('+')) {
                pos++
                return parseUnary()
            }
            var value = parsePrimary()
            while (isSymbol('%')) {
                pos++
                value /= 100.0
            }
            return value
        }

        private fun parsePrimary(): Double {
            val token = peek() ?: throw FormulaException("Неожиданный конец формулы")
            return when (token) {
                is Token.Number -> {
                    pos++
                    token.value
                }
                is Token.Identifier -> {
                    pos++
                    if (isSymbol('(')) {
                        pos++
                        val args = ArrayList<Double>()
                        if (!isSymbol(')')) {
                            args += parseExpression()
                            while (isSymbol(';')) {
                                pos++
                                args += parseExpression()
                            }
                        }
                        if (!isSymbol(')')) throw FormulaException("Ожидается «)»")
                        pos++
                        callFunction(token.name, args)
                    } else {
                        val name = ALIASES[token.name] ?: token.name
                        variables[name] ?: throw FormulaException("Неизвестная переменная «${token.name}»")
                    }
                }
                is Token.Symbol -> {
                    if (token.char == '(') {
                        pos++
                        val value = parseExpression()
                        if (!isSymbol(')')) throw FormulaException("Ожидается «)»")
                        pos++
                        value
                    } else {
                        throw FormulaException("Неожиданный символ «${token.char}»")
                    }
                }
            }
        }

        private fun callFunction(name: String, args: List<Double>): Double = when (name) {
            "MIN", "МИН" -> {
                if (args.isEmpty()) throw FormulaException("min требует аргументы")
                args.reduce { a, b -> min(a, b) }
            }
            "MAX", "МАКС" -> {
                if (args.isEmpty()) throw FormulaException("max требует аргументы")
                args.reduce { a, b -> max(a, b) }
            }
            "ROUND", "ОКРУГЛ" -> when (args.size) {
                1 -> round(args[0])
                2 -> {
                    val factor = Math.pow(10.0, args[1])
                    round(args[0] * factor) / factor
                }
                else -> throw FormulaException("round принимает 1 или 2 аргумента")
            }
            else -> throw FormulaException("Неизвестная функция «$name»")
        }
    }
}
