/**
 * @author Younes Belgroune - Password recovery screen Follows the same design patterns as
 *   SignInMainScreen Made with the help of AI Joey Gugler - refactor using chatGPT
 */
package com.swent.skillswap.ui.auth

import android.graphics.Color
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

/**
 * Password Recovery screen that allows users to reset their password.
 *
 * Provides:
 * - Email input field
 * - Send password reset email button
 * - Success and error messages
 * - Navigation back to sign-in screen
 *
 * @param goBackToSignIn Lambda called to navigate back to the sign-in screen
 * @param vm The [PasswordRecoveryViewModel] providing UI state and actions
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
                MessageCard(
                    message = uiState.successMessage,
                    colors =
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) to
                            MaterialTheme.colorScheme.onPrimaryContainer,
                    testTag = PasswordRecoveryTags.SUCCESS_MESSAGE
                )
            }

            if (uiState.errorMessage.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                MessageCard(
                    message = uiState.errorMessage,
                    colors =
                        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f) to
                            MaterialTheme.colorScheme.onErrorContainer,
                    testTag = PasswordRecoveryTags.ERROR_MESSAGE
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            SendButton(isLoading = uiState.isLoading, onClick = { vm.sendPasswordResetEmail() })

            Spacer(modifier = Modifier.height(16.dp))

            BackButton(onClick = goBackToSignIn)
        }
    }
}
/**
 * Displays the header section of the Password Recovery screen.
 *
 * Includes:
 * - Screen title
 * - Instructions for entering an email to reset the password
 */
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
/**
 * Composable for the email input field in the Password Recovery screen.
 *
 * @param email Current email value
 * @param error Optional error message to display below the field
 * @param onEmailChange Lambda called when the email input changes
 */
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
/**
 * Displays a message card with a background and content color.
 *
 * Can be used for success or error messages.
 *
 * @param message The text to display inside the card
 * @param colors Pair of background color (first) and content color (second)
 * @param testTag Optional test tag for UI testing
 */
@Composable
private fun MessageCard(
    message: String,
    colors: Pair<androidx.compose.ui.graphics.Color, androidx.compose.ui.graphics.Color>,
    testTag: String? = null
) {
    val (backgroundColor, contentColor) = colors

    Card(
        modifier =
            Modifier.wrapContentWidth(Alignment.CenterHorizontally)
                .fillMaxWidth(0.8f)
                .then(if (testTag != null) Modifier.testTag(testTag) else Modifier),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = contentColor,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(16.dp)
        )
    }
}
/**
 * Button for sending the password reset email.
 *
 * @param isLoading True if the reset email is currently being sent
 * @param onClick Lambda called when the button is clicked
 */
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
/**
 * Button to navigate back to the sign-in screen.
 *
 * @param onClick Lambda called when the button is clicked$
 */
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
