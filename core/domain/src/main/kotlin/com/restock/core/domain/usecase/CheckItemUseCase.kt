package com.restock.core.domain.usecase

import com.restock.core.domain.repository.CurrentListRepository
import javax.inject.Inject

/**
 * Use case for checking/unchecking items on the shopping list.
 *
 * When an item is checked:
 * - It moves to the "In Cart" section
 * - The checkedAt timestamp is recorded
 *
 * When an item is unchecked:
 * - It moves back to the active shopping list
 * - The checkedAt timestamp is cleared
 *
 * Note: Purchase recording (for pattern learning) is handled separately
 * in Sprint 5-6 via RecordPurchaseUseCase triggered by the ViewModel.
 */
class CheckItemUseCase
    @Inject
    constructor(
        private val currentListRepository: CurrentListRepository,
    ) {
        /**
         * Toggles the checked state of an item.
         *
         * @param id The list item ID (not the grocery item ID).
         * @param isChecked The new checked state.
         * @return Result indicating success or failure.
         */
        suspend operator fun invoke(
            id: String,
            isChecked: Boolean,
        ): Result<Unit> {
            return if (isChecked) {
                currentListRepository.checkItem(id)
            } else {
                currentListRepository.uncheckItem(id)
            }
        }
    }
