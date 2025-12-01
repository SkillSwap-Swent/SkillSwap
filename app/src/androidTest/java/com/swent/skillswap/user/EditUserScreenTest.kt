/**
 * @author Léonard MARTI 394185 /!\ Written with help of Copilot /!\
 * @author Topaze17 used chatGPT to make adjustement with new screen
 * > helped me write the general structure of tests, firebase emulator initialization, commented the
 * > code
 */
package com.swent.skillswap.user

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import com.swent.skillswap.firebase.CloudReferences.PROFILE_PICTURES_PATH
import com.swent.skillswap.model.images.PictureRepository
import com.swent.skillswap.model.tags.SkillTag
import com.swent.skillswap.model.user.Skill
import com.swent.skillswap.model.user.User
import com.swent.skillswap.model.user.UserRepoFirestore
import com.swent.skillswap.resources.theme.SkillSwapAppTheme
import com.swent.skillswap.ui.user.editUser.EditUserScreen
import com.swent.skillswap.ui.user.editUser.EditUserTags
import com.swent.skillswap.ui.user.editUser.EditUserViewModel
import com.swent.skillswap.utils.FirebaseEmulator
import com.swent.skillswap.utils.FirebaseEmulator.storage
import java.io.File
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.After
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
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
    private lateinit var storageRepo: PictureRepository
    private lateinit var viewModel: EditUserViewModel

    private val testUser =
        User(
            uid = "test-user-123",
            username = "Chef",
            email = "test@example.com",
            profilePicture = "",
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
        storageRepo = PictureRepository(storage)
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
        viewModel = EditUserViewModel(repo, storageRepo)
        viewModel.loadCurrentUser()
    }

    @After
    fun tearDown() = runBlocking {
        /** Clean all emulators */
        FirebaseEmulator.clearAuthEmulator()
        FirebaseEmulator.clearFirestoreEmulator()

        /** Clean up storage manually */
        val storageRef = storage.reference.child(PROFILE_PICTURES_PATH)
        val listResult = storageRef.listAll().await()
        for (item in listResult.items) {
            item.delete().await()
        }
    }

    /**
     * HELPER FUNCTION Generates a test image Uri by creating a Bitmap, saving it to a temporary
     * file
     *
     * @return Uri of the generated test image
     *
     *   Ai-generated code
     */
    fun generateTestImageUri(
        context: Context,
        width: Int = 100,
        height: Int = 100,
        color: Int = Color.RED
    ): Uri {
        /** Create a bitmap */
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(color)

        /** Create a temporary file to hold the bitmap */
        val tempFile = File(context.cacheDir, "test_image_${System.currentTimeMillis()}.png")
        tempFile.outputStream().use { out -> bitmap.compress(Bitmap.CompressFormat.PNG, 100, out) }

        return Uri.fromFile(tempFile)
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
            assertNull(viewModel.uiState.value.usernameError)
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
    fun validateButtonPerformsUserUpdate() = run {
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

        composeTestRule.waitForIdle()
        Thread.sleep(5000)

        // Check that UI state is updated
        assertEquals("UpdatedChef", viewModel.uiState.value.editedUser!!.username)

        // Click validate button
        composeTestRule.onNodeWithTag(EditUserTags.VALIDATE_BUTTON).performScrollTo().performClick()

        // Wait for save operation to complete
        composeTestRule.waitUntil(timeoutMillis = 5000) { viewModel.uiState.value.isSaved }

        assertNotNull(Firebase.auth.currentUser)

        step("Verify user data is updated in repository") {
            runBlocking {
                val editedUser = repo.getUser(Firebase.auth.currentUser!!.uid)

                assertEquals("UpdatedChef", editedUser.username)
            }
        }
    }

    @Test
    fun deleteProfilePictureRemoveURLFromBothRepoAndStorageAndDisplayDefaultProfilePicture() = run {
        composeTestRule.setContent {
            SkillSwapAppTheme { EditUserScreen(vm = viewModel, onGoBack = {}) }
        }

        /** Wait for user to be loaded */
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            viewModel.uiState.value.editedUser != null
        }

        /** Simulate selecting a new profile picture */
        val uri = generateTestImageUri(ctx)
        viewModel.onSelectedProfilePicture(uri)

        /** Wait for the profile picture URI to be updated in the UI state */
        composeTestRule.waitUntil(timeoutMillis = 10_000) {
            (viewModel.uiState.value.editedUser!!.profilePicture != "" &&
                !viewModel.uiState.value.isLoading)
        }

        /** Check precondition for validate function */
        assertTrue(viewModel.uiState.value.usernameError == null)
        assertTrue(viewModel.uiState.value.emailError == null)
        assertTrue(viewModel.uiState.value.profilePictureError == null)
        assertTrue(viewModel.uiState.value.skillSetError == null)
        assertTrue(viewModel.uiState.value.ratingError == null)
        assertTrue(viewModel.uiState.value.availabilityError == null)
        assertTrue(!viewModel.uiState.value.isLoading)

        /** Validate profile picture upload */
        composeTestRule.onNodeWithTag(EditUserTags.VALIDATE_BUTTON).performScrollTo().performClick()

        /** Wait for save operation to complete */
        composeTestRule.waitUntil(timeoutMillis = 20_000) {
            runBlocking {
                repo.getUser(viewModel.uiState.value.editedUser!!.uid).profilePicture.isNotEmpty()
            }
        }

        /** check that profile picture is set in firebase */
        val userAfterPictureSet = runBlocking {
            repo.getUser(viewModel.uiState.value.editedUser!!.uid)
        }
        assertTrue(userAfterPictureSet.profilePicture.isNotEmpty())

        /** delete the profile picture */
        composeTestRule
            .onNodeWithTag(EditUserTags.DELETE_PROFILE_PICTURE)
            .performScrollTo()
            .performClick()

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag(EditUserTags.VALIDATE_BUTTON).performScrollTo().performClick()

        /** Wait for save operation to complete */
        composeTestRule.waitUntil(timeoutMillis = 10_000) {
            runBlocking {
                repo.getUser(viewModel.uiState.value.editedUser!!.uid).profilePicture == ""
            }
        }

        /** Verify that the profile picture URL is removed from the user repository and storage */
        runBlocking {
            val editedUserFromRepo = repo.getUser(viewModel.uiState.value.editedUser!!.uid)
            assertTrue(editedUserFromRepo.profilePicture == "")

            /** Verify that the picture is removed from storage */
            val pictureName = viewModel.uiState.value.editedUser!!.uid

            // Attempting to get the download URL should throw an exception if the picture has been
            // deleted
            assertThrows(com.google.firebase.storage.StorageException::class.java) {
                runBlocking {
                    val storageRef =
                        storage.reference.child(PROFILE_PICTURES_PATH).child(pictureName)
                    storageRef.downloadUrl.await()
                }
            }
        }

        /** Verify that the default profile picture is displayed in the UI */
        composeTestRule
            .onNodeWithTag(EditUserTags.PROFILE_PICTURE)
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun testGeneralErrorMessageIsDisplayed() = run {
        step("Display EditUserScreen") {
            composeTestRule.setContent {
                SkillSwapAppTheme { EditUserScreen(vm = viewModel, onGoBack = {}) }
            }
        }

        step("Wait for user to load") {
            composeTestRule.waitUntil(timeoutMillis = 5000) {
                viewModel.uiState.value.editedUser != null
            }
        }

        step("Set a general error in the ViewModel") {
            // Simulate an error occurring in the VM
            viewModel.setGeneralError("Something went wrong")
            composeTestRule.waitForIdle()
        }

        step("Verify the error message is displayed") {
            composeTestRule
                .onNodeWithTag(EditUserTags.GENERAL_ERROR)
                .assertIsDisplayed()
                .assert(hasText("Something went wrong"))
        }
    }

    @Test
    fun profilePictureIsUpdatedAndStoredWhenValidURIisProvided() = run {
        step("Display EditUserScreen") {
            composeTestRule.setContent {
                SkillSwapAppTheme { EditUserScreen(vm = viewModel, onGoBack = {}) }
            }
        }

        /** generate a test image URI */
        val uri = generateTestImageUri(ctx)

        /** Simulate selecting a new profile picture */
        viewModel.onSelectedProfilePicture(uri)

        /** Wait for the profile picture URI to be updated in the UI state */
        composeTestRule.waitUntil(timeoutMillis = 10_000) {
            (!(viewModel.uiState.value.isLoading) &&
                viewModel.uiState.value.editedUser!!.profilePicture.isNotEmpty())
        }

        /** Verify that the profile picture URI is stored in storage */
        runBlocking {
            val pictureName = viewModel.uiState.value.editedUser!!.uid
            val storageRef = storage.reference.child(PROFILE_PICTURES_PATH).child(pictureName)
            val downloadUrl = storageRef.downloadUrl.await()
            assertTrue(downloadUrl.toString().isNotEmpty())
            assertEquals(
                downloadUrl.toString(),
                viewModel.uiState.value.editedUser!!.profilePicture
            )
        }

        /** Wait for UI to display profile picture... */
        composeTestRule.waitForIdle()
        composeTestRule.waitUntil(timeoutMillis = 10_000) {
            try {
                composeTestRule.onNodeWithTag(EditUserTags.PROFILE_PICTURE).assertExists()
                true
            } catch (_: AssertionError) {
                false
            }
        }
    }

    @Test
    fun selectWrongMediaSetProfilePictureErrorAndDoesNothing() {
        /** Wait for user to be loaded */
        composeTestRule.setContent {
            SkillSwapAppTheme { EditUserScreen(vm = viewModel, onGoBack = {}) }
        }

        composeTestRule.waitUntil(timeoutMillis = 5000) {
            viewModel.uiState.value.editedUser != null
        }

        val wrongURI = Uri.parse("ftp://invalid_uri.com/image.png")
        viewModel.onSelectedProfilePicture(wrongURI)

        /** wait until isLoading is false */
        composeTestRule.waitUntil(timeoutMillis = 10_000) {
            viewModel.uiState.value.profilePictureError != null
        }

        assertTrue(viewModel.uiState.value.profilePictureError != null)

        /** Check storage to ensure no picture was uploaded */
        runBlocking {
            val pictureName = viewModel.uiState.value.editedUser!!.uid
            val storageRef = storage.reference.child(PROFILE_PICTURES_PATH).child(pictureName)
            assertThrows(com.google.firebase.storage.StorageException::class.java) {
                runBlocking { storageRef.downloadUrl.await() }
            }
        }
    }

    @Test
    fun selectNoMediaThrowException() {
        assertThrows(IllegalArgumentException::class.java) {
            viewModel.onSelectedProfilePicture(Uri.parse(""))
        }
    }
}
