package com.shadowfox.calculatorapp

import java.util.Locale
import kotlin.math.cos
import kotlin.math.log10
import kotlin.math.sin
import kotlin.math.sqrt

class CalculatorEngine {

    var expression: String = ""
        private set

    private var hasDot: Boolean = false

    private val operators = setOf('+', '-', '×', '÷', '%')

    fun appendDigit(digit: String) {
        expression += digit
    }

    fun appendDot() {
        if (!hasDot) {
            val lastChar = expression.lastOrNull()
            if (expression.isEmpty() || lastChar in operators || lastChar == '(') {
                expression += "0."
            } else {
                expression += "."
            }
            hasDot = true
        }
    }

    fun appendOperator(op: String) {
        if (expression.isEmpty()) {
            if (op == "-") {
                expression += "-"
                hasDot = false
            }
            return
        }

        val lastChar = expression.last()
        if (lastChar in operators) {
            // Replace the last operator with the new one
            expression = expression.dropLast(1) + op
        } else if (lastChar != '(') {
            expression += op
        }
        hasDot = false
    }

    fun appendScientificFunction(func: String) {
        expression += "$func("
        hasDot = false
    }

    fun appendParenthesis(paren: String) {
        expression += paren
        if (paren == ")") {
            // Re-evaluate hasDot by looking back to the last operator or parenthesis
            reEvaluateHasDot()
        } else {
            hasDot = false
        }
    }

    fun backspace() {
        if (expression.isNotEmpty()) {
            val removedChar = expression.last()
            expression = expression.dropLast(1)
            
            // Handle drop of functions like "sin(", "cos(", "log(", "sqrt("
            if (expression.endsWith("sin") || expression.endsWith("cos") || expression.endsWith("log")) {
                expression = expression.dropLast(3)
            } else if (expression.endsWith("sqrt")) {
                expression = expression.dropLast(4)
            }
            
            reEvaluateHasDot()
        }
    }

    fun clear() {
        expression = ""
        hasDot = false
    }

    private fun reEvaluateHasDot() {
        // Look back from the end of the expression to the last operator or function start
        // to see if the current active number contains a decimal point
        if (expression.isEmpty()) {
            hasDot = false
            return
        }
        var foundDot = false
        for (i in expression.indices.reversed()) {
            val c = expression[i]
            if (c == '.') {
                foundDot = true
                break
            }
            if (c in operators || c == '(' || c == ')') {
                break
            }
        }
        hasDot = foundDot
    }

    fun calculate(): String {
        if (expression.isEmpty()) return "0"
        
        // Normalize expression for parsing
        var normalized = expression
            .replace("×", "*")
            .replace("÷", "/")
        
        // Auto-close open parentheses
        val openCount = normalized.count { it == '(' }
        val closeCount = normalized.count { it == ')' }
        if (openCount > closeCount) {
            normalized += ")".repeat(openCount - closeCount)
        }

        return try {
            val eval = ExpressionEvaluator(normalized).parse()
            if (eval.isNaN()) {
                "Error"
            } else if (eval.isInfinite()) {
                if (eval < 0) "-Infinity" else "Cannot divide by zero"
            } else {
                // Formatting double to remove trailing decimals if it is an integer
                val formatted = String.format(Locale.US, "%.8f", eval)
                val trimmed = formatted.trimEnd('0').trimEnd('.')
                if (trimmed == "-0") "0" else trimmed
            }
        } catch (e: ArithmeticException) {
            e.message ?: "Cannot divide by zero"
        } catch (e: Exception) {
            "Error"
        }
    }

    // Custom Recursive Descent Parser for expression evaluation
    private class ExpressionEvaluator(private val str: String) {
        private var pos = -1
        private var ch = ' '

        private fun nextChar() {
            pos++
            ch = if (pos < str.length) str[pos] else '\u0000'
        }

        private fun eat(charToEat: Char): Boolean {
            while (ch == ' ') nextChar()
            if (ch == charToEat) {
                nextChar()
                return true
            }
            return false
        }

        fun parse(): Double {
            nextChar()
            val x = parseExpression()
            if (pos < str.length) throw RuntimeException("Unexpected character: $ch")
            return x
        }

        // Expression = Term | Expression '+' Term | Expression '-' Term
        private fun parseExpression(): Double {
            var x = parseTerm()
            while (true) {
                if (eat('+')) x += parseTerm() // addition
                else if (eat('-')) x -= parseTerm() // subtraction
                else return x
            }
        }

        // Term = Factor | Term '*' Factor | Term '/' Factor
        private fun parseTerm(): Double {
            var x = parseFactor()
            while (true) {
                if (eat('*')) x *= parseFactor() // multiplication
                else if (eat('/')) {
                    val denominator = parseFactor()
                    if (denominator == 0.0) {
                        throw ArithmeticException("Cannot divide by zero")
                    }
                    x /= denominator // division
                } else if (eat('%')) {
                    val divisor = parseFactor()
                    if (divisor == 0.0) {
                        throw ArithmeticException("Cannot divide by zero")
                    }
                    x %= divisor // modulus
                } else return x
            }
        }

        // Factor = '+' Factor | '-' Factor | '(' Expression ')' | number | FunctionName '(' Expression ')'
        private fun parseFactor(): Double {
            if (eat('+')) return parseFactor() // unary plus
            if (eat('-')) return -parseFactor() // unary minus

            var x: Double
            val startPos = this.pos
            if (eat('(')) { // parentheses
                x = parseExpression()
                eat(')')
            } else if ((ch in '0'..'9') || ch == '.') { // numbers
                while ((ch in '0'..'9') || ch == '.') nextChar()
                x = str.substring(startPos, this.pos).toDouble()
            } else if (ch in 'a'..'z' || ch == '√') { // functions
                while (ch in 'a'..'z' || ch == '√') nextChar()
                val func = str.substring(startPos, this.pos)
                eat('(')
                x = parseExpression()
                eat(')')
                x = when (func) {
                    "sqrt", "√" -> {
                        if (x < 0) Double.NaN else sqrt(x)
                    }
                    "sin" -> sin(Math.toRadians(x)) // Standard Trigonometry uses degrees
                    "cos" -> cos(Math.toRadians(x))
                    "log" -> {
                        if (x <= 0) Double.NaN else log10(x)
                    }
                    else -> throw RuntimeException("Unknown function: $func")
                }
            } else {
                throw RuntimeException("Unexpected: " + ch)
            }

            return x
        }
    }
}
