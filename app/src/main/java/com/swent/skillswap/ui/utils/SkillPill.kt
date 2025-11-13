package com.swent.skillswap.ui.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swent.skillswap.model.tags.SkillTag
import com.swent.skillswap.ui.auth.CreateAccountTags

@Composable
fun SkillPill(skill: SkillTag, isSelected: Boolean, onClick: (skill: SkillTag) -> Unit) {
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

    Box(
        modifier =
            Modifier.background(backgroundColor, shape = RoundedCornerShape(50))
                .clickable { onClick(skill) }
                .testTag(CreateAccountTags.SKILL_CHIP_PREFIX + skill.name)
                .padding(horizontal = 11.dp, vertical = 6.dp)
    ) {
        Text(
            text = skill.name, // TODO: make enum names user-friendly
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp,
            color = textColor,
        )
    }
}
