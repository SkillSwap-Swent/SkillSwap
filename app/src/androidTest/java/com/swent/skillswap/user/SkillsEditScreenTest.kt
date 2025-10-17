// Kotlin
package com.swent.skillswap.user

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.swent.skillswap.model.tags.SkillTag
import com.swent.skillswap.ui.user.SkillsEditScreen
import com.swent.skillswap.ui.user.SkillsEditTestTags
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SkillsEditScreenTest {

    @get:Rule val composeTestRule = createComposeRule()

    @Test
    fun skillsEditScreen_displaysTitle() {
        composeTestRule.setContent {
            SkillsEditScreen(currentSkills = emptySet(), onBackClick = {}, onSkillsUpdated = {})
        }

        composeTestRule.onNodeWithTag(SkillsEditTestTags.TITLE).assertExists()
    }

    @Test
    fun skillsEditScreen_displaysCurrentSkills() {
        val skills = setOf(SkillTag.COMPUTER_PROGRAMMING, SkillTag.DATA_STRUCTURES)

        composeTestRule.setContent {
            SkillsEditScreen(currentSkills = skills, onBackClick = {}, onSkillsUpdated = {})
        }

        composeTestRule.onNodeWithTag(SkillsEditTestTags.SELECTED_COUNT).assertExists()
        composeTestRule.onNodeWithTag(SkillsEditTestTags.SELECTED_LIST).assertExists()
        composeTestRule.onNodeWithTag("${SkillsEditTestTags.SKILL_CHIP_PREFIX}_${SkillTag.COMPUTER_PROGRAMMING.name}").assertExists()
        composeTestRule.onNodeWithTag("${SkillsEditTestTags.SKILL_CHIP_PREFIX}_${SkillTag.DATA_STRUCTURES.name}").assertExists()
    }

    @Test
    fun skillsEditScreen_displaysEmptySkills() {
        composeTestRule.setContent {
            SkillsEditScreen(currentSkills = emptySet(), onBackClick = {}, onSkillsUpdated = {})
        }

        composeTestRule.onNodeWithTag(SkillsEditTestTags.SELECTED_COUNT).assertExists()
    }

    @Test
    fun skillsEditScreen_cancelButtonTriggersCallback() {
        var backClicked = false

        composeTestRule.setContent {
            SkillsEditScreen(
                currentSkills = emptySet(),
                onBackClick = { backClicked = true },
                onSkillsUpdated = {}
            )
        }

        composeTestRule.onNodeWithTag(SkillsEditTestTags.CANCEL_BUTTON).performClick()
        assert(backClicked)
    }

    @Test
    fun skillsEditScreen_saveButtonTriggersCallback() {
        var skillsUpdated = false
        var updatedSkills: Set<SkillTag>? = null

        composeTestRule.setContent {
            SkillsEditScreen(
                currentSkills = setOf(SkillTag.COMPUTER_PROGRAMMING),
                onBackClick = {},
                onSkillsUpdated = { skills ->
                    skillsUpdated = true
                    updatedSkills = skills
                }
            )
        }

        composeTestRule.onNodeWithTag(SkillsEditTestTags.SAVE_BUTTON).performClick()
        assert(skillsUpdated)
        assert(updatedSkills != null)
    }

    @Test
    fun skillsEditScreen_searchFieldAcceptsInput() {
        composeTestRule.setContent {
            SkillsEditScreen(currentSkills = emptySet(), onBackClick = {}, onSkillsUpdated = {})
        }

        composeTestRule.onNodeWithTag(SkillsEditTestTags.SEARCH_FIELD).performTextInput("prog")
    }

    @Test
    fun skillsEditScreen_displaysAddSkillsSection() {
        composeTestRule.setContent {
            SkillsEditScreen(currentSkills = emptySet(), onBackClick = {}, onSkillsUpdated = {})
        }

        // no dedicated tag for the section label; keep text lookup
        composeTestRule.onNodeWithText("Add Skills").assertExists()
    }
}
