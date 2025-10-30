/**
 * @author Léonard MARTI 394185 /!\ Written with help of Copilot /!\
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
import com.swent.skillswap.model.user.User
import com.swent.skillswap.model.user.UserRepoFirestore
import com.swent.skillswap.ui.editUser.EditUserScreen
import com.swent.skillswap.ui.editUser.EditUserTags
import com.swent.skillswap.ui.theme.SkillSwapAppTheme
import com.swent.skillswap.utils.FirebaseEmulator
import com.swent.skillswap.utils.FirebaseEmulator.auth
import com.swent.skillswap.viewModel.EditUserViewModel
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
            profilePicture = "",
            skillSet = emptySet(),
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
        step("Display EditUserScreen with real repository") {
            composeTestRule.setContent {
                SkillSwapAppTheme { EditUserScreen(vm = viewModel, onGoBack = {}) }
            }
        }

        step("Verify Edit User Screen elements are displayed") {
            composeTestRule.onNodeWithTag(EditUserTags.GO_BACK_BUTTON).assertIsDisplayed()
            composeTestRule.onNodeWithTag(EditUserTags.PROFILE_PICTURE).assertIsDisplayed()
            composeTestRule.onNodeWithTag(EditUserTags.USERNAME_TEXTFIELD).assertIsDisplayed()
            composeTestRule.onNodeWithTag(EditUserTags.EMAIL_TEXTFIELD).assertIsDisplayed()
            composeTestRule.onNodeWithTag(EditUserTags.SKILLSET_SECTION).assertIsDisplayed()
            composeTestRule.onNodeWithTag(EditUserTags.VALIDATE_BUTTON).assertIsDisplayed()
        }
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

            // Clear username field
            composeTestRule.onNodeWithTag(EditUserTags.USERNAME_TEXTFIELD).performTextClearance()

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

            // Clear username field
            composeTestRule.onNodeWithTag(EditUserTags.USERNAME_TEXTFIELD).performTextClearance()

            // Enter invalid username (only spaces)
            composeTestRule.onNodeWithTag(EditUserTags.USERNAME_TEXTFIELD).performTextInput("   ")

            composeTestRule.waitForIdle()

            // Verify error appears
            assertNotNull(viewModel.uiState.value.usernameError)
        }
    }

    @Test
    fun testEmailFieldWithValidEmail() = run {
        step("Display screen") {
            composeTestRule.setContent {
                SkillSwapAppTheme { EditUserScreen(vm = viewModel, onGoBack = {}) }
            }
        }

        step("Test email field validation with valid email") {
            // Wait for user to be loaded
            composeTestRule.waitUntil(timeoutMillis = 5000) {
                viewModel.uiState.value.editedUser != null
            }

            // Clear email field
            composeTestRule.onNodeWithTag(EditUserTags.EMAIL_TEXTFIELD).performTextClearance()

            // Enter valid email
            composeTestRule
                .onNodeWithTag(EditUserTags.EMAIL_TEXTFIELD)
                .performTextInput("valid.email@example.com")

            composeTestRule.waitForIdle()

            // Verify error disappears
            assert(viewModel.uiState.value.emailError == null)
        }
    }

    @Test
    fun testEmailFieldWithInvalidEmail() = run {
        step("Display screen") {
            composeTestRule.setContent {
                SkillSwapAppTheme { EditUserScreen(vm = viewModel, onGoBack = {}) }
            }
        }

        step("Test email field validation with invalid email") {
            // Wait for user to be loaded
            composeTestRule.waitUntil(timeoutMillis = 5000) {
                viewModel.uiState.value.editedUser != null
            }

            // Clear email field
            composeTestRule.onNodeWithTag(EditUserTags.EMAIL_TEXTFIELD).performTextClearance()

            // Enter invalid email
            composeTestRule
                .onNodeWithTag(EditUserTags.EMAIL_TEXTFIELD)
                .performTextInput("invalid-email")

            composeTestRule.waitForIdle()

            // Verify error appears
            assertNotNull(viewModel.uiState.value.emailError)
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
                .assertIsDisplayed()
                .assertHasClickAction()
                .performClick()
        }
    }

    @Test
    fun testProfilePictureIsClickable() = run {
        step("Display screen") {
            composeTestRule.setContent {
                SkillSwapAppTheme { EditUserScreen(vm = viewModel, onGoBack = {}) }
            }
        }

        step("Test profile picture is clickable") {
            // Wait for user to be loaded
            composeTestRule.waitUntil(timeoutMillis = 5000) {
                viewModel.uiState.value.editedUser != null
            }

            composeTestRule
                .onNodeWithTag(EditUserTags.PROFILE_PICTURE)
                .assertIsDisplayed()
                .assertHasClickAction()
                .performClick()
        }
    }

    @Test
    fun testSkillsetSectionIsClickable() = run {
        step("Display screen") {
            composeTestRule.setContent {
                SkillSwapAppTheme { EditUserScreen(vm = viewModel, onGoBack = {}) }
            }
        }

        step("Test skillset section is clickable") {
            // Wait for user to be loaded
            composeTestRule.waitUntil(timeoutMillis = 5000) {
                viewModel.uiState.value.editedUser != null
            }

            composeTestRule
                .onNodeWithTag(EditUserTags.SKILLSET_SECTION)
                .assertIsDisplayed()
                .assertHasClickAction()
                .performClick()
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

        // Update username and email fields
        composeTestRule.onNodeWithTag(EditUserTags.USERNAME_TEXTFIELD).performTextClearance()

        composeTestRule
            .onNodeWithTag(EditUserTags.USERNAME_TEXTFIELD)
            .performTextInput("UpdatedChef")

        composeTestRule.onNodeWithTag(EditUserTags.EMAIL_TEXTFIELD).performTextClearance()

        composeTestRule
            .onNodeWithTag(EditUserTags.EMAIL_TEXTFIELD)
            .performTextInput("gladal@barbeuc.com")

        // Check that UI state is updated
        assert(viewModel.uiState.value.editedUser!!.username == "UpdatedChef")
        assert(viewModel.uiState.value.editedUser!!.email == "gladal@barbeuc.com")

        // Click validate button
        composeTestRule.onNodeWithTag(EditUserTags.VALIDATE_BUTTON).performClick()

        // Wait for save operation to complete
        composeTestRule.waitUntil(timeoutMillis = 5000) { viewModel.uiState.value.isSaved }

        assertNotNull(Firebase.auth.currentUser)

        val editedUser = repo.getUser(Firebase.auth.currentUser!!.uid)

        assert(editedUser.username == "UpdatedChef")
        assert(editedUser.email == "gladal@barbeuc.com")
    }
}
