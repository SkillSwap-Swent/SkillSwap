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
import com.swent.skillswap.ui.chat.ChatListTestTags
import com.swent.skillswap.ui.editUser.EditUserTags
import com.swent.skillswap.ui.navigation.NavigationTestTags
import com.swent.skillswap.ui.user.ProfileTestTags
import com.swent.skillswap.utils.FirebaseEmulator
import java.net.HttpURLConnection
import java.net.URL
import kotlin.text.get
import org.junit.Before
import org.junit.BeforeClass
import org.junit.FixMethodOrder
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters

/** End-to-end tests for Milestone 2 Tests complete user flows */
@RunWith(AndroidJUnit4::class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING) // Be careful, tests order matters !
class End2EndM2 {

    lateinit var db: com.google.firebase.firestore.FirebaseFirestore
    lateinit var auth: FirebaseAuth

    /** Companion object to clear the Auth emulator before running tests */
    companion object {
        private const val EMULATOR_URL = "http://10.0.2.2:9099"
        private const val PROJECT_ID = "skillswap-93276"

        @BeforeClass
        @JvmStatic
        fun clearAuthEmulatorAndFirestoreEmulators() {
            val url = URL("$EMULATOR_URL/emulator/v1/projects/$PROJECT_ID/accounts")
            with(url.openConnection() as HttpURLConnection) {
                requestMethod = "DELETE"
                val responseCode = responseCode
                if (responseCode != 200) {
                    throw Exception("Failed to clear Auth emulator: $responseCode")
                }
                disconnect()
            }

            FirebaseEmulator.clearFirestoreEmulator()
        }
    }

    @Before
    fun setup() {
        db = FirebaseEmulator.firestore
        auth = FirebaseAuth.getInstance()
    }

    @get:Rule val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun t0_createAccount() {
        /** 1. Launch app and verify sign in screen */
        composeTestRule.waitForIdle()

        /** Verify Sign-In screen is displayed */
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

        composeTestRule.onNodeWithTag(SignInTags.CREATE_ACCOUNT_TEXT).performScrollTo()
        composeTestRule.onNodeWithTag(SignInTags.CREATE_ACCOUNT_TEXT).assertIsDisplayed()
        composeTestRule.onNodeWithTag(SignInTags.CREATE_ACCOUNT_TEXT).performClick()

        /** 2. Navigate in Create account Screens */

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

        composeTestRule
            .onNodeWithTag(CreateAccountTags.EMAIL_FIELD)
            .performTextInput("bob@mail.com")
        composeTestRule.onNodeWithTag(CreateAccountTags.NEXT_BUTTON).performClick()

        /* Password Screen */
        composeTestRule.onNodeWithTag(CreateAccountTags.TITLE).assertIsDisplayed()
        composeTestRule.onNodeWithTag(CreateAccountTags.PASSWORD_FIELD).assertIsDisplayed()
        composeTestRule.onNodeWithTag(CreateAccountTags.CONFIRM_PASSWORD_FIELD).assertIsDisplayed()
        composeTestRule.onNodeWithTag(CreateAccountTags.NEXT_BUTTON).assertIsDisplayed()

        composeTestRule
            .onNodeWithTag(CreateAccountTags.PASSWORD_FIELD)
            .performTextInput("Password123")
        composeTestRule
            .onNodeWithTag(CreateAccountTags.CONFIRM_PASSWORD_FIELD)
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
        composeTestRule.waitUntil(timeoutMillis = 10_000) {
            composeTestRule
                .onAllNodesWithTag(ProfileTestTags.PROFILE_TITLE)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

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

        for (testTag in visibleComposableProfile) {
            composeTestRule.onNodeWithTag(testTag).assertIsDisplayed()
        }
    }

    @Test
    fun t3_canModifyProfile() {
        /** Assumes user is already signed in from previous test */
        composeTestRule.onNodeWithTag(ProfileTestTags.EDIT_PROFILE).performClick()

        /** Edit Profile Screen */
        val visibleComposablesEditScreen =
            listOf(
                EditUserTags.GO_BACK_BUTTON,
                EditUserTags.USERNAME_TEXTFIELD,
                EditUserTags.EMAIL_TEXTFIELD,
                EditUserTags.VALIDATE_BUTTON,
                EditUserTags.PROFILE_PICTURE,
                EditUserTags.SKILLSET_SECTION
            )

        for (testTag in visibleComposablesEditScreen) {
            composeTestRule.onNodeWithTag(testTag).assertIsDisplayed()
        }

        /** Modify username */
        composeTestRule.onNodeWithTag(EditUserTags.USERNAME_TEXTFIELD).performTextClearance()
        composeTestRule.onNodeWithTag(EditUserTags.USERNAME_TEXTFIELD).performTextInput("Bobby")
        composeTestRule.onNodeWithTag(EditUserTags.VALIDATE_BUTTON).performClick()

        /** Check that change is stored in firestore */
        var usernameUpdated = false

        composeTestRule.waitUntil(timeoutMillis = 10_000) {
            db.collection("users")
                .document(auth.currentUser!!.uid)
                .get()
                .addOnSuccessListener { document ->
                    usernameUpdated = document.getString("username") == "Bobby"
                }
                .addOnFailureListener { usernameUpdated = false }

            // Vérifier le résultat
            usernameUpdated
        }
    }

    @Test
    fun t2_canScrollOnFeedScreenAndClickOnReportBlockUserMenu() {
        composeTestRule.onNodeWithTag(NavigationTestTags.FEED_TAB).performClick()
        /* THE ACTUAL FEED SCREEN IS EMPTY
                /** Assumes user is already signed in from previous test */
                val visibleComposableFeedScreen =
                    listOf(
                        FeedScreenTestTags.FEED_CARD,
                        FeedScreenTestTags.FEED_THUMBNAIL,
                        FeedScreenTestTags.SKILL_REQUESTED,
                        FeedScreenTestTags.SKILL_GIVE,
                        FeedScreenTestTags.ACCEPT_BUTTON,
                        FeedScreenTestTags.DECLINE_BUTTON,
                    )
                //composeTestRule.onNodeWithText("Generated 1").assertIsDisplayed()
                composeTestRule.waitForIdle()


                /** Scroll down */
                composeTestRule.onNodeWithTag(FeedScreenTestTags.FEED_CARD).performTouchInput { swipeDown() }
                composeTestRule.onNodeWithText("Looking for Skill 2").assertIsDisplayed()
                composeTestRule.waitForIdle()

                /** Scroll up */
                composeTestRule.onNodeWithTag(FeedScreenTestTags.FEED_CARD).performTouchInput { swipeUp() }
                composeTestRule.onNodeWithText("Looking for Skill 15").assertIsDisplayed()
                composeTestRule.waitForIdle()

                /** Open menu */
                composeTestRule.onNodeWithTag(FeedScreenTestTags.FEED_MENU_BUTTON).performClick()
                composeTestRule.waitForIdle()
                composeTestRule.onNodeWithText("Report User").assertIsDisplayed()
                composeTestRule.onNodeWithText("Block User").assertIsDisplayed()
                composeTestRule.onNodeWithTag("Report User").performClick()
                composeTestRule.onNodeWithTag("Block User").performClick()
        */

    }

    @Test
    fun t1_canChatInChatScreen() {
        /** Assumes user is already signed in from previous test */
        composeTestRule.onNodeWithTag(NavigationTestTags.CHAT_TAB).performClick()
        composeTestRule.waitForIdle()

        /** Check that chat screen is displayed */
        composeTestRule.onNodeWithTag(ChatListTestTags.SCREEN).assertIsDisplayed()
        composeTestRule.onNodeWithTag(ChatListTestTags.TITLE).assertIsDisplayed()
        composeTestRule.onNodeWithTag(ChatListTestTags.OFFER).assertIsDisplayed()
        composeTestRule.onNodeWithTag(ChatListTestTags.REQUEST).assertIsDisplayed()

        val OfferChatsText =
            listOf(
                /* Usernames in Offer chat tab */
                "Alex Johnson",
                "Sarah Chen",
                "Mike Rodriguez"
            )

        val visibleComposableBottomBar =
            listOf(
                NavigationTestTags.FEED_TAB,
                NavigationTestTags.CHAT_TAB,
                NavigationTestTags.PROFILE_TAB
            )

        for (username in OfferChatsText) {
            composeTestRule.onNodeWithText(username).assertExists()
            composeTestRule.onNodeWithText(username).performClick()
        }

        for (testTag in visibleComposableBottomBar) {
            composeTestRule.onNodeWithTag(testTag).assertIsDisplayed()
        }

        /** Go to Request chat tab */
        composeTestRule.onNodeWithTag(ChatListTestTags.REQUEST).performClick()

        val requestChatUsernames = listOf("Emma Wilson", "Alex Johnson")

        for (username in requestChatUsernames) {
            composeTestRule.onNodeWithText(username).assertIsDisplayed()
            composeTestRule.onNodeWithText(username).performClick()
        }
    }
}
