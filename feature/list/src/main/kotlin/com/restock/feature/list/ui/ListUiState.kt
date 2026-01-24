package com.restock.feature.list.ui

import com.restock.core.domain.model.CurrentListItem
import com.restock.core.domain.model.GroceryItem
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/**
 * UI state for the main list screen.
 *
 * Follows the UDF (Unidirectional Data Flow) pattern as the single source
 * of truth for the list screen UI. Uses ImmutableList for Compose stability.
 *
 * @property uncheckedItems Items still to be purchased, sorted by position.
 * @property checkedItems Items already in cart, sorted by checkedAt descending.
 * @property autoCompleteSuggestions Search suggestions from typing input.
 * @property searchQuery Current search/filter query.
 * @property inputText Current text in the add item input field.
 * @property isLoading Whether the list is currently loading.
 * @property error Error message to display, null if no error.
 * @property selectedItemForQuantity Item currently being edited for quantity.
 */
data class ListUiState(
    val uncheckedItems: ImmutableList<CurrentListItem> = persistentListOf(),
    val checkedItems: ImmutableList<CurrentListItem> = persistentListOf(),
    val autoCompleteSuggestions: ImmutableList<GroceryItem> = persistentListOf(),
    val searchQuery: String = "",
    val inputText: String = "",
    val isLoading: Boolean = true,
    val error: String? = null,
    val selectedItemForQuantity: CurrentListItem? = null,
) {
    /**
     * Whether the list is empty (no items at all).
     */
    val isEmpty: Boolean
        get() = uncheckedItems.isEmpty() && checkedItems.isEmpty()

    /**
     * Total count of all items in the list.
     */
    val totalItemCount: Int
        get() = uncheckedItems.size + checkedItems.size

    /**
     * Count of unchecked items (still to get).
     */
    val uncheckedCount: Int
        get() = uncheckedItems.size

    /**
     * Count of checked items (in cart).
     */
    val checkedCount: Int
        get() = checkedItems.size

    /**
     * Whether there are any checked items to clear.
     */
    val hasCheckedItems: Boolean
        get() = checkedItems.isNotEmpty()

    /**
     * Whether autocomplete suggestions should be shown.
     */
    val showAutoComplete: Boolean
        get() = inputText.isNotBlank() && autoCompleteSuggestions.isNotEmpty()
}
