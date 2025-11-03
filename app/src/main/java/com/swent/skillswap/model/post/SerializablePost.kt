package com.swent.skillswap.model.post

import com.google.firebase.Timestamp
import com.swent.skillswap.model.tags.EveryTag
import java.io.Serializable

data class SerializablePost(
    override val uid: String,
    override val title: String,
    override val description: String,
    override val ownerId: String,
    override val tags: List<EveryTag>,
    override val paymentMethod: PaymentMethod,
    override val expiry: Timestamp,
    override val creation: Timestamp,
    override val status: PostStatus,
    override val media: List<String>,
    override val type: PostType,
    override val postReplies: List<PostReply>
) : Post, Serializable
