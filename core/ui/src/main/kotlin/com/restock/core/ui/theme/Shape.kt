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
