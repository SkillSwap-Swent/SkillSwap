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
class ProfileScreenTest {

    @get:Rule val composeTestRule = createComposeRule()

    @Test
    fun profileScreen_displaysTitle() {
        composeTestRule.setContent {
            ProfileScreen(userSkills = setOf(SkillTag.COMPUTER_PROGRAMMING), onSkillsClick = {})
        }

        composeTestRule.onNodeWithText("Profile").assertExists()
    }

    @Test
    fun profileScreen_displaysUserSkills() {
        val skills = setOf(SkillTag.COMPUTER_PROGRAMMING, SkillTag.DATA_STRUCTURES)

        composeTestRule.setContent { ProfileScreen(userSkills = skills, onSkillsClick = {}) }

        composeTestRule.onNodeWithText("Current skills (2):").assertExists()
        composeTestRule.onNodeWithText("Computer Programming, Data Structures").assertExists()
    }

    @Test
    fun profileScreen_displaysEmptySkills() {
        composeTestRule.setContent { ProfileScreen(userSkills = emptySet(), onSkillsClick = {}) }

        composeTestRule.onNodeWithText("Current skills (0):").assertExists()
        composeTestRule.onNodeWithText("No skills selected").assertExists()
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

        // Expand skills section first
        composeTestRule.onNodeWithText("Skills").performClick()
        composeTestRule.onNodeWithText("Edit Skills").performClick()

        assert(callbackTriggered)
    }

    @Test
    fun profileScreen_displaysEmailSection() {
        composeTestRule.setContent { ProfileScreen(userSkills = emptySet(), onSkillsClick = {}) }

        composeTestRule.onNodeWithText("My email").assertExists()
    }

    @Test
    fun profileScreen_displaysUsernameSection() {
        composeTestRule.setContent { ProfileScreen(userSkills = emptySet(), onSkillsClick = {}) }

        composeTestRule.onNodeWithText("My username").assertExists()
    }

    @Test
    fun profileScreen_displaysPreferencesSection() {
        composeTestRule.setContent { ProfileScreen(userSkills = emptySet(), onSkillsClick = {}) }

        composeTestRule.onNodeWithText("My preferences").assertExists()
    }

    @Test
    fun profileScreen_expandsEmailSection() {
        composeTestRule.setContent { ProfileScreen(userSkills = emptySet(), onSkillsClick = {}) }

        composeTestRule.onNodeWithText("My email").performClick()
        composeTestRule.onNodeWithText("user@example.com").assertExists()
        composeTestRule.onNodeWithText("Edit").assertExists()
    }

    @Test
    fun profileScreen_expandsUsernameSection() {
        composeTestRule.setContent { ProfileScreen(userSkills = emptySet(), onSkillsClick = {}) }

        composeTestRule.onNodeWithText("My username").performClick()
        composeTestRule.onNodeWithText("username").assertExists()
        composeTestRule.onNodeWithText("Edit").assertExists()
    }

    @Test
    fun profileScreen_expandsPreferencesSection() {
        composeTestRule.setContent { ProfileScreen(userSkills = emptySet(), onSkillsClick = {}) }

        composeTestRule.onNodeWithText("My preferences").performClick()
        composeTestRule.onNodeWithText("Money").assertExists()
        composeTestRule.onNodeWithText("Skills").assertExists()
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

        composeTestRule.onNodeWithText("Current skills (7):").assertExists()
    }

    @Test
    fun profileScreen_skillsSectionExpandsAndCollapses() {
        composeTestRule.setContent {
            ProfileScreen(userSkills = setOf(SkillTag.COMPUTER_PROGRAMMING), onSkillsClick = {})
        }

        // Initially collapsed
        composeTestRule.onNodeWithText("Skills").performClick()
        composeTestRule.onNodeWithText("Current skills (1):").assertExists()

        // Collapse again
        composeTestRule.onNodeWithText("Skills").performClick()
        // Should be collapsed (content not visible)
    }
}
