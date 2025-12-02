package com.swent.skillswap.model.user

import com.swent.skillswap.model.tags.SkillTag

/**
 * Represents a skill a user can offer to others.
 *
 * @property name The category or type of skill, defined by [SkillTag].
 * @property rank The user's self-assessed proficiency as a float value (1.0–3.0).
 * @property description Optional details describing the user’s experience or how they can help with
 *   this skill.
 */
data class Skill(val name: SkillTag, val rank: Float, val description: String)

/**
 * Defines the baseline proficiency levels a user can select when adding a skill. These serve as
 * starting points for a user’s self-rated ability, while allowing the system to refine their
 * effective skill score over time through algorithmic updates (e.g., usage patterns, feedback, or
 * performance signals).
 *
 * @property value Numeric baseline associated with the level, used within the float-based ranking
 *   system.
 * @property label Human-readable value used for display purposes.
 *
 * Levels:
 * - [FAMILIAR]: 2.0 — User has a basic understanding of the skill and can assist with simple tasks.
 * - [CAPABLE]: 3.5 — User can reliably perform tasks and help others confidently.
 * - [EXPERT]: 5.0 — User has deep knowledge and extensive experience.
 */
enum class SkillRank(val value: Float, val label: String) {
    FAMILIAR(2.0f, "Familiar"),
    CAPABLE(3.5f, "Capable"),
    EXPERT(5.0f, "Expert")
}
