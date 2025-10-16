package com.swent.skillswap.ui.post

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
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

data class NewRequestUIState(
    val title: String = "",
    val description: String = "",
    val tags: List<EveryTag> = emptyList(),
    val paymentMethods: Set<PaymentMethod> = emptySet(),
    // val expiry: TimeStamp = Timestamp.now(),

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

class NewRequestViewModel(private val postRepository: PostRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(NewRequestUIState())
    val uiState: StateFlow<NewRequestUIState> = _uiState

    companion object {
        const val REQUEST_LIFESPAN_DAYS = 30L * 24 * 60 * 60 * 1000
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

    fun togglePaymentMethod(method: PaymentMethod) {
        _uiState.update { current ->
            val newMethods =
                if (method in current.paymentMethods) {
                    current.paymentMethods - method
                } else {
                    current.paymentMethods + method
                }
            current.copy(paymentMethods = newMethods)
        }
    }

    fun createRequest() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, submitError = null) }

            try {
                // Generate new UID from repository
                val uid = postRepository.getNewUid(PostType.REQUEST)

                // Create Request with current UI state + dummy values
                val request =
                    Request(
                        uid = uid,
                        title = _uiState.value.title,
                        description = _uiState.value.description,
                        ownerId = "DUMMY_USER_ID", // TODO: Get from Firebase Auth current user
                        tags = _uiState.value.tags,
                        paymentMethods = _uiState.value.paymentMethods.toList(),
                        expiry =
                            Timestamp(
                                Date(System.currentTimeMillis() + REQUEST_LIFESPAN_DAYS)
                            ), // DUMMY
                        creation = Timestamp.now(),
                        status = PostStatus.POSTED,
                        media = emptyList() // DUMMY
                    )

                // Will call validate() internally
                postRepository.addPost(request)

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
