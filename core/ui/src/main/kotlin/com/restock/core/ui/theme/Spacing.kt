package com.restock.core.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Restock spacing system using a 4dp base unit.
 *
 * Spacing scale:
 * - xs (4dp): Tight spacing, icon gaps
 * - sm (8dp): Between related elements
 * - md (16dp): Card padding, item gaps
 * - lg (24dp): Section spacing
 * - xl (32dp): Major section breaks
 *
 * Extended scale for specific use cases:
 * - xxs (2dp): Very tight spacing
 * - xxl (40dp): Large gaps
 * - xxxl (48dp): Screen edge margins
 */
data class Spacing(
    val xxs: Dp = 2.dp,
    val xs: Dp = 4.dp,
    val sm: Dp = 8.dp,
    val md: Dp = 16.dp,
    val lg: Dp = 24.dp,
    val xl: Dp = 32.dp,
    val xxl: Dp = 40.dp,
    val xxxl: Dp = 48.dp,
    // Semantic spacing
    val cardPadding: Dp = 16.dp,
    val cardPaddingHorizontal: Dp = 12.dp,
    val cardPaddingVertical: Dp = 14.dp,
    val listItemGap: Dp = 8.dp,
    val sectionGap: Dp = 24.dp,
    val screenPadding: Dp = 16.dp,
    val screenPaddingTop: Dp = 48.dp,
    // Component sizing
    val bottomNavHeight: Dp = 64.dp,
    val listItemMinHeight: Dp = 56.dp,
    val inputFieldHeight: Dp = 52.dp,
    val suggestionCardWidth: Dp = 140.dp,
    val buttonHeight: Dp = 48.dp,
    // Icon sizes
    val iconSizeSmall: Dp = 20.dp,
    val iconSize: Dp = 24.dp,
    val iconSizeLarge: Dp = 32.dp,
    // Touch targets (minimum 48dp for accessibility)
    val minTouchTarget: Dp = 48.dp,
    val checkboxSize: Dp = 24.dp,
    val checkboxTouchTarget: Dp = 48.dp,
    // Swipe threshold
    val swipeThreshold: Dp = 72.dp,
)

/**
 * CompositionLocal for accessing spacing values within the theme.
 */
val LocalSpacing = staticCompositionLocalOf { Spacing() }

/**
 * Convenience accessor for spacing from MaterialTheme.
 *
 * Usage:
 * ```
 * Box(modifier = Modifier.padding(RestockTheme.spacing.md))
 * ```
 */
object RestockSpacing {
    val xxs: Dp
        @Composable @ReadOnlyComposable
        get() = LocalSpacing.current.xxs
    val xs: Dp
        @Composable @ReadOnlyComposable
        get() = LocalSpacing.current.xs
    val sm: Dp
        @Composable @ReadOnlyComposable
        get() = LocalSpacing.current.sm
    val md: Dp
        @Composable @ReadOnlyComposable
        get() = LocalSpacing.current.md
    val lg: Dp
        @Composable @ReadOnlyComposable
        get() = LocalSpacing.current.lg
    val xl: Dp
        @Composable @ReadOnlyComposable
        get() = LocalSpacing.current.xl
    val xxl: Dp
        @Composable @ReadOnlyComposable
        get() = LocalSpacing.current.xxl
    val xxxl: Dp
        @Composable @ReadOnlyComposable
        get() = LocalSpacing.current.xxxl

    val cardPadding: Dp
        @Composable @ReadOnlyComposable
        get() = LocalSpacing.current.cardPadding
    val cardPaddingHorizontal: Dp
        @Composable @ReadOnlyComposable
        get() = LocalSpacing.current.cardPaddingHorizontal
    val cardPaddingVertical: Dp
        @Composable @ReadOnlyComposable
        get() = LocalSpacing.current.cardPaddingVertical
    val listItemGap: Dp
        @Composable @ReadOnlyComposable
        get() = LocalSpacing.current.listItemGap
    val sectionGap: Dp
        @Composable @ReadOnlyComposable
        get() = LocalSpacing.current.sectionGap
    val screenPadding: Dp
        @Composable @ReadOnlyComposable
        get() = LocalSpacing.current.screenPadding
    val screenPaddingTop: Dp
        @Composable @ReadOnlyComposable
        get() = LocalSpacing.current.screenPaddingTop

    // Component sizing
    val bottomNavHeight: Dp
        @Composable @ReadOnlyComposable
        get() = LocalSpacing.current.bottomNavHeight
    val listItemMinHeight: Dp
        @Composable @ReadOnlyComposable
        get() = LocalSpacing.current.listItemMinHeight
    val inputFieldHeight: Dp
        @Composable @ReadOnlyComposable
        get() = LocalSpacing.current.inputFieldHeight
    val suggestionCardWidth: Dp
        @Composable @ReadOnlyComposable
        get() = LocalSpacing.current.suggestionCardWidth
    val buttonHeight: Dp
        @Composable @ReadOnlyComposable
        get() = LocalSpacing.current.buttonHeight

    // Icon sizes
    val iconSizeSmall: Dp
        @Composable @ReadOnlyComposable
        get() = LocalSpacing.current.iconSizeSmall
    val iconSize: Dp
        @Composable @ReadOnlyComposable
        get() = LocalSpacing.current.iconSize
    val iconSizeLarge: Dp
        @Composable @ReadOnlyComposable
        get() = LocalSpacing.current.iconSizeLarge

    // Touch targets
    val minTouchTarget: Dp
        @Composable @ReadOnlyComposable
        get() = LocalSpacing.current.minTouchTarget
    val checkboxSize: Dp
        @Composable @ReadOnlyComposable
        get() = LocalSpacing.current.checkboxSize
    val checkboxTouchTarget: Dp
        @Composable @ReadOnlyComposable
        get() = LocalSpacing.current.checkboxTouchTarget

    // Swipe
    val swipeThreshold: Dp
        @Composable @ReadOnlyComposable
        get() = LocalSpacing.current.swipeThreshold
}
