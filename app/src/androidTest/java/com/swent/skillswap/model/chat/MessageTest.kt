/* Written with the help of Sonnet 4.5 */

package com.swent.skillswap.model.chat

import org.junit.Assert.*
import org.junit.Test

class MessageTest {

    @Test
    fun createMessageAndCheckProperties() {
        val message =
            Message(
                id = "msg123",
                senderId = "user456",
                content = "Hello, world!",
                timestamp = 1234567890L
            )

        assertEquals("msg123", message.id)
        assertEquals("user456", message.senderId)
        assertEquals("Hello, world!", message.content)
        assertEquals(1234567890L, message.timestamp)
    }

    @Test
    fun createEmptyMessage() {
        val message = Message("", "", "", 0L)

        assertEquals("", message.id)
        assertEquals("", message.senderId)
        assertEquals("", message.content)
        assertEquals(0L, message.timestamp)
    }
}