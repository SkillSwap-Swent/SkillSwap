package com.swent.skillswap.ui.chat

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.Timestamp
import com.swent.skillswap.model.post.Offer
import com.swent.skillswap.model.post.PaymentMethod
import com.swent.skillswap.model.post.Post
import com.swent.skillswap.model.post.PostStatus
import com.swent.skillswap.model.post.PostType
import com.swent.skillswap.model.post.Request
import com.swent.skillswap.model.tags.SkillTag
import com.swent.skillswap.model.user.User
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ChatScreenTest {

    @get:Rule val composeRule = createComposeRule()

    private fun now() = Timestamp.now().seconds

    private fun futureTs() = Timestamp(now() + 86400, 0)

    private fun pastTs() = Timestamp(now() - 10, 0)

    private fun samplePosts(): List<Post> =
        listOf(
            Offer(
                "o1",
                "Graphic Design Help",
                "desc",
                "u2",
                setOf(SkillTag.COMPUTER_PROGRAMMING),
                PaymentMethod.SKILLS,
                futureTs(),
                pastTs(),
                PostStatus.POSTED,
                emptyList()
            ),
            Request(
                "r1",
                "Need Math Tutor",
                "desc",
                "u1",
                setOf(SkillTag.CALCULUS),
                PaymentMethod.SKILLS,
                futureTs(),
                pastTs(),
                PostStatus.POSTED,
                emptyList()
            )
        )

    private fun users(): Map<String, User> =
        mapOf(
            "u1" to User("u1", "Alex Johnson", "", "", emptySet(), 4.5f, emptyList()),
            "u2" to User("u2", "Sarah Chen", "", "", emptySet(), 4.8f, emptyList())
        )

    @Test
    fun shows_title_and_filters_and_list() {
        composeRule.setContent {
            MaterialTheme { ChatScreen(posts = samplePosts(), users = users()) }
        }

        composeRule.onNodeWithText("Chat").assertExists()
        composeRule.onNodeWithText("FeedOffer").assertExists()
        composeRule.onNodeWithText("Request").assertExists()
        // default is FeedOffer selected, should show offer title
        composeRule.onNodeWithText("Graphic Design Help").assertExists()
    }

    @Test
    fun clicking_request_filter_shows_request_posts() {
        composeRule.setContent {
            MaterialTheme { ChatScreen(posts = samplePosts(), users = users()) }
        }

        composeRule.onNodeWithText("Request").performClick()
        composeRule.onNodeWithText("Need Math Tutor").assertExists()
    }

    @Test
    fun empty_state_when_no_posts_for_filter() {
        val onlyOffers = samplePosts().filter { it.type == PostType.OFFER }
        composeRule.setContent { MaterialTheme { ChatScreen(posts = onlyOffers, users = users()) } }

        // Switch to Request to force empty state
        composeRule.onNodeWithText("Request").performClick()
        composeRule.onNodeWithText("No request posts available").assertExists()
    }

    @Test
    fun unknown_user_fallback_is_displayed() {
        val posts = samplePosts()
        val noUsers = emptyMap<String, User>()
        composeRule.setContent { MaterialTheme { ChatScreen(posts = posts, users = noUsers) } }
        composeRule.onNodeWithText("Unknown User").assertExists()
    }

    @Test
    fun post_item_click_triggers_callback() {
        var clicks = 0
        composeRule.setContent {
            MaterialTheme {
                ChatScreen(posts = samplePosts(), users = users(), onPostClick = { clicks++ })
            }
        }
        // Default screen shows offer "Graphic Design Help"
        composeRule.onNodeWithText("Graphic Design Help").performClick()
        assert(clicks == 1)
    }

    @Test
    fun toggling_filters_multiple_times_updates_list() {
        composeRule.setContent {
            MaterialTheme { ChatScreen(posts = samplePosts(), users = users()) }
        }

        // FeedOffer visible first
        composeRule.onNodeWithText("Graphic Design Help").assertExists()
        composeRule.onNodeWithText("Request").performClick()
        composeRule.onNodeWithText("Need Math Tutor").assertExists()
        composeRule.onNodeWithText("FeedOffer").performClick()
        composeRule.onNodeWithText("Graphic Design Help").assertExists()
    }

    @Test
    fun clicking_item_in_request_mode_calls_callback() {
        var clicks = 0
        composeRule.setContent {
            MaterialTheme {
                ChatScreen(posts = samplePosts(), users = users(), onPostClick = { clicks++ })
            }
        }
        composeRule.onNodeWithText("Request").performClick()
        composeRule.onNodeWithText("Need Math Tutor").performClick()
        assert(clicks == 1)
    }

    @Test
    fun empty_state_when_no_offers() {
        val onlyRequests = samplePosts().filter { it.type == PostType.REQUEST }
        composeRule.setContent {
            MaterialTheme { ChatScreen(posts = onlyRequests, users = users()) }
        }
        // FeedOffer tab is default; should show empty for offers
        composeRule.onNodeWithText("No offer posts available").assertExists()
    }

    @Test
    fun mixed_known_and_unknown_users_render() {
        // Provide users map missing one of the owners
        val partialUsers =
            mapOf("u1" to User("u1", "Alex Johnson", "", "", emptySet(), 4.5f, emptyList()))
        composeRule.setContent {
            MaterialTheme { ChatScreen(posts = samplePosts(), users = partialUsers) }
        }
        // FeedOffer owner is u2 (unknown) → shows Unknown User
        composeRule.onNodeWithText("Unknown User").assertExists()
        // Switch to request (owner u1) → shows known user
        composeRule.onNodeWithText("Request").performClick()
        composeRule.onNodeWithText("Alex Johnson").assertExists()
    }
}
