package com.swent.skillswap.model.tags

data class EveryTag(
    var postTag: PostTag? = null,
    var skillTag: SkillTag? = null
) {
    companion object {
        fun of(tag: PostTag) = EveryTag(postTag = tag)
        fun of(tag: SkillTag) = EveryTag(skillTag = tag)
    }
}

