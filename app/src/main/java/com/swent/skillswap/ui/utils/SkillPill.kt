/** nextPillRankOrNull with ChatGPT, comments also with ChatGPT */
package com.swent.skillswap.ui.utils

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swent.skillswap.model.tags.SkillTag
import com.swent.skillswap.model.user.Skill
import com.swent.skillswap.model.user.SkillRank
import com.swent.skillswap.ui.auth.CreateAccountTags

private val defaultSkill = Skill(SkillTag.MACHINE_DESIGN, SkillRank.CAPABLE.value, "")

@Preview(showBackground = true)
@Composable
fun SkillPill(
    skill: SkillTag = defaultSkill.name,
    isSelected: Boolean = false,
    onClick: (skill: SkillTag) -> Unit = { skill -> skill },
    modifier: Modifier = Modifier
) {
    val (backgroundColor, textColor) = skillPillColours(isSelected)

    Button(
        onClick = { onClick(skill) },
        colors = ButtonDefaults.buttonColors(containerColor = backgroundColor),
        contentPadding = PaddingValues(11.dp, 0.dp),
        modifier =
            modifier.heightIn(34.dp).testTag(CreateAccountTags.SKILL_CHIP_PREFIX + skill.name)
    ) {
        Text(
            text = skill.toUIString(),
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp,
            color = textColor,
            textAlign = TextAlign.Center
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SkillPillRated(
    skill: Skill = defaultSkill,
    isSelected: Boolean = false,
    onClick: (Skill) -> Unit = {}
) {
    val (backgroundColor, textColor) = skillPillColours(isSelected)

    Button(
        onClick = { onClick(skill) },
        colors = ButtonDefaults.buttonColors(containerColor = backgroundColor),
        contentPadding = PaddingValues(11.dp, 2.dp),
        modifier = Modifier.testTag(CreateAccountTags.SKILL_CHIP_PREFIX + skill.name)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = skill.name.toUIString(),
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                color = textColor
            )

            StarRatingBar(rating = skill.rank * 3f / 5f, size = 13, max = 3, color = textColor)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SelectedRatedPill() {
    SkillPillRated(skill = defaultSkill, isSelected = true)
}

@Composable
private fun skillPillColours(isSelected: Boolean): Pair<Color, Color> {
    val backgroundColor =
        if (isSelected) {
            MaterialTheme.colorScheme.secondary
        } else {
            MaterialTheme.colorScheme.secondaryContainer
        }
    val textColor =
        if (isSelected) {
            MaterialTheme.colorScheme.onSecondary
        } else {
            MaterialTheme.colorScheme.onSecondaryContainer
        }
    return Pair(backgroundColor, textColor)
}

fun nextPillRankOrNull(currentRank: Float): Float? {
    val ordered = SkillRank.entries.sortedBy { it.value }

    val currentIndex = ordered.indexOfFirst { it.value == currentRank }

    return when {
        // Skill has no rank matching enum -> start at first level
        currentIndex == -1 -> ordered.first().value

        // Move to next enum level
        currentIndex < ordered.lastIndex -> ordered[currentIndex + 1].value

        // Already at highest (EXPERT) -> return null to indicate "remove"
        else -> null
    }
}
