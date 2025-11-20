/** Credits: Alexander Magnus */
package com.swent.skillswap.model.post

import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint
import com.swent.skillswap.model.tags.PostTag
import com.swent.skillswap.model.tags.SkillTag

data class Offer(
    override val uid: String,
    override val title: String,
    override val description: String,
    override val ownerId: String,
    override val skills: Set<SkillTag>,
    override val tags: Set<PostTag>,
    override val paymentMethod: PaymentMethod,
    override val expiry: Timestamp,
    override val creation: Timestamp,
    override val status: PostStatus,
    override val media: List<String>,
    override val postReplies: Set<PostReply> = emptySet(),
    override val location: GeoPoint
) : Post {
    override val type: PostType = PostType.OFFER
}
