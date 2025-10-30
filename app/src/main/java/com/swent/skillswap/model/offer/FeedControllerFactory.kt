package com.swent.skillswap.model.offer

import com.swent.skillswap.model.post.PostRepository
import com.swent.skillswap.model.post.PostType
import javax.inject.Inject

/**
 * A factory responsible for creating instances of [FeedController]. This is useful for dependency
 * injection and separating creation logic from the ViewModel.
 */
class FeedControllerFactory
@Inject
constructor(
    private val recommendationEngine: RecommendationEngine,
    private val thumbnailRepository: ThumbnailRepository,
    private val postRepository: PostRepository,
    private val chatRepository: ChatRepository,
) {
    /**
     * Creates a new instance of [FeedController].
     *
     * @param userIdPerformingAction The ID of the user interacting with the feed.
     * @param feedType The type of posts to be displayed in the feed.
     * @return A configured instance of [FeedController].
     */
    suspend fun create(userIdPerformingAction: String, feedType: PostType): FeedController {
        val fc =
            FeedControllerImpl(
                recommendationEngine = recommendationEngine,
                thumbnailRepository = thumbnailRepository,
                postRepository = postRepository,
                chatRepository = chatRepository,
                userIdPerformingActions = userIdPerformingAction,
                feedType = feedType
            )
        fc.initialLoad()
        return fc
    }
}
