/*
 * Written with help of copilot to complete all repetitive code, and set up the companion object
 */
package com.swent.skillswap.end2end

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.swent.skillswap.MainActivity
import com.swent.skillswap.model.post.PaymentMethod
import com.swent.skillswap.model.tags.SkillTag
import com.swent.skillswap.model.user.deserializeSkills
import com.swent.skillswap.ui.auth.CreateAccountTags
import com.swent.skillswap.ui.auth.SignInTags
import com.swent.skillswap.ui.chat.ChatListTestTags
import com.swent.skillswap.ui.editUser.EditUserTags
import com.swent.skillswap.ui.navigation.NavigationTestTags
import com.swent.skillswap.ui.personalPosts.PersonalPostsScreenTags
import com.swent.skillswap.ui.post.RequestScreenTags
import com.swent.skillswap.ui.user.ProfileTestTags
import com.swent.skillswap.ui.user.SkillsEditTestTags
import com.swent.skillswap.utils.FirebaseEmulator
import kotlin.collections.get
import kotlin.text.get
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.AfterClass
import org.junit.Before
import org.junit.BeforeClass
import org.junit.FixMethodOrder
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters

/** End-to-end tests for Milestone 2 Tests complete user flows */
@RunWith(AndroidJUnit4::class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class End2EndM2 {

    lateinit var db: com.google.firebase.firestore.FirebaseFirestore
    lateinit var auth: FirebaseAuth

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
        auth = FirebaseEmulator.auth
    }

    @get:Rule val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun t0_createAccount() {
        /** 1. Launch app and verify sign in screen */
        composeTestRule.waitUntil(timeoutMillis = 200_000) {
            try {
                composeTestRule.onNodeWithTag(SignInTags.LOGO).assertIsDisplayed()
                composeTestRule.onNodeWithTag(SignInTags.SIGN_IN_BUTTON).assertIsDisplayed()
                composeTestRule.onNodeWithTag(SignInTags.EMAIL_FIELD).assertIsDisplayed()
                composeTestRule.onNodeWithTag(SignInTags.PASSWORD_FIELD).assertIsDisplayed()
                // composeTestRule.onNodeWithTag(SignInTags.GOOGLE_BUTTON).assertIsDisplayed()
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
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule
                .onAllNodesWithTag(CreateAccountTags.TITLE)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        composeTestRule.onNodeWithTag(CreateAccountTags.TITLE).assertIsDisplayed()
        composeTestRule.onNodeWithTag(CreateAccountTags.USERNAME_FIELD).assertIsDisplayed()
        composeTestRule.onNodeWithTag(CreateAccountTags.NEXT_BUTTON).assertIsDisplayed()

        composeTestRule.onNodeWithTag(CreateAccountTags.USERNAME_FIELD).performTextInput("Bob")
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(CreateAccountTags.NEXT_BUTTON).performClick()
        composeTestRule.waitForIdle()

        /* Email Screen */
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule
                .onAllNodesWithTag(CreateAccountTags.EMAIL_FIELD)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        composeTestRule.onNodeWithTag(CreateAccountTags.TITLE).assertIsDisplayed()
        composeTestRule.onNodeWithTag(CreateAccountTags.EMAIL_FIELD).assertIsDisplayed()
        composeTestRule.onNodeWithTag(CreateAccountTags.NEXT_BUTTON).assertIsDisplayed()

        composeTestRule
            .onNodeWithTag(CreateAccountTags.EMAIL_FIELD)
            .performTextInput("bob@mail.com")
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(CreateAccountTags.NEXT_BUTTON).performClick()
        composeTestRule.waitForIdle()

        /* Password Screen */
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule
                .onAllNodesWithTag(CreateAccountTags.PASSWORD_FIELD)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        composeTestRule.onNodeWithTag(CreateAccountTags.TITLE).assertIsDisplayed()
        composeTestRule.onNodeWithTag(CreateAccountTags.PASSWORD_FIELD).assertIsDisplayed()
        composeTestRule.onNodeWithTag(CreateAccountTags.CONFIRM_PASSWORD_FIELD).assertIsDisplayed()
        composeTestRule.onNodeWithTag(CreateAccountTags.NEXT_BUTTON).assertIsDisplayed()

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

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule
                .onAllNodesWithTag(CreateAccountTags.SKILLS_FLOW)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        composeTestRule.onNodeWithTag(CreateAccountTags.TITLE).assertIsDisplayed()
        composeTestRule.onNodeWithTag(CreateAccountTags.SKILLS_FLOW).assertIsDisplayed()
        composeTestRule.onNodeWithTag(skillTag).performScrollTo()
        composeTestRule.onNodeWithTag(skillTag).assertIsDisplayed()
        composeTestRule.onNodeWithTag(CreateAccountTags.NEXT_BUTTON).assertIsDisplayed()

        composeTestRule.onNodeWithTag(skillTag).performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(CreateAccountTags.NEXT_BUTTON).performClick()

        /** Wait until firestore auth operation completes and Profile Screen is displayed */
        composeTestRule.waitUntil(timeoutMillis = 15_000) {
            composeTestRule
                .onAllNodesWithTag(ProfileTestTags.PROFILE_TITLE)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        val visibleComposableProfile =
            listOf(
                ProfileTestTags.PROFILE_TITLE,
                ProfileTestTags.PROFILE_PICTURE_BOX,
                ProfileTestTags.PROFILE_PICTURE_IMAGE,
                ProfileTestTags.EDIT_PROFILE_BUTTON,
                ProfileTestTags.INFO_CARD,
                ProfileTestTags.EMAIL_VALUE,
                ProfileTestTags.USERNAME_VALUE,
                ProfileTestTags.PREFERENCE_SWITCH,
                ProfileTestTags.SKILLS_BUTTON,
                ProfileTestTags.ADD_POST_BUTTON,
                ProfileTestTags.LOGOUT_BUTTON,
                ProfileTestTags.MY_POSTS_BUTTON
            )

        for (testTag in visibleComposableProfile) {
            composeTestRule.onNodeWithTag(testTag).performScrollTo()
            composeTestRule.onNodeWithTag(testTag).assertIsDisplayed()
        }
    }

    @Test
    fun t1_canModifyProfile() {
        /** Assumes user is already signed in from previous test */
        composeTestRule.waitUntil(timeoutMillis = 10_000) {
            composeTestRule
                .onAllNodesWithTag(ProfileTestTags.PROFILE_TITLE)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        composeTestRule.onNodeWithTag(ProfileTestTags.EDIT_PROFILE_BUTTON).performScrollTo()
        composeTestRule.onNodeWithTag(ProfileTestTags.EDIT_PROFILE_BUTTON).performClick()
        composeTestRule.waitForIdle()

        /** Edit Profile Screen */
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule
                .onAllNodesWithTag(EditUserTags.USERNAME_TEXTFIELD)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        val visibleComposablesEditScreen =
            listOf(
                EditUserTags.GO_BACK_BUTTON,
                EditUserTags.USERNAME_TEXTFIELD,
                EditUserTags.VALIDATE_BUTTON,
                EditUserTags.PROFILE_PICTURE
            )

        for (testTag in visibleComposablesEditScreen) {
            composeTestRule.onNodeWithTag(testTag).assertIsDisplayed()
        }

        /** Insert Invalid username doesn't navigate back and shows error */
        composeTestRule.onNodeWithTag(EditUserTags.USERNAME_TEXTFIELD).performTextClearance()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(EditUserTags.USERNAME_TEXTFIELD).performTextInput(" ")
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(EditUserTags.VALIDATE_BUTTON).performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(EditUserTags.VALIDATE_BUTTON).assertIsDisplayed()

        /** Modify username */
        composeTestRule.onNodeWithTag(EditUserTags.USERNAME_TEXTFIELD).performTextClearance()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(EditUserTags.USERNAME_TEXTFIELD).performTextInput("Bobby")
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(EditUserTags.VALIDATE_BUTTON).performClick()
        composeTestRule.onNodeWithTag(EditUserTags.GO_BACK_BUTTON).performClick()

        /** Wait for navigation back to profile */
        composeTestRule.waitUntil(timeoutMillis = 10_000) {
            composeTestRule
                .onAllNodesWithTag(ProfileTestTags.USERNAME_VALUE)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        /** Check username is updated on UI */
        composeTestRule.onNodeWithTag(ProfileTestTags.USERNAME_VALUE).assertTextEquals("Bobby")

        /** Verify change persisted to Firestore */
        composeTestRule.runOnIdle {
            runBlocking(Dispatchers.IO) {
                val document =
                    Tasks.await(db.collection("users").document(auth.currentUser!!.uid).get())
                assert(document.getString("username") == "Bobby") {
                    "Username was not updated in Firestore"
                }
            }
        }
    }

    @Test
    fun t2_canModifySkillsInProfile() {
        // ---------- Ensure Profile screen is fully loaded ----------
        composeTestRule.waitUntil(timeoutMillis = 10_000) {
            composeTestRule
                .onAllNodesWithTag(ProfileTestTags.SKILLS_BUTTON)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        // Navigate to Skills Edit Screen
        composeTestRule.onNodeWithTag(ProfileTestTags.SKILLS_BUTTON).performScrollTo()
        composeTestRule.onNodeWithTag(ProfileTestTags.SKILLS_BUTTON).performClick()

        // ---------- Wait until both skill lists are available ----------
        composeTestRule.waitUntil(timeoutMillis = 12_000) {
            composeTestRule
                .onAllNodesWithTag(SkillsEditTestTags.USER_SKILLS_BOX)
                .fetchSemanticsNodes()
                .isNotEmpty() &&
                composeTestRule
                    .onAllNodesWithTag(SkillsEditTestTags.OTHER_SKILLS_BOX)
                    .fetchSemanticsNodes()
                    .isNotEmpty()
        }

        // Verify Skills Edit Screen visible
        listOf(
                SkillsEditTestTags.TITLE_YOUR_SKILLS,
                SkillsEditTestTags.TITLE_SELECT_NEW,
                SkillsEditTestTags.USER_SKILLS_BOX,
                SkillsEditTestTags.OTHER_SKILLS_BOX,
                SkillsEditTestTags.BACK_BUTTON
            )
            .forEach { tag ->
                composeTestRule.onNodeWithTag(tag).performScrollTo()
                composeTestRule.onNodeWithTag(tag).assertIsDisplayed()
            }

        // ---------- Helper function: wait until chip appears ----------
        fun waitForChip(tag: String) {
            composeTestRule.waitUntil(timeoutMillis = 8000) {
                composeTestRule.onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty()
            }
        }

        // ---------- Add ALGORITHMS ----------
        val algorithmChipTag = CreateAccountTags.SKILL_CHIP_PREFIX + SkillTag.ALGORITHMS.name

        composeTestRule.onNodeWithTag(algorithmChipTag).performScrollTo()
        waitForChip(algorithmChipTag)
        composeTestRule.onNodeWithTag(algorithmChipTag).performClick()
        // Confirm ALGORITHMS moved to "Your Skills"
        waitForChip(algorithmChipTag)

        // ---------- Add PHYSICS_MECHANICS ----------
        val physicsTag = CreateAccountTags.SKILL_CHIP_PREFIX + SkillTag.PHYSICS_MECHANICS.name

        composeTestRule.onNodeWithTag(physicsTag).performScrollTo()
        waitForChip(physicsTag)
        composeTestRule.onNodeWithTag(physicsTag).performClick()

        waitForChip(physicsTag)

        // ---------- Add DATA_STRUCTURES ----------
        val dataTag = CreateAccountTags.SKILL_CHIP_PREFIX + SkillTag.DATA_STRUCTURES.name

        composeTestRule.onNodeWithTag(dataTag).performScrollTo()
        waitForChip(dataTag)
        composeTestRule.onNodeWithTag(dataTag).performClick()

        waitForChip(dataTag)

        // ---------- Save the changes ----------
        composeTestRule.onNodeWithTag(SkillsEditTestTags.BACK_BUTTON).performScrollTo()
        composeTestRule.onNodeWithTag(SkillsEditTestTags.BACK_BUTTON).performClick()

        // ---------- Wait for return to Profile ----------
        composeTestRule.waitUntil(timeoutMillis = 10_000) {
            composeTestRule
                .onAllNodesWithTag(ProfileTestTags.PROFILE_TITLE)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        // ---------- Validate Firestore Update ----------
        composeTestRule.runOnIdle {
            runBlocking(Dispatchers.IO) {
                val document =
                    Tasks.await(db.collection("users").document(auth.currentUser!!.uid).get())
                val skillSetString = document.getString("skillSet") ?: ""
                val skills = deserializeSkills(skillSetString)
                val skillNames = skills.map { it.name.name }

                val algorithmsAdded = skillNames.contains("ALGORITHMS")
                val physicsMechanicsAdded = skillNames.contains("PHYSICS_MECHANICS")
                val dataStructuresAdded = skillNames.contains("DATA_STRUCTURES")

                assert(algorithmsAdded && physicsMechanicsAdded && dataStructuresAdded) {
                    "Skills were not updated correctly. Got: $skillNames"
                }
            }
        }
    }

    @Test
    fun t3_canViewAndNavigateMyPostsScreen() {
        /** Assumes user is already signed in from previous test */
        composeTestRule.waitUntil(timeoutMillis = 10_000) {
            composeTestRule
                .onAllNodesWithTag(ProfileTestTags.PROFILE_TITLE)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        /** Navigate to My Posts Screen from Profile */
        composeTestRule.onNodeWithTag(ProfileTestTags.MY_POSTS_BUTTON).performScrollTo()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(ProfileTestTags.MY_POSTS_BUTTON).performClick()

        /** Wait for My Posts Screen to be displayed */
        composeTestRule.waitUntil(timeoutMillis = 10_000) {
            composeTestRule
                .onAllNodesWithTag(PersonalPostsScreenTags.TITLE)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        /** Verify My Posts Screen elements are displayed */
        val myPostsTags =
            listOf(
                PersonalPostsScreenTags.TITLE,
                PersonalPostsScreenTags.FILTER_ALL,
                PersonalPostsScreenTags.FILTER_OFFERS,
                PersonalPostsScreenTags.FILTER_REQUESTS
            )

        for (testTag in myPostsTags) {
            // composeTestRule.onNodeWithTag(testTag).performScrollTo()
            composeTestRule.onNodeWithTag(testTag).assertIsDisplayed()
        }

        /** Verify title text */
        composeTestRule.onNodeWithText("My Posts").assertIsDisplayed()

        /** Test filter tabs - click on Offers */
        composeTestRule.onNodeWithTag(PersonalPostsScreenTags.FILTER_OFFERS).performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(500) // Allow animation to complete
        composeTestRule.onNodeWithTag(PersonalPostsScreenTags.EMPTY_STATE).isDisplayed()

        /** Test filter tabs - click on Requests */
        composeTestRule.onNodeWithTag(PersonalPostsScreenTags.FILTER_REQUESTS).performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(500)
        composeTestRule.onNodeWithTag(PersonalPostsScreenTags.EMPTY_STATE).isDisplayed()

        /** Test filter tabs - click back on All */
        composeTestRule.onNodeWithTag(PersonalPostsScreenTags.FILTER_ALL).performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(500)
        composeTestRule.onNodeWithTag(PersonalPostsScreenTags.EMPTY_STATE).isDisplayed()

        /** Navigate back to Profile */
        composeTestRule.waitForIdle()
        composeTestRule.onAllNodesWithContentDescription("Back")[0].performClick()

        /** Wait for Profile Screen to be displayed again */
        composeTestRule.waitUntil(timeoutMillis = 10_000) {
            composeTestRule
                .onAllNodesWithTag(ProfileTestTags.PROFILE_TITLE)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        composeTestRule.onNodeWithTag(ProfileTestTags.PROFILE_TITLE).assertIsDisplayed()
    }

    @Test
    fun t4_canNavigateToAddPostScreenAndCreateRequest() {
        composeTestRule.waitUntil(timeoutMillis = 10_000) {
            composeTestRule
                .onAllNodesWithTag(ProfileTestTags.PROFILE_TITLE)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        // ---------- Navigate to Add Post Screen ----------
        composeTestRule.onNodeWithTag(ProfileTestTags.ADD_POST_BUTTON).performScrollTo()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(ProfileTestTags.ADD_POST_BUTTON).performClick()

        // ---------- Wait for Add Request Screen ----------
        composeTestRule.waitUntil(10_000) {
            composeTestRule
                .onAllNodesWithTag(RequestScreenTags.TITLE_INPUT)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        // ---------- Verify UI ----------
        composeTestRule.onNodeWithTag(RequestScreenTags.BACK_BUTTON).assertIsDisplayed()
        composeTestRule.onNodeWithTag(RequestScreenTags.TITLE_INPUT).assertIsDisplayed()
        composeTestRule.onNodeWithTag(RequestScreenTags.DESCRIPTION_INPUT).assertIsDisplayed()
        composeTestRule.onNodeWithTag(RequestScreenTags.TAGS_INPUT).assertIsDisplayed()
        composeTestRule.onNodeWithTag(RequestScreenTags.CREATE_BUTTON).assertIsDisplayed()

        // ---------- Fill title ----------
        composeTestRule
            .onNodeWithTag(RequestScreenTags.TITLE_INPUT)
            .performTextInput("Need Help with Physics")
        composeTestRule.waitForIdle()

        // ---------- Try submit without description ----------
        composeTestRule.onNodeWithTag(RequestScreenTags.CREATE_BUTTON).performScrollTo()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(RequestScreenTags.CREATE_BUTTON).performClick()

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule
                .onAllNodesWithTag(RequestScreenTags.ERROR_MESSAGE)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeTestRule.onNodeWithTag(RequestScreenTags.ERROR_MESSAGE).assertIsDisplayed()

        composeTestRule.onNodeWithTag(RequestScreenTags.DESCRIPTION_INPUT).assertIsDisplayed()

        // ---------- Fill description ----------
        composeTestRule
            .onNodeWithTag(RequestScreenTags.DESCRIPTION_INPUT)
            .performTextInput("Looking for someone to help me understand mechanics")
        composeTestRule.waitForIdle()

        // ---------- Type tag: physics ----------
        composeTestRule.onNodeWithTag(RequestScreenTags.TAGS_INPUT).performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(RequestScreenTags.TAGS_INPUT).performTextInput("physics")
        composeTestRule.waitForIdle()

        composeTestRule.waitUntil(5_000) {
            composeTestRule
                .onAllNodesWithTag("${RequestScreenTags.TAG_SUGGESTION}_PHYSICS_MECHANICS")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        // ---------- Select tag ----------
        composeTestRule
            .onNodeWithTag("${RequestScreenTags.TAG_SUGGESTION}_PHYSICS_MECHANICS")
            .performClick()
        composeTestRule.waitForIdle()

        composeTestRule.waitUntil(5_000) {
            composeTestRule
                .onAllNodesWithTag("${RequestScreenTags.TAG_CHIP}_PHYSICS_MECHANICS")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        composeTestRule
            .onNodeWithTag("${RequestScreenTags.TAG_CHIP}_PHYSICS_MECHANICS")
            .assertIsDisplayed()

        // ---------- Select Payment Method ----------
        val skillsChip = "${RequestScreenTags.PAYMENT_METHOD_CHIP}_${PaymentMethod.SKILLS.name}"

        composeTestRule.onNodeWithTag(skillsChip).performScrollTo()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(skillsChip).performClick()
        composeTestRule.waitForIdle()

        // ---------- Remove previously selected tag ----------
        composeTestRule
            .onNodeWithTag("${RequestScreenTags.TAG_CHIP}_PHYSICS_MECHANICS")
            .performClick()
        composeTestRule.waitForIdle()

        composeTestRule.waitUntil(5_000) {
            composeTestRule
                .onAllNodesWithTag("${RequestScreenTags.TAG_CHIP}_PHYSICS_MECHANICS")
                .fetchSemanticsNodes()
                .isEmpty()
        }

        composeTestRule
            .onNodeWithTag("${RequestScreenTags.TAG_CHIP}_PHYSICS_MECHANICS")
            .assertDoesNotExist()

        // ---------- Try submitting without tags ----------
        composeTestRule.onNodeWithTag(RequestScreenTags.CREATE_BUTTON).performScrollTo()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(RequestScreenTags.CREATE_BUTTON).performClick()

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule
                .onAllNodesWithTag(RequestScreenTags.ERROR_MESSAGE)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeTestRule.onNodeWithTag(RequestScreenTags.ERROR_MESSAGE).assertIsDisplayed()

        composeTestRule.waitUntil(5_000) {
            composeTestRule
                .onAllNodesWithTag(RequestScreenTags.TAGS_INPUT)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        composeTestRule.onNodeWithTag(RequestScreenTags.TAGS_INPUT).assertIsDisplayed()
        composeTestRule.onNodeWithTag(ProfileTestTags.PROFILE_TITLE).assertDoesNotExist()

        // ---------- Re-add a tag: DATA_STRUCTURES ----------
        composeTestRule.onNodeWithTag(RequestScreenTags.TAGS_INPUT).performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(RequestScreenTags.TAGS_INPUT).performTextInput("data")
        composeTestRule.waitForIdle()

        composeTestRule.waitUntil(5_000) {
            composeTestRule
                .onAllNodesWithTag("${RequestScreenTags.TAG_SUGGESTION}_DATA_STRUCTURES")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        composeTestRule
            .onNodeWithTag("${RequestScreenTags.TAG_SUGGESTION}_DATA_STRUCTURES")
            .performClick()
        composeTestRule.waitForIdle()

        composeTestRule.waitUntil(5_000) {
            composeTestRule
                .onAllNodesWithTag("${RequestScreenTags.TAG_CHIP}_DATA_STRUCTURES")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        composeTestRule
            .onNodeWithTag("${RequestScreenTags.TAG_CHIP}_DATA_STRUCTURES")
            .assertIsDisplayed()

        // ---------- Submit form ----------
        composeTestRule.onNodeWithTag(RequestScreenTags.CREATE_BUTTON).performScrollTo()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(RequestScreenTags.CREATE_BUTTON).performClick()

        // ---------- Wait for return to Profile ----------
        composeTestRule.waitUntil(timeoutMillis = 15_000) {
            composeTestRule
                .onAllNodesWithTag(ProfileTestTags.PROFILE_TITLE)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        composeTestRule.onNodeWithTag(ProfileTestTags.PROFILE_TITLE).assertIsDisplayed()
    }

    @Test
    fun t5_canChatInChatScreen() {
        /** Assumes user is already signed in from previous test */
        composeTestRule.waitUntil(timeoutMillis = 10_000) {
            composeTestRule
                .onAllNodesWithTag(NavigationTestTags.CHAT_TAB)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        composeTestRule.onNodeWithTag(NavigationTestTags.CHAT_TAB).performClick()
        composeTestRule.waitForIdle()

        /** Check that chat screen is displayed */
        composeTestRule.waitUntil(timeoutMillis = 10_000) {
            composeTestRule
                .onAllNodesWithTag(ChatListTestTags.SCREEN)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

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
            composeTestRule.waitUntil(timeoutMillis = 5_000) {
                composeTestRule.onAllNodesWithText(username).fetchSemanticsNodes().isNotEmpty()
            }
            composeTestRule.onNodeWithText(username).assertExists()
            composeTestRule.onNodeWithText(username).performClick()
            composeTestRule.waitForIdle()
        }

        for (testTag in visibleComposableBottomBar) {
            composeTestRule.onNodeWithTag(testTag).assertIsDisplayed()
        }

        /** Go to Request chat tab */
        composeTestRule.onNodeWithTag(ChatListTestTags.REQUEST).performClick()
        composeTestRule.waitForIdle()

        val requestChatUsernames = listOf("Emma Wilson", "Alex Johnson")

        for (username in requestChatUsernames) {
            composeTestRule.waitUntil(timeoutMillis = 5_000) {
                composeTestRule.onAllNodesWithText(username).fetchSemanticsNodes().isNotEmpty()
            }
            composeTestRule.onNodeWithText(username).assertIsDisplayed()
            composeTestRule.onNodeWithText(username).performClick()
            composeTestRule.waitForIdle()
        }
    }

    @Test
    fun t6_canLogoutFromProfile() {
        /** Assumes user is already signed in from previous test */
        composeTestRule.waitUntil(timeoutMillis = 10_000) {
            composeTestRule
                .onAllNodesWithTag(NavigationTestTags.PROFILE_TAB)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        /** Navigate to Profile tab */
        composeTestRule.onNodeWithTag(NavigationTestTags.PROFILE_TAB).performClick()
        composeTestRule.waitForIdle()

        /** Wait for Profile Screen to be displayed */
        composeTestRule.waitUntil(timeoutMillis = 10_000) {
            composeTestRule
                .onAllNodesWithTag(ProfileTestTags.PROFILE_TITLE)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        /** Scroll to logout button and click it */
        composeTestRule.onNodeWithTag(ProfileTestTags.LOGOUT_BUTTON).performScrollTo()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(ProfileTestTags.LOGOUT_BUTTON).assertIsDisplayed()
        composeTestRule.onNodeWithTag(ProfileTestTags.LOGOUT_BUTTON).performClick()

        /** Wait for Sign-In screen to be displayed */
        composeTestRule.waitUntil(timeoutMillis = 15_000) {
            composeTestRule.onAllNodesWithTag(SignInTags.LOGO).fetchSemanticsNodes().isNotEmpty()
        }

        /** Verify Sign-In screen elements are displayed */
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

        /** Verify user is logged out */
        composeTestRule.runOnIdle {
            assert(auth.currentUser == null) {
                "User should be logged out but currentUser is not null"
            }
        }
    }
}
