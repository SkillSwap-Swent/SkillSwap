package com.swent.skillswap.navigation

/* With the help of Claude Sonnet 4.5 for repetitive tasks */

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.compose.rememberNavController
import com.swent.skillswap.SkillSwapApp
import com.swent.skillswap.ui.navigation.NavigationActions
import com.swent.skillswap.ui.navigation.Screen
import org.junit.Rule
import org.junit.Test

class NavigationTest {
    @get:Rule val composeTestRule = createComposeRule()

    private fun setupNavigation(): NavigationActions {
        lateinit var navigationActions: NavigationActions

        composeTestRule.setContent {
            val navController = rememberNavController()
            navigationActions = NavigationActions(navController)
            SkillSwapApp(navController = navController)
        }

        return navigationActions
    }

    @Test
    fun testStartDestinationIsSingInMain() {
        val navigationActions = setupNavigation()

        // Verify initial route is signIn
        composeTestRule.runOnIdle { assert(navigationActions.currentRoute() == "signIn") }
    }

    @Test
    fun testSkillSwapAppWithDefaultNavController() {
        composeTestRule.setContent { SkillSwapApp() }

        // Verify initial screen is displayed
        composeTestRule.waitForIdle()
    }

    @Test
    fun testNavigateToChangesRoute() {
        val navigationActions = setupNavigation()

        // Navigate programmatically
        composeTestRule.runOnIdle { navigationActions.navigateTo(Screen.SignInCreateAccount) }

        // Verify route changed
        composeTestRule.runOnIdle { assert(navigationActions.currentRoute() == "create_account") }
    }

    @Test
    fun testCurrentRouteReturnsCorrectRoute() {
        val navigationActions = setupNavigation()

        // Check initial route
        composeTestRule.runOnIdle { assert(navigationActions.currentRoute() == "signIn") }

        // Navigate and check new route
        composeTestRule.runOnIdle { navigationActions.navigateTo(Screen.SignInCreateAccount) }

        composeTestRule.runOnIdle { assert(navigationActions.currentRoute() == "create_account") }
    }

    @Test
    fun testNavigateBackToSignInMain() {
        val navigationActions = setupNavigation()

        composeTestRule.runOnIdle { navigationActions.navigateTo(Screen.SignInCreateAccount) }
        composeTestRule.runOnIdle { assert(navigationActions.currentRoute() == "create_account") }

        composeTestRule.runOnIdle { navigationActions.navigateTo(Screen.SignInMain) }
        composeTestRule.runOnIdle { assert(navigationActions.currentRoute() == "signIn") }
    }
}
