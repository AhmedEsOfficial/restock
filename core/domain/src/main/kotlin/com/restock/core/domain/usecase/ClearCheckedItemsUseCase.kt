package com.restock.core.domain.usecase

import com.restock.core.domain.repository.CurrentListRepository
import javax.inject.Inject

/**
 * Use case for clearing all checked items from the shopping list.
 *
 * This removes all items that have been marked as purchased (checked)
 * from the current list, effectively resetting the "In Cart" section.
 */
class ClearCheckedItemsUseCase
    @Inject
    constructor(
        private val currentListRepository: CurrentListRepository,
    ) {
        /**
         * Clears all checked items from the current list.
         *
         * @return Result indicating success or failure.
         */
        suspend operator fun invoke(): Result<Unit> = currentListRepository.clearCheckedItems()
    }
