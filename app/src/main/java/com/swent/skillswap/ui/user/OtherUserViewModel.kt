package com.swent.skillswap.ui.user

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.swent.skillswap.model.user.User
import com.swent.skillswap.model.user.UserRepoFirestore
import com.swent.skillswap.model.user.UserRepositery
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
class OtherUserViewModel(
    private val repo: UserRepositery = UserRepoFirestore(FirebaseFirestore.getInstance()),
    val userId: String = ""
) : ViewModel() {

    /** Mutable state flow for internal user state updates */
    private val _userState = MutableStateFlow(User())

    /** Public immutable state flow for observing user data changes */
    val userState: StateFlow<User> = _userState

    init {
        loadUser()
    }

    /**
     * Loads the currently authenticated user from Firestore.
     *
     * Retrieves the current user's UID from Firebase Authentication and fetches their profile data
     * from the repository. Updates [_userState] with the fetched data. Logs warnings if no user is
     * authenticated and errors if the database operation fails.
     */
    fun loadUser() {

        viewModelScope.launch {
            try {
                val user = repo.getUser(userId)
                _userState.value = user
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error while getting user", e)
            }
        }
    }
}

class OtherUserViewModelFactory(
    private val userId: String,
    private val repo: UserRepositery = UserRepoFirestore(FirebaseFirestore.getInstance())
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(OtherUserViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST") return OtherUserViewModel(repo, userId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
