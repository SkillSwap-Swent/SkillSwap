// Kotlin
package com.swent.skillswap.user

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.swent.skillswap.model.tags.SkillTag
import com.swent.skillswap.ui.user.ProfileMainScreen
import com.swent.skillswap.ui.user.ProfileTestTags
import com.swent.skillswap.ui.user.SkillsEditTestTags
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

        composeTestRule.onNodeWithTag(ProfileTestTags.PROFILE_TITLE).assertExists()
    }

    @Test
    fun profileMainScreen_navigatesToSkillsEdit() {
        composeTestRule.setContent {
            ProfileMainScreen(
                userSkills = setOf(SkillTag.COMPUTER_PROGRAMMING),
                onSkillsUpdated = {}
            )
        }

        // Toggle skills accordion and click edit
        composeTestRule.onNodeWithTag(ProfileTestTags.SKILLS_SECTION).performClick()
        composeTestRule.onNodeWithTag(ProfileTestTags.SKILLS_EDIT).performClick()

        // Should now show skills edit screen title
        composeTestRule.onNodeWithTag(SkillsEditTestTags.TITLE).assertExists()
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
        composeTestRule.onNodeWithTag(ProfileTestTags.SKILLS_SECTION).performClick()
        composeTestRule.onNodeWithTag(ProfileTestTags.SKILLS_EDIT).performClick()

        // Save changes
        composeTestRule.onNodeWithTag(SkillsEditTestTags.SAVE_BUTTON).performClick()

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

        // Navigate to skills edit and cancel
        composeTestRule.onNodeWithTag(ProfileTestTags.SKILLS_SECTION).performClick()
        composeTestRule.onNodeWithTag(ProfileTestTags.SKILLS_EDIT).performClick()
        composeTestRule.onNodeWithTag(SkillsEditTestTags.CANCEL_BUTTON).performClick()

        composeTestRule.onNodeWithTag(ProfileTestTags.PROFILE_TITLE).assertExists()
    }

    @Test
    fun profileMainScreen_displaysUserSkills() {
        val skills = setOf(SkillTag.COMPUTER_PROGRAMMING, SkillTag.DATA_STRUCTURES)

        composeTestRule.setContent { ProfileMainScreen(userSkills = skills, onSkillsUpdated = {}) }

        // Verify the skills count element is present
        composeTestRule.onNodeWithTag(ProfileTestTags.SKILLS_SECTION).performClick()
        composeTestRule.onNodeWithTag(ProfileTestTags.SKILLS_COUNT).assertExists()
        // Verify selected skills list exists
        composeTestRule.onNodeWithTag(ProfileTestTags.SKILLS_LIST).assertExists()
    }

    @Test
    fun profileMainScreen_handlesEmptySkills() {
        composeTestRule.setContent {
            ProfileMainScreen(userSkills = emptySet(), onSkillsUpdated = {})
        }
        composeTestRule.onNodeWithTag(ProfileTestTags.SKILLS_SECTION).performClick()
        composeTestRule.onNodeWithTag(ProfileTestTags.SKILLS_COUNT).assertExists()
        composeTestRule.onNodeWithTag(ProfileTestTags.SKILLS_EMPTY).assertExists()
    }

    @Test
    fun profileMainScreen_emailEditClickable() {
        composeTestRule.setContent {
            ProfileMainScreen(
                userSkills = setOf(SkillTag.COMPUTER_PROGRAMMING),
                onSkillsUpdated = {}
            )
        }

        // Open email accordion and click Edit
        composeTestRule.onNodeWithTag(ProfileTestTags.EMAIL_SECTION).performClick()
        composeTestRule.onNodeWithTag(ProfileTestTags.EMAIL_EDIT).performClick()

        // Ensure screen still shows profile title (no navigation / crash)
        composeTestRule.onNodeWithTag(ProfileTestTags.PROFILE_TITLE).assertExists()
    }

    @Test
    fun profileMainScreen_preferencesToggle() {
        composeTestRule.setContent {
            ProfileMainScreen(
                userSkills = setOf(SkillTag.COMPUTER_PROGRAMMING),
                onSkillsUpdated = {}
            )
        }

        // Open preferences and toggle selections
        composeTestRule.onNodeWithTag(ProfileTestTags.PREFERENCES_SECTION).performClick()
        composeTestRule.onNodeWithTag(ProfileTestTags.PREF_OPTION_SKILLS).performClick()

        composeTestRule.onNodeWithTag(ProfileTestTags.PREF_OPTION_MONEY).performClick()
    }
}
