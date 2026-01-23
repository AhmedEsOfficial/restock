package com.restock.core.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.restock.core.data.database.entity.ItemEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for grocery items.
 */
@Dao
interface ItemDao {
    /**
     * Observe all active items sorted by name.
     */
    @Query("SELECT * FROM items WHERE is_active = 1 ORDER BY name ASC")
    fun observeActiveItems(): Flow<List<ItemEntity>>

    /**
     * Get all active items.
     */
    @Query("SELECT * FROM items WHERE is_active = 1 ORDER BY name ASC")
    suspend fun getActiveItems(): List<ItemEntity>

    /**
     * Get an item by its ID.
     */
    @Query("SELECT * FROM items WHERE id = :id")
    suspend fun getById(id: String): ItemEntity?

    /**
     * Find an item by its normalized name for deduplication.
     */
    @Query("SELECT * FROM items WHERE normalized_name = :normalizedName LIMIT 1")
    suspend fun findByNormalizedName(normalizedName: String): ItemEntity?

    /**
     * Search items by name prefix for autocomplete.
     */
    @Query("SELECT * FROM items WHERE is_active = 1 AND name LIKE :query || '%' ORDER BY name ASC LIMIT :limit")
    suspend fun searchByNamePrefix(
        query: String,
        limit: Int = 10,
    ): List<ItemEntity>

    /**
     * Search items by name containing query.
     */
    @Query("SELECT * FROM items WHERE is_active = 1 AND name LIKE '%' || :query || '%' ORDER BY name ASC LIMIT :limit")
    suspend fun searchByName(
        query: String,
        limit: Int = 20,
    ): List<ItemEntity>

    /**
     * Insert a new item.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: ItemEntity)

    /**
     * Insert multiple items.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<ItemEntity>)

    /**
     * Update an existing item.
     */
    @Update
    suspend fun update(item: ItemEntity)

    /**
     * Soft delete an item by marking it inactive.
     */
    @Query("UPDATE items SET is_active = 0 WHERE id = :id")
    suspend fun softDelete(id: String)

    /**
     * Hard delete an item.
     */
    @Query("DELETE FROM items WHERE id = :id")
    suspend fun hardDelete(id: String)

    /**
     * Get the count of all items.
     */
    @Query("SELECT COUNT(*) FROM items WHERE is_active = 1")
    suspend fun getCount(): Int
}
