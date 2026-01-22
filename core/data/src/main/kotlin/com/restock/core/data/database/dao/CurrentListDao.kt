package com.restock.core.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.restock.core.data.database.entity.CurrentListEntity
import com.restock.core.data.database.entity.ItemEntity
import kotlinx.coroutines.flow.Flow

/**
 * Combined view of current list item with its item details.
 */
data class CurrentListItemWithDetails(
    val currentListId: String,
    val itemId: String,
    val itemName: String,
    val normalizedName: String,
    val quantity: Int,
    val unit: String?,
    val addedAt: Long,
    val checkedAt: Long?,
    val position: Int,
    val isChecked: Boolean
)

/**
 * Data Access Object for the current shopping list.
 */
@Dao
interface CurrentListDao {

    /**
     * Observe the current list with item details, sorted by position.
     */
    @Query("""
        SELECT
            cl.id as currentListId,
            cl.item_id as itemId,
            i.name as itemName,
            i.normalized_name as normalizedName,
            cl.quantity,
            cl.unit,
            cl.added_at as addedAt,
            cl.checked_at as checkedAt,
            cl.position,
            cl.is_checked as isChecked
        FROM current_list cl
        INNER JOIN items i ON cl.item_id = i.id
        ORDER BY cl.is_checked ASC, cl.position ASC
    """)
    fun observeCurrentList(): Flow<List<CurrentListItemWithDetails>>

    /**
     * Observe unchecked items in the list.
     */
    @Query("""
        SELECT
            cl.id as currentListId,
            cl.item_id as itemId,
            i.name as itemName,
            i.normalized_name as normalizedName,
            cl.quantity,
            cl.unit,
            cl.added_at as addedAt,
            cl.checked_at as checkedAt,
            cl.position,
            cl.is_checked as isChecked
        FROM current_list cl
        INNER JOIN items i ON cl.item_id = i.id
        WHERE cl.is_checked = 0
        ORDER BY cl.position ASC
    """)
    fun observeUncheckedItems(): Flow<List<CurrentListItemWithDetails>>

    /**
     * Observe checked items in the list (in cart).
     */
    @Query("""
        SELECT
            cl.id as currentListId,
            cl.item_id as itemId,
            i.name as itemName,
            i.normalized_name as normalizedName,
            cl.quantity,
            cl.unit,
            cl.added_at as addedAt,
            cl.checked_at as checkedAt,
            cl.position,
            cl.is_checked as isChecked
        FROM current_list cl
        INNER JOIN items i ON cl.item_id = i.id
        WHERE cl.is_checked = 1
        ORDER BY cl.checked_at DESC
    """)
    fun observeCheckedItems(): Flow<List<CurrentListItemWithDetails>>

    /**
     * Get all current list entities.
     */
    @Query("SELECT * FROM current_list ORDER BY position ASC")
    suspend fun getAll(): List<CurrentListEntity>

    /**
     * Get a current list item by ID.
     */
    @Query("SELECT * FROM current_list WHERE id = :id")
    suspend fun getById(id: String): CurrentListEntity?

    /**
     * Check if an item is already in the current list.
     */
    @Query("SELECT * FROM current_list WHERE item_id = :itemId LIMIT 1")
    suspend fun findByItemId(itemId: String): CurrentListEntity?

    /**
     * Get the maximum position in the list.
     */
    @Query("SELECT COALESCE(MAX(position), -1) FROM current_list")
    suspend fun getMaxPosition(): Int

    /**
     * Insert a new item into the current list.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: CurrentListEntity)

    /**
     * Insert multiple items.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<CurrentListEntity>)

    /**
     * Update an item in the current list.
     */
    @Update
    suspend fun update(item: CurrentListEntity)

    /**
     * Mark an item as checked.
     */
    @Query("UPDATE current_list SET is_checked = 1, checked_at = :checkedAt WHERE id = :id")
    suspend fun checkItem(id: String, checkedAt: Long)

    /**
     * Mark an item as unchecked.
     */
    @Query("UPDATE current_list SET is_checked = 0, checked_at = NULL WHERE id = :id")
    suspend fun uncheckItem(id: String)

    /**
     * Update the quantity of an item.
     */
    @Query("UPDATE current_list SET quantity = :quantity WHERE id = :id")
    suspend fun updateQuantity(id: String, quantity: Int)

    /**
     * Update item position.
     */
    @Query("UPDATE current_list SET position = :position WHERE id = :id")
    suspend fun updatePosition(id: String, position: Int)

    /**
     * Delete an item from the current list.
     */
    @Query("DELETE FROM current_list WHERE id = :id")
    suspend fun delete(id: String)

    /**
     * Delete all checked items from the list.
     */
    @Query("DELETE FROM current_list WHERE is_checked = 1")
    suspend fun deleteCheckedItems()

    /**
     * Clear the entire list.
     */
    @Query("DELETE FROM current_list")
    suspend fun clearAll()

    /**
     * Get count of items in the list.
     */
    @Query("SELECT COUNT(*) FROM current_list")
    suspend fun getCount(): Int

    /**
     * Get count of unchecked items.
     */
    @Query("SELECT COUNT(*) FROM current_list WHERE is_checked = 0")
    suspend fun getUncheckedCount(): Int
}
