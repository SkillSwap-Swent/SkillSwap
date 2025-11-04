/** Credits: Alexander Magnus */
package com.swent.skillswap.model.post

import com.google.firebase.Timestamp
import com.swent.skillswap.model.tags.EveryTag

data class Offer(
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
    override val type: PostType = PostType.OFFER
}
