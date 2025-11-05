/** @author Topaze17 (Eliott) Used ChatGPT commenting, but all comments were checked manually. */
package com.swent.skillswap.viewModel

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
import com.swent.skillswap.ui.Auth.CreateAccountRoutes
import com.swent.skillswap.resources.ValidationConfig
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

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
    val skillsError: String = ""
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
    val firestore: FirebaseFirestore = Firebase.firestore
) : ViewModel() {
    private val model: SignInInterface =
        if (isGoogleAccount) AuthGoogleModel(auth, firestore) else AuthClassicModel(auth, firestore)

    private val _uiState: MutableStateFlow<CreateAccountUIState> =
        MutableStateFlow<CreateAccountUIState>(CreateAccountUIState())
    // SharedFlow used to emit one-time navigation events to the UI layer.
    private val _eventFlow = MutableSharedFlow<CreateAccountEvent>()
    val eventFlow: SharedFlow<CreateAccountEvent> = _eventFlow

    val uiState: StateFlow<CreateAccountUIState> = _uiState

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
    }

    /** Updates the username without affecting any error fields. */
    fun onUsernameChange(newUsername: String) {
        _uiState.update { current -> current.copy(username = newUsername) }
    }

    /** Updates the password without affecting any error fields. */
    fun onPasswordChange(newPassword: String) {
        _uiState.update { current -> current.copy(password = newPassword) }
    }

    /** Updates the confirm password field without affecting any error fields. */
    fun onConfirmPasswordChange(newConfirmPassword: String) {
        _uiState.update { current -> current.copy(confirmPassword = newConfirmPassword) }
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
    }

    // ---------- Validation Section ----------

    // Use shared email validation regex from ValidationConfig
    private val emailRegex = ValidationConfig.EMAIL_REGEX

    /** Validates that username is not blank. */
    private fun validateUsername(): Boolean {
        val ok = _uiState.value.username.isNotBlank()
        _uiState.update { it.copy(usernameError = if (ok) "" else "Username cannot be empty") }
        return ok
    }

    /** Validates email format (skips if Google account). */
    private fun validateEmail(): Boolean {
        if (isGoogleAccount) {
            _uiState.update { it.copy(emailError = "") }
            return true
        }
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

    /** Validates password rules and confirmation (skips if Google account). */
    private fun validatePasswords(): Boolean {
        if (isGoogleAccount) {
            _uiState.update { it.copy(passwordError = "", confirmPasswordError = "") }
            return true
        }

        val pwd = _uiState.value.password
        val confirm = _uiState.value.confirmPassword

        val passwordError =
            when {
                pwd.isBlank() -> "Password cannot be empty"
                pwd.length < 8 -> "Password must be at least 8 characters long"
                !pwd.any { it.isUpperCase() } ->
                    "Password must contain at least one uppercase letter"
                else -> ""
            }

        val confirmError =
            when {
                confirm.isBlank() -> "Please confirm your password"
                confirm != pwd -> "Passwords do not match"
                else -> ""
            }

        _uiState.update {
            it.copy(passwordError = passwordError, confirmPasswordError = confirmError)
        }

        return passwordError.isEmpty() && confirmError.isEmpty()
    }

    /** Validates that the user has selected at least one skill. */
    private fun validateSkills(): Boolean {
        val ok = _uiState.value.skills.isNotEmpty()
        _uiState.update {
            it.copy(skillsError = if (ok) "" else "At least one skill must be selected")
        }
        return ok
    }

    // ---------- Aggregate Validators ----------

    /**
     * Runs all validation functions (username, email, password, skills) and returns true only if
     * all are valid.
     */
    fun validateInputs(): Boolean {
        val results =
            listOf(validateUsername(), validateEmail(), validatePasswords(), validateSkills())
        return results.all { it }
    }

    /**
     * Validates only the fields relevant to the current screen or step of the Create Account flow.
     *
     * @param route One of the [CreateAccountRoutes] constants.
     * @return True if the inputs for that step are valid; false otherwise.
     */
    fun validateByRoute(route: String): Boolean {
        return when (route) {
            CreateAccountRoutes.USERNAME -> validateUsername()
            CreateAccountRoutes.EMAIL -> validateEmail()
            CreateAccountRoutes.PASSWORD -> validatePasswords()
            CreateAccountRoutes.SKILLS -> validateSkills()
            else -> false // Unknown route
        }
    }

    /**
     * Called when the user finishes the account creation process. Performs full validation and, if
     * successful, triggers the model to create the account and navigates to the main screen.
     */
    fun done() =
        viewModelScope.launch {
            if (validateInputs()) {
                try {
                    model.createAccount(
                        if (isGoogleAccount) {
                            CreateAccountGoogleParams(
                                _uiState.value.username,
                                _uiState.value.skills
                            )
                        } else {
                            CreateAccountClassicParams(
                                _uiState.value.username,
                                _uiState.value.email,
                                _uiState.value.skills,
                                _uiState.value.password
                            )
                        }
                    )
                    _eventFlow.emit(CreateAccountEvent.NavigateToMainScreen)
                } catch (e: Exception) {
                    Log.w("Create Account", "Firestore Error", e)
                }
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
