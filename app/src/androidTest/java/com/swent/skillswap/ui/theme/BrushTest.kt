package com.swent.skillswap.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.swent.skillswap.resources.theme.BrushDirection
import com.swent.skillswap.resources.theme.SkillSwapAppTheme
import com.swent.skillswap.resources.theme.getLinearBrush
import junit.framework.TestCase.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BrushTest {

    @get:Rule val composeTestRule = createComposeRule()

    @Test
    fun getLinearBrush_composes_without_crashing_for_all_directions() {
        composeTestRule.setContent {
            SkillSwapAppTheme {
                BrushDirection.values().forEach { direction ->
                    val brush = getLinearBrush(direction)
                    Box(modifier = Modifier.background(brush).size(10.dp))
                }
            }
        }
    }

    private val gradient = listOf(Color.Red, Color.Blue)

    @Composable
    private fun TestBrush(brush: Brush) {
        Box(modifier = Modifier.size(100.dp).background(brush))
    }

    @Test
    fun getLinearBrush_rendersGradientWithExpectedColors() {
        composeTestRule.setContent {
            TestBrush(getLinearBrush(BrushDirection.LEFT_RIGHT, gradient))
        }

        val image = composeTestRule.onRoot().captureToImage()

        // Convert to Android Bitmap to read pixels
        val bitmap = image.asAndroidBitmap()

        val width = bitmap.width
        val height = bitmap.height

        val leftPixelColor = Color(bitmap.getPixel((width * 0.05f).toInt(), height / 2))
        val rightPixelColor = Color(bitmap.getPixel((width * 0.95f).toInt(), height / 2))

        fun isColorClose(a: Color, b: Color): Boolean {
            val threshold = 5f
            return (kotlin.math.abs(a.red - b.red) < threshold &&
                kotlin.math.abs(a.green - b.green) < threshold &&
                kotlin.math.abs(a.blue - b.blue) < threshold)
        }

        // Assertions
        val startColor = gradient.first()
        val endColor = gradient.last()

        assertTrue(
            "Left pixel ($leftPixelColor) should be close to the start color ($startColor)",
            isColorClose(leftPixelColor, endColor)
        )
        assertTrue(
            "Right pixel ($rightPixelColor) should be close to the end color ($endColor)",
            isColorClose(rightPixelColor, endColor)
        )
    }
}
