package com.swent.skillswap.model.navigation

/**
 * A fake implementation of [NavigationBottomBar] for testing purposes.
 *
 * Tracks which navigation methods were called by setting boolean flags. Can be reset using [reset]
 * to clear all call states.
 *
 * @property goToProfileCalled True if [goToProfile] was called.
 * @property goToChatCalled True if [goToChat] was called.
 * @property goToOfferScreenCalled True if [goToFeedScreen] was called.
 * @author Joey Gugler Made Using Ai (chatGPT)
 */
class FakeNavigationBottomBar : NavigationBottomBar {

    var goToProfileCalled = false
        private set

    var goToChatCalled = false
        private set

    var goToOfferScreenCalled = false
        private set

    /** Marks [goToProfileCalled] as true. */
    override fun goToProfile() {
        goToProfileCalled = true
    }

    /** Marks [goToChatCalled] as true. */
    override fun goToChat() {
        goToChatCalled = true
    }

    /** Marks [goToOfferScreenCalled] as true. */
    override fun goToFeedScreen() {
        goToOfferScreenCalled = true
    }

    /** Resets all call flags to false. */
    fun reset() {
        goToProfileCalled = false
        goToChatCalled = false
        goToOfferScreenCalled = false
    }
}
