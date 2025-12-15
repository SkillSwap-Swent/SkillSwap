/** @author Topaze17(ELiott) huge help from chatGPT to make it work correctly */
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
import com.google.firebase.auth.GoogleAuthProvider
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import com.swent.skillswap.model.chat.ChatRepositoryFirestore
import com.swent.skillswap.model.feed.FeedControllerFactory
import com.swent.skillswap.model.feed.RecommendationEngineImpl
import com.swent.skillswap.model.feed.ThumbnailRepository
import com.swent.skillswap.model.post.PostFirestoreRepository
import com.swent.skillswap.model.post.PostType
import com.swent.skillswap.model.tags.SkillTag
import com.swent.skillswap.model.user.UserRepoFirestore
import com.swent.skillswap.ui.auth.AuthCreateAccountScreen
import com.swent.skillswap.ui.auth.AuthMainScreen
import com.swent.skillswap.ui.auth.CreateAccountTags
import com.swent.skillswap.ui.auth.CreateAccountViewModel
import com.swent.skillswap.ui.auth.SignInTags
import com.swent.skillswap.ui.auth.SignInViewModel
import com.swent.skillswap.ui.chat.ChatListScreen
import com.swent.skillswap.ui.feed.FeedScreen
import com.swent.skillswap.ui.feed.FeedScreenTestTags
import com.swent.skillswap.ui.feed.FeedScreenViewModel
import com.swent.skillswap.ui.feed.FeedScreenViewModelFactory
import com.swent.skillswap.ui.navigation.NavigationActions
import com.swent.skillswap.ui.navigation.Screen
import com.swent.skillswap.ui.user.ProfileScreen
import com.swent.skillswap.utils.FakeCredentialManager
import com.swent.skillswap.utils.FakeJwtGenerator
import com.swent.skillswap.utils.FirebaseEmulator
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.*
import org.junit.rules.TestName
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters

@RunWith(AndroidJUnit4::class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class AuthGoogleTest : TestCase() {

    @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()
    @get:Rule val testName = TestName()

    // Build AFTER emulator is configured
    private lateinit var repo: UserRepoFirestore
    private lateinit var vmCreateAccount: CreateAccountViewModel
    private lateinit var vmSignIn: SignInViewModel
    private lateinit var token: String

    // Use SAME email across tests so t2 is truly "returning user"
    private val email = "testy@example.com"
    private val displayName = "Testy McTestface"

    companion object {
        @JvmStatic lateinit var auth: com.google.firebase.auth.FirebaseAuth
        @JvmStatic lateinit var firestore: com.google.firebase.firestore.FirebaseFirestore

        @BeforeClass
        @JvmStatic
        fun globalSetUp() {
            FirebaseEmulator.startEmulator()
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
        // Build after emulator binding
        repo = UserRepoFirestore(firestore)
        vmCreateAccount = CreateAccountViewModel(true, auth, firestore)
        vmSignIn = SignInViewModel(auth)

        // Stable token for same user identity across tests
        token = FakeJwtGenerator.createFakeGoogleIdToken(name = displayName, email = email)

        // Ensure the Auth emulator has this Google user registered
        FirebaseEmulator.createGoogleUser(token)

        val controller = runBlocking {
            FeedControllerFactory(
                    recommendationEngine = RecommendationEngineImpl(),
                    thumbnailRepository = ThumbnailRepository(),
                    postRepository = PostFirestoreRepository(FirebaseEmulator.firestore),
                    chatRepository =
                        ChatRepositoryFirestore(
                            FirebaseEmulator.firestore,
                            PostFirestoreRepository(FirebaseEmulator.firestore)
                        ),
                    userRepository = UserRepoFirestore(FirebaseEmulator.firestore),
                    locationManager = null
                )
                .create(
                    userIdPerformingActions = FirebaseEmulator.auth.uid ?: "AnoUser",
                    feedType = PostType.REQUEST
                )
        }

        // If this is t2, we simulate "returning user" by pre-seeding the profile in Firestore
        if (testName.methodName == "t2_googleUser_can_log_on") {
            runBlocking {
                // Sign in briefly to get uid, write user doc, sign out again
                val cred = GoogleAuthProvider.getCredential(token, null)
                auth.signInWithCredential(cred).await()
                val uid = requireNotNull(auth.currentUser?.uid)

                val userDoc =
                    mapOf(
                        "username" to "testy",
                        "email" to email,
                        "skills" to listOf(SkillTag.MACHINE_DESIGN.toString())
                    )
                firestore.collection("users").document(uid).set(userDoc).await()
                auth.signOut()
            }
        }

        // Compose content
        composeTestRule.setContent {
            val navController: NavHostController = rememberNavController()
            val navigationActions = NavigationActions(navController)
            val credential = FakeCredentialManager.create(token)

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
                        vm = vmSignIn,
                        credentialManager = credential
                    )
                }
                composable(Screen.CreateAccount.route) {
                    AuthCreateAccountScreen(
                        goToMainScreen = { navigationActions.navigateTo(Screen.Feed) },
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

    /** New Google user → goes through Create Account (username + skill) → lands on Offers. */
    @Test
    fun t1_googleNewUser_completesCreateAccount_andNavigatesToOffers() {
        composeTestRule.onNodeWithTag(SignInTags.GOOGLE_BUTTON).performScrollTo()
        composeTestRule.onNodeWithTag(SignInTags.GOOGLE_BUTTON).assertIsDisplayed()
        composeTestRule.onNodeWithTag(SignInTags.GOOGLE_BUTTON).performClick()

        composeTestRule.waitUntil(10_000L) {
            composeTestRule
                .onAllNodesWithTag(CreateAccountTags.USERNAME_FIELD)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        composeTestRule.onNodeWithTag(CreateAccountTags.USERNAME_FIELD).performScrollTo()
        composeTestRule.onNodeWithTag(CreateAccountTags.USERNAME_FIELD).performTextInput("testy")
        composeTestRule.onNodeWithTag(CreateAccountTags.NEXT_BUTTON).performClick()

        composeTestRule.onNodeWithTag(CreateAccountTags.SKILLS_FLOW).performScrollTo()
        composeTestRule
            .onNodeWithTag(
                CreateAccountTags.SKILL_CHIP_PREFIX + SkillTag.MACHINE_DESIGN,
                useUnmergedTree = true
            )
            .performScrollTo()
        composeTestRule
            .onNodeWithTag(
                CreateAccountTags.SKILL_CHIP_PREFIX + SkillTag.MACHINE_DESIGN,
                useUnmergedTree = true
            )
            .performClick()

        composeTestRule.onNodeWithTag(CreateAccountTags.NEXT_BUTTON).performClick()
        composeTestRule.waitUntil(timeoutMillis = 10_000L) {
            composeTestRule
                .onAllNodesWithTag(FeedScreenTestTags.NO_OFFER_TEXT)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeTestRule.onNodeWithTag(FeedScreenTestTags.NO_OFFER_TEXT).assertIsDisplayed()
    }

    /** Returning Google user (profile already exists) → straight to Offers after sign-in. */
    @Test
    fun t2_googleUser_can_log_on() {
        composeTestRule.onNodeWithTag(SignInTags.GOOGLE_BUTTON).performScrollTo()
        composeTestRule.onNodeWithTag(SignInTags.GOOGLE_BUTTON).assertIsDisplayed()
        composeTestRule.onNodeWithTag(SignInTags.GOOGLE_BUTTON).performClick()

        composeTestRule.waitUntil(timeoutMillis = 10_000L) {
            composeTestRule
                .onAllNodesWithTag(FeedScreenTestTags.NO_OFFER_TEXT)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeTestRule.onNodeWithTag(FeedScreenTestTags.NO_OFFER_TEXT).assertIsDisplayed()
    }
}
