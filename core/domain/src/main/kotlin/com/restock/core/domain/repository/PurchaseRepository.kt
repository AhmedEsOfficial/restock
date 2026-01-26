package com.restock.core.domain.repository

import com.restock.core.domain.model.Purchase
import com.restock.core.domain.model.PurchaseEvent
import java.time.Instant
import kotlinx.coroutines.flow.Flow

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
     * Observe the latest purchase for each item.
     *
     * Returns a flow of purchases where each item appears only once,
     * with its most recent purchase. Useful for pattern calculation triggers.
     */
    fun observeLatestPurchases(): Flow<List<Purchase>>

    /**
     * Get all purchases for an item.
     */
    suspend fun getPurchasesForItem(itemId: String): List<Purchase>

    /**
     * Get the last purchase for an item.
     */
    suspend fun getLastPurchaseForItem(itemId: String): Purchase?

    /**
     * Get a recent purchase for an item within the specified time window.
     *
     * Used for debouncing to prevent duplicate purchases within a shopping trip.
     * If a purchase exists within the window, the quantity should be updated
     * instead of creating a new record.
     *
     * @param itemId The ID of the item to check.
     * @param withinHours The number of hours to look back (default 4).
     * @return The recent purchase if one exists within the window, null otherwise.
     */
    suspend fun getRecentPurchase(
        itemId: String,
        withinHours: Int = 4,
    ): Purchase?

    /**
     * Get purchase count for an item.
     */
    suspend fun getPurchaseCountForItem(itemId: String): Int

    /**
     * Get all item IDs that have at least one purchase.
     *
     * Used by pattern calculation workers to know which items to process.
     */
    suspend fun getItemIdsWithPurchases(): List<String>

    /**
     * Get item IDs that have at least the minimum number of purchases.
     */
    suspend fun getItemsWithMinPurchases(minPurchases: Int): List<String>

    /**
     * Get purchases within a date range.
     */
    suspend fun getPurchasesInRange(
        start: Instant,
        end: Instant,
    ): List<Purchase>

    /**
     * Record a new purchase.
     *
     * Called when an item is checked off the list.
     */
    suspend fun recordPurchase(purchase: Purchase): Result<Unit>

    /**
     * Record a purchase event with debounce logic.
     *
     * If a purchase for the same item exists within the debounce window
     * (default 4 hours), the quantity is added to the existing purchase
     * instead of creating a new record. This handles the case where
     * a user checks the same item multiple times during a shopping trip.
     *
     * @param event The purchase event to record.
     * @return Result containing the recorded or updated Purchase on success.
     */
    suspend fun recordPurchaseEvent(event: PurchaseEvent): Result<Purchase>

    /**
     * Record multiple purchases.
     */
    suspend fun recordPurchases(purchases: List<Purchase>): Result<Unit>

    /**
     * Update an existing purchase.
     *
     * Used when aggregating quantities within the debounce window.
     */
    suspend fun updatePurchase(purchase: Purchase): Result<Unit>

    /**
     * Delete a purchase.
     */
    suspend fun deletePurchase(id: String): Result<Unit>

    /**
     * Delete all purchases for an item.
     */
    suspend fun deletePurchasesForItem(itemId: String): Result<Unit>
}
