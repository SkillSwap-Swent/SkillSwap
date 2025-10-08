package com.swent.skillswap.model.user

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

const val USERS_COLLECTION_PATH = "users" // TODO: is it user ?

class UserRepoFirestore(private val db: FirebaseFirestore) : UserRepositery {

    override fun getNewUid(): String {
        return java.util.UUID.randomUUID().toString()
    }

    override suspend fun getUser(userID: String): User {
        return try {
            val document =
                db.collection(USERS_COLLECTION_PATH).get().await().documents.first{
                    it.data != null && it.data!!.containsKey(userID)
                }

            val serializedStr = document.data!![userID]
            deserializeUser(serializedStr as String)

        } catch (e: Exception) {
            Log.e("UserRepoFirestore", "Error while getting user in getUser", e)
            throw Exception("Error while getting user in getUser")
        }
    }

    override suspend fun addUser(user: User) {
       db.collection(USERS_COLLECTION_PATH)
            .document(user.uid)
            .set(mapOf(user.uid to serializeUser(user)))
    }

    override suspend fun editUser(userID: String, newValue: User) {
        //check if user exists
        if (
            db.collection(USERS_COLLECTION_PATH).get().await().documents
            .firstOrNull {it.data != null && it.data!!.containsKey(userID)}
            == null
            ){
            Log.e("UserRepoFirestore", "Error while editing user in editUser")
            throw Exception("Error while editing user in editUser: user does not exist")
        }

        //if user exist, edit it
        db.collection(USERS_COLLECTION_PATH)
            .document(userID)
            .set(mapOf(userID to serializeUser(newValue)))
    }

    override suspend fun deleteUser(userID: String) {
        try {
            db.collection(USERS_COLLECTION_PATH).document(userID).delete()
        } catch (e: Exception){
            Log.e("UserRepoFirestore", "Error while deleting user in deleteUser", e)
            throw Exception("Error while deleting user in deleteUser")
        }
    }
}