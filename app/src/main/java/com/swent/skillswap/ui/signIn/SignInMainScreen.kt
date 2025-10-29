/** @author Topaze17(Eliott) used chatGPT for tagging the composable but they were checked */
package com.swent.skillswap.ui.signIn

import android.app.Activity
import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.swent.skillswap.ui.theme.getLinearBrush
import com.swent.skillswap.ui.utils.GradientButton
import com.swent.skillswap.viewModel.SignInViewModel
import com.swent.skillswap.viewModel.SignInVmFactory

object SignInTags {
    const val LOGO = "SIGN_IN_LOGO"
    const val GOOGLE_BUTTON = "SIGN_IN_GOOGLE_BUTTON"
    const val SIGN_IN_BUTTON = "SIGN_IN_BUTTON"
    const val OR_TEXT = "SIGN_IN_OR_TEXT"
    const val CREATE_ACCOUNT_TEXT = "SIGN_IN_CREATE_ACCOUNT_TEXT"
}

val signInButtonColor = ButtonColors(Color.Transparent, Color.White, Color.White, Color.Black)
val signInButtonStroke = BorderStroke(1.dp, Color.White)

@Preview(showBackground = true)
@Composable
fun SignInMainScreen(
    goToMainScreen: () -> Unit = {},
    goToCreateAccountScreen: () -> Unit = {},
    context: Context = LocalContext.current,
    credentialManager: CredentialManager = CredentialManager.create(LocalContext.current)
) {
    val vm: SignInViewModel =
        viewModel(factory = SignInVmFactory(goToMainScreen, goToCreateAccountScreen))
    val scroll = rememberScrollState()
    Scaffold() { padding ->
        Column(
            modifier =
                Modifier.padding(padding)
                    .background(getLinearBrush(BrushDirection.DOWN_TOP))
                    .fillMaxSize(1f)
                    .verticalScroll(scroll)
        ) {
            Spacer(modifier = Modifier.height(200.dp))
            Image(
                painter = painterResource(R.drawable.logo),
                contentDescription = "SkillSwap logo",
                Modifier.size(280.dp).align(Alignment.CenterHorizontally).testTag(SignInTags.LOGO)
            )
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
                    Text(
                        text = "SIGN IN WITH GOOGLE",
                    )
                }
            }
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
