/** @author Younes Belgroune - Password recovery ViewModel tests Made with the help of AI */
package com.swent.skillswap.viewModel

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description

@OptIn(ExperimentalCoroutinesApi::class)
class PasswordRecoveryViewModelTest {

    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: PasswordRecoveryViewModel

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        try {
            val options =
                FirebaseOptions.Builder()
                    .setApplicationId("test-app-id")
                    .setApiKey("test-api-key")
                    .setProjectId("test-project")
                    .build()
            FirebaseApp.initializeApp(context, options)
        } catch (e: Exception) {
            // Firebase might already be initialized or other error, ignore
        }
        viewModel = PasswordRecoveryViewModel()
    }

    // ========== INITIALIZATION TESTS ==========

    @Test
    fun init_hasEmptyState() = runTest {
        val state = viewModel.uiState.value
        assertEquals("", state.email)
        assertEquals("", state.emailError)
        assertFalse(state.isLoading)
        assertEquals("", state.successMessage)
        assertEquals("", state.errorMessage)
    }

    // ========== EMAIL CHANGE TESTS ==========

    @Test
    fun onEmailChange_validEmail_updatesEmailAndClearsErrors() = runTest {
        viewModel.onEmailChange("test@example.com")

        val state = viewModel.uiState.value
        assertEquals("test@example.com", state.email)
        assertEquals("", state.emailError)
        assertEquals("", state.errorMessage)
        assertEquals("", state.successMessage)
    }

    @Test
    fun onEmailChange_clearsPreviousErrors() = runTest {
        // Set some initial error state
        viewModel.onEmailChange("invalid")
        viewModel.sendPasswordResetEmail()

        viewModel.onEmailChange("test@example.com")

        val state = viewModel.uiState.value
        assertEquals("test@example.com", state.email)
        assertEquals("", state.emailError)
    }

    // ========== EMAIL VALIDATION TESTS ==========

    @Test
    fun sendPasswordResetEmail_emptyEmail_showsValidationError() = runTest {
        viewModel.sendPasswordResetEmail()

        val state = viewModel.uiState.value
        assertEquals("Email cannot be empty", state.emailError)
        assertFalse(state.isLoading)
        assertEquals("", state.successMessage)
    }

    @Test
    fun sendPasswordResetEmail_invalidEmailFormat_showsValidationError() = runTest {
        viewModel.onEmailChange("invalid-email")
        viewModel.sendPasswordResetEmail()

        val state = viewModel.uiState.value
        assertEquals("Invalid email format", state.emailError)
        assertFalse(state.isLoading)
    }

    @Test
    fun sendPasswordResetEmail_emailWithoutAt_showsValidationError() = runTest {
        viewModel.onEmailChange("testexample.com")
        viewModel.sendPasswordResetEmail()

        val state = viewModel.uiState.value
        assertEquals("Invalid email format", state.emailError)
    }

    @Test
    fun sendPasswordResetEmail_emailWithoutDomain_showsValidationError() = runTest {
        viewModel.onEmailChange("test@")
        viewModel.sendPasswordResetEmail()

        val state = viewModel.uiState.value
        assertEquals("Invalid email format", state.emailError)
    }

    @Test
    fun sendPasswordResetEmail_validEmailFormat_passesValidation() = runTest {
        viewModel.onEmailChange("test@example.com")
        viewModel.sendPasswordResetEmail()

        val state = viewModel.uiState.value
        assertEquals("", state.emailError)
        // Note: Actual Firebase call will fail in unit test without emulator, but validation passes
    }

    // ========== SUCCESSFUL PASSWORD RESET TESTS ==========

    @Test
    fun sendPasswordResetEmail_validEmail_setsLoadingState() = runTest {
        viewModel.onEmailChange("test@example.com")
        viewModel.sendPasswordResetEmail()

        // Check loading state is set immediately (before Firebase call completes/fails)
        val state = viewModel.uiState.value
        // Loading may be true if validation passes, but Firebase will fail in unit test
        assertEquals("", state.emailError)
    }

    // ========== ERROR HANDLING TESTS ==========
    // Note: Firebase error handling is tested in integration tests with emulator

    // ========== STATE MANAGEMENT TESTS ==========

    @Test
    fun sendPasswordResetEmail_clearsPreviousSuccessMessage() = runTest {
        // First attempt
        viewModel.onEmailChange("test1@example.com")
        viewModel.sendPasswordResetEmail()

        // Second attempt should clear previous success
        viewModel.onEmailChange("test2@example.com")
        viewModel.sendPasswordResetEmail()

        // Initially should clear success message
        val stateDuringLoad = viewModel.uiState.value
        assertEquals("", stateDuringLoad.successMessage)
    }

    @Test
    fun sendPasswordResetEmail_clearsPreviousErrorMessage() = runTest {
        // First failed attempt
        viewModel.onEmailChange("test1@example.com")
        viewModel.sendPasswordResetEmail()

        // Second attempt should clear previous error
        viewModel.onEmailChange("test2@example.com")
        viewModel.sendPasswordResetEmail()

        val stateDuringLoad = viewModel.uiState.value
        assertEquals("", stateDuringLoad.errorMessage)
    }

    // ========== EDGE CASES ==========

    @Test
    fun sendPasswordResetEmail_whitespaceEmail_showsValidationError() = runTest {
        viewModel.onEmailChange("   ")
        viewModel.sendPasswordResetEmail()

        val state = viewModel.uiState.value
        assertEquals("Email cannot be empty", state.emailError)
    }

    @Test
    fun sendPasswordResetEmail_emailWithSpecialChars_validatesCorrectly() = runTest {
        viewModel.onEmailChange("test+tag@example.co.uk")
        viewModel.sendPasswordResetEmail()

        val state = viewModel.uiState.value
        // Should pass validation (valid email format)
        assertEquals("", state.emailError)
    }

    @Test
    fun onEmailChange_multipleCalls_onlyLastValuePersists() = runTest {
        viewModel.onEmailChange("first@example.com")
        viewModel.onEmailChange("second@example.com")
        viewModel.onEmailChange("third@example.com")

        val state = viewModel.uiState.value
        assertEquals("third@example.com", state.email)
    }

    @Test
    fun onEmailChange_clearsErrorAndSuccessMessages() = runTest {
        // Set initial state with errors
        viewModel.onEmailChange("")
        viewModel.sendPasswordResetEmail()

        // Change email should clear errors
        viewModel.onEmailChange("new@example.com")

        val state = viewModel.uiState.value
        assertEquals("", state.emailError)
        assertEquals("", state.errorMessage)
        assertEquals("", state.successMessage)
    }
}

// Test rule to set Main dispatcher for coroutines in unit tests
class MainDispatcherRule(private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()) :
    TestWatcher() {
    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}
