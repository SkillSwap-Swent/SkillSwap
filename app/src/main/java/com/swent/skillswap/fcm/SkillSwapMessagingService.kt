/** Created with the help of Cursor */
package com.swent.skillswap.fcm

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.swent.skillswap.R
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
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val data = message.data
        val title = data["title"] ?: message.notification?.title ?: "New Message"
        val body = data["message"] ?: message.notification?.body ?: "You have a new message"

        // Create notification channel for Android O+
        val channel =
            NotificationChannel(
                "chat_messages",
                "Chat Messages",
                NotificationManager.IMPORTANCE_HIGH
            )
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)

        // Build and show notification
        val notification =
            NotificationCompat.Builder(this, "chat_messages")
                .setSmallIcon(R.drawable.logo)
                .setContentTitle(title)
                .setContentText(body)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build()

        NotificationManagerCompat.from(this)
            .notify(System.currentTimeMillis().toInt(), notification)
    }
}
