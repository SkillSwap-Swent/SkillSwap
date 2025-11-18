package com.swent.skillswap.model.tags

object TagRegistry {
    private val parsers: MutableList<(String) -> EveryTag?> = mutableListOf()

    init {
        // Register SkillTag
        register { value ->
            try {
                SkillTag.valueOf(value)
            } catch (_: IllegalArgumentException) {
                null
            }
        }

        // Register PostTag
        register { value ->
            try {
                PostTag.valueOf(value)
            } catch (_: IllegalArgumentException) {
                null
            }
        }
    }

    fun register(parser: (String) -> EveryTag?) {
        parsers += parser
    }

    fun fromValue(value: String): EveryTag {
        for (parser in parsers) {
            val tag = parser(value)
            if (tag != null) return tag
        }
        throw IllegalArgumentException("Unknown tag: $value")
    }
}
