package com.restock.core.domain.usecase

import com.restock.core.domain.model.CurrentListItem
import com.restock.core.domain.repository.CurrentListRepository
import javax.inject.Inject

/**
 * Represents a deleted item with all information needed for undo.
 *
 * This data class stores the complete state of a deleted list item,
 * allowing it to be restored to its exact previous position and state.
 */
data class DeletedItem(
    val listItemId: String,
    val itemId: String,
    val name: String,
    val quantity: Int,
    val unit: String?,
    val position: Int,
)

/**
 * Use case for deleting items from the shopping list.
 *
 * Returns a DeletedItem that can be used for undo functionality.
 * The undo operation restores the item at its original position.
 */
class DeleteItemUseCase
    @Inject
    constructor(
        private val currentListRepository: CurrentListRepository,
    ) {
        /**
         * Deletes an item from the current list.
         *
         * @param id The list item ID to delete.
         * @return Result containing DeletedItem (for undo) or an error.
         */
        suspend operator fun invoke(id: String): Result<DeletedItem> {
            // Get the item first to store for undo
            val item =
                currentListRepository.getById(id)
                    ?: return Result.failure(IllegalArgumentException("Item not found"))

            // Delete the item
            return currentListRepository.removeItem(id).map {
                DeletedItem(
                    listItemId = item.id,
                    itemId = item.itemId,
                    name = item.name,
                    quantity = item.quantity,
                    unit = item.unit,
                    position = item.position,
                )
            }
        }

        /**
         * Restores a previously deleted item.
         *
         * Called when user taps "Undo" on the snackbar within the undo window.
         *
         * @param deletedItem The item data to restore.
         * @return Result indicating success or failure.
         */
        suspend fun undoDelete(deletedItem: DeletedItem): Result<CurrentListItem> {
            return currentListRepository.addItemAt(
                itemId = deletedItem.itemId,
                position = deletedItem.position,
                quantity = deletedItem.quantity,
                unit = deletedItem.unit,
            )
        }
    }
