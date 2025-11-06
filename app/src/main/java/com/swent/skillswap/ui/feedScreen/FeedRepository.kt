package com.swent.skillswap.ui.feedScreen

import com.swent.skillswap.model.offer.FeedOffer

/**
 * Defines the contract for accessing and manipulating offer-related data.
 *
 * Implementations of this interface are responsible for handling operations such as accepting,
 * skipping, or fetching offers, as well as retrieving thumbnails associated with specific offers.
 */
interface FeedRepository {

    /**
     * Accepts a given [offer] on behalf of the specified [userId].
     *
     * @param offer The offer being accepted.
     * @param userId The ID of the user performing the action.
     */
    fun accept(offer: FeedOffer, userId: String)

    /**
     * Retrieves a new offer to display for the specified [userId].
     *
     * @param userId The ID of the user requesting an offer.
     * @return The next [FeedOffer] available for the user.
     */
    fun getPost(userId: String): FeedOffer

    /**
     * Skips the given [offer] for the specified [userId].
     *
     * @param offer The offer to be skipped.
     * @param userId The ID of the user skipping the offer.
     */
    fun skip(offer: FeedOffer, userId: String)

    /**
     * Retrieves the thumbnail associated with the given [thumbnailId].
     *
     * @param thumbnailId The identifier for the thumbnail image.
     */
    fun getThumbnail(thumbnailId: String)

    fun blockUser(userId: String)

    fun reportOffer(offer: com.swent.skillswap.model.offer.FeedOffer)
}
