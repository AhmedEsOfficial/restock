package com.restock.core.domain.model

/**
 * Domain model representing a smart suggestion.
 *
 * Suggestions are generated from patterns when an item
 * is predicted to be needed based on purchase history.
 */
data class Suggestion(
    val itemId: String,
    val itemName: String,
    val reason: String,
    val confidenceScore: Double,
    val daysSinceLastPurchase: Int,
    val expectedIntervalDays: Int,
    val urgencyScore: Double,
) {
    companion object {
        /**
         * Maximum number of suggestions to show at once.
         */
        const val MAX_SUGGESTIONS = 5
    }
}

/**
 * Calculate urgency score based on how overdue an item is.
 *
 * Urgency = (days_since_last - expected_interval + 1) / expected_interval
 * Clamped to [0, 2] range.
 */
fun calculateUrgencyScore(
    daysSinceLastPurchase: Int,
    expectedIntervalDays: Int,
): Double {
    if (expectedIntervalDays <= 0) return 0.0

    val urgency = (daysSinceLastPurchase - expectedIntervalDays + 1).toDouble() / expectedIntervalDays
    return urgency.coerceIn(0.0, 2.0)
}
