/**
 * @author Topaze17 (Eliott) Used ChatGPT for tagging the composables and commenting, but all tags
 *   and comments were checked manually.
 */
package com.swent.skillswap.ui.signIn

// ----- Imports -----
import android.app.Activity
import android.content.Context
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.lifecycle.viewmodel.compose.viewModel
import com.swent.skillswap.R
import com.swent.skillswap.ui.theme.BrushDirection
import com.swent.skillswap.ui.theme.DefaultGradient
import com.swent.skillswap.ui.theme.getLinearBrush
import com.swent.skillswap.ui.utils.GradientButton
import com.swent.skillswap.viewModel.SignInEvent
import com.swent.skillswap.viewModel.SignInViewModel

// ----- UI Test Tags -----
object SignInTags {
    const val LOGO = "SIGN_IN_LOGO"
    const val GOOGLE_BUTTON = "SIGN_IN_GOOGLE_BUTTON"
    const val SIGN_IN_BUTTON = "SIGN_IN_BUTTON"
    const val OR_TEXT = "SIGN_IN_OR_TEXT"
    const val CREATE_ACCOUNT_TEXT = "SIGN_IN_CREATE_ACCOUNT_TEXT"
}

// ----- Default styles for Sign-In buttons -----
val signInButtonColor = ButtonColors(Color.Transparent, Color.White, Color.White, Color.Black)
val signInButtonStroke = BorderStroke(1.dp, Color.White)

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
@Preview(showBackground = true)
@Composable
fun SignInMainScreen(
    goToMainScreen: () -> Unit = {},
    goToCreateAccountScreen: () -> Unit = {},
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
                is SignInEvent.NavigateToClassicSignIn ->
                    goToMainScreen() // TODO: Update when classic sign-in screen exists
            }
        }
    }

    val scroll = rememberScrollState()

    // Scaffold gives a top-level layout structure (with padding, backgrounds, etc.)
    Scaffold { padding ->
        Column(
            modifier =
                Modifier.padding(padding)
                    // Background gradient (from theme)
                    .background(getLinearBrush(DefaultGradient, BrushDirection.DOWN_TOP))
                    .fillMaxSize()
                    .verticalScroll(scroll) // Enable scroll for smaller devices
        ) {
            // ----- App logo -----
            Spacer(modifier = Modifier.height(200.dp))
            Image(
                painter = painterResource(R.drawable.logo),
                contentDescription = "SkillSwap logo",
                modifier =
                    Modifier.size(280.dp)
                        .align(Alignment.CenterHorizontally)
                        .testTag(SignInTags.LOGO)
            )

            // ----- Google Sign-In button -----
            Spacer(modifier = Modifier.height(50.dp))
            OutlinedButton(
                onClick = { vm.googleSignIn(credentialManager, context as Activity) },
                colors = signInButtonColor,
                border = signInButtonStroke,
                modifier =
                    Modifier.align(Alignment.CenterHorizontally)
                        .fillMaxWidth(0.8f)
                        .testTag(SignInTags.GOOGLE_BUTTON)
            ) {
                Row(modifier = Modifier.height(21.dp)) {
                    Image(
                        painter = painterResource(R.drawable.google_logo),
                        contentDescription = "Google logo",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(text = "SIGN IN WITH GOOGLE")
                }
            }

            // ----- Classic Sign-In button -----
            Spacer(modifier = Modifier.height(20.dp))
            OutlinedButton(
                onClick = { vm.classicSignIn() },
                colors = signInButtonColor,
                border = signInButtonStroke,
                modifier =
                    Modifier.align(Alignment.CenterHorizontally)
                        .fillMaxWidth(0.8f)
                        .testTag(SignInTags.SIGN_IN_BUTTON)
            ) {
                Text(text = "SIGN IN")
            }

            // ----- Create Account button -----
            Spacer(modifier = Modifier.height(40.dp))
            GradientButton(
                onClick = { vm.createAccount() },
                modifier =
                    Modifier.align(Alignment.CenterHorizontally)
                        .testTag(SignInTags.CREATE_ACCOUNT_TEXT)
                        .fillMaxWidth(0.4f)
            ) {
                Text(
                    text = "Next",
                    fontSize = 24.sp,
                )
            }
        }
    }
}
