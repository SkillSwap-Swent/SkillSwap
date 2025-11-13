/**
 * @author Léonard MARTI 394185 /!\ Written with help of Copilot /!\
 * > helped me with coroutine process and syntax, complete all the repetitive code
 */
package com.swent.skillswap.ui.editUser

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.swent.skillswap.model.user.Skill
import com.swent.skillswap.model.user.User
import com.swent.skillswap.model.user.UserRepoFirestore
import com.swent.skillswap.model.user.UserRepositery
import kotlin.String
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Represents the current state of the Edit User Screen UI.
 *
 * @property editedUser the user being edited.
 * @property isLoading indicates if a save operation is in progress.
 * @property isSaved indicates if the user has been successfully saved.
 * @property error holds any error message encountered during save.
 */
data class EditUserUiState(
    /** The user being edited */
    val editedUser: User? = null,

    /** Status */
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,

    /** Error fields for each User attribute */
    val usernameError: String? = null,
    val emailError: String? = null,
    val profilePictureError: String? = null,
    val skillSetError: String? = null,
    val ratingError: String? = null,
    val availabilityError: String? = null,
    val generalError: String? = null
)

class EditUserViewModel(
    // val navigation:
    private val repo: UserRepositery = UserRepoFirestore(FirebaseFirestore.getInstance())
) : ViewModel() {

    /** Internal state of the Edit User screen. */
    private val _uiState = MutableStateFlow(EditUserUiState())
    /** Publicly exposed, read-only state of the Edit User screen. */
    val uiState: StateFlow<EditUserUiState> = _uiState.asStateFlow()

    /** Current user to be fetched from firestore and then edited */
    private lateinit var currentUser: User
    private var isDataLoaded = false

    /**
     * Loads the current user data from Firestore. Should be called when navigating to the
     * EditUserScreen.
     */
    fun loadCurrentUser() {

        // Skip if data is already loaded
        if (isDataLoaded && ::currentUser.isInitialized) {
            return
        }

        val currentFirestoreUser = Firebase.auth.currentUser

        if (currentFirestoreUser == null) {
            _uiState.update {
                it.copy(
                    generalError =
                        "No authenticated user found, you have to login to edit your profile"
                )
            }
            isDataLoaded = false
            return
        }

        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                currentUser = repo.getUser(currentFirestoreUser.uid)
                _uiState.update { it.copy(editedUser = currentUser, isLoading = false) }
                isDataLoaded = true
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, generalError = "Failed to load current user")
                }
                isDataLoaded = false
            }
        }
    }

    fun clearLoadedState() {
        isDataLoaded = false
    }

    /**
     * Abstraction of a function that updates a given field in the edited user.
     *
     * @param input the new value for the field.
     * @param precondition a predicate that must be satisfied for the update to occur.
     * @param applyToUser a function that applies the new value to the User object.
     * @param applyToError a function that applies the error message to the UI state, could be a
     *   non-trivial one if there is multiple error messages, that why it leaves the implementation
     *   in the caller side.
     * @param clearError a function that clears the correct error message.
     */
    private fun <T> setField(
        input: T,
        precondition: (T) -> Boolean,
        applyToUser: (User, T) -> User,
        applyToError: () -> Unit,
        clearError: (EditUserUiState) -> EditUserUiState
    ) {
        if (precondition(input)) {
            /** everything is fine, update the state and clear error */
            _uiState.update { it.copy(editedUser = applyToUser(it.editedUser!!, input)) }
            _uiState.update { clearError(it) }
        } else {
            /** precondition failed, update the error state */
            applyToError()
        }
    }

    /** Sets the username of the edited user after validating it is not empty. */
    fun setUsername(name: String) {
        setField(
            input = name,
            precondition = { it.isNotBlank() },
            applyToUser = { user, value -> user.copy(username = value) },
            applyToError = {
                _uiState.update { it.copy(usernameError = "Username cannot be empty") }
            },
            clearError = { it.copy(usernameError = null) }
        )
    }

    /** Sets the email of the edited user after validating its format. */
    fun setEmail(email: String) {
        setField(
            input = email,
            precondition = { Patterns.EMAIL_ADDRESS.matcher(it).matches() },
            applyToUser = { user, value -> user.copy(email = value) },
            applyToError = { _uiState.update { it.copy(emailError = "Invalid email format") } },
            clearError = { it.copy(emailError = null) }
        )
    }

    /* THE NEXT FUNCTION ARE NOT USED YET, THEN ON COMMENT FOR NOW TO MAKE LINE COVERAGE HAPPY


    /** Sets the profile picture URL of the edited user. */
    fun setProfilePicture(url: String) {
        setField(
            input = url,
            precondition = { true }, // Better validation to be added later
            applyToUser = { user, value -> user.copy(profilePicture = value) },
            applyToError = { /* No error handling for now */},
            clearError = { it.copy(profilePictureError = null) }
        )
    }

     */
    /** Sets the skill set of the edited user */
    fun setSkills(skills: Set<Skill>) {
        setField(
            input = skills,
            precondition = { it.isNotEmpty() },
            applyToUser = { user, value -> user.copy(skillSet = value) },
            applyToError = {
                _uiState.update { it.copy(skillSetError = "You should select at least one skill") }
            },
            clearError = { it.copy(skillSetError = null) }
        )
    }

    /*

    /** Sets the availability of the edited user */
    fun setAvailability(availability: List<com.swent.skillswap.model.user.Availability>) {
        setField(
            input = availability,
            precondition = { true }, // no Specific precondition for now
            applyToUser = { user, value -> user.copy(availability = value) },
            applyToError = { /* No error handling for now */},
            clearError = { it.copy(availabilityError = null) }
        )
    }
     */

    /** Updates the edited user in the state with new values. */
    fun validate() {
        /** PRECONDITIONS */
        if (
            uiState.value.usernameError != null ||
                uiState.value.emailError != null ||
                uiState.value.profilePictureError != null ||
                uiState.value.skillSetError != null ||
                uiState.value.ratingError != null ||
                uiState.value.availabilityError != null ||
                uiState.value.isLoading
        ) {
            /** There is still some error in the form, do not proceed */
            return
        }
        /** Fetch the edited user from the state and do nothing if the user is not fetched */
        val editedUser = _uiState.value.editedUser ?: return

        /** The preconditions are fulfilled, proceed to validate */
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, generalError = null) }

            /** Init the network call to edit the user */
            try {
                repo.editUser(editedUser.uid, editedUser)
                /** Operation successful, update the state */
                _uiState.update { it.copy(isLoading = false, isSaved = true) }
            } catch (e: Exception) {
                /** If the operation goes wrong handle the error */
                _uiState.update {
                    it.copy(isLoading = false, generalError = "Failed to edit user: ${e.message}")
                }
            }
        }
    }
}
