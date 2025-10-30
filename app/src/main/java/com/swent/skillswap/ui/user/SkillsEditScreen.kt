package com.swent.skillswap.ui.user

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties
import com.swent.skillswap.model.tags.SkillTag
import com.swent.skillswap.ui.utils.GradientButton

object SkillsEditTestTags {
    const val SCREEN_CONTAINER = "skills_edit_screen_container"
    const val TITLE = "skills_edit_title"

    const val SEARCH_FIELD = "skills_search_field"
    const val DROPDOWN = "skills_dropdown"
    const val SUGGESTIONS_LIST = "skills_suggestions_list"
    const val SUGGESTION_ITEM_PREFIX = "skills_suggestion"

    const val SELECTED_COUNT = "skills_selected_count"
    const val SELECTED_LIST = "skills_selected_list"
    const val SKILL_CHIP_PREFIX = "skills_chip"

    const val CANCEL_BUTTON = "skills_cancel_button"
    const val SAVE_BUTTON = "skills_save_button"
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SkillsEditScreen(
    currentSkills: Set<SkillTag> = emptySet(),
    onBackClick: () -> Unit = {},
    onSkillsUpdated: (Set<SkillTag>) -> Unit = {}
) {
    var selectedSkills by remember { mutableStateOf(currentSkills) }
    var expanded by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    var hasFocus by remember { mutableStateOf(false) }

    Column(
        modifier =
            Modifier.fillMaxSize().padding(16.dp).testTag(SkillsEditTestTags.SCREEN_CONTAINER),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Title
        Text(
            text = "Edit Skills",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 24.dp).testTag(SkillsEditTestTags.TITLE)
        )

        // Skills selection section
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
                // Skills input section
                Text(
                    text = "Add Skills",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it },
                    modifier = Modifier.fillMaxWidth().testTag(SkillsEditTestTags.DROPDOWN)
                ) {
                    val suggestions =
                        remember(query) {
                            SkillTag.entries
                                .filter { skill ->
                                    query.isNotBlank() &&
                                        skill.name.contains(query, ignoreCase = true) &&
                                        skill !in selectedSkills
                                }
                                .take(5)
                        }

                    TextField(
                        value = query,
                        onValueChange = { query = it },
                        label = { Text("Search skills", color = Color.White.copy(alpha = 0.7f)) },
                        placeholder = {
                            Text("Type to search...", color = Color.White.copy(alpha = 0.5f))
                        },
                        modifier =
                            Modifier.menuAnchor()
                                .onFocusChanged { hasFocus = it.isFocused }
                                .fillMaxWidth()
                                .testTag(SkillsEditTestTags.SEARCH_FIELD),
                        colors =
                            TextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                disabledIndicatorColor = Color.Transparent
                            ),
                        shape = RoundedCornerShape(8.dp)
                    )

                    DropdownMenu(
                        expanded = expanded && hasFocus && suggestions.isNotEmpty(),
                        onDismissRequest = { expanded = false },
                        properties = PopupProperties(focusable = false),
                        modifier =
                            Modifier.fillMaxWidth(0.8f).testTag(SkillsEditTestTags.SUGGESTIONS_LIST)
                    ) {
                        suggestions.forEachIndexed { index, skill ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        skill.name.replace("_", " ").lowercase().replaceFirstChar {
                                            if (it.isLowerCase()) it.titlecase() else it.toString()
                                        },
                                        color = Color.Black
                                    )
                                },
                                onClick = {
                                    selectedSkills = selectedSkills + skill
                                    query = ""
                                    expanded = false
                                },
                                modifier =
                                    Modifier.testTag(
                                        "${SkillsEditTestTags.SUGGESTION_ITEM_PREFIX}_$index"
                                    )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Selected skills display
                Text(
                    text = "Selected Skills (${selectedSkills.size}):",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    modifier =
                        Modifier.padding(bottom = 8.dp).testTag(SkillsEditTestTags.SELECTED_COUNT)
                )

                Box(modifier = Modifier.height(120.dp).fillMaxWidth()) {
                    val flowScroll = rememberScrollState()
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier =
                            Modifier.verticalScroll(flowScroll)
                                .testTag(SkillsEditTestTags.SELECTED_LIST)
                    ) {
                        selectedSkills.forEach { skill ->
                            val tag = "${SkillsEditTestTags.SKILL_CHIP_PREFIX}_${skill.name}"
                            Box(
                                modifier =
                                    Modifier.background(
                                            color = Color.White.copy(alpha = 0.2f),
                                            shape = RoundedCornerShape(16.dp)
                                        )
                                        .clickable { selectedSkills = selectedSkills - skill }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                        .testTag(tag)
                            ) {
                                Text(
                                    text =
                                        skill.name.replace("_", " ").lowercase().replaceFirstChar {
                                            if (it.isLowerCase()) it.titlecase() else it.toString()
                                        },
                                    fontSize = 12.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Cancel button
            GradientButton(
                onClick = onBackClick,
                modifier = Modifier.weight(1f).testTag(SkillsEditTestTags.CANCEL_BUTTON)
            ) {
                Text("Cancel")
            }

            // Save button
            GradientButton(
                onClick = {
                    onSkillsUpdated(selectedSkills)
                    onBackClick()
                },
                modifier = Modifier.weight(1f).testTag(SkillsEditTestTags.SAVE_BUTTON)
            ) {
                Text("Save")
            }
        }
    }
}
