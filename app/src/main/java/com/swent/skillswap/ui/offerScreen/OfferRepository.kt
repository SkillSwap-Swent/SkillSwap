package com.swent.skillswap.ui.offerScreen

import com.swent.skillswap.model.offer.Offer

/**
 * Defines the contract for accessing and manipulating offer-related data.
 *
 * Implementations of this interface are responsible for handling operations such as accepting,
 * skipping, or fetching offers, as well as retrieving thumbnails associated with specific offers.
 */
interface OfferRepository {

    /**
     * Accepts a given [offer] on behalf of the specified [userId].
     *
     * @param offer The offer being accepted.
     * @param userId The ID of the user performing the action.
     */
    fun accept(offer: Offer, userId: String)

    /**
     * Retrieves a new offer to display for the specified [userId].
     *
     * @param userId The ID of the user requesting an offer.
     * @return The next [Offer] available for the user.
     */
    fun getPost(userId: String): Offer

    /**
     * Skips the given [offer] for the specified [userId].
     *
     * @param offer The offer to be skipped.
     * @param userId The ID of the user skipping the offer.
     */
    fun skip(offer: Offer, userId: String)

    /**
     * Retrieves the thumbnail associated with the given [thumbnailId].
     *
     * @param thumbnailId The identifier for the thumbnail image.
     */
    fun getThumbnail(thumbnailId: String)
}
