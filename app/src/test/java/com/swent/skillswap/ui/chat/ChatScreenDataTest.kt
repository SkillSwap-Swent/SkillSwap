// AI-Generated: Comprehensive test suite for ChatScreenData
// This file contains 12 test cases for the ChatScreenData object, covering data validation,
// integrity checks, and relationship validation. Tests ensure sample data is properly structured
// and maintains consistency with the Post and User models used throughout the application.
package com.swent.skillswap.ui.chat

import com.swent.skillswap.model.post.PostStatus
import com.swent.skillswap.model.post.PostType
import com.swent.skillswap.model.tags.SkillTag
import org.junit.Assert.*
import org.junit.Test

class ChatScreenDataTest {

    @Test
    fun getSampleUsers_returnsCorrectNumberOfUsers() {
        val users = ChatScreenData.getSampleUsers()
        assertEquals(4, users.size)
    }

    @Test
    fun getSampleUsers_containsExpectedUsers() {
        val users = ChatScreenData.getSampleUsers()

        assertTrue(users.containsKey("user1"))
        assertTrue(users.containsKey("user2"))
        assertTrue(users.containsKey("user3"))
        assertTrue(users.containsKey("user4"))
    }

    @Test
    fun getSampleUsers_hasCorrectUsernames() {
        val users = ChatScreenData.getSampleUsers()

        assertEquals("Alex Johnson", users["user1"]?.username)
        assertEquals("Sarah Chen", users["user2"]?.username)
        assertEquals("Mike Rodriguez", users["user3"]?.username)
        assertEquals("Emma Wilson", users["user4"]?.username)
    }

    @Test
    fun getSampleUsers_hasValidUserData() {
        val users = ChatScreenData.getSampleUsers()
        val user = users["user1"]

        assertNotNull(user)
        assertEquals("user1", user?.uid)
        assertEquals("alex@example.com", user?.email)
        assertTrue(user?.rating ?: 0f > 0f)
    }

    @Test
    fun getSamplePosts_returnsCorrectNumberOfPosts() {
        val posts = ChatScreenData.getSamplePosts()
        assertEquals(5, posts.size)
    }

    @Test
    fun getSamplePosts_containsOfferPosts() {
        val posts = ChatScreenData.getSamplePosts()
        val offerPosts = posts.filter { it.type == PostType.OFFER }

        assertEquals(3, offerPosts.size)
    }

    @Test
    fun getSamplePosts_containsRequestPosts() {
        val posts = ChatScreenData.getSamplePosts()
        val requestPosts = posts.filter { it.type == PostType.REQUEST }

        assertEquals(2, requestPosts.size)
    }

    @Test
    fun getSamplePosts_hasValidPostData() {
        val posts = ChatScreenData.getSamplePosts()
        val post = posts.first()

        assertNotNull(post.uid)
        assertNotNull(post.title)
        assertNotNull(post.description)
        assertNotNull(post.ownerId)
        assertTrue(post.tags.isNotEmpty())
        assertEquals(PostStatus.POSTED, post.status)
    }

    @Test
    fun getSamplePosts_hasValidTimestamps() {
        val posts = ChatScreenData.getSamplePosts()
        val post = posts.first()

        assertNotNull(post.creation)
        assertNotNull(post.expiry)
        assertTrue(post.expiry.seconds > post.creation.seconds)
    }

    @Test
    fun getSamplePosts_hasValidSkills() {
        val posts = ChatScreenData.getSamplePosts()
        val post = posts.first()

        assertTrue(post.tags.isNotEmpty())
        assertTrue(post.tags.all { it is SkillTag })
    }

    @Test
    fun getSamplePosts_hasValidPaymentMethods() {
        val posts = ChatScreenData.getSamplePosts()
        val post = posts.first()

        assertTrue(post.paymentMethod.name.isNotEmpty())
    }

    @Test
    fun getSamplePosts_hasUniqueIds() {
        val posts = ChatScreenData.getSamplePosts()
        val ids = posts.map { it.uid }

        assertEquals(ids.size, ids.distinct().size)
    }

    @Test
    fun getSamplePosts_hasValidOwnerIds() {
        val posts = ChatScreenData.getSamplePosts()
        val users = ChatScreenData.getSampleUsers()

        posts.forEach { post -> assertTrue(users.containsKey(post.ownerId)) }
    }
}
