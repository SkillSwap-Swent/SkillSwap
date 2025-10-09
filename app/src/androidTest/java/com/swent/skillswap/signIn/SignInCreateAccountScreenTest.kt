/** @author Topaze17(Eliott) */
package com.swent.skillswap.signIn

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import com.swent.skillswap.model.tags.SkillTag
import com.swent.skillswap.ui.signIn.CreateAccountTags
import com.swent.skillswap.ui.signIn.SignInCreateAccountScreen
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SignInCreateAccountScreenTest : TestCase() {

    @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Before
    fun setUp() {
        composeTestRule.setContent { SignInCreateAccountScreen() }
    }

    @Test
    fun testEverythingIsDisplay() {
        composeTestRule.onNodeWithTag(CreateAccountTags.TITLE).performScrollTo()
        composeTestRule.onNodeWithTag(CreateAccountTags.TITLE).assertIsDisplayed()
        composeTestRule.onNodeWithTag(CreateAccountTags.DONE_BUTTON).performScrollTo()
        composeTestRule.onNodeWithTag(CreateAccountTags.DONE_BUTTON).assertIsDisplayed()
        composeTestRule.onNodeWithTag(CreateAccountTags.EMAIL_FIELD).performScrollTo()
        composeTestRule.onNodeWithTag(CreateAccountTags.EMAIL_FIELD).assertIsDisplayed()
        composeTestRule.onNodeWithTag(CreateAccountTags.SKILLS_FLOW).assertExists()
        composeTestRule.onNodeWithTag(CreateAccountTags.PASSWORD_FIELD).performScrollTo()
        composeTestRule.onNodeWithTag(CreateAccountTags.PASSWORD_FIELD).assertIsDisplayed()
        composeTestRule.onNodeWithTag(CreateAccountTags.CONFIRM_PASSWORD_FIELD).performScrollTo()
        composeTestRule.onNodeWithTag(CreateAccountTags.CONFIRM_PASSWORD_FIELD).assertIsDisplayed()
        composeTestRule.onNodeWithTag(CreateAccountTags.USERNAME_FIELD).performScrollTo()
        composeTestRule.onNodeWithTag(CreateAccountTags.USERNAME_FIELD).assertIsDisplayed()
        composeTestRule.onNodeWithTag(CreateAccountTags.SKILLS_INPUT).performScrollTo()
        composeTestRule.onNodeWithTag(CreateAccountTags.SKILLS_INPUT).assertIsDisplayed()
    }

    @Test
    fun testInputSkillsCanInputSkill() {
        val skillTag = SkillTag.MACHINE_DESIGN.name
        composeTestRule.onNodeWithTag(CreateAccountTags.SKILLS_INPUT).performScrollTo()
        composeTestRule.onNodeWithTag(CreateAccountTags.SKILLS_INPUT).performClick()
        composeTestRule.onNodeWithTag(CreateAccountTags.SKILLS_INPUT).performTextInput(skillTag)
        composeTestRule.waitUntil(5_000) {
            composeTestRule
                .onNodeWithTag(CreateAccountTags.SKILL_SUGGESTION_PREFIX + skillTag)
                .isDisplayed()
        }
        composeTestRule
            .onNodeWithTag(CreateAccountTags.SKILL_SUGGESTION_PREFIX + skillTag)
            .performClick()
        composeTestRule.onNodeWithTag(CreateAccountTags.SKILLS_FLOW).performScrollTo()
        composeTestRule.onNodeWithTag(CreateAccountTags.SKILLS_FLOW).assertIsDisplayed()
        composeTestRule
            .onNodeWithTag(CreateAccountTags.SKILL_CHIP_PREFIX + skillTag)
            .performScrollTo()
        composeTestRule
            .onNodeWithTag(CreateAccountTags.SKILL_CHIP_PREFIX + skillTag)
            .assertIsDisplayed()
    }

    @Test
    fun testCanRemoveSkill() {
        val skillTag = SkillTag.MACHINE_DESIGN.name
        composeTestRule.onNodeWithTag(CreateAccountTags.SKILLS_INPUT).performScrollTo()
        composeTestRule.onNodeWithTag(CreateAccountTags.SKILLS_INPUT).performClick()
        composeTestRule.onNodeWithTag(CreateAccountTags.SKILLS_INPUT).performTextInput(skillTag)
        composeTestRule.waitUntil(5_000) {
            composeTestRule
                .onNodeWithTag(CreateAccountTags.SKILL_SUGGESTION_PREFIX + skillTag)
                .isDisplayed()
        }
        composeTestRule
            .onNodeWithTag(CreateAccountTags.SKILL_SUGGESTION_PREFIX + skillTag)
            .performClick()
        composeTestRule.onNodeWithTag(CreateAccountTags.SKILLS_FLOW).performScrollTo()
        composeTestRule.onNodeWithTag(CreateAccountTags.SKILLS_FLOW).assertIsDisplayed()
        composeTestRule
            .onNodeWithTag(CreateAccountTags.SKILL_CHIP_PREFIX + skillTag)
            .performScrollTo()
        composeTestRule
            .onNodeWithTag(CreateAccountTags.SKILL_CHIP_PREFIX + skillTag)
            .assertIsDisplayed()
        composeTestRule.onNodeWithTag(CreateAccountTags.SKILL_CHIP_PREFIX + skillTag).performClick()
        composeTestRule
            .onNodeWithTag(CreateAccountTags.SKILL_CHIP_PREFIX + skillTag)
            .assertDoesNotExist()
    }
}
