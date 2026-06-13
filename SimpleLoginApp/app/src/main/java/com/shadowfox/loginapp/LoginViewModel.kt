package com.shadowfox.loginapp

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class LoginViewModel : ViewModel() {

    // ViewModel variables survive orientation changes
    var emailText: String = ""
    var passwordText: String = ""
    var loginAttemptCount: Int = 0
        private set

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _emailError = MutableLiveData<String?>()
    val emailError: LiveData<String?> get() = _emailError

    private val _passwordError = MutableLiveData<String?>()
    val passwordError: LiveData<String?> get() = _passwordError

    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> get() = _authState

    sealed class AuthState {
        object Idle : AuthState()
        class Success(val email: String) : AuthState()
        class Failure(val message: String) : AuthState()
    }

    fun validateInputs(email: String, password: String): Boolean {
        var isValid = true

        if (email.trim().isEmpty()) {
            _emailError.value = "Email cannot be empty"
            isValid = false
        } else if (!isValidEmailPattern(email)) {
            _emailError.value = "Please enter a valid email address"
            isValid = false
        } else {
            _emailError.value = null
        }

        if (password.isEmpty()) {
            _passwordError.value = "Password cannot be empty"
            isValid = false
        } else if (password.length < 6) {
            _passwordError.value = "Password must be at least 6 characters"
            isValid = false
        } else {
            _passwordError.value = null
        }

        return isValid
    }

    fun incrementAttemptCount() {
        loginAttemptCount++
    }

    fun setSuccess(email: String) {
        _authState.value = AuthState.Success(email)
    }

    fun setFailure(message: String) {
        _authState.value = AuthState.Failure(message)
    }

    fun setLoading(loading: Boolean) {
        _isLoading.value = loading
    }

    fun resetAuthState() {
        _authState.value = AuthState.Idle
    }

    // Static helper for email pattern to make unit testing easy and independent of Android framework patterns
    companion object {
        private val EMAIL_REGEX = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$".toRegex()
        
        fun isValidEmailPattern(email: String): Boolean {
            return EMAIL_REGEX.matches(email)
        }
    }
}
