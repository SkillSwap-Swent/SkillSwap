package com.swent.skillswap.model.tags

sealed interface EveryTag {
    companion object {
        fun valueOf(value: String): EveryTag = TagRegistry.fromValue(value)
    }
}
