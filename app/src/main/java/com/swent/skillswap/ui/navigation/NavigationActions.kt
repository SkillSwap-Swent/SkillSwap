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

    object OtherUser : Screen(route = "other_user/{userId}", name = "Other User") {
        fun createRoute(userId: String) = "other_user/$userId"
    }

    object Profile : Screen(route = "profile", name = "Profile", isTopLevelDestination = true)

    object EditProfile : Screen(route = "edit_profile", name = "Edit profile")

    object EditSkills : Screen(route = "edit_skills", name = "Edit Skills")

    object PersonalPosts : Screen(route = "personal_posts", name = "Personal Posts")

    object EditRequest : Screen(route = "edit_request/{postId}", name = "Edit Request") {
        fun createRoute(postId: String) = "edit_request/$postId"
    }

    object Chat : Screen(route = "chat", name = "Chat", isTopLevelDestination = true)

    object AddRequest : Screen(route = "addRequest", name = "Add Request")
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

        val navOptionsBuilder = NavOptions.Builder().setLaunchSingleTop(true)

        if (screen.isTopLevelDestination) {
            navOptionsBuilder
                .setRestoreState(true)
                .setPopUpTo(
                    navController.graph.findStartDestination().id,
                    inclusive = true,
                    saveState = true
                )
        }
        if (screen == Screen.AuthMain) {
            navOptionsBuilder.setPopUpTo(0, inclusive = true)
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
