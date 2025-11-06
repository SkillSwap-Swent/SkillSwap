/**
 * @author Younes Belgroune - Password recovery screen Follows the same design patterns as
 *   SignInMainScreen Made with the help of AI
 */
package com.swent.skillswap.ui.signIn

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
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.swent.skillswap.ui.theme.BrushDirection
import com.swent.skillswap.ui.theme.getLinearBrush
import com.swent.skillswap.ui.utils.SkillSwapShadowButton
import com.swent.skillswap.ui.utils.SkillSwapTextField
import com.swent.skillswap.viewModel.PasswordRecoveryEvent
import com.swent.skillswap.viewModel.PasswordRecoveryViewModel

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
    // Listen for one-time events from the ViewModel
    LaunchedEffect(Unit) {
        vm.eventFlow.collect { event ->
            when (event) {
                is PasswordRecoveryEvent.NavigateToSignIn -> goBackToSignIn()
            }
        }
    }

    val scroll = rememberScrollState()
    val uiState by vm.uiState.collectAsState()

    Scaffold { padding ->
        Column(
            modifier =
                Modifier.padding(padding)
                    .background(getLinearBrush(BrushDirection.DOWN_TOP))
                    .fillMaxSize()
                    .verticalScroll(scroll)
        ) {
            // Title
            Spacer(modifier = Modifier.height(200.dp))
            Text(
                text = "Password Recovery",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            )

            Text(
                text = "Enter your email address and we'll send you a link to reset your password.",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 16.dp)
            )

            // Email field
            Spacer(modifier = Modifier.height(40.dp))
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
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontSize = 14.sp,
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
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            // Send button
            Spacer(modifier = Modifier.height(40.dp))
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
                    Text(text = "Send Reset Link", fontSize = 18.sp, fontWeight = FontWeight.Medium)
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
                Text(text = "Back to Sign In", fontSize = 14.sp)
            }
        }
    }
}
