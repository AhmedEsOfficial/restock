package com.restock.core.domain.repository

import com.restock.core.domain.model.CurrentListItem
import com.restock.core.domain.model.PositionUpdate
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for the current shopping list.
 *
 * Implementations manage the list of items currently being shopped for.
 */
interface CurrentListRepository {
    /**
     * Observe the current list with item details.
     */
    fun observeCurrentList(): Flow<List<CurrentListItem>>

    /**
     * Observe unchecked items in the list.
     */
    fun observeUncheckedItems(): Flow<List<CurrentListItem>>

    /**
     * Observe checked items in the list (in cart).
     */
    fun observeCheckedItems(): Flow<List<CurrentListItem>>

    /**
     * Get the current list.
     */
    suspend fun getCurrentList(): List<CurrentListItem>

    /**
     * Get a list item by ID.
     */
    suspend fun getById(id: String): CurrentListItem?

    /**
     * Check if an item is already in the list.
     */
    suspend fun isItemInList(itemId: String): Boolean

    /**
     * Find an unchecked list item by its item ID.
     *
     * Used to check if an item is already on the list before adding,
     * and to get the existing list item for quantity updates.
     *
     * @param itemId The ID of the grocery item to find.
     * @return The list item if found and unchecked, null otherwise.
     */
    suspend fun findByItemId(itemId: String): CurrentListItem?

    /**
     * Get the IDs of all items currently in the list.
     *
     * Used for filtering suggestions - items already on the list
     * should not be suggested.
     *
     * @return List of item IDs in the current list.
     */
    suspend fun getCurrentItemIds(): List<String>

    /**
     * Add an item to the list.
     *
     * @param itemId The ID of the item to add.
     * @param quantity Initial quantity (default 1).
     * @param unit Optional unit of measurement.
     * @return Result containing the new list item or an error.
     */
    suspend fun addItem(
        itemId: String,
        quantity: Int = 1,
        unit: String? = null,
    ): Result<CurrentListItem>

    /**
     * Add an item to the list at a specific position.
     *
     * Primarily used for undo operations to restore an item
     * to its original position.
     *
     * @param itemId The ID of the item to add.
     * @param position The position to insert at.
     * @param quantity Initial quantity (default 1).
     * @param unit Optional unit of measurement.
     * @return Result containing the new list item or an error.
     */
    suspend fun addItemAt(
        itemId: String,
        position: Int,
        quantity: Int = 1,
        unit: String? = null,
    ): Result<CurrentListItem>

    /**
     * Check an item (mark as purchased).
     */
    suspend fun checkItem(id: String): Result<Unit>

    /**
     * Uncheck an item.
     */
    suspend fun uncheckItem(id: String): Result<Unit>

    /**
     * Toggle the checked state of an item.
     */
    suspend fun toggleChecked(id: String): Result<Unit>

    /**
     * Update the quantity and unit of an item.
     *
     * @param id The list item ID.
     * @param quantity The new quantity (must be positive).
     * @param unit Optional unit of measurement (null to clear).
     */
    suspend fun updateQuantity(
        id: String,
        quantity: Int,
        unit: String? = null,
    ): Result<Unit>

    /**
     * Update the position of an item (for drag-and-drop reordering).
     */
    suspend fun updatePosition(
        id: String,
        newPosition: Int,
    ): Result<Unit>

    /**
     * Reorder items after drag-and-drop.
     *
     * @param itemId The ID of the moved item.
     * @param fromPosition Original position.
     * @param toPosition New position.
     */
    suspend fun reorderItems(
        itemId: String,
        fromPosition: Int,
        toPosition: Int,
    ): Result<Unit>

    /**
     * Batch update positions for multiple items.
     *
     * Used for efficient reordering when multiple items need
     * their positions updated simultaneously (e.g., after drag-drop).
     *
     * @param updates List of position updates to apply.
     */
    suspend fun updatePositions(updates: List<PositionUpdate>): Result<Unit>

    /**
     * Remove an item from the list.
     */
    suspend fun removeItem(id: String): Result<Unit>

    /**
     * Remove all checked items from the list.
     */
    suspend fun clearCheckedItems(): Result<Unit>

    /**
     * Clear the entire list.
     */
    suspend fun clearAll(): Result<Unit>

    /**
     * Get the count of items in the list.
     */
    suspend fun getCount(): Int

    /**
     * Get the count of unchecked items.
     */
    suspend fun getUncheckedCount(): Int
}
