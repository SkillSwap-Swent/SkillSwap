/** @author Topaze17 (ELiott) Huge help from ChatGPT to make it work correctly */
package com.swent.skillswap.auth

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import com.swent.skillswap.model.chat.ChatRepositoryFirestore
import com.swent.skillswap.model.feed.*
import com.swent.skillswap.model.post.PostFirestoreRepository
import com.swent.skillswap.model.post.PostType
import com.swent.skillswap.model.tags.SkillTag
import com.swent.skillswap.model.user.UserRepoFirestore
import com.swent.skillswap.ui.auth.*
import com.swent.skillswap.ui.auth.CreateAccountViewModel
import com.swent.skillswap.ui.auth.SignInViewModel
import com.swent.skillswap.ui.chat.ChatListScreen
import com.swent.skillswap.ui.feed.*
import com.swent.skillswap.ui.navigation.NavigationActions
import com.swent.skillswap.ui.navigation.Screen
import com.swent.skillswap.ui.user.ProfileScreen
import com.swent.skillswap.utils.FirebaseEmulator
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.*
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters

@RunWith(AndroidJUnit4::class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class AuthClassicTest : TestCase() {

    @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private lateinit var vmCreateAccount: CreateAccountViewModel
    private lateinit var vmSignIn: SignInViewModel
    private val email = "classic.user@example.com"
    private val password = "PasswordA1" // >= 8, contains uppercase
    private val username = "classicUser"

    companion object {
        @JvmStatic lateinit var auth: com.google.firebase.auth.FirebaseAuth
        @JvmStatic lateinit var firestore: com.google.firebase.firestore.FirebaseFirestore

        @BeforeClass
        @JvmStatic
        fun globalSetUp() {
            FirebaseEmulator.startEmulator()
            FirebaseEmulator.clearAuthEmulator()
            auth = FirebaseEmulator.auth
            firestore = FirebaseEmulator.firestore
        }

        @AfterClass
        @JvmStatic
        fun globalTearDown() {
            auth.signOut()
            FirebaseEmulator.clearAuthEmulator()
            FirebaseEmulator.clearFirestoreEmulator()
        }
    }

    @Before
    fun setUp() {
        // Initialize ViewModels after Firebase emulator is ready
        vmCreateAccount =
            CreateAccountViewModel(isGoogleAccount = false, auth = auth, db = firestore)
        vmSignIn = SignInViewModel(auth)

        // Build controller via helper (cleaner, reusable)
        val controller = createFeedController()

        // Compose app content
        composeTestRule.setContent {
            val navController: NavHostController = rememberNavController()
            val navigationActions = NavigationActions(navController)

            NavHost(
                navController = navController,
                startDestination = Screen.AuthMain.route,
                modifier = Modifier.fillMaxSize()
            ) {
                composable(Screen.AuthMain.route) {
                    AuthMainScreen(
                        goToCreateAccountScreen = {
                            navigationActions.navigateTo(Screen.CreateAccount)
                        },
                        goToMainScreen = { navigationActions.navigateTo(Screen.Feed) },
                        vm = vmSignIn
                    )
                }
                composable(Screen.CreateAccount.route) {
                    AuthCreateAccountScreen(
                        goToMainScreen = { navigationActions.navigateTo(Screen.Feed) },
                        googleAccount = false,
                        vm = vmCreateAccount
                    )
                }
                composable(Screen.Feed.route) {
                    val factory =
                        FeedScreenViewModelFactory(
                            navigation = { uid -> navController.navigate("profile/$uid") },
                            controller = controller
                        )
                    val vm: FeedScreenViewModel = viewModel(factory = factory)
                    FeedScreen(vm = vm)
                }
                composable(Screen.Chat.route) { ChatListScreen() }
                composable(Screen.Profile.route) { ProfileScreen() }
            }
        }
    }

    @After
    fun tearDown() {
        auth.signOut()
        FirebaseEmulator.clearAuthEmulator()
        FirebaseEmulator.clearFirestoreEmulator()
    }

    /**
     * Classic NEW user:
     * - Tap "Create Account"
     * - Fill username, email, password + confirm, select one skill
     * - Next -> navigates to Offers
     */
    @Test
    fun t1_classicNewUser_createsAccount_andNavigatesToOffers() {

        // Go to Create Account screen
        composeTestRule.waitUntil(30_000L) {
            try {
                composeTestRule.onNodeWithTag(SignInTags.CREATE_ACCOUNT_TEXT).assertExists()
                true
            } catch (e: AssertionError) {
                false
            }
        }
        composeTestRule.onNodeWithTag(SignInTags.CREATE_ACCOUNT_TEXT).performScrollTo()
        composeTestRule.waitUntil(5_000L) {
            try {
                composeTestRule.onNodeWithTag(SignInTags.CREATE_ACCOUNT_TEXT).assertIsDisplayed()
                true
            } catch (e: AssertionError) {
                false
            }
        }
        composeTestRule.onNodeWithTag(SignInTags.CREATE_ACCOUNT_TEXT).performClick()

        // USERNAME
        composeTestRule.waitUntil(10_000L) {
            composeTestRule
                .onAllNodesWithTag(CreateAccountTags.USERNAME_FIELD)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeTestRule.onNodeWithTag(CreateAccountTags.USERNAME_FIELD).performScrollTo()
        composeTestRule.onNodeWithTag(CreateAccountTags.USERNAME_FIELD).performTextInput(username)
        composeTestRule.onNodeWithTag(CreateAccountTags.NEXT_BUTTON).performClick()

        // EMAIL
        composeTestRule.waitUntil(5000) {
            try {
                composeTestRule.onNodeWithTag(CreateAccountTags.EMAIL_FIELD).assertExists()
                true
            } catch (e: Exception) {
                false
            }
        }
        composeTestRule.onNodeWithTag(CreateAccountTags.EMAIL_FIELD).performScrollTo()
        composeTestRule.onNodeWithTag(CreateAccountTags.EMAIL_FIELD).performTextInput(email)
        composeTestRule.onNodeWithTag(CreateAccountTags.NEXT_BUTTON).performClick()

        // PASSWORD + CONFIRM
        composeTestRule.waitUntil(5000) {
            try {
                composeTestRule.onNodeWithTag(CreateAccountTags.PASSWORD_FIELD).assertExists()
                true
            } catch (e: Exception) {
                false
            }
        }
        composeTestRule.onNodeWithTag(CreateAccountTags.PASSWORD_FIELD).performScrollTo()
        composeTestRule.onNodeWithTag(CreateAccountTags.PASSWORD_FIELD).performTextInput(password)
        composeTestRule
            .onNodeWithTag(CreateAccountTags.CONFIRM_PASSWORD_FIELD)
            .performTextInput(password)
        composeTestRule.onNodeWithTag(CreateAccountTags.NEXT_BUTTON).performClick()

        // SKILLS
        composeTestRule.waitUntil(5000) {
            try {
                composeTestRule.onNodeWithTag(CreateAccountTags.SKILLS_FLOW).assertExists()
                true
            } catch (e: Exception) {
                false
            }
        }
        composeTestRule.onNodeWithTag(CreateAccountTags.SKILLS_FLOW).performScrollTo()
        composeTestRule
            .onNodeWithTag(
                CreateAccountTags.SKILL_CHIP_PREFIX + SkillTag.MACHINE_DESIGN.name,
                useUnmergedTree = true
            )
            .performScrollTo()
        composeTestRule
            .onNodeWithTag(
                CreateAccountTags.SKILL_CHIP_PREFIX + SkillTag.MACHINE_DESIGN.name,
                useUnmergedTree = true
            )
            .performClick()
        composeTestRule.onNodeWithTag(CreateAccountTags.NEXT_BUTTON).performClick()

        // Arrive at Offers
        composeTestRule.waitUntil(timeoutMillis = 30_000L) {
            composeTestRule
                .onAllNodesWithTag(FeedScreenTestTags.NO_OFFER_TEXT)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeTestRule.onNodeWithTag(FeedScreenTestTags.NO_OFFER_TEXT).assertIsDisplayed()
    }

    /**
     * Classic RETURNING user:
     * - Pre-seed Auth user + Firestore profile
     * - Fill email & password and tap "SIGN IN"
     * - Should land directly on Offers (no Create Account flow)
     */
    @Test
    fun t2_classicUser_can_log_on() {
        // Seed returning user (Auth + Firestore) on emulator
        runBlocking {
            try {
                auth.createUserWithEmailAndPassword(email, password).await()
            } catch (_: Exception) {
                // If it already exists, ignore
            }
            // Get UID (sign in temporarily to obtain it)
            val signIn = auth.signInWithEmailAndPassword(email, password).await()
            val uid = requireNotNull(signIn.user?.uid)
            // Ensure Firestore profile exists (what your app checks to skip Create Account)
            val userDoc =
                mapOf(
                    "username" to username,
                    "email" to email,
                    "skills" to listOf(SkillTag.MACHINE_DESIGN.toString())
                )
            firestore.collection("users").document(uid).set(userDoc).await()
            auth.signOut()
        }

        // Fill email/password on SignInMainScreen and sign in
        composeTestRule.onNodeWithTag(SignInTags.EMAIL_FIELD).performScrollTo()
        composeTestRule.onNodeWithTag(SignInTags.EMAIL_FIELD).performTextInput(email)
        composeTestRule.onNodeWithTag(SignInTags.PASSWORD_FIELD).performTextInput(password)
        composeTestRule.onNodeWithTag(SignInTags.SIGN_IN_BUTTON).performScrollTo()
        composeTestRule.onNodeWithTag(SignInTags.SIGN_IN_BUTTON).assertIsDisplayed()
        composeTestRule.onNodeWithTag(SignInTags.SIGN_IN_BUTTON).performClick()

        // Arrive at Offers
        composeTestRule.waitUntil(timeoutMillis = 10_000L) {
            composeTestRule
                .onAllNodesWithTag(FeedScreenTestTags.NO_OFFER_TEXT)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeTestRule.onNodeWithTag(FeedScreenTestTags.NO_OFFER_TEXT).assertIsDisplayed()
        auth.signOut()
    }

    /**
     * Helper to create a FeedController for tests. Uses runBlocking since tests are not on the UI
     * thread.
     */
    private fun createFeedController(): FeedController = runBlocking {
        FeedControllerFactory(
                recommendationEngine = RecommendationEngineImpl(),
                thumbnailRepository = ThumbnailRepository(),
                postRepository = PostFirestoreRepository(FirebaseEmulator.firestore),
                chatRepository = ChatRepositoryFirestore(FirebaseEmulator.firestore),
                userRepository = UserRepoFirestore(FirebaseEmulator.firestore),
                locationManager = null
            )
            .create(
                userIdPerformingActions = FirebaseEmulator.auth.uid ?: "AnoUser",
                feedType = PostType.REQUEST
            )
    }
}
