package com.swent.skillswap.auth

import androidx.activity.ComponentActivity
import androidx.compose.ui.semantics.SemanticsPropertyKey
import androidx.compose.ui.semantics.SemanticsPropertyReceiver
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import com.swent.skillswap.model.tags.SkillTag
import com.swent.skillswap.ui.auth.AuthCreateAccountScreen
import com.swent.skillswap.ui.auth.CreateAccountTags
import com.swent.skillswap.ui.auth.CreateAccountViewModel
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

val TextColorArgbKey = SemanticsPropertyKey<Int>("TextColorArgb")
var SemanticsPropertyReceiver.textColorArgb by TextColorArgbKey

@RunWith(AndroidJUnit4::class)
class SignInCreateAccountScreenTest : TestCase() {

    @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()
    private lateinit var vm: CreateAccountViewModel

    @Before
    fun setUp() {
        vm = CreateAccountViewModel(false)
    }

    // --- Helpers ---

    /** Launches the screen. Call this at the start of every test. */
    private fun launchScreen() {
        composeTestRule.setContent { AuthCreateAccountScreen(vm = vm, googleAccount = false) }
    }

    /**
     * DIRECTLY adds a user to Firestore. This allows us to test "Email Taken" without running the
     * UI flow twice.
     */
    private fun seedUserInFirestore(email: String) {
        val user =
            hashMapOf(
                "email" to email,
                "username" to "ExistingUser",
                "skills" to listOf<String>() // empty list
            )
        // Blocks the test thread until the DB write is complete
        Tasks.await(Firebase.firestore.collection("users").add(user))
    }

    private fun pressNext() {
        composeTestRule.onNodeWithTag(CreateAccountTags.NEXT_BUTTON).performClick()
    }

    private fun goToEmailStep() {
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
        launchScreen()

        composeTestRule
            .onNodeWithTag(CreateAccountTags.USERNAME_FIELD)
            .performScrollTo()
            .assertIsDisplayed()

        goToEmailStep()
        composeTestRule
            .onNodeWithTag(CreateAccountTags.EMAIL_FIELD)
            .performScrollTo()
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithTag(CreateAccountTags.EMAIL_FIELD)
            .performScrollTo()
            .performTextInput("test@example.com")
        pressNext()

        composeTestRule
            .onNodeWithTag(CreateAccountTags.PASSWORD_FIELD)
            .performScrollTo()
            .assertIsDisplayed()
    }

    // --- Username ---

    @Test
    fun usernameIsEmpty_disablesNextButton() {
        launchScreen()

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
        launchScreen()
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
        launchScreen()
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

    // --- Passwords ---

    @Test
    fun passwordIsEmpty_disablesNextButton() {
        launchScreen()
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
        launchScreen()
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
        launchScreen()
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
        launchScreen()
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
        launchScreen()
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

    // --- Skills ---

    @Test
    fun skillsIsEmpty_disablesNextButton() {
        launchScreen()
        goToSkillsStep()

        composeTestRule.waitForIdle()
        verifyNextButtonIsNotEnabled()
    }

    @Test
    fun skills_canAddAndRemoveSkill_withChipToggle() {
        launchScreen()
        goToSkillsStep()
        val skillTag = SkillTag.MACHINE_DESIGN
        val skillName = skillTag.name

        val chipTag = CreateAccountTags.SKILL_CHIP_PREFIX + skillName

        composeTestRule.onNodeWithTag(chipTag).performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag(chipTag).performClick()
        assert(vm.uiState.value.skills.contains(skillTag))
        composeTestRule.onNodeWithTag(chipTag).performClick()
        assert(!vm.uiState.value.skills.contains(skillTag))
    }

    @Test
    fun allValid_happyPath_navigatesThroughWithoutShowingErrors() {
        launchScreen()

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

        // Skills
        val skillTag = SkillTag.MACHINE_DESIGN.name
        composeTestRule
            .onNodeWithTag(CreateAccountTags.SKILL_CHIP_PREFIX + skillTag)
            .performScrollTo()
            .performClick()
    }

    @Test
    fun email_alreadyTaken_slowCheck_disablesNext() {
        val uniqueEmail = "slow_${System.currentTimeMillis()}@skillswap.com"
        seedUserInFirestore(uniqueEmail)
        launchScreen()
        composeTestRule.onNodeWithTag(CreateAccountTags.USERNAME_FIELD).performTextInput("User2")
        pressNext()
        composeTestRule.onNodeWithTag(CreateAccountTags.EMAIL_FIELD).performTextInput(uniqueEmail)

        // 5. Wait for the async check (Debounce + Network)
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            try {
                composeTestRule.onNodeWithText("Email is already in use").assertIsDisplayed()
                true
            } catch (e: AssertionError) {
                false
            }
        }

        // 6. Verify Next button is disabled
        composeTestRule
            .onNodeWithTag(CreateAccountTags.NEXT_BUTTON)
            .assertIsDisplayed()
            .assertIsNotEnabled()
    }

    @Test
    fun email_alreadyTaken_fastClick_navigatesBack() {
        // 1. Generate unique email and SEED DB
        val uniqueEmail = "fast_${System.currentTimeMillis()}@skillswap.com"
        seedUserInFirestore(uniqueEmail)
        launchScreen()
        composeTestRule.onNodeWithTag(CreateAccountTags.USERNAME_FIELD).performTextInput("UserFast")
        pressNext()
        composeTestRule.onNodeWithTag(CreateAccountTags.EMAIL_FIELD).performTextInput(uniqueEmail)
        pressNext()
        composeTestRule
            .onNodeWithTag(CreateAccountTags.PASSWORD_FIELD)
            .performTextInput("Password123")
        composeTestRule
            .onNodeWithTag(CreateAccountTags.CONFIRM_PASSWORD_FIELD)
            .performTextInput("Password123")
        pressNext()
        pressNext()

        composeTestRule.waitUntil(timeoutMillis = 15000) {
            try {
                composeTestRule.onNodeWithText("My Email is ").assertIsDisplayed()
                true
            } catch (e: AssertionError) {
                false
            }
        }
        composeTestRule.onNodeWithText("Email is already in use").assertIsDisplayed()
        composeTestRule.waitUntil(timeoutMillis = 10000) {
            try {
                composeTestRule.onNodeWithTag(CreateAccountTags.NEXT_BUTTON).assertIsNotEnabled()
                true
            } catch (e: AssertionError) {
                false
            }
        }
    }
}
