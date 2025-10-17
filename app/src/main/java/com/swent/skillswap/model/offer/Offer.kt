package com.swent.skillswap.model.offer
/**
 * Represents an offer exchanged between users.
 *
 * @property give The skill, service, or item the author is offering.
 * @property receive The skill, service, or item the author wants in return.
 * @property authorID The unique identifier of the user who created the offer.
 * @property thumbnail Optional thumbnail image ID for display purposes.
 */
data class Offer(
    val give: String = "",
    val receive: String = "",
    val authorID: String = "",
    val thumbnail: String = ""
)
