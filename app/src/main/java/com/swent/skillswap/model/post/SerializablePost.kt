package com.swent.skillswap.model.post

import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint
import com.swent.skillswap.model.tags.PostTag
import com.swent.skillswap.model.tags.SkillTag
import java.io.Serializable

data class SerializablePost(
    override val uid: String,
    override val title: String,
    override val description: String,
    override val ownerId: String,
    override val skills: List<SkillTag>,
    override val tags: List<PostTag>,
    override val paymentMethod: PaymentMethod,
    override val expiry: Timestamp,
    override val creation: Timestamp,
    override val status: PostStatus,
    override val media: List<String>,
    override val type: PostType,
    override val postReplies: List<PostReply>,
    override val location: GeoPoint,
    override val reportCount: Long
) : BasePost(), Serializable
