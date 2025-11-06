package com.swent.skillswap.ui.feedScreen

/**
 * Defines test tags used to identify UI elements in the FeedOffer screen during Compose testing.
 *
 * These tags ensure stable and maintainable test references, allowing tests to locate and interact
 * with specific components in the UI tree.
 */
object FeedScreenTestTags {
    /** Tag used for the main feed card displaying an offer. */
    const val FEED_CARD = "FEED_CARD"

    /** Tag for the menu button that opens block/report actions for an offer. */
    const val FEED_MENU_BUTTON = "FEED_MENU_BUTTON"

    /** Tag for the thumbnail image shown on each feed card. */
    const val FEED_THUMBNAIL = "FEED_THUMBNAIL"

    /** Tag for the text showing the skill that the user wants to learn or receive. */
    const val SKILL_REQUESTED = "SKILL_REQUESTED"

    /** Tag for the text showing the skill that the user offers to teach or provide. */
    const val SKILL_GIVE = "SKILL_GIVE"

    /** Tag for the title section of the offer’s specification. */
    const val SPECIFICATION_TITLE = "SPECIFICATION_TITLE"

    /** Tag for the description section of the offer’s specification. */
    const val SPECIFICATION_DESCRIPTION = "SPECIFICATION_DESCRIPTION"

    /** Tag for the button allowing the current user to accept the offer. */
    const val ACCEPT_BUTTON = "ACCEPT_BUTTON"

    /** Tag for the button allowing the current user to decline or skip the offer. */
    const val DECLINE_BUTTON = "DECLINE_BUTTON"

    /** Tag for the profile picture of the user who created the offer. */
    const val REQUESTER_PROFILE_PICTURE = "REQUESTER_PROFILE_PICTURE"

    /** Tag for the display name of the user who created the offer. */
    const val REQUESTER_NAME = "REQUESTER_NAME"
    const val SCROLL_BOX = "SCROLL_BOX"
}
