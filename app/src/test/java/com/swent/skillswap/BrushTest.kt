/**
 * @author chatGPT I have read the test and those are quite good for the intended purpose finished
 *   it with the 2 last test
 */
package com.swent.skillswap

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.LinearGradient
import com.swent.skillswap.ui.theme.BrushDirection
import com.swent.skillswap.ui.theme.getLinearBrush
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible
import org.junit.Assert.*
import org.junit.Test

/** little sanitize test */
class BrushTest {

    private val colors = listOf(Color.Red, Color.Blue)

    @Test
    fun `getLinearBrush returns a Brush for all directions`() {
        BrushDirection.values().forEach { direction ->
            val brush = getLinearBrush(colors, direction)
            assertTrue("Should return a LinearGradient Brush", brush is LinearGradient)
        }
    }

    @Test
    fun `getLinearBrush TOP_DOWN has correct offsets and color order`() {
        val brush = getLinearBrush(colors, BrushDirection.TOP_DOWN)
        assertBrushHasOffsets(brush, startY = 0f, endY = Float.POSITIVE_INFINITY)
        assertBrushHasColors(brush, expectedColors = colors)
    }

    @Test
    fun `getLinearBrush DOWN_TOP has correct offsets and color order`() {
        val brush = getLinearBrush(colors, BrushDirection.DOWN_TOP)
        assertBrushHasOffsets(brush, startY = Float.POSITIVE_INFINITY, endY = 0f)
        assertBrushHasColors(brush, expectedColors = colors)
    }

    @Test
    fun `getLinearBrush LEFT_RIGHT has correct offsets and color order`() {
        val brush = getLinearBrush(colors, BrushDirection.LEFT_RIGHT)
        assertBrushHasOffsets(brush, startX = 0f, endX = Float.POSITIVE_INFINITY)
        assertBrushHasColors(brush, expectedColors = colors)
    }

    @Test
    fun `getLinearBrush RIGHT_LEFT has correct offsets and color order`() {
        val brush = getLinearBrush(colors, BrushDirection.RIGHT_LEFT)
        assertBrushHasOffsets(brush, startX = Float.POSITIVE_INFINITY, endX = 0f)
        assertBrushHasColors(brush, expectedColors = colors)
    }

    private fun assertBrushHasColors(brush: Brush, expectedColors: List<Color>) {
        assertTrue(brush is LinearGradient)
        val colorProp = brush::class.memberProperties.find { it.name == "colors" }
        colorProp?.isAccessible = true
        val actualColors = colorProp?.getter?.call(brush) as? List<Color>
        assertNotNull("Brush colors should not be null", actualColors)
        assertEquals("Color count should match input", expectedColors.size, actualColors!!.size)
        assertEquals("First color should match", expectedColors.first(), actualColors.first())
        assertEquals("Last color should match", expectedColors.last(), actualColors.last())
    }
    /**
     * Helper to reflectively check the start and end offsets of a Brush.LinearGradient. Compose's
     * Brush doesn't expose them publicly, so we use reflection here only for test validation
     * purposes.
     */
    private fun assertBrushHasOffsets(
        brush: Brush,
        startX: Float? = null,
        startY: Float? = null,
        endX: Float? = null,
        endY: Float? = null
    ) {
        assertTrue(brush is LinearGradient)
        val kClass = brush::class
        val startProp = kClass.memberProperties.find { it.name == "start" }
        val endProp = kClass.memberProperties.find { it.name == "end" }

        startProp?.isAccessible = true
        endProp?.isAccessible = true
        val start = startProp?.getter?.call(brush) as? Offset
        val end = endProp?.getter?.call(brush) as? Offset

        start?.let {
            startX?.let { expected -> assertEquals(expected, it.x) }
            startY?.let { expected -> assertEquals(expected, it.y) }
        }
        end?.let {
            endX?.let { expected -> assertEquals(expected, it.x) }
            endY?.let { expected -> assertEquals(expected, it.y) }
        }
    }
}
