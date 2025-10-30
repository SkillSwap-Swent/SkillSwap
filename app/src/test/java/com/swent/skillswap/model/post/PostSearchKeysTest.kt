package com.swent.skillswap.model.post

import com.google.firebase.Timestamp
import com.swent.skillswap.model.tags.SkillTag
import org.junit.Assert.assertTrue
import org.junit.Test

class PostSearchKeysTest {

    private fun now() = Timestamp.now()
    private fun future() = Timestamp(now().seconds + 3600, 0)

    @Test
    fun offer_buildsSearchKeys_fromTitleTagsAndPayments() {
        val post = Offer(
            uid = "o1",
            title = "Learn React Basics",
            description = "desc",
            ownerId = "u1",
            tags = listOf(SkillTag.COMPUTER_PROGRAMMING, SkillTag.DATA_STRUCTURES),
            paymentMethods = listOf(PaymentMethod.SKILLS, PaymentMethod.CASH),
            expiry = future(),
            creation = now(),
            status = PostStatus.POSTED,
            media = emptyList()
        )

        val keys = post.searchKeys
        assertTrue("learn" in keys)
        assertTrue("react" in keys)
        assertTrue("basics" in keys)
        assertTrue("skills" in keys)
        assertTrue("cash" in keys)
        assertTrue(SkillTag.COMPUTER_PROGRAMMING.toString().lowercase() in keys)
        assertTrue(SkillTag.DATA_STRUCTURES.toString().lowercase() in keys)
    }

    @Test
    fun request_buildsSearchKeys_fromTitleTagsAndPayments() {
        val post = Request(
            uid = "r1",
            title = "Math Linear Algebra",
            description = "desc",
            ownerId = "u2",
            tags = listOf(SkillTag.LINEAR_ALGEBRA),
            paymentMethods = listOf(PaymentMethod.SKILLS),
            expiry = future(),
            creation = now(),
            status = PostStatus.POSTED,
            media = emptyList()
        )

        val keys = post.searchKeys
        assertTrue("math" in keys)
        assertTrue("linear" in keys)
        assertTrue("algebra" in keys)
        assertTrue("skills" in keys)
        assertTrue(SkillTag.LINEAR_ALGEBRA.toString().lowercase() in keys)
    }
}


