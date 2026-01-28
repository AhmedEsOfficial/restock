package com.restock.feature.meals.ui

import com.restock.core.domain.model.CurrentListItem
import com.restock.core.domain.model.MealType

/**
 * User actions for the Meals screen.
 *
 * Follows the UDF pattern - all user interactions are represented as
 * actions that flow through the ViewModel's onAction method.
 */
sealed interface MealsAction {
    // Item check/uncheck actions

    /**
     * Toggle the checked state of an item.
     * When checked, the item moves to "In Cart" and is removed from the meals view.
     *
     * @param id The list item ID.
     * @param isChecked The new checked state.
     */
    data class ToggleItem(
        val id: String,
        val isChecked: Boolean,
    ) : MealsAction

    // Tag selection actions

    /**
     * Open the tag selection sheet for an item.
     * Long-press on an item triggers this action.
     *
     * @param item The item to edit tags for.
     */
    data class SelectItemForTagging(val item: CurrentListItem) : MealsAction

    /**
     * Close the tag selection sheet without making changes.
     */
    data object DismissTagSelection : MealsAction

    /**
     * Toggle a meal tag for the selected item.
     * If the tag exists, remove it. If not, add it.
     *
     * @param mealType The meal type to toggle.
     */
    data class ToggleTag(val mealType: MealType) : MealsAction

    // Error handling

    /**
     * Dismiss the current error.
     */
    data object DismissError : MealsAction

    // Refresh

    /**
     * Refresh the meals data.
     */
    data object Refresh : MealsAction
}
