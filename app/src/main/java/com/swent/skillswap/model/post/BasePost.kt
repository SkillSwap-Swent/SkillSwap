package com.swent.skillswap.model.post
/**
 * An abstract base class for all implementations of [Post] that provides a cached, precomputed
 * implementation of [Post.searchKeys].
 *
 * @author Joey Gugler using ChatGPT
 */
abstract class BasePost : Post {
    override val searchKeys: List<String> by lazy { buildSearchKeys() }
}
