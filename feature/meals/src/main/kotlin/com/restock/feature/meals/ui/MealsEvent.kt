package com.restock.feature.meals.ui

import com.restock.core.domain.model.MealType

/**
 * One-shot events emitted by the MealsViewModel.
 *
 * Events are consumed exactly once and are used for transient UI effects
 * like snackbars and notifications. They are not persisted across
 * configuration changes.
 */
sealed interface MealsEvent {
    /**
     * An item was checked off from the meals view.
     *
     * @param itemName The name of the checked item.
     */
    data class ItemChecked(val itemName: String) : MealsEvent

    /**
     * A meal tag was added to an item.
     *
     * @param itemName The name of the item.
     * @param mealType The meal type that was added.
     */
    data class TagAdded(
        val itemName: String,
        val mealType: MealType,
    ) : MealsEvent

    /**
     * A meal tag was removed from an item.
     *
     * @param itemName The name of the item.
     * @param mealType The meal type that was removed.
     */
    data class TagRemoved(
        val itemName: String,
        val mealType: MealType,
    ) : MealsEvent

    /**
     * An error occurred.
     *
     * @param message The error message to display.
     */
    data class Error(val message: String) : MealsEvent
}
