package com.restock.feature.suggestions.ui

import com.restock.core.domain.model.Suggestion

/**
 * User actions for the suggestions section.
 *
 * Follows the UDF pattern - all user interactions are represented as
 * actions that flow through the ViewModel's onAction method.
 */
sealed interface SuggestionsAction {
    /**
     * Accept a suggestion, adding the item to the current list.
     *
     * @param suggestion The suggestion to accept.
     */
    data class AcceptSuggestion(val suggestion: Suggestion) : SuggestionsAction

    /**
     * Dismiss a suggestion.
     *
     * Effects:
     * - First/second dismiss: reduces pattern confidence
     * - Third dismiss: persistently dismisses (won't suggest again)
     *
     * @param itemId The ID of the item to dismiss.
     */
    data class DismissSuggestion(val itemId: String) : SuggestionsAction

    /**
     * Refresh the learning state.
     *
     * Used when returning to the suggestions screen or after significant
     * changes to determine if learning state has progressed.
     */
    data object RefreshLearningState : SuggestionsAction

    /**
     * Dismiss the current error.
     */
    data object DismissError : SuggestionsAction

    /**
     * Retry loading suggestions after an error.
     */
    data object Retry : SuggestionsAction
}
