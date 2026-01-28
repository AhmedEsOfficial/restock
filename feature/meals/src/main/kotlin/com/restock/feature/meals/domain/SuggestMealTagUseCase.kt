package com.restock.feature.meals.domain

import com.restock.core.domain.model.MealType
import com.restock.core.domain.repository.MealTagRepository
import javax.inject.Inject

/**
 * Use case for suggesting a meal tag based on item name.
 *
 * Suggestions are based on:
 * 1. Keyword matching for common meal items (eggs -> breakfast, etc.)
 * 2. Historical patterns from similar items the user has tagged
 *
 * Historical suggestions require at least 3 occurrences to ensure
 * the pattern is well-established.
 *
 * @property mealTagRepository Repository for meal tag operations.
 */
class SuggestMealTagUseCase
    @Inject
    constructor(
        private val mealTagRepository: MealTagRepository,
    ) {
        /**
         * Suggests a meal tag for an item based on its name.
         *
         * @param itemName The name of the item to suggest a tag for.
         * @return The suggested MealType, or null if no suggestion is appropriate.
         */
        suspend operator fun invoke(itemName: String): MealType? = mealTagRepository.suggestTag(itemName)
    }
