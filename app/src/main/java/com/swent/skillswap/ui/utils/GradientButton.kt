package com.swent.skillswap.ui.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import com.swent.skillswap.ui.theme.BrushDirection
import com.swent.skillswap.ui.theme.DefaultGradient
import com.swent.skillswap.ui.theme.getLinearBrush

@Composable
fun GradientButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    gradient: List<Color> = DefaultGradient,
    contentColor: Color = Color.White,
    disableContentColor: Color = Color.Gray,
    gradientDirection: BrushDirection = BrushDirection.LEFT_RIGHT,
    content: @Composable (RowScope.() -> Unit)
) {
    OutlinedButton(
        onClick = onClick,
        colors =
            ButtonColors(Color.Transparent, contentColor, Color.Transparent, disableContentColor),
        border = null,
        modifier =
            modifier
                .clip(ButtonDefaults.outlinedShape)
                .background(getLinearBrush(gradient, gradientDirection))
    ) {
        content()
    }
}
