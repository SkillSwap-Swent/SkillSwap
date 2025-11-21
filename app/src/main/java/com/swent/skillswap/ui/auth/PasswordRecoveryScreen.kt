/**
 * @author Younes Belgroune - Password recovery screen Follows the same design patterns as
 *   SignInMainScreen Made with the help of AI
 */
package com.swent.skillswap.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.swent.skillswap.ui.utils.SkillSwapButtonOutline
import com.swent.skillswap.ui.utils.SkillSwapOutlinedTextField
import kotlinx.coroutines.delay

// ----- UI Test Tags -----
object PasswordRecoveryTags {
    const val EMAIL_FIELD = "PASSWORD_RECOVERY_EMAIL_FIELD"
    const val SEND_BUTTON = "PASSWORD_RECOVERY_SEND_BUTTON"
    const val BACK_BUTTON = "PASSWORD_RECOVERY_BACK_BUTTON"
    const val SUCCESS_MESSAGE = "PASSWORD_RECOVERY_SUCCESS_MESSAGE"
    const val ERROR_MESSAGE = "PASSWORD_RECOVERY_ERROR_MESSAGE"
}

// Note: Button colors are defined inline in the composable to access MaterialTheme

/**
 * Password Recovery screen that allows users to reset their password.
 *
 * Provides:
 * - Email input field
 * - Send password reset email button
 * - Success/error messages
 * - Navigation back to sign-in screen
 */
@Preview(showBackground = true)
@Composable
fun PasswordRecoveryScreen(
    goBackToSignIn: () -> Unit = {},
    vm: PasswordRecoveryViewModel = viewModel()
) {
    val scroll = rememberScrollState()
    val uiState by vm.uiState.collectAsState()

    // One-time events
    LaunchedEffect(Unit) {
        vm.eventFlow.collect { event ->
            if (event is PasswordRecoveryEvent.NavigateToSignIn) goBackToSignIn()
        }
    }

    // Auto-navigate after showing success
    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage.isNotEmpty()) {
            delay(2000)
            goBackToSignIn()
        }
    }

    Scaffold { padding ->
        Column(
            modifier =
                Modifier.padding(padding)
                    .background(MaterialTheme.colorScheme.background)
                    .fillMaxSize()
                    .verticalScroll(scroll),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(25.dp))

            HeaderSection()

            Spacer(modifier = Modifier.height(25.dp))

            EmailField(
                email = uiState.email,
                error = uiState.emailError,
                onEmailChange = { vm.onEmailChange(it) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.successMessage.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                MessageCard(message = uiState.successMessage, success = true)
            }

            if (uiState.errorMessage.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                MessageCard(message = uiState.errorMessage, success = false)
            }

            Spacer(modifier = Modifier.height(16.dp))

            SendButton(isLoading = uiState.isLoading, onClick = { vm.sendPasswordResetEmail() })

            Spacer(modifier = Modifier.height(16.dp))

            BackButton(onClick = goBackToSignIn)
        }
    }
}

@Composable
private fun HeaderSection() {
    Text(
        text = "Password Recovery",
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(18.dp))
    Text(
        text = "Enter your email address and we'll send you a link to reset your password.",
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f),
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun EmailField(email: String, error: String?, onEmailChange: (String) -> Unit) {
    SkillSwapOutlinedTextField(
        value = email,
        supportText = error ?: "",
        onValueChange = onEmailChange,
        label = "Email",
        placeholder = "your.email@gmail.com",
        keyboardOptions =
            KeyboardOptions(
                keyboardType = KeyboardType.Email,
                capitalization = KeyboardCapitalization.None,
                imeAction = ImeAction.Done
            ),
        modifier =
            Modifier.wrapContentWidth(Alignment.CenterHorizontally)
                .testTag(PasswordRecoveryTags.EMAIL_FIELD)
    )
}

@Composable
private fun MessageCard(message: String, success: Boolean) {
    val colors =
        if (success) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) to
                MaterialTheme.colorScheme.onPrimaryContainer
        } else {
            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f) to
                MaterialTheme.colorScheme.onErrorContainer
        }
    Card(
        modifier =
            Modifier.wrapContentWidth(Alignment.CenterHorizontally)
                .fillMaxWidth(0.8f)
                .testTag(
                    if (success) PasswordRecoveryTags.SUCCESS_MESSAGE
                    else PasswordRecoveryTags.ERROR_MESSAGE
                ),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = colors.first)
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = colors.second,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Composable
private fun SendButton(isLoading: Boolean, onClick: () -> Unit) {
    SkillSwapButtonOutline(
        labelText = if (isLoading) "Sending..." else "Send Reset Link",
        onClick = onClick,
        enabled = !isLoading,
        modifier =
            Modifier.wrapContentWidth(Alignment.CenterHorizontally)
                .testTag(PasswordRecoveryTags.SEND_BUTTON)
    )
}

@Composable
private fun BackButton(onClick: () -> Unit) {
    SkillSwapButtonOutline(
        labelText = "Back to Sign In",
        onClick = onClick,
        modifier =
            Modifier.wrapContentWidth(Alignment.CenterHorizontally)
                .testTag(PasswordRecoveryTags.BACK_BUTTON)
    )
}
