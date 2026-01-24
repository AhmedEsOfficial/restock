package com.restock.core.domain.usecase

import com.restock.core.domain.repository.CurrentListRepository
import javax.inject.Inject

/**
 * Use case for updating the quantity and unit of an item on the shopping list.
 *
 * Validates that quantity is at least 1 before updating.
 */
class UpdateQuantityUseCase
    @Inject
    constructor(
        private val currentListRepository: CurrentListRepository,
    ) {
        /**
         * Updates the quantity and optional unit of a list item.
         *
         * @param id The list item ID.
         * @param quantity The new quantity (must be at least 1).
         * @param unit Optional unit of measurement (null to clear).
         * @return Result indicating success or failure.
         */
        suspend operator fun invoke(
            id: String,
            quantity: Int,
            unit: String? = null,
        ): Result<Unit> {
            if (quantity < 1) {
                return Result.failure(IllegalArgumentException("Quantity must be at least 1"))
            }

            return currentListRepository.updateQuantity(id, quantity, unit)
        }
    }
