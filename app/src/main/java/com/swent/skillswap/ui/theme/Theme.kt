package com.swent.skillswap.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.colorResource
import androidx.core.view.WindowCompat
import com.swent.skillswap.R


@Composable
fun SkillSwapAppTheme(
    content: @Composable () -> Unit
) {
    val appColorScheme = lightColorScheme(
        primary = colorResource(id = R.color.md_theme_primary),
        onPrimary = colorResource(id = R.color.md_theme_onPrimary),
        primaryContainer = colorResource(id = R.color.md_theme_primaryContainer),
        onPrimaryContainer = colorResource(id = R.color.md_theme_onPrimaryContainer),
        secondary = colorResource(id = R.color.md_theme_secondary),
        onSecondary = colorResource(id = R.color.md_theme_onSecondary),
        tertiary = colorResource(id = R.color.md_theme_tertiary),
        onTertiary = colorResource(id = R.color.md_theme_onTertiary),
        background = colorResource(id = R.color.md_theme_background),
        onBackground = colorResource(id = R.color.md_theme_onBackground),
        surface = colorResource(id = R.color.md_theme_surface),
        onSurface = colorResource(id = R.color.md_theme_onSurface)
    )
    val view = LocalView.current
    SideEffect {
        val window = (view.context as Activity).window
        window.statusBarColor = appColorScheme.primary.toArgb()
        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
    }

    MaterialTheme(
        colorScheme = appColorScheme,
        typography = Typography,
        content = content
    )
}

