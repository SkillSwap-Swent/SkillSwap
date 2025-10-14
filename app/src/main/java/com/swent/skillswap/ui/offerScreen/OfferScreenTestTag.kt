package com.swent.skillswap.ui.offerScreen

import com.swent.skillswap.model.offer.Offer

/**
 * Defines test tags used to identify UI elements in the Offer screen during Compose testing.
 *
 * These tags ensure stable and maintainable test references, allowing tests to locate and interact
 * with specific components in the UI tree.
 */
object OfferScreenTestTags {

    /** Tag for the entire offer card component. */
    const val OFFER_CARD = "OFFER_CARD"

    /** Tag for the text element displaying what the user is offering (the "give" field). */
    const val OFFER_GIVE = "OFFER_GIVE"

    /** Tag for the text element displaying what the user wants to receive (the "receive" field). */
    const val OFFER_RECEIVE = "OFFER_RECEIVE"

    /**
     * Generates a unique test tag for a given [Offer] instance.
     *
     * @param offer The offer for which to generate a unique test tag.
     * @return A unique tag string in the format `"OFFER_<authorID>"`.
     */
    fun getTestTagForOffer(offer: Offer): String = "OFFER_${offer.authorID}"
}
