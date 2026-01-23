package com.restock.core.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Light color scheme for Restock using Material 3.
 */
private val LightColorScheme =
    lightColorScheme(
        primary = RestockColors.Primary,
        onPrimary = RestockColors.OnPrimary,
        primaryContainer = RestockColors.PrimaryContainer,
        onPrimaryContainer = RestockColors.OnPrimary,
        secondary = RestockColors.Accent,
        onSecondary = RestockColors.OnAccent,
        secondaryContainer = RestockColors.Accent.copy(alpha = 0.1f),
        onSecondaryContainer = RestockColors.Accent,
        tertiary = RestockColors.Success,
        onTertiary = RestockColors.OnSuccess,
        tertiaryContainer = RestockColors.Success.copy(alpha = 0.1f),
        onTertiaryContainer = RestockColors.Success,
        error = RestockColors.Error,
        onError = RestockColors.OnError,
        errorContainer = RestockColors.Error.copy(alpha = 0.1f),
        onErrorContainer = RestockColors.Error,
        background = RestockColors.Background,
        onBackground = RestockColors.OnBackground,
        surface = RestockColors.Surface,
        onSurface = RestockColors.OnSurface,
        surfaceVariant = RestockColors.Background,
        onSurfaceVariant = RestockColors.Muted,
        outline = RestockColors.MutedVariant,
        outlineVariant = RestockColors.MutedVariant.copy(alpha = 0.5f),
        scrim = RestockColors.Scrim,
    )

/**
 * Dark color scheme for Restock using Material 3.
 * Placeholder for future dark theme implementation.
 */
private val DarkColorScheme =
    darkColorScheme(
        primary = RestockDarkColors.Primary,
        onPrimary = RestockDarkColors.OnPrimary,
        primaryContainer = RestockDarkColors.PrimaryContainer,
        onPrimaryContainer = RestockDarkColors.OnPrimary,
        secondary = RestockDarkColors.Accent,
        onSecondary = RestockDarkColors.OnAccent,
        secondaryContainer = RestockDarkColors.Accent.copy(alpha = 0.2f),
        onSecondaryContainer = RestockDarkColors.Accent,
        tertiary = RestockDarkColors.Success,
        onTertiary = RestockDarkColors.OnSuccess,
        tertiaryContainer = RestockDarkColors.Success.copy(alpha = 0.2f),
        onTertiaryContainer = RestockDarkColors.Success,
        error = RestockDarkColors.Error,
        onError = RestockDarkColors.OnError,
        errorContainer = RestockDarkColors.Error.copy(alpha = 0.2f),
        onErrorContainer = RestockDarkColors.Error,
        background = RestockDarkColors.Background,
        onBackground = RestockDarkColors.OnBackground,
        surface = RestockDarkColors.Surface,
        onSurface = RestockDarkColors.OnSurface,
        surfaceVariant = RestockDarkColors.Surface,
        onSurfaceVariant = RestockDarkColors.Muted,
        outline = RestockDarkColors.MutedVariant,
        outlineVariant = RestockDarkColors.MutedVariant.copy(alpha = 0.5f),
        scrim = RestockDarkColors.Scrim,
    )

/**
 * Restock application theme.
 *
 * Wraps Material 3 theme with custom colors, typography, shapes, and spacing.
 *
 * @param darkTheme Whether to use dark theme. Defaults to system setting.
 * @param content The composable content to be themed.
 *
 * Usage:
 * ```kotlin
 * RestockTheme {
 *     Surface {
 *         // Your app content
 *     }
 * }
 * ```
 */
@Composable
fun RestockTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    // Update system UI colors
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(
        LocalSpacing provides Spacing(),
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = RestockTypography,
            shapes = RestockShapes,
            content = content,
        )
    }
}

/**
 * Object providing convenient access to theme values.
 *
 * Usage:
 * ```kotlin
 * val padding = RestockTheme.spacing.md
 * ```
 */
object RestockTheme {
    /**
     * Access spacing values from the current theme.
     */
    val spacing: Spacing
        @Composable
        @androidx.compose.runtime.ReadOnlyComposable
        get() = LocalSpacing.current
}
