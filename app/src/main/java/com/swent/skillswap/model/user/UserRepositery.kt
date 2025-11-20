package com.swent.skillswap.model.user

/** Represents a repository that manages User items. */
interface UserRepositery {

    /** Generates and returns a new unique identifier for a User item. */
    fun getNewUid(): String

    /**
     * Retrieves a specific User item by its unique identifier.
     *
     * @throws Exception if the User item is not found.
     */
    suspend fun getUser(userID: String): User

    /** Adds a new User item to the repository. */
    suspend fun addUser(user: User)

    /**
     * Edits an existing User item in the repository.
     *
     * @throws Exception if the User item is not found.
     */
    suspend fun editUser(userID: String, newValue: User)

    /**
     * Deletes a User item from the repository.
     *
     * @throws Exception if the User item is not found.
     */
    suspend fun deleteUser(userID: String)

    /**
     * @param userId user ID to verify his existence
     * @return if the user exist or not in firestore
     */
    suspend fun userExists(userId: String): Boolean

    /**
     * Updates the FCM token for a specific user.
     *
     * @param userId The ID of the user whose FCM token to update.
     * @param fcmToken The new FCM token to save.
     * @throws Exception if the user does not exist or update fails.
     */
    suspend fun updateFcmToken(userId: String, fcmToken: String)
}
