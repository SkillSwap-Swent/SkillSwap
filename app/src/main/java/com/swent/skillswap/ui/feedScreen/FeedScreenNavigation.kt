package com.swent.skillswap.ui.feedScreen

/**
 * Defines a navigation action for the Feed screen.
 *
 * Implementations handle navigation events such as moving to a user's profile when interacting with
 * an offer.
 */
fun interface FeedScreenNavigation {
    /**
     * Navigates to the profile view of the specified user.
     *
     * @param userId The ID of the user whose profile should be displayed.
     */
    fun goToProfileView(userId: String)
}
