package com.restock.core.data.suggestion

import com.restock.core.data.database.dao.ItemDao
import com.restock.core.data.database.entity.PatternEntity
import com.restock.core.data.database.entity.PurchaseEntity
import com.restock.core.domain.model.Suggestion
import javax.inject.Inject
import kotlin.math.roundToInt
import kotlinx.datetime.Clock

/**
 * Scores patterns to create prioritized suggestions.
 *
 * Scoring formula:
 * ```
 * score = confidence × (1 + urgency_factor)
 * urgency_factor = clamp(days_overdue / avg_interval, 0.0, 2.0)
 * days_overdue = days_since_last_purchase - avg_interval
 * ```
 *
 * This ensures:
 * - Higher confidence patterns score higher
 * - Overdue items get priority (up to 2x boost)
 * - Very overdue items don't dominate (capped urgency)
 */
class SuggestionScorer
    @Inject
    constructor(
        private val itemDao: ItemDao,
    ) {
        /**
         * Scores a pattern/purchase pair to create a Suggestion.
         *
         * @param pattern The pattern containing confidence and interval data.
         * @param lastPurchase The most recent purchase for this item.
         * @return A scored Suggestion ready for display.
         * @throws IllegalStateException if the item is not found.
         */
        suspend fun score(
            pattern: PatternEntity,
            lastPurchase: PurchaseEntity,
        ): Suggestion {
            val item =
                itemDao.getById(pattern.itemId)
                    ?: throw IllegalStateException("Item not found: ${pattern.itemId}")

            val daysSince = daysSince(lastPurchase.purchasedAt)
            val avgInterval = pattern.avgIntervalDays

            val daysOverdue = (daysSince - avgInterval).coerceAtLeast(0.0)
            val urgencyFactor = (daysOverdue / avgInterval).coerceIn(0.0, MAX_URGENCY_FACTOR)
            val score = pattern.confidenceScore * (1 + urgencyFactor)

            val reason = generateReason(daysSince.roundToInt(), avgInterval.roundToInt())

            return Suggestion(
                itemId = pattern.itemId,
                itemName = item.name,
                reason = reason,
                score = score,
                confidence = pattern.confidenceScore,
                daysSinceLastPurchase = daysSince.roundToInt(),
                avgIntervalDays = avgInterval.roundToInt(),
            )
        }

        /**
         * Generates human-readable reason text for a suggestion.
         *
         * @param daysSince Days since last purchase.
         * @param avgInterval Average interval in days.
         * @return Reason text like "Usually every 7 days" or "Last bought 10 days ago".
         */
        private fun generateReason(
            daysSince: Int,
            avgInterval: Int,
        ): String {
            return if (daysSince > avgInterval + OVERDUE_THRESHOLD_DAYS) {
                "Last bought $daysSince days ago"
            } else {
                "Usually every $avgInterval days"
            }
        }

        /**
         * Calculates the number of days since a timestamp.
         *
         * @param timestampMs Timestamp in milliseconds since epoch.
         * @return Number of days as a Double for precision in calculations.
         */
        private fun daysSince(timestampMs: Long): Double {
            val now = Clock.System.now().toEpochMilliseconds()
            return (now - timestampMs) / MS_PER_DAY
        }

        companion object {
            /**
             * Maximum urgency factor to prevent runaway scores.
             * A factor of 2.0 means max score = confidence × 3.0
             */
            private const val MAX_URGENCY_FACTOR = 2.0

            /**
             * Days past due before showing "Last bought X days ago" instead
             * of "Usually every X days".
             */
            private const val OVERDUE_THRESHOLD_DAYS = 2

            /**
             * Milliseconds per day for time calculations.
             */
            private const val MS_PER_DAY = 24.0 * 60.0 * 60.0 * 1000.0
        }
    }
