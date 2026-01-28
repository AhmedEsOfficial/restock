package com.restock.feature.suggestions.ui

/**
 * One-shot events emitted by the SuggestionsViewModel.
 *
 * Events are consumed exactly once and are used for transient UI effects
 * like snackbars, navigation, and notifications. They are not persisted
 * across configuration changes.
 */
sealed interface SuggestionsEvent {
    /**
     * A suggestion was accepted and the item was added to the list.
     *
     * @param itemName The name of the added item.
     */
    data class SuggestionAccepted(val itemName: String) : SuggestionsEvent

    /**
     * A suggestion was dismissed.
     *
     * @param itemName The name of the dismissed item.
     * @param isPersistent True if this was the third dismissal (permanently hidden).
     */
    data class SuggestionDismissed(
        val itemName: String,
        val isPersistent: Boolean = false,
    ) : SuggestionsEvent

    /**
     * An error occurred.
     *
     * @param message The error message to display.
     */
    data class Error(val message: String) : SuggestionsEvent
}
