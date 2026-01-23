package com.restock.core.ui.util

import androidx.compose.ui.graphics.Color
import com.restock.core.ui.theme.RestockColors
import com.restock.core.ui.components.MealType as UiMealType

/**
 * Data class holding color information for a meal type.
 */
data class MealTypeColorInfo(
    val name: String,
    val displayName: String,
    val color: Color,
    val containerColor: Color,
)

/**
 * Utility object for mapping meal type strings to UI colors.
 *
 * This provides a bridge between domain meal types (represented as strings
 * or enums in the domain layer) and UI colors, without creating a direct
 * dependency between UI and domain modules.
 *
 * Usage:
 * ```kotlin
 * val color = MealTypeColorMapper.getColor(mealTag.mealType.name)
 * val containerColor = MealTypeColorMapper.getContainerColor("BREAKFAST")
 * ```
 */
object MealTypeColorMapper {
    /**
     * Gets all available meal types with their colors.
     *
     * Useful for building meal tag selection UIs.
     */
    val allMealTypes: List<MealTypeColorInfo> =
        listOf(
            MealTypeColorInfo(
                "BREAKFAST",
                "Breakfast",
                RestockColors.Breakfast,
                RestockColors.BreakfastContainer,
            ),
            MealTypeColorInfo(
                "LUNCH",
                "Lunch",
                RestockColors.Lunch,
                RestockColors.LunchContainer,
            ),
            MealTypeColorInfo(
                "DINNER",
                "Dinner",
                RestockColors.Dinner,
                RestockColors.DinnerContainer,
            ),
            MealTypeColorInfo(
                "SNACK",
                "Snack",
                RestockColors.Snack,
                RestockColors.SnackContainer,
            ),
        )

    /**
     * Maps a meal type name to its primary color.
     *
     * @param mealTypeName The meal type name (case-insensitive): BREAKFAST, LUNCH, DINNER, or SNACK
     * @return The corresponding color, or [RestockColors.Muted] for unknown types
     */
    fun getColor(mealTypeName: String): Color {
        return when (mealTypeName.uppercase()) {
            "BREAKFAST" -> RestockColors.Breakfast
            "LUNCH" -> RestockColors.Lunch
            "DINNER" -> RestockColors.Dinner
            "SNACK" -> RestockColors.Snack
            else -> RestockColors.Muted
        }
    }

    /**
     * Maps a meal type name to its container (background) color.
     *
     * @param mealTypeName The meal type name (case-insensitive)
     * @return The corresponding container color, or transparent for unknown types
     */
    fun getContainerColor(mealTypeName: String): Color {
        return when (mealTypeName.uppercase()) {
            "BREAKFAST" -> RestockColors.BreakfastContainer
            "LUNCH" -> RestockColors.LunchContainer
            "DINNER" -> RestockColors.DinnerContainer
            "SNACK" -> RestockColors.SnackContainer
            else -> Color.Transparent
        }
    }

    /**
     * Maps a meal type name to the corresponding UI MealType enum.
     *
     * @param mealTypeName The meal type name (case-insensitive)
     * @return The corresponding [UiMealType], or null for unknown types
     */
    fun toUiMealType(mealTypeName: String): UiMealType? {
        return when (mealTypeName.uppercase()) {
            "BREAKFAST" -> UiMealType.BREAKFAST
            "LUNCH" -> UiMealType.LUNCH
            "DINNER" -> UiMealType.DINNER
            "SNACK" -> UiMealType.SNACK
            else -> null
        }
    }
}
