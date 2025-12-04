/*
 * @author: Léo. MARTI
 * /!\ Written with help of Copilot
 * > complete all the repetitive code (construction of instances for example)
 * > helped me with the firebase functions and structure
 */

package com.swent.skillswap.model.user

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.SetOptions
import com.swent.skillswap.firebase.FirestorePaths.USERS_COLLECTION
import kotlinx.coroutines.tasks.await

class UserRepoFirestore(private val db: FirebaseFirestore) : UserRepositery {

    override fun getNewUid(): String {
        return java.util.UUID.randomUUID().toString()
    }

    override suspend fun getUser(userID: String): User {
        return try {
            val document = db.collection(USERS_COLLECTION).document(userID).get().await()
            val data = document.data ?: throw Exception("No data found for user with ID: $userID")

            User(
                uid = document.id,
                username = data["username"] as String,
                email = data["email"] as String,
                profilePicture = data["profilePicture"] as String,
                skillSet = deserializeSkills(data["skillSet"] as String),
                rating = (data["rating"] as? Number)?.toFloat() ?: 0f,
                availability = deserializeAvailabilities(data["availability"] as String),
                preference = deserializePreference(data["preference"] as String),
                location = data["location"] as GeoPoint,
                blockedUsers = deserializeBlockedUsers(data["blockedUsers"] as String),
                fcmToken = (data["fcmToken"] as? String)?.takeIf { it.isNotBlank() }
            )
        } catch (e: Exception) {
            Log.e("UserRepoFirestore", "Error while getting user in getUser", e)
            throw Exception("Error while getting user in getUser")
        }
    }

    override suspend fun addUser(user: User) {
        val userData: Map<String, Any> =
            mapOf(
                "username" to user.username,
                "email" to user.email,
                "profilePicture" to user.profilePicture,
                "skillSet" to serializeSkills(user.skillSet),
                "rating" to user.rating,
                "availability" to serializeAvailabilities(user.availability),
                "preference" to serializePreference(user.preference),
                "location" to user.location,
                "blockedUsers" to serializeBlockedUsers(user.blockedUsers),
                "fcmToken" to (user.fcmToken ?: "")
            )

        db.collection(USERS_COLLECTION).document(user.uid).set(userData)
    }

    override suspend fun editUser(userID: String, newValue: User) {
        // check if user exists
        if (!db.collection(USERS_COLLECTION).document(userID).get().await().exists()) {
            Log.e("UserRepoFirestore", "Error while editing user in editUser")
            throw Exception("Error while editing user in editUser: user does not exist")
        }

        // if user exist, edit it
        db.collection(USERS_COLLECTION)
            .document(userID)
            .set(
                mapOf(
                    "username" to newValue.username,
                    "email" to newValue.email,
                    "profilePicture" to newValue.profilePicture,
                    "skillSet" to serializeSkills(newValue.skillSet),
                    "rating" to newValue.rating,
                    "availability" to serializeAvailabilities(newValue.availability),
                    "preference" to serializePreference(newValue.preference),
                    "location" to newValue.location,
                    "blockedUsers" to serializeBlockedUsers(newValue.blockedUsers),
                    "fcmToken" to (newValue.fcmToken ?: ""),
                ),
                SetOptions.merge()
            )
    }

    override suspend fun deleteUser(userID: String) {
        try {
            db.collection(USERS_COLLECTION).document(userID).delete()
        } catch (e: Exception) {
            Log.e("UserRepoFirestore", "Error while deleting user in deleteUser", e)
            throw Exception("Error while deleting user in deleteUser")
        }
    }

    override suspend fun userExists(userId: String): Boolean {
        val doc = db.collection(USERS_COLLECTION).document(userId).get().await()
        return doc.exists()
    }

    override suspend fun updateFcmToken(userId: String, fcmToken: String) {
        try {
            if (!db.collection(USERS_COLLECTION).document(userId).get().await().exists()) {
                Log.e("UserRepoFirestore", "Error while updating FCM token: user does not exist")
                throw Exception("User does not exist: $userId")
            }
            db.collection(USERS_COLLECTION).document(userId).update("fcmToken", fcmToken).await()
        } catch (e: Exception) {
            Log.e("UserRepoFirestore", "Error while updating FCM token", e)
            throw Exception("Failed to update FCM token: ${e.message}")
        }
    }

    override suspend fun updateRating(userId: String, incomingRating: Float) {
        try {
            if (!userExists(userId)) {
                Log.e("UserRepoFirestore", "Error while updating rating: user does not exist")
                throw Exception("User does not exist: $userId")
            }
            val newRating =
                computeNewRating(
                    currentRating = getUser(userId).rating,
                    incomingRating = incomingRating
                )
            db.collection(USERS_COLLECTION).document(userId).update("rating", newRating).await()
        } catch (e: Exception) {
            Log.e("UserRepoFirestore", "Error while updating rating", e)
            throw Exception("Failed to update rating: ${e.message}")
        }
    }

    companion object {
        private const val RATING_ALPHA = 0.2f
        private const val MAX_RATING = 5f
        private const val MIN_RATING = 0f
    }

    // Uses EMA to compute new rating
    private fun computeNewRating(currentRating: Float, incomingRating: Float): Float {
        return ((1 - RATING_ALPHA) * currentRating + RATING_ALPHA * incomingRating).coerceIn(
            MIN_RATING,
            MAX_RATING
        )
    }
}
