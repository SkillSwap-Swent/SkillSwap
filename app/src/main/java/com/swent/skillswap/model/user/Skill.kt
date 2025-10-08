package com.swent.skillswap.model.user

import com.swent.skillswap.model.tags.SkillTag

data class Skill(val name: SkillTag, val rank: Float, val description: String)
