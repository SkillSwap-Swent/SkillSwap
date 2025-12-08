package com.swent.skillswap

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.auth.FirebaseAuth
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import com.swent.skillswap.resources.theme.SkillSwapAppTheme
import com.swent.skillswap.ui.navigation.Screen
import com.swent.skillswap.ui.notification.NotificationScreenTags
import com.swent.skillswap.utils.FirebaseEmulator
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityNavigationTest : TestCase() {

    @get:Rule val composeTestRule = createComposeRule()

    private val ctx =
        androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().targetContext
    private lateinit var navController: NavHostController
    private lateinit var auth: FirebaseAuth

    init {
        FirebaseEmulator.startEmulator()
    }

    @Before
    fun setUp() = runBlocking {
        // Initialize FirebaseApp if necessary
        try {
            if (com.google.firebase.FirebaseApp.getApps(ctx).isEmpty()) {
                com.google.firebase.FirebaseApp.initializeApp(ctx)
            }
        } catch (e: Exception) {
            // Ignore if already initialized
        }

        // Auth: sign in anonymously on the emulator
        auth = FirebaseAuth.getInstance()
        try {
            auth.signInAnonymously().await()
        } catch (e: Exception) {
            // Ignore if sign-in fails (may already be signed in or emulator issue)
        }

        composeTestRule.setContent {
            navController = rememberNavController()
            SkillSwapAppTheme { SkillSwapApp(navController = navController) }
        }
        composeTestRule.waitForIdle()

        // Navigate to Profile screen first
        composeTestRule.runOnUiThread {
            navController.navigate(Screen.Profile.route) {
                popUpTo(Screen.AuthMain.route) { inclusive = true }
            }
        }
        composeTestRule.waitForIdle()
        Unit // Explicitly return Unit for JUnit
    }

    @Test
    fun notificationListRoute_mapsToNotificationTab() = run {
        // Navigate to NotificationList route
        composeTestRule.runOnUiThread {
            navController.navigate(Screen.NotificationList.route) {
                popUpTo(Screen.Profile.route) { inclusive = false }
            }
        }
        composeTestRule.waitForIdle()

        // Verify notification screen is displayed
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule
                .onAllNodesWithTag(NotificationScreenTags.SCREEN)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeTestRule.onNodeWithTag(NotificationScreenTags.SCREEN).assertIsDisplayed()
    }

    @After
    fun tearDown() = runBlocking {
        try {
            auth.signOut()
        } catch (e: Exception) {
            // Ignore
        }
        Unit // Explicitly return Unit for JUnit
    }
}
