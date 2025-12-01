/*
 * Written with help of copilot to complete all repetitive code, and set up the companion object
 */
package com.swent.skillswap.end2end

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.google.firebase.auth.FirebaseAuth
import com.swent.skillswap.MainActivity
import com.swent.skillswap.ui.auth.CreateAccountTags
import com.swent.skillswap.ui.auth.SignInTags
import com.swent.skillswap.ui.feed.FeedScreenTestTags
import com.swent.skillswap.ui.navigation.NavigationTestTags
import com.swent.skillswap.ui.user.ProfileTestTags
import com.swent.skillswap.utils.FirebaseEmulator
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import org.junit.AfterClass
import org.junit.Before
import org.junit.BeforeClass
import org.junit.FixMethodOrder
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters

/** End-to-end tests for Milestone 1 Tests complete user flows */
@RunWith(AndroidJUnit4::class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class End2EndM1 {

    lateinit var db: com.google.firebase.firestore.FirebaseFirestore
    lateinit var auth: FirebaseAuth

    /** Companion object to clear the Auth emulator after running all tests */
    companion object {
        private const val PROJECT_ID = "skillswap-93276"

        @BeforeClass
        @JvmStatic
        fun setupEmulator() {
            FirebaseEmulator.reinitialize()

            FirebaseEmulator.clearAuthEmulator()
            FirebaseEmulator.clearFirestoreEmulator()
        }

        @AfterClass
        @JvmStatic
        fun tearDownFirebase() {

            // Sign out before clearing
            try {
                FirebaseAuth.getInstance().signOut()
            } catch (_: Exception) {}

            // Clear emulators AFTER this test class finishes
            FirebaseEmulator.clearAuthEmulator()
            FirebaseEmulator.clearFirestoreEmulator()
        }
    }

    @Before
    fun setup() {
        db = FirebaseEmulator.firestore
    }

    @get:Rule
    val grantPermissionRule: GrantPermissionRule =
        GrantPermissionRule.grant(
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.POST_NOTIFICATIONS
        )

    @get:Rule val composeTestRule = createAndroidComposeRule<MainActivity>()

    @OptIn(InternalTestApi::class)
    @Test
    fun completeUserFlow0_CreateAnAccountAndNavigate() {
        /**
         * NOTE: During this test, assert displays are wrapped in waitUntil with very big timeout,
         * to compensate the CI emulator slowness. You may want to remove these waits when running
         * tests locally for debugging.
         */
        /** 1. Launch app and wait for setup */
        composeTestRule.waitUntil(timeoutMillis = 40_000) {
            try {
                composeTestRule.onNodeWithTag(SignInTags.LOGO).assertIsDisplayed()
                composeTestRule.onNodeWithTag(SignInTags.SIGN_IN_BUTTON).assertIsDisplayed()
                composeTestRule.onNodeWithTag(SignInTags.EMAIL_FIELD).assertIsDisplayed()
                composeTestRule.onNodeWithTag(SignInTags.PASSWORD_FIELD).assertIsDisplayed()
                true
            } catch (e: AssertionError) {
                false
            }
        }

        composeTestRule.onNodeWithTag(SignInTags.CREATE_ACCOUNT_TEXT).performScrollTo()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(SignInTags.CREATE_ACCOUNT_TEXT).assertIsDisplayed()
        composeTestRule.onNodeWithTag(SignInTags.CREATE_ACCOUNT_TEXT).performClick()
        composeTestRule.waitForIdle()

        /** 2. Navigate in Create account Screens */

        /* Username Screen */
        composeTestRule.waitUntil(timeoutMillis = 40_000) {
            try {
                composeTestRule.onNodeWithTag(CreateAccountTags.TITLE).assertIsDisplayed()
                composeTestRule.onNodeWithTag(CreateAccountTags.USERNAME_FIELD).assertIsDisplayed()
                composeTestRule.onNodeWithTag(CreateAccountTags.NEXT_BUTTON).assertIsDisplayed()
                true
            } catch (e: AssertionError) {
                false
            }
        }

        composeTestRule.onNodeWithTag(CreateAccountTags.USERNAME_FIELD).performTextInput("Bob")
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(CreateAccountTags.NEXT_BUTTON).performClick()
        composeTestRule.waitForIdle()

        /* Email Screen */
        composeTestRule.waitUntil(timeoutMillis = 40_000) {
            try {
                composeTestRule.onNodeWithTag(CreateAccountTags.TITLE).assertIsDisplayed()
                composeTestRule.onNodeWithTag(CreateAccountTags.EMAIL_FIELD).assertIsDisplayed()
                composeTestRule.onNodeWithTag(CreateAccountTags.NEXT_BUTTON).assertIsDisplayed()
                true
            } catch (e: AssertionError) {
                false
            }
        }

        composeTestRule
            .onNodeWithTag(CreateAccountTags.EMAIL_FIELD)
            .performTextInput("bob@mail.com")
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(CreateAccountTags.NEXT_BUTTON).performClick()
        composeTestRule.waitForIdle()

        /* Password Screen */
        composeTestRule.waitUntil(timeoutMillis = 40_000) {
            try {
                composeTestRule.onNodeWithTag(CreateAccountTags.TITLE).assertIsDisplayed()
                composeTestRule.onNodeWithTag(CreateAccountTags.PASSWORD_FIELD).assertIsDisplayed()
                composeTestRule
                    .onNodeWithTag(CreateAccountTags.CONFIRM_PASSWORD_FIELD)
                    .assertIsDisplayed()
                composeTestRule.onNodeWithTag(CreateAccountTags.NEXT_BUTTON).assertIsDisplayed()
                true
            } catch (e: AssertionError) {
                false
            }
        }

        composeTestRule
            .onNodeWithTag(CreateAccountTags.PASSWORD_FIELD)
            .performTextInput("Password123")
        composeTestRule.waitForIdle()
        composeTestRule
            .onNodeWithTag(CreateAccountTags.CONFIRM_PASSWORD_FIELD)
            .performTextInput("Password123")
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(CreateAccountTags.NEXT_BUTTON).performClick()
        composeTestRule.waitForIdle()

        /* Skills Screen */
        val skillTag = CreateAccountTags.SKILL_CHIP_PREFIX + "CALCULUS"
        composeTestRule.waitUntil(timeoutMillis = 40_000) {
            try {
                composeTestRule.onNodeWithTag(CreateAccountTags.TITLE).assertIsDisplayed()
                composeTestRule.onNodeWithTag(CreateAccountTags.SKILLS_FLOW).assertIsDisplayed()
                composeTestRule.onNodeWithTag(skillTag, useUnmergedTree = true).performScrollTo()
                composeTestRule.onNodeWithTag(skillTag, useUnmergedTree = true).assertIsDisplayed()
                composeTestRule.onNodeWithTag(CreateAccountTags.NEXT_BUTTON).assertIsDisplayed()
                true
            } catch (e: AssertionError) {
                false
            }
        }

        composeTestRule.onNodeWithTag(skillTag, useUnmergedTree = true).performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(CreateAccountTags.NEXT_BUTTON).performClick()
        composeTestRule.waitForIdle()
        /* End of Create Account Screens */

        /** Wait until firestore auth operation completes and Profile Screen is displayed */
        composeTestRule.waitUntil(timeoutMillis = 60_000) {
            try {
                composeTestRule.onNodeWithTag(ProfileTestTags.PROFILE_TITLE).performScrollTo()
                composeTestRule.onNodeWithTag(ProfileTestTags.PROFILE_TITLE).assertIsDisplayed()
                true
            } catch (e: AssertionError) {
                false
            }
        }
        composeTestRule.waitForIdle()

        /** 3. Profile Screen Navigation Verification */
        navigateThroughAllScreensAfterAuthentification()
    }

    @Test
    fun completeUserFlow1_AutoSignInAndNavigate() {
        /** The User is already sign in, hence the app should open directly on Profile Screen */
        composeTestRule.waitForIdle()

        /** Wait until the previous test is done a the app is set up again */
        composeTestRule.waitUntil(timeoutMillis = 40_000) {
            try {
                composeTestRule.onNodeWithTag(ProfileTestTags.PROFILE_TITLE).assertIsDisplayed()
                true
            } catch (e: AssertionError) {
                false
            }
        }
        composeTestRule.waitForIdle()

        /** Perform Navigation Verification */
        navigateThroughAllScreensAfterAuthentification()
    }

    /**
     * Navigate through all main screens after authentication. Starting from Profile Screen, then to
     * Feed Screen, and finally to the Chat Screen.
     */
    fun navigateThroughAllScreensAfterAuthentification() {
        val visibleComposableProfile =
            listOf(
                ProfileTestTags.PROFILE_TITLE,
                ProfileTestTags.PROFILE_PICTURE_BOX,
                ProfileTestTags.EDIT_PROFILE_BUTTON,
                ProfileTestTags.INFO_CARD,
                ProfileTestTags.EMAIL_VALUE,
                ProfileTestTags.USERNAME_VALUE,
                ProfileTestTags.PREFERENCE_SWITCH,
                ProfileTestTags.SKILLS_BUTTON,
                ProfileTestTags.LOGOUT_BUTTON
            )

        val visibleComposableBotBar =
            listOf(
                NavigationTestTags.BOTTOM_NAVIGATION_MENU,
                NavigationTestTags.PROFILE_TAB,
                NavigationTestTags.FEED_TAB,
                NavigationTestTags.CHAT_TAB
            )

        val visibleComposableChatScreen = emptyList<String>()

        composeTestRule.waitForIdle()

        /** Profile Screen checks */
        composeTestRule.waitUntil(timeoutMillis = 40_000) {
            try {
                for (testTag in visibleComposableProfile) {
                    composeTestRule.onNodeWithTag(testTag).performScrollTo()
                    composeTestRule.onNodeWithTag(testTag).assertIsDisplayed()
                }
                for (testTag in visibleComposableBotBar) {
                    composeTestRule.onNodeWithTag(testTag).assertIsDisplayed()
                }
                true
            } catch (e: AssertionError) {
                false
            }
        }

        /** Navigate to Feed Screen */
        composeTestRule.onNodeWithTag(NavigationTestTags.FEED_TAB).performClick()
        composeTestRule.waitForIdle()

        composeTestRule.waitUntil(timeoutMillis = 40_000) {
            try {
                composeTestRule.onNodeWithTag(FeedScreenTestTags.NO_OFFER_TEXT).assertIsDisplayed()
                for (testTag in visibleComposableBotBar) {
                    composeTestRule.onNodeWithTag(testTag).assertIsDisplayed()
                }
                true
            } catch (e: AssertionError) {
                false
            }
        }

        /** Navigate to Chat Screen */
        composeTestRule.onNodeWithTag(NavigationTestTags.CHAT_TAB).performClick()
        composeTestRule.waitForIdle()

        composeTestRule.waitUntil(timeoutMillis = 40_000) {
            try {
                for (testTag in visibleComposableChatScreen) {
                    composeTestRule.onNodeWithTag(testTag).assertIsDisplayed()
                }
                for (testTag in visibleComposableBotBar) {
                    composeTestRule.onNodeWithTag(testTag).assertIsDisplayed()
                }
                true
            } catch (e: AssertionError) {
                false
            }
        }
    }

    @Test
    fun finalTest_UserIsAuthentified() {
        composeTestRule.waitForIdle()

        /** Verify that the user is correctly authenticated */
        val currentUser = FirebaseAuth.getInstance().currentUser
        assertNotNull(currentUser)
        assertEquals("bob@mail.com", currentUser!!.email)
    }
}
