package com.restock.core.ui.util

import androidx.compose.ui.graphics.Color
import com.restock.core.ui.components.MealType as UiMealType
import com.restock.core.ui.theme.RestockColors

/**
 * Data class holding color and display information for a meal type.
 *
 * @property name The uppercase enum name (e.g., "BREAKFAST")
 * @property displayName The human-readable name (e.g., "Breakfast")
 * @property emoji The emoji associated with this meal type
 * @property color The primary color for text/icons
 * @property containerColor The background/container color
 */
data class MealTypeColorInfo(
    val name: String,
    val displayName: String,
    val emoji: String,
    val color: Color,
    val containerColor: Color,
)

/**
 * Utility object for mapping meal type strings to UI colors and display information.
 *
 * This provides a bridge between domain meal types (represented as strings
 * or enums in the domain layer) and UI colors/display data, without creating
 * a direct dependency between UI and domain modules.
 *
 * **Meal Type Colors:**
 * - Breakfast: Amber (#F59E0B)
 * - Lunch: Emerald (#10B981)
 * - Dinner: Indigo (#6366F1)
 * - Snack: Pink (#EC4899)
 *
 * Usage:
 * ```kotlin
 * // Get color from domain meal type string
 * val color = MealTypeColorMapper.getColor(mealTag.mealType.name)
 * val containerColor = MealTypeColorMapper.getContainerColor("BREAKFAST")
 *
 * // Get all meal types for selection UIs
 * MealTypeColorMapper.allMealTypes.forEach { info ->
 *     Chip(
 *         text = "${info.emoji} ${info.displayName}",
 *         color = info.color
 *     )
 * }
 *
 * // Convert string to UI MealType enum
 * val uiMealType = MealTypeColorMapper.toUiMealType("DINNER")
 * ```
 */
object MealTypeColorMapper {
    /**
     * Gets all available meal types with their colors and display information.
     *
     * Useful for building meal tag selection UIs.
     */
    val allMealTypes: List<MealTypeColorInfo> =
        listOf(
            MealTypeColorInfo(
                name = "BREAKFAST",
                displayName = "Breakfast",
                emoji = "\uD83C\uDF05", // 🌅
                color = RestockColors.Breakfast,
                containerColor = RestockColors.BreakfastContainer,
            ),
            MealTypeColorInfo(
                name = "LUNCH",
                displayName = "Lunch",
                emoji = "\u2600\uFE0F", // ☀️
                color = RestockColors.Lunch,
                containerColor = RestockColors.LunchContainer,
            ),
            MealTypeColorInfo(
                name = "DINNER",
                displayName = "Dinner",
                emoji = "\uD83C\uDF19", // 🌙
                color = RestockColors.Dinner,
                containerColor = RestockColors.DinnerContainer,
            ),
            MealTypeColorInfo(
                name = "SNACK",
                displayName = "Snack",
                emoji = "\u2728", // ✨
                color = RestockColors.Snack,
                containerColor = RestockColors.SnackContainer,
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
     * Maps a meal type name to its emoji.
     *
     * @param mealTypeName The meal type name (case-insensitive)
     * @return The corresponding emoji, or empty string for unknown types
     */
    fun getEmoji(mealTypeName: String): String {
        return when (mealTypeName.uppercase()) {
            "BREAKFAST" -> "\uD83C\uDF05" // 🌅
            "LUNCH" -> "\u2600\uFE0F" // ☀️
            "DINNER" -> "\uD83C\uDF19" // 🌙
            "SNACK" -> "\u2728" // ✨
            else -> ""
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

    /**
     * Gets the full MealTypeColorInfo for a given meal type name.
     *
     * @param mealTypeName The meal type name (case-insensitive)
     * @return The corresponding [MealTypeColorInfo], or null for unknown types
     */
    fun getInfo(mealTypeName: String): MealTypeColorInfo? {
        return allMealTypes.find { it.name == mealTypeName.uppercase() }
    }
}
