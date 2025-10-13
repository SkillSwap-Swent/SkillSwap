/**
 * Credits: Code: Alexander Magnus Comments: Gemini
 *
 * This file defines the data structures for posts within the SkillSwap application. It includes the
 * central `Post` interface, which outlines the common properties for all post types, and several
 * enums to represent different states and attributes of a post, such as its type, payment method,
 * and status.
 */
package com.swent.skillswap.model.post

import com.google.firebase.Timestamp
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
    val tags: List<EveryTag>
    /** A list of accepted/offered payment methods for the skill exchange. */
    val paymentMethods: List<PaymentMethod>
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
            paymentMethods.isNotEmpty() &&
            (creation.toDate().before(Timestamp.now().toDate()) ||
                creation.toDate() == Timestamp.now().toDate())
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
enum class PaymentMethod {
    /** The exchange is for another skill or service (bartering). */
    SKILLS,
    /** The exchange involves a monetary transaction. */
    CASH
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
