package com.swent.skillswap.user

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import com.swent.skillswap.model.tags.SkillTag
import com.swent.skillswap.model.user.Skill
import com.swent.skillswap.model.user.User
import com.swent.skillswap.model.user.UserRepoFirestore
import com.swent.skillswap.ui.theme.SkillSwapAppTheme
import com.swent.skillswap.ui.user.SkillsEditScreen
import com.swent.skillswap.ui.user.SkillsEditTestTags
import com.swent.skillswap.utils.FirebaseEmulator
import com.swent.skillswap.viewModel.EditUserViewModel
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

        // Initialize FirebaseApp if necessary (useful for UI component runtime)
        try {
            if (FirebaseApp.getApps(ctx).isEmpty()) {
                FirebaseApp.initializeApp(ctx)
            }
        } catch (e: Exception) {
            // Ignore if already initialized or if initialization fails in emulator test
        }

        // Auth: create / sign in a user on the emulator
        val auth = FirebaseAuth.getInstance()
        val testPassword = "test-password-123"
        try {
            // Try creating the user; if it exists, creation will throw and we'll sign in instead
            auth.createUserWithEmailAndPassword(testUser.email, testPassword).await()
        } catch (e: Exception) {
            // Ignore - user may already exist on emulator
        }
        // Ensure signed in
        try {
            auth.signInWithEmailAndPassword(testUser.email, testPassword).await()
        } catch (e: Exception) {
            // Fallback to anonymous sign-in if email sign-in failed
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
        viewModel = EditUserViewModel(repo)
    }

    @Test
    fun testEditUserScreenDisplayedBasicComponents() = run {
        step("Display SkillsEditScreen with real repository") {
            composeTestRule.setContent {
                SkillSwapAppTheme { SkillsEditScreen(vm = viewModel, onBackClick = {}) }
            }
        }

        step("Verify Edit User Screen elements are displayed") {
            composeTestRule.onNodeWithTag(SkillsEditTestTags.SCREEN_CONTAINER).assertIsDisplayed()
            composeTestRule.onNodeWithTag(SkillsEditTestTags.TITLE).assertIsDisplayed()
            composeTestRule.onNodeWithTag(SkillsEditTestTags.DROPDOWN).assertIsDisplayed()
            composeTestRule.onNodeWithTag(SkillsEditTestTags.SEARCH_FIELD).assertIsDisplayed()
            composeTestRule.onNodeWithTag(SkillsEditTestTags.SELECTED_COUNT).assertIsDisplayed()
            composeTestRule.onNodeWithTag(SkillsEditTestTags.SELECTED_LIST).assertIsDisplayed()
            composeTestRule.onNodeWithTag(SkillsEditTestTags.CANCEL_BUTTON).assertIsDisplayed()
            composeTestRule.onNodeWithTag(SkillsEditTestTags.SAVE_BUTTON).assertIsDisplayed()
        }

        Thread.sleep(1000)
    }

    @Test
    fun skillsEditScreen_removingSkillUpdatesViewModel() = run {
        step("Display SkillsEditScreen with real repository") {
            composeTestRule.setContent {
                SkillSwapAppTheme { SkillsEditScreen(vm = viewModel, onBackClick = {}) }
            }
        }

        step("Remove one skill from the user") {
            composeTestRule
                .onNodeWithTag("${SkillsEditTestTags.SKILL_CHIP_PREFIX}_${SkillTag.DATABASES.name}")
                .performClick()

            composeTestRule.onNodeWithTag(SkillsEditTestTags.SAVE_BUTTON).performClick()

            Thread.sleep(500)

            val updatedUser = viewModel.uiState.value.editedUser
            val skillNames = updatedUser?.skillSet?.map { it.name }

            assert(!skillNames?.contains(SkillTag.DATABASES)!!)
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
        }

        step("Add a new skill and verify ViewModel state") {
            composeTestRule.onNodeWithTag(SkillsEditTestTags.SEARCH_FIELD).performClick()
            composeTestRule
                .onNodeWithTag(SkillsEditTestTags.SEARCH_FIELD)
                .performTextInput("algorithms")

            Thread.sleep(500)

            composeTestRule
                .onNodeWithTag("${SkillsEditTestTags.SUGGESTION_ITEM_PREFIX}_0")
                .performClick()

            composeTestRule.onNodeWithTag(SkillsEditTestTags.SAVE_BUTTON).performClick()

            Thread.sleep(500)

            val updatedUser = viewModel.uiState.value.editedUser
            val skillNames = updatedUser?.skillSet?.map { it.name }

            assert(skillNames?.contains(SkillTag.ALGORITHMS)!!)
            assert(skillNames.contains(SkillTag.DATABASES))
            assert(skillNames.size == 4)
        }
    }

    @Test
    fun skillsEditScreen_removeAndAddMultipleSkillsUpdatesViewModel() = run {
        step("Display SkillsEditScreen") {
            composeTestRule.setContent {
                SkillSwapAppTheme { SkillsEditScreen(vm = viewModel, onBackClick = {}) }
            }
        }

        step("Remove and add multiple skills") {
            // Remove DATABASES and DIGITAL_LOGIC
            composeTestRule
                .onNodeWithTag("${SkillsEditTestTags.SKILL_CHIP_PREFIX}_${SkillTag.DATABASES.name}")
                .performClick()

            composeTestRule
                .onNodeWithTag(
                    "${SkillsEditTestTags.SKILL_CHIP_PREFIX}_${SkillTag.DIGITAL_LOGIC.name}"
                )
                .performClick()

            // Add ALGORITHMS
            composeTestRule.onNodeWithTag(SkillsEditTestTags.SEARCH_FIELD).performClick()
            composeTestRule
                .onNodeWithTag(SkillsEditTestTags.SEARCH_FIELD)
                .performTextInput("algorithms")

            Thread.sleep(500)

            composeTestRule
                .onNodeWithTag("${SkillsEditTestTags.SUGGESTION_ITEM_PREFIX}_0")
                .performClick()

            // Add MACHINE_DESIGN
            composeTestRule.onNodeWithTag(SkillsEditTestTags.SEARCH_FIELD).performClick()
            composeTestRule
                .onNodeWithTag(SkillsEditTestTags.SEARCH_FIELD)
                .performTextInput("machine")

            Thread.sleep(500)

            composeTestRule
                .onNodeWithTag("${SkillsEditTestTags.SUGGESTION_ITEM_PREFIX}_0")
                .performClick()

            composeTestRule.onNodeWithTag(SkillsEditTestTags.SAVE_BUTTON).performClick()

            Thread.sleep(500)

            val updatedUser = viewModel.uiState.value.editedUser
            val skillNames = updatedUser?.skillSet?.map { it.name }

            assert(!skillNames?.contains(SkillTag.DATABASES)!!)
            assert(!skillNames.contains(SkillTag.DIGITAL_LOGIC))
            assert(skillNames.contains(SkillTag.ALGORITHMS))
            assert(skillNames.contains(SkillTag.MACHINE_DESIGN))
            assert(skillNames.contains(SkillTag.PHYSICS_MECHANICS))
            assert(skillNames.size == 3)
        }
    }
}
