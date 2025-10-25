/** @author Topaze17 (Eliott) Used ChatGPT commenting, but all comments were checked manually. */
package com.swent.skillswap.viewModel

import android.app.Activity
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swent.skillswap.model.SignIn.SignInGoogleModel
import com.swent.skillswap.model.SignIn.SignInGoogleParams
import com.swent.skillswap.model.SignIn.SignInInterface
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
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

    /** Navigate to the Classic (email/password) sign-in screen. */
    object NavigateToClassicSignIn : SignInEvent()
}

/**
 * ViewModel responsible for managing Sign-In logic and emitting navigation events.
 *
 * Handles:
 * - Google Sign-In via CredentialManager
 * - Triggering navigation events based on authentication results
 * - Directing the UI to appropriate next steps (main screen or account creation)
 */
class SignInViewModel() : ViewModel() {

    // The sign-in model abstraction. We use the Google-specific implementation here.
    private val googleModel: SignInInterface = SignInGoogleModel()

    // SharedFlow used for one-time UI events (navigation actions).
    // Unlike StateFlow, SharedFlow won't re-emit old events when the UI recomposes.
    private val _eventFlow = MutableSharedFlow<SignInEvent>()
    val eventFlow: SharedFlow<SignInEvent> = _eventFlow // Public read-only access

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

                // Check if user’s Google account info already exists in Firestore.
                if ((googleModel as SignInGoogleModel).googleAccountInfoAreSavedInFirestore()) {
                    // User exists → go to main app.
                    _eventFlow.emit(SignInEvent.NavigateToMainScreen)
                } else {
                    // New Google user → go to account creation.
                    _eventFlow.emit(SignInEvent.NavigateToCreateAccountScreen)
                }
            } catch (e: Exception) {
                // Exception is caught but ignored; consider logging or showing error feedback.
            }
        }

    /**
     * Handles navigation to the Classic Sign-In screen. Triggered when user chooses to log in with
     * email/password instead of Google.
     */
    fun classicSignIn() =
        viewModelScope.launch { _eventFlow.emit(SignInEvent.NavigateToClassicSignIn) }

    /** Handles navigation to the Create Account screen directly (manual registration path). */
    fun createAccount() =
        viewModelScope.launch { _eventFlow.emit(SignInEvent.NavigateToCreateAccountScreen) }
}
