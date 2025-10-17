/* Adapted from B3-Solution */
package com.swent.skillswap.ui.navigation

import androidx.navigation.NavHostController

sealed class Screen(
    val route: String,
    val name: String,
    val isTopLevelDestination: Boolean = false
) {
    object SignInMain : Screen(route = "signIn", name = "signIn")

    object SignInCreateAccount : Screen(route = "create_account", name = "Create account")

    object Offers : Screen(route = "offers", name = "offers")

    object Profile : Screen(route = "profile", name = "profile")

    object Chat : Screen(route = "chat", name = "chat")
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
        /*
        if (screen.isTopLevelDestination && currentRoute() == screen.route) {
            // If the user is already on the top-level destination, do nothing
            return
        } */
        navController.navigate(screen.route) {
            if (screen !is Screen.SignInMain) {
                // Restore state when reselecting a previously selected item
                restoreState = true
            }
        }
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
