/*
 * Written with help of copilot to complete all repetitive code, and set up the companion object
 */
package com.swent.skillswap.end2end


import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.auth.FirebaseAuth
import com.swent.skillswap.MainActivity
import com.swent.skillswap.utils.FirebaseEmulator
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Before
import org.junit.BeforeClass
import org.junit.FixMethodOrder
import org.junit.runners.MethodSorters
import java.net.HttpURLConnection
import java.net.URL
import org.junit.Assert.assertEquals

/** Test Tags */
import com.swent.skillswap.ui.signIn.SignInTags
import com.swent.skillswap.ui.signIn.CreateAccountTags
import com.swent.skillswap.ui.navigation.bottomBar.BottomBarTestTag
import com.swent.skillswap.ui.user.ProfileTestTags
import com.swent.skillswap.ui.offerScreen.OfferScreenTestTags
import junit.framework.TestCase.assertNotNull


/**
 * End-to-end tests for Milestone 1
 * Tests complete user flows from authentication to profile editing
 */
@RunWith(AndroidJUnit4::class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class End2EndM1 {

    lateinit var db: com.google.firebase.firestore.FirebaseFirestore
    lateinit var auth : FirebaseAuth

    /**
     * Companion object to clear the Auth emulator before running tests
     */
    companion object {
        private const val EMULATOR_URL = "http://10.0.2.2:9099"
        private const val PROJECT_ID = "skillswap-93276"
        @BeforeClass
        @JvmStatic
        fun clearAuthEmulator() {
            val url = URL("$EMULATOR_URL/emulator/v1/projects/$PROJECT_ID/accounts")
            with(url.openConnection() as HttpURLConnection) {
                requestMethod = "DELETE"
                val responseCode = responseCode
                if (responseCode != 200) {throw Exception("Failed to clear Auth emulator: $responseCode")}
                disconnect()
            }
        }
    }

    @Before
    fun setup() {
        FirebaseEmulator.startEmulator()
        db = FirebaseEmulator.firestore
    }

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @OptIn(InternalTestApi::class)
    @Test
    fun completeUserFlow0_CreateAnAccountAndNavigate() {
        /** 1. Launch app and verify sign in screen */
        composeTestRule.waitForIdle()

        /** Verify Sign-In screen is displayed */
        val signInTags = listOf(
            SignInTags.LOGO,
            SignInTags.SIGN_IN_BUTTON,
            SignInTags.GOOGLE_BUTTON,
            SignInTags.EMAIL_FIELD,
            SignInTags.PASSWORD_FIELD
        )

        for (testTag in signInTags) {
            composeTestRule.onNodeWithTag(testTag).assertIsDisplayed()
        }

        composeTestRule.onNodeWithTag(SignInTags.CREATE_ACCOUNT_TEXT).performScrollTo()
        composeTestRule.onNodeWithTag(SignInTags.CREATE_ACCOUNT_TEXT).assertIsDisplayed()
        composeTestRule.onNodeWithTag(SignInTags.CREATE_ACCOUNT_TEXT).performClick()


        /** 2. Navigate in Create account Screens*/

        /* Username Screen */
        composeTestRule.onNodeWithTag(CreateAccountTags.TITLE).assertIsDisplayed()
        composeTestRule.onNodeWithTag(CreateAccountTags.USERNAME_FIELD).assertIsDisplayed()
        composeTestRule.onNodeWithTag(CreateAccountTags.NEXT_BUTTON).assertIsDisplayed()

        composeTestRule.onNodeWithTag(CreateAccountTags.USERNAME_FIELD).performTextInput("Bob")
        composeTestRule.onNodeWithTag(CreateAccountTags.NEXT_BUTTON).performClick()

        /* Email Screen */
        composeTestRule.onNodeWithTag(CreateAccountTags.TITLE).assertIsDisplayed()
        composeTestRule.onNodeWithTag(CreateAccountTags.EMAIL_FIELD).assertIsDisplayed()
        composeTestRule.onNodeWithTag(CreateAccountTags.NEXT_BUTTON).assertIsDisplayed()

        composeTestRule.onNodeWithTag(CreateAccountTags.EMAIL_FIELD)
            .performTextInput("bob@mail.com")
        composeTestRule.onNodeWithTag(CreateAccountTags.NEXT_BUTTON).performClick()

        /* Password Screen */
        composeTestRule.onNodeWithTag(CreateAccountTags.TITLE).assertIsDisplayed()
        composeTestRule.onNodeWithTag(CreateAccountTags.PASSWORD_FIELD).assertIsDisplayed()
        composeTestRule.onNodeWithTag(CreateAccountTags.CONFIRM_PASSWORD_FIELD).assertIsDisplayed()
        composeTestRule.onNodeWithTag(CreateAccountTags.NEXT_BUTTON).assertIsDisplayed()

        composeTestRule.onNodeWithTag(CreateAccountTags.PASSWORD_FIELD)
            .performTextInput("Password123")
        composeTestRule.onNodeWithTag(CreateAccountTags.CONFIRM_PASSWORD_FIELD)
            .performTextInput("Password123")
        composeTestRule.onNodeWithTag(CreateAccountTags.NEXT_BUTTON).performClick()

        /* Skills Screen */
        val skillTag = CreateAccountTags.SKILL_CHIP_PREFIX + "CALCULUS"
        composeTestRule.onNodeWithTag(CreateAccountTags.TITLE).assertIsDisplayed()
        composeTestRule.onNodeWithTag(CreateAccountTags.SKILLS_FLOW).assertIsDisplayed()
        composeTestRule.onNodeWithTag(skillTag).assertIsDisplayed()
        composeTestRule.onNodeWithTag(CreateAccountTags.NEXT_BUTTON).assertIsDisplayed()

        composeTestRule.onNodeWithTag(skillTag).performClick()
        composeTestRule.onNodeWithTag(CreateAccountTags.NEXT_BUTTON).performClick()
        /* End of Create Account Screens */

        /** Wait until firestore auth operation completes and Profile Screen is displayed */
        composeTestRule.waitUntil(timeoutMillis = 10_000){
            composeTestRule.onAllNodesWithTag(ProfileTestTags.PROFILE_TITLE)
                .fetchSemanticsNodes().isNotEmpty()
        }

        /** 3. Profile Screen Navigation Verification */
        navigateThroughAllScreensAfterAuthentification()
    }

    @Test
    fun completeUserFlow1_AutoSignInAndNavigate() {
        composeTestRule.waitForIdle()

        /** Wait until the previous test is done a the app is set up again */
        composeTestRule.waitUntil(timeoutMillis = 10_000){
            composeTestRule.onAllNodesWithTag(ProfileTestTags.PROFILE_TITLE)
                .fetchSemanticsNodes().isNotEmpty()
        }

        /** Perform Navigation Verification */
        navigateThroughAllScreensAfterAuthentification()
    }

    /**
     * Navigate through all main screens after authentication. Starting from Profile Screen, then to
     * Feed Screen, and finally to the Chat Screen.
     */
    fun navigateThroughAllScreensAfterAuthentification() {

        val visibleComposableProfile = listOf(
            ProfileTestTags.PROFILE_TITLE,
            ProfileTestTags.EDIT_PROFILE,
            ProfileTestTags.PROFILE_TITLE,
            ProfileTestTags.EMAIL_SECTION,
            ProfileTestTags.USERNAME_SECTION,
            ProfileTestTags.SKILLS_SECTION,
            ProfileTestTags.PREFERENCES_SECTION
        )

        val visibleComposableBotBar = listOf(
            BottomBarTestTag.BOTTOM_BAR,
            BottomBarTestTag.PROFILE_BUTTON,
            BottomBarTestTag.OFFER_SCREEN_BUTTON,
            BottomBarTestTag.CHAT_BUTTON
        )

        val visibleComposableOfferScreen = listOf(
            OfferScreenTestTags.OFFER_CARD,
            OfferScreenTestTags.OFFER_GIVE,
            OfferScreenTestTags.OFFER_RECEIVE
        )

        val visibleComposableChatScreen = emptyList<String>() //No tests tags defined yet

        composeTestRule.waitForIdle()

        /** Profile Screen checks */
        for (testTag in visibleComposableProfile) {
            composeTestRule.onNodeWithTag(testTag).assertIsDisplayed()
        }

        for (testTag in visibleComposableBotBar) {
            composeTestRule.onNodeWithTag(testTag).assertIsDisplayed()
        }

        /** Navigate to Offer Screen */
        composeTestRule.onNodeWithTag(BottomBarTestTag.OFFER_SCREEN_BUTTON).performClick()

        for (testTag in visibleComposableOfferScreen) {
            composeTestRule.onNodeWithTag(testTag).assertIsDisplayed()
        }

        for (testTag in visibleComposableBotBar) {
            composeTestRule.onNodeWithTag(testTag).assertIsDisplayed()
        }

        /** Navigate to Chat Screen */
        composeTestRule.onNodeWithTag(BottomBarTestTag.CHAT_BUTTON).performClick()

        for (testTag in visibleComposableChatScreen) {
            composeTestRule.onNodeWithTag(testTag).assertIsDisplayed()
        }

        for (testTag in visibleComposableBotBar) {
            composeTestRule.onNodeWithTag(testTag).assertIsDisplayed()
        }
    }

    @Test
    fun finalTest_UserIsAuthentified() {
        composeTestRule.waitForIdle()

        /** Verify that the user is correctly authenticated */
        val currentUser = FirebaseAuth.getInstance().currentUser
        assertNotNull(currentUser)
        assertEquals("bob@mail.com",currentUser!!.email)

    }
}
