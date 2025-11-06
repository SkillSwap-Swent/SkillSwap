package com.swent.skillswap.model.post

import com.google.firebase.Timestamp
import java.util.UUID

data class PostReply(
    /** The unique identifier for the post reply. */
    val uid: String = UUID.randomUUID().toString(),
    /** The ID of the post that the reply is associated with. */
    val postId: String,
    /** The ID of the user who created the post reply. */
    val ownerId: String,
    /** The timestamp of when the post reply was created. */
    val creation: Timestamp,
    /** The content of the post reply. */
    val message: String,
    /** The type of the post that the reply is associated with. */
    val postType: PostType,
    /** The status of the post reply. */
    val replyStatus: ReplyStatus
)

enum class ReplyStatus {
    PROPOSED,
    ACCEPTED,
    REJECTED
}
