/** Created with the help of Cursor */
package com.swent.skillswap.fcm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
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
        UserRepoFirestore(com.google.firebase.firestore.FirebaseFirestore.getInstance())
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
        val type = message.data["type"]
        val relatedId = message.data["relatedId"]

        when (type) {
            NotificationType.MESSAGE.name -> onChatNotificationReceived(message)
            NotificationType.POST_ACCEPTED.name -> onAcceptedPostNotificationReceived(message)
            else -> Log.w(TAG, "Unknown notification type: $type")
        }
    }

    private fun onChatNotificationReceived(message: RemoteMessage) {
        Log.d(TAG, "Handling chat notification: ${message.data}")
        val notification = message.notification
        val title = notification?.title ?: "New Chat"
        val body = notification?.body ?: "You have a new message"

        val channelId = "chat_channel"
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        // Create notification channel for Android O+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel(
                    channelId,
                    "Chat Notifications",
                    NotificationManager.IMPORTANCE_DEFAULT
                )
            notificationManager.createNotificationChannel(channel)
        }

        val builder =
            NotificationCompat.Builder(this, channelId)
                .setSmallIcon(android.R.drawable.ic_dialog_email)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)

        notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
    }

    private fun onAcceptedPostNotificationReceived(message: RemoteMessage) {
        Log.d(TAG, "Handling accepted post notification: ${message.data}")
        // TODO: HANDLE POST NOTIFICATION PAYLOAD RECEPTION
    }
}
