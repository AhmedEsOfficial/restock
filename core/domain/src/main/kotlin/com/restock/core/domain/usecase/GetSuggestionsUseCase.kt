package com.restock.core.domain.usecase

import com.restock.core.domain.model.Suggestion
import com.restock.core.domain.repository.SuggestionRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

/**
 * Use case for retrieving smart suggestions.
 *
 * Provides both reactive (Flow-based) and one-shot methods for getting suggestions.
 * Suggestions are:
 * - Generated from learned purchase patterns
 * - Filtered to exclude items already on the current list
 * - Filtered to exclude persistently dismissed items
 * - Sorted by score (confidence × urgency)
 * - Limited to [Suggestion.MAX_SUGGESTIONS] (5)
 *
 * @property suggestionRepository Repository for suggestion generation.
 */
class GetSuggestionsUseCase
    @Inject
    constructor(
        private val suggestionRepository: SuggestionRepository,
    ) {
        /**
         * Observes suggestions as a Flow.
         *
         * Updates automatically when:
         * - Patterns change
         * - Items are added to/removed from current list
         * - Suggestions are dismissed
         *
         * @return Flow of suggestions, limited to [Suggestion.MAX_SUGGESTIONS].
         */
        operator fun invoke(): Flow<List<Suggestion>> = suggestionRepository.observeSuggestions()

        /**
         * Gets suggestions once (non-reactive).
         *
         * Use this for one-shot queries where reactivity is not needed.
         *
         * @param limit Maximum number of suggestions to return.
         * @return List of suggestions, sorted by score descending.
         */
        suspend fun getOnce(limit: Int = Suggestion.MAX_SUGGESTIONS): List<Suggestion> =
            suggestionRepository.getSuggestions(limit)
    }
