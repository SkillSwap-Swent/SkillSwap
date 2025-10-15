// AI-Generated: Comprehensive test suite for profile screen components
package com.swent.skillswap.ui.profile

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.swent.skillswap.model.tags.SkillTag
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProfileMainScreenTest {

    @get:Rule val composeTestRule = createComposeRule()

    @Test
    fun profileMainScreen_displaysMainProfile() {
        composeTestRule.setContent {
            ProfileMainScreen(
                userSkills = setOf(SkillTag.COMPUTER_PROGRAMMING),
                onSkillsUpdated = {}
            )
        }

        composeTestRule.onNodeWithText("Profile").assertExists()
    }

    @Test
    fun profileMainScreen_navigatesToSkillsEdit() {
        composeTestRule.setContent {
            ProfileMainScreen(
                userSkills = setOf(SkillTag.COMPUTER_PROGRAMMING),
                onSkillsUpdated = {}
            )
        }

        // Expand skills section
        composeTestRule.onNodeWithText("Skills").performClick()
        // Click edit skills
        composeTestRule.onNodeWithText("Edit Skills").performClick()

        // Should now show skills edit screen
        composeTestRule.onNodeWithText("Edit Skills").assertExists()
    }

    @Test
    fun profileMainScreen_skillsUpdatedCallback() {
        var skillsUpdated = false
        var updatedSkills: Set<SkillTag>? = null

        composeTestRule.setContent {
            ProfileMainScreen(
                userSkills = setOf(SkillTag.COMPUTER_PROGRAMMING),
                onSkillsUpdated = { skills ->
                    skillsUpdated = true
                    updatedSkills = skills
                }
            )
        }

        // Navigate to skills edit
        composeTestRule.onNodeWithText("Skills").performClick()
        composeTestRule.onNodeWithText("Edit Skills").performClick()

        // Save changes
        composeTestRule.onNodeWithText("Save").performClick()

        assert(skillsUpdated)
        assert(updatedSkills != null)
    }

    @Test
    fun profileMainScreen_cancelFromSkillsEdit() {
        composeTestRule.setContent {
            ProfileMainScreen(
                userSkills = setOf(SkillTag.COMPUTER_PROGRAMMING),
                onSkillsUpdated = {}
            )
        }

        // Navigate to skills edit
        composeTestRule.onNodeWithText("Skills").performClick()
        composeTestRule.onNodeWithText("Edit Skills").performClick()

        // Cancel should return to main profile
        composeTestRule.onNodeWithText("Cancel").performClick()
        composeTestRule.onNodeWithText("Profile").assertExists()
    }

    @Test
    fun profileMainScreen_displaysUserSkills() {
        val skills = setOf(SkillTag.COMPUTER_PROGRAMMING, SkillTag.DATA_STRUCTURES)

        composeTestRule.setContent { ProfileMainScreen(userSkills = skills, onSkillsUpdated = {}) }

        composeTestRule.onNodeWithText("Current skills (2):").assertExists()
    }

    @Test
    fun profileMainScreen_handlesEmptySkills() {
        composeTestRule.setContent {
            ProfileMainScreen(userSkills = emptySet(), onSkillsUpdated = {})
        }

        composeTestRule.onNodeWithText("Current skills (0):").assertExists()
        composeTestRule.onNodeWithText("No skills selected").assertExists()
    }
}
