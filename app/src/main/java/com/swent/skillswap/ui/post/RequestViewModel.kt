package com.swent.skillswap.ui.post

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint
import com.swent.skillswap.firebase.CloudReferences.FEED_PICTURES_PATH
import com.swent.skillswap.firebase.FirestoreSettings
import com.swent.skillswap.model.images.PictureRepositoryInterface
import com.swent.skillswap.model.post.PaymentMethod
import com.swent.skillswap.model.post.PostRepository
import com.swent.skillswap.model.post.PostStatus
import com.swent.skillswap.model.post.PostType
import com.swent.skillswap.model.post.Request
import com.swent.skillswap.model.tags.PostTag
import com.swent.skillswap.model.tags.SkillTag
import com.swent.skillswap.model.utils.LocationManager
import java.util.Date
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class PostOperation {
    ADD,
    EDIT;

    fun toTitle() =
        when (this) {
            ADD -> "New"
            EDIT -> "Edit"
        }
}

data class RequestUIState(
    val uid: String = "",
    val title: String = "",
    val description: String = "",
    val skills: Set<SkillTag> = emptySet(),
    val tags: Set<PostTag> = emptySet(),
    val paymentMethod: PaymentMethod = PaymentMethod.SKILLS,
    val expiry: Timestamp = Timestamp.now(),
    val location: GeoPoint = GeoPoint(46.5191, 6.5668), // Default fallback

    // Error fields
    val titleError: String = "",
    val descriptionError: String = "",
    val tagsError: String = "",
    val attachmentsError: String = "",
    val paymentMethodsError: String = "",
    val expiryError: String = "",

    // Submission state
    val isLoading: Boolean = false,
    val submitError: String? = null,
    val isSubmitSuccessful: Boolean = false,

    // photo picker
    val attachments: Set<Uri> = emptySet()
)

class RequestViewModel(
    private val appContext: Context? = null,
    private val postRepository: PostRepository,
    private val storageRepository: PictureRepositoryInterface,
    private val currentUserId: String,
    private val postId: String? = null // Only necessary if postOperation is edit
) : ViewModel() {
    private val _uiState = MutableStateFlow(RequestUIState())
    val uiState: StateFlow<RequestUIState> = _uiState

    companion object {
        const val REQUEST_LIFESPAN_DAYS = 30L
    }

    // Load existing post data if editing
    init {
        if (postId != null) {
            loadPost(postId)
        }
    }

    private fun loadPost(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val post = postRepository.getPost(PostType.REQUEST, id)
                _uiState.update {
                    it.copy(
                        uid = post.uid,
                        title = post.title,
                        description = post.description,
                        skills = post.skills.toSet(),
                        tags = post.tags.toSet(),
                        paymentMethod = post.paymentMethod,
                        location = post.location,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, submitError = "Failed to load post: ${e.message}")
                }
            }
        }
    }

    fun setTitle(newTitle: String) {
        _uiState.update {
            it.copy(
                title = newTitle,
                titleError = if (newTitle.isBlank()) "Title cannot be empty" else ""
            )
        }
    }

    fun setDescription(newDescription: String) {
        _uiState.update {
            it.copy(
                description = newDescription,
                descriptionError =
                    if (newDescription.isBlank()) "Description cannot be empty" else ""
            )
        }
    }

    fun addTag(tag: PostTag) {
        _uiState.update { current ->
            if (tag !in current.tags) {
                current.copy(tags = current.tags + tag, tagsError = "")
            } else {
                current
            }
        }
    }

    fun removeTag(tag: PostTag) {
        _uiState.update { current ->
            if (tag in current.tags) {
                current.copy(tags = current.tags - tag)
            } else {
                current
            }
        }
    }

    fun addSkill(skill: SkillTag) {
        _uiState.update { current ->
            if (skill !in current.skills) {
                current.copy(skills = current.skills + skill, tagsError = "")
            } else {
                current
            }
        }
    }

    fun removeSkill(skill: SkillTag) {
        _uiState.update { current ->
            if (skill in current.skills) {
                current.copy(skills = current.skills - skill)
            } else {
                current
            }
        }
    }

    fun togglePaymentMethod(methodClicked: PaymentMethod) {
        _uiState.update { current -> current.copy(paymentMethod = methodClicked) }
    }

    fun setLocation(newLocation: GeoPoint) {
        _uiState.update { it.copy(location = newLocation) }
    }

    fun save(postOperation: PostOperation) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, submitError = null) }

            try {
                /** determine uid based on operation */
                val uid =
                    when (postOperation) {
                        PostOperation.ADD -> postRepository.getNewUid(PostType.REQUEST)
                        PostOperation.EDIT -> postId!!
                    }
                if (postOperation == PostOperation.ADD && appContext != null) {
                    setLocation(LocationManager(appContext).getCurrentLocationSync())
                }

                /** attachments upload logic */
                val stringUrls = mutableListOf<String>()
                var counter = 0
                for (uri in _uiState.value.attachments) {
                    /** media name construction : concatenate uid and counter */
                    val mediaName = "$uid$counter"
                    counter += 1

                    val url = storageRepository.uploadPicture(mediaName, uri, FEED_PICTURES_PATH)
                    stringUrls.add(url.toString())
                }

                /** construct request object */
                val request =
                    Request(
                        uid = uid,
                        title = _uiState.value.title,
                        description = _uiState.value.description,
                        ownerId = currentUserId,
                        skills = _uiState.value.skills,
                        tags = _uiState.value.tags,
                        paymentMethod = _uiState.value.paymentMethod,
                        expiry =
                            Timestamp(
                                Date(
                                    System.currentTimeMillis() +
                                        REQUEST_LIFESPAN_DAYS * 24 * 60 * 60 * 1000
                                )
                            ),
                        creation = Timestamp.now(),
                        status = PostStatus.POSTED,
                        media = stringUrls,
                        location = _uiState.value.location
                    )

                // Will call validate() internally
                when (postOperation) {
                    PostOperation.ADD -> postRepository.addPost(request)
                    PostOperation.EDIT -> postRepository.editPost(uid, request)
                }

                // Success
                _uiState.update { it.copy(isLoading = false, isSubmitSuccessful = true) }
            } catch (e: Exception) {
                // Handle error
                _uiState.update {
                    it.copy(isLoading = false, submitError = e.message ?: "Failed to create post")
                }
            }
        }
    }

    // photo picker
    fun addAttachments(uris: Collection<Uri>) {
        val combined = _uiState.value.attachments + uris

        // TODO: also perform file size check when that is decided with image repo impl
        if (combined.size > FirestoreSettings.MAX_ATTACHMENTS) {
            _uiState.update { it.copy(attachmentsError = "You can attach up to 5 photos.") }
            return
        }

        // Check permissions on new uris, fail on error
        for (uri in uris) {
            if (!grantPersistablePermission(uri)) {
                return // error state is set by the helper
            }
        }

        _uiState.update { it.copy(attachments = combined.toSet(), attachmentsError = "") }
    }

    // used to signal to android we want persistent access to these files
    // prevents crashes during app recomposition
    private fun grantPersistablePermission(uri: Uri): Boolean {
        val resolver = appContext?.contentResolver ?: return true // skip in preview/tests

        return try {
            resolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            true
        } catch (e: Exception) {
            _uiState.update {
                it.copy(attachmentsError = "Failed getting permission for attachment: $e")
            }
            false
        }
    }

    fun removeAttachments(uris: Collection<Uri>) {
        _uiState.update { it.copy(attachments = it.attachments - uris, attachmentsError = "") }
    }
}
