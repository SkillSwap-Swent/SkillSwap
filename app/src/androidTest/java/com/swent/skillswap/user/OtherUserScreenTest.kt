package com.swent.skillswap.user

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import com.swent.skillswap.model.tags.SkillTag
import com.swent.skillswap.model.user.Preference
import com.swent.skillswap.model.user.Skill
import com.swent.skillswap.model.user.User
import com.swent.skillswap.model.user.UserRepoFirestore
import com.swent.skillswap.resources.theme.SkillSwapAppTheme
import com.swent.skillswap.ui.user.OtherUserScreen
import com.swent.skillswap.ui.user.OtherUserViewModel
import com.swent.skillswap.ui.user.ProfileTestTags
import com.swent.skillswap.utils.FirebaseEmulator
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OtherUserScreenTest : TestCase() {

    @get:Rule val composeTestRule = createComposeRule()

    private val ctx = InstrumentationRegistry.getInstrumentation().targetContext
    private var db: FirebaseFirestore
    private var repo: UserRepoFirestore
    private lateinit var viewModel: OtherUserViewModel

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

    private val otherUser =
        User(
            uid = "other-user-profile",
            username = "Other",
            email = "other@example.com",
            profilePicture = "",
            skillSet =
                setOf(
                    Skill(name = SkillTag.COMPUTER_PROGRAMMING, rank = 4.5f, ""),
                    Skill(name = SkillTag.THERMODYNAMICS, rank = 5.0f, "")
                ),
            rating = 1f,
            availability = emptyList(),
            preference = Preference.MONEY
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
        } catch (_: Exception) {
            // Ignore if already initialized
        }

        val auth = FirebaseAuth.getInstance()
        val testPassword = "test-password-123"
        try {
            auth.createUserWithEmailAndPassword(testUser.email, testPassword).await()
        } catch (_: Exception) {
            // Ignore - user may already exist
        }
        try {
            auth.signInWithEmailAndPassword(testUser.email, testPassword).await()
        } catch (_: Exception) {
            try {
                auth.signInAnonymously().await()
            } catch (_: Exception) {}
        }

        val authUid = auth.currentUser?.uid ?: repo.getNewUid()
        val userToAdd = testUser.copy(uid = authUid)
        repo.addUser(userToAdd)
        repo.addUser(otherUser)

        viewModel = OtherUserViewModel(userId = otherUser.uid, repo = repo, onGoBack = {})

        composeTestRule.setContent { SkillSwapAppTheme { OtherUserScreen(vm = viewModel) } }
        composeTestRule.waitForIdle()
    }

    private fun waitForNodeToExist(tag: String, timeoutMillis: Long = 10_000) {
        composeTestRule.waitUntil(timeoutMillis) {
            try {
                composeTestRule.onNodeWithTag(tag).assertExists()
                true
            } catch (_: Exception) {
                false
            }
        }
    }

    @Test
    fun profileScreen_displaysAllElements() = run {
        step("Verify all profile elements are displayed") {
            val tags =
                listOf(
                    ProfileTestTags.PROFILE_TITLE,
                    ProfileTestTags.PROFILE_PICTURE_BOX,
                    ProfileTestTags.PROFILE_PICTURE_IMAGE,
                    ProfileTestTags.INFO_CARD,
                    ProfileTestTags.EMAIL_VALUE,
                    ProfileTestTags.USERNAME_VALUE,
                )

            tags.forEach { tag ->
                waitForNodeToExist(tag)
                composeTestRule.onNodeWithTag(tag).performScrollTo()
                composeTestRule.onNodeWithTag(tag).assertIsDisplayed()
            }
        }
    }
}
