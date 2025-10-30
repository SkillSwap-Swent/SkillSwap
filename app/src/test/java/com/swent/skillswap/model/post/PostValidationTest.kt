package com.swent.skillswap.model.post

import com.google.firebase.Timestamp
import com.swent.skillswap.model.tags.SkillTag
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PostValidationTest {

    private fun now() = Timestamp.now()
    private fun past() = Timestamp(now().seconds - 3600, 0)
    private fun future() = Timestamp(now().seconds + 3600, 0)

    @Test
    fun request_validate_true_on_validFields() {
        val post = Request(
            uid = "id",
            title = "Title",
            description = "Desc",
            ownerId = "owner",
            tags = listOf(SkillTag.CALCULUS),
            paymentMethods = listOf(PaymentMethod.SKILLS),
            expiry = future(),
            creation = past(),
            status = PostStatus.POSTED,
            media = emptyList()
        )
        assertTrue(post.validate())
    }

    @Test
    fun request_validate_false_on_missingFieldsOrWrongTimestamps() {
        val invalid = Request(
            uid = "",
            title = "",
            description = "",
            ownerId = "owner",
            tags = emptyList(),
            paymentMethods = emptyList(),
            expiry = past(),
            creation = future(),
            status = PostStatus.POSTED,
            media = emptyList()
        )
        assertFalse(invalid.validate())
    }
}


