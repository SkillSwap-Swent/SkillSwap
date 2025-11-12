package com.swent.skillswap.model.offer

import com.swent.skillswap.ui.feedScreen.FeedRepository

/**
 * Fake implementation of FeedRepository for UI tests, tracking accept/skip/block/report actions.
 */
class FakeFeedRepository : FeedRepository {

    private val acceptedOffers = mutableListOf<Pair<FeedOffer, String>>()
    private val skippedOffers = mutableListOf<Pair<FeedOffer, String>>()
    private val preloadedOffers = mutableListOf<FeedOffer>()
    private var postCounter = 0
    private val _blockedUsers = mutableListOf<String>()
    private val _reportedOffers = mutableListOf<FeedOffer>()

    fun preloadOffers(vararg offers: FeedOffer) {
        preloadedOffers.clear()
        preloadedOffers.addAll(offers)
        postCounter = 0
    }

    override fun accept(offer: FeedOffer, userId: String) {
        acceptedOffers.add(offer to userId)
    }

    override fun getPost(userId: String): FeedOffer {
        return if (postCounter < preloadedOffers.size) {
            preloadedOffers[postCounter++]
        } else {
            postCounter++
            FeedOffer(
                skillProvided = "Generated $postCounter (from $userId)",
                skillRequested = "Looking for Skill ${postCounter + 1}",
                authorID = "author_$postCounter",
                thumbnail = "thumb_$postCounter"
            )
        }
    }

    override fun skip(offer: FeedOffer, userId: String) {
        skippedOffers.add(offer to userId)
    }

    override fun blockUser(userId: String) {
        _blockedUsers.add(userId)
    }

    override fun reportOffer(offer: FeedOffer) {
        _reportedOffers.add(offer)
    }

    fun getAcceptedOffers(): List<Pair<FeedOffer, String>> = acceptedOffers.toList()

    fun getSkippedOffers(): List<Pair<FeedOffer, String>> = skippedOffers.toList()
}
