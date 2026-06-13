package com.shadowfox.loginapp

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class LoginValidationTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: LoginViewModel

    @Before
    fun setUp() {
        viewModel = LoginViewModel()
    }

    @Test
    fun testEmailPatternValidation_ValidEmails() {
        assertTrue(LoginViewModel.isValidEmailPattern("test@example.com"))
        assertTrue(LoginViewModel.isValidEmailPattern("user.name+tag@sub.domain.co.uk"))
        assertTrue(LoginViewModel.isValidEmailPattern("admin123@internal.site"))
    }

    @Test
    fun testEmailPatternValidation_InvalidEmails() {
        assertFalse(LoginViewModel.isValidEmailPattern("plainaddress"))
        assertFalse(LoginViewModel.isValidEmailPattern("@missingusername.com"))
        assertFalse(LoginViewModel.isValidEmailPattern("username@.com"))
        assertFalse(LoginViewModel.isValidEmailPattern("username@domain.c"))
    }

    @Test
    fun testValidateInputs_EmptyFields() {
        // Mock validations that don't rely on LiveData or Android main thread
        val isValid = viewModel.validateInputs("", "")
        assertFalse(isValid)
        assertEquals("Email cannot be empty", viewModel.emailError.value)
        assertEquals("Password cannot be empty", viewModel.passwordError.value)
    }

    @Test
    fun testValidateInputs_InvalidEmailFormat() {
        val isValid = viewModel.validateInputs("invalid-email", "password123")
        assertFalse(isValid)
        assertEquals("Please enter a valid email address", viewModel.emailError.value)
        assertNull(viewModel.passwordError.value)
    }

    @Test
    fun testValidateInputs_PasswordTooShort() {
        val isValid = viewModel.validateInputs("user@example.com", "123")
        assertFalse(isValid)
        assertNull(viewModel.emailError.value)
        assertEquals("Password must be at least 6 characters", viewModel.passwordError.value)
    }

    @Test
    fun testValidateInputs_CorrectInputs() {
        val isValid = viewModel.validateInputs("user@example.com", "password123")
        assertTrue(isValid)
        assertNull(viewModel.emailError.value)
        assertNull(viewModel.passwordError.value)
    }

    @Test
    fun testLoginAttemptCount_Incrementation() {
        assertEquals(0, viewModel.loginAttemptCount)
        viewModel.incrementAttemptCount()
        assertEquals(1, viewModel.loginAttemptCount)
        viewModel.incrementAttemptCount()
        assertEquals(2, viewModel.loginAttemptCount)
    }
}
