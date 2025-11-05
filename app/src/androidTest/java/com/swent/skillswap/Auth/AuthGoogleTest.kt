/** @author Topaze17(ELiott) huge help from chatGPT to make it work correctly */
package com.swent.skillswap.Auth

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
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.auth.GoogleAuthProvider
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import com.swent.skillswap.model.tags.SkillTag
import com.swent.skillswap.model.user.UserRepoFirestore
import com.swent.skillswap.ui.Auth.CreateAccountTags
import com.swent.skillswap.ui.Auth.SignInCreateAccountScreen
import com.swent.skillswap.ui.Auth.SignInMainScreen
import com.swent.skillswap.ui.Auth.SignInTags
import com.swent.skillswap.ui.chat.ChatListScreen
import com.swent.skillswap.ui.feedScreen.FeedScreen
import com.swent.skillswap.ui.feedScreen.FeedScreenTestTags
import com.swent.skillswap.ui.navigation.NavigationActions
import com.swent.skillswap.ui.navigation.Screen
import com.swent.skillswap.ui.user.ProfileScreen
import com.swent.skillswap.utils.FakeCredentialManager
import com.swent.skillswap.utils.FakeJwtGenerator
import com.swent.skillswap.utils.FirebaseEmulator
import com.swent.skillswap.viewModel.CreateAccountViewModel
import com.swent.skillswap.viewModel.SignInViewModel
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
                startDestination = Screen.SignInMain.route,
                modifier = Modifier.fillMaxSize()
            ) {
                composable(Screen.SignInMain.route) {
                    SignInMainScreen(
                        goToCreateAccountScreen = {
                            navigationActions.navigateTo(Screen.SignInCreateAccount)
                        },
                        goToMainScreen = { navigationActions.navigateTo(Screen.Offers) },
                        vm = vmSignIn,
                        credentialManager = credential
                    )
                }
                composable(Screen.SignInCreateAccount.route) {
                    SignInCreateAccountScreen(
                        goToMainScreen = { navigationActions.navigateTo(Screen.Offers) },
                        vm = vmCreateAccount
                    )
                }
                composable(Screen.Offers.route) { FeedScreen() }
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
            .performClick()

        composeTestRule.onNodeWithTag(CreateAccountTags.NEXT_BUTTON).performClick()
        composeTestRule.waitUntil(timeoutMillis = 10_000L) {
            composeTestRule
                .onAllNodesWithTag(FeedScreenTestTags.FEED_CARD)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeTestRule.onNodeWithTag(FeedScreenTestTags.FEED_CARD).assertIsDisplayed()
    }

    /** Returning Google user (profile already exists) → straight to Offers after sign-in. */
    @Test
    fun t2_googleUser_can_log_on() {
        composeTestRule.onNodeWithTag(SignInTags.GOOGLE_BUTTON).performScrollTo()
        composeTestRule.onNodeWithTag(SignInTags.GOOGLE_BUTTON).assertIsDisplayed()
        composeTestRule.onNodeWithTag(SignInTags.GOOGLE_BUTTON).performClick()

        composeTestRule.waitUntil(timeoutMillis = 10_000L) {
            composeTestRule
                .onAllNodesWithTag(FeedScreenTestTags.FEED_CARD)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeTestRule.onNodeWithTag(FeedScreenTestTags.FEED_CARD).assertIsDisplayed()
    }
}
