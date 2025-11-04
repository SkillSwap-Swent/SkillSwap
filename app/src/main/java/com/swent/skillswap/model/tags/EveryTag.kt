package com.swent.skillswap.model.tags

data class EveryTag(var postTag: PostTag? = null, var skillTag: SkillTag? = null) {
    val value
        get() = postTag ?: skillTag

    companion object {
        fun of(tag: PostTag) = EveryTag(postTag = tag)

        fun of(tag: SkillTag) = EveryTag(skillTag = tag)
    }
}
