package com.swent.skillswap.ui.post

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint
import com.swent.skillswap.model.post.PaymentMethod
import com.swent.skillswap.model.post.PostRepository
import com.swent.skillswap.model.post.PostStatus
import com.swent.skillswap.model.post.PostType
import com.swent.skillswap.model.post.Request
import com.swent.skillswap.model.tags.EveryTag
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
    val tags: Set<EveryTag> = emptySet(),
    val paymentMethod: PaymentMethod = PaymentMethod.SKILLS,
    val expiry: Timestamp = Timestamp.now(),
    val location: GeoPoint = GeoPoint(46.5191, 6.5668), // Default fallback

    // Error fields
    val titleError: String = "",
    val descriptionError: String = "",
    val tagsError: String = "",
    val paymentMethodsError: String = "",
    val expiryError: String = "",

    // Submission state
    val isLoading: Boolean = false,
    val submitError: String? = null,
    val isSubmitSuccessful: Boolean = false
)

class RequestViewModel(
    private val appContext: Context? = null,
    private val postRepository: PostRepository,
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

    fun addTag(tag: EveryTag) {
        _uiState.update { current ->
            if (tag !in current.tags) {
                current.copy(tags = current.tags + tag, tagsError = "")
            } else {
                current
            }
        }
    }

    fun removeTag(tag: EveryTag) {
        _uiState.update { current ->
            if (tag in current.tags) {
                current.copy(tags = current.tags - tag)
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
                val uid =
                    when (postOperation) {
                        PostOperation.ADD -> postRepository.getNewUid(PostType.REQUEST)
                        PostOperation.EDIT -> postId!!
                    }
                val request =
                    Request(
                        uid = uid,
                        title = _uiState.value.title,
                        description = _uiState.value.description,
                        ownerId = currentUserId,
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
                        media = emptyList(),
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
}
