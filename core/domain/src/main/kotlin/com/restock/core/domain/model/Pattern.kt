package com.restock.core.domain.model

import java.time.Instant

/**
 * Domain model representing a learned purchase pattern.
 *
 * Patterns are calculated from purchase history and used
 * to generate smart suggestions.
 */
data class Pattern(
    val itemId: String,
    val avgIntervalDays: Double,
    val confidenceScore: Double,
    val sampleCount: Int,
    val lastCalculated: Instant,
    val lastSuggested: Instant? = null
) {
    companion object {
        /**
         * Minimum number of purchases required to calculate a pattern.
         */
        const val MIN_PURCHASES = 3

        /**
         * Minimum confidence score for a pattern to be considered for suggestions.
         */
        const val SUGGESTION_CONFIDENCE_THRESHOLD = 0.6
    }

    /**
     * Whether this pattern has enough confidence to be suggested.
     */
    val isSuggestionReady: Boolean
        get() = confidenceScore >= SUGGESTION_CONFIDENCE_THRESHOLD &&
                sampleCount >= MIN_PURCHASES
}

/**
 * Pattern with associated item details for display.
 */
data class PatternWithItem(
    val itemId: String,
    val itemName: String,
    val avgIntervalDays: Double,
    val confidenceScore: Double,
    val sampleCount: Int,
    val lastCalculated: Instant,
    val lastSuggested: Instant?,
    val lastPurchasedAt: Instant?
) {
    /**
     * Human-readable interval description.
     */
    val intervalDescription: String
        get() {
            val days = avgIntervalDays.toInt()
            return when {
                days == 1 -> "Daily"
                days == 7 -> "Weekly"
                days == 14 -> "Every 2 weeks"
                days in 28..31 -> "Monthly"
                days < 7 -> "Every $days days"
                days < 14 -> "About weekly"
                days < 28 -> "Every ${days / 7} weeks"
                else -> "Every ${days / 30} months"
            }
        }
}
