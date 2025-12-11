package com.swent.skillswap.ui.post

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.swent.skillswap.model.images.PictureRepositoryInterface
import com.swent.skillswap.model.post.PostRepository

class RequestViewModelFactory(
    private val appContext: Context? = null,
    private val postRepository: PostRepository,
    private val storageRepository: PictureRepositoryInterface,
    private val currentUserId: String,
    private val postId: String? = null
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return RequestViewModel(
            appContext,
            postRepository,
            storageRepository,
            currentUserId,
            postId
        )
            as T
    }
}
