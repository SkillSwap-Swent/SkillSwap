package com.swent.skillswap.ui.chat

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import java.util.concurrent.atomic.AtomicInteger
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PostTypeFilterButtonTest {

    @get:Rule val composeRule = createComposeRule()

    @Test
    fun button_displaysText_and_clicks() {
        val clicks = AtomicInteger(0)

        composeRule.setContent {
            MaterialTheme {
                PostTypeFilterButton(
                    text = "FeedOffer",
                    isSelected = false,
                    onClick = { clicks.incrementAndGet() }
                )
            }
        }

        composeRule.onNodeWithText("FeedOffer").assertExists().performClick()
        assert(clicks.get() == 1)
    }

    @Test
    fun button_selected_and_unselected_states_render() {
        composeRule.setContent {
            MaterialTheme {
                PostTypeFilterButton(text = "Request", isSelected = true, onClick = {})
            }
        }

        composeRule.onNodeWithText("Request").assertExists()
    }
}
