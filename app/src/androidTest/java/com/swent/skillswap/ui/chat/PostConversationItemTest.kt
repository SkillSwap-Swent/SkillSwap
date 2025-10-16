// AI-Generated: Comprehensive test suite for PostConversationItem component
// This file contains 9 test cases for the PostConversationItem component, covering data display,
// user interactions, skill formatting, edge cases, and null handling. Tests ensure proper rendering
// of post conversation cards and validate all display scenarios.
package com.swent.skillswap.ui.chat

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.Timestamp
import com.swent.skillswap.model.post.PaymentMethod
import com.swent.skillswap.model.post.Post
import com.swent.skillswap.model.post.PostStatus
import com.swent.skillswap.model.post.Request
import com.swent.skillswap.model.tags.SkillTag
import com.swent.skillswap.model.user.User
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PostConversationItemTest {

    @get:Rule val composeTestRule = createComposeRule()

    private fun createSamplePost(
        uid: String,
        title: String,
        ownerId: String,
        tags: List<SkillTag> = listOf(SkillTag.COMPUTER_PROGRAMMING)
    ): Post {
        val now = Timestamp.now()
        val future = Timestamp(now.seconds + 86400, 0)

        return Request(
            uid = uid,
            title = title,
            description = "Test description",
            ownerId = ownerId,
            tags = tags,
            paymentMethods = listOf(PaymentMethod.SKILLS),
            expiry = future,
            creation = now,
            status = PostStatus.POSTED,
            media = emptyList()
        )
    }

    private fun createSampleUser(uid: String, username: String): User {
        return User(
            uid = uid,
            username = username,
            email = "test@example.com",
            profilePicture = "",
            skillSet = setOf(),
            rating = 4.5f,
            availability = listOf()
        )
    }

    @Test
    fun postConversationItem_displaysUsername() {
        val post = createSamplePost("post1", "Test Post", "user1")
        val user = createSampleUser("user1", "Test User")

        composeTestRule.setContent { PostConversationItem(post = post, user = user, onClick = {}) }

        composeTestRule.onNodeWithText("Test User").assertExists()
    }

    @Test
    fun postConversationItem_displaysPostTitle() {
        val post = createSamplePost("post1", "Spanish Tutoring", "user1")
        val user = createSampleUser("user1", "Alex Johnson")

        composeTestRule.setContent { PostConversationItem(post = post, user = user, onClick = {}) }

        composeTestRule.onNodeWithText("Spanish Tutoring").assertExists()
    }

    @Test
    fun postConversationItem_displaysSkills() {
        val post =
            createSamplePost(
                "post1",
                "Math Help",
                "user1",
                listOf(SkillTag.CALCULUS, SkillTag.LINEAR_ALGEBRA)
            )
        val user = createSampleUser("user1", "Math Expert")

        composeTestRule.setContent { PostConversationItem(post = post, user = user, onClick = {}) }

        composeTestRule.onNodeWithText("Skills:").assertExists()
        composeTestRule.onNodeWithText("Calculus, Linear Algebra").assertExists()
    }

    @Test
    fun postConversationItem_handlesNullUser() {
        val post = createSamplePost("post1", "Test Post", "unknown")

        composeTestRule.setContent { PostConversationItem(post = post, user = null, onClick = {}) }

        composeTestRule.onNodeWithText("Unknown User").assertExists()
    }

    @Test
    fun postConversationItem_triggersOnClick() {
        val post = createSamplePost("post1", "Test Post", "user1")
        val user = createSampleUser("user1", "Test User")
        var clicked = false

        composeTestRule.setContent {
            PostConversationItem(post = post, user = user, onClick = { clicked = true })
        }

        composeTestRule.onNodeWithText("Test User").performClick()
        assert(clicked)
    }

    @Test
    fun postConversationItem_displaysLimitedSkills() {
        val post =
            createSamplePost(
                "post1",
                "Multiple Skills",
                "user1",
                listOf(
                    SkillTag.COMPUTER_PROGRAMMING,
                    SkillTag.DATA_STRUCTURES,
                    SkillTag.ALGORITHMS,
                    SkillTag.DATABASES
                )
            )
        val user = createSampleUser("user1", "Tech Expert")

        composeTestRule.setContent { PostConversationItem(post = post, user = user, onClick = {}) }

        // Should only show first 2 skills
        composeTestRule.onNodeWithText("Computer Programming, Data Structures").assertExists()
    }

    @Test
    fun postConversationItem_handlesEmptySkills() {
        val post = createSamplePost("post1", "No Skills", "user1", emptyList())
        val user = createSampleUser("user1", "No Skills User")

        composeTestRule.setContent { PostConversationItem(post = post, user = user, onClick = {}) }

        composeTestRule.onNodeWithText("No Skills User").assertExists()
        composeTestRule.onNodeWithText("No Skills").assertExists()
    }

    @Test
    fun postConversationItem_formatsSkillNames() {
        val post =
            createSamplePost(
                "post1",
                "Formatted Skills",
                "user1",
                listOf(SkillTag.LINEAR_ALGEBRA, SkillTag.COMPUTER_PROGRAMMING)
            )
        val user = createSampleUser("user1", "Format Test")

        composeTestRule.setContent { PostConversationItem(post = post, user = user, onClick = {}) }

        // Should format skill names properly
        composeTestRule.onNodeWithText("Linear Algebra, Computer Programming").assertExists()
    }

    @Test
    fun postConversationItem_rendersWithoutCrashing() {
        val post = createSamplePost("post1", "Stable Post", "user1")
        val user = createSampleUser("user1", "Stable User")

        composeTestRule.setContent { PostConversationItem(post = post, user = user, onClick = {}) }

        // Should render without any exceptions
        composeTestRule.onNodeWithText("Stable User").assertExists()
    }
}
