package com.swent.skillswap.model.tags

enum class PostTag : EveryTag {
    ONE_TIME,
    REOCCURRING,
    ;

    companion object {
        // this runs only once per JVM instance
        init {
            TagRegistry.register { value ->
                try {
                    valueOf(value)
                } catch (_: IllegalArgumentException) {
                    null
                }
            }
        }
    }
}
