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
}
