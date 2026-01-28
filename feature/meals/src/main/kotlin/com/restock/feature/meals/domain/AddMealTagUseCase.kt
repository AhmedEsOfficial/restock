package com.restock.feature.meals.domain

import com.restock.core.domain.model.MealTag
import com.restock.core.domain.model.MealType
import com.restock.core.domain.repository.MealTagRepository
import javax.inject.Inject

/**
 * Use case for adding a meal tag to an item.
 *
 * When a tag is added:
 * - If the tag doesn't exist, creates a new tag with frequency 1
 * - If the tag already exists, increments the frequency count
 *
 * The frequency count is used for learning user preferences and
 * improving future tag suggestions.
 *
 * @property mealTagRepository Repository for managing meal tags.
 */
class AddMealTagUseCase
    @Inject
    constructor(
        private val mealTagRepository: MealTagRepository,
    ) {
        /**
         * Adds a meal tag to an item.
         *
         * @param itemId The ID of the item to tag.
         * @param mealType The meal type to associate with the item.
         * @return Result containing the created/updated MealTag, or an error.
         */
        suspend operator fun invoke(
            itemId: String,
            mealType: MealType,
        ): Result<MealTag> = mealTagRepository.addTag(itemId, mealType)
    }
