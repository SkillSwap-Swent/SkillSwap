/* Adapted from B3-Solution */
package com.swent.skillswap.ui.navigation

import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions

sealed class Screen(
    val route: String,
    val name: String,
    val isTopLevelDestination: Boolean = false,
) {
    object SignInMain : Screen(route = "signIn", name = "signIn", isTopLevelDestination = false)

    object SignInCreateAccount : Screen(route = "create_account", name = "Create account")

    object Offers : Screen(route = "offers", name = "offers", isTopLevelDestination = true)

    object Profile : Screen(route = "profile", name = "profile", isTopLevelDestination = true)

    object EditSkills : Screen(route = "edit_skills", name = "Edit Skills")

    object Chat : Screen(route = "chat", name = "chat", isTopLevelDestination = true)
}

open class NavigationActions(
    private val navController: NavHostController,
) {
    /**
     * Navigate to the specified screen.
     *
     * @param screen The screen to navigate to
     */
    open fun navigateTo(screen: Screen) {

        val navOptionsBuilder = NavOptions.Builder().setLaunchSingleTop(true).setRestoreState(true)

        if (screen.isTopLevelDestination) {
            navOptionsBuilder.setPopUpTo(
                navController.graph.findStartDestination().id,
                inclusive = false,
                saveState = true
            )
        }
        navController.navigate(screen.route, navOptionsBuilder.build())
    }

    /** Navigate back to the previous screen. */
    open fun goBack() {
        navController.popBackStack()
    }

    /**
     * Get the current route of the navigation controller.
     *
     * @return The current route
     */
    open fun currentRoute(): String {
        return navController.currentDestination?.route ?: ""
    }
}
