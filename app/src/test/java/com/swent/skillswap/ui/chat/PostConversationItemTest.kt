package com.swent.skillswap.ui.chat

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.Timestamp
import com.swent.skillswap.model.post.Offer
import com.swent.skillswap.model.post.PaymentMethod
import com.swent.skillswap.model.post.PostStatus
import com.swent.skillswap.model.tags.SkillTag
import com.swent.skillswap.model.user.User
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PostConversationItemTest {

    @get:Rule val composeRule = createComposeRule()

    private fun offer(): Offer =
        Offer(
            uid = "o1",
            title = "Graphic Design Help",
            description = "desc",
            ownerId = "u1",
            tags = listOf(SkillTag.COMPUTER_PROGRAMMING, SkillTag.DATA_STRUCTURES),
            paymentMethods = listOf(PaymentMethod.SKILLS),
            expiry = Timestamp(Timestamp.now().seconds + 86400, 0),
            creation = Timestamp(Timestamp.now().seconds - 10, 0),
            status = PostStatus.POSTED,
            media = emptyList()
        )

    @Test
    fun renders_username_title_and_tags() {
        val user = User("u1", "Sarah Chen", "sarah@example.com", "", emptySet(), 4.8f, emptyList())

        composeRule.setContent {
            MaterialTheme { PostConversationItem(post = offer(), user = user, onClick = {}) }
        }

        composeRule.onNodeWithText("Sarah Chen").assertExists()
        composeRule.onNodeWithText("Graphic Design Help").assertExists()
        composeRule.onNodeWithText("Skills:").assertExists()
    }

    @Test
    fun shows_unknown_user_when_missing() {
        composeRule.setContent {
            MaterialTheme { PostConversationItem(post = offer(), user = null, onClick = {}) }
        }
        composeRule.onNodeWithText("Unknown User").assertExists()
    }

    @Test
    fun formats_tag_names_with_readable_casing() {
        composeRule.setContent {
            MaterialTheme { PostConversationItem(post = offer(), user = null, onClick = {}) }
        }
        // We expect something like "Computer programming, Data structures"
        // Check presence of at least one properly cased token (as substring)
        composeRule.onNodeWithText("Computer programming", substring = true).assertExists()
    }

    @Test
    fun handles_no_tags_gracefully() {
        val noTagOffer = offer().copy(tags = emptyList())
        composeRule.setContent {
            MaterialTheme { PostConversationItem(post = noTagOffer, user = null, onClick = {}) }
        }
        // Should still render without crashing; label exists, and shows "No skills listed"
        composeRule.onNodeWithText("Skills:").assertExists()
        composeRule.onNodeWithText("No skills listed").assertExists()
    }

    @Test
    fun renders_only_first_two_tags_when_more_present() {
        val manyTags =
            offer()
                .copy(
                    tags =
                        listOf(
                            SkillTag.COMPUTER_PROGRAMMING,
                            SkillTag.DATA_STRUCTURES,
                            SkillTag.ALGORITHMS
                        )
                )
        composeRule.setContent {
            MaterialTheme { PostConversationItem(post = manyTags, user = null, onClick = {}) }
        }
        // First two visible
        composeRule.onNodeWithText("Computer programming", substring = true).assertExists()
        composeRule.onNodeWithText("Data structures", substring = true).assertExists()
        // Third should not appear due to take(2)
        composeRule.onNodeWithText("Algorithms", substring = true).assertDoesNotExist()
    }

    @Test
    fun long_title_renders_without_crash() {
        val longTitle = offer().copy(title = "A".repeat(200))
        composeRule.setContent {
            MaterialTheme { PostConversationItem(post = longTitle, user = null, onClick = {}) }
        }
        // Substring exists; ellipsis not directly testable in unit tests
        composeRule.onNodeWithText("AAAA", substring = true).assertExists()
    }
}
