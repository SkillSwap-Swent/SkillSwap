// AI-Generated: Comprehensive test suite for profile screen components
/** @author Topaze17(Eliott) big help from chatGPT to made all the annoying textfield test */
package com.swent.skillswap.signIn

import androidx.activity.ComponentActivity
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import com.swent.skillswap.model.tags.SkillTag
import com.swent.skillswap.ui.signIn.CreateAccountTags
import com.swent.skillswap.ui.signIn.SignInCreateAccountScreen
import junit.framework.TestCase.assertTrue
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
    /** Triggers validation by pressing the done button */
    private fun triggerValidation() {
        composeTestRule
            .onNodeWithTag(CreateAccountTags.DONE_BUTTON)
            .performScrollTo()
            .performClick()
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

    @Test
    fun username_showsError_whenEmpty() {
        composeTestRule
            .onNodeWithTag(CreateAccountTags.USERNAME_FIELD)
            .performScrollTo()
            .performTextClearance()

        triggerValidation()

        composeTestRule
            .onNodeWithTag(CreateAccountTags.USERNAME_FIELD)
            .performScrollTo()
            .assertIsDisplayed()
            .assertTextContains("Username cannot be empty")
    }

    // ---------- Email ----------
    @Test
    fun email_showsError_whenEmpty() {
        composeTestRule
            .onNodeWithTag(CreateAccountTags.EMAIL_FIELD)
            .performScrollTo()
            .performTextClearance()

        triggerValidation()

        composeTestRule
            .onNodeWithTag(CreateAccountTags.EMAIL_FIELD)
            .performScrollTo()
            .assertIsDisplayed()
            .assertTextContains("Email cannot be empty")
    }

    @Test
    fun email_showsError_onInvalidFormat() {
        composeTestRule
            .onNodeWithTag(CreateAccountTags.EMAIL_FIELD)
            .performScrollTo()
            .performTextInput("invalidEmail")

        triggerValidation()

        composeTestRule
            .onNodeWithTag(CreateAccountTags.EMAIL_FIELD)
            .performScrollTo()
            .assertIsDisplayed()
            .assertTextContains("Invalid email format")
    }

    // ---------- Password ----------
    @Test
    fun password_showsError_whenEmpty() {
        composeTestRule
            .onNodeWithTag(CreateAccountTags.PASSWORD_FIELD)
            .performScrollTo()
            .performTextClearance()

        triggerValidation()

        composeTestRule
            .onNodeWithTag(CreateAccountTags.PASSWORD_FIELD)
            .performScrollTo()
            .assertIsDisplayed()
            .assertTextContains("Password cannot be empty")
    }

    @Test
    fun password_showsError_whenTooShort() {
        composeTestRule
            .onNodeWithTag(CreateAccountTags.PASSWORD_FIELD)
            .performScrollTo()
            .performTextInput("short")

        triggerValidation()

        composeTestRule
            .onNodeWithTag(CreateAccountTags.PASSWORD_FIELD)
            .performScrollTo()
            .assertIsDisplayed()
            .assertTextContains("Password must be at least 8 characters long")
    }

    @Test
    fun password_showsError_whenNoUppercase() {
        composeTestRule
            .onNodeWithTag(CreateAccountTags.PASSWORD_FIELD)
            .performScrollTo()
            .performTextInput("lowercasepassword")

        triggerValidation()

        composeTestRule
            .onNodeWithTag(CreateAccountTags.PASSWORD_FIELD)
            .performScrollTo()
            .assertIsDisplayed()
            .assertTextContains("Password must contain at least one uppercase letter")
    }

    // ---------- Confirm Password ----------
    @Test
    fun confirmPassword_showsError_whenEmpty() {
        composeTestRule
            .onNodeWithTag(CreateAccountTags.PASSWORD_FIELD)
            .performScrollTo()
            .performTextInput("PasswordA")

        composeTestRule
            .onNodeWithTag(CreateAccountTags.CONFIRM_PASSWORD_FIELD)
            .performScrollTo()
            .performTextClearance()

        triggerValidation()

        composeTestRule
            .onNodeWithTag(CreateAccountTags.CONFIRM_PASSWORD_FIELD)
            .performScrollTo()
            .assertIsDisplayed()
            .assertTextContains("Please confirm your password")
    }

    @Test
    fun confirmPassword_showsError_whenMismatch() {
        composeTestRule
            .onNodeWithTag(CreateAccountTags.PASSWORD_FIELD)
            .performScrollTo()
            .performTextInput("PasswordA")

        composeTestRule
            .onNodeWithTag(CreateAccountTags.CONFIRM_PASSWORD_FIELD)
            .performScrollTo()
            .performTextInput("PasswordB")

        triggerValidation()

        composeTestRule
            .onNodeWithTag(CreateAccountTags.CONFIRM_PASSWORD_FIELD)
            .performScrollTo()
            .assertIsDisplayed()
            .assertTextContains("Passwords do not match")
    }

    // ---------- Skills ----------
    @Test
    fun skills_showsError_whenEmpty() {
        // Don’t pick any skill
        triggerValidation()

        // The skills error text is merged into the Skills input TextField
        composeTestRule
            .onNodeWithTag(CreateAccountTags.SKILLS_INPUT)
            .performScrollTo()
            .assertIsDisplayed()
            .assertTextContains("At least one skill must be selected")
    }

    // ---------- All valid ----------
    @Test
    fun allValid_noErrorTextOnFields() {
        // Fill all valid fields
        composeTestRule
            .onNodeWithTag(CreateAccountTags.USERNAME_FIELD)
            .performScrollTo()
            .performTextInput("Eliott")

        composeTestRule
            .onNodeWithTag(CreateAccountTags.EMAIL_FIELD)
            .performScrollTo()
            .performTextInput("test@example.com")

        composeTestRule
            .onNodeWithTag(CreateAccountTags.PASSWORD_FIELD)
            .performScrollTo()
            .performTextInput("PasswordA")

        composeTestRule
            .onNodeWithTag(CreateAccountTags.CONFIRM_PASSWORD_FIELD)
            .performScrollTo()
            .performTextInput("PasswordA")

        // Add one skill
        val skillTag = SkillTag.MACHINE_DESIGN.name
        composeTestRule
            .onNodeWithTag(CreateAccountTags.SKILLS_INPUT)
            .performScrollTo()
            .performClick()
            .performTextInput(skillTag)

        composeTestRule.waitUntil(5_000) {
            // suggestion appears
            composeTestRule
                .onAllNodesWithTag(CreateAccountTags.SKILL_SUGGESTION_PREFIX + skillTag)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeTestRule
            .onNodeWithTag(CreateAccountTags.SKILL_SUGGESTION_PREFIX + skillTag)
            .performClick()

        triggerValidation()

        // Assert fields do NOT contain error texts
        composeTestRule.onAllNodesWithTag(CreateAccountTags.ERROR).fetchSemanticsNodes().forEach {
            node ->
            val text = node.config.getOrNull(androidx.compose.ui.semantics.SemanticsProperties.Text)
            assertTrue("Expected no error text, but got: $text", text.isNullOrEmpty())
        }
    }
}
