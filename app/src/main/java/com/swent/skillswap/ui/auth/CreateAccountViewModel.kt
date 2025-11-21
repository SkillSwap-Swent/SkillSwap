/** @author Topaze17 (Eliott) Used ChatGPT commenting, but all comments were checked manually. */
package com.swent.skillswap.ui.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.swent.skillswap.model.Auth.AuthClassicModel
import com.swent.skillswap.model.Auth.AuthGoogleModel
import com.swent.skillswap.model.Auth.CreateAccountClassicParams
import com.swent.skillswap.model.Auth.CreateAccountGoogleParams
import com.swent.skillswap.model.Auth.SignInInterface
import com.swent.skillswap.model.tags.SkillTag
import com.swent.skillswap.model.user.UserRepoFirestore
import com.swent.skillswap.model.utils.FCMTokenManager
import com.swent.skillswap.resources.config.ValidationConfig
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Represents one-time events (usually navigation or UI triggers) that occur during the Create
 * Account flow.
 */
sealed class CreateAccountEvent {

    /**
     * Event indicating that the user has successfully completed account creation and should be
     * navigated to the main screen.
     */
    object NavigateToMainScreen : CreateAccountEvent()

    object NavigateToEmail : CreateAccountEvent()
}
/**
 * Represents all UI state fields for the Create Account screen. This includes both user-entered
 * data and validation errors.
 */
data class CreateAccountUIState(
    val email: String = "",
    val username: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val skills: Set<SkillTag> = setOf<SkillTag>(),
    val emailError: String = "",
    val usernameError: String = "",
    val passwordError: String = "",
    val confirmPasswordError: String = "",
    val skillsError: String = "",
    val buttonEnabled: Boolean = false,
    val currentRoute: String = CreateAccountRoutes.USERNAME
)

/**
 * ViewModel responsible for managing all logic and UI state during the Create Account flow.
 *
 * Supports both:
 * - **Google account creation**, where email/password are already known
 * - **Classic account creation**, where the user provides credentials manually
 *
 * Responsibilities:
 * - Maintain [CreateAccountUIState] for reactive updates.
 * - Validate user input fields.
 * - Interact with [AuthGoogleModel] or [AuthClassicModel] to persist data.
 * - Emit [CreateAccountEvent]s for one-time navigation actions.
 */
class CreateAccountViewModel(
    private val isGoogleAccount: Boolean,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    val db: FirebaseFirestore = Firebase.firestore
) : ViewModel() {
    private val model: SignInInterface =
        if (isGoogleAccount) AuthGoogleModel(auth, db) else AuthClassicModel(auth, db)

    private val _uiState: MutableStateFlow<CreateAccountUIState> =
        MutableStateFlow<CreateAccountUIState>(CreateAccountUIState())
    // SharedFlow used to emit one-time navigation events to the UI layer.
    private val _eventFlow = MutableSharedFlow<CreateAccountEvent>()
    val eventFlow: SharedFlow<CreateAccountEvent> = _eventFlow

    val uiState: StateFlow<CreateAccountUIState> = _uiState
    // FCM token manager for saving push notification tokens
    private val fcmTokenManager: FCMTokenManager = FCMTokenManager(UserRepoFirestore(db), auth)

    /**
     * Checks whether the current user already has a valid account record in Firestore. If so,
     * automatically navigates to the main screen.
     *
     * This is primarily used when returning to the Create Account screen to skip redundant account
     * creation for existing users.
     */
    fun check() {
        if (isGoogleAccount && model is AuthGoogleModel) {
            viewModelScope.launch {
                if (model.googleAccountInfoAreSavedInFirestore()) {
                    _eventFlow.emit(CreateAccountEvent.NavigateToMainScreen)
                    Log.e("Check", "Google Account")
                }
            }
        } else if (auth.currentUser != null) {
            viewModelScope.launch {
                _eventFlow.emit(CreateAccountEvent.NavigateToMainScreen)
                Log.e("Check", "Password")
            }
        }
    }
    /** Updates the email without affecting any error fields. */
    fun onEmailChange(newEmail: String) {
        _uiState.update { current -> current.copy(email = newEmail) }
        refreshEnabled()
        refreshError()
    }

    /** Updates the username without affecting any error fields. */
    fun onUsernameChange(newUsername: String) {
        _uiState.update { current -> current.copy(username = newUsername) }
        refreshEnabled()
        refreshError()
    }

    /** Updates the password without affecting any error fields. */
    fun onPasswordChange(newPassword: String) {
        _uiState.update { current -> current.copy(password = newPassword) }
        refreshEnabled()
        refreshError()
    }

    /** Updates the confirm password field without affecting any error fields. */
    fun onConfirmPasswordChange(newConfirmPassword: String) {
        _uiState.update { current -> current.copy(confirmPassword = newConfirmPassword) }
        refreshEnabled()
        refreshError()
    }

    /** Adds a single skill to the selected skills set. */
    private fun addSkill(skill: SkillTag) {
        _uiState.update { current -> current.copy(skills = current.skills + skill) }
    }

    /** Removes a single skill from the selected skills set. */
    private fun removeSkill(skill: SkillTag) {
        _uiState.update { current -> current.copy(skills = current.skills - skill) }
    }

    /**
     * Toggles a skill selection — if already selected, remove it; otherwise, add it to the skill
     * set.
     */
    fun clickSkill(skill: SkillTag) {
        if (_uiState.value.skills.contains(skill)) removeSkill(skill) else addSkill(skill)
        refreshEnabled()
        refreshError()
    }

    // ---------- Validation Section ----------

    // ---------- VALIDATORS (return ok + cause, and can update UI) ----------
    private val emailRegex = ValidationConfig.EMAIL_REGEX
    val usernameRegex = ValidationConfig.USERNAME_REGEX

    private fun validateUsernameResult(): Pair<Boolean, String> {
        val username = _uiState.value.username

        val cause =
            when {
                username.isBlank() -> "Username cannot be empty"
                !usernameRegex.matches(username) ->
                    "Username must be 3–20 characters and contain only letters, numbers, or underscores"
                else -> ""
            }

        return (cause.isEmpty()) to cause
    }

    private fun validateEmailResult(): Pair<Boolean, String> {
        if (isGoogleAccount) return true to ""
        val e = _uiState.value.email
        val cause =
            when {
                e.isBlank() -> "Email cannot be empty"
                !emailRegex.matches(e) -> "Invalid email format"
                else -> ""
            }
        return (cause.isEmpty()) to cause
    }

    /**
     * Returns (ok, cause). The single 'cause' is the first failing reason. We'll route it to
     * passwordError or confirmPasswordError below.
     */
    private fun validatePasswordsResult(): Pair<Boolean, String> {
        if (isGoogleAccount) return true to ""
        val pwd = _uiState.value.password
        val confirm = _uiState.value.confirmPassword

        val cause =
            when {
                pwd.isBlank() -> "Password cannot be empty"
                pwd.length < 8 -> "Password must be at least 8 characters long"
                !pwd.any { it.isUpperCase() } ->
                    "Password must contain at least one uppercase letter"
                confirm.isBlank() -> "Please confirm your password"
                confirm != pwd -> "Passwords do not match"
                else -> ""
            }
        return (cause.isEmpty()) to cause
    }

    // ---------- Existing validate* wrappers now use the result + update UI ----------
    private fun validateUsername(): Boolean {
        if (_uiState.value.username.isEmpty()) return true
        val (ok, cause) = validateUsernameResult()
        _uiState.update { it.copy(usernameError = cause) }
        return ok
    }

    private fun validateEmail(): Boolean {
        val email = _uiState.value.email
        if (email.isEmpty()) return true
        val (ok, cause) = validateEmailResult()
        _uiState.update { it.copy(emailError = cause) }

        if (cause.isEmpty()) {
            emailCheckJob?.cancel()
            emailCheckJob =
                viewModelScope.launch {
                    delay(500) // prevent unnecessary check for job cancel
                    if (isEmailTakenInDb(email)) emailIsInDb()
                }
        }
        return ok
    }

    private fun validatePasswords(): Boolean {
        if (_uiState.value.password.isEmpty()) return true
        val (ok, cause) = validatePasswordsResult()
        _uiState.update {
            it.copy(
                passwordError =
                    when {
                        cause.startsWith("Password") -> cause // password-related cause
                        cause.isEmpty() -> ""
                        else -> "" // cause belongs to confirm
                    },
                confirmPasswordError =
                    when {
                        cause.startsWith("Please confirm") ||
                            cause.startsWith("Passwords do not match") -> cause
                        else -> ""
                    }
            )
        }
        return ok
    }

    // ---------- Button enabled? section -----------
    /**
     * Determines if the "Next" or "Done" button should be enabled for a given route. This function
     * uses the `validate*Result()` methods directly, as they provide validation status without
     * triggering UI state changes (i.e., updating error messages).
     *
     * @param route The current route in the creation flow.
     */
    fun computeEnabledFor(route: String?): Boolean =
        when (route) {
            CreateAccountRoutes.USERNAME -> validateUsernameResult().first
            CreateAccountRoutes.EMAIL -> validateEmailResult().first
            CreateAccountRoutes.PASSWORD -> validatePasswordsResult().first
            CreateAccountRoutes.SKILLS -> true
            else -> false
        }

    private fun refreshEnabled() {
        val r = _uiState.value.currentRoute
        _uiState.update { it.copy(buttonEnabled = computeEnabledFor(r)) }
    }

    fun onRouteChanged(route: String?) {
        _uiState.update { it.copy(currentRoute = route ?: CreateAccountRoutes.USERNAME) }
        refreshEnabled()
    }

    // ---------- Aggregate Validators ----------

    private fun refreshError() {
        val r = _uiState.value.currentRoute
        validateByRoute(r)
    }

    /**
     * Runs all validation functions (username, email, password, skills) and returns true only if
     * all are valid.
     */
    private fun validateInputs(): Boolean {
        val results = listOf(validateUsername(), validateEmail(), validatePasswords())
        return results.all { it }
    }

    /**
     * Validates only the fields relevant to the current screen or step of the Create Account flow.
     *
     * @param route One of the [CreateAccountRoutes] constants.
     * @return True if the inputs for that step are valid; false otherwise.
     */
    private fun validateByRoute(route: String): Boolean {
        return when (route) {
            CreateAccountRoutes.USERNAME -> validateUsername()
            CreateAccountRoutes.EMAIL -> validateEmail()
            CreateAccountRoutes.PASSWORD -> validatePasswords()
            CreateAccountRoutes.SKILLS -> true
            else -> false // Unknown route
        }
    }

    private var emailCheckJob: Job? = null

    /**
     * Checks Firestore to see if the email exists. Returns TRUE if email is TAKEN (already in use).
     */
    private suspend fun isEmailTakenInDb(email: String): Boolean {
        return try {
            val snapshot =
                db.collection("users") // Ensure this matches your collection path const
                    .whereEqualTo("email", email)
                    .get()
                    .await()
            !snapshot.isEmpty
        } catch (e: Exception) {
            false // Assume not taken on error to avoid blocking user, or handle differently
        }
    }

    private fun emailIsInDb() {
        _uiState.update { it.copy(emailError = "Email is already in use", buttonEnabled = false) }
    }

    /**
     * Called when the user finishes the account creation process. Performs full validation and, if
     * successful, triggers the model to create the account and navigates to the main screen.
     */
    /**
     * Called when the user finishes the account creation process. Performs full validation and, if
     * successful, triggers the model to create the account and navigates to the main screen.
     */
    fun done() =
        viewModelScope.launch {
            if (!validateInputs()) return@launch

            if (!validateEmailAvailability()) return@launch

            createUserAccount()
        }

    /**
     * Validates email availability for non-Google accounts. Returns false if email is already
     * taken, true otherwise.
     */
    private suspend fun validateEmailAvailability(): Boolean {
        if (isGoogleAccount) return true

        val email = _uiState.value.email
        if (isEmailTakenInDb(email)) {
            emailIsInDb()
            _eventFlow.emit(CreateAccountEvent.NavigateToEmail)
            return false
        }
        return true
    }

    /** Creates the user account and navigates to the main screen on success. */
    private suspend fun createUserAccount() {
        try {
            val params =
                if (isGoogleAccount) {
                    CreateAccountGoogleParams(_uiState.value.username, _uiState.value.skills)
                } else {
                    CreateAccountClassicParams(
                        _uiState.value.username,
                        _uiState.value.email,
                        _uiState.value.skills,
                        _uiState.value.password
                    )
                }

            model.createAccount(params)
            fcmTokenManager.getAndSaveToken()
            _eventFlow.emit(CreateAccountEvent.NavigateToMainScreen)
        } catch (e: Exception) {
            Log.w("Create Account", "Firestore Error", e)
        }
    }
}

/**
 * Factory class for constructing CreateAccountViewModel instances. Needed when a ViewModel has
 * non-default constructor parameters.
 */
class CreateAccountVmFactory(
    private val isGoogleAccount: Boolean // Whether user is creating Google or classic account
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return CreateAccountViewModel(isGoogleAccount) as T
    }
}
