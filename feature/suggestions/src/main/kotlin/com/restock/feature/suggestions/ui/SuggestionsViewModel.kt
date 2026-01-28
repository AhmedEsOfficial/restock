package com.restock.feature.suggestions.ui

import androidx.lifecycle.viewModelScope
import com.restock.core.domain.model.LearningState
import com.restock.core.domain.model.Suggestion
import com.restock.core.domain.usecase.AcceptSuggestionUseCase
import com.restock.core.domain.usecase.DismissSuggestionUseCase
import com.restock.core.domain.usecase.GetShoppingTripCountUseCase
import com.restock.core.domain.usecase.GetSuggestionsUseCase
import com.restock.core.ui.viewmodel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

/**
 * ViewModel for the suggestions section.
 *
 * Follows the UDF (Unidirectional Data Flow) pattern by extending BaseViewModel.
 * Manages suggestion display, acceptance, and dismissal with optimistic UI updates.
 *
 * Key features:
 * - Reactive suggestion observation with automatic filtering
 * - Optimistic UI updates for accept/dismiss (instant feedback)
 * - Learning state management based on shopping trip count
 * - Error recovery with state restoration
 */
@HiltViewModel
class SuggestionsViewModel
    @Inject
    constructor(
        private val getSuggestionsUseCase: GetSuggestionsUseCase,
        private val acceptSuggestionUseCase: AcceptSuggestionUseCase,
        private val dismissSuggestionUseCase: DismissSuggestionUseCase,
        private val getShoppingTripCountUseCase: GetShoppingTripCountUseCase,
    ) : BaseViewModel<SuggestionsUiState, SuggestionsAction, SuggestionsEvent>(
            initialState = SuggestionsUiState(),
        ) {
        init {
            observeSuggestions()
            loadLearningState()
        }

        /**
         * Observes suggestions and updates UI state reactively.
         *
         * The suggestions flow automatically updates when:
         * - Patterns change (e.g., after pattern calculation)
         * - Items are added to/removed from current list
         * - Suggestions are dismissed
         */
        private fun observeSuggestions() {
            viewModelScope.launch {
                getSuggestionsUseCase()
                    .catch { e ->
                        updateState {
                            copy(
                                isLoading = false,
                                error = e.message ?: ERROR_LOAD_SUGGESTIONS,
                            )
                        }
                        sendEvent(SuggestionsEvent.Error(e.message ?: ERROR_LOAD_SUGGESTIONS))
                    }
                    .collect { suggestions ->
                        updateState {
                            copy(
                                suggestions = suggestions.toImmutableList(),
                                isLoading = false,
                                error = null,
                            )
                        }
                    }
            }
        }

        /**
         * Loads the learning state based on shopping trip count.
         *
         * Determines what to show when there are no suggestions:
         * - 0 trips: NOT_STARTED - hide suggestions section
         * - 1 trip: LEARNING - show "Learning your patterns..."
         * - 2 trips: ALMOST_READY - show "Almost ready..."
         * - 3+ trips: READY - suggestions can appear
         */
        private fun loadLearningState() {
            viewModelScope.launch {
                try {
                    val tripCount = getShoppingTripCountUseCase()
                    val learningState = LearningState.fromTripCount(tripCount)
                    updateState { copy(learningState = learningState) }
                } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
                    // Non-critical error - don't show to user, just default to NOT_STARTED
                    // Learning state failure should not affect core functionality
                }
            }
        }

        override fun onAction(action: SuggestionsAction) {
            when (action) {
                is SuggestionsAction.AcceptSuggestion -> acceptSuggestion(action.suggestion)
                is SuggestionsAction.DismissSuggestion -> dismissSuggestion(action.itemId)
                is SuggestionsAction.RefreshLearningState -> loadLearningState()
                is SuggestionsAction.DismissError -> dismissError()
                is SuggestionsAction.Retry -> retry()
            }
        }

        /**
         * Accepts a suggestion by adding the item to the current list.
         *
         * Uses optimistic UI updates for instant feedback:
         * 1. Immediately removes suggestion from UI
         * 2. Calls use case to add item to list
         * 3. On failure: restores state and shows error
         */
        private fun acceptSuggestion(suggestion: Suggestion) {
            // Store original suggestions for potential restoration
            val originalSuggestions = currentState.suggestions

            viewModelScope.launch {
                // Optimistically remove from UI for instant feedback
                updateState {
                    copy(
                        suggestions =
                            suggestions.filter { it.itemId != suggestion.itemId }
                                .toImmutableList(),
                    )
                }

                acceptSuggestionUseCase(suggestion)
                    .onSuccess {
                        sendEvent(SuggestionsEvent.SuggestionAccepted(suggestion.itemName))
                    }
                    .onFailure { e ->
                        // Restore original state on error
                        updateState { copy(suggestions = originalSuggestions) }
                        sendEvent(SuggestionsEvent.Error(e.message ?: ERROR_ADD_ITEM))
                    }
            }
        }

        /**
         * Dismisses a suggestion.
         *
         * Uses optimistic UI updates for instant feedback:
         * 1. Immediately removes suggestion from UI
         * 2. Calls use case to record dismissal
         *
         * Note: We don't restore on failure since dismissals are fire-and-forget.
         * The repository handles confidence reduction and persistent dismissal tracking.
         */
        private fun dismissSuggestion(itemId: String) {
            // Find the suggestion for the event
            val suggestion = currentState.suggestions.find { it.itemId == itemId }

            viewModelScope.launch {
                // Optimistically remove from UI for instant feedback
                updateState {
                    copy(
                        suggestions =
                            suggestions.filter { it.itemId != itemId }
                                .toImmutableList(),
                    )
                }

                // Record dismissal (fire-and-forget)
                dismissSuggestionUseCase(itemId)

                // Send event for UI feedback (snackbar, haptic, etc.)
                suggestion?.let {
                    sendEvent(SuggestionsEvent.SuggestionDismissed(it.itemName))
                }
            }
        }

        /**
         * Dismisses the current error.
         */
        private fun dismissError() {
            updateState { copy(error = null) }
        }

        /**
         * Retries loading suggestions after an error.
         */
        private fun retry() {
            updateState {
                copy(
                    isLoading = true,
                    error = null,
                    suggestions = persistentListOf(),
                )
            }
            observeSuggestions()
            loadLearningState()
        }

        companion object {
            // Error messages
            private const val ERROR_LOAD_SUGGESTIONS = "Failed to load suggestions"
            private const val ERROR_ADD_ITEM = "Failed to add item"
        }
    }
