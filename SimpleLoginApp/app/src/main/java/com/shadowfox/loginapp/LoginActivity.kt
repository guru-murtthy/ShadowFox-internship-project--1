package com.shadowfox.loginapp

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.shadowfox.loginapp.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var viewModel: LoginViewModel
    private lateinit var firebaseHelper: FirebaseHelper
    private lateinit var biometricHelper: BiometricHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize helper objects
        viewModel = ViewModelProvider(this)[LoginViewModel::class.java]
        firebaseHelper = FirebaseHelper(this)
        biometricHelper = BiometricHelper(this)

        setupListeners()
        setupObservers()
        restoreInputFields()
    }

    private fun setupListeners() {
        // Track changes to inputs and update ViewModel (survives rotation)
        binding.etEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.emailText = s?.toString() ?: ""
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.etPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.passwordText = s?.toString() ?: ""
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Login button click handler
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString()

            if (viewModel.validateInputs(email, password)) {
                viewModel.setLoading(true)
                // Authentication logic - Firebase with fallback
                firebaseHelper.login(email, password) { success, errorMessage ->
                    viewModel.setLoading(false)
                    if (success) {
                        viewModel.setSuccess(email)
                    } else {
                        viewModel.incrementAttemptCount()
                        viewModel.setFailure(errorMessage ?: "Authentication failed")
                    }
                }
            }
        }

        // Signup button click handler
        binding.btnSignup.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString()

            if (viewModel.validateInputs(email, password)) {
                viewModel.setLoading(true)
                firebaseHelper.signUp(email, password) { success, errorMessage ->
                    viewModel.setLoading(false)
                    if (success) {
                        Toast.makeText(
                            this@LoginActivity,
                            getString(R.string.signup_success),
                            Toast.LENGTH_SHORT
                        ).show()
                        viewModel.setSuccess(email)
                    } else {
                        viewModel.setFailure(errorMessage ?: "Registration failed")
                    }
                }
            }
        }

        // Biometric unlock click handler
        binding.btnBiometric.setOnClickListener {
            triggerBiometricAuthentication()
        }
    }

    private fun setupObservers() {
        // Observe Email validation errors
        viewModel.emailError.observe(this) { error ->
            binding.layoutEmail.error = error
        }

        // Observe Password validation errors
        viewModel.passwordError.observe(this) { error ->
            binding.layoutPassword.error = error
        }

        // Observe loading state
        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnLogin.isEnabled = !isLoading
            binding.btnSignup.isEnabled = !isLoading
            binding.btnBiometric.isEnabled = !isLoading
        }

        // Observe Authentication state outcomes
        viewModel.authState.observe(this) { state ->
            when (state) {
                is LoginViewModel.AuthState.Success -> {
                    Toast.makeText(
                        this,
                        getString(R.string.login_success),
                        Toast.LENGTH_SHORT
                    ).show()
                    navigateToWelcome(state.email)
                    viewModel.resetAuthState()
                }
                is LoginViewModel.AuthState.Failure -> {
                    Toast.makeText(
                        this,
                        getString(R.string.login_failed, state.message),
                        Toast.LENGTH_LONG
                    ).show()
                    viewModel.resetAuthState()
                }
                else -> { /* Idle */ }
            }
        }
    }

    private fun restoreInputFields() {
        // Restore text in fields if they were saved in the ViewModel (survives rotation)
        if (viewModel.emailText.isNotEmpty()) {
            binding.etEmail.setText(viewModel.emailText)
        }
        if (viewModel.passwordText.isNotEmpty()) {
            binding.etPassword.setText(viewModel.passwordText)
        }
        
        // Hide biometric option if hardware is not available
        if (!biometricHelper.canAuthenticate()) {
            binding.btnBiometric.visibility = View.GONE
        }
    }

    private fun triggerBiometricAuthentication() {
        biometricHelper.authenticate(
            title = getString(R.string.biometric_title),
            subtitle = getString(R.string.biometric_subtitle),
            negativeButtonText = getString(R.string.biometric_cancel),
            onSuccess = {
                val userEmail = firebaseHelper.getCurrentUserEmail() ?: "biometric_user@example.com"
                viewModel.setSuccess(userEmail)
            },
            onError = { errorCode, errString ->
                Toast.makeText(
                    this,
                    "Biometric error: $errString ($errorCode)",
                    Toast.LENGTH_SHORT
                ).show()
            },
            onFailed = {
                Toast.makeText(
                    this,
                    "Biometric recognition failed",
                    Toast.LENGTH_SHORT
                ).show()
            }
        )
    }

    private fun navigateToWelcome(email: String) {
        val intent = Intent(this, WelcomeActivity::class.java).apply {
            putExtra("EXTRA_EMAIL", email)
            putExtra("EXTRA_ATTEMPTS", viewModel.loginAttemptCount)
        }
        startActivity(intent)
        finish()
    }
}
