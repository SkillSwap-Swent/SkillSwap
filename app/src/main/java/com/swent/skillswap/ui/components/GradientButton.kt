// AI-Generated: Reusable gradient button component with primary/secondary styles
package com.swent.skillswap.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swent.skillswap.ui.theme.ButtonCancelEnd
import com.swent.skillswap.ui.theme.ButtonCancelStart
import com.swent.skillswap.ui.theme.ButtonGradientEnd
import com.swent.skillswap.ui.theme.ButtonGradientStart
import com.swent.skillswap.ui.theme.ProfileTextPrimary

/**
 * A reusable gradient button component with primary and secondary styles.
 *
 * @param text The text to display on the button
 * @param onClick The callback function when the button is clicked
 * @param modifier The modifier to apply to the button
 * @param isPrimary If true, uses primary gradient (blue), otherwise uses secondary gradient (gray)
 * @param height The height of the button in dp
 */
@Composable
fun GradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isPrimary: Boolean = true,
    height: Int = 48
) {
    val startColor = if (isPrimary) ButtonGradientStart else ButtonCancelStart
    val endColor = if (isPrimary) ButtonGradientEnd else ButtonCancelEnd

    Button(
        onClick = onClick,
        modifier = modifier.height(height.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(8.dp)
    ) {
        Box(
            modifier =
                Modifier.fillMaxSize()
                    .background(
                        brush = Brush.horizontalGradient(colors = listOf(startColor, endColor))
                    )
                    .clip(RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = ProfileTextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
