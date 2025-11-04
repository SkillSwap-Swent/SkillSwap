package com.swent.skillswap.user

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
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
        // Clean up the "users" collection
        val users = FirebaseEmulator.firestore.collection("users").get().await()
        for (doc in users.documents) {
            FirebaseEmulator.firestore.collection("users").document(doc.id).delete().await()
        }

        // Initialize FirebaseApp if necessary
        try {
            if (FirebaseApp.getApps(ctx).isEmpty()) {
                FirebaseApp.initializeApp(ctx)
            }
        } catch (e: Exception) {
            // Ignore if already initialized
        }

        // Auth: create / sign in a user on the emulator
        val auth = FirebaseAuth.getInstance()
        val testPassword = "test-password-123"
        try {
            auth.createUserWithEmailAndPassword(testUser.email, testPassword).await()
        } catch (e: Exception) {
            // Ignore - user may already exist on emulator
        }
        try {
            auth.signInWithEmailAndPassword(testUser.email, testPassword).await()
        } catch (e: Exception) {
            try {
                auth.signInAnonymously().await()
            } catch (_: Exception) {
                /* ignore */
            }
        }

        // Get the effective uid (auth) or generate one if absent
        val authUid = auth.currentUser?.uid ?: repo.getNewUid()

        // Add a Firestore user corresponding to the logged-in user
        val userToAdd = testUser.copy(uid = authUid)
        repo.addUser(userToAdd)

        // Instantiate the ViewModel with the emulated repo
        viewModel = ProfileViewModel(repo)
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
            // Give the initial composition time to settle
            composeTestRule.waitUntil(5000) {
                try {
                    composeTestRule.onNodeWithTag(ProfileTestTags.PROFILE_TITLE).assertIsDisplayed()
                    true
                } catch (e: Exception) {
                    false
                }
            }
        }

        step("Verify all profile elements are displayed") {
            composeTestRule.onNodeWithTag(ProfileTestTags.PROFILE_TITLE).assertIsDisplayed()
            composeTestRule.onNodeWithTag(ProfileTestTags.EMAIL_SECTION).assertIsDisplayed()
            composeTestRule.onNodeWithTag(ProfileTestTags.USERNAME_SECTION).assertIsDisplayed()
            composeTestRule.onNodeWithTag(ProfileTestTags.SKILLS_SECTION).assertIsDisplayed()
            composeTestRule.onNodeWithTag(ProfileTestTags.PREFERENCES_SECTION).assertIsDisplayed()
            composeTestRule.onNodeWithTag(ProfileTestTags.PROFILE_PICTURE).assertIsDisplayed()
            composeTestRule
                .onNodeWithTag(ProfileTestTags.EDIT_PROFILE)
                .assertIsDisplayed()
                .assertHasClickAction()
        }

        step("Expand and verify email section") {
            composeTestRule.onNodeWithTag(ProfileTestTags.EMAIL_SECTION).performClick()
            composeTestRule.waitUntil(5000) {
                try {
                    composeTestRule.onNodeWithTag(ProfileTestTags.EMAIL_VALUE).assertIsDisplayed()
                    true
                } catch (e: Exception) {
                    false
                }
            }
        }

        step("Expand and verify username section") {
            composeTestRule.onNodeWithTag(ProfileTestTags.USERNAME_SECTION).performClick()
            composeTestRule.waitUntil(5000) {
                try {
                    composeTestRule
                        .onNodeWithTag(ProfileTestTags.USERNAME_VALUE)
                        .assertIsDisplayed()
                    true
                } catch (e: Exception) {
                    false
                }
            }
        }

        step("Expand and verify skills section") {
            composeTestRule.onNodeWithTag(ProfileTestTags.SKILLS_SECTION).performClick()
            composeTestRule.waitUntil(5000) {
                try {
                    composeTestRule.onNodeWithTag(ProfileTestTags.SKILLS_LIST).assertIsDisplayed()
                    true
                } catch (e: Exception) {
                    false
                }
            }
        }

        step("Expand and verify preferences section") {
            composeTestRule.onNodeWithTag(ProfileTestTags.PREFERENCES_SECTION).performClick()
            composeTestRule.waitUntil(5000) {
                try {
                    composeTestRule
                        .onNodeWithTag(ProfileTestTags.PREF_OPTION_MONEY)
                        .assertIsDisplayed()
                    composeTestRule
                        .onNodeWithTag(ProfileTestTags.PREF_OPTION_SKILLS)
                        .assertIsDisplayed()
                    true
                } catch (e: Exception) {
                    false
                }
            }
        }
    }

    @Test
    fun profileScreen_selectingMoneyPreferenceUpdatesViewModel() = run {
        step("Display ProfileScreen") {
            composeTestRule.setContent { SkillSwapAppTheme { ProfileScreen(vm = viewModel) } }
        }

        step("Expand preferences section and select Money") {
            composeTestRule.onNodeWithTag(ProfileTestTags.PREFERENCES_SECTION).performClick()
            composeTestRule.onNodeWithTag(ProfileTestTags.PREF_OPTION_MONEY).performClick()

            // Wait for ViewModel preference to update to MONEY
            waitForPreferenceUpdate(Preference.MONEY)

            val updatedPreference = viewModel.userState.value.preference
            assert(updatedPreference == Preference.MONEY)
        }

        step("Select Skills preference") {
            composeTestRule.onNodeWithTag(ProfileTestTags.PREF_OPTION_SKILLS).performClick()

            // Wait for ViewModel preference to update back to SKILLS
            waitForPreferenceUpdate(Preference.SKILLS)

            val updatedPreference = viewModel.userState.value.preference
            assert(updatedPreference == Preference.SKILLS)
        }
    }
}
