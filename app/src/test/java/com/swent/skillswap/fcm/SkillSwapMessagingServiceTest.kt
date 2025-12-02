/** Created with the help of Cursor */
package com.swent.skillswap.fcm

import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.messaging.RemoteMessage
import com.swent.skillswap.model.notification.NotificationType
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLog

@Config(sdk = [33])
@org.junit.runner.RunWith(RobolectricTestRunner::class)
class SkillSwapMessagingServiceTest {

    private lateinit var service: SkillSwapMessagingService

    @Before
    fun setUp() {
        val context = RuntimeEnvironment.getApplication()
        try {
            FirebaseApp.getInstance()
        } catch (e: IllegalStateException) {
            val options =
                FirebaseOptions.Builder()
                    .setApplicationId("test-app-id")
                    .setApiKey("test-api-key")
                    .setProjectId("test-project")
                    .build()
            FirebaseApp.initializeApp(context, options)
        }
        service = SkillSwapMessagingService()
    }

    @Test
    fun onNewToken_withValidToken_doesNotCrash() {
        val testToken = "test-fcm-token-12345"
        service.onNewToken(testToken)
        // Verify method completes without throwing
    }

    @Test
    fun onMessageReceived_logsMessageData() {
        val remoteMessage = mockk<RemoteMessage>(relaxed = true)
        every { remoteMessage.from } returns "test-sender"
        every { remoteMessage.data } returns mapOf("key1" to "value1")
        every { remoteMessage.notification } returns null

        service.onMessageReceived(remoteMessage)

        verify { remoteMessage.from }
        verify { remoteMessage.data }
    }

    @Test
    fun onMessageReceived_withNotification_withNoType_logs_error() {
        val remoteMessage = mockk<RemoteMessage>(relaxed = true)
        val notification = mockk<RemoteMessage.Notification>(relaxed = true)
        every { remoteMessage.from } returns "test-sender"
        every { remoteMessage.data } returns emptyMap()
        every { remoteMessage.notification } returns notification
        every { notification.title } returns "Test Title"
        every { notification.body } returns "Test Body"

        ShadowLog.clear()
        service.onMessageReceived(remoteMessage)

        val warningLogs =
            ShadowLog.getLogs().filter {
                it.type == android.util.Log.WARN && it.msg.contains("Unknown notification type")
            }
        assert(warningLogs.isNotEmpty()) { "Expected warning log for unknown notification type" }
    }

    @Test
    fun onMessageReceived_withChatNotification_logs_chat_handling() {
        val remoteMessage = mockk<RemoteMessage>(relaxed = true)
        every { remoteMessage.from } returns "test-sender"
        every { remoteMessage.data } returns
            mapOf("type" to NotificationType.MESSAGE.name, "relatedId" to "chat123")
        every { remoteMessage.notification } returns null

        ShadowLog.clear()
        service.onMessageReceived(remoteMessage)

        val chatLogs =
            ShadowLog.getLogs().filter {
                it.type == android.util.Log.DEBUG && it.msg.contains("Handling chat notification")
            }
        assert(chatLogs.isNotEmpty()) { "Expected log for handling chat notification" }
    }

    @Test
    fun onMessageReceived_withPostNotification_logs_post_handling() {
        val remoteMessage = mockk<RemoteMessage>(relaxed = true)
        every { remoteMessage.from } returns "test-sender"
        every { remoteMessage.data } returns
            mapOf("type" to NotificationType.POST_ACCEPTED.name, "relatedId" to "post456")
        every { remoteMessage.notification } returns null

        ShadowLog.clear()
        service.onMessageReceived(remoteMessage)

        val postLogs =
            ShadowLog.getLogs().filter {
                it.type == android.util.Log.DEBUG &&
                    it.msg.contains("Handling accepted post notification")
            }
        assert(postLogs.isNotEmpty()) { "Expected log for handling accepted post notification" }
    }
}
