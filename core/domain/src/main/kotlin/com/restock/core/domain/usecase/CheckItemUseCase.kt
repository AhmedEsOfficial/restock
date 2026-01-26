package com.restock.core.domain.usecase

import com.restock.core.domain.repository.CurrentListRepository
import javax.inject.Inject

/**
 * Use case for checking/unchecking items on the shopping list.
 *
 * When an item is checked:
 * - It moves to the "In Cart" section
 * - The checkedAt timestamp is recorded
 * - A purchase event is recorded asynchronously for pattern learning
 *
 * When an item is unchecked:
 * - It moves back to the active shopping list
 * - The checkedAt timestamp is cleared
 * - No purchase is recorded (unchecking doesn't constitute a purchase)
 */
class CheckItemUseCase
    @Inject
    constructor(
        private val currentListRepository: CurrentListRepository,
        private val recordPurchaseUseCase: RecordPurchaseUseCase,
    ) {
        /**
         * Toggles the checked state of an item.
         *
         * When checking an item:
         * 1. Retrieves the item details (itemId and quantity)
         * 2. Marks the item as checked in the list
         * 3. Records the purchase asynchronously (fire-and-forget)
         *
         * The purchase recording is non-blocking and does not affect
         * the result of this operation.
         *
         * @param id The list item ID (not the grocery item ID).
         * @param isChecked The new checked state.
         * @return Result indicating success or failure of the check operation.
         */
        suspend operator fun invoke(
            id: String,
            isChecked: Boolean,
        ): Result<Unit> {
            return if (isChecked) {
                // Get item details before checking
                val listItem =
                    currentListRepository.getById(id)
                        ?: return Result.failure(IllegalArgumentException("Item not found: $id"))

                // Check the item
                currentListRepository.checkItem(id)
                    .onSuccess {
                        // Record purchase asynchronously (fire-and-forget)
                        // This doesn't block and errors are logged internally
                        recordPurchaseUseCase(
                            itemId = listItem.itemId,
                            quantity = listItem.quantity,
                        )
                    }
            } else {
                currentListRepository.uncheckItem(id)
            }
        }
    }
