/**
 * @author Léonard MARTI 394185 /!\ Written with help of Copilot /!\
 * @author Topaze17 used chatGPT to make adjustement with new screen
 * > helped me write the general structure of tests, firebase emulator initialization, commented the
 * > code
 */
package com.swent.skillswap.user

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import com.swent.skillswap.model.tags.SkillTag
import com.swent.skillswap.model.user.Skill
import com.swent.skillswap.model.user.User
import com.swent.skillswap.model.user.UserRepoFirestore
import com.swent.skillswap.ui.editUser.EditUserScreen
import com.swent.skillswap.ui.editUser.EditUserTags
import com.swent.skillswap.ui.editUser.EditUserViewModel
import com.swent.skillswap.ui.theme.SkillSwapAppTheme
import com.swent.skillswap.utils.FirebaseEmulator
import junit.framework.TestCase.assertNotNull
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test for EditUserScreen. Tests the edit profile functionality with Firebase
 * emulator.
 */
@RunWith(AndroidJUnit4::class)
class EditUserScreenTest : TestCase() {

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
        viewModel.loadCurrentUser()
    }

    @Test
    fun testEditUserScreenDisplayedBasicComponents() = run {
        step("Display EditUserScreen with real repository") {
            composeTestRule.setContent {
                SkillSwapAppTheme { EditUserScreen(vm = viewModel, onGoBack = {}) }
            }
        }

        step("Verify Edit User Screen elements are displayed") {
            // Scroll before each assertIsDisplayed
            composeTestRule
                .onNodeWithTag(EditUserTags.GO_BACK_BUTTON)
                .performScrollTo()
                .assertIsDisplayed()

            composeTestRule
                .onNodeWithTag(EditUserTags.PROFILE_PICTURE)
                .performScrollTo()
                .assertIsDisplayed()

            composeTestRule
                .onNodeWithTag(EditUserTags.USERNAME_TEXTFIELD)
                .performScrollTo()
                .assertIsDisplayed()

            composeTestRule
                .onNodeWithTag(EditUserTags.VALIDATE_BUTTON)
                .performScrollTo()
                .assertIsDisplayed()
        }

        Thread.sleep(7000)
    }

    @Test
    fun testUsernameFieldWithValidUsername() = run {
        step("Display screen") {
            composeTestRule.setContent {
                SkillSwapAppTheme { EditUserScreen(vm = viewModel, onGoBack = {}) }
            }
        }

        step("Test username field validation") {
            // Wait for user to be loaded
            composeTestRule.waitUntil(timeoutMillis = 5000) {
                viewModel.uiState.value.editedUser != null
            }

            // Make sure the field is visible
            composeTestRule
                .onNodeWithTag(EditUserTags.USERNAME_TEXTFIELD)
                .performScrollTo()
                .performTextClearance()

            // Enter valid username
            composeTestRule
                .onNodeWithTag(EditUserTags.USERNAME_TEXTFIELD)
                .performTextInput("LeLaitier")

            composeTestRule.waitForIdle()

            // Verify error disappears
            assert(viewModel.uiState.value.usernameError == null)
        }
    }

    @Test
    fun testUsernameFieldWithEmptyUsername() = run {
        step("Display screen") {
            composeTestRule.setContent {
                SkillSwapAppTheme { EditUserScreen(vm = viewModel, onGoBack = {}) }
            }
        }

        step("Test username field validation") {
            // Wait for user to be loaded
            composeTestRule.waitUntil(timeoutMillis = 5000) {
                viewModel.uiState.value.editedUser != null
            }

            // Make sure the field is visible
            composeTestRule
                .onNodeWithTag(EditUserTags.USERNAME_TEXTFIELD)
                .performScrollTo()
                .performTextClearance()

            // Enter invalid username (only spaces)
            composeTestRule.onNodeWithTag(EditUserTags.USERNAME_TEXTFIELD).performTextInput("   ")

            composeTestRule.waitForIdle()

            // Verify error appears
            assertNotNull(viewModel.uiState.value.usernameError)
        }
    }

    @Test
    fun testGoBackButtonIsClickable() = run {
        step("Display screen") {
            composeTestRule.setContent {
                SkillSwapAppTheme { EditUserScreen(vm = viewModel, onGoBack = {}) }
            }
        }

        step("Test go back button is clickable") {
            composeTestRule
                .onNodeWithTag(EditUserTags.GO_BACK_BUTTON)
                .performScrollTo()
                .assertIsDisplayed()
                .assertHasClickAction()
                .performClick()
        }
    }

    @Test
    fun testProfilePictureIsDisplayed() = run {
        step("Display screen") {
            composeTestRule.setContent {
                SkillSwapAppTheme { EditUserScreen(vm = viewModel, onGoBack = {}) }
            }
        }

        step("Test profile picture container is displayed") {
            // Wait for user to be loaded
            composeTestRule.waitUntil(timeoutMillis = 5000) {
                viewModel.uiState.value.editedUser != null
            }

            // Box with tag PROFILE_PICTURE is not clickable in current UI, so we only assert
            // display
            composeTestRule
                .onNodeWithTag(EditUserTags.PROFILE_PICTURE)
                .performScrollTo()
                .assertIsDisplayed()
        }
    }

    @Test
    fun validateButtonPerformsUserUpdate() = runBlocking {
        composeTestRule.setContent {
            SkillSwapAppTheme { EditUserScreen(vm = viewModel, onGoBack = {}) }
        }

        // Wait for user to be loaded
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            viewModel.uiState.value.editedUser != null
        }

        // Update username field
        composeTestRule
            .onNodeWithTag(EditUserTags.USERNAME_TEXTFIELD)
            .performScrollTo()
            .performTextClearance()

        composeTestRule
            .onNodeWithTag(EditUserTags.USERNAME_TEXTFIELD)
            .performTextInput("UpdatedChef")

        composeTestRule
            .onNodeWithTag(EditUserTags.PROFILE_PICTURE_TEXTFIELD)
            .performTextInput("https://images.voicy.network/Content/Clips/Images/3bb25d87-2d2d-4f93-b3d1-01f310f81aeb-small.png")

        // Check that UI state is updated
        assert(viewModel.uiState.value.editedUser!!.username == "UpdatedChef")

        // Click validate button
        composeTestRule.onNodeWithTag(EditUserTags.VALIDATE_BUTTON).performScrollTo().performClick()

        // Wait for save operation to complete
        composeTestRule.waitUntil(timeoutMillis = 5000) { viewModel.uiState.value.isSaved }

        assertNotNull(Firebase.auth.currentUser)

        val editedUser = repo.getUser(Firebase.auth.currentUser!!.uid)

        assert(editedUser.username == "UpdatedChef")
        assert(editedUser.profilePicture == "https://images.voicy.network/Content/Clips/Images/3bb25d87-2d2d-4f93-b3d1-01f310f81aeb-small.png")

        composeTestRule.onNodeWithTag(EditUserTags.PROFILE_PICTURE_CONTENT).performScrollTo().assertIsDisplayed()
    }
}
