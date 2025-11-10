// AI-Generated: Sample data for chat list screen demonstration
// This file provides mock data for testing and demonstrating the chat list screen functionality.
// It includes sample users and posts with realistic data that matches the Post and User models,
// enabling comprehensive testing of the chat interface without requiring Firebase integration.
package com.swent.skillswap.ui.chat

import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint
import com.swent.skillswap.model.post.Offer
import com.swent.skillswap.model.post.PaymentMethod
import com.swent.skillswap.model.post.Post
import com.swent.skillswap.model.post.PostStatus
import com.swent.skillswap.model.post.Request
import com.swent.skillswap.model.tags.SkillTag
import com.swent.skillswap.model.user.User

/** Sample data provider for chat list screen demonstration */
object ChatListScreenData {

    private val defaultLocation = GeoPoint(46.5191, 6.5668)

    fun getSampleUsers(): Map<String, User> {
        return mapOf(
            "user1" to
                User(
                    uid = "user1",
                    username = "Alex Johnson",
                    email = "alex@example.com",
                    profilePicture = "",
                    skillSet = setOf(),
                    rating = 4.5f,
                    availability = listOf()
                ),
            "user2" to
                User(
                    uid = "user2",
                    username = "Sarah Chen",
                    email = "sarah@example.com",
                    profilePicture = "",
                    skillSet = setOf(),
                    rating = 4.8f,
                    availability = listOf()
                ),
            "user3" to
                User(
                    uid = "user3",
                    username = "Mike Rodriguez",
                    email = "mike@example.com",
                    profilePicture = "",
                    skillSet = setOf(),
                    rating = 4.2f,
                    availability = listOf()
                ),
            "user4" to
                User(
                    uid = "user4",
                    username = "Emma Wilson",
                    email = "emma@example.com",
                    profilePicture = "",
                    skillSet = setOf(),
                    rating = 4.9f,
                    availability = listOf()
                )
        )
    }

    fun getSamplePosts(): List<Post> {
        val now = Timestamp.now()
        val future = Timestamp(now.seconds + 86400, 0) // 1 day from now

        return listOf(
            // FeedOffer posts (3)
            Offer(
                uid = "post1",
                title = "Spanish Tutoring",
                description = "Native Spanish speaker offering conversational practice",
                ownerId = "user1",
                tags = setOf(SkillTag.LINEAR_ALGEBRA, SkillTag.CALCULUS),
                paymentMethod = PaymentMethod.SKILLS,
                expiry = future,
                creation = now,
                status = PostStatus.POSTED,
                media = emptyList(),
                location = defaultLocation
            ),
            Offer(
                uid = "post2",
                title = "Graphic Design Help",
                description = "Professional designer offering logo and branding assistance",
                ownerId = "user2",
                tags = setOf(SkillTag.COMPUTER_PROGRAMMING, SkillTag.DATA_STRUCTURES),
                paymentMethod = PaymentMethod.SKILLSANDCASH,
                expiry = future,
                creation = now,
                status = PostStatus.POSTED,
                media = emptyList(),
                location = defaultLocation
            ),
            Offer(
                uid = "post3",
                title = "Bike Repair Service",
                description = "Experienced mechanic offering bike maintenance and repair",
                ownerId = "user3",
                tags = setOf(SkillTag.PHYSICS_MECHANICS, SkillTag.MATERIALS_ENGINEERING),
                paymentMethod = PaymentMethod.SKILLS,
                expiry = future,
                creation = now,
                status = PostStatus.POSTED,
                media = emptyList(),
                location = defaultLocation
            ),
            // Request posts (2)
            Request(
                uid = "post4",
                title = "Need Math Tutor",
                description = "Looking for help with calculus and linear algebra",
                ownerId = "user4",
                tags = setOf(SkillTag.CALCULUS, SkillTag.LINEAR_ALGEBRA),
                paymentMethod = PaymentMethod.SKILLS,
                expiry = future,
                creation = now,
                status = PostStatus.POSTED,
                media = emptyList(),
                location = defaultLocation
            ),
            Request(
                uid = "post5",
                title = "Web Development Help",
                description = "Need assistance with React and JavaScript projects",
                ownerId = "user1",
                tags = setOf(SkillTag.COMPUTER_PROGRAMMING, SkillTag.ALGORITHMS),
                paymentMethod = PaymentMethod.SKILLS,
                expiry = future,
                creation = now,
                status = PostStatus.POSTED,
                media = emptyList(),
                location = defaultLocation
            )
        )
    }
}
