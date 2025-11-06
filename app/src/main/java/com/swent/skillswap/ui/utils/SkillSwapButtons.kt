/**
 * Utility composables and modifiers used across the SkillSwap UI.
 *
 * Provides gradient-based buttons, transparent styled buttons, and a custom outer-shadow modifier
 * that supports translucent components without obscuring shadows.
 *
 * @author Topaze17 (Eliott) Comments drafted with ChatGPT, reviewed and validated manually.
 */
package com.swent.skillswap.ui.utils

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.*
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swent.skillswap.ui.theme.BrushDirection
import com.swent.skillswap.ui.theme.SkillSwapAppTheme
import com.swent.skillswap.ui.theme.getLinearBrush

enum class SkillSwapButtonShape {
    ROUND,
    SQUARE
}

enum class SkillSwapButtonSize {
    XS,
    S,
    M,
    L,
    XL
}

/**
 * A reusable gradient-filled button built on top of [OutlinedButton].
 *
 * The button uses a transparent container and renders a linear gradient as its background via the
 * [getLinearBrush] helper.
 *
 * @param onClick callback invoked when the button is pressed.
 * @param modifier optional [Modifier] for layout or styling adjustments.
 * @param gradient list of [Color] values used to generate the gradient.
 * @param contentColor color applied to the button’s content when enabled.
 * @param disableContentColor color applied to the content when disabled.
 * @param gradientDirection direction of the gradient flow.
 * @param content composable lambda defining the button’s inner content.
 */
@Composable
fun GradientButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    gradient: List<Color> =
        listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primaryContainer),
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    disableContentColor: Color = MaterialTheme.colorScheme.onSurface,
    gradientDirection: BrushDirection = BrushDirection.LEFT_RIGHT,
    content: @Composable (RowScope.() -> Unit)
) {
    OutlinedButton(
        onClick = onClick,
        colors =
            ButtonColors(
                containerColor = Color.Transparent,
                contentColor = contentColor,
                disabledContainerColor = Color.Transparent,
                disabledContentColor = disableContentColor
            ),
        enabled = enabled,
        border = null,
        modifier =
            modifier
                .clip(ButtonDefaults.outlinedShape)
                .background(getLinearBrush(brushDirection = gradientDirection, gradient = gradient))
    ) {
        content()
    }
}

/**
 * A semi-transparent SkillSwap button variant designed for subtle surfaces.
 *
 * Uses an [OutlinedButton] with a translucent background, optional border when disabled, and a soft
 * outer shadow defined by [outerShadow].
 *
 * @param onClick callback invoked when the button is pressed.
 * @param enable whether the button is enabled.
 * @param modifier optional [Modifier] for layout or styling adjustments.
 * @param shape defines the button’s outline shape (default is pill-shaped).
 * @param contentColor color applied to text and icons when enabled.
 * @param disableContentColor color applied when the button is disabled.
 * @param content composable lambda defining the button’s inner content.
 */
@Composable
fun SkillSwapShadowButton(
    onClick: () -> Unit = {},
    enable: Boolean = true,
    modifier: Modifier = Modifier,
    shape: Shape = pill_shape,
    contentColor: Color = MaterialTheme.colorScheme.primary,
    disableContentColor: Color =
        MaterialTheme.colorScheme.onSurface.copy(text_disable_button_alpha),
    content: @Composable (RowScope.() -> Unit) = { Text(text = "test         test") },
) {
    ElevatedButton(
        enabled = enable,
        onClick = onClick,
        colors =
            ButtonColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(container_field_alpha),
                contentColor = contentColor,
                disabledContainerColor =
                    MaterialTheme.colorScheme.onSurface.copy(container_disable_button_alpha),
                disabledContentColor = disableContentColor
            ),
        border =
            if (!enable)
                BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outline.copy(stroke_disable_button_alpha)
                )
            else null,
        shape = shape,
        modifier = modifier
    ) {
        content()
    }
}

/**
 * Draws a blurred, **outer-only** shadow around a shape, keeping the inner area fully transparent.
 * Useful for “glassmorphism” effects where a component should appear raised but translucent.
 *
 * The shadow is drawn outside the provided [shape] using a blur mask. The inside of the shape
 * remains untouched, so the button or surface can stay transparent.
 *
 * @param shape the outline shape used to calculate shadow boundaries.
 * @param blur the radius of the shadow blur; higher values produce a softer glow.
 * @param offsetY the vertical offset of the shadow, in dp.
 */
fun Modifier.outerShadow(
    shape: Shape,
    blur: Dp = 4.dp,
    offsetY: Dp = 2.dp,
): Modifier =
    this.then(
        Modifier.drawBehind {
            // Create an outline for the current shape and size
            val outline = shape.createOutline(size, layoutDirection, this)
            val rr =
                when (outline) {
                    is Outline.Rounded -> outline.roundRect
                    is Outline.Generic -> return@drawBehind // generic paths not supported
                    is Outline.Rectangle ->
                        RoundRect(0f, 0f, size.width, size.height, CornerRadius.Zero)
                }

            val path = Path().apply { addRoundRect(rr) }

            // Configure the native paint with a blur mask
            val frameworkPaint =
                Paint().asFrameworkPaint().apply {
                    isAntiAlias = true
                    alpha = shadow_opacity // shadow opacity
                    maskFilter =
                        android.graphics.BlurMaskFilter(
                            blur.toPx(),
                            android.graphics.BlurMaskFilter.Blur.NORMAL
                        )
                }
            // Clip outside of the shape so the shadow is drawn only externally
            drawIntoCanvas { canvas ->
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

@Composable
fun SkillSwapButtonOutline(
    labelText: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    shape: SkillSwapButtonShape = SkillSwapButtonShape.ROUND,
    size: SkillSwapButtonSize = SkillSwapButtonSize.M
) {
    val height: Dp
    val textSize: androidx.compose.ui.unit.TextUnit
    val horizontalPadding: Dp
    val iconSize: Dp

    when (size) {
        SkillSwapButtonSize.XS -> {
            height = 28.dp
            textSize = 12.sp
            horizontalPadding = 8.dp
            iconSize = 14.dp
        }
        SkillSwapButtonSize.S -> {
            height = 32.dp
            textSize = 13.sp
            horizontalPadding = 10.dp
            iconSize = 16.dp
        }
        SkillSwapButtonSize.M -> {
            height = 40.dp
            textSize = 14.sp
            horizontalPadding = 12.dp
            iconSize = 18.dp
        }
        SkillSwapButtonSize.L -> {
            height = 48.dp
            textSize = 16.sp
            horizontalPadding = 16.dp
            iconSize = 20.dp
        }
        SkillSwapButtonSize.XL -> {
            height = 56.dp
            textSize = 18.sp
            horizontalPadding = 20.dp
            iconSize = 24.dp
        }
    }

    // Define shape
    val buttonShape: Shape =
        when (shape) {
            SkillSwapButtonShape.ROUND -> MaterialTheme.shapes.extraLarge
            SkillSwapButtonShape.SQUARE -> MaterialTheme.shapes.small
        }

    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(height).defaultMinSize(minWidth = 80.dp),
        shape = buttonShape,
        colors =
            ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.onSurface,
                disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            ),
        border = ButtonDefaults.outlinedButtonBorder
    ) {
        Row(
            modifier = Modifier.padding(horizontal = horizontalPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(iconSize).padding(end = 6.dp)
                )
            }
            Text(text = labelText, fontSize = textSize, fontWeight = FontWeight.Medium)
        }
    }
}
/**
 * /** Preview showcasing different variations of [SkillSwapButtonOutline].
 *
 * This preview demonstrates:
 * - A **default** outlined button.
 * - A button **with an icon** and larger size.
 * - A **disabled** medium-sized button.
 * - A **square extra-large** button for visual contrast.
 *
 * The preview uses the [SkillSwapAppTheme] for consistent styling and layout spacing to visualize
 * how the component adapts to various configurations.
 *
 * @author Joey Gugler using chatGPT /
 * @Preview(showBackground = true, showSystemUi = true)
 * @Composable fun PreviewSkillSwapButtonOutline() { SkillSwapAppTheme { Column( modifier =
 *   Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp), ) {
 *   SkillSwapButtonOutline( labelText = "Default", onClick = {}, )
 *
 * SkillSwapButtonOutline( labelText = "With Icon", onClick = {}, icon = Icons.Default.Favorite,
 * size = SkillSwapButtonSize.L )
 *
 * SkillSwapButtonOutline( labelText = "Disabled", onClick = {}, enabled = false, size =
 * SkillSwapButtonSize.M )
 *
 * SkillSwapButtonOutline( labelText = "Square XL", onClick = {}, shape =
 * SkillSwapButtonShape.SQUARE, size = SkillSwapButtonSize.XL ) } } } /
 */
