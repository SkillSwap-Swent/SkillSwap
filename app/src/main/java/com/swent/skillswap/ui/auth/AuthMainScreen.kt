/**
 * @author Topaze17 (Eliott) Used ChatGPT for tagging the composables and commenting, but all tags
 *   and comments were checked manually.
 */
package com.swent.skillswap.ui.auth

// ----- Imports -----
import android.app.Activity
import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.lifecycle.viewmodel.compose.viewModel
import com.swent.skillswap.R
import com.swent.skillswap.ui.utils.SkillSwapPasswordTextField
import com.swent.skillswap.ui.utils.SkillSwapShadowButton
import com.swent.skillswap.ui.utils.SkillSwapTextField

// ----- UI Test Tags -----
object SignInTags {
    const val LOGO = "SIGN_IN_LOGO"
    const val GOOGLE_BUTTON = "SIGN_IN_GOOGLE_BUTTON"
    const val SIGN_IN_BUTTON = "SIGN_IN_BUTTON"
    const val EMAIL_FIELD = "EMAIL_FIELD"
    const val PASSWORD_FIELD = "PASSWORD_FIELD"
    const val CREATE_ACCOUNT_TEXT = "SIGN_IN_CREATE_ACCOUNT_TEXT"
    const val FORGOT_PASSWORD = "FORGOT_PASSWORD"
}

/**
 * Main Sign-In screen of the app.
 *
 * Provides:
 * - Google Sign-In using CredentialManager
 * - Classic email/password Sign-In button
 * - “Create Account” button for new users
 *
 * The ViewModel emits navigation events via a SharedFlow (SignInEvent), which are collected here
 * using LaunchedEffect.
 */
@Composable
fun AuthMainScreen(
    goToMainScreen: () -> Unit = {},
    goToCreateAccountScreen: () -> Unit = {},
    goToPasswordRecovery: () -> Unit = {},
    context: Context = LocalContext.current,
    credentialManager: CredentialManager = CredentialManager.create(LocalContext.current),
    vm: SignInViewModel = viewModel()
) {
    // Listen for one-time events from the ViewModel
    LaunchedEffect(Unit) {
        vm.eventFlow.collect { event ->
            when (event) {
                is SignInEvent.NavigateToMainScreen -> goToMainScreen()
                is SignInEvent.NavigateToCreateAccountScreen -> goToCreateAccountScreen()
            }
        }
    }
    LaunchedEffect(Unit) { vm.check() }
    val scroll = rememberScrollState()
    val uiState by vm.uiState.collectAsState()
    // Scaffold gives a top-level layout structure (with padding, backgrounds, etc.)
    Scaffold { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().verticalScroll(scroll)) {
            // ----- App logo -----
            Spacer(modifier = Modifier.height(50.dp))
            Image(
                painter = painterResource(R.drawable.logo),
                contentDescription = "SkillSwap logo",
                modifier =
                    Modifier.size(280.dp)
                        .align(Alignment.CenterHorizontally)
                        .testTag(SignInTags.LOGO)
            )
            // ------------------------------------------------------------
            // Email and Password Input Fields
            // ------------------------------------------------------------
            // ----- Email Field -----
            SkillSwapTextField(
                value = uiState.email,
                supportText = uiState.emailError,
                onValueChange = { vm.onEmailChange(it) },
                label = "Email",
                placeholder = "your.email@gmail.com",
                leadingIcon = {
                    Icon(imageVector = Icons.Outlined.Person, contentDescription = "Email Icon")
                },
                keyboardOptions =
                    KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        capitalization = KeyboardCapitalization.None,
                        imeAction = ImeAction.Next
                    ),
                modifier =
                    Modifier.align(Alignment.CenterHorizontally).testTag(SignInTags.EMAIL_FIELD)
            )
            // ----- Password Field -----
            SkillSwapPasswordTextField(
                value = uiState.password,
                supportText = uiState.passwordError,
                label = "Password",
                placeholder = "Type your password",
                onValueChange = { vm.onPasswordChange(it) },
                modifier =
                    Modifier.align(Alignment.CenterHorizontally).testTag(SignInTags.PASSWORD_FIELD)
            )
            // ----- Classic Sign-In button -----
            Spacer(modifier = Modifier.height(20.dp))
            SkillSwapShadowButton(
                onClick = { vm.classicSignIn() },
                modifier =
                    Modifier.align(Alignment.CenterHorizontally)
                        .fillMaxWidth(0.3f)
                        .testTag(SignInTags.SIGN_IN_BUTTON)
                        .height(55.dp)
            ) {
                Text(
                    text = "Login",
                    fontSize = 24.sp,
                )
            }
            // ----- Forgot Password link -----
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(
                onClick = goToPasswordRecovery,
                modifier =
                    Modifier.testTag(SignInTags.FORGOT_PASSWORD).align(Alignment.CenterHorizontally)
            ) {
                Text(text = "Forgot Password?", fontSize = 14.sp, textAlign = TextAlign.Center)
            }
            Spacer(modifier = Modifier.height(50.dp))
            // ------------------------------------------------------------
            // Sign-In Buttons (Google + Classic)
            // ------------------------------------------------------------
            // ----- Google Sign-In button -----
            SkillSwapShadowButton(
                onClick = { vm.googleSignIn(credentialManager, context as Activity) },
                modifier =
                    Modifier.align(Alignment.CenterHorizontally)
                        .fillMaxWidth(0.6f)
                        .testTag(SignInTags.GOOGLE_BUTTON)
                        .height(55.dp)
            ) {
                Row(modifier = Modifier.height(21.dp)) {
                    Image(
                        painter = painterResource(R.drawable.google_logo),
                        contentDescription = "Google logo",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Sign up with Google", fontSize = 14.sp)
                }
            }

            // ------------------------------------------------------------
            // Create Account Button
            // ------------------------------------------------------------
            Spacer(modifier = Modifier.height(10.dp))
            SkillSwapShadowButton(
                onClick = { vm.createAccount() },
                modifier =
                    Modifier.align(Alignment.CenterHorizontally)
                        .testTag(SignInTags.CREATE_ACCOUNT_TEXT)
                        .fillMaxWidth(0.6f)
                        .height(55.dp)
            ) {
                Row {
                    Icon(
                        imageVector = Icons.Outlined.AccountCircle,
                        contentDescription = "User",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Sign up with email", fontSize = 14.sp)
                }
            }
        }
    }
}
