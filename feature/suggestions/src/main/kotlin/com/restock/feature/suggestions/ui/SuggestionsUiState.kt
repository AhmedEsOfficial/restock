package com.restock.feature.suggestions.ui

import com.restock.core.domain.model.LearningState
import com.restock.core.domain.model.Suggestion
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/**
 * UI state for the suggestions section.
 *
 * Follows the UDF (Unidirectional Data Flow) pattern as the single source
 * of truth for the suggestions UI. Uses ImmutableList for Compose stability.
 *
 * @property suggestions List of smart suggestions to display.
 * @property learningState Current learning state for displaying progress.
 * @property isLoading Whether suggestions are currently loading.
 * @property error Error message to display, null if no error.
 */
data class SuggestionsUiState(
    val suggestions: ImmutableList<Suggestion> = persistentListOf(),
    val learningState: LearningState = LearningState.NOT_STARTED,
    val isLoading: Boolean = true,
    val error: String? = null,
) {
    /**
     * Whether there are suggestions to display.
     */
    val hasSuggestions: Boolean
        get() = suggestions.isNotEmpty()

    /**
     * Number of suggestions available.
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
}
