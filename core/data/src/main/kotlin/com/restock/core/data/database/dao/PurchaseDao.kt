package com.restock.core.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.restock.core.data.database.entity.PurchaseEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for purchase history.
 */
@Dao
interface PurchaseDao {
    /**
     * Observe all purchases sorted by date descending.
     */
    @Query("SELECT * FROM purchases ORDER BY purchased_at DESC")
    fun observeAllPurchases(): Flow<List<PurchaseEntity>>

    /**
     * Observe purchases for a specific item.
     */
    @Query("SELECT * FROM purchases WHERE item_id = :itemId ORDER BY purchased_at DESC")
    fun observePurchasesForItem(itemId: String): Flow<List<PurchaseEntity>>

    /**
     * Observe the latest purchase for each item.
     *
     * Returns the most recent purchase for each unique item_id.
     * Useful for pattern calculation triggers.
     */
    @Query(
        """
        SELECT p.* FROM purchases p
        INNER JOIN (
            SELECT item_id, MAX(purchased_at) as max_time
            FROM purchases
            GROUP BY item_id
        ) latest ON p.item_id = latest.item_id AND p.purchased_at = latest.max_time
        ORDER BY p.purchased_at DESC
    """,
    )
    fun observeLatestPurchases(): Flow<List<PurchaseEntity>>

    /**
     * Get all purchases for an item.
     */
    @Query("SELECT * FROM purchases WHERE item_id = :itemId ORDER BY purchased_at ASC")
    suspend fun getPurchasesForItem(itemId: String): List<PurchaseEntity>

    /**
     * Get the last purchase for an item.
     */
    @Query("SELECT * FROM purchases WHERE item_id = :itemId ORDER BY purchased_at DESC LIMIT 1")
    suspend fun getLastPurchaseForItem(itemId: String): PurchaseEntity?

    /**
     * Get a recent purchase for an item within the specified time window.
     *
     * Used for debouncing to prevent duplicate purchases within a shopping trip.
     *
     * @param itemId The ID of the item to check.
     * @param afterTimestamp The timestamp to look back from (exclusive lower bound).
     * @return The most recent purchase if one exists after the timestamp, null otherwise.
     */
    @Query(
        """
        SELECT * FROM purchases
        WHERE item_id = :itemId AND purchased_at > :afterTimestamp
        ORDER BY purchased_at DESC
        LIMIT 1
    """,
    )
    suspend fun getRecentPurchase(
        itemId: String,
        afterTimestamp: Long,
    ): PurchaseEntity?

    /**
     * Get purchase count for an item.
     */
    @Query("SELECT COUNT(*) FROM purchases WHERE item_id = :itemId")
    suspend fun getPurchaseCountForItem(itemId: String): Int

    /**
     * Get all distinct item IDs that have at least one purchase.
     *
     * Used by pattern calculation workers to know which items to process.
     */
    @Query("SELECT DISTINCT item_id FROM purchases")
    suspend fun getItemIdsWithPurchases(): List<String>

    /**
     * Get items with minimum number of purchases (for pattern calculation).
     */
    @Query(
        """
        SELECT item_id FROM purchases
        GROUP BY item_id
        HAVING COUNT(*) >= :minPurchases
    """,
    )
    suspend fun getItemsWithMinPurchases(minPurchases: Int): List<String>

    /**
     * Get purchases within a date range.
     */
    @Query("SELECT * FROM purchases WHERE purchased_at BETWEEN :startTime AND :endTime ORDER BY purchased_at DESC")
    suspend fun getPurchasesInRange(
        startTime: Long,
        endTime: Long,
    ): List<PurchaseEntity>

    /**
     * Insert a new purchase.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(purchase: PurchaseEntity)

    /**
     * Insert multiple purchases.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(purchases: List<PurchaseEntity>)

    /**
     * Update an existing purchase.
     *
     * Used when aggregating quantities within the debounce window.
     */
    @Update
    suspend fun update(purchase: PurchaseEntity)

    /**
     * Delete a purchase by ID.
     */
    @Query("DELETE FROM purchases WHERE id = :id")
    suspend fun delete(id: String)

    /**
     * Delete all purchases for an item.
     */
    @Query("DELETE FROM purchases WHERE item_id = :itemId")
    suspend fun deleteForItem(itemId: String)

    /**
     * Get total purchase count.
     */
    @Query("SELECT COUNT(*) FROM purchases")
    suspend fun getTotalCount(): Int
}
