package com.swent.skillswap.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.colorResource
import com.swent.skillswap.R

@Composable
fun SkillSwapAppTheme(content: @Composable () -> Unit) {
    val isDarkTheme = isSystemInDarkTheme()

    val appColorScheme = if (isDarkTheme) {
        darkColorScheme(
            primary = colorResource(id = R.color.md_theme_primary),
            onPrimary = colorResource(id = R.color.md_theme_onPrimary),
            primaryContainer = colorResource(id = R.color.md_theme_primaryContainer),
            onPrimaryContainer = colorResource(id = R.color.md_theme_onPrimaryContainer),
            secondaryContainer = colorResource(id = R.color.md_theme_secondaryContainer),
            onSecondaryContainer = colorResource(id = R.color.md_theme_onSecondaryContainer),
            secondary = colorResource(id = R.color.md_theme_secondary),
            onSecondary = colorResource(id = R.color.md_theme_onSecondary),
            tertiary = colorResource(id = R.color.md_theme_tertiary),
            onTertiary = colorResource(id = R.color.md_theme_onTertiary),
            background = colorResource(id = R.color.md_theme_background),
            onBackground = colorResource(id = R.color.md_theme_onBackground),
            surface = colorResource(id = R.color.md_theme_surface),
            onSurface = colorResource(id = R.color.md_theme_onSurface)
        )
    } else {
        lightColorScheme(
            primary = colorResource(id = R.color.md_theme_primary),
            onPrimary = colorResource(id = R.color.md_theme_onPrimary),
            primaryContainer = colorResource(id = R.color.md_theme_primaryContainer),
            onPrimaryContainer = colorResource(id = R.color.md_theme_onPrimaryContainer),
            secondaryContainer = colorResource(id = R.color.md_theme_secondaryContainer),
            onSecondaryContainer = colorResource(id = R.color.md_theme_onSecondaryContainer),
            secondary = colorResource(id = R.color.md_theme_secondary),
            onSecondary = colorResource(id = R.color.md_theme_onSecondary),
            tertiary = colorResource(id = R.color.md_theme_tertiary),
            onTertiary = colorResource(id = R.color.md_theme_onTertiary),
            background = colorResource(id = R.color.md_theme_background),
            onBackground = colorResource(id = R.color.md_theme_onBackground),
            surface = colorResource(id = R.color.md_theme_surface),
            onSurface = colorResource(id = R.color.md_theme_onSurface)
        )
    }

    MaterialTheme(colorScheme = appColorScheme, typography = Typography, content = content)
}
