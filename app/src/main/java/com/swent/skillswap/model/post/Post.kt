/**
 * Credits: Alexander Magnus
 *
 * Comments: Gemini
 *
 * This file defines the data structures for posts within SkillSwap. It includes the central `Post`
 * interface, which outlines the common properties for all post types, and several enums to
 * represent different states and attributes of a post, such as its type, payment method, and
 * status.
 */
package com.swent.skillswap.model.post

import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint
import com.swent.skillswap.model.tags.EveryTag

/**
 * Represents a generic post in the application, either a request for a skill or an offer of a
 * skill. This interface defines the essential properties that all posts must have.
 */
interface Post {
    /** A unique identifier for the post. */
    val uid: String
    /** The title of the post. */
    val title: String
    /** A detailed description of the skill being offered or requested. */
    val description: String
    /** The ID of the user who created the post. */
    val ownerId: String
    /** A list of tags that categorize the post, making it easier to search for. */
    val tags: Collection<EveryTag>
    /** A list of accepted/offered payment methods for the skill exchange. */
    val paymentMethod: PaymentMethod
    /** The expiration timestamp of the post, after which it may become inactive. */
    val expiry: Timestamp
    /** The timestamp of when the post was created. */
    val creation: Timestamp
    /** The current status of the post (e.g., Draft, Posted, Completed). */
    val status: PostStatus
    /** A list of TODO("How we store media") for any associated media, like images or videos. */
    val media: List<String>
    /** The type of the post, indicating whether it's an offer or a request. */
    val type: PostType
    /** The meeting location of the post */
    val location: GeoPoint
    /**
     * A list of normalized search keywords used to support Firestore queries with
     * `whereArrayContainsAny`.
     *
     * This list is built from the [title], and [tags] fields by:
     * - Splitting the title into lowercase words.
     * - Converting each payment method to a lowercase string.
     * - Converting each tag to a lowercase string.
     *
     * The resulting list enables flexible case-insensitive searching across multiple fields.
     *
     * TODO: Ideally this list shouldn't be dynamically built as offer and request are immutable
     *   data classes but I don't know how to do that while keeping the code non duplicated in the
     *   interface. https://github.com/SkillSwap-Swent/SkillSwap/issues/45
     */
    val searchKeys: List<String>
        get() = buildSearchKeys()

    /**
     * In the current implementation the keywords aren't separated by category. This means that when
     * searching for a title keyword we can also return a keyword from the tags. I see this as a
     * desired behaviour, hover this can be changed by naming the array elements TAG_$it, TITLE_$it,
     * etc.
     */
    private fun buildSearchKeys(): List<String> {
        val searchKeysTemp = mutableListOf<String>()

        searchKeysTemp.addAll(title.split(" ").map { it.lowercase() })
        searchKeysTemp.addAll(tags.map { it.toString().lowercase() })

        return searchKeysTemp
    }

    /**
     * Validates the essential fields of the post to ensure they are not empty. This is a temporary
     * implementation until the inheritors implement a proper validation.
     *
     * @return `true` if the post is valid, `false` otherwise.
     */
    fun validate(): Boolean {
        return uid.isNotBlank() &&
            title.isNotBlank() &&
            description.isNotBlank() &&
            tags.isNotEmpty() &&
            expiry.toDate().after(Timestamp.now().toDate()) &&
            creation.toDate().before(Timestamp.now().toDate())
    }
}

/** Enum representing the type of a post. */
enum class PostType {
    /** A post where a user is requesting a skill or service. */
    REQUEST,
    /** A post where a user is offering a skill or service. */
    OFFER
}

/** Enum representing the possible payment methods for a skill exchange. */
enum class PaymentMethod(val displayName: String) {
    /** The exchange is for another skill or service (bartering). */
    SKILLS("Skills"),
    /** The exchange involves a monetary transaction. */
    CASH("Cash"),
    /** Both methods */
    SKILLSANDCASH("Skills and Cash")
}

/** Enum representing the lifecycle status of a post. */
enum class PostStatus {
    /** The post has been created but is not yet visible to other users. */
    DRAFT,
    /** The post is active and visible to other users. */
    POSTED,
    /** The skill exchange has been completed. */
    COMPLETED,
    /** The post has been hidden and is no longer active. */
    ARCHIVED
}
