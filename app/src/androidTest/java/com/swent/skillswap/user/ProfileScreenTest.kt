// AI-Generated: Profile screen tests adapted to use test tags
package com.swent.skillswap.user

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.swent.skillswap.model.tags.SkillTag
import com.swent.skillswap.ui.user.ProfileScreen
import com.swent.skillswap.ui.user.ProfileTestTags
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProfileScreenTest {

    @get:Rule val composeTestRule = createComposeRule()

    @Test
    fun profileScreen_displaysTitle() {
        composeTestRule.setContent {
            ProfileScreen(userSkills = setOf(SkillTag.COMPUTER_PROGRAMMING), onSkillsClick = {})
        }

        composeTestRule.onNodeWithTag(ProfileTestTags.PROFILE_TITLE).assertExists()
    }

    @Test
    fun profileScreen_displaysUserSkills() {
        val skills = setOf(SkillTag.COMPUTER_PROGRAMMING, SkillTag.DATA_STRUCTURES)

        composeTestRule.setContent { ProfileScreen(userSkills = skills, onSkillsClick = {}) }

        // Open skills accordion first
        composeTestRule.onNodeWithTag(ProfileTestTags.SKILLS_SECTION).performClick()
        composeTestRule.onNodeWithTag(ProfileTestTags.SKILLS_COUNT).assertExists()
        composeTestRule.onNodeWithTag(ProfileTestTags.SKILLS_LIST).assertExists()
    }

    @Test
    fun profileScreen_displaysEmptySkills() {
        composeTestRule.setContent { ProfileScreen(userSkills = emptySet(), onSkillsClick = {}) }

        // Open skills accordion first
        composeTestRule.onNodeWithTag(ProfileTestTags.SKILLS_SECTION).performClick()
        composeTestRule.onNodeWithTag(ProfileTestTags.SKILLS_COUNT).assertExists()
        composeTestRule.onNodeWithTag(ProfileTestTags.SKILLS_EMPTY).assertExists()
    }

    @Test
    fun profileScreen_skillsClickTriggersCallback() {
        var callbackTriggered = false

        composeTestRule.setContent {
            ProfileScreen(
                userSkills = setOf(SkillTag.COMPUTER_PROGRAMMING),
                onSkillsClick = { callbackTriggered = true }
            )
        }

        // Expand skills section and click Edit
        composeTestRule.onNodeWithTag(ProfileTestTags.SKILLS_SECTION).performClick()
        composeTestRule.onNodeWithTag(ProfileTestTags.SKILLS_EDIT).performClick()

        assert(callbackTriggered)
    }

    @Test
    fun profileScreen_displaysEmailSection() {
        composeTestRule.setContent { ProfileScreen(userSkills = emptySet(), onSkillsClick = {}) }

        composeTestRule.onNodeWithTag(ProfileTestTags.EMAIL_SECTION).assertExists()
    }

    @Test
    fun profileScreen_displaysUsernameSection() {
        composeTestRule.setContent { ProfileScreen(userSkills = emptySet(), onSkillsClick = {}) }

        composeTestRule.onNodeWithTag(ProfileTestTags.USERNAME_SECTION).assertExists()
    }

    @Test
    fun profileScreen_displaysPreferencesSection() {
        composeTestRule.setContent { ProfileScreen(userSkills = emptySet(), onSkillsClick = {}) }

        composeTestRule.onNodeWithTag(ProfileTestTags.PREFERENCES_SECTION).assertExists()
    }

    @Test
    fun profileScreen_expandsEmailSection() {
        composeTestRule.setContent { ProfileScreen(userSkills = emptySet(), onSkillsClick = {}) }

        composeTestRule.onNodeWithTag(ProfileTestTags.EMAIL_SECTION).performClick()
        composeTestRule.onNodeWithTag(ProfileTestTags.EMAIL_VALUE).assertExists()
        composeTestRule.onNodeWithTag(ProfileTestTags.EMAIL_EDIT).assertExists()
    }

    @Test
    fun profileScreen_expandsUsernameSection() {
        composeTestRule.setContent { ProfileScreen(userSkills = emptySet(), onSkillsClick = {}) }

        composeTestRule.onNodeWithTag(ProfileTestTags.USERNAME_SECTION).performClick()
        composeTestRule.onNodeWithTag(ProfileTestTags.USERNAME_VALUE).assertExists()
        composeTestRule.onNodeWithTag(ProfileTestTags.USERNAME_EDIT).assertExists()
    }

    @Test
    fun profileScreen_expandsPreferencesSection() {
        composeTestRule.setContent { ProfileScreen(userSkills = emptySet(), onSkillsClick = {}) }

        composeTestRule.onNodeWithTag(ProfileTestTags.PREFERENCES_SECTION).performClick()
        composeTestRule.onNodeWithTag(ProfileTestTags.PREF_OPTION_MONEY).assertExists()
        composeTestRule.onNodeWithTag(ProfileTestTags.PREF_OPTION_SKILLS).assertExists()
    }

    @Test
    fun profileScreen_handlesLargeSkillSet() {
        val largeSkillSet =
            setOf(
                SkillTag.COMPUTER_PROGRAMMING,
                SkillTag.DATA_STRUCTURES,
                SkillTag.ALGORITHMS,
                SkillTag.DATABASES,
                SkillTag.PHYSICS_MECHANICS,
                SkillTag.CALCULUS,
                SkillTag.LINEAR_ALGEBRA
            )

        composeTestRule.setContent { ProfileScreen(userSkills = largeSkillSet, onSkillsClick = {}) }

        composeTestRule.onNodeWithTag(ProfileTestTags.SKILLS_SECTION).performClick()
        composeTestRule.onNodeWithTag(ProfileTestTags.SKILLS_COUNT).assertExists()
    }

    @Test
    fun profileScreen_skillsSectionExpandsAndCollapses() {
        composeTestRule.setContent {
            ProfileScreen(userSkills = setOf(SkillTag.COMPUTER_PROGRAMMING), onSkillsClick = {})
        }

        // Expand
        composeTestRule.onNodeWithTag(ProfileTestTags.SKILLS_SECTION).performClick()
        composeTestRule.onNodeWithTag(ProfileTestTags.SKILLS_COUNT).assertExists()

        // Collapse
        composeTestRule.onNodeWithTag(ProfileTestTags.SKILLS_SECTION).performClick()
    }
}
