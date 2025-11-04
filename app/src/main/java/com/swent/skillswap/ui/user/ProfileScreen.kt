package com.swent.skillswap.ui.user

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.swent.skillswap.model.user.Preference
import com.swent.skillswap.ui.utils.AccordionSection
import com.swent.skillswap.ui.utils.ProfileDivider
import com.swent.skillswap.viewModel.ProfileViewModel

object ProfileTestTags {
    const val PROFILE_TITLE = "profile_title"

    const val EDIT_PROFILE = "edit_profile"
    const val PROFILE_PICTURE = "profile_picture"

    const val EMAIL_SECTION = "email_section"
    const val EMAIL_VALUE = "email_value"
    const val USERNAME_SECTION = "username_section"
    const val USERNAME_VALUE = "username_value"
    const val SKILLS_SECTION = "skills_section"
    const val SKILLS_COUNT = "skills_count"
    const val SKILLS_LIST = "skills_list"
    const val SKILLS_EMPTY = "skills_empty"
    const val PREFERENCES_SECTION = "preferences_section"
    const val PREF_OPTION_MONEY = "pref_option_money"
    const val PREF_OPTION_SKILLS = "pref_option_skills"
}

@Composable
fun ProfileScreen(
    onEditProfileClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
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

        // Profile picture Section
        Box(
            modifier =
                Modifier.align(Alignment.CenterHorizontally)
                    .size(140.dp)
                    .padding(8.dp)
                    .testTag(ProfileTestTags.PROFILE_PICTURE)
        ) {
            if (uiState.profilePicture.isNotEmpty()) {
                AsyncImage(
                    model = uiState.profilePicture,
                    contentDescription = "Profile picture",
                    modifier = Modifier.size(140.dp).clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier =
                        Modifier.size(120.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile picture placeholder",
                        modifier = Modifier.size(60.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            // Edit button overlay
            Box(
                modifier =
                    Modifier.align(Alignment.BottomEnd)
                        .size(40.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                        .clickable { onEditProfileClick() }
                        .testTag(ProfileTestTags.EDIT_PROFILE),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit profile picture",
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }

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
                    modifier = Modifier.testTag(ProfileTestTags.EMAIL_SECTION),
                    content = {
                        EditableField(
                            value = uiState.email,
                            valueTestTag = ProfileTestTags.EMAIL_VALUE,
                        )
                    }
                )
                ProfileDivider()

                AccordionSection(
                    title = "My username",
                    isExpanded = expandedUsername,
                    onToggle = { expandedUsername = !expandedUsername },
                    modifier = Modifier.testTag(ProfileTestTags.USERNAME_SECTION),
                    content = {
                        EditableField(
                            value = uiState.username,
                            valueTestTag = ProfileTestTags.USERNAME_VALUE,
                        )
                    }
                )
                ProfileDivider()

                AccordionSection(
                    title = "Skills",
                    isExpanded = expandedSkills,
                    onToggle = { expandedSkills = !expandedSkills },
                    modifier = Modifier.testTag(ProfileTestTags.SKILLS_SECTION),
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
                        }
                    }
                )
                ProfileDivider()

                AccordionSection(
                    title = "My preferences",
                    isExpanded = expandedPreferences,
                    onToggle = { expandedPreferences = !expandedPreferences },
                    modifier = Modifier.testTag(ProfileTestTags.PREFERENCES_SECTION),
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

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = { onLogoutClick() },
            modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Text(
                text = "Logout",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onError
            )
        }
    }
}

@Composable
fun EditableField(
    value: String,
    valueTestTag: String,
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
