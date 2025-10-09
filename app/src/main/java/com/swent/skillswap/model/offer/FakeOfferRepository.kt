package com.swent.skillswap.model.offer

import com.swent.skillswap.ui.offerScreen.OfferRepository

/**
 * Fake implementation of [com.swent.skillswap.ui.offerScreen.OfferRepository] for testing without
 * network or database.
 */
class FakeOfferRepository : OfferRepository {

    private val acceptedOffers = mutableListOf<Pair<Offer, String>>()
    private val skippedOffers = mutableListOf<Pair<Offer, String>>()
    private val preloadedOffers = mutableListOf<Offer>()
    private var postCounter = 0

    // Preload offers for deterministic output in tests
    fun preloadOffers(vararg offers: Offer) {
        preloadedOffers.clear()
        preloadedOffers.addAll(offers)
        postCounter = 0
    }

    override fun accept(offer: Offer, userId: String) {
        acceptedOffers.add(offer to userId)
    }

    override fun getPost(userId: String): Offer {
        return if (postCounter < preloadedOffers.size) {
            preloadedOffers[postCounter++]
        } else {
            // fallback to generated offer if we run out
            postCounter++
            Offer(
                give = "Generated $postCounter (from $userId)",
                receive = "Looking for Skill ${postCounter + 1}",
                authorID = "author_$postCounter",
                thumbnail = "thumb_$postCounter"
            )
        }
    }

    override fun skip(offer: Offer, userId: String) {
        skippedOffers.add(offer to userId)
    }

    override fun getThumbnail(thumbnailId: String) {
        /* noop */
    }

    /** Helpers for assertions in tests */
    fun getAcceptedOffers(): List<Pair<Offer, String>> = acceptedOffers.toList()

    fun getSkippedOffers(): List<Pair<Offer, String>> = skippedOffers.toList()
}
