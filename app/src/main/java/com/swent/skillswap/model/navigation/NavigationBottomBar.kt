package com.swent.skillswap.model.navigation

/**
 * Interface representing the navigation actions for the BottomBar.
 *
 * Implementations of this interface should handle navigation to the Profile, Chat, and Offer
 * screens when corresponding BottomBar buttons are selected.
 *
 * @author Joey Gugler Made Using Ai (chatGPT)
 */
interface NavigationBottomBar {

    /** Navigate to the Profile screen. */
    fun goToProfile()

    /** Navigate to the Chat screen. */
    fun goToChat()

    /** Navigate to the Offer screen. */
    fun goToOfferScreen()
}
