package com.swent.skillswap.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.colorResource
import com.swent.skillswap.R

@Composable
fun SkillSwapAppTheme(content: @Composable () -> Unit) {

    val appColorScheme =
        ColorScheme(
            primary = colorResource(id = R.color.md_theme_primary),
            onPrimary = colorResource(id = R.color.md_theme_onPrimary),
            primaryContainer = colorResource(id = R.color.md_theme_primaryContainer),
            onPrimaryContainer = colorResource(id = R.color.md_theme_onPrimaryContainer),
            inversePrimary = colorResource(id = R.color.md_theme_inversePrimary),
            secondary = colorResource(id = R.color.md_theme_secondary),
            onSecondary = colorResource(id = R.color.md_theme_onSecondary),
            secondaryContainer = colorResource(id = R.color.md_theme_secondaryContainer),
            onSecondaryContainer = colorResource(id = R.color.md_theme_onSecondaryContainer),
            tertiary = colorResource(id = R.color.md_theme_tertiary),
            onTertiary = colorResource(id = R.color.md_theme_onTertiary),
            tertiaryContainer = colorResource(id = R.color.md_theme_tertiaryContainer),
            onTertiaryContainer = colorResource(id = R.color.md_theme_onTertiaryContainer),
            background = colorResource(id = R.color.md_theme_background),
            onBackground = colorResource(id = R.color.md_theme_onBackground),
            surface = colorResource(id = R.color.md_theme_surface),
            onSurface = colorResource(id = R.color.md_theme_onSurface),
            surfaceVariant = colorResource(id = R.color.md_theme_surfaceVariant),
            onSurfaceVariant = colorResource(id = R.color.md_theme_onSurfaceVariant),
            surfaceTint = colorResource(id = R.color.md_theme_surfaceTint),
            inverseSurface = colorResource(id = R.color.md_theme_inverseSurface),
            inverseOnSurface = colorResource(id = R.color.md_theme_inverseOnSurface),
            error = colorResource(id = R.color.md_theme_error),
            onError = colorResource(id = R.color.md_theme_onError),
            errorContainer = colorResource(id = R.color.md_theme_errorContainer),
            onErrorContainer = colorResource(id = R.color.md_theme_onErrorContainer),
            outline = colorResource(id = R.color.md_theme_outline),
            outlineVariant = colorResource(id = R.color.md_theme_outlineVariant),
            scrim = colorResource(id = R.color.md_theme_scrim),
            surfaceBright = colorResource(id = R.color.md_theme_surfaceBright),
            surfaceDim = colorResource(id = R.color.md_theme_surfaceDim),
            surfaceContainer = colorResource(id = R.color.md_theme_surfaceContainer),
            surfaceContainerHigh = colorResource(id = R.color.md_theme_surfaceContainerHigh),
            surfaceContainerHighest = colorResource(id = R.color.md_theme_surfaceContainerHighest),
            surfaceContainerLow = colorResource(id = R.color.md_theme_surfaceContainerLow),
            surfaceContainerLowest = colorResource(id = R.color.md_theme_surfaceContainerLowest)
        )

    MaterialTheme(colorScheme = appColorScheme, typography = Typography, content = content)
}
