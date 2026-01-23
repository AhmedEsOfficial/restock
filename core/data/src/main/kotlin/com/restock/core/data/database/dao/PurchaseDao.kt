package com.restock.core.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
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
     * Get purchase count for an item.
     */
    @Query("SELECT COUNT(*) FROM purchases WHERE item_id = :itemId")
    suspend fun getPurchaseCountForItem(itemId: String): Int

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
