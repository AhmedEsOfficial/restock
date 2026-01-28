package com.restock.feature.meals.domain

import com.restock.core.domain.model.CurrentListItem
import com.restock.core.domain.model.MealType
import com.restock.core.domain.repository.CurrentListRepository
import com.restock.core.domain.repository.MealTagRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * Use case for getting current list items grouped by meal type.
 *
 * Used for the Meals tab to display items organized by:
 * - Breakfast
 * - Lunch
 * - Dinner
 * - Snack
 * - Untagged (null key)
 *
 * Items with multiple tags appear in multiple groups.
 * Only unchecked items are included (checked items are "in cart").
 *
 * @property currentListRepository Repository for current list items.
 * @property mealTagRepository Repository for meal tag associations.
 */
class GetItemsByMealUseCase
    @Inject
    constructor(
        private val currentListRepository: CurrentListRepository,
        private val mealTagRepository: MealTagRepository,
    ) {
        /**
         * Returns a Flow of current list items grouped by meal type.
         *
         * The map has MealType keys for tagged items, and null for untagged items.
         * Items with multiple tags appear under each applicable meal type.
         *
         * @return Flow emitting a map of meal type to list items.
         */
        operator fun invoke(): Flow<Map<MealType?, List<CurrentListItem>>> =
            combine(
                currentListRepository.observeUncheckedItems(),
                mealTagRepository.observeItemsWithTags(),
            ) { uncheckedItems, itemsWithTags ->
                val tagMap = itemsWithTags.associate { it.item.itemId to it.mealTags }

                uncheckedItems
                    .flatMap { item ->
                        val tags = tagMap[item.itemId] ?: emptyList()
                        if (tags.isEmpty()) {
                            listOf(null to item)
                        } else {
                            tags.map { it to item }
                        }
                    }
                    .groupBy({ it.first }, { it.second })
            }
    }
