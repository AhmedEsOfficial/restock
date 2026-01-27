package com.restock.feature.list.ui

import com.restock.core.domain.model.CurrentListItem
import com.restock.core.domain.model.GroceryItem

/**
 * User actions for the list screen.
 *
 * Follows the UDF pattern - all user interactions are represented as
 * actions that flow through the ViewModel's onAction method.
 */
sealed interface ListAction {
    // Input actions
    /**
     * Update the text in the add item input field.
     * Triggers autocomplete suggestions.
     */
    data class UpdateInputText(val text: String) : ListAction

    /**
     * Add a new item to the list with the given name.
     */
    data class AddItem(val name: String) : ListAction

    /**
     * Clear the input text field.
     */
    data object ClearInput : ListAction

    // Item actions

    /**
     * Toggle the checked state of an item.
     *
     * @param id The list item ID.
     * @param isChecked The new checked state.
     */
    data class ToggleItem(
        val id: String,
        val isChecked: Boolean,
    ) : ListAction

    /**
     * Delete an item from the list.
     * Starts the 5-second undo timer.
     */
    data class DeleteItem(val id: String) : ListAction

    /**
     * Undo the last delete action.
     * Only valid within the undo window.
     */
    data object UndoDelete : ListAction

    // Reordering

    /**
     * Reorder an item from one position to another.
     *
     * @param fromIndex Original index in the unchecked items list.
     * @param toIndex Target index in the unchecked items list.
     */
    data class ReorderItems(
        val fromIndex: Int,
        val toIndex: Int,
    ) : ListAction

    // Quantity

    /**
     * Select an item for quantity adjustment.
     * Pass null to close the quantity sheet.
     */
    data class SelectItemForQuantity(val item: CurrentListItem?) : ListAction

    /**
     * Update the quantity and unit of an item.
     */
    data class UpdateQuantity(
        val id: String,
        val quantity: Int,
        val unit: String?,
    ) : ListAction

    // Search

    /**
     * Search/filter the list by query.
     * This is separate from input text for autocomplete.
     */
    data class SearchItems(val query: String) : ListAction

    /**
     * Clear the search query.
     */
    data object ClearSearch : ListAction

    /**
     * Select an autocomplete suggestion to add to the list.
     */
    data class SelectAutoComplete(val item: GroceryItem) : ListAction

    // Bulk operations

    /**
     * Clear all checked items from the list.
     */
    data object ClearCheckedItems : ListAction

    /**
     * Dismiss the current error.
     */
    data object DismissError : ListAction

    // Learning feedback

    /**
     * Toggle whether to show learning feedback animations.
     */
    data class SetLearningFeedback(val enabled: Boolean) : ListAction
}
