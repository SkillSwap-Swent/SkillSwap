/*
 * Written with help of copilot to complete all repetitive code, and set up the companion object
 */
package com.swent.skillswap.end2end

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.auth.FirebaseAuth
import com.swent.skillswap.MainActivity
import com.swent.skillswap.ui.auth.CreateAccountTags
import com.swent.skillswap.ui.auth.SignInTags
import com.swent.skillswap.ui.feedScreen.FeedScreenTestTags
import com.swent.skillswap.ui.navigation.NavigationTestTags
import com.swent.skillswap.ui.user.ProfileTestTags
import com.swent.skillswap.utils.FirebaseEmulator
import java.net.HttpURLConnection
import java.net.URL
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
@FixMethodOrder(MethodSorters.NAME_ASCENDING) // Be careful, tests order matters !
class End2EndM1 {

    lateinit var db: com.google.firebase.firestore.FirebaseFirestore
    lateinit var auth: FirebaseAuth

    /** Companion object to clear the Auth emulator after running all tests */
    companion object {
        private const val EMULATOR_URL = "http://10.0.2.2:9099"
        private const val PROJECT_ID = "skillswap-93276"

        @BeforeClass
        @JvmStatic
        fun setupEmulator() {
            FirebaseEmulator.startEmulator()
        }

        @AfterClass
        @JvmStatic
        fun cleanupAuthEmulator() {
            val url = URL("$EMULATOR_URL/emulator/v1/projects/$PROJECT_ID/accounts")
            val maxAttempts = 20
            var attempt = 0
            var cleared = false

            while (attempt < maxAttempts && !cleared) {
                try {
                    with(url.openConnection() as HttpURLConnection) {
                        connectTimeout = 2000
                        readTimeout = 2000
                        requestMethod = "DELETE"
                        val responseCode = responseCode
                        if (responseCode == HttpURLConnection.HTTP_OK) {
                            cleared = true
                            println("Firebase Auth emulator cleared successfully")
                        }
                        disconnect()
                    }
                } catch (e: Exception) {
                    attempt++
                    Thread.sleep(1000)
                }
            }

            if (!cleared) {
                println("Warning: Failed to clear Auth emulator after $maxAttempts attempts")
            }
        }
    }

    @Before
    fun setup() {
        FirebaseEmulator.startEmulator()
        db = FirebaseEmulator.firestore
    }

    @get:Rule val composeTestRule = createAndroidComposeRule<MainActivity>()

    @OptIn(InternalTestApi::class)
    @Test
    fun completeUserFlow0_CreateAnAccountAndNavigate() {
        /** 1. Launch app and wait for setup */
        composeTestRule.waitUntil(timeoutMillis = 15_000) {
            try {
                val signInTags =
                    listOf(
                        SignInTags.LOGO,
                        SignInTags.SIGN_IN_BUTTON,
                        SignInTags.GOOGLE_BUTTON,
                        SignInTags.EMAIL_FIELD,
                        SignInTags.PASSWORD_FIELD
                    )

                for (testTag in signInTags) {
                    composeTestRule.onNodeWithTag(testTag).assertIsDisplayed()
                }
                true
            } catch (e: AssertionError) {
                false
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag(SignInTags.CREATE_ACCOUNT_TEXT).performScrollTo()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(SignInTags.CREATE_ACCOUNT_TEXT).assertIsDisplayed()
        composeTestRule.onNodeWithTag(SignInTags.CREATE_ACCOUNT_TEXT).performClick()
        composeTestRule.waitForIdle()

        /** 2. Navigate in Create account Screens */

        /* Username Screen */
        composeTestRule.waitUntil(timeoutMillis = 15_001) {
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
        composeTestRule.waitUntil(timeoutMillis = 15_002) {
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
        composeTestRule.waitUntil(timeoutMillis = 15_003) {
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
        composeTestRule.waitUntil(timeoutMillis = 15_004) {
            try {
                composeTestRule.onNodeWithTag(CreateAccountTags.TITLE).assertIsDisplayed()
                composeTestRule.onNodeWithTag(CreateAccountTags.SKILLS_FLOW).assertIsDisplayed()
                composeTestRule.onNodeWithTag(skillTag).assertIsDisplayed()
                composeTestRule.onNodeWithTag(CreateAccountTags.NEXT_BUTTON).assertIsDisplayed()
                true
            } catch (e: AssertionError) {
                false
            }
        }

        composeTestRule.onNodeWithTag(skillTag).performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(CreateAccountTags.NEXT_BUTTON).performClick()
        composeTestRule.waitForIdle()
        /* End of Create Account Screens */

        /** Wait until firestore auth operation completes and Profile Screen is displayed */
        composeTestRule.waitUntil(timeoutMillis = 30_000) {
            composeTestRule
                .onAllNodesWithTag(ProfileTestTags.PROFILE_TITLE)
                .fetchSemanticsNodes()
                .isNotEmpty()
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
        composeTestRule.waitUntil(timeoutMillis = 30_000) {
            composeTestRule
                .onAllNodesWithTag(ProfileTestTags.PROFILE_TITLE)
                .fetchSemanticsNodes()
                .isNotEmpty()
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
                ProfileTestTags.EDIT_PROFILE,
                ProfileTestTags.PROFILE_TITLE,
                ProfileTestTags.EMAIL_SECTION,
                ProfileTestTags.USERNAME_SECTION,
                ProfileTestTags.SKILLS_SECTION,
                ProfileTestTags.PREFERENCES_SECTION
            )

        val visibleComposableBotBar =
            listOf(
                NavigationTestTags.BOTTOM_NAVIGATION_MENU,
                NavigationTestTags.PROFILE_TAB,
                NavigationTestTags.FEED_TAB,
                NavigationTestTags.CHAT_TAB
            )

        val visibleComposableFeedScreen =
            listOf(
                FeedScreenTestTags.FEED_CARD,
                FeedScreenTestTags.FEED_THUMBNAIL,
                FeedScreenTestTags.SKILL_REQUESTED,
                FeedScreenTestTags.SKILL_GIVE,
                FeedScreenTestTags.ACCEPT_BUTTON,
                FeedScreenTestTags.DECLINE_BUTTON,
            )

        val visibleComposableChatScreen = emptyList<String>() // No tests tags defined yet

        composeTestRule.waitForIdle()

        /** Profile Screen checks */
        composeTestRule.waitUntil(timeoutMillis = 15_010) {
            try {
                for (testTag in visibleComposableProfile) {
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

        composeTestRule.waitUntil(timeoutMillis = 15_020) {
            try {
                for (testTag in visibleComposableFeedScreen) {
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

        /** Navigate to Chat Screen */
        composeTestRule.onNodeWithTag(NavigationTestTags.CHAT_TAB).performClick()
        composeTestRule.waitForIdle()

        composeTestRule.waitUntil(timeoutMillis = 15_030) {
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
