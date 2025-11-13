package com.swent.skillswap.user

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import com.swent.skillswap.model.tags.SkillTag
import com.swent.skillswap.model.user.Preference
import com.swent.skillswap.model.user.Skill
import com.swent.skillswap.model.user.User
import com.swent.skillswap.model.user.UserRepoFirestore
import com.swent.skillswap.ui.theme.SkillSwapAppTheme
import com.swent.skillswap.ui.user.ProfileScreen
import com.swent.skillswap.ui.user.ProfileTestTags
import com.swent.skillswap.utils.FirebaseEmulator
import com.swent.skillswap.viewModel.ProfileViewModel
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProfileScreenTest : TestCase() {

    @get:Rule val composeTestRule = createComposeRule()

    private val ctx =
        androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().targetContext
    private lateinit var db: FirebaseFirestore
    private lateinit var repo: UserRepoFirestore
    private lateinit var viewModel: ProfileViewModel

    private val testUser =
        User(
            uid = "test-user-profile",
            username = "Profile Tester",
            email = "profiletest@example.com",
            profilePicture = "",
            skillSet =
                setOf(
                    Skill(name = SkillTag.MACHINE_DESIGN, rank = 4.5f, ""),
                    Skill(name = SkillTag.ALGORITHMS, rank = 5.0f, "")
                ),
            rating = 4.8f,
            availability = emptyList(),
            preference = Preference.SKILLS
        )

    init {
        FirebaseEmulator.startEmulator()
        db = FirebaseEmulator.firestore
        repo = UserRepoFirestore(db)
    }

    @Before
    fun setUp() = runBlocking {
        val users = FirebaseEmulator.firestore.collection("users").get().await()
        for (doc in users.documents) {
            FirebaseEmulator.firestore.collection("users").document(doc.id).delete().await()
        }

        try {
            if (FirebaseApp.getApps(ctx).isEmpty()) {
                FirebaseApp.initializeApp(ctx)
            }
        } catch (e: Exception) {
            // Ignore if already initialized
        }

        val auth = FirebaseAuth.getInstance()
        val testPassword = "test-password-123"
        try {
            auth.createUserWithEmailAndPassword(testUser.email, testPassword).await()
        } catch (e: Exception) {
            // Ignore - user may already exist
        }
        try {
            auth.signInWithEmailAndPassword(testUser.email, testPassword).await()
        } catch (e: Exception) {
            try {
                auth.signInAnonymously().await()
            } catch (_: Exception) {}
        }

        val authUid = auth.currentUser?.uid ?: repo.getNewUid()
        val userToAdd = testUser.copy(uid = authUid)
        repo.addUser(userToAdd)

        viewModel = ProfileViewModel(repo)
    }

    private fun waitForNodeToExist(tag: String, timeoutMillis: Long = 10_000) {
        composeTestRule.waitUntil(timeoutMillis) {
            try {
                composeTestRule.onNodeWithTag(tag).assertExists()
                true
            } catch (e: Exception) {
                false
            }
        }
    }

    private fun waitForPreferenceUpdate(
        expectedPreference: Preference,
        timeoutMillis: Long = 5000
    ) {
        composeTestRule.waitUntil(timeoutMillis) {
            viewModel.userState.value.preference == expectedPreference
        }
    }

    @Test
    fun profileScreen_displaysAllElements() = run {
        step("Display ProfileScreen") {
            composeTestRule.setContent { SkillSwapAppTheme { ProfileScreen(vm = viewModel) } }
            composeTestRule.waitForIdle()
        }

        step("Verify all profile elements are displayed") {
            val tags =
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
                    ProfileTestTags.LOGOUT_BUTTON,
                    ProfileTestTags.ADD_POST_BUTTON
                )

            tags.forEach { tag ->
                waitForNodeToExist(tag)
                composeTestRule.onNodeWithTag(tag).performScrollTo()
                composeTestRule.onNodeWithTag(tag).assertIsDisplayed()
            }

            composeTestRule
                .onNodeWithTag(ProfileTestTags.EDIT_PROFILE_BUTTON)
                .assertHasClickAction()
        }
    }

    @Test
    fun profileScreen_selectingMoneyPreferenceUpdatesViewModel() = run {
        step("Display ProfileScreen") {
            composeTestRule.setContent { SkillSwapAppTheme { ProfileScreen(vm = viewModel) } }
            composeTestRule.waitForIdle()
        }

        step("Toggle preference switch to MONEY") {
            waitForNodeToExist(ProfileTestTags.PREFERENCE_SWITCH)

            composeTestRule
                .onNodeWithTag(ProfileTestTags.PREFERENCE_SWITCH)
                .performScrollTo()
                .performClick() // SKILLS -> MONEY

            waitForPreferenceUpdate(Preference.MONEY)

            val updatedPreference = viewModel.userState.value.preference
            assert(updatedPreference == Preference.MONEY)
        }

        step("Toggle preference switch back to SKILLS") {
            composeTestRule
                .onNodeWithTag(ProfileTestTags.PREFERENCE_SWITCH)
                .performScrollTo()
                .performClick() // MONEY -> SKILLS

            waitForPreferenceUpdate(Preference.SKILLS)

            val updatedPreference = viewModel.userState.value.preference
            assert(updatedPreference == Preference.SKILLS)
        }
    }

    @Test
    fun profileScreen_buttonsAreClickable() = run {
        step("Display ProfileScreen") {
            composeTestRule.setContent { SkillSwapAppTheme { ProfileScreen(vm = viewModel) } }
            composeTestRule.waitForIdle()
        }

        step("Edit profile button is clickable") {
            waitForNodeToExist(ProfileTestTags.EDIT_PROFILE_BUTTON)
            composeTestRule
                .onNodeWithTag(ProfileTestTags.EDIT_PROFILE_BUTTON)
                .performScrollTo()
                .assertIsDisplayed()
                .assertHasClickAction()
                .performClick()
        }

        step("Skills button is clickable") {
            waitForNodeToExist(ProfileTestTags.SKILLS_BUTTON)
            composeTestRule
                .onNodeWithTag(ProfileTestTags.SKILLS_BUTTON)
                .performScrollTo()
                .assertIsDisplayed()
                .assertHasClickAction()
                .performClick()
        }
        step("Add post button is clickable") {
            waitForNodeToExist(ProfileTestTags.ADD_POST_BUTTON)
            composeTestRule
                .onNodeWithTag(ProfileTestTags.ADD_POST_BUTTON)
                .performScrollTo()
                .assertIsDisplayed()
                .assertHasClickAction()
                .performClick()
        }

        step("Logout button is clickable") {
            waitForNodeToExist(ProfileTestTags.LOGOUT_BUTTON)
            composeTestRule
                .onNodeWithTag(ProfileTestTags.LOGOUT_BUTTON)
                .performScrollTo()
                .assertIsDisplayed()
                .assertHasClickAction()
                .performClick()
        }
    }
}
