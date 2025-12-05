/** Created with the help of Cursor */
package com.swent.skillswap.model.user

import kotlinx.coroutines.delay

/**
 * In-memory UserRepository implementation for testing. Provides deterministic behavior and allows
 * testing success/failure cases without actual database interactions.
 */
class FakeUserRepository : UserRepositery {
    companion object {
        private const val ERROR_GET_FAILURE = "Simulated get failure"
        private const val ERROR_ADD_FAILURE = "Simulated add failure"
        private const val ERROR_EDIT_FAILURE = "Simulated edit failure"
        private const val ERROR_UPDATE_FCM_TOKEN_FAILURE = "Simulated updateFcmToken failure"
    }

    private val users = mutableMapOf<String, User>()
    private var uidCounter = 0
    private var shouldFailOnAdd = false
    private var shouldFailOnGet = false
    private var shouldFailOnEdit = false
    private var shouldFailOnUpdateFcmToken = false
    private var delayMillis = 0L

    fun preloadUsers(vararg usersToPreload: User) {
        users.clear()
        usersToPreload.forEach { users[it.uid] = it }
    }

    fun setShouldFailOnAdd(fail: Boolean) {
        shouldFailOnAdd = fail
    }

    fun setShouldFailOnGet(fail: Boolean) {
        shouldFailOnGet = fail
    }

    fun setShouldFailOnEdit(fail: Boolean) {
        shouldFailOnEdit = fail
    }

    fun setShouldFailOnUpdateFcmToken(fail: Boolean) {
        shouldFailOnUpdateFcmToken = fail
    }

    fun setDelay(delayMs: Long) {
        delayMillis = delayMs
    }

    override fun getNewUid(): String {
        return "test-user-${uidCounter++}"
    }

    override suspend fun getUser(userID: String): User {
        if (delayMillis > 0) {
            delay(delayMillis)
        }
        if (shouldFailOnGet) {
            throw Exception(ERROR_GET_FAILURE)
        }
        return users[userID] ?: throw Exception("User not found: $userID")
    }

    override suspend fun addUser(user: User) {
        if (delayMillis > 0) {
            delay(delayMillis)
        }
        if (shouldFailOnAdd) {
            throw Exception(ERROR_ADD_FAILURE)
        }
        users[user.uid] = user
    }

    override suspend fun editUser(userID: String, newValue: User) {
        if (delayMillis > 0) {
            delay(delayMillis)
        }
        if (shouldFailOnEdit) {
            throw Exception(ERROR_EDIT_FAILURE)
        }
        if (!users.containsKey(userID)) {
            throw Exception("Cannot edit non-existent user: $userID")
        }
        users[userID] = newValue
    }

    override suspend fun deleteUser(userID: String) {
        users.remove(userID)
    }

    override suspend fun userExists(userId: String): Boolean {
        return users.containsKey(userId)
    }

    override suspend fun updateFcmToken(userId: String, fcmToken: String) {
        if (delayMillis > 0) {
            delay(delayMillis)
        }
        if (shouldFailOnUpdateFcmToken) {
            throw Exception(ERROR_UPDATE_FCM_TOKEN_FAILURE)
        }
        val user = users[userId] ?: throw Exception("User not found: $userId")
        users[userId] = user.copy(fcmToken = fcmToken)
    }

    override suspend fun updateRating(userId: String, incomingRating: Float) {}

    fun getAddedUsers(): List<User> = users.values.toList()

    fun getUserById(id: String): User? = users[id]

    fun clear() {
        users.clear()
        uidCounter = 0
    }
}
