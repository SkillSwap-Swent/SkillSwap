package com.swent.skillswap.model.offer

import com.swent.skillswap.ui.offerScreen.OfferNavigation

/**
 * Fake implementation of [com.swent.skillswap.ui.offerScreen.OfferNavigation] for testing
 * navigation logic.
 */
class FakeOfferNavigation : OfferNavigation {

    private val _visitedProfiles = mutableSetOf<String>() // use set to avoid duplicates

    override fun goToProfileView(userId: String) {
        _visitedProfiles.add(userId)
    }

    fun getVisitedProfiles() = _visitedProfiles.toList()
}
