// AI-Generated: Comprehensive test suite for profile screen components
package com.swent.skillswap.user

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.swent.skillswap.model.tags.SkillTag
import com.swent.skillswap.ui.user.SkillsEditScreen
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

        composeTestRule.onNodeWithText("Edit Skills").assertExists()
    }

    @Test
    fun skillsEditScreen_displaysCurrentSkills() {
        val skills = setOf(SkillTag.COMPUTER_PROGRAMMING, SkillTag.DATA_STRUCTURES)

        composeTestRule.setContent {
            SkillsEditScreen(currentSkills = skills, onBackClick = {}, onSkillsUpdated = {})
        }

        composeTestRule.onNodeWithText("Selected Skills (2):").assertExists()
        composeTestRule.onNodeWithText("Computer Programming").assertExists()
        composeTestRule.onNodeWithText("Data Structures").assertExists()
    }

    @Test
    fun skillsEditScreen_displaysEmptySkills() {
        composeTestRule.setContent {
            SkillsEditScreen(currentSkills = emptySet(), onBackClick = {}, onSkillsUpdated = {})
        }

        composeTestRule.onNodeWithText("Selected Skills (0):").assertExists()
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

        composeTestRule.onNodeWithText("Cancel").performClick()
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

        composeTestRule.onNodeWithText("Save").performClick()
        assert(skillsUpdated)
        assert(updatedSkills != null)
    }

    @Test
    fun skillsEditScreen_searchFieldAcceptsInput() {
        composeTestRule.setContent {
            SkillsEditScreen(currentSkills = emptySet(), onBackClick = {}, onSkillsUpdated = {})
        }

        composeTestRule.onNodeWithText("Search skills").performTextInput("prog")
        // Search field should accept input without crashing
    }

    @Test
    fun skillsEditScreen_displaysAddSkillsSection() {
        composeTestRule.setContent {
            SkillsEditScreen(currentSkills = emptySet(), onBackClick = {}, onSkillsUpdated = {})
        }

        composeTestRule.onNodeWithText("Add Skills").assertExists()
    }
}
