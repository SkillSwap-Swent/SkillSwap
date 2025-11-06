/*
 * Body of test written by copilot
 */
package com.swent.skillswap.model.chat

import org.junit.Assert.*
import org.junit.Test

class ChatUtilsTest {
    @Test
    fun testSerializeDeserializeMultipleMessages() {
        val messages =
            listOf(
                Message("msg1", "user1", "Hello", 1616161616L),
                Message("msg2", "user2", "Hi there!", 1616161626L),
                Message("msg3", "user1", "How are you?", 1616161636L)
            )

        messages.forEach { originalMessage ->
            val serialized = serializeMessage(originalMessage)
            val deserialized = deserializeMessage(serialized)

            assertEquals(originalMessage.id, deserialized.id)
            assertEquals(originalMessage.senderId, deserialized.senderId)
            assertEquals(originalMessage.content, deserialized.content)
            assertEquals(originalMessage.timestamp, deserialized.timestamp)
        }
    }

    @Test
    fun testDeserializeInvalidDataThrowsException() {
        val emptyIdMessage = Message(
            id = "",
            senderId = "user1",
            content = "Hello",
            timestamp = 1616161616L
        )

        val emptyUserIdMessage = Message(
            id = "msg1",
            senderId = "",
            content = "Hello",
            timestamp = 1616161616L
        )

        val emptyContentMessage = Message(
            id = "msg1",
            senderId = "user1",
            content = "",
            timestamp = 1616161616L
        )

        val negativeTimestampMessage = Message(
            id = "msg1",
            senderId = "user1",
            content = "Hello",
            timestamp = -1616161616L
        )

        val messagesToTest = listOf(
            emptyIdMessage,
            emptyUserIdMessage,
            emptyContentMessage,
            negativeTimestampMessage
        )

        messagesToTest.forEach { message ->
           assertThrows(IllegalArgumentException::class.java) {
               serializeMessage(message)
           }
        }

    }
}
