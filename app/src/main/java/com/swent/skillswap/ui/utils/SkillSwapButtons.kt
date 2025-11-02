package com.swent.skillswap.ui.utils

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.swent.skillswap.ui.theme.BrushDirection
import com.swent.skillswap.ui.theme.getLinearBrush

@Composable
fun GradientButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    gradient: List<Color> =
        listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primaryContainer),
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
                .background(getLinearBrush(brushDirection = gradientDirection, gradient = gradient))
    ) {
        content()
    }
}

@Preview(showBackground = true)
@Composable
fun SkillSwapButtonV1(
    onClick: () -> Unit = {},
    enable: Boolean = true,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(50),
    contentColor: Color = MaterialTheme.colorScheme.primary,
    disableContentColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.16f),
    content: @Composable (RowScope.() -> Unit) = { Text(text = "test         test") },
) {
    OutlinedButton(
        enabled = enable,
        onClick = onClick,
        colors =
            ButtonColors(
                MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                contentColor,
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                disableContentColor
            ),
        border =
            if (!enable) BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
            else null,
        shape = shape,
        modifier = modifier.outerShadow(shape)
    ) {
        content()
    }
}
/**
 * @author chatGPT a function to make an outer only shadow only while the button is still
 *   transparent
 */
fun Modifier.outerShadow(
    shape: Shape,
    blur: Dp = 4.dp,
    offsetY: Dp = 2.dp,
): Modifier =
    this.then(
        Modifier.drawBehind {
            // Build the pill/path for this size
            val outline = shape.createOutline(size, layoutDirection, this)
            val rr =
                when (outline) {
                    is Outline.Rounded -> outline.roundRect
                    is Outline.Generic -> return@drawBehind // not supported here
                    is Outline.Rectangle ->
                        RoundRect(0f, 0f, size.width, size.height, CornerRadius.Zero)
                }
            val path = Path().apply { addRoundRect(rr) }

            val frameworkPaint =
                Paint().asFrameworkPaint().apply {
                    isAntiAlias = true
                    alpha = 120
                    // Blur for the soft shadow
                    maskFilter =
                        android.graphics.BlurMaskFilter(
                            blur.toPx(),
                            android.graphics.BlurMaskFilter.Blur.NORMAL
                        )
                }

            drawIntoCanvas { canvas ->
                // Clip OUTSIDE the pill so shadow is only outside, not under the fill
                canvas.save()
                canvas.clipPath(path, clipOp = ClipOp.Difference)
                canvas.nativeCanvas.drawRoundRect(
                    rr.left,
                    rr.top + offsetY.toPx(),
                    rr.right,
                    rr.bottom + offsetY.toPx(),
                    rr.topLeftCornerRadius.x,
                    rr.topLeftCornerRadius.y,
                    frameworkPaint
                )
                canvas.restore()
            }
        }
    )
