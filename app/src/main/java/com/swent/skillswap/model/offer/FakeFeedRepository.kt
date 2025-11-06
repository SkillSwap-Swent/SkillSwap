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

    // Private mutable lists + public read-only exposure
    private val _blockedUsers = mutableListOf<String>()
    val blockedUsers: List<String>
        get() = _blockedUsers

    private val _reportedOffers = mutableListOf<FeedOffer>()
    val reportedOffers: List<FeedOffer>
        get() = _reportedOffers

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

    override fun getThumbnail(thumbnailId: String) {}

    override fun blockUser(userId: String) {
        _blockedUsers.add(userId)
    }

    override fun reportOffer(offer: FeedOffer) {
        _reportedOffers.add(offer)
    }

    fun getAcceptedOffers(): List<Pair<FeedOffer, String>> = acceptedOffers.toList()

    fun getSkippedOffers(): List<Pair<FeedOffer, String>> = skippedOffers.toList()
}
