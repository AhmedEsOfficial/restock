package com.restock.core.domain.usecase

import com.restock.core.domain.repository.SuggestionRepository
import javax.inject.Inject

/**
 * Use case for getting the shopping trip count.
 *
 * A shopping trip is defined as a distinct day with at least one purchase.
 * This count is used to determine the learning state:
 * - 0 trips: NOT_STARTED - No purchase history
 * - 1 trip: LEARNING - Building pattern data
 * - 2 trips: ALMOST_READY - Close to having suggestions
 * - 3+ trips: READY - Suggestions can appear
 *
 * @property suggestionRepository Repository for querying shopping trip count.
 */
class GetShoppingTripCountUseCase
    @Inject
    constructor(
        private val suggestionRepository: SuggestionRepository,
    ) {
        /**
         * Returns the number of shopping trips (distinct days with purchases).
         *
         * @return Number of distinct days with at least one purchase.
         */
        suspend operator fun invoke(): Int = suggestionRepository.getShoppingTripCount()
    }
