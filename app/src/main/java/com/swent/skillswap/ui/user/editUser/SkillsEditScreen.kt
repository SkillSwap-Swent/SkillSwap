package com.swent.skillswap.ui.user.editUser

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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
import com.swent.skillswap.model.user.SkillRank
import com.swent.skillswap.ui.utils.RichTooltipSkillswap
import com.swent.skillswap.ui.utils.SkillPill
import com.swent.skillswap.ui.utils.SkillPillRated
import com.swent.skillswap.ui.utils.SkillSwapShadowButton
import com.swent.skillswap.ui.utils.TooltipDescriptions
import com.swent.skillswap.ui.utils.icon_size
import com.swent.skillswap.ui.utils.nextPillRankOrNull

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
    const val HELP_TIP = "skills_help_tip"
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SkillsEditScreen(vm: EditUserViewModel = viewModel(), onBackClick: () -> Unit = {}) {
    val userState by vm.uiState.collectAsState()
    val screenScroll = rememberScrollState()
    val scrollForOwnSkills = rememberScrollState()
    val scrollForOtherSkills = rememberScrollState()
    DisposableEffect(Unit) { onDispose { vm.clearLoadedState() } }
    Column(modifier = Modifier.fillMaxSize(1f).verticalScroll(screenScroll)) {
        Spacer(modifier = Modifier.height(40.dp))
        /** own skill Row Flow* */
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            val pad = 26.dp
            Spacer(Modifier.width(pad + icon_size))

            Box(modifier = Modifier.weight(2f), contentAlignment = Alignment.Center) {
                Text(
                    text = "Your Skills",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.testTag(SkillsEditTestTags.TITLE_YOUR_SKILLS)
                )
            }

            RichTooltipSkillswap(
                body = TooltipDescriptions.SKILL_RATING,
                modifier = Modifier.padding(end = pad).testTag(SkillsEditTestTags.HELP_TIP)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
        val skillOfUser = userState.editedUser?.skillSet ?: setOf()
        Box(
            modifier =
                Modifier.align(Alignment.CenterHorizontally)
                    .height(250.dp)
                    .testTag(SkillsEditTestTags.USER_SKILLS_BOX)
                    .clipToBounds()
                    .verticalScroll(scrollForOwnSkills)
        ) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth(0.7f).testTag(SkillsEditTestTags.USER_SKILLS_FLOW)
            ) {
                for (skill in skillOfUser) {
                    SkillPillRated(
                        skill,
                        true,
                        onClick = { clicked -> cycleSkillPillState(clicked, skillOfUser, vm) }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
        /** other skill Row Flow* */
        Text(
            text = "Select new ones",
            fontSize = 36.sp,
            fontWeight = FontWeight.Medium,
            modifier =
                Modifier.align(Alignment.CenterHorizontally)
                    .testTag(SkillsEditTestTags.TITLE_SELECT_NEW)
        )
        Spacer(modifier = Modifier.height(20.dp))
        val otherSkills =
            (SkillTag.entries.filter { skillTag ->
                    !(skillOfUser.any { it.name == skillTag }) && skillTag != SkillTag.MONEY
                })
                .toSet()

        Box(
            modifier =
                Modifier.align(Alignment.CenterHorizontally)
                    .testTag(SkillsEditTestTags.OTHER_SKILLS_BOX)
                    .height(250.dp)
                    .clipToBounds()
                    .verticalScroll(scrollForOtherSkills)
        ) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth(0.7f).testTag(SkillsEditTestTags.OTHER_SKILLS_FLOW)
            ) {
                for (skill in otherSkills) {
                    SkillPill(
                        skill,
                        false,
                        { skill ->
                            vm.setSkills(skillOfUser + Skill(skill, SkillRank.FAMILIAR.value, ""))
                        }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.weight(0.9f))
        /** save button* */
        SkillSwapShadowButton(
            onClick = {
                vm.validate()
                onBackClick()
            },
            modifier =
                Modifier.height(56.dp)
                    .align(Alignment.CenterHorizontally)
                    .testTag(SkillsEditTestTags.BACK_BUTTON),
        ) {
            Icon(imageVector = Icons.AutoMirrored.Default.ArrowForward, contentDescription = "Done")
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                "Save",
                fontSize = 16.sp,
            )
        }
    }
}

private fun cycleSkillPillState(clicked: Skill, skillOfUser: Set<Skill>, vm: EditUserViewModel) {
    val nextRank = nextPillRankOrNull(clicked.rank)

    val newSkills =
        if (nextRank == null) {
            // At EXPERT -> remove the skill entirely
            skillOfUser.filter { it.name != clicked.name }.toSet()
        } else {
            // Update that skill's rank
            skillOfUser
                .map { if (it.name == clicked.name) it.copy(rank = nextRank) else it }
                .toSet()
        }

    vm.setSkills(newSkills)
}
