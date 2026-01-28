package com.restock.core.domain.usecase

import com.restock.core.domain.repository.SuggestionRepository
import javax.inject.Inject

/**
 * Use case for dismissing a suggestion.
 *
 * When a user dismisses a suggestion:
 * 1. First dismiss: Reduces pattern confidence by 0.05
 * 2. Second dismiss: Reduces pattern confidence by 0.05
 * 3. Third dismiss: Marks as persistently dismissed (won't suggest again)
 *
 * The dismissal state is reset when the user purchases the item,
 * allowing it to be suggested again based on new patterns.
 *
 * @property suggestionRepository Repository handling dismissal logic.
 */
class DismissSuggestionUseCase
    @Inject
    constructor(
        private val suggestionRepository: SuggestionRepository,
    ) {
        /**
         * Dismisses a suggestion.
         *
         * Effects:
         * - First dismiss: reduces confidence by 0.05
         * - Second dismiss: reduces confidence by 0.05
         * - Third dismiss: marks as persistently dismissed (won't suggest again)
         *
         * @param itemId The ID of the item to dismiss.
         */
        suspend operator fun invoke(itemId: String) {
            suggestionRepository.dismissSuggestion(itemId)
        }
    }
