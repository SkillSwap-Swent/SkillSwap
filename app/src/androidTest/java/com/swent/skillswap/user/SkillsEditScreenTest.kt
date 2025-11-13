package com.swent.skillswap.user

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
import com.swent.skillswap.model.user.Skill
import com.swent.skillswap.model.user.User
import com.swent.skillswap.model.user.UserRepoFirestore
import com.swent.skillswap.ui.auth.CreateAccountTags
import com.swent.skillswap.ui.editUser.EditUserViewModel
import com.swent.skillswap.ui.theme.SkillSwapAppTheme
import com.swent.skillswap.ui.user.SkillsEditScreen
import com.swent.skillswap.ui.user.SkillsEditTestTags
import com.swent.skillswap.utils.FirebaseEmulator
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SkillsEditScreenTest : TestCase() {

    @get:Rule val composeTestRule = createComposeRule()

    private val ctx =
        androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().targetContext
    private lateinit var db: FirebaseFirestore
    private lateinit var repo: UserRepoFirestore
    private lateinit var viewModel: EditUserViewModel

    private val testUser =
        User(
            uid = "test-user-123",
            username = "Chef",
            email = "test@example.com",
            profilePicture =
                "https://upload.wikimedia.org/wikipedia/commons/thumb/b/ba/She-goat_J1.jpg/500px-She-goat_J1.jpg",
            skillSet =
                setOf(
                    Skill(name = SkillTag.DATABASES, rank = 4F, ""),
                    Skill(name = SkillTag.DIGITAL_LOGIC, rank = 2F, ""),
                    Skill(name = SkillTag.PHYSICS_MECHANICS, rank = 5F, "")
                ),
            rating = 4.5f,
            availability = emptyList()
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
            // Ignore - user may already exist
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

        val authUid = auth.currentUser?.uid ?: repo.getNewUid()
        val userToAdd = testUser.copy(uid = authUid)
        repo.addUser(userToAdd)

        viewModel = EditUserViewModel(repo)
        viewModel.loadCurrentUser()
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

    private fun waitForSkillInViewModel(
        skillTag: SkillTag,
        shouldExist: Boolean,
        timeoutMillis: Long = 5_000
    ) {
        composeTestRule.waitUntil(timeoutMillis) {
            val updatedUser = viewModel.uiState.value.editedUser
            val skillNames = updatedUser?.skillSet?.map { it.name } ?: emptyList()
            if (shouldExist) {
                skillNames.contains(skillTag)
            } else {
                !skillNames.contains(skillTag)
            }
        }
    }

    @Test
    fun skillsEditScreen_displaysBasicComponents() = run {
        step("Display SkillsEditScreen with real repository") {
            composeTestRule.setContent {
                SkillSwapAppTheme { SkillsEditScreen(vm = viewModel, onBackClick = {}) }
            }
            composeTestRule.waitForIdle()
        }

        step("Verify Skills Edit Screen elements are displayed") {
            // Wait for user to be loaded in the VM (so skills appear)
            composeTestRule.waitUntil(timeoutMillis = 5_000) {
                viewModel.uiState.value.editedUser != null
            }

            val tags =
                listOf(
                    SkillsEditTestTags.TITLE_YOUR_SKILLS,
                    SkillsEditTestTags.USER_SKILLS_BOX,
                    SkillsEditTestTags.USER_SKILLS_FLOW,
                    SkillsEditTestTags.TITLE_SELECT_NEW,
                    SkillsEditTestTags.OTHER_SKILLS_BOX,
                    SkillsEditTestTags.OTHER_SKILLS_FLOW,
                    SkillsEditTestTags.BACK_BUTTON
                )

            tags.forEach { tag ->
                waitForNodeToExist(tag)
                composeTestRule.onNodeWithTag(tag).performScrollTo()
                composeTestRule.onNodeWithTag(tag).assertIsDisplayed()
            }
        }
    }

    @Test
    fun skillsEditScreen_removingSkillUpdatesViewModel() = run {
        step("Display SkillsEditScreen with real repository") {
            composeTestRule.setContent {
                SkillSwapAppTheme { SkillsEditScreen(vm = viewModel, onBackClick = {}) }
            }
            composeTestRule.waitForIdle()
        }

        step("Remove one skill from the user and verify ViewModel") {
            // Wait for user & skills
            composeTestRule.waitUntil(timeoutMillis = 5_000) {
                viewModel.uiState.value.editedUser?.skillSet?.isNotEmpty() == true
            }

            // DATABASES should initially be present in VM
            waitForSkillInViewModel(SkillTag.DATABASES, shouldExist = true)

            val chipTag = CreateAccountTags.SKILL_CHIP_PREFIX + SkillTag.DATABASES.name

            // Wait for chip then click it
            waitForNodeToExist(chipTag)
            composeTestRule.onNodeWithTag(chipTag).performScrollTo().performClick()

            // Wait until it's removed from the VM
            waitForSkillInViewModel(SkillTag.DATABASES, shouldExist = false)

            val updatedUser = viewModel.uiState.value.editedUser
            val skillNames = updatedUser?.skillSet?.map { it.name } ?: emptyList()

            assert(!skillNames.contains(SkillTag.DATABASES))
            assert(skillNames.contains(SkillTag.DIGITAL_LOGIC))
            assert(skillNames.contains(SkillTag.PHYSICS_MECHANICS))
        }
    }

    @Test
    fun skillsEditScreen_addingSkillUpdatesViewModel() = run {
        step("Display SkillsEditScreen") {
            composeTestRule.setContent {
                SkillSwapAppTheme { SkillsEditScreen(vm = viewModel, onBackClick = {}) }
            }
            composeTestRule.waitForIdle()
        }

        step("Add a new skill and verify ViewModel state") {
            // Wait for user & initial skills
            composeTestRule.waitUntil(timeoutMillis = 5_000) {
                viewModel.uiState.value.editedUser?.skillSet?.isNotEmpty() == true
            }

            // ALGORITHMS should not be in the initial user skill set
            waitForSkillInViewModel(SkillTag.ALGORITHMS, shouldExist = false)

            val chipTag = CreateAccountTags.SKILL_CHIP_PREFIX + SkillTag.ALGORITHMS.name

            // Click on the ALGORITHMS pill in "other skills"
            waitForNodeToExist(chipTag)
            composeTestRule.onNodeWithTag(chipTag).performScrollTo().performClick()

            // Wait for ViewModel state to update
            waitForSkillInViewModel(SkillTag.ALGORITHMS, shouldExist = true)

            val updatedUser = viewModel.uiState.value.editedUser
            val skillNames = updatedUser?.skillSet?.map { it.name } ?: emptyList()

            assert(skillNames.contains(SkillTag.ALGORITHMS))
            assert(skillNames.contains(SkillTag.DATABASES))
            assert(skillNames.contains(SkillTag.DIGITAL_LOGIC))
            assert(skillNames.contains(SkillTag.PHYSICS_MECHANICS))
        }
    }
}
