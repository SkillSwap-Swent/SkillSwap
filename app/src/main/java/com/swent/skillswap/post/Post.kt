package com.swent.skillswap.post

import com.google.firebase.Timestamp

interface Post {
  val uid: String
  val title: String
  val description: String
  val ownerId: String
  val tags: List<PostTags>
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
