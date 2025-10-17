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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swent.skillswap.model.tags.SkillTag
import com.swent.skillswap.ui.theme.ProfileGradientEnd
import com.swent.skillswap.ui.theme.ProfileGradientStart
import com.swent.skillswap.ui.theme.ProfileTextPrimary
import com.swent.skillswap.ui.theme.ProfileTextSecondary
import com.swent.skillswap.ui.utils.AccordionSection
import com.swent.skillswap.ui.utils.ProfileDivider

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
fun ProfileScreen(userSkills: Set<SkillTag> = emptySet(), onSkillsClick: () -> Unit = {}) {
    var expandedEmail by remember { mutableStateOf(false) }
    var expandedUsername by remember { mutableStateOf(false) }
    var expandedSkills by remember { mutableStateOf(false) }
    var expandedPreferences by remember { mutableStateOf(false) }
    var selectedPreference by remember { mutableStateOf("Money") }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Centered "Profile" title at the top
        Text(
            text = "Profile",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 24.dp).testTag(ProfileTestTags.PROFILE_TITLE)
        )

        // Single gradient rectangle with accordion sections
        Box(
            modifier =
                Modifier.fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        brush =
                            Brush.verticalGradient(
                                colors = listOf(ProfileGradientStart, ProfileGradientEnd)
                            )
                    )
                    .padding(16.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // My Email Section
                Box(modifier = Modifier.fillMaxWidth().testTag(ProfileTestTags.EMAIL_SECTION)) {
                    AccordionSection(
                        title = "My email",
                        isExpanded = expandedEmail,
                        onToggle = { expandedEmail = !expandedEmail },
                        content = {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "user@example.com",
                                    fontSize = 14.sp,
                                    color = ProfileTextSecondary,
                                    modifier =
                                        Modifier.weight(1f).testTag(ProfileTestTags.EMAIL_VALUE)
                                )
                                Text(
                                    text = "Edit",
                                    fontSize = 14.sp,
                                    color = ProfileTextPrimary,
                                    fontWeight = FontWeight.Medium,
                                    modifier =
                                        Modifier.testTag(ProfileTestTags.EMAIL_EDIT)
                                            .clickable {
                                                // TODO: Handle edit email
                                                println("Edit email clicked")
                                            }
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    )
                }

                // Horizontal divider
                ProfileDivider()

                // My Username Section
                Box(modifier = Modifier.fillMaxWidth().testTag(ProfileTestTags.USERNAME_SECTION)) {
                    AccordionSection(
                        title = "My username",
                        isExpanded = expandedUsername,
                        onToggle = { expandedUsername = !expandedUsername },
                        content = {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "john_doe",
                                    fontSize = 14.sp,
                                    color = Color.White.copy(alpha = 0.8f),
                                    modifier =
                                        Modifier.weight(1f).testTag(ProfileTestTags.USERNAME_VALUE)
                                )
                                Text(
                                    text = "Edit",
                                    fontSize = 14.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Medium,
                                    modifier =
                                        Modifier.testTag(ProfileTestTags.USERNAME_EDIT)
                                            .clickable {
                                                // TODO: Handle edit username
                                                println("Edit username clicked")
                                            }
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    )
                }

                // Horizontal divider
                ProfileDivider()

                // Skills Section
                Box(modifier = Modifier.fillMaxWidth().testTag(ProfileTestTags.SKILLS_SECTION)) {
                    AccordionSection(
                        title = "Skills",
                        isExpanded = expandedSkills,
                        onToggle = { expandedSkills = !expandedSkills },
                        content = {
                            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                                // Display current skills count
                                Text(
                                    text = "Current skills (${userSkills.size}):",
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.6f),
                                    modifier =
                                        Modifier.padding(bottom = 4.dp)
                                            .testTag(ProfileTestTags.SKILLS_COUNT)
                                )

                                // Display skills as a comma-separated list
                                if (userSkills.isNotEmpty()) {
                                    Text(
                                        text =
                                            userSkills.joinToString(", ") { skill ->
                                                skill.name
                                                    .replace("_", " ")
                                                    .lowercase()
                                                    .replaceFirstChar {
                                                        if (it.isLowerCase()) it.titlecase()
                                                        else it.toString()
                                                    }
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

                                // Edit skills button
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
                }

                // Horizontal divider
                ProfileDivider()

                // My Preferences Section
                Box(
                    modifier = Modifier.fillMaxWidth().testTag(ProfileTestTags.PREFERENCES_SECTION)
                ) {
                    AccordionSection(
                        title = "My preferences",
                        isExpanded = expandedPreferences,
                        onToggle = { expandedPreferences = !expandedPreferences },
                        content = {
                            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                                // Money option
                                Row(
                                    modifier =
                                        Modifier.fillMaxWidth()
                                            .testTag(ProfileTestTags.PREF_OPTION_MONEY)
                                            .clickable { selectedPreference = "Money" }
                                            .padding(vertical = 8.dp, horizontal = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Selection indicator
                                    Canvas(modifier = Modifier.size(16.dp)) {
                                        if (selectedPreference == "Money") {
                                            drawCircle(color = Color.White, radius = 6.dp.toPx())
                                        } else {
                                            drawCircle(
                                                color = Color.White.copy(alpha = 0.3f),
                                                radius = 6.dp.toPx(),
                                                style =
                                                    androidx.compose.ui.graphics.drawscope.Stroke(
                                                        width = 2.dp.toPx()
                                                    )
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Text(
                                        text = "Money",
                                        fontSize = 14.sp,
                                        color =
                                            if (selectedPreference == "Money") Color.White
                                            else Color.White.copy(alpha = 0.7f),
                                        fontWeight =
                                            if (selectedPreference == "Money") FontWeight.Medium
                                            else FontWeight.Normal
                                    )
                                }

                                // Skills option
                                Row(
                                    modifier =
                                        Modifier.fillMaxWidth()
                                            .testTag(ProfileTestTags.PREF_OPTION_SKILLS)
                                            .clickable { selectedPreference = "Skills" }
                                            .padding(vertical = 8.dp, horizontal = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Selection indicator
                                    Canvas(modifier = Modifier.size(16.dp)) {
                                        if (selectedPreference == "Skills") {
                                            drawCircle(color = Color.White, radius = 6.dp.toPx())
                                        } else {
                                            drawCircle(
                                                color = Color.White.copy(alpha = 0.3f),
                                                radius = 6.dp.toPx(),
                                                style =
                                                    androidx.compose.ui.graphics.drawscope.Stroke(
                                                        width = 2.dp.toPx()
                                                    )
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Text(
                                        text = "Skills",
                                        fontSize = 14.sp,
                                        color =
                                            if (selectedPreference == "Skills") Color.White
                                            else Color.White.copy(alpha = 0.7f),
                                        fontWeight =
                                            if (selectedPreference == "Skills") FontWeight.Medium
                                            else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    ProfileScreen(userSkills = emptySet(), onSkillsClick = {})
}
