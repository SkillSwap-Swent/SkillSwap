/** @author Topaze17(Eliott) used chatGPT for tagging the composable but they were checked */
package com.swent.skillswap.ui.signIn

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.swent.skillswap.R

object SignInTags {
    const val LOGO = "SIGN_IN_LOGO"
    const val GOOGLE_BUTTON = "SIGN_IN_GOOGLE_BUTTON"
    const val SIGN_IN_BUTTON = "SIGN_IN_BUTTON"
    const val OR_TEXT = "SIGN_IN_OR_TEXT"
    const val CREATE_ACCOUNT_TEXT = "SIGN_IN_CREATE_ACCOUNT_TEXT"
}

@Preview(showBackground = true)
@Composable
fun SignInMainScreen(
    /*TODO remove comment once viewModel made ->*/
    /*viewModel: ViewModel = viewModel()*/
    goToMainScreen: () -> Unit = {},
    goToCreateAccountScreen: () -> Unit = {}
) {
    Scaffold() { padding ->
        Column(
            modifier =
                Modifier.padding(padding)
                    .background(color = MaterialTheme.colorScheme.primary)
                    .fillMaxSize(1f)
        ) {
            Spacer(modifier = Modifier.height(200.dp))
            Image(
                painter = painterResource(R.drawable.logo),
                contentDescription = "SkillSwap logo",
                Modifier.size(280.dp).align(Alignment.CenterHorizontally).testTag(SignInTags.LOGO)
            )
            Spacer(modifier = Modifier.height(50.dp))
            OutlinedButton(
                onClick = { /*TODO CLICK LOGIC GOOGLE SIGN IN*/},
                colors = ButtonColors(Color.White, Color.Black, Color.White, Color.Black),
                modifier =
                    Modifier.align(Alignment.CenterHorizontally).testTag(SignInTags.GOOGLE_BUTTON)
            ) {
                Row(modifier = Modifier.height(21.dp)) {
                    Image(
                        painter = painterResource(R.drawable.google_logo),
                        contentDescription = "Google logo",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Sign in with Google",
                    )
                }
            }
            OutlinedButton(
                onClick = { /*TODO CLICK LOGIC SIGN IN*/},
                colors = ButtonColors(Color.White, Color.Black, Color.White, Color.Black),
                modifier =
                    Modifier.align(Alignment.CenterHorizontally).testTag(SignInTags.SIGN_IN_BUTTON)
            ) {
                Text(text = "Sign in")
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "or",
                color = Color.White,
                modifier = Modifier.align(Alignment.CenterHorizontally).testTag(SignInTags.OR_TEXT)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text =
                    "Create an account", /*TODO remove comment when color theme correct color = MaterialTheme.colorScheme.secondary,*/
                modifier =
                    Modifier.align(Alignment.CenterHorizontally)
                        .clickable(enabled = true, onClick = { /*TODO CLICK LOGIC CREATE ACCOUNT*/})
                        .testTag(SignInTags.CREATE_ACCOUNT_TEXT)
            )
        }
    }
}
