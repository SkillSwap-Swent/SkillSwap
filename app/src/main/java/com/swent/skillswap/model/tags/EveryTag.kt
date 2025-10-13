package com.swent.skillswap.model.tags

sealed interface EveryTag {
    // TODO: This code is kind of hacky. Maybe find a better way.
    companion object {
        fun valueOf(value: String): EveryTag {
            return try {
                PostTag.valueOf(value)
            } catch (_: IllegalArgumentException) {
                try {
                    SkillTag.valueOf(value)
                } catch (_: IllegalArgumentException) {
                    throw IllegalArgumentException("Unknown tag: $value")
                }
            }
        }
    }
}
