/*
 * @author: Léo. MARTI
 * /!\ Written with help of Copilot
 * > complete all the repetitive code (construction of instances for example)
 * > helped me with the firebase functions and structure
 */

package com.swent.skillswap.model.user

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.swent.skillswap.model.firestore.*
import kotlinx.coroutines.tasks.await

class UserRepoFirestore(private val db: FirebaseFirestore) : UserRepositery {

    override fun getNewUid(): String {
        return java.util.UUID.randomUUID().toString()
    }

    override suspend fun getUser(userID: String): User {
        return try {
            val document = db.collection(USERS_COLLECTION_PATH).document(userID).get().await()
            val data = document.data ?: throw Exception("No data found for user with ID: $userID")

            User(
                uid = document.id,
                username = data["username"] as String,
                email = data["email"] as String,
                profilePicture = data["profilePicture"] as String,
                skillSet = deserializeSkills(data["skillSet"] as String),
                rating = (data["rating"] as? Number)?.toFloat() ?: 0f,
                availability = deserializeAvailabilities(data["availability"] as String)
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
                "availability" to serializeAvailabilities(user.availability)
            )

        db.collection(USERS_COLLECTION_PATH).document(user.uid).set(userData)
    }

    override suspend fun editUser(userID: String, newValue: User) {
        // check if user exists
        if (!db.collection(USERS_COLLECTION_PATH).document(userID).get().await().exists()) {
            Log.e("UserRepoFirestore", "Error while editing user in editUser")
            throw Exception("Error while editing user in editUser: user does not exist")
        }

        // if user exist, edit it
        db.collection(USERS_COLLECTION_PATH)
            .document(userID)
            .set(
                mapOf(
                    "username" to newValue.username,
                    "email" to newValue.email,
                    "profilePicture" to newValue.profilePicture,
                    "skillSet" to serializeSkills(newValue.skillSet),
                    "rating" to newValue.rating,
                    "availability" to serializeAvailabilities(newValue.availability)
                ),
                SetOptions.merge()
            )
    }

    override suspend fun deleteUser(userID: String) {
        try {
            db.collection(USERS_COLLECTION_PATH).document(userID).delete()
        } catch (e: Exception) {
            Log.e("UserRepoFirestore", "Error while deleting user in deleteUser", e)
            throw Exception("Error while deleting user in deleteUser")
        }
    }

    override suspend fun userExists(userId: String): Boolean {
        val doc = db.collection(USERS_COLLECTION_PATH).document(userId).get().await()
        return doc.exists()
    }
}
