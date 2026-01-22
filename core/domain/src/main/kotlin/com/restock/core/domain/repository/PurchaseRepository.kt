package com.restock.core.domain.repository

import com.restock.core.domain.model.Purchase
import kotlinx.coroutines.flow.Flow
import java.time.Instant

/**
 * Repository interface for purchase history.
 *
 * Implementations manage the record of all item purchases,
 * which feeds into the pattern learning system.
 */
interface PurchaseRepository {

    /**
     * Observe all purchases sorted by date descending.
     */
    fun observeAllPurchases(): Flow<List<Purchase>>

    /**
     * Observe purchases for a specific item.
     */
    fun observePurchasesForItem(itemId: String): Flow<List<Purchase>>

    /**
     * Get all purchases for an item.
     */
    suspend fun getPurchasesForItem(itemId: String): List<Purchase>

    /**
     * Get the last purchase for an item.
     */
    suspend fun getLastPurchaseForItem(itemId: String): Purchase?

    /**
     * Get purchase count for an item.
     */
    suspend fun getPurchaseCountForItem(itemId: String): Int

    /**
     * Get item IDs that have at least the minimum number of purchases.
     */
    suspend fun getItemsWithMinPurchases(minPurchases: Int): List<String>

    /**
     * Get purchases within a date range.
     */
    suspend fun getPurchasesInRange(start: Instant, end: Instant): List<Purchase>

    /**
     * Record a new purchase.
     *
     * Called when an item is checked off the list.
     */
    suspend fun recordPurchase(purchase: Purchase): Result<Unit>

    /**
     * Record multiple purchases.
     */
    suspend fun recordPurchases(purchases: List<Purchase>): Result<Unit>

    /**
     * Delete a purchase.
     */
    suspend fun deletePurchase(id: String): Result<Unit>

    /**
     * Delete all purchases for an item.
     */
    suspend fun deletePurchasesForItem(itemId: String): Result<Unit>
}
