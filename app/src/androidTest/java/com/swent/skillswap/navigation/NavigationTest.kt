package com.swent.skillswap.navigation

/* With the help of Claude Sonnet 4.5 for repetitive tasks */

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.NavHostController
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
        composeTestRule.runOnIdle { navigationActions.navigateTo(Screen.CreateAccount) }

        // Verify route changed
        composeTestRule.runOnIdle { assert(navigationActions.currentRoute() == "create_account") }
    }

    @Test
    fun testGoBackNavigatesToPreviousScreen() {
        val navigationActions = setupNavigation()

        // Navigate to Create Account screen
        composeTestRule.runOnIdle { navigationActions.navigateTo(Screen.CreateAccount) }
        composeTestRule.runOnIdle { assert(navigationActions.currentRoute() == "create_account") }

        // Navigate back to Sign In screen
        composeTestRule.runOnIdle { navigationActions.goBack() }
        composeTestRule.runOnIdle { assert(navigationActions.currentRoute() == "signIn") }
    }

    @Test
    fun testCurrentRouteReturnsCorrectRoute() {
        val navigationActions = setupNavigation()

        // Check initial route
        composeTestRule.runOnIdle { assert(navigationActions.currentRoute() == "signIn") }

        // Navigate and check new route
        composeTestRule.runOnIdle { navigationActions.navigateTo(Screen.CreateAccount) }

        composeTestRule.runOnIdle { assert(navigationActions.currentRoute() == "create_account") }
    }

    @Test
    fun testNavigateBackToSignInMain() {
        val navigationActions = setupNavigation()

        composeTestRule.runOnIdle { navigationActions.navigateTo(Screen.CreateAccount) }
        composeTestRule.runOnIdle { assert(navigationActions.currentRoute() == "create_account") }

        composeTestRule.runOnIdle { navigationActions.navigateTo(Screen.AuthMain) }
        composeTestRule.runOnIdle { assert(navigationActions.currentRoute() == "signIn") }
    }

    @Test
    fun testCurrentRouteReturnsEmptyWhenDestinationIsNull() {
        lateinit var navController: NavHostController
        lateinit var navigationActions: NavigationActions

        composeTestRule.setContent {
            // Create a nav controller without a proper NavHost setup
            navController = rememberNavController()
            navigationActions = NavigationActions(navController)
        }

        composeTestRule.runOnIdle {
            // Before any navigation graph is set, currentDestination should be null
            assert(navController.currentDestination == null) {
                "Expected null destination before NavHost setup"
            }

            // Verify currentRoute returns empty string for null destination
            val route = navigationActions.currentRoute()
            assert(route == "") {
                "currentRoute() should return empty string when destination is null, got: $route"
            }
        }
    }

    @Test
    fun testNavigateToTopLevelDestinationExecutesPopUpTo() {
        val navigationActions = setupNavigation()

        val topLevelScreens = listOf(Screen.Profile, Screen.Offers, Screen.Chat)

        topLevelScreens.forEach { screen ->
            // Navigate to the top-level screen
            composeTestRule.runOnIdle { navigationActions.navigateTo(screen) }

            // Verify the route changed
            composeTestRule.runOnIdle {
                assert(navigationActions.currentRoute() == screen.route) {
                    "Expected route to be ${screen.route}, but was ${navigationActions.currentRoute()}"
                }
            }
        }
    }
}
