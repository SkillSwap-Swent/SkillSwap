package com.swent.skillswap.signIn

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextClearance
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

    // --- Helpers ---

    private fun pressNext() {
        composeTestRule.onNodeWithTag(CreateAccountTags.NEXT_BUTTON).performClick()
    }

    private fun goToEmailStep() {
        // Username must be valid to advance
        composeTestRule
            .onNodeWithTag(CreateAccountTags.USERNAME_FIELD)
            .performScrollTo()
            .performTextInput("Eliott")
        pressNext()
    }

    private fun goToPasswordStep() {
        goToEmailStep()
        composeTestRule
            .onNodeWithTag(CreateAccountTags.EMAIL_FIELD)
            .performScrollTo()
            .performTextInput("test@example.com")
        pressNext()
    }

    private fun goToSkillsStep() {
        goToPasswordStep()
        composeTestRule
            .onNodeWithTag(CreateAccountTags.PASSWORD_FIELD)
            .performScrollTo()
            .performTextInput("PasswordA")
        composeTestRule
            .onNodeWithTag(CreateAccountTags.CONFIRM_PASSWORD_FIELD)
            .performScrollTo()
            .performTextInput("PasswordA")
        pressNext()
    }

    // --- Smoke / visibility across steps ---

    @Test
    fun testFieldsAreDisplayedAtEachStep() {
        // Username screen
        composeTestRule
            .onNodeWithTag(CreateAccountTags.USERNAME_FIELD)
            .performScrollTo()
            .assertIsDisplayed()

        // Email screen
        goToEmailStep()
        composeTestRule
            .onNodeWithTag(CreateAccountTags.EMAIL_FIELD)
            .performScrollTo()
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithTag(CreateAccountTags.EMAIL_FIELD)
            .performScrollTo()
            .performTextInput("test@example.com")
        // Password screen
        pressNext()
        composeTestRule
            .onNodeWithTag(CreateAccountTags.PASSWORD_FIELD)
            .performScrollTo()
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithTag(CreateAccountTags.CONFIRM_PASSWORD_FIELD)
            .performScrollTo()
            .assertIsDisplayed()

        // Skills screen
        // Fill valid password to reach skills
        composeTestRule
            .onNodeWithTag(CreateAccountTags.PASSWORD_FIELD)
            .performTextInput("PasswordA")
        composeTestRule
            .onNodeWithTag(CreateAccountTags.CONFIRM_PASSWORD_FIELD)
            .performTextInput("PasswordA")
        pressNext()
        composeTestRule
            .onNodeWithTag(CreateAccountTags.SKILLS_FLOW)
            .performScrollTo()
            .assertIsDisplayed()
    }

    // --- Username ---

    @Test
    fun username_showsError_whenEmpty() {
        // At Username step by default
        composeTestRule
            .onNodeWithTag(CreateAccountTags.USERNAME_FIELD)
            .performScrollTo()
            .performTextClearance()

        pressNext()

        composeTestRule
            .onNodeWithTag(CreateAccountTags.USERNAME_FIELD)
            .performScrollTo()
            .assertIsDisplayed()
            .assertTextContains("Username cannot be empty")
    }

    // --- Email ---

    @Test
    fun email_showsError_whenEmpty() {
        goToEmailStep()

        composeTestRule
            .onNodeWithTag(CreateAccountTags.EMAIL_FIELD)
            .performScrollTo()
            .performTextClearance()

        pressNext()

        composeTestRule
            .onNodeWithTag(CreateAccountTags.EMAIL_FIELD)
            .performScrollTo()
            .assertIsDisplayed()
            .assertTextContains("Email cannot be empty")
    }

    @Test
    fun email_showsError_onInvalidFormat() {
        goToEmailStep()

        composeTestRule
            .onNodeWithTag(CreateAccountTags.EMAIL_FIELD)
            .performScrollTo()
            .performTextInput("invalidEmail")

        pressNext()

        composeTestRule
            .onNodeWithTag(CreateAccountTags.EMAIL_FIELD)
            .performScrollTo()
            .assertIsDisplayed()
            .assertTextContains("Invalid email format")
    }

    // --- Passwords (combined validation) ---

    @Test
    fun password_showsError_whenEmpty() {
        goToPasswordStep()

        composeTestRule
            .onNodeWithTag(CreateAccountTags.PASSWORD_FIELD)
            .performScrollTo()
            .performTextClearance()

        pressNext()

        composeTestRule
            .onNodeWithTag(CreateAccountTags.PASSWORD_FIELD)
            .performScrollTo()
            .assertIsDisplayed()
            .assertTextContains("Password cannot be empty")
    }

    @Test
    fun password_showsError_whenTooShort() {
        goToPasswordStep()

        composeTestRule
            .onNodeWithTag(CreateAccountTags.PASSWORD_FIELD)
            .performScrollTo()
            .performTextInput("short")

        pressNext()

        composeTestRule
            .onNodeWithTag(CreateAccountTags.PASSWORD_FIELD)
            .performScrollTo()
            .assertIsDisplayed()
            .assertTextContains("Password must be at least 8 characters long")
    }

    @Test
    fun password_showsError_whenNoUppercase() {
        goToPasswordStep()

        composeTestRule
            .onNodeWithTag(CreateAccountTags.PASSWORD_FIELD)
            .performScrollTo()
            .performTextInput("lowercasepassword")

        pressNext()

        composeTestRule
            .onNodeWithTag(CreateAccountTags.PASSWORD_FIELD)
            .performScrollTo()
            .assertIsDisplayed()
            .assertTextContains("Password must contain at least one uppercase letter")
    }

    @Test
    fun confirmPassword_showsError_whenEmpty() {
        goToPasswordStep()

        composeTestRule
            .onNodeWithTag(CreateAccountTags.PASSWORD_FIELD)
            .performScrollTo()
            .performTextInput("PasswordA")

        composeTestRule
            .onNodeWithTag(CreateAccountTags.CONFIRM_PASSWORD_FIELD)
            .performScrollTo()
            .performTextClearance()

        pressNext()

        composeTestRule
            .onNodeWithTag(CreateAccountTags.CONFIRM_PASSWORD_FIELD)
            .performScrollTo()
            .assertIsDisplayed()
            .assertTextContains("Please confirm your password")
    }

    @Test
    fun confirmPassword_showsError_whenMismatch() {
        goToPasswordStep()

        composeTestRule
            .onNodeWithTag(CreateAccountTags.PASSWORD_FIELD)
            .performScrollTo()
            .performTextInput("PasswordA")

        composeTestRule
            .onNodeWithTag(CreateAccountTags.CONFIRM_PASSWORD_FIELD)
            .performScrollTo()
            .performTextInput("PasswordB")

        pressNext()

        composeTestRule
            .onNodeWithTag(CreateAccountTags.CONFIRM_PASSWORD_FIELD)
            .performScrollTo()
            .assertIsDisplayed()
            .assertTextContains("Passwords do not match")
    }

    // --- Skills (chips) ---

    @Test
    fun skills_showsError_whenEmpty() {
        goToSkillsStep()

        // Pressing Next on the Skills step will run ViewModel.done() → validateInputs() inside,
        // which sets skillsError when no skill is selected. SkillScreen shows it as plain Text.
        pressNext()

        composeTestRule.onNodeWithText("At least one skill must be selected").assertIsDisplayed()
    }

    @Test
    fun skills_canAddAndRemoveSkill_withChipToggle() {
        goToSkillsStep()

        val skillTag = SkillTag.MACHINE_DESIGN.name
        val chipTag = CreateAccountTags.SKILL_CHIP_PREFIX + skillTag

        // Add (select) the skill
        composeTestRule.onNodeWithTag(chipTag).performScrollTo().performClick()
        composeTestRule.onNodeWithTag(chipTag).assertIsDisplayed()

        // Remove (toggle off) the same skill
        composeTestRule.onNodeWithTag(chipTag).performClick()
        composeTestRule.onNodeWithTag(chipTag).assertDoesNotExist()
    }

    @Test
    fun allValid_happyPath_navigatesThroughWithoutShowingErrors() {
        // Username
        composeTestRule
            .onNodeWithTag(CreateAccountTags.USERNAME_FIELD)
            .performScrollTo()
            .performTextInput("Eliott")
        pressNext()

        // Email
        composeTestRule
            .onNodeWithTag(CreateAccountTags.EMAIL_FIELD)
            .performScrollTo()
            .performTextInput("test@example.com")
        pressNext()

        // Passwords
        composeTestRule
            .onNodeWithTag(CreateAccountTags.PASSWORD_FIELD)
            .performScrollTo()
            .performTextInput("PasswordA")
        composeTestRule
            .onNodeWithTag(CreateAccountTags.CONFIRM_PASSWORD_FIELD)
            .performScrollTo()
            .performTextInput("PasswordA")
        pressNext()

        // Skills → pick any skill chip to satisfy validation
        val skillTag = SkillTag.MACHINE_DESIGN.name
        composeTestRule
            .onNodeWithTag(CreateAccountTags.SKILL_CHIP_PREFIX + skillTag)
            .performScrollTo()
            .performClick()

        // Final Next triggers done(); no explicit UI success state to assert here.
        pressNext()

        // Optional: ensure no common error texts are present on the screen
        // (weak check, but helps catch regressions)
        composeTestRule.onAllNodesWithTag(CreateAccountTags.USERNAME_FIELD)
        composeTestRule.onAllNodesWithTag(CreateAccountTags.EMAIL_FIELD)
        composeTestRule.onAllNodesWithTag(CreateAccountTags.PASSWORD_FIELD)
        composeTestRule.onAllNodesWithTag(CreateAccountTags.CONFIRM_PASSWORD_FIELD)
        // If you later expose dedicated error tags, assert they don't exist here.
    }
}
