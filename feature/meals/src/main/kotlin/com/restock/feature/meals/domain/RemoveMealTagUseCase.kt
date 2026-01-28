package com.restock.feature.meals.domain

import com.restock.core.domain.model.MealType
import com.restock.core.domain.repository.MealTagRepository
import javax.inject.Inject

/**
 * Use case for removing a meal tag from an item.
 *
 * Removes the association between an item and a meal type.
 * This is typically triggered when a user taps to deselect a tag
 * in the tag selection UI.
 *
 * @property mealTagRepository Repository for managing meal tags.
 */
class RemoveMealTagUseCase
    @Inject
    constructor(
        private val mealTagRepository: MealTagRepository,
    ) {
        /**
         * Removes a meal tag from an item.
         *
         * @param itemId The ID of the item to untag.
         * @param mealType The meal type to remove from the item.
         * @return Result indicating success or failure.
         */
        suspend operator fun invoke(
            itemId: String,
            mealType: MealType,
        ): Result<Unit> = mealTagRepository.removeTag(itemId, mealType)
    }
