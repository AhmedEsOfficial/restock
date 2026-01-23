package com.restock.core.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Restock elevation/shadow system following the design specification.
 *
 * Elevation levels:
 * - Level 0: None (flat elements)
 * - Level 1: Subtle (0 1px 3px rgba(0,0,0,0.05)) - List item cards
 * - Level 2: Medium (0 2px 8px rgba(0,0,0,0.08)) - Suggestion cards, inputs
 * - Level 3: Prominent (0 4px 16px rgba(0,0,0,0.12)) - Modals, bottom sheets
 *
 * In Material 3, these map to tonal elevation + shadow elevation.
 * The actual shadow rendering is handled by the Card component with these values.
 */
data class Elevation(
    /** No elevation - flat elements */
    val level0: Dp = 0.dp,
    /** Subtle elevation for list item cards */
    val level1: Dp = 1.dp,
    /** Medium elevation for suggestion cards and focused inputs */
    val level2: Dp = 4.dp,
    /** Prominent elevation for modals and bottom sheets */
    val level3: Dp = 8.dp,
    // Semantic elevation values
    /** Elevation for list item cards (maps to level1) */
    val listItemCard: Dp = 1.dp,
    /** Elevation for suggestion cards (maps to level2) */
    val suggestionCard: Dp = 4.dp,
    /** Elevation for input fields when focused */
    val inputField: Dp = 2.dp,
    /** Elevation for bottom sheets */
    val bottomSheet: Dp = 8.dp,
    /** Elevation for modal dialogs */
    val dialog: Dp = 8.dp,
    /** Elevation for snackbar */
    val snackbar: Dp = 6.dp,
    /** Elevation for bottom navigation */
    val bottomNavigation: Dp = 3.dp,
    /** Elevation for floating action button */
    val fab: Dp = 6.dp,
    /** Elevation for dragged items */
    val dragged: Dp = 8.dp,
)

/**
 * CompositionLocal for accessing elevation values within the theme.
 */
val LocalElevation = staticCompositionLocalOf { Elevation() }

/**
 * Convenience accessor for elevation from RestockTheme.
 *
 * Usage:
 * ```kotlin
 * Card(
 *     elevation = CardDefaults.cardElevation(
 *         defaultElevation = RestockElevation.listItemCard
 *     )
 * )
 * ```
 */
object RestockElevation {
    val level0: Dp
        @Composable @ReadOnlyComposable
        get() = LocalElevation.current.level0

    val level1: Dp
        @Composable @ReadOnlyComposable
        get() = LocalElevation.current.level1

    val level2: Dp
        @Composable @ReadOnlyComposable
        get() = LocalElevation.current.level2

    val level3: Dp
        @Composable @ReadOnlyComposable
        get() = LocalElevation.current.level3

    val listItemCard: Dp
        @Composable @ReadOnlyComposable
        get() = LocalElevation.current.listItemCard

    val suggestionCard: Dp
        @Composable @ReadOnlyComposable
        get() = LocalElevation.current.suggestionCard

    val inputField: Dp
        @Composable @ReadOnlyComposable
        get() = LocalElevation.current.inputField

    val bottomSheet: Dp
        @Composable @ReadOnlyComposable
        get() = LocalElevation.current.bottomSheet

    val dialog: Dp
        @Composable @ReadOnlyComposable
        get() = LocalElevation.current.dialog

    val snackbar: Dp
        @Composable @ReadOnlyComposable
        get() = LocalElevation.current.snackbar

    val bottomNavigation: Dp
        @Composable @ReadOnlyComposable
        get() = LocalElevation.current.bottomNavigation

    val fab: Dp
        @Composable @ReadOnlyComposable
        get() = LocalElevation.current.fab

    val dragged: Dp
        @Composable @ReadOnlyComposable
        get() = LocalElevation.current.dragged
}
