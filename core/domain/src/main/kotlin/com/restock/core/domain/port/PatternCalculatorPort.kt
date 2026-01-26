package com.restock.core.domain.port

import com.restock.core.domain.model.Pattern
import com.restock.core.domain.model.Purchase

/**
 * Port interface for pattern calculation.
 *
 * This abstraction allows the domain layer to use pattern calculation
 * without directly depending on the ML module. Implementations should
 * analyze purchase history and produce confidence-scored patterns.
 *
 * Algorithm requirements:
 * - Minimum 3 purchases required to calculate a pattern
 * - Confidence score based on coefficient of variation (CV)
 * - Confidence = 1 / (1 + CV), where CV = σ / μ
 * - Same-day purchases should be filtered out (treat as one shopping trip)
 */
interface PatternCalculatorPort {
    /**
     * Calculates a pattern from purchase history.
     *
     * @param purchases List of purchases for the item (any order).
     * @param itemId The ID of the item for the pattern.
     * @return Calculated pattern or null if insufficient data.
     */
    fun calculate(
        purchases: List<Purchase>,
        itemId: String,
    ): Pattern?

    /**
     * Determines if an item should be suggested based on its pattern.
     *
     * @param pattern The calculated pattern for the item.
     * @param lastPurchase The most recent purchase of the item.
     * @return True if the item should be suggested to the user.
     */
    fun shouldSuggest(
        pattern: Pattern,
        lastPurchase: Purchase?,
    ): Boolean

    /**
     * Calculates priority score for ranking suggestions.
     *
     * Higher priority items should be shown first in the suggestions list.
     *
     * @param pattern The calculated pattern for the item.
     * @param daysSinceLastPurchase Number of days since the last purchase.
     * @return Priority score where higher values indicate higher priority.
     */
    fun calculatePriority(
        pattern: Pattern,
        daysSinceLastPurchase: Long,
    ): Double
}
