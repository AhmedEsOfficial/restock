package com.restock.core.domain.model

/**
 * Domain model representing a smart suggestion for the user.
 *
 * Suggestions are generated from learned purchase patterns when:
 * - Pattern confidence >= 0.6
 * - Days since last purchase >= avg_interval - 1
 * - Item is not already on current list
 * - Item is not persistently dismissed
 *
 * @property itemId Unique identifier for the item being suggested.
 * @property itemName Display name of the item.
 * @property reason Human-readable reason for the suggestion (e.g., "Usually every 7 days").
 * @property score Combined score for prioritization (confidence × (1 + urgency_factor)).
 * @property confidence Pattern confidence score (0.0 to 1.0).
 * @property daysSinceLastPurchase Number of days since the item was last purchased.
 * @property avgIntervalDays Average interval in days between purchases.
 */
data class Suggestion(
    val itemId: String,
    val itemName: String,
    val reason: String,
    val score: Double,
    val confidence: Double,
    val daysSinceLastPurchase: Int,
    val avgIntervalDays: Int,
) {
    /**
     * Returns true if the item is overdue (past expected purchase date).
     */
    val isOverdue: Boolean
        get() = daysSinceLastPurchase > avgIntervalDays

    /**
     * Returns the number of days overdue, or 0 if not overdue.
     */
    val daysOverdue: Int
        get() = (daysSinceLastPurchase - avgIntervalDays).coerceAtLeast(0)

    companion object {
        /**
         * Minimum confidence score required to show a suggestion.
         */
        const val MIN_CONFIDENCE = 0.6

        /**
         * Maximum number of suggestions to display.
         */
        const val MAX_SUGGESTIONS = 5

        /**
         * Number of consecutive dismissals before permanently hiding.
         */
        const val PERSISTENT_DISMISS_THRESHOLD = 3

        /**
         * Confidence reduction applied per dismissal.
         */
        const val CONFIDENCE_REDUCTION = 0.05f
    }
}
