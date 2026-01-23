package com.restock.core.domain.repository

import com.restock.core.domain.model.GroceryItem
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for grocery items.
 *
 * Implementations provide access to the item catalog,
 * handling persistence and data transformations.
 */
interface ItemRepository {
    /**
     * Observe all active items sorted by name.
     */
    fun observeActiveItems(): Flow<List<GroceryItem>>

    /**
     * Get all active items.
     */
    suspend fun getActiveItems(): List<GroceryItem>

    /**
     * Get an item by its ID.
     */
    suspend fun getById(id: String): GroceryItem?

    /**
     * Find an item by its normalized name.
     */
    suspend fun findByNormalizedName(name: String): GroceryItem?

    /**
     * Search items by name prefix for autocomplete.
     */
    suspend fun searchByNamePrefix(
        query: String,
        limit: Int = 10,
    ): List<GroceryItem>

    /**
     * Search items by name containing query.
     */
    suspend fun searchByName(
        query: String,
        limit: Int = 20,
    ): List<GroceryItem>

    /**
     * Insert a new item.
     *
     * @return Result containing the inserted item or an error.
     */
    suspend fun insert(item: GroceryItem): Result<GroceryItem>

    /**
     * Update an existing item.
     */
    suspend fun update(item: GroceryItem): Result<Unit>

    /**
     * Soft delete an item (mark as inactive).
     */
    suspend fun softDelete(id: String): Result<Unit>

    /**
     * Hard delete an item (remove from database).
     */
    suspend fun hardDelete(id: String): Result<Unit>
}
