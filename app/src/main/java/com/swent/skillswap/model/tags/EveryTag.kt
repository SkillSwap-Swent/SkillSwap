package com.swent.skillswap.model.tags

/**
 * A sealed interface representing any type of tag that can be associated with a post. This acts as
 * a common supertype for different tag categories, currently including skill tags and post tags.
 */
sealed interface EveryTag {
    companion object {
        fun valueOf(value: String): EveryTag = TagRegistry.fromValue(value)
    }
}
