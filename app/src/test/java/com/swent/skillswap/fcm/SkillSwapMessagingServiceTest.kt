/** Created with the help of Cursor */
package com.swent.skillswap.fcm

import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.messaging.RemoteMessage
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

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
    fun onMessageReceived_withNotification_logsNotification() {
        val remoteMessage = mockk<RemoteMessage>(relaxed = true)
        val notification =
            mockk<com.google.firebase.messaging.RemoteMessage.Notification>(relaxed = true)
        every { remoteMessage.from } returns "test-sender"
        every { remoteMessage.data } returns emptyMap()
        every { remoteMessage.notification } returns notification
        every { notification.title } returns "Test Title"
        every { notification.body } returns "Test Body"

        service.onMessageReceived(remoteMessage)

        verify { remoteMessage.notification }
        verify { notification.title }
        verify { notification.body }
    }
}
