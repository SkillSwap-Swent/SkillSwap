/** @author Topaze17(Eliott) used chatGPT for tagging the composable but they were checked */
package com.swent.skillswap.ui.signIn

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
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
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties
import com.swent.skillswap.model.tags.SkillTag
import com.swent.skillswap.ui.signIn.CreateAccountTags.SKILL_SUGGESTION_PREFIX
import com.swent.skillswap.ui.theme.SkillSwapAppTheme
import kotlin.Boolean

object CreateAccountTags {

    const val TITLE = "CREATE_TITLE"
    const val USERNAME_FIELD = "CREATE_USERNAME_FIELD"
    const val EMAIL_FIELD = "CREATE_EMAIL_FIELD"
    const val PASSWORD_FIELD = "CREATE_PASSWORD_FIELD"
    const val CONFIRM_PASSWORD_FIELD = "CREATE_CONFIRM_PASSWORD_FIELD"
    const val SKILLS_INPUT = "CREATE_SKILLS_INPUT"
    const val SKILLS_FLOW = "CREATE_SKILLS_FLOW"
    const val SKILL_SUGGESTION_PREFIX = "CREATE_SKILL_SUGGESTION_"
    const val SKILL_CHIP_PREFIX = "CREATE_SKILL_" // final tag = SKILL_CHIP_PREFIX + skill.name

    const val DONE_BUTTON = "CREATE_DONE_BUTTON"
}

@Preview(showBackground = true)
@Composable
fun View() {
    SkillSwapAppTheme(dynamicColor = false, content = { SignInCreateAccountScreen() })
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SignInCreateAccountScreen(
    /*TODO remove comment once viewModel made ->*/
    /*viewModel: CreateAccountViewModel = viewModel()*/
    goToMainScreen: () -> Unit = {},
    googleAccount: Boolean = false
) {
    var selectedSkills by remember {
        mutableStateOf(setOf<SkillTag>())
    } /*TODO move it to the viewModel once created*/
    val scroll = rememberScrollState()
    Scaffold { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).fillMaxSize(1f).verticalScroll(scroll)) {
            Spacer(modifier = Modifier.height(60.dp))
            Text(
                text = "Create an account",
                modifier =
                    Modifier.align(Alignment.CenterHorizontally).testTag(CreateAccountTags.TITLE),
                color = MaterialTheme.colorScheme.primary,
                fontSize = 24.sp
            )
            Spacer(modifier = Modifier.height(76.dp))
            SkillSwapTextField(
                /*TODO remove comment once viewModel done
                TODO value = ,
                TODO supportText = ,*/
                label = "Username",
                placeholder = "username",
                modifier =
                    Modifier.align(Alignment.CenterHorizontally)
                        .testTag(CreateAccountTags.USERNAME_FIELD)
            )
            var expanded by remember { mutableStateOf(false) }
            val query = remember { mutableStateOf("") }
            var hasFocus by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it },
                modifier =
                    Modifier.fillMaxWidth(1f)
                        .align(Alignment.CenterHorizontally)
                        .wrapContentSize(Alignment.TopCenter)
            ) {
                val suggestions =
                    remember(query.value) {
                        SkillTag.entries
                            .filter {
                                query.value.isNotBlank() &&
                                    it.name.contains(query.value, ignoreCase = true)
                            }
                            .take(5)
                    }
                SkillSwapTextField(
                    value = query,
                    /*TODO remove comment once viewModel done
                    TODO supportText = ,*/
                    label = "Skills",
                    placeholder = "choose a skill",
                    modifier =
                        Modifier.menuAnchor()
                            .onFocusChanged { hasFocus = it.isFocused }
                            .testTag(CreateAccountTags.SKILLS_INPUT)
                )
                DropdownMenu(
                    expanded = expanded && hasFocus && suggestions.isNotEmpty(),
                    onDismissRequest = { expanded = false },
                    properties = PopupProperties(focusable = false),
                    modifier = Modifier.fillMaxWidth(0.8f).focusable(false)
                ) {
                    SkillForDropDownMenu(
                        suggestions,
                        { skillTag ->
                            selectedSkills = selectedSkills + skillTag
                            query.value = ""
                        }
                    )
                }
            }
            Box(
                modifier =
                    Modifier.height(100.dp).fillMaxWidth(0.8f).align(Alignment.CenterHorizontally)
            ) {
                val flowScroll = rememberScrollState()
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalArrangement = Arrangement.spacedBy(1.dp),
                    modifier =
                        Modifier.testTag(CreateAccountTags.SKILLS_FLOW).verticalScroll(scroll)
                ) {
                    for (skill in selectedSkills) {
                        Box(
                            modifier =
                                Modifier.background(
                                        color =
                                            MaterialTheme.colorScheme
                                                .tertiary /*TODO util fun for getting primary color of enum object*/,
                                        shape = RoundedCornerShape(50)
                                    )
                                    .clickable { selectedSkills = selectedSkills - skill }
                                    .padding(horizontal = 2.dp, vertical = 0.dp)
                                    .testTag(CreateAccountTags.SKILL_CHIP_PREFIX + skill.name)
                        ) {
                            Text(
                                text = skill.name /*TODO util function for better enum name*/,
                                fontSize = 10.sp,
                                color =
                                    MaterialTheme.colorScheme
                                        .secondary /*TODO util fun for getting secondary color of enum object*/,
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            SkillSwapTextField(
                /*TODO remove comment once viewModel done
                TODO value = ,
                TODO supportText = ,*/
                label = "Email",
                placeholder = "your.email@gmail.com",
                enabled = !googleAccount,
                modifier =
                    Modifier.align(Alignment.CenterHorizontally)
                        .testTag(CreateAccountTags.EMAIL_FIELD)
            )
            SkillSwapPasswordTextField(
                /*TODO remove comment once viewModel done
                TODO value = ,
                TODO supportText = ,*/
                label = "Password",
                placeholder = "enter password",
                enabled = !googleAccount,
                modifier =
                    Modifier.align(Alignment.CenterHorizontally)
                        .testTag(CreateAccountTags.PASSWORD_FIELD)
            )
            SkillSwapPasswordTextField(
                /*TODO remove comment once viewModel done
                TODO value = ,
                TODO supportText = ,*/
                label = "Confirm Password",
                enabled = !googleAccount,
                placeholder = "enter password",
                modifier =
                    Modifier.align(Alignment.CenterHorizontally)
                        .testTag(CreateAccountTags.CONFIRM_PASSWORD_FIELD)
            )
            Spacer(modifier = Modifier.height(20.dp))
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
            ) {
                Text(text = "Done")
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun SkillSwapTextField(
    modifier: Modifier = Modifier,
    value: MutableState<String> = mutableStateOf(""),
    supportText: String = "",
    label: String = "",
    placeholder: String = "",
    onValueChange: () -> Unit = {},
    enabled: Boolean = true
) {
    TextField(
        value = value.value,
        label = { Text(text = label, color = Color(0x5F000000)) },
        singleLine = true,
        placeholder = { Text(text = placeholder, color = Color(0x5F000000)) },
        supportingText = { Text(text = supportText) },
        onValueChange = { it ->
            value.value = it
            onValueChange()
        },
        enabled = enabled,
        shape = RoundedCornerShape(10.dp),
        colors =
            TextFieldDefaults.colors(
                unfocusedIndicatorColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent
            ),
        modifier = modifier.fillMaxWidth(0.8f)
    )
}

@Composable
fun SkillSwapPasswordTextField(
    modifier: Modifier = Modifier,
    value: MutableState<String> = mutableStateOf(""),
    supportText: String = "",
    label: String = "",
    placeholder: String = "",
    enabled: Boolean = true,
) {
    var showPassword by remember { mutableStateOf(false) }
    TextField(
        value = value.value,
        onValueChange = { value.value = it },
        label = { Text(label, color = Color(0x5F000000)) },
        placeholder = { Text(placeholder, color = Color(0x5F000000)) },
        supportingText = { Text(text = supportText) },
        singleLine = true,
        keyboardOptions =
            KeyboardOptions(
                keyboardType = KeyboardType.Password,
                autoCorrect = false,
                capitalization = KeyboardCapitalization.None,
                imeAction = ImeAction.Done
            ),
        shape = RoundedCornerShape(10.dp),
        enabled = enabled,
        colors =
            TextFieldDefaults.colors(
                unfocusedIndicatorColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent
            ),
        visualTransformation =
            if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            val image = if (showPassword) Icons.Filled.Close else Icons.Filled.Search

            val description = if (showPassword) "Hide password" else "Show password"

            IconButton(onClick = { showPassword = !showPassword }) {
                Icon(imageVector = image, contentDescription = description)
            }
        },
        modifier = modifier.fillMaxWidth(0.8f)
    )
}

@Composable
fun SkillForDropDownMenu(suggestion: List<SkillTag>, onPick: (SkillTag) -> Unit) {
    suggestion.forEach { skillTag ->
        DropdownMenuItem(
            text = { Text(skillTag.name, maxLines = 1, overflow = TextOverflow.Ellipsis) },
            onClick = { onPick(skillTag) },
            modifier = Modifier.testTag(SKILL_SUGGESTION_PREFIX + skillTag)
        )
    }
}
