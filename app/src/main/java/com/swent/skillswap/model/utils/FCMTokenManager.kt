/** Created with the help of Cursor */
package com.swent.skillswap.model.utils

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import com.swent.skillswap.model.user.UserRepositery
import kotlinx.coroutines.tasks.await

/**
 * Utility class for managing Firebase Cloud Messaging (FCM) tokens.
 *
 * Handles:
 * - Retrieving FCM tokens from Firebase
 * - Saving tokens to user document in Firestore
 * - Updating tokens when they refresh
 */
class FCMTokenManager(
    private val userRepository: UserRepositery,
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
) {

    companion object {
        private const val TAG = "FCMTokenManager"
    }

    /**
     * Retrieves the current FCM token and saves it to the user's document.
     *
     * @param userId The ID of the user whose token to save. If null, uses current authenticated
     *   user.
     * @return The FCM token, or null if retrieval failed.
     */
    suspend fun getAndSaveToken(userId: String? = null): String? {
        return try {
            val token = FirebaseMessaging.getInstance().token.await()
            val targetUserId = userId ?: firebaseAuth.currentUser?.uid

            if (targetUserId != null && token.isNotEmpty()) {
                userRepository.updateFcmToken(targetUserId, token)
            } else {
                Log.w(TAG, "Cannot save FCM token: userId is null or token is empty")
            }

            token
        } catch (e: Exception) {
            Log.e(TAG, "Error getting FCM token", e)
            null
        }
    }

    /**
     * Saves an FCM token to the user's document.
     *
     * @param token The FCM token to save.
     * @param userId The ID of the user whose token to save. If null, uses current authenticated
     *   user.
     */
    suspend fun saveToken(token: String, userId: String? = null) {
        try {
            val targetUserId = userId ?: firebaseAuth.currentUser?.uid

            if (targetUserId != null && token.isNotEmpty()) {
                userRepository.updateFcmToken(targetUserId, token)
            } else {
                Log.w(TAG, "Cannot save FCM token: userId is null or token is empty")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error saving FCM token", e)
        }
    }

    /**
     * Retrieves the current FCM token without saving it.
     *
     * @return The FCM token, or null if retrieval failed.
     */
    suspend fun getToken(): String? {
        return try {
            FirebaseMessaging.getInstance().token.await()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting FCM token", e)
            null
        }
    }
}
