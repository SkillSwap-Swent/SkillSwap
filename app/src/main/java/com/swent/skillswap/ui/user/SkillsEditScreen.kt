package com.swent.skillswap.ui.user

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.swent.skillswap.model.tags.SkillTag
import com.swent.skillswap.model.user.Skill
import com.swent.skillswap.ui.editUser.EditUserViewModel
import com.swent.skillswap.ui.user.SkillsEditTestTags.BACK_BUTTON
import com.swent.skillswap.ui.user.SkillsEditTestTags.OTHER_SKILLS_BOX
import com.swent.skillswap.ui.user.SkillsEditTestTags.OTHER_SKILLS_FLOW
import com.swent.skillswap.ui.user.SkillsEditTestTags.TITLE_SELECT_NEW
import com.swent.skillswap.ui.user.SkillsEditTestTags.TITLE_YOUR_SKILLS
import com.swent.skillswap.ui.user.SkillsEditTestTags.USER_SKILLS_BOX
import com.swent.skillswap.ui.user.SkillsEditTestTags.USER_SKILLS_FLOW
import com.swent.skillswap.ui.utils.SkillPill
import com.swent.skillswap.ui.utils.SkillSwapShadowButton

object SkillsEditTestTags {
    // Screen-level
    const val TITLE_YOUR_SKILLS = "skills_title_your_skills"
    const val TITLE_SELECT_NEW = "skills_title_select_new"

    // User’s skills section
    const val USER_SKILLS_BOX = "skills_user_box"
    const val USER_SKILLS_FLOW = "skills_user_flow"

    // Other skills section
    const val OTHER_SKILLS_BOX = "skills_other_box"
    const val OTHER_SKILLS_FLOW = "skills_other_flow"

    // Buttons
    const val BACK_BUTTON = "skills_back_button"
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SkillsEditScreen(vm: EditUserViewModel = viewModel(), onBackClick: () -> Unit = {}) {
    val userState by vm.uiState.collectAsState()
    val screenScroll = rememberScrollState()
    val scrollForOwnSkills = rememberScrollState()
    val scrollForOtherSkills = rememberScrollState()
    Column(modifier = Modifier.fillMaxSize(1f).verticalScroll(screenScroll)) {
        Spacer(modifier = Modifier.height(40.dp))
        Text(
            text = "Your Skills",
            fontSize = 36.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.align(Alignment.CenterHorizontally).testTag(TITLE_YOUR_SKILLS)
        )
        Spacer(modifier = Modifier.height(20.dp))
        val skillOfUser = userState.editedUser?.skillSet ?: setOf()
        Box(
            modifier =
                Modifier.align(Alignment.CenterHorizontally)
                    .height(250.dp)
                    .testTag(USER_SKILLS_BOX)
                    .clipToBounds()
                    .verticalScroll(scrollForOwnSkills)
        ) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth(0.7f).testTag(USER_SKILLS_FLOW)
            ) {
                for (skill in skillOfUser) {
                    SkillPill(
                        skill.name,
                        true,
                        { skill -> vm.setSkills(skillOfUser.filter { it.name != skill }.toSet()) }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "Select new ones",
            fontSize = 36.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.align(Alignment.CenterHorizontally).testTag(TITLE_SELECT_NEW)
        )
        Spacer(modifier = Modifier.height(20.dp))
        val otherSkills =
            (SkillTag.entries.filter { skillTag -> !(skillOfUser.any { it.name == skillTag }) })
                .toSet()
        Box(
            modifier =
                Modifier.align(Alignment.CenterHorizontally)
                    .testTag(OTHER_SKILLS_BOX)
                    .height(250.dp)
                    .clipToBounds()
                    .verticalScroll(scrollForOtherSkills)
        ) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth(0.7f).testTag(OTHER_SKILLS_FLOW)
            ) {
                for (skill in otherSkills) {
                    SkillPill(
                        skill,
                        false,
                        { skill -> vm.setSkills(skillOfUser + Skill(skill, 0f, "")) }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.weight(0.9f))
        SkillSwapShadowButton(
            onClick = { onBackClick() },
            modifier =
                Modifier.height(56.dp).align(Alignment.CenterHorizontally).testTag(BACK_BUTTON),
        ) {
            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                "Back",
                fontSize = 16.sp,
            )
        }
    }
}
