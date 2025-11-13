package com.swent.skillswap.model.offer

import com.google.firebase.firestore.GeoPoint

/**
 * Represents an offer exchanged between users on the feed.
 *
 * @property skillProvided The skill, service, or item the author is offering to others.
 * @property authorID The unique identifier of the user who created the offer.
 * @property authorName The display name of the user who created the offer.
 * @property requesterAvatar The profile image URL or resource of the offer's requester.
 * @property receiverName The name of the user interested in or receiving the offer.
 * @property skillRequested The skill, service, or item the author is requesting in return.
 * @property thumbnail The image URL or resource representing the offer visually.
 * @property specification A short title summarizing the details or purpose of the offer.
 * @property description A longer text providing additional details about the offer.
 * @property location The meeting location of the offer (defaults to EPFL location).
 */
data class FeedOffer(
    val skillProvided: String = "",
    val authorID: String = "",
    val authorName: String = "",
    val requesterAvatar: String = "",
    val receiverName: String = "",
    val skillRequested: String = "",
    val thumbnail: String = "",
    val specification: String = "",
    val description: String = "",
    val location: GeoPoint = GeoPoint(46.5191, 6.5668) // EPFL default location
)
