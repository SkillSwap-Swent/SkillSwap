/** Created with the help of Cursor */
package com.swent.skillswap.fcm

import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.messaging.RemoteMessage
import com.swent.skillswap.model.chat.CurrentChatTracker
import com.swent.skillswap.model.notification.NotificationRepository
import com.swent.skillswap.model.notification.NotificationType
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
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
        } catch (_: IllegalStateException) {
            val options =
                FirebaseOptions.Builder()
                    .setApplicationId("test-app-id")
                    .setApiKey("test-api-key")
                    .setProjectId("test-project")
                    .build()
            FirebaseApp.initializeApp(context, options)
        }
        service =
            org.robolectric.Robolectric.buildService(SkillSwapMessagingService::class.java)
                .create()
                .get()
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
        val notification = mockk<RemoteMessage.Notification>(relaxed = true)
        every { remoteMessage.from } returns "test-sender"
        every { remoteMessage.data } returns
            mapOf("type" to NotificationType.MESSAGE.name, "relatedId" to "chat123")
        every { remoteMessage.notification } returns notification

        ShadowLog.clear()
        service.onMessageReceived(remoteMessage)

        val chatLogs =
            ShadowLog.getLogs().filter {
                it.type == android.util.Log.DEBUG && it.msg.contains("Handling chat notification")
            }
        assert(chatLogs.isNotEmpty()) { "Expected log for handling chat notification" }
    }

    @Test
    fun onMessageReceived_withPostAcceptedNotification_logs_post_handling() {
        val remoteMessage = mockk<RemoteMessage>(relaxed = true)
        val notification = mockk<RemoteMessage.Notification>(relaxed = true)
        every { remoteMessage.from } returns "test-sender"
        every { remoteMessage.data } returns
            mapOf("type" to NotificationType.POST_ACCEPTED.name, "relatedId" to "post456")
        every { remoteMessage.notification } returns notification
        every { notification.title } returns "Post Accepted"
        every { notification.body } returns "Your reply was accepted"

        ShadowLog.clear()
        service.onMessageReceived(remoteMessage)

        val postLogs =
            ShadowLog.getLogs().filter {
                it.type == android.util.Log.DEBUG && it.msg.contains("Handling post notification")
            }
        assert(postLogs.isNotEmpty()) { "Expected log for handling post notification" }
    }

    @Test
    fun onMessageReceived_withPostReplyNotification_logs_post_handling() {
        val remoteMessage = mockk<RemoteMessage>(relaxed = true)
        val notification = mockk<RemoteMessage.Notification>(relaxed = true)
        every { remoteMessage.from } returns "test-sender"
        every { remoteMessage.data } returns
            mapOf("type" to NotificationType.POST_REPLY.name, "relatedId" to "post789")
        every { remoteMessage.notification } returns notification
        every { notification.title } returns "New Post Reply"
        every { notification.body } returns "Someone replied to your post"

        ShadowLog.clear()
        service.onMessageReceived(remoteMessage)

        val postLogs =
            ShadowLog.getLogs().filter {
                it.type == android.util.Log.DEBUG && it.msg.contains("Handling post notification")
            }
        assert(postLogs.isNotEmpty()) { "Expected log for handling post notification" }
    }

    @Test
    fun onMessageReceived_withPostRejectedNotification_logs_post_handling() {
        val remoteMessage = mockk<RemoteMessage>(relaxed = true)
        val notification = mockk<RemoteMessage.Notification>(relaxed = true)
        every { remoteMessage.from } returns "test-sender"
        every { remoteMessage.data } returns
            mapOf("type" to NotificationType.POST_REJECTED.name, "relatedId" to "post101")
        every { remoteMessage.notification } returns notification
        every { notification.title } returns "Post Rejected"
        every { notification.body } returns "Your reply was rejected"

        ShadowLog.clear()
        service.onMessageReceived(remoteMessage)

        val postLogs =
            ShadowLog.getLogs().filter {
                it.type == android.util.Log.DEBUG && it.msg.contains("Handling post notification")
            }
        assert(postLogs.isNotEmpty()) { "Expected log for handling post notification" }
    }

    @Test
    fun onMessageReceived_userInChat_marksChatNotificationsAsRead_and_returns() {
        val relatedId = "chat123"
        val remoteMessage = mockk<RemoteMessage>(relaxed = true)
        every { remoteMessage.from } returns "test-sender"
        every { remoteMessage.data } returns
            mapOf("type" to NotificationType.MESSAGE.name, "relatedId" to relatedId)
        every { remoteMessage.notification } returns null

        // Set current chat to the same relatedId so the service should mark as read and return
        CurrentChatTracker.currentChatId = relatedId

        // Mock FirebaseAuth.getInstance() to return a FirebaseAuth with a current user
        mockkStatic(FirebaseAuth::class)
        val mockAuth = mockk<FirebaseAuth>(relaxed = true)
        val mockUser = mockk<FirebaseUser>(relaxed = true)
        every { mockAuth.currentUser } returns mockUser
        every { mockUser.uid } returns "user-1"
        every { FirebaseAuth.getInstance() } returns mockAuth

        // Create a mock NotificationRepository and inject it by replacing the lazy delegate
        val mockRepo = mockk<NotificationRepository>(relaxed = true)
        val delegateField = service.javaClass.getDeclaredField("notificationRepositery\$delegate")
        delegateField.isAccessible = true
        delegateField.set(service, lazy { mockRepo })

        ShadowLog.clear()
        service.onMessageReceived(remoteMessage)

        // Verify that markChatNotificationsAsRead was called for the related chat and user
        io.mockk.coVerify(timeout = 2000) {
            mockRepo.markChatNotificationsAsRead(relatedId, "user-1")
        }

        val debugLogs =
            ShadowLog.getLogs().filter {
                it.type == android.util.Log.DEBUG && it.msg.contains("User is in chat $relatedId")
            }
        assert(debugLogs.isNotEmpty()) { "Expected debug log about user in chat" }

        // Cleanup
        CurrentChatTracker.currentChatId = null
        unmockkStatic(FirebaseAuth::class)
    }
}
