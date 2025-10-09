package com.swent.skillswap.navigation

/* With the help of Claude Sonnet 4.5 for repetitive tasks */

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.rememberNavController
import com.swent.skillswap.SkillSwapApp
import com.swent.skillswap.ui.navigation.NavigationActions
import com.swent.skillswap.ui.navigation.Screen
import com.swent.skillswap.ui.signIn.CreateAccountTags
import com.swent.skillswap.ui.signIn.SignInTags
import org.junit.Rule
import org.junit.Test

class NavigationTest {
    @get:Rule val composeTestRule = createComposeRule()

    @Test
    fun testStartDestinationIsSingInMain() {
        composeTestRule.setContent { SkillSwapApp() }

        composeTestRule.onNodeWithTag(SignInTags.LOGO).assertIsDisplayed()
        composeTestRule.onNodeWithTag(SignInTags.GOOGLE_BUTTON).assertIsDisplayed()
    }

    @Test
    fun testNavigateFromSignInMainToCreateAccount() {
        composeTestRule.setContent { SkillSwapApp() }

        // Click "Create an account" text
        composeTestRule.onNodeWithTag(SignInTags.CREATE_ACCOUNT_TEXT).performClick()

        // Verify CreateAccount screen is displayed
        composeTestRule.onNodeWithTag(CreateAccountTags.TITLE).assertIsDisplayed()
        composeTestRule.onNodeWithTag(CreateAccountTags.USERNAME_FIELD).assertIsDisplayed()
    }

    // Test 3
    @Test
    fun testNavigateFromCreateAccountBackToSignInMain() {
        composeTestRule.setContent { SkillSwapApp() }

        // Navigate to create account
        composeTestRule.onNodeWithTag(SignInTags.CREATE_ACCOUNT_TEXT).performClick()

        // Click Done button
        composeTestRule.onNodeWithTag(CreateAccountTags.DONE_BUTTON).performClick()

        // Verify back on SignInMain
        composeTestRule.onNodeWithTag(SignInTags.LOGO).assertIsDisplayed()
    }

    // Test 5
    @Test
    fun testNavigateToChangesRoute() {
        lateinit var navigationActions: NavigationActions

        composeTestRule.setContent {
            val navController = rememberNavController()
            navigationActions = NavigationActions(navController)
            SkillSwapApp(navController = navController)
        }

        // Navigate programmatically
        composeTestRule.runOnIdle { navigationActions.navigateTo(Screen.SignInCreateAccount) }

        // Verify route changed
        composeTestRule.runOnIdle { assert(navigationActions.currentRoute() == "create_account") }
    }

    // Test 6
    @Test
    fun testCurrentRouteReturnsCorrectRoute() {
        lateinit var navigationActions: NavigationActions

        composeTestRule.setContent {
            val navController = rememberNavController()
            navigationActions = NavigationActions(navController)
            SkillSwapApp(navController = navController)
        }

        // Check initial route
        composeTestRule.runOnIdle { assert(navigationActions.currentRoute() == "signIn") }

        // Navigate and check new route
        composeTestRule.runOnIdle { navigationActions.navigateTo(Screen.SignInCreateAccount) }

        composeTestRule.runOnIdle { assert(navigationActions.currentRoute() == "create_account") }
    }
}
