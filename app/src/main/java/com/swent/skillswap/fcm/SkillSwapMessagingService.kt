/** Created with the help of Cursor */
package com.swent.skillswap.fcm

import android.app.Notification
import android.app.NotificationManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.swent.skillswap.R
import com.swent.skillswap.model.chat.CurrentChatTracker
import com.swent.skillswap.model.notification.NotificationRepository
import com.swent.skillswap.model.notification.NotificationRepositoryFirestore
import com.swent.skillswap.model.notification.NotificationType
import com.swent.skillswap.model.user.UserRepoFirestore
import com.swent.skillswap.model.user.UserRepositery
import com.swent.skillswap.model.utils.FCMTokenManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Firebase Cloud Messaging service for handling push notifications and token refresh.
 *
 * This service:
 * - Handles incoming push notifications
 * - Automatically refreshes and saves FCM tokens when they change
 * - Updates the user's document in Firestore with the new token
 */
class SkillSwapMessagingService : FirebaseMessagingService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val userRepository: UserRepositery by lazy {
        UserRepoFirestore(FirebaseFirestore.getInstance())
    }
    private val notificationRepositery: NotificationRepository by lazy {
        NotificationRepositoryFirestore(FirebaseFirestore.getInstance())
    }
    private val fcmTokenManager: FCMTokenManager by lazy { FCMTokenManager(userRepository) }

    companion object {
        private const val TAG = "SkillSwapMessagingService"
    }

    /**
     * Called when a new FCM token is generated or refreshed. Automatically saves the token to the
     * user's document in Firestore.
     *
     * @param token The new FCM token.
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New FCM token received: $token")

        serviceScope.launch {
            try {
                val userId = FirebaseAuth.getInstance().currentUser?.uid
                if (userId != null) {
                    fcmTokenManager.saveToken(token, userId)
                    Log.d(TAG, "FCM token refreshed and saved for user: $userId")
                } else {
                    Log.w(TAG, "Cannot save refreshed token: user not authenticated")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error saving refreshed FCM token", e)
            }
        }
    }

    /**
     * Called when a push notification is received.
     *
     * @param message The remote message containing notification data.
     */
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d(TAG, "Message received from: ${message.from}")

        // Extract the type of the notification to choose which way to handle it (chat or post)
        when (val type = message.data["type"]) {
            NotificationType.MESSAGE.name -> onChatNotificationReceived(message)
            NotificationType.POST_ACCEPTED.name,
            NotificationType.POST_REJECTED.name,
            NotificationType.POST_REPLY.name -> onPostNotificationReceived(message, type)
            else -> Log.w(TAG, "Unknown notification type: $type")
        }
    }

    private fun onChatNotificationReceived(message: RemoteMessage) {
        Log.d(TAG, "Handling chat notification: ${message.data}")
        val notification = message.notification
        val title = notification?.title ?: "New Chat"
        val body = notification?.body ?: "You have a new message"
        val channelId = "chat_channel"
        val relatedChatId = message.data["relatedId"]

        // If user is currently viewing this chat, mark as read and do not show notification
        if (relatedChatId != null && CurrentChatTracker.currentChatId == relatedChatId) {
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            if (userId != null) {
                serviceScope.launch {
                    notificationRepositery.markChatNotificationsAsRead(relatedChatId, userId)
                }
            }
            Log.d(TAG, "User is in chat $relatedChatId, notification marked as read and not shown.")
            return
        }

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val builder =
            NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.logo)
                .setContentTitle(title)
                .setContentText(body)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(Notification.DEFAULT_ALL)
                .setAutoCancel(true)

        notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
    }

    private fun onPostNotificationReceived(message: RemoteMessage, type: String) {
        Log.d(TAG, "Handling post notification, type: $type")
        val notification = message.notification
        val title =
            notification?.title
                ?: when (type) {
                    NotificationType.POST_ACCEPTED.name -> "Post Reply Accepted"
                    NotificationType.POST_REJECTED.name -> "Post Reply Rejected"
                    NotificationType.POST_REPLY.name -> "New Post Reply"
                    else -> "Post Notification"
                }
        val body = notification?.body ?: "You have a new post notification"
        val channelId = "post_channel"
        // TODO: Use relatedPostId to implement CurrentPostTracker similar to CurrentChatTracker
        // val relatedPostId = message.data["relatedId"]

        // If user is currently viewing this post, mark as read and do not show notification
        // TODO: Implement CurrentPostTracker similar to CurrentChatTracker if needed
        // For now, we'll always show the notification

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val builder =
            NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.logo)
                .setContentTitle(title)
                .setContentText(body)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(Notification.DEFAULT_ALL)
                .setAutoCancel(true)

        notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
    }
}
