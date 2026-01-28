package com.restock.feature.meals.ui

import com.restock.core.domain.model.CurrentListItem
import com.restock.core.domain.model.MealType
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/**
 * UI state for the Meals screen.
 *
 * Follows the UDF (Unidirectional Data Flow) pattern as the single source
 * of truth for the meals screen UI. Uses ImmutableList for Compose stability.
 *
 * Items are grouped by meal type:
 * - Breakfast items
 * - Lunch items
 * - Dinner items
 * - Snack items
 * - Untagged items (no meal tag assigned)
 *
 * @property breakfastItems Items tagged with Breakfast.
 * @property lunchItems Items tagged with Lunch.
 * @property dinnerItems Items tagged with Dinner.
 * @property snackItems Items tagged with Snack.
 * @property untaggedItems Items without any meal tag.
 * @property isLoading Whether the list is currently loading.
 * @property error Error message to display, null if no error.
 * @property selectedItemForTagging Item currently being edited for tags.
 * @property selectedItemTags Current tags for the selected item.
 */
data class MealsUiState(
    val breakfastItems: ImmutableList<CurrentListItem> = persistentListOf(),
    val lunchItems: ImmutableList<CurrentListItem> = persistentListOf(),
    val dinnerItems: ImmutableList<CurrentListItem> = persistentListOf(),
    val snackItems: ImmutableList<CurrentListItem> = persistentListOf(),
    val untaggedItems: ImmutableList<CurrentListItem> = persistentListOf(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val selectedItemForTagging: CurrentListItem? = null,
    val selectedItemTags: ImmutableList<MealType> = persistentListOf(),
) {
    /**
     * Whether the meals list is empty (no items at all).
     */
    val isEmpty: Boolean
        get() =
            breakfastItems.isEmpty() &&
                lunchItems.isEmpty() &&
                dinnerItems.isEmpty() &&
                snackItems.isEmpty() &&
                untaggedItems.isEmpty()

    /**
     * Total count of all items across all meal categories.
     * Note: Items with multiple tags are counted once per category they appear in.
     */
    val totalItemCount: Int
        get() =
            breakfastItems.size +
                lunchItems.size +
                dinnerItems.size +
                snackItems.size +
                untaggedItems.size

    /**
     * Whether there are any breakfast items.
     */
    val hasBreakfastItems: Boolean
        get() = breakfastItems.isNotEmpty()

    /**
     * Whether there are any lunch items.
     */
    val hasLunchItems: Boolean
        get() = lunchItems.isNotEmpty()

    /**
     * Whether there are any dinner items.
     */
    val hasDinnerItems: Boolean
        get() = dinnerItems.isNotEmpty()

    /**
     * Whether there are any snack items.
     */
    val hasSnackItems: Boolean
        get() = snackItems.isNotEmpty()

    /**
     * Whether there are any untagged items.
     */
    val hasUntaggedItems: Boolean
        get() = untaggedItems.isNotEmpty()

    /**
     * Whether the tag selection sheet should be shown.
     */
    val showTagSelectionSheet: Boolean
        get() = selectedItemForTagging != null

    /**
     * Count of items in each category, useful for section headers.
     */
    val breakfastCount: Int get() = breakfastItems.size
    val lunchCount: Int get() = lunchItems.size
    val dinnerCount: Int get() = dinnerItems.size
    val snackCount: Int get() = snackItems.size
    val untaggedCount: Int get() = untaggedItems.size
}
