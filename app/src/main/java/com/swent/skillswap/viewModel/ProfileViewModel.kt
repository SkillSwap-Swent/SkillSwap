package com.swent.skillswap.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.swent.skillswap.model.user.Availability
import com.swent.skillswap.model.user.Preference
import com.swent.skillswap.model.user.Skill
import com.swent.skillswap.model.user.User
import com.swent.skillswap.model.user.UserRepoFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel responsible for managing user profile data and operations.
 *
 * Handles loading and updating user information from Firestore, maintaining the current user state
 * through a StateFlow for reactive UI updates.
 *
 * @param repo The UserRepoFirestore instance for database operations
 */
class ProfileViewModel(
    private val repo: UserRepoFirestore = UserRepoFirestore(FirebaseFirestore.getInstance())
) : ViewModel() {

    /** Mutable state flow for internal user state updates */
    private val _userState = MutableStateFlow<User>(User())

    /** Public immutable state flow for observing user data changes */
    val userState: StateFlow<User> = _userState

    init {
        loadCurrentUser()
    }

    /**
     * Loads the currently authenticated user from Firestore.
     *
     * Retrieves the current user's UID from Firebase Authentication and fetches their profile data
     * from the repository. Updates [_userState] with the fetched data. Logs warnings if no user is
     * authenticated and errors if the database operation fails.
     */
    fun loadCurrentUser() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            Log.w("ProfileViewModel", "No user logged in yet")
            return
        }

        viewModelScope.launch {
            try {
                val user = repo.getUser(uid)
                _userState.value = user
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error while getting user", e)
            }
        }
    }

    /**
     * Updates the entire user profile in Firestore.
     *
     * Persists the provided user object to the database and updates the local state. Uses the
     * user's UID if available, otherwise falls back to the current authenticated user's UID.
     *
     * @param user The user object containing all updated profile information
     */
    fun updateUser(user: User) {
        val uid = user.uid.ifEmpty { FirebaseAuth.getInstance().currentUser?.uid }
        if (uid == null) {
            Log.e("ProfileViewModel", "No UID available to update user")
            return
        }

        viewModelScope.launch {
            try {
                repo.editUser(uid, user.copy(uid = uid))
                _userState.value = user.copy(uid = uid)
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Failed to update user", e)
            }
        }
    }

    /**
     * Updates specific user profile attributes selectively.
     *
     * Allows partial updates of user data without requiring the entire user object. Only provided
     * parameters are updated; null values preserve existing data.
     *
     * @param username Optional new username
     * @param email Optional new email address
     * @param profilePicture Optional new profile picture URL
     * @param skillSet Optional new set of skills
     * @param rating Optional new user rating
     * @param availability Optional new availability schedule
     * @param preference Optional new user preferences
     */
    fun updateUserAttributes(
        username: String? = null,
        email: String? = null,
        profilePicture: String? = null,
        skillSet: Set<Skill>? = null,
        rating: Float? = null,
        availability: List<Availability>? = null,
        preference: Preference? = null
    ) {
        val current = _userState.value
        val uid = current.uid.ifEmpty { FirebaseAuth.getInstance().currentUser?.uid }
        if (uid == null) {
            Log.e("ProfileViewModel", "No UID available to update attributes")
            return
        }

        val updated =
            current.copy(
                uid = uid,
                username = username ?: current.username,
                email = email ?: current.email,
                profilePicture = profilePicture ?: current.profilePicture,
                skillSet = skillSet ?: current.skillSet,
                rating = rating ?: current.rating,
                availability = availability ?: current.availability,
                preference = preference ?: current.preference
            )

        viewModelScope.launch {
            repo.editUser(uid, updated)
            _userState.value = updated
        }
    }
}
