/**
 * @author Topaze17 (Eliott)
 * Used ChatGPT commenting,
 * but all comments were checked manually.
 */
package com.swent.skillswap.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.swent.skillswap.model.SignIn.CreateAccountClassicParams
import com.swent.skillswap.model.SignIn.CreateAccountGoogleParams
import com.swent.skillswap.model.SignIn.SignInClassicModel
import com.swent.skillswap.model.SignIn.SignInGoogleModel
import com.swent.skillswap.model.SignIn.SignInInterface
import com.swent.skillswap.model.tags.SkillTag
import com.swent.skillswap.ui.signIn.CreateAccountRoutes
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Represents one-time events (usually navigation or UI triggers)
 * that occur during the Create Account flow.
 */
sealed class CreateAccountEvent {

    /**
     * Event indicating that the user has successfully completed
     * account creation and should be navigated to the main screen.
     */
    object NavigateToMainScreen : CreateAccountEvent()
}
/**
 * Represents all UI state fields for the Create Account screen.
 * This includes both user-entered data and validation errors.
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
 * ViewModel responsible for handling all logic and state updates
 * for the Create Account flow (both Google and Classic sign-in types).
 */
class CreateAccountViewModel(
    private val isGoogleAccount: Boolean
) : ViewModel() {

    private val model: SignInInterface =
        if (isGoogleAccount) SignInGoogleModel() else SignInClassicModel()

    private val _uiState: MutableStateFlow<CreateAccountUIState> =
        MutableStateFlow<CreateAccountUIState>(CreateAccountUIState())
    private val _eventFlow = MutableSharedFlow<CreateAccountEvent>()
    val eventFlow: SharedFlow<CreateAccountEvent> = _eventFlow

    val uiState: StateFlow<CreateAccountUIState> = _uiState

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
     * Toggles a skill selection — if already selected, remove it;
     * otherwise, add it to the skill set.
     */
    fun clickSkill(skill: SkillTag) {
        if (_uiState.value.skills.contains(skill)) removeSkill(skill) else addSkill(skill)
    }

    // ---------- Validation Section ----------

    // Regular expression for validating email formats (simple pattern) can be change easily
    private val emailRegex by lazy { "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$".toRegex() }

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
     * Runs all validation functions (username, email, password, skills)
     * and returns true only if all are valid.
     */
    fun validateInputs(): Boolean {
        val results =
            listOf(validateUsername(), validateEmail(), validatePasswords(), validateSkills())
        return results.all { it }
    }

    /**
     * Validates only the inputs relevant to a specific route (screen step).
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
     * Called when the user finishes the account creation process.
     * Performs full validation and, if successful, triggers the model
     * to create the account and navigates to the main screen.
     */
    fun done() =
        viewModelScope.launch {
            if (validateInputs()) {
                model.createAccount(
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
                )
                _eventFlow.emit(CreateAccountEvent.NavigateToMainScreen)
            }
        }
}

/**
 * Factory class for constructing CreateAccountViewModel instances.
 * Needed when a ViewModel has non-default constructor parameters.
 */
class CreateAccountVmFactory(
    private val isGoogleAccount: Boolean         // Whether user is creating Google or classic account
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return CreateAccountViewModel(isGoogleAccount) as T
    }
}