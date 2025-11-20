/** @author Younes Belgroune - Password recovery functionality Made with the help of AI */
package com.swent.skillswap.ui.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.swent.skillswap.resources.config.ValidationConfig
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/** Represents one-time navigation events for the Password Recovery flow. */
sealed class PasswordRecoveryEvent {
    /** Navigate back to the Sign-In screen after successful password reset email sent. */
    object NavigateToSignIn : PasswordRecoveryEvent()
}

/** UI state for the Password Recovery screen. */
data class PasswordRecoveryUIState(
    val email: String = "",
    val emailError: String = "",
    val isLoading: Boolean = false,
    val successMessage: String = "",
    val errorMessage: String = ""
)

/**
 * ViewModel responsible for managing password recovery logic.
 *
 * Handles:
 * - Sending password reset email via Firebase Auth
 * - Email validation
 * - Error handling and success feedback
 */
class PasswordRecoveryViewModel(private val auth: FirebaseAuth = FirebaseAuth.getInstance()) :
    ViewModel() {

    private val _uiState: MutableStateFlow<PasswordRecoveryUIState> =
        MutableStateFlow(PasswordRecoveryUIState())
    val uiState: StateFlow<PasswordRecoveryUIState> = _uiState

    private val _eventFlow = MutableSharedFlow<PasswordRecoveryEvent>()
    val eventFlow: SharedFlow<PasswordRecoveryEvent> = _eventFlow

    /** Updates the email field without affecting error fields. */
    fun onEmailChange(newEmail: String) {
        _uiState.update { current ->
            current.copy(email = newEmail, emailError = "", errorMessage = "", successMessage = "")
        }
    }

    /**
     * Sends a password reset email to the provided email address. Validates the email format before
     * sending.
     */
    fun sendPasswordResetEmail() {
        viewModelScope.launch {
            if (!validateEmail()) {
                return@launch
            }

            _uiState.update { it.copy(isLoading = true, errorMessage = "", successMessage = "") }

            try {
                auth.sendPasswordResetEmail(_uiState.value.email).await()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        successMessage = "Password reset email sent! Check your inbox.",
                        errorMessage = ""
                    )
                }
                // UI will handle navigation based on success message state
                // No hardcoded delay - let user see success message and navigate when ready
            } catch (e: Exception) {
                Log.e("PasswordRecovery", "Error sending password reset email", e)
                val errorMsg =
                    when {
                        e.message?.contains("user-not-found") == true ->
                            "No account found with this email address."
                        e.message?.contains("invalid-email") == true -> "Invalid email address."
                        else -> "Failed to send reset email. Please try again."
                    }
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = errorMsg, successMessage = "")
                }
            }
        }
    }

    /**
     * Validates the email format using shared validation config. Updates the emailError field in UI
     * state.
     *
     * @return true if email is valid, false otherwise
     */
    private fun validateEmail(): Boolean {
        val email = _uiState.value.email
        val msg =
            when {
                email.isBlank() -> "Email cannot be empty"
                !ValidationConfig.EMAIL_REGEX.matches(email) -> "Invalid email format"
                else -> ""
            }
        _uiState.update { it.copy(emailError = msg) }
        return msg.isEmpty()
    }
}
