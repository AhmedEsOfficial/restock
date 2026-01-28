package com.restock.core.domain.usecase

import com.restock.core.domain.model.CurrentListItem
import com.restock.core.domain.model.Suggestion
import com.restock.core.domain.repository.SuggestionRepository
import javax.inject.Inject

/**
 * Use case for accepting a suggestion.
 *
 * When a user accepts a suggestion:
 * 1. The item is added to the current shopping list via [AddItemUseCase]
 * 2. The acceptance is recorded in [SuggestionRepository] for analytics/learning
 *
 * This use case delegates to [AddItemUseCase] for list operations to maintain
 * consistency with the existing add flow (deduplication, quantity increment, etc.).
 */
class AcceptSuggestionUseCase
    @Inject
    constructor(
        private val addItemUseCase: AddItemUseCase,
        private val suggestionRepository: SuggestionRepository,
    ) {
        /**
         * Accepts a suggestion by adding the item to the current list.
         *
         * @param suggestion The suggestion to accept.
         * @return Result containing the added [CurrentListItem], or failure if the add fails.
         */
        suspend operator fun invoke(suggestion: Suggestion): Result<CurrentListItem> {
            // Add item to list using existing use case
            val result = addItemUseCase(suggestion.itemName)

            // Record acceptance for analytics/learning on success
            if (result.isSuccess) {
                suggestionRepository.acceptSuggestion(suggestion.itemId)
            }

            return result
        }
    }
