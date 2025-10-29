package com.swent.skillswap.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
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
                    Box(modifier = Modifier.Companion.background(brush).size(10.dp))
                }
            }
        }
    }
}
