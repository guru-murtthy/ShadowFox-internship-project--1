package com.shadowfox.calculatorapp

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class CalculatorEngineTest {

    private lateinit var engine: CalculatorEngine

    @Before
    fun setUp() {
        engine = CalculatorEngine()
    }

    @Test
    fun testBasicAddition() {
        engine.appendDigit("5")
        engine.appendOperator("+")
        engine.appendDigit("3")
        assertEquals("5+3", engine.expression)
        assertEquals("8", engine.calculate())
    }

    @Test
    fun testPrecedence() {
        // 2 + 3 * 4 = 14
        engine.appendDigit("2")
        engine.appendOperator("+")
        engine.appendDigit("3")
        engine.appendOperator("×")
        engine.appendDigit("4")
        assertEquals("14", engine.calculate())
    }

    @Test
    fun testDivisionByZero() {
        engine.appendDigit("1")
        engine.appendDigit("0")
        engine.appendOperator("÷")
        engine.appendDigit("0")
        assertEquals("Cannot divide by zero", engine.calculate())
    }

    @Test
    fun testPreventMultipleDots() {
        engine.appendDigit("1")
        engine.appendDot()
        engine.appendDigit("2")
        engine.appendDot() // Should be ignored
        engine.appendDigit("5")
        assertEquals("1.25", engine.expression)
    }

    @Test
    fun testDotResetOnOperator() {
        engine.appendDigit("1")
        engine.appendDot()
        engine.appendDigit("5")
        engine.appendOperator("+")
        engine.appendDigit("2")
        engine.appendDot() // Should be allowed for the second operand
        engine.appendDigit("3")
        assertEquals("1.5+2.3", engine.expression)
        assertEquals("3.8", engine.calculate())
    }

    @Test
    fun testBackspaceRecalculatesDot() {
        engine.appendDigit("1")
        engine.appendDot()
        engine.appendDigit("5")
        engine.backspace() // Removes "5", expression is "1."
        engine.backspace() // Removes ".", expression is "1"
        engine.appendDot() // Should be allowed again since we removed the previous dot
        engine.appendDigit("9")
        assertEquals("1.9", engine.expression)
    }

    @Test
    fun testScientificFunctions() {
        // sin(90) = 1
        engine.appendScientificFunction("sin")
        engine.appendDigit("9")
        engine.appendDigit("0")
        engine.appendParenthesis(")")
        assertEquals("sin(90)", engine.expression)
        assertEquals("1", engine.calculate())

        // sqrt(16) = 4
        engine.clear()
        engine.appendScientificFunction("sqrt")
        engine.appendDigit("1")
        engine.appendDigit("6")
        engine.appendParenthesis(")")
        assertEquals("4", engine.calculate())
    }

    @Test
    fun testModulus() {
        engine.appendDigit("1")
        engine.appendDigit("0")
        engine.appendOperator("%")
        engine.appendDigit("3")
        assertEquals("1", engine.calculate())
    }
}
