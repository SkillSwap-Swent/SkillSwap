package com.swent.skillswap.chat

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.swent.skillswap.ui.chat.ChatScreen
import org.junit.Rule
import org.junit.Test

class ChatScreenTest {

    @get:Rule val composeTestRule = createComposeRule()

    @Test
    fun chatScreen_displaysPlaceholderText() {
        composeTestRule.setContent { ChatScreen() }
        composeTestRule.onNodeWithText("Placeholder Chat Screen").assertIsDisplayed()
    }
}
