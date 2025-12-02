package com.swent.skillswap.model.user

import com.swent.skillswap.model.tags.SkillTag

/**
 * Represents a skill a user can offer to others.
 *
 * @property name The category or type of skill, defined by [SkillTag].
 * @property rank The user's self-assessed proficiency, represented by [SkillRank].
 * @property description Optional details describing the user’s experience or how they can help with
 *   this skill.
 */
data class Skill(val name: SkillTag, val rank: SkillRank, val description: String)

/**
 * Defines the self-rated proficiency levels a user can choose for a skill.
 *
 * @property label Human-readable value used for display purposes.
 *
 * Levels:
 * - [FAMILIAR]: The user understands the skill and can assist with basic tasks.
 * - [CAPABLE]: The user can reliably perform tasks and help others confidently.
 * - [EXPERT]: The user has deep knowledge and extensive experience.
 */
enum class SkillRank(val label: String) {
    FAMILIAR("Familiar"),
    CAPABLE("Capable"),
    EXPERT("Expert")
}
