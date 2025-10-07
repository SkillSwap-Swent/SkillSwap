package com.swent.skillswap.model.post

import com.google.firebase.Timestamp
import com.swent.skillswap.model.tags.EveryTag

interface Post {
    val uid: String
    val title: String
    val description: String
    val ownerId: String
    val tags: List<EveryTag>
    val paymentMethods: List<PaymentMethod>
    val expiry: Timestamp
    val creation: Timestamp
    val status: PostStatus
    val media: List<String>
    val type: PostType
}

enum class PostType {
    REQUEST,
    OFFER
}

enum class PaymentMethod {
    SKILLS,
    CASH
}

enum class PostStatus {
    DRAFT,
    POSTED,
    COMPLETED,
    ARCHIVED
}
