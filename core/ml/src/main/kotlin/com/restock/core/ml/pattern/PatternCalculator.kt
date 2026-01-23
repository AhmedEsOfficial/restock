package com.restock.core.ml.pattern

import com.restock.core.domain.model.Pattern
import com.restock.core.domain.model.Purchase
import java.time.Duration
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.sqrt

/**
 * Calculator for purchase patterns using statistical analysis.
 *
 * Uses coefficient of variation (CV) to determine pattern confidence:
 * - Confidence Score = 1 / (1 + CV)
 * - where CV = σ / μ (standard deviation / mean)
 *
 * A higher confidence score indicates more regular purchase behavior.
 */
@Singleton
class PatternCalculator
    @Inject
    constructor() {
        /**
         * Calculate a pattern from purchase history.
         *
         * @param purchases List of purchases sorted by date ascending.
         * @param itemId The item ID for the pattern.
         * @return Pattern if calculable, null if insufficient data.
         */
        fun calculatePattern(
            purchases: List<Purchase>,
            itemId: String,
        ): Pattern? {
            if (purchases.size < MIN_PURCHASES) {
                return null
            }

            // Sort purchases by date
            val sortedPurchases = purchases.sortedBy { it.purchasedAt }

            // Calculate intervals between consecutive purchases (in days)
            val intervals =
                sortedPurchases
                    .zipWithNext { a, b ->
                        Duration.between(a.purchasedAt, b.purchasedAt).toDays().toDouble()
                    }
                    .filter { it > 0 } // Filter out same-day purchases

            if (intervals.isEmpty()) {
                return null
            }

            // Calculate statistics
            val mean = intervals.average()
            val stdDev = intervals.standardDeviation(mean)
            val cv = if (mean > 0) stdDev / mean else Double.MAX_VALUE
            val confidence = (1.0 / (1.0 + cv)).coerceIn(0.0, 1.0)

            return Pattern(
                itemId = itemId,
                avgIntervalDays = mean,
                confidenceScore = confidence,
                sampleCount = purchases.size,
                lastCalculated = Instant.now(),
            )
        }

        /**
         * Determine if an item should be suggested based on pattern and last purchase.
         *
         * Suggestion criteria:
         * - Confidence > SUGGESTION_CONFIDENCE_THRESHOLD
         * - Days since last purchase >= avg_interval - SUGGESTION_BUFFER_DAYS
         * - Sample count >= MIN_PURCHASES
         */
        fun shouldSuggest(
            pattern: Pattern,
            lastPurchase: Purchase?,
        ): Boolean {
            if (!pattern.isSuggestionReady) {
                return false
            }

            if (lastPurchase == null) {
                // No purchases recorded, don't suggest
                return false
            }

            val daysSinceLastPurchase =
                Duration.between(
                    lastPurchase.purchasedAt,
                    Instant.now(),
                ).toDays()

            val triggerThreshold = pattern.avgIntervalDays - SUGGESTION_BUFFER_DAYS

            return daysSinceLastPurchase >= triggerThreshold
        }

        /**
         * Calculate priority score for ranking suggestions.
         *
         * Priority = confidence × urgency
         * where urgency = (days_since_last - avg_interval + buffer) / avg_interval
         */
        fun calculatePriority(
            pattern: Pattern,
            daysSinceLastPurchase: Long,
        ): Double {
            val urgency =
                (
                    (daysSinceLastPurchase - pattern.avgIntervalDays + SUGGESTION_BUFFER_DAYS) /
                        pattern.avgIntervalDays
                ).coerceIn(0.0, 2.0)

            return pattern.confidenceScore * (1.0 + urgency)
        }

        companion object {
            /**
             * Minimum number of purchases required to calculate a pattern.
             */
            const val MIN_PURCHASES = 3

            /**
             * Minimum confidence score for suggestions.
             */
            const val SUGGESTION_CONFIDENCE_THRESHOLD = 0.6

            /**
             * Days before expected purchase to start suggesting.
             */
            const val SUGGESTION_BUFFER_DAYS = 1
        }
    }

/**
 * Extension function to calculate standard deviation of a list of doubles.
 */
private fun List<Double>.standardDeviation(mean: Double? = null): Double {
    if (this.size < 2) return 0.0

    val avg = mean ?: this.average()
    val variance = this.map { (it - avg) * (it - avg) }.average()
    return sqrt(variance)
}
