// AI-Generated: Reusable horizontal divider component for profile screens
package com.swent.skillswap.ui.utils

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
/**
 * A reusable horizontal divider component with consistent styling for profile screens. Uses the
 * ProfileDivider color from the theme.
 */
@Composable
fun ProfileDivider() {
    val color = MaterialTheme.colorScheme.outline
    Canvas(modifier = Modifier.fillMaxWidth().height(1.dp)) {
        drawLine(
            color = color,
            start = Offset(0f, 0f),
            end = Offset(size.width, 0f),
            strokeWidth = 1.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}
