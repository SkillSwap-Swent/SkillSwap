/**
 * @author Topaze17(Eliott) used chatGPT for tagging the composable but they were
 *   checked
 */
package com.swent.skillswap.ui.signIn

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swent.skillswap.model.user.SkillName

object CreateAccountTags {
    const val TITLE = "CREATE_TITLE"
    const val USERNAME_FIELD = "CREATE_USERNAME_FIELD"
    const val EMAIL_FIELD = "CREATE_EMAIL_FIELD"
    const val PASSWORD_FIELD = "CREATE_PASSWORD_FIELD"
    const val CONFIRM_PASSWORD_FIELD = "CREATE_CONFIRM_PASSWORD_FIELD"
    const val SKILLS_FLOW = "CREATE_SKILLS_FLOW"
    const val SKILL_CHIP_PREFIX = "CREATE_SKILL_" // final tag = SKILL_CHIP_PREFIX + skill.name

    const val DONE_BUTTON = "CREATE_DONE_BUTTON"
}

@OptIn(ExperimentalLayoutApi::class)
@Preview(showBackground = true)
@Composable
fun SignInCreateAccountScreen(
    /*TODO remove comment once viewModel made ->*/
    /*viewModel: CreateAccountViewModel = viewModel()*/
    goToMainScreen: () -> Unit = {}
) {
    Scaffold { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).fillMaxSize(1f)) {
            Spacer(modifier = Modifier.height(100.dp))
            Text(
                text = "Create an account",
                modifier =
                    Modifier.align(Alignment.CenterHorizontally).testTag(CreateAccountTags.TITLE),
                color = MaterialTheme.colorScheme.primary,
                fontSize = 24.sp
            )
            Spacer(modifier = Modifier.height(100.dp))
            TextField(
                value = "" /*TODO value from viewModel to add*/,
                label = { Text(text = "Username", color = Color(0x5F000000)) },
                singleLine = true,
                placeholder = { Text(text = "username") },
                supportingText = { Text(text = "" /*TODO error message*/) },
                onValueChange = { /*TODO on value change logic with view event*/},
                shape = RoundedCornerShape(10.dp),
                colors =
                    TextFieldDefaults.colors(
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent
                    ),
                modifier =
                    Modifier.align(Alignment.CenterHorizontally)
                        .fillMaxWidth(0.8f)
                        .height(26.dp)
                        .testTag(CreateAccountTags.USERNAME_FIELD)
            )
            Spacer(modifier = Modifier.height(20.dp))
            Box(
                modifier = Modifier.height(210.dp).width(300.dp).align(Alignment.CenterHorizontally)
            ) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.testTag(CreateAccountTags.SKILLS_FLOW)
                ) {
                    for (skill in SkillName.entries) {
                        Text(
                            text = skill.name,
                            fontSize = 11.sp,
                            /*TODO remove when true theme are there color = MaterialTheme.colorScheme.secondary,*/ modifier =
                                Modifier.background(
                                        color = MaterialTheme.colorScheme.tertiary,
                                        shape = RoundedCornerShape(percent = 50)
                                    )
                                    .padding(5.dp, 0.dp)
                                    .clickable(
                                        enabled = true,
                                        onClick = { /*TODO Click logic on skill*/}
                                    )
                                    .testTag(CreateAccountTags.SKILL_CHIP_PREFIX + skill.name)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            TextField(
                value = "" /*TODO value from viewModel to add*/,
                label = { Text(text = "Email", color = Color(0x5F000000)) },
                singleLine = true,
                placeholder = { Text(text = "your.email@gmail.com") },
                supportingText = { Text(text = "" /*TODO error message*/) },
                onValueChange = { /*TODO on value change logic with view event*/},
                shape = RoundedCornerShape(10.dp),
                colors =
                    TextFieldDefaults.colors(
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent
                    ),
                modifier =
                    Modifier.align(Alignment.CenterHorizontally)
                        .fillMaxWidth(0.8f)
                        .height(26.dp)
                        .testTag(CreateAccountTags.EMAIL_FIELD)
            )
            Spacer(modifier = Modifier.height(30.dp))
            TextField(
                value = "" /*TODO value from viewModel to add*/,
                label = { Text(text = "Password", color = Color(0x5F000000)) },
                singleLine = true,
                placeholder = { Text(text = "password") },
                supportingText = { Text(text = "" /*TODO error message*/) },
                onValueChange = { /*TODO on value change logic with view event*/},
                shape = RoundedCornerShape(10.dp),
                colors =
                    TextFieldDefaults.colors(
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent
                    ),
                modifier =
                    Modifier.align(Alignment.CenterHorizontally)
                        .fillMaxWidth(0.8f)
                        .height(26.dp)
                        .testTag(CreateAccountTags.PASSWORD_FIELD)
            )
            Spacer(modifier = Modifier.height(30.dp))
            TextField(
                value = "" /*TODO value from viewModel to add*/,
                label = { Text(text = "Confirm Password", color = Color(0x5F000000)) },
                singleLine = true,
                placeholder = { Text(text = "confirm password") },
                supportingText = { Text(text = "" /*TODO error message*/) },
                onValueChange = { /*TODO on value change logic with view event*/},
                shape = RoundedCornerShape(10.dp),
                colors =
                    TextFieldDefaults.colors(
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent
                    ),
                modifier =
                    Modifier.align(Alignment.CenterHorizontally)
                        .fillMaxWidth(0.8f)
                        .height(26.dp)
                        .testTag(CreateAccountTags.CONFIRM_PASSWORD_FIELD)
            )
            Spacer(modifier = Modifier.height(80.dp))
            OutlinedButton(
                onClick = { /*TODO CLICK LOGIC DONE*/},
                colors =
                    ButtonColors(
                        MaterialTheme.colorScheme.primary,
                        Color.White,
                        MaterialTheme.colorScheme.primary,
                        Color.White
                    ),
                modifier =
                    Modifier.align(Alignment.CenterHorizontally)
                        .fillMaxWidth(0.33f)
                        .testTag(CreateAccountTags.DONE_BUTTON)
                        .testTag(CreateAccountTags.DONE_BUTTON)
            ) {
                Text(text = "Done")
            }
        }
    }
}
