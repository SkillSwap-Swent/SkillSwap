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
}
