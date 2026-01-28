package com.restock.feature.list.ui

import com.restock.core.domain.model.CurrentListItem
import com.restock.core.domain.model.GroceryItem
import com.restock.core.domain.model.LearningState
import com.restock.core.domain.model.Suggestion
import com.restock.core.domain.usecase.LearningStatus
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
 * @property suggestions Smart suggestions based on learned patterns.
 * @property learningState Current learning state for suggestions section.
 * @property learningStatus Current learning status for feedback UI.
 * @property showLearningFeedback Whether to show learning animations on checkbox.
 * @property showCelebration Whether to show the list completion celebration.
 * @property isPatternProcessing Whether pattern calculation is in progress.
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
    val suggestions: ImmutableList<Suggestion> = persistentListOf(),
    val learningState: LearningState = LearningState.NOT_STARTED,
    val learningStatus: LearningStatus = LearningStatus.EMPTY,
    val showLearningFeedback: Boolean = true,
    val showCelebration: Boolean = false,
    val isPatternProcessing: Boolean = false,
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

    /**
     * Number of completed shopping trips.
     */
    val shoppingTripCount: Int
        get() = learningStatus.shoppingTripCount

    /**
     * Whether to show the learning status banner during onboarding.
     */
    val showLearningBanner: Boolean
        get() = learningStatus.showLearningBanner

    /**
     * Whether there are smart suggestions available.
     */
    val hasSuggestions: Boolean
        get() = suggestions.isNotEmpty()

    /**
     * Number of smart suggestions available.
     */
    val suggestionCount: Int
        get() = suggestions.size

    /**
     * Whether the suggestions section should be visible.
     *
     * Shows when:
     * - There are suggestions to display, OR
     * - User is in learning state (to show progress indicator)
     */
    val showSuggestionsSection: Boolean
        get() = hasSuggestions || learningState != LearningState.READY

    /**
     * Whether to show the learning progress indicator.
     *
     * Shows when learning is in progress but no suggestions are ready yet.
     */
    val showLearningProgress: Boolean
        get() =
            !hasSuggestions && learningState != LearningState.READY &&
                learningState != LearningState.NOT_STARTED

    /**
     * Whether all items are checked (list is complete).
     * Only true when there are checked items and no unchecked items.
     */
    val isListComplete: Boolean
        get() = checkedItems.isNotEmpty() && uncheckedItems.isEmpty()
}
