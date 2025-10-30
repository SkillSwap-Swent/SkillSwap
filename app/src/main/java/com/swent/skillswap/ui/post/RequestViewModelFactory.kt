package com.swent.skillswap.ui.post

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.swent.skillswap.model.post.PostRepository

class RequestViewModelFactory(
    private val postRepository: PostRepository,
    private val currentUserId: String,
    private val postId: String? = null
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T: ViewModel> create(modelClass: Class<T>): T {
        return RequestViewModel(postRepository, currentUserId, postId) as T
    }
}