package com.swent.skillswap.ui.profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swent.skillswap.model.tags.SkillTag
import com.swent.skillswap.ui.components.AccordionSection
import com.swent.skillswap.ui.components.ProfileDivider
import com.swent.skillswap.ui.theme.ProfileGradientEnd
import com.swent.skillswap.ui.theme.ProfileGradientStart
import com.swent.skillswap.ui.theme.ProfileTextPrimary
import com.swent.skillswap.ui.theme.ProfileTextSecondary

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
            modifier = Modifier.padding(bottom = 24.dp)
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
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "Edit",
                                fontSize = 14.sp,
                                color = ProfileTextPrimary,
                                fontWeight = FontWeight.Medium,
                                modifier =
                                    Modifier.clickable {
                                            // TODO: Handle edit email
                                            println("Edit email clicked")
                                        }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                )

                // Horizontal divider
                HorizontalDivider()

                // My Username Section
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
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "Edit",
                                fontSize = 14.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Medium,
                                modifier =
                                    Modifier.clickable(
                                            interactionSource =
                                                remember { MutableInteractionSource() },
                                            indication = null
                                        ) {
                                            // TODO: Handle edit username
                                            println("Edit username clicked")
                                        }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                )

                // Horizontal divider
                HorizontalDivider()

                // Skills Section
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
                                modifier = Modifier.padding(bottom = 4.dp)
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
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                            } else {
                                Text(
                                    text = "No skills selected",
                                    fontSize = 14.sp,
                                    color = Color.White.copy(alpha = 0.5f),
                                    modifier = Modifier.padding(bottom = 8.dp)
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
                                        .clickable(
                                            interactionSource =
                                                remember { MutableInteractionSource() },
                                            indication = null
                                        ) {
                                            onSkillsClick()
                                        }
                                        .padding(vertical = 8.dp, horizontal = 4.dp)
                            )
                        }
                    }
                )

                // Horizontal divider
                HorizontalDivider()

                // My Preferences Section
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
                                        .clickable(
                                            interactionSource =
                                                remember { MutableInteractionSource() },
                                            indication = null
                                        ) {
                                            selectedPreference = "Money"
                                        }
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
                                        .clickable(
                                            interactionSource =
                                                remember { MutableInteractionSource() },
                                            indication = null
                                        ) {
                                            selectedPreference = "Skills"
                                        }
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

@Composable
fun AccordionSection(
    title: String,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Clickable header row
        Row(
            modifier =
                Modifier.fillMaxWidth()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        onToggle()
                    }
                    .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontSize = 14.sp,
                color = Color.White,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )

            Icon(
                imageVector =
                    if (isExpanded) Icons.Default.KeyboardArrowUp
                    else Icons.Default.KeyboardArrowDown,
                contentDescription = if (isExpanded) "Collapse" else "Expand",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }

        // Expandable content
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically(animationSpec = tween(300)),
            exit = shrinkVertically(animationSpec = tween(300))
        ) {
            content()
        }
    }
}

@Composable
fun HorizontalDivider() {
    Canvas(modifier = Modifier.fillMaxWidth().height(1.dp)) {
        drawLine(
            color = ProfileDivider,
            start = Offset(0f, 0f),
            end = Offset(size.width, 0f),
            strokeWidth = 1.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}
