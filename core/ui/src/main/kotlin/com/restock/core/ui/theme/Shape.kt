package com.restock.core.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Restock shape system for consistent corner radius across the app.
 *
 * Corner radius scale:
 * - Extra Small: 4dp - Small elements like badges
 * - Small: 8dp - Buttons, chips
 * - Medium: 14dp - List item cards (per design spec)
 * - Large: 16dp - Suggestion cards, sheets
 * - Extra Large: 24dp - Modals, bottom sheets
 */
val RestockShapes =
    Shapes(
        extraSmall = RoundedCornerShape(4.dp),
        small = RoundedCornerShape(8.dp),
        // List item cards
        medium = RoundedCornerShape(14.dp),
        // Suggestion cards
        large = RoundedCornerShape(16.dp),
        // Bottom sheets, dialogs
        extraLarge = RoundedCornerShape(24.dp),
    )

/**
 * Additional shape constants for specific use cases.
 */
object RestockShapeTokens {
    val CardRadius = 14.dp
    val SuggestionCardRadius = 16.dp
    val InputFieldRadius = 16.dp
    val ChipRadius = 8.dp
    /** Button corner radius per design spec (12dp) */
    val ButtonRadius = 12.dp
    val BottomSheetRadius = 24.dp
    val DialogRadius = 24.dp
}

/**
 * Pre-defined button shape with 12dp corner radius.
 */
val RestockButtonShape = RoundedCornerShape(RestockShapeTokens.ButtonRadius)

/**
 * Corner radius constants for direct use in components.
 *
 * Use these when you need raw Dp values for corner radius
 * rather than complete Shape objects.
 *
 * Usage:
 * ```kotlin
 * Surface(
 *     shape = RoundedCornerShape(RestockCornerRadius.large)
 * )
 * ```
 */
object RestockCornerRadius {
    /** Extra small radius for badges and small elements (4dp) */
    val extraSmall = 4.dp
    /** Small radius for chips and compact elements (8dp) */
    val small = 8.dp
    /** Medium radius for list item cards (14dp) */
    val medium = 14.dp
    /** Large radius for suggestion cards, text fields (16dp) */
    val large = 16.dp
    /** Extra large radius for bottom sheets and dialogs (24dp) */
    val extraLarge = 24.dp
}
