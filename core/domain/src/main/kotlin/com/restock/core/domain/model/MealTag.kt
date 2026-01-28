package com.restock.core.domain.model

/**
 * Enum representing meal types for item categorization.
 *
 * Each meal type has a display name and emoji for UI presentation.
 */
enum class MealType(val displayName: String, val emoji: String) {
    BREAKFAST("Breakfast", "🌅"),
    LUNCH("Lunch", "☀️"),
    DINNER("Dinner", "🌙"),
    SNACK("Snack", "✨"),
    ;

    companion object {
        /**
         * Parses a string to a MealType (case-insensitive).
         *
         * @param value The string representation of the meal type
         * @return The matching MealType or null if not found
         */
        fun fromString(value: String): MealType? {
            return entries.find { it.name.equals(value, ignoreCase = true) }
        }
    }
}

/**
 * Domain model representing a meal tag association.
 *
 * Links items to meal types and tracks frequency for learning.
 * The frequencyCount increases each time the same tag is applied,
 * enabling the system to learn user preferences over time.
 */
data class MealTag(
    val id: String,
    val itemId: String,
    val mealType: MealType,
    val frequencyCount: Int = 1,
)

/**
 * Combines a CurrentListItem with its associated meal tags.
 *
 * Used for displaying items in the Meals tab grouped by meal type.
 * An item can have multiple meal tags and appear in multiple groups.
 */
data class ItemWithMealTags(
    val item: CurrentListItem,
    val mealTags: List<MealType>,
)
