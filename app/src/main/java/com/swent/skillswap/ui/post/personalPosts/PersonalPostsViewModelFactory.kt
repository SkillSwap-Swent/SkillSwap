/** @author Younes Belgroune - Made with the help of AI */
package com.swent.skillswap.ui.post.personalPosts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.swent.skillswap.model.post.PostRepository

/**
 * Factory for creating [PersonalPostsViewModel] instances.
 *
 * This factory is used to inject dependencies (PostRepository) into the ViewModel, following the
 * dependency injection pattern.
 *
 * @property postRepository The repository used to fetch posts from the database.
 */
class PersonalPostsViewModelFactory(private val postRepository: PostRepository) :
    ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PersonalPostsViewModel::class.java)) {
            return PersonalPostsViewModel(postRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
