package com.swent.skillswap.ui.utils

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swent.skillswap.model.tags.SkillTag
import com.swent.skillswap.ui.auth.CreateAccountTags

@Preview(showBackground = true)
@Composable
fun SkillPill(
    skill: SkillTag = SkillTag.MACHINE_DESIGN,
    isSelected: Boolean = false,
    onClick: (skill: SkillTag) -> Unit = { skill -> skill }
) {
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
    Button(
        onClick = { onClick(skill) },
        colors = ButtonDefaults.buttonColors(containerColor = backgroundColor),
        contentPadding = PaddingValues(11.dp, 0.dp),
        modifier = Modifier.height(34.dp).testTag(CreateAccountTags.SKILL_CHIP_PREFIX + skill.name)
    ) {
        Text(
            text = skill.name, // TODO: make enum names user-friendly
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp,
            color = textColor,
        )
    }
}
