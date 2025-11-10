package com.swent.skillswap.model.offer

import com.swent.skillswap.ui.feedScreen.FeedScreenNavigation

/**
 * Fake implementation of [com.swent.skillswap.ui.feedScreen.FeedScreenNavigation] for testing
 * navigation logic.
 */
class FakeFeedNavigation : FeedScreenNavigation {

    private val _visitedProfiles = mutableSetOf<String>()

    override fun goToProfileView(userId: String) {
        _visitedProfiles.add(userId)
    }

    fun getVisitedProfiles(): List<String> = _visitedProfiles.toList()
}
