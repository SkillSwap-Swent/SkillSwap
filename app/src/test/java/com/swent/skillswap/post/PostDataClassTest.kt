package com.swent.skillswap.post

import com.google.firebase.Timestamp
import com.swent.skillswap.model.post.PaymentMethod
import com.swent.skillswap.model.post.PostStatus
import com.swent.skillswap.model.post.PostType
import com.swent.skillswap.model.post.Request
import com.swent.skillswap.model.tags.EveryTag
import com.swent.skillswap.model.tags.PostTag
import com.swent.skillswap.model.tags.SkillTag
import java.util.Date
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class PostDataClassTest {

    val request1 =
        Request(
            uid = "123",
            title = "Need help with Kotlin",
            description = "Looking for an expert to teach me Kotlin.",
            ownerId = "user456",
            tags = listOf(PostTag.REOCCURRING),
            expiry = Timestamp(Date(System.currentTimeMillis() + 86400000)), // 1 day later
            creation = Timestamp.now(),
            status = PostStatus.POSTED,
            media = listOf("media_url_1", "media_url_2"),
            paymentMethod = PaymentMethod.SKILLSANDCASH
        )

    @Test
    fun testRequestDataClass() {
        val creationDate = Timestamp(Date())
        val expiryDate = Timestamp(Date(System.currentTimeMillis() + 86400000)) // 1 day later

        val request =
            Request(
                uid = "request123",
                title = "Need help with Kotlin",
                description = "Looking for an expert to teach me Kotlin.",
                ownerId = "user456",
                tags = listOf(PostTag.REOCCURRING),
                expiry = expiryDate,
                creation = creationDate,
                status = PostStatus.POSTED,
                media = listOf("media_url_1", "media_url_2"),
                paymentMethod = PaymentMethod.SKILLSANDCASH
            )

        assertEquals("request123", request.uid)
        assertEquals("Need help with Kotlin", request.title)
        assertEquals("Looking for an expert to teach me Kotlin.", request.description)
        assertEquals("user456", request.ownerId)
        assertEquals(listOf(PostTag.REOCCURRING), request.tags)
        assertEquals(expiryDate, request.expiry)
        assertEquals(creationDate, request.creation)
        assertEquals(PostStatus.POSTED, request.status)
        assertEquals(listOf("media_url_1", "media_url_2"), request.media)
        assertEquals(PaymentMethod.SKILLSANDCASH, request.paymentMethod)
        assertEquals(PostType.REQUEST, request.type)
    }

    @Test
    fun testRequestValidation_valid() {
        assertTrue(request1.validate())
    }

    @Test
    fun testRequestValidation_invalid() {
        val baseRequest = request1.copy()

        // Test invalid UID
        assertFalse(baseRequest.copy(uid = "").validate())

        // Test invalid Title
        assertFalse(baseRequest.copy(title = "").validate())

        // Test invalid Description
        assertFalse(baseRequest.copy(description = "").validate())

        // Test invalid Tags
        assertFalse(baseRequest.copy(tags = emptyList()).validate())
    }

    @Test
    fun testPostEnums() {
        // Test PostType enum
        PostType.entries.forEach { assertEquals(it, PostType.valueOf(it.name)) }

        // Test PaymentMethod enum
        PaymentMethod.entries.forEach { assertEquals(it, PaymentMethod.valueOf(it.name)) }

        // Test PostStatus enum
        PostStatus.entries.forEach { assertEquals(it, PostStatus.valueOf(it.name)) }
    }

    @Test
    fun testPostTagsEnum() {
        PostTag.entries.forEach { assertEquals(it, PostTag.valueOf(it.name)) }
    }

    @Test
    fun testEveryTagCasting() {
        val postTag = EveryTag.valueOf(PostTag.REOCCURRING.toString())
        assertEquals(PostTag.REOCCURRING.toString(), postTag.toString())

        val skillTag = EveryTag.valueOf(SkillTag.ALGORITHMS.toString())
        assertEquals(SkillTag.ALGORITHMS.toString(), skillTag.toString())
    }

    @Test
    fun testEveryTagCasting_incorrect() {
        assertThrows(IllegalArgumentException::class.java) { EveryTag.valueOf("Walter White") }
    }
}
