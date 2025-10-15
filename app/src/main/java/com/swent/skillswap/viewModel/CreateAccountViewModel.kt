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
import com.swent.skillswap.ui.signIn.CreateAccountRoutes
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
    private fun addSkill(skill: SkillTag) {
        _uiState.update { current -> current.copy(skills = current.skills + skill) }
    }

    /** Removes a single skill from the selected skills set. */
    private fun removeSkill(skill: SkillTag) {
        _uiState.update { current -> current.copy(skills = current.skills - skill) }
    }

    fun clickSkill(skill: SkillTag) {
        if (_uiState.value.skills.contains(skill)) removeSkill(skill) else addSkill(skill)
    }
    // ---------- Tiny validators ----------
    private val emailRegex by lazy { "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$".toRegex() }

    private fun validateUsername(): Boolean {
        val ok = _uiState.value.username.isNotBlank()
        _uiState.update { it.copy(usernameError = if (ok) "" else "Username cannot be empty") }
        return ok
    }

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

    private fun validateSkills(): Boolean {
        val ok = _uiState.value.skills.isNotEmpty()
        _uiState.update {
            it.copy(skillsError = if (ok) "" else "At least one skill must be selected")
        }
        return ok
    }

    // ---------- Aggregate validator ----------
    fun validateInputs(): Boolean {
        val results =
            listOf(validateUsername(), validateEmail(), validatePasswords(), validateSkills())
        return results.all { it }
    }

    fun validateByRoute(route: String): Boolean {
        return when (route) {
            CreateAccountRoutes.USERNAME -> validateUsername()
            CreateAccountRoutes.EMAIL -> validateEmail()
            CreateAccountRoutes.PASSWORD -> validatePasswords()
            CreateAccountRoutes.SKILLS -> validateSkills()
            else -> false // Unknown route
        }
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
