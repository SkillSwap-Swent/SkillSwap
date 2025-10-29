package com.swent.skillswap.post

import com.google.firebase.Timestamp
import com.swent.skillswap.model.post.Offer
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
            paymentMethods = listOf(PaymentMethod.SKILLS, PaymentMethod.CASH)
        )

    val offer1 =
        Offer(
            uid = "123",
            title = "Offering help with Kotlin",
            description = "Took CS-311. I am an expert in Kotlin",
            ownerId = "user678",
            tags = listOf(PostTag.REOCCURRING),
            expiry = Timestamp(Date(System.currentTimeMillis() + 86400000)), // 1 day later
            creation = Timestamp.now(),
            status = PostStatus.POSTED,
            media = listOf("media_url_1", "media_url_2"),
            paymentMethods = listOf(PaymentMethod.SKILLS, PaymentMethod.CASH)
        )

    @Test
    fun testRequestDataClass() {
        val creationDate = Timestamp(Date())
        val expiryDate = Timestamp(Date(System.currentTimeMillis() + 86400000)) // 1 day later

        val request =
            Request(
                uid = "123",
                title = "Need help with Kotlin",
                description = "Looking for an expert to teach me Kotlin.",
                ownerId = "user456",
                tags = listOf(PostTag.REOCCURRING),
                expiry = expiryDate,
                creation = creationDate,
                status = PostStatus.POSTED,
                media = listOf("media_url_1", "media_url_2"),
                paymentMethods = listOf(PaymentMethod.SKILLS, PaymentMethod.CASH)
            )

        assertEquals("123", request.uid)
        assertEquals("Need help with Kotlin", request.title)
        assertEquals("Looking for an expert to teach me Kotlin.", request.description)
        assertEquals("user456", request.ownerId)
        assertEquals(listOf(PostTag.REOCCURRING), request.tags)
        assertEquals(expiryDate, request.expiry)
        assertEquals(creationDate, request.creation)
        assertEquals(PostStatus.POSTED, request.status)
        assertEquals(listOf("media_url_1", "media_url_2"), request.media)
        assertEquals(listOf(PaymentMethod.SKILLS, PaymentMethod.CASH), request.paymentMethods)
        assertEquals(PostType.REQUEST, request.type)
    }

    @Test
    fun testOfferDataClass() {
        val creationDate = Timestamp(Date())
        val expiryDate = Timestamp(Date(System.currentTimeMillis() + 86400000)) // 1 day later

        val offer =
            Offer(
                uid = "123",
                title = "Offering help with Kotlin",
                description = "Took CS-311. I am an expert in Kotlin",
                ownerId = "user678",
                tags = listOf(PostTag.REOCCURRING),
                expiry = expiryDate,
                creation = creationDate,
                status = PostStatus.POSTED,
                media = listOf("media_url_1", "media_url_2"),
                paymentMethods = listOf(PaymentMethod.SKILLS, PaymentMethod.CASH)
            )

        assertEquals("123", offer.uid)
        assertEquals("Offering help with Kotlin", offer.title)
        assertEquals("Took CS-311. I am an expert in Kotlin", offer.description)
        assertEquals("user678", offer.ownerId)
        assertEquals(listOf(PostTag.REOCCURRING), offer.tags)
        assertEquals(expiryDate, offer.expiry)
        assertEquals(creationDate, offer.creation)
        assertEquals(PostStatus.POSTED, offer.status)
        assertEquals(listOf("media_url_1", "media_url_2"), offer.media)
        assertEquals(listOf(PaymentMethod.SKILLS, PaymentMethod.CASH), offer.paymentMethods)
        assertEquals(PostType.OFFER, offer.type)
    }

    @Test
    fun testRequestValidation_valid() {
        assertTrue(request1.validate())
    }

    @Test
    fun testOfferValidation_valid() {
        assertTrue(offer1.validate())
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
    fun testOfferValidation_invalid() {
        val baseOffer = offer1.copy()

        // Test invalid UID
        assertFalse(baseOffer.copy(uid = "").validate())

        // Test invalid Title
        assertFalse(baseOffer.copy(title = "").validate())

        // Test invalid Description
        assertFalse(baseOffer.copy(description = "").validate())

        // Test invalid Tags
        assertFalse(baseOffer.copy(tags = emptyList()).validate())
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
