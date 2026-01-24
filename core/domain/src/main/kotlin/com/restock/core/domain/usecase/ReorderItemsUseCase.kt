package com.restock.core.domain.usecase

import com.restock.core.domain.model.PositionUpdate
import com.restock.core.domain.repository.CurrentListRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.first

/**
 * Use case for reordering items in the shopping list via drag-and-drop.
 *
 * Only operates on unchecked items - checked items (in cart) maintain
 * their order by checkedAt timestamp.
 *
 * Uses batch position updates for efficiency and maintains ordering
 * integrity by shifting affected items appropriately.
 */
class ReorderItemsUseCase
    @Inject
    constructor(
        private val currentListRepository: CurrentListRepository,
    ) {
        /**
         * Reorders items after a drag-and-drop operation.
         *
         * @param fromIndex The original index of the moved item (within unchecked items).
         * @param toIndex The target index for the moved item (within unchecked items).
         * @return Result indicating success or failure.
         */
        suspend operator fun invoke(
            fromIndex: Int,
            toIndex: Int,
        ): Result<Unit> {
            // No-op if positions are the same
            if (fromIndex == toIndex) return Result.success(Unit)

            // Get current unchecked items sorted by position
            val currentList =
                currentListRepository.observeUncheckedItems()
                    .first()
                    .sortedBy { it.position }

            // Validate indices
            val validRange = currentList.indices
            if (fromIndex !in validRange || toIndex !in validRange) {
                return Result.failure(IndexOutOfBoundsException("Invalid reorder indices"))
            }

            // Calculate position updates
            val updates = mutableListOf<PositionUpdate>()
            val movingItem = currentList[fromIndex]

            if (fromIndex < toIndex) {
                // Moving down: shift items between fromIndex+1 and toIndex up
                for (i in fromIndex + 1..toIndex) {
                    updates.add(
                        PositionUpdate(
                            id = currentList[i].id,
                            newPosition = currentList[i - 1].position,
                        ),
                    )
                }
            } else {
                // Moving up: shift items between toIndex and fromIndex-1 down
                for (i in toIndex until fromIndex) {
                    updates.add(
                        PositionUpdate(
                            id = currentList[i].id,
                            newPosition = currentList[i + 1].position,
                        ),
                    )
                }
            }

            // Update moving item to target position
            updates.add(
                PositionUpdate(
                    id = movingItem.id,
                    newPosition = currentList[toIndex].position,
                ),
            )

            return currentListRepository.updatePositions(updates)
        }
    }
