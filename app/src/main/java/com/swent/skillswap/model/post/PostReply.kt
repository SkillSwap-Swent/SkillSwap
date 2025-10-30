package com.swent.skillswap.model.post

import com.google.firebase.Timestamp

data class PostReply (
    /** A unique identifier for the post reply. */
    val uid: String,
    /** The ID of the post that the reply is associated with. */
    val postId: String,
    /** The ID of the user who created the post reply. */
    val ownerId: String,
    /** The timestamp of when the post reply was created. */
    val creation: Timestamp,
    /** The content of the post reply. */
    val message: String,
    /** The type of the post that the reply is associated with. */
    val postType: PostType
) {
    // Needed only for Firestore's deserializer
    constructor() : this(
        uid = "",
        postId = "",
        ownerId = "",
        creation = Timestamp.now(),
        message = "",
        postType = PostType.REQUEST
    )
}
