/** Credits: Alexander Magnus */
package com.swent.skillswap.model.post

import com.google.firebase.Timestamp
import com.swent.skillswap.model.tags.EveryTag

data class Request(
    override val uid: String,
    override val title: String,
    override val description: String,
    override val ownerId: String,
    override val tags: Set<EveryTag>,
    override val paymentMethod: PaymentMethod,
    override val expiry: Timestamp,
    override val creation: Timestamp,
    override val status: PostStatus,
    override val media: List<String>,
    override val postReplies: Set<PostReply> = emptySet()
) : Post {
    override val type: PostType
        get() = PostType.REQUEST

    // TODO: implement proper validation logic
    // https://github.com/orgs/SkillSwap-Swent/projects/1/views/2?filterQuery=&pane=issue&itemId=132697400

    // Need this for Firestore deserializer
    constructor() :
        this(
            uid = "",
            title = "",
            description = "",
            ownerId = "",
            tags = emptySet(),
            paymentMethod = PaymentMethod.SKILLSANDCASH,
            expiry = Timestamp.now(),
            creation = Timestamp.now(),
            status = PostStatus.POSTED,
            media = emptyList(),
            postReplies = emptySet()
        )
}
