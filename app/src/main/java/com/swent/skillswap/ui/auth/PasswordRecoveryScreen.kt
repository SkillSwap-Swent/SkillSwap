/**
 * @author Younes Belgroune - Password recovery screen Follows the same design patterns as
 *   SignInMainScreen Made with the help of AI
 */
package com.swent.skillswap.ui.auth

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.swent.skillswap.resources.theme.BrushDirection
import com.swent.skillswap.resources.theme.getLinearBrush
import com.swent.skillswap.ui.utils.SkillSwapShadowButton
import com.swent.skillswap.ui.utils.SkillSwapTextField
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

    // Listen for one-time events from the ViewModel
    // Also handle navigation when success message is shown (user can navigate after seeing success)
    LaunchedEffect(Unit) {
        vm.eventFlow.collect { event ->
            when (event) {
                is PasswordRecoveryEvent.NavigateToSignIn -> goBackToSignIn()
            }
        }
    }

    // Auto-navigate after success message is shown (replaces hardcoded delay in ViewModel)
    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage.isNotEmpty()) {
            delay(2000) // Show success message for 2 seconds
            goBackToSignIn()
        }
    }

    Scaffold { padding ->
        Column(
            modifier =
                Modifier.padding(padding)
                    .background(getLinearBrush(BrushDirection.DOWN_TOP))
                    .fillMaxSize()
                    .verticalScroll(scroll)
        ) {
            // Title - using aspect ratio for spacing instead of magic numbers
            Spacer(modifier = Modifier.fillMaxHeight(0.25f))
            Text(
                text = "Password Recovery",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            )

            Text(
                text = "Enter your email address and we'll send you a link to reset your password.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 16.dp)
            )

            // Email field - using aspect ratio for spacing
            Spacer(modifier = Modifier.fillMaxHeight(0.05f))
            SkillSwapTextField(
                value = uiState.email,
                supportText = uiState.emailError,
                onValueChange = { vm.onEmailChange(it) },
                label = "Email",
                placeholder = "your.email@gmail.com",
                keyboardOptions =
                    KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        capitalization = KeyboardCapitalization.None,
                        imeAction = ImeAction.Done
                    ),
                modifier =
                    Modifier.align(Alignment.CenterHorizontally)
                        .fillMaxWidth(0.8f)
                        .testTag(PasswordRecoveryTags.EMAIL_FIELD)
            )

            // Success message
            if (uiState.successMessage.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier =
                        Modifier.align(Alignment.CenterHorizontally)
                            .fillMaxWidth(0.8f)
                            .testTag(PasswordRecoveryTags.SUCCESS_MESSAGE),
                    shape = RoundedCornerShape(8.dp),
                    colors =
                        CardDefaults.cardColors(
                            containerColor =
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        )
                ) {
                    Text(
                        text = uiState.successMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            // Error message
            if (uiState.errorMessage.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier =
                        Modifier.align(Alignment.CenterHorizontally)
                            .fillMaxWidth(0.8f)
                            .testTag(PasswordRecoveryTags.ERROR_MESSAGE),
                    shape = RoundedCornerShape(8.dp),
                    colors =
                        CardDefaults.cardColors(
                            containerColor =
                                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                        )
                ) {
                    Text(
                        text = uiState.errorMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            // Send button - using aspect ratio for spacing
            Spacer(modifier = Modifier.fillMaxHeight(0.05f))
            SkillSwapShadowButton(
                onClick = { vm.sendPasswordResetEmail() },
                modifier =
                    Modifier.align(Alignment.CenterHorizontally)
                        .testTag(PasswordRecoveryTags.SEND_BUTTON)
                        .fillMaxWidth(0.4f),
                enable = !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onSurface,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Send Reset Link",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Back to sign-in button
            Spacer(modifier = Modifier.height(20.dp))
            OutlinedButton(
                onClick = { goBackToSignIn() },
                colors =
                    ButtonColors(
                        containerColor = Color.Transparent,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        disabledContainerColor = Color.Transparent,
                        disabledContentColor =
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier =
                    Modifier.align(Alignment.CenterHorizontally)
                        .fillMaxWidth(0.6f)
                        .testTag(PasswordRecoveryTags.BACK_BUTTON)
            ) {
                Text(text = "Back to Sign In", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
