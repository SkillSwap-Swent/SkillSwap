package com.swent.skillswap.model.feed

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
    fun accept(offer: FeedPost, userId: String)

    /**
     * Retrieves a new offer to display for the specified [userId].
     *
     * @param userId The ID of the user requesting an offer.
     * @return The next [FeedPost] available for the user.
     */
    fun getPost(userId: String): FeedPost

    /**
     * Skips the given [offer] for the specified [userId].
     *
     * @param offer The offer to be skipped.
     * @param userId The ID of the user skipping the offer.
     */
    fun skip(offer: FeedPost, userId: String)

    /**
     * Blocks a user with the specified [userId], preventing further interaction or communication
     * with that user within the application.
     *
     * @param userId The unique identifier of the user to be blocked.
     */
    fun blockUser(userId: String)

    /**
     * Reports an offer to the system for review, indicating that it may violate guidelines or
     * contain inappropriate content.
     *
     * @param offer The [FeedPost] instance representing the offer to be reported.
     */
    fun reportOffer(offer: FeedPost)
}
