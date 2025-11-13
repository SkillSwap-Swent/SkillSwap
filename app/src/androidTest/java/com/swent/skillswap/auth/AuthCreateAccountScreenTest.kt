package com.swent.skillswap.auth

import androidx.activity.ComponentActivity
import androidx.compose.ui.semantics.SemanticsPropertyKey
import androidx.compose.ui.semantics.SemanticsPropertyReceiver
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import com.swent.skillswap.model.tags.SkillTag
import com.swent.skillswap.ui.auth.AuthCreateAccountScreen
import com.swent.skillswap.ui.auth.CreateAccountTags
import com.swent.skillswap.viewModel.CreateAccountViewModel
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

val TextColorArgbKey = SemanticsPropertyKey<Int>("TextColorArgb")
var SemanticsPropertyReceiver.textColorArgb by TextColorArgbKey

@RunWith(AndroidJUnit4::class)
class SignInCreateAccountScreenTest : TestCase() {

    @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()
    val vm: CreateAccountViewModel = CreateAccountViewModel(false)

    @Before
    fun setUp() {
        composeTestRule.setContent { AuthCreateAccountScreen(vm = vm, googleAccount = false) }
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

    private fun verifyNextButtonIsNotEnabled() {
        composeTestRule
            .onNodeWithTag(CreateAccountTags.NEXT_BUTTON)
            .assertIsDisplayed()
            .assertIsNotEnabled()
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
    fun usernameIsEmpty_disablesNextButton() {
        // At Username step by default
        composeTestRule
            .onNodeWithTag(CreateAccountTags.USERNAME_FIELD)
            .performScrollTo()
            .performTextClearance()

        composeTestRule.waitForIdle()
        verifyNextButtonIsNotEnabled()
    }

    // --- Email ---

    @Test
    fun emailIsEmpty_disablesNextButton() {
        goToEmailStep()

        composeTestRule
            .onNodeWithTag(CreateAccountTags.EMAIL_FIELD)
            .performScrollTo()
            .performTextClearance()

        composeTestRule.waitForIdle()
        verifyNextButtonIsNotEnabled()
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
    fun passwordIsEmpty_disablesNextButton() {
        goToPasswordStep()

        composeTestRule
            .onNodeWithTag(CreateAccountTags.PASSWORD_FIELD)
            .performScrollTo()
            .performTextClearance()

        composeTestRule.waitForIdle()
        verifyNextButtonIsNotEnabled()
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
    fun skillsIsEmpty_disablesNextButton() {
        goToSkillsStep()

        composeTestRule.waitForIdle()
        verifyNextButtonIsNotEnabled()
    }

    @Test
    fun skills_canAddAndRemoveSkill_withChipToggle() {
        goToSkillsStep()
        val skillTag = SkillTag.MACHINE_DESIGN
        val skillName = skillTag.name

        val chipTag = CreateAccountTags.SKILL_CHIP_PREFIX + skillName

        // Scroll to and ensure displayed
        composeTestRule.onNodeWithTag(chipTag).performScrollTo().assertIsDisplayed()

        // Select (click)
        composeTestRule.onNodeWithTag(chipTag).performClick()
        assert(vm.uiState.value.skills.contains(skillTag))

        // Deselect (click again)
        composeTestRule.onNodeWithTag(chipTag).performClick()
        assert(!vm.uiState.value.skills.contains(skillTag))
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
    }
}
