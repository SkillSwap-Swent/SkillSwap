/** @author Topaze17 (Eliott) Used ChatGPT commenting, but all comments were checked manually. */
package com.swent.skillswap.viewModel

import android.app.Activity
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.swent.skillswap.model.SignIn.SignInClassicModel
import com.swent.skillswap.model.SignIn.SignInClassicParams
import com.swent.skillswap.model.SignIn.SignInGoogleModel
import com.swent.skillswap.model.SignIn.SignInGoogleParams
import com.swent.skillswap.resources.ValidationConfig
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Represents one-time navigation events for the Sign-In flow. These are emitted from the ViewModel
 * and collected by the UI layer to perform navigation or show transient UI states.
 */
sealed class SignInEvent {
    /** Navigate to the app's main screen (user already has account info). */
    object NavigateToMainScreen : SignInEvent()

    /** Navigate to the Create Account screen (first-time Google sign-in). */
    object NavigateToCreateAccountScreen : SignInEvent()
}
/**
 * Represents the current state of the Sign-In screen UI.
 *
 * Holds user inputs and validation error messages so that the UI can reactively update fields and
 * display errors as needed.
 */
data class SignInUIState(
    val email: String = "",
    val password: String = "",
    val emailError: String = "",
    val passwordError: String = "",
)

/**
 * ViewModel responsible for managing Sign-In logic, user validation, and navigation events.
 *
 * Responsibilities:
 * - Handles both **Google Sign-In** (via [CredentialManager]) and **Classic Sign-In**.
 * - Validates user input (email, password).
 * - Emits [SignInEvent]s for navigation to the appropriate screen.
 * - Coordinates Firebase Authentication logic through [SignInGoogleModel] and [SignInClassicModel].
 */
class SignInViewModel(private val auth: FirebaseAuth = FirebaseAuth.getInstance()) : ViewModel() {

    private val _uiState: MutableStateFlow<SignInUIState> =
        MutableStateFlow<SignInUIState>(SignInUIState())
    val uiState: StateFlow<SignInUIState> = _uiState
    // The sign-in model abstraction. We use the Google-specific implementation here.
    private val googleModel: SignInGoogleModel =
        SignInGoogleModel(auth) // Handles Google sign-in logic
    private val classicModel: SignInClassicModel =
        SignInClassicModel(auth) // Handles email/password sign-in
    // SharedFlow used for one-time UI events (navigation actions).
    // Unlike StateFlow, SharedFlow won't re-emit old events when the UI recomposes.
    private val _eventFlow = MutableSharedFlow<SignInEvent>()
    val eventFlow: SharedFlow<SignInEvent> = _eventFlow // Public read-only access
    /**
     * Checks whether the current user already has a valid account record in Firestore. If so,
     * automatically navigates to the main screen.
     *
     * This is primarily used when returning to the Create Account screen to skip redundant account
     * creation for existing users.
     */
    fun check() {
        if (auth.currentUser != null) {
            val isGoogleUser = auth.currentUser?.providerData?.any { it.providerId == "google.com" }
            if (isGoogleUser == true) {
                viewModelScope.launch {
                    if (googleModel.googleAccountInfoAreSavedInFirestore()) {
                        _eventFlow.emit(SignInEvent.NavigateToMainScreen)
                    } else {
                        _eventFlow.emit(SignInEvent.NavigateToCreateAccountScreen)
                    }
                }
            } else {
                viewModelScope.launch { _eventFlow.emit(SignInEvent.NavigateToMainScreen) }
            }
        }
    }

    /**
     * Initiates Google Sign-In flow using the CredentialManager.
     *
     * @param credentialManager Handles user credential retrieval (sign-in process).
     * @param activity The current activity context required by CredentialManager.
     *
     * Once sign-in completes:
     * - If user data already exists in Firestore → navigate to main screen.
     * - Otherwise → navigate to Create Account screen to collect user info.
     */
    fun googleSignIn(credentialManager: CredentialManager, activity: Activity) =
        viewModelScope.launch {
            try {
                // Perform Google sign-in using provided credentials.
                googleModel.signIn(SignInGoogleParams(activity, credentialManager))
                try {
                    // Check if user’s Google account info already exists in Firestore.
                    if (googleModel.googleAccountInfoAreSavedInFirestore()) {
                        // User exists → go to main app.
                        _eventFlow.emit(SignInEvent.NavigateToMainScreen)
                    } else {
                        // New Google user → go to account creation.
                        _eventFlow.emit(SignInEvent.NavigateToCreateAccountScreen)
                    }
                } catch (e: Exception) {
                    Log.w("Info Check", "Firestore Error", e)
                }
            } catch (e: Exception) {
                Log.w("SignIn", "Credential Error", e)
            }
        }
    /** Updates the email without affecting any error fields. */
    fun onEmailChange(newEmail: String) {
        _uiState.update { current -> current.copy(email = newEmail) }
    }
    /** Updates the password without affecting any error fields. */
    fun onPasswordChange(newPassword: String) {
        _uiState.update { current -> current.copy(password = newPassword) }
    }
    /**
     * Handle classical Login. Triggered when user chooses to log in with email/password instead of
     * Google.
     */
    fun classicSignIn() =
        viewModelScope.launch {
            if (validateInputs()) {
                try {
                    classicModel.signIn(
                        SignInClassicParams(uiState.value.email, uiState.value.password)
                    )
                    _eventFlow.emit(SignInEvent.NavigateToMainScreen)
                } catch (e: Exception) {
                    _uiState.update { it.copy(passwordError = "Email or password incorrect") }
                }
            }
        }

    /** Handles navigation to the Create Account screen directly (manual registration path). */
    fun createAccount() =
        viewModelScope.launch { _eventFlow.emit(SignInEvent.NavigateToCreateAccountScreen) }

    // Use shared email validation regex from ValidationConfig
    private val emailRegex = ValidationConfig.EMAIL_REGEX

    private fun validateEmail(): Boolean {
        val email = _uiState.value.email
        val msg =
            when {
                email.isBlank() -> "Email cannot be empty"
                !emailRegex.matches(email) -> "Invalid email format"
                else -> ""
            }
        _uiState.update { it.copy(emailError = msg) }
        return msg.isEmpty()
    }
    /** Validates password rules */
    private fun validatePasswords(): Boolean {
        val pwd = _uiState.value.password

        val passwordError =
            when {
                pwd.isBlank() -> "Password cannot be empty"
                pwd.length < 8 -> "Password must be at least 8 characters long"
                !pwd.any { it.isUpperCase() } ->
                    "Password must contain at least one uppercase letter"
                else -> ""
            }

        _uiState.update { it.copy(passwordError = passwordError) }

        return passwordError.isEmpty()
    }
    // ---------- Aggregate Validators ----------

    /**
     * Runs all validation functions (username, email, password, skills) and returns true only if
     * all are valid.
     */
    fun validateInputs(): Boolean {
        val results = listOf(validateEmail(), validatePasswords())
        return results.all { it }
    }
}
