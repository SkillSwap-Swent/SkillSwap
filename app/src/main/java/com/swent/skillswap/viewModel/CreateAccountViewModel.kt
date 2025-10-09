package com.swent.skillswap.viewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.swent.skillswap.model.SignIn.CreateAccountClassicParams
import com.swent.skillswap.model.SignIn.CreateAccountGoogleParams
import com.swent.skillswap.model.SignIn.SignInClassicModel
import com.swent.skillswap.model.SignIn.SignInGoogleModel
import com.swent.skillswap.model.SignIn.SignInInterface
import com.swent.skillswap.model.tags.SkillTag
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

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

class CreateAccountViewModel(
    private val goToMainScreen: () -> Unit,
    private val isGoogleAccount: Boolean
) : ViewModel() {
    private val model: SignInInterface =
        if (isGoogleAccount) SignInGoogleModel() else SignInClassicModel()
    private val _uiState: MutableStateFlow<CreateAccountUIState> =
        MutableStateFlow<CreateAccountUIState>(CreateAccountUIState())
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
    fun addSkill(skill: SkillTag) {
        _uiState.update { current -> current.copy(skills = current.skills + skill) }
    }

    /** Removes a single skill from the selected skills set. */
    fun removeSkill(skill: SkillTag) {
        _uiState.update { current -> current.copy(skills = current.skills - skill) }
    }
    /**
     * Validates all fields of the current [CreateAccountUIState] and updates the corresponding
     * error messages.
     *
     * Validation rules:
     * - **Username** must not be blank.
     * - **Email** must match a standard email pattern.
     * - **Password** must be at least 8 characters long and contain an uppercase letter.
     * - **Confirm password** must match the password.
     * - **At least one skill** must be selected.
     *
     * @return `true` if all fields are valid, `false` otherwise.
     */
    fun validateInputs(): Boolean {
        val current = _uiState.value

        // --- Username ---
        val usernameError = if (current.username.isBlank()) "Username cannot be empty" else ""

        // --- Email ---
        val emailError =
            if (isGoogleAccount) ""
            else {
                val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$".toRegex()
                when {
                    current.email.isBlank() -> "Email cannot be empty"
                    !emailRegex.matches(current.email) -> "Invalid email format"
                    else -> ""
                }
            }

        // --- Password ---
        val passwordError =
            if (isGoogleAccount) ""
            else
                when {
                    current.password.isBlank() -> "Password cannot be empty"
                    current.password.length < 8 -> "Password must be at least 8 characters long"
                    !current.password.any { it.isUpperCase() } ->
                        "Password must contain at least one uppercase letter"
                    else -> ""
                }

        // --- Confirm Password ---
        val confirmPasswordError =
            if (isGoogleAccount) ""
            else
                when {
                    current.confirmPassword.isBlank() -> "Please confirm your password"
                    current.confirmPassword != current.password -> "Passwords do not match"
                    else -> ""
                }

        // --- Skills ---
        val skillsError =
            if (current.skills.isEmpty()) "At least one skill must be selected" else ""

        // --- Result ---
        val hasErrors =
            listOf(usernameError, emailError, passwordError, confirmPasswordError, skillsError)
                .any { it.isNotEmpty() }

        _uiState.update {
            it.copy(
                usernameError = usernameError,
                emailError = emailError,
                passwordError = passwordError,
                confirmPasswordError = confirmPasswordError,
                skillsError = skillsError
            )
        }

        return !hasErrors
    }

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
                goToMainScreen()
            }
        }
}

class CreateAccountVmFactory(
    private val goToMainScreen: () -> Unit = {},
    private val isGoogleAccount: Boolean
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return CreateAccountViewModel(goToMainScreen, isGoogleAccount) as T
    }
}
