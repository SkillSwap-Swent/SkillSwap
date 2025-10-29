package com.swent.skillswap.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/** an enum class to represent brush direction */
enum class BrushDirection {
    TOP_DOWN,
    DOWN_TOP,
    LEFT_RIGHT,
    RIGHT_LEFT
}

@Composable
fun DefaultGradient(endColor: Color = MaterialTheme.colorScheme.primaryContainer): List<Color> {
    return listOf(MaterialTheme.colorScheme.primary, endColor)
}

/**
 * a function to make directional brush for linear gradient
 *
 * @param gradient the list of color making the gradient (optional, defaults to DEFAULT_GRADIENT)
 * @param brushDirection the direction wanted for the brush
 * @return a ready to use brush
 */
@Composable
fun getLinearBrush(
    brushDirection: BrushDirection,
    gradient: List<Color> =
        listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primaryContainer)
): Brush {
    return when (brushDirection) {
        BrushDirection.TOP_DOWN ->
            Brush.linearGradient(
                colors = gradient,
                start = Offset(0f, 0f),
                end = Offset(0f, Float.POSITIVE_INFINITY)
            )
        BrushDirection.DOWN_TOP ->
            Brush.linearGradient(
                colors = gradient,
                start = Offset(0f, Float.POSITIVE_INFINITY),
                end = Offset(0f, 0f)
            )
        BrushDirection.LEFT_RIGHT ->
            Brush.linearGradient(
                colors = gradient,
                start = Offset(0f, 0f),
                end = Offset(Float.POSITIVE_INFINITY, 0f)
            )
        BrushDirection.RIGHT_LEFT ->
            Brush.linearGradient(
                colors = gradient,
                start = Offset(Float.POSITIVE_INFINITY, 0f),
                end = Offset(0f, 0f)
            )
    }
}
