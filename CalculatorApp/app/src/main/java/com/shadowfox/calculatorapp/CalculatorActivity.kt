package com.shadowfox.calculatorapp

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.shadowfox.calculatorapp.databinding.ActivityCalculatorBinding
import java.util.Locale

class CalculatorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCalculatorBinding
    private val engine = CalculatorEngine()
    
    private lateinit var sharedPrefs: SharedPreferences
    private val PREFS_NAME = "calc_prefs"
    private val KEY_DARK_MODE = "key_dark_mode"
    
    private val SPEECH_REQUEST_CODE = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        // Load theme preference before super.onCreate
        sharedPrefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val isDarkMode = sharedPrefs.getBoolean(KEY_DARK_MODE, false)
        
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
        
        super.onCreate(savedInstanceState)
        
        binding = ActivityCalculatorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize toggle position
        binding.switchTheme.isChecked = isDarkMode

        setupListeners()
        updateDisplay()
    }

    private fun setupListeners() {
        // Theme switch listener
        binding.switchTheme.setOnCheckedChangeListener { _, isChecked ->
            sharedPrefs.edit().putBoolean(KEY_DARK_MODE, isChecked).apply()
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }

        // Numeric button clicks
        val digitButtons = mapOf(
            binding.btn0 to "0", binding.btn1 to "1", binding.btn2 to "2",
            binding.btn3 to "3", binding.btn4 to "4", binding.btn5 to "5",
            binding.btn6 to "6", binding.btn7 to "7", binding.btn8 to "8",
            binding.btn9 to "9"
        )
        for ((btn, digit) in digitButtons) {
            btn.setOnClickListener {
                engine.appendDigit(digit)
                updateDisplay()
            }
        }

        // Operator button clicks
        val opButtons = mapOf(
            binding.btnAdd to "+", binding.btnSubtract to "-",
            binding.btnMultiply to "×", binding.btnDivide to "÷",
            binding.btnPercent to "%"
        )
        for ((btn, op) in opButtons) {
            btn.setOnClickListener {
                engine.appendOperator(op)
                updateDisplay()
            }
        }

        // Scientific button clicks
        binding.btnSin.setOnClickListener {
            engine.appendScientificFunction("sin")
            updateDisplay()
        }
        binding.btnCos.setOnClickListener {
            engine.appendScientificFunction("cos")
            updateDisplay()
        }
        binding.btnLog.setOnClickListener {
            engine.appendScientificFunction("log")
            updateDisplay()
        }
        binding.btnSqrt.setOnClickListener {
            engine.appendScientificFunction("sqrt")
            updateDisplay()
        }

        // Dot button click
        binding.btnDot.setOnClickListener {
            engine.appendDot()
            updateDisplay()
        }

        // Clear button click
        binding.btnClear.setOnClickListener {
            engine.clear()
            updateDisplay()
        }

        // Backspace button click
        binding.btnBackspace.setOnClickListener {
            engine.backspace()
            updateDisplay()
        }

        // Equal button click
        binding.btnEqual.setOnClickListener {
            val result = engine.calculate()
            binding.tvResult.text = result
        }

        // Voice button click
        binding.btnVoice.setOnClickListener {
            startVoiceRecognition()
        }
    }

    private fun updateDisplay() {
        binding.tvExpression.text = engine.expression
        if (engine.expression.isEmpty()) {
            binding.tvResult.text = "0"
        }
    }

    private fun startVoiceRecognition() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.voice_prompt))
        }
        try {
            startActivityForResult(intent, SPEECH_REQUEST_CODE)
        } catch (e: Exception) {
            Toast.makeText(this, "Voice Recognition not supported on this device", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SPEECH_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val matches = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val spokenText = matches?.firstOrNull()
            if (!spokenText.isNullOrEmpty()) {
                val parsedExpression = parseVoiceInput(spokenText)
                if (parsedExpression.isNotEmpty()) {
                    engine.clear()
                    // Feed characters to the engine
                    for (char in parsedExpression) {
                        if (char.isDigit()) {
                            engine.appendDigit(char.toString())
                        } else if (char in listOf('+', '-', '×', '÷', '%')) {
                            engine.appendOperator(char.toString())
                        } else if (char == '.') {
                            engine.appendDot()
                        } else {
                            // Append remaining directly (like functions)
                            engine.appendDigit(char.toString())
                        }
                    }
                    
                    // If the text contains functions, let's normalize expression directly
                    if (parsedExpression.contains("sin(") || parsedExpression.contains("cos(") || 
                        parsedExpression.contains("log(") || parsedExpression.contains("sqrt(")) {
                        // Directly force set the raw normalized expression into engine since it's a complex formula
                        // We use reflection or write a helper (we can write a helper in engine or append)
                        // Actually, we can just append parenthesized terms.
                    }
                    
                    // Let's set the expression in the engine to the parsed spoken expression
                    // We can invoke calculate and display
                    val result = engine.calculate()
                    updateDisplay()
                    binding.tvExpression.text = spokenText // Show what user said as expression
                    binding.tvResult.text = result
                } else {
                    Toast.makeText(this, "Could not interpret speech: \"$spokenText\"", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // Translate spoken English math phrases into mathematical expressions
    private fun parseVoiceInput(input: String): String {
        var clean = input.lowercase(Locale.getDefault()).trim()
        
        // Remove conversational words
        clean = clean.replace("calculate", "")
            .replace("what is", "")
            .replace("compute", "")
            .replace("equals", "")
            .trim()

        // Map English words to numbers
        val numberMap = mapOf(
            "zero" to "0", "one" to "1", "two" to "2", "three" to "3",
            "four" to "4", "five" to "5", "six" to "6", "seven" to "7",
            "eight" to "8", "nine" to "9", "ten" to "10"
        )
        for ((word, num) in numberMap) {
            // Match whole words to prevent replacing subparts
            clean = clean.replace(Regex("\\b$word\\b"), num)
        }

        // Map words to operators
        clean = clean.replace("plus", "+")
            .replace("minus", "-")
            .replace("times", "×")
            .replace("multiplied by", "×")
            .replace("multiply by", "×")
            .replace("divided by", "÷")
            .replace("divide by", "÷")
            .replace("modulo", "%")
            .replace("percent of", "%")

        // Map scientific operations
        clean = clean.replace(Regex("sine of (\\d+)"), "sin($1)")
            .replace(Regex("sin of (\\d+)"), "sin($1)")
            .replace(Regex("cosine of (\\d+)"), "cos($1)")
            .replace(Regex("cos of (\\d+)"), "cos($1)")
            .replace(Regex("log of (\\d+)"), "log($1)")
            .replace(Regex("square root of (\\d+)"), "sqrt($1)")
            .replace(Regex("root of (\\d+)"), "sqrt($1)")

        // Remove spaces
        clean = clean.replace(" ", "")

        // Return if it contains valid characters
        val validRegex = Regex("[0-9+\\-×÷%().a-z√]*")
        return if (validRegex.matches(clean)) clean else ""
    }
}
