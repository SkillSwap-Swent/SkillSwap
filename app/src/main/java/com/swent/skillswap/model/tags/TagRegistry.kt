package com.swent.skillswap.model.tags

object TagRegistry {
    private val parsers: MutableList<(String) -> EveryTag?> = mutableListOf()

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
