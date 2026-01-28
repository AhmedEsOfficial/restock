package com.restock.feature.list.ui

/**
 * One-shot events emitted by the ListViewModel.
 *
 * Events are consumed exactly once and are used for transient UI effects
 * like snackbars, navigation, and notifications. They are not persisted
 * across configuration changes.
 */
sealed interface ListEvent {
    /**
     * An item was successfully added to the list.
     *
     * @param name The name of the added item.
     */
    data class ItemAdded(val name: String) : ListEvent

    /**
     * An item was deleted from the list.
     *
     * @param name The name of the deleted item.
     * @param canUndo Whether the deletion can be undone (within the undo window).
     */
    data class ItemDeleted(
        val name: String,
        val canUndo: Boolean = true,
    ) : ListEvent

    /**
     * The undo action was successful.
     */
    data object UndoSuccessful : ListEvent

    /**
     * The undo window has expired.
     * The deleted item can no longer be restored.
     */
    data object UndoExpired : ListEvent

    /**
     * An error occurred.
     *
     * @param message The error message to display.
     */
    data class Error(val message: String) : ListEvent

    /**
     * All checked items were cleared from the list.
     */
    data object CheckedItemsCleared : ListEvent

    /**
     * The item quantity was updated successfully.
     */
    data object QuantityUpdated : ListEvent

    /**
     * All items have been checked off - trigger celebration.
     * This is emitted when the last unchecked item is checked.
     */
    data object ListCompleted : ListEvent

    /**
     * A suggestion was accepted and added to the list.
     *
     * @param itemName The name of the item that was added.
     */
    data class SuggestionAccepted(val itemName: String) : ListEvent
}
