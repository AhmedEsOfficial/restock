package com.restock.core.domain.model

/**
 * Enum representing meal types for item categorization.
 */
enum class MealType(val displayName: String) {
    BREAKFAST("Breakfast"),
    LUNCH("Lunch"),
    DINNER("Dinner"),
    SNACK("Snack");

    companion object {
        fun fromString(value: String): MealType? {
            return entries.find { it.name.equals(value, ignoreCase = true) }
        }
    }
}

/**
 * Domain model representing a meal tag association.
 *
 * Links items to meal types and tracks frequency.
 */
data class MealTag(
    val id: String,
    val itemId: String,
    val mealType: MealType,
    val frequencyCount: Int = 1
)

/**
 * Item with its associated meal tags.
 */
data class ItemWithMealTags(
    val itemId: String,
    val itemName: String,
    val mealTypes: List<MealType>
)
