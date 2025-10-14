package com.swent.skillswap.ui.offerScreen

/**
 * Defines the navigation actions related to offers.
 *
 * Implementations handle navigation events such as moving to a user's profile when interacting with
 * an offer.
 */
interface OfferNavigation {

    /**
     * Navigates to the profile view of the specified user.
     *
     * @param userId The ID of the user whose profile should be displayed.
     */
    fun goToProfileView(userId: String)
}
