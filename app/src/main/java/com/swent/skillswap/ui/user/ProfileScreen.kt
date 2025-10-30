package com.swent.skillswap.ui.user

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.swent.skillswap.model.user.Preference
import com.swent.skillswap.ui.utils.AccordionSection
import com.swent.skillswap.ui.utils.ProfileDivider
import com.swent.skillswap.viewModel.ProfileViewModel

object ProfileTestTags {
    const val PROFILE_TITLE = "profile_title"

    const val EMAIL_SECTION = "email_section"
    const val EMAIL_VALUE = "email_value"
    const val EMAIL_EDIT = "email_edit"

    const val USERNAME_SECTION = "username_section"
    const val USERNAME_VALUE = "username_value"
    const val USERNAME_EDIT = "username_edit"

    const val SKILLS_SECTION = "skills_section"
    const val SKILLS_COUNT = "skills_count"
    const val SKILLS_LIST = "skills_list"
    const val SKILLS_EMPTY = "skills_empty"
    const val SKILLS_EDIT = "skills_edit"

    const val PREFERENCES_SECTION = "preferences_section"
    const val PREF_OPTION_MONEY = "pref_option_money"
    const val PREF_OPTION_SKILLS = "pref_option_skills"
}

@Composable
fun ProfileScreen(
    onSkillsClick: () -> Unit = {},
    onUsernameEditClick: () -> Unit = {},
    onEmailEditClick: () -> Unit = {},
    vm: ProfileViewModel = viewModel()
) {
    var expandedEmail by remember { mutableStateOf(false) }
    var expandedUsername by remember { mutableStateOf(false) }
    var expandedSkills by remember { mutableStateOf(false) }
    var expandedPreferences by remember { mutableStateOf(false) }

    val uiState by vm.userState.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Profile",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 24.dp).testTag(ProfileTestTags.PROFILE_TITLE)
        )

        Box(
            modifier =
                Modifier.fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        brush =
                            Brush.verticalGradient(
                                colors =
                                    listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.primaryContainer
                                    )
                            )
                    )
                    .padding(16.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                AccordionSection(
                    title = "My email",
                    isExpanded = expandedEmail,
                    onToggle = { expandedEmail = !expandedEmail },
                    content = {
                        EditableField(
                            value = uiState.email,
                            onEditClick = { onEmailEditClick() },
                            valueTestTag = ProfileTestTags.EMAIL_VALUE,
                            editTestTag = ProfileTestTags.EMAIL_EDIT
                        )
                    }
                )
                ProfileDivider()

                AccordionSection(
                    title = "My username",
                    isExpanded = expandedUsername,
                    onToggle = { expandedUsername = !expandedUsername },
                    content = {
                        EditableField(
                            value = uiState.username,
                            onEditClick = { onUsernameEditClick() },
                            valueTestTag = ProfileTestTags.USERNAME_VALUE,
                            editTestTag = ProfileTestTags.USERNAME_EDIT
                        )
                    }
                )
                ProfileDivider()

                AccordionSection(
                    title = "Skills",
                    isExpanded = expandedSkills,
                    onToggle = { expandedSkills = !expandedSkills },
                    content = {
                        Column(modifier = Modifier.padding(vertical = 8.dp)) {
                            Text(
                                text = "Current skills (${uiState.skillSet.size}):",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.6f),
                                modifier =
                                    Modifier.padding(bottom = 4.dp)
                                        .testTag(ProfileTestTags.SKILLS_COUNT)
                            )
                            if (uiState.skillSet.isNotEmpty()) {
                                Text(
                                    text =
                                        uiState.skillSet.joinToString(", ") { skill ->
                                            skill.name.name
                                                .replace("_", " ")
                                                .lowercase()
                                                .replaceFirstChar { it.titlecase() }
                                        },
                                    fontSize = 14.sp,
                                    color = Color.White.copy(alpha = 0.8f),
                                    modifier =
                                        Modifier.padding(bottom = 8.dp)
                                            .testTag(ProfileTestTags.SKILLS_LIST)
                                )
                            } else {
                                Text(
                                    text = "No skills selected",
                                    fontSize = 14.sp,
                                    color = Color.White.copy(alpha = 0.5f),
                                    modifier =
                                        Modifier.padding(bottom = 8.dp)
                                            .testTag(ProfileTestTags.SKILLS_EMPTY)
                                )
                            }
                            Text(
                                text = "Edit Skills",
                                fontSize = 14.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Medium,
                                modifier =
                                    Modifier.fillMaxWidth()
                                        .testTag(ProfileTestTags.SKILLS_EDIT)
                                        .clickable { onSkillsClick() }
                                        .padding(vertical = 8.dp, horizontal = 4.dp)
                            )
                        }
                    }
                )
                ProfileDivider()

                AccordionSection(
                    title = "My preferences",
                    isExpanded = expandedPreferences,
                    onToggle = { expandedPreferences = !expandedPreferences },
                    content = {
                        Column(modifier = Modifier.padding(vertical = 8.dp)) {
                            PreferenceOption(
                                label = "Money",
                                isSelected = uiState.preference == Preference.MONEY,
                                onClick = {
                                    vm.updateUserAttributes(preference = Preference.MONEY)
                                },
                                testTag = ProfileTestTags.PREF_OPTION_MONEY
                            )
                            PreferenceOption(
                                label = "Skills",
                                isSelected = uiState.preference == Preference.SKILLS,
                                onClick = {
                                    vm.updateUserAttributes(preference = Preference.SKILLS)
                                },
                                testTag = ProfileTestTags.PREF_OPTION_SKILLS
                            )
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun EditableField(
    value: String,
    onEditClick: () -> Unit,
    valueTestTag: String,
    editTestTag: String
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = value,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f).testTag(valueTestTag)
        )
        Text(
            text = "Edit",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Medium,
            modifier =
                Modifier.testTag(editTestTag)
                    .clickable { onEditClick() }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun PreferenceOption(label: String, isSelected: Boolean, onClick: () -> Unit, testTag: String) {
    Row(
        modifier =
            Modifier.fillMaxWidth()
                .testTag(testTag)
                .clickable { onClick() }
                .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Canvas(modifier = Modifier.size(16.dp)) {
            if (isSelected) {
                drawCircle(color = Color.White, radius = 6.dp.toPx())
            } else {
                drawCircle(
                    color = Color.White.copy(alpha = 0.3f),
                    radius = 6.dp.toPx(),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = label,
            fontSize = 14.sp,
            color = if (isSelected) Color.White else Color.White.copy(alpha = 0.7f),
            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
        )
    }
}
