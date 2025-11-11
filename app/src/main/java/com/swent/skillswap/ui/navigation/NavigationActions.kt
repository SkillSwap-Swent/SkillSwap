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
    object AuthMain : Screen(route = "signIn", name = "Sign In", isTopLevelDestination = false)

    object CreateAccount : Screen(route = "create_account", name = "Create account")

    object PasswordRecovery : Screen(route = "password_recovery", name = "Password Recovery")

    object Feed : Screen(route = "feed", name = "Feed", isTopLevelDestination = true)

    object Profile : Screen(route = "profile", name = "Profile", isTopLevelDestination = true)

    object EditProfile : Screen(route = "edit_profile", name = "Edit profile")

    object EditSkills : Screen(route = "edit_skills", name = "Edit Skills")

    object Chat : Screen(route = "chat", name = "Chat", isTopLevelDestination = true)
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

        if (screen.isTopLevelDestination && currentRoute() == screen.route) {
            return // Prevent re-navigating to the same screen
        }

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
