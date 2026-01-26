package com.restock.core.ml.pattern

import com.google.common.truth.Truth.assertThat
import com.restock.core.domain.model.Pattern
import com.restock.core.domain.model.Purchase
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.temporal.ChronoUnit

/**
 * Comprehensive unit tests for [PatternCalculator].
 *
 * Tests cover:
 * - Interval calculation accuracy
 * - Confidence score calculation using CV
 * - Edge cases (same-day purchases, long gaps, irregular patterns)
 * - Minimum purchase requirements
 * - Suggestion determination logic
 * - Priority calculation
 */
class PatternCalculatorTest {
    private lateinit var calculator: PatternCalculator
    private val now = Instant.now()
    private val testItemId = "test-item-1"

    @BeforeEach
    fun setup() {
        calculator = PatternCalculator()
    }

    @Nested
    @DisplayName("Minimum purchases requirement")
    inner class MinimumPurchasesTests {
        @Test
        fun `returns null for zero purchases`() {
            val pattern = calculator.calculate(emptyList(), testItemId)

            assertThat(pattern).isNull()
        }

        @Test
        fun `returns null for one purchase`() {
            val purchases =
                listOf(
                    purchase(daysAgo = 0),
                )

            val pattern = calculator.calculate(purchases, testItemId)

            assertThat(pattern).isNull()
        }

        @Test
        fun `returns null for two purchases`() {
            val purchases =
                listOf(
                    purchase(daysAgo = 14),
                    purchase(daysAgo = 7),
                )

            val pattern = calculator.calculate(purchases, testItemId)

            assertThat(pattern).isNull()
        }

        @Test
        fun `returns pattern for exactly three purchases`() {
            val purchases =
                listOf(
                    purchase(daysAgo = 14),
                    purchase(daysAgo = 7),
                    purchase(daysAgo = 0),
                )

            val pattern = calculator.calculate(purchases, testItemId)

            assertThat(pattern).isNotNull()
            assertThat(pattern!!.sampleCount).isEqualTo(3)
        }

        @Test
        fun `returns pattern for more than three purchases`() {
            val purchases =
                listOf(
                    purchase(daysAgo = 28),
                    purchase(daysAgo = 21),
                    purchase(daysAgo = 14),
                    purchase(daysAgo = 7),
                    purchase(daysAgo = 0),
                )

            val pattern = calculator.calculate(purchases, testItemId)

            assertThat(pattern).isNotNull()
            assertThat(pattern!!.sampleCount).isEqualTo(5)
        }
    }

    @Nested
    @DisplayName("Interval calculation")
    inner class IntervalCalculationTests {
        @Test
        fun `calculates correct average for weekly purchases`() {
            val purchases =
                listOf(
                    purchase(daysAgo = 21),
                    purchase(daysAgo = 14),
                    purchase(daysAgo = 7),
                    purchase(daysAgo = 0),
                )

            val pattern = calculator.calculate(purchases, testItemId)

            assertThat(pattern).isNotNull()
            assertThat(pattern!!.avgIntervalDays).isWithin(0.1).of(7.0)
        }

        @Test
        fun `calculates correct average for bi-weekly purchases`() {
            val purchases =
                listOf(
                    purchase(daysAgo = 42),
                    purchase(daysAgo = 28),
                    purchase(daysAgo = 14),
                    purchase(daysAgo = 0),
                )

            val pattern = calculator.calculate(purchases, testItemId)

            assertThat(pattern).isNotNull()
            assertThat(pattern!!.avgIntervalDays).isWithin(0.1).of(14.0)
        }

        @Test
        fun `calculates correct average for monthly purchases`() {
            val purchases =
                listOf(
                    purchase(daysAgo = 90),
                    purchase(daysAgo = 60),
                    purchase(daysAgo = 30),
                    purchase(daysAgo = 0),
                )

            val pattern = calculator.calculate(purchases, testItemId)

            assertThat(pattern).isNotNull()
            assertThat(pattern!!.avgIntervalDays).isWithin(0.1).of(30.0)
        }

        @Test
        fun `handles mixed intervals correctly`() {
            // Intervals: 7, 10, 4 days -> average = 7 days
            val purchases =
                listOf(
                    purchase(daysAgo = 21),
                    purchase(daysAgo = 14),
                    purchase(daysAgo = 4),
                    purchase(daysAgo = 0),
                )

            val pattern = calculator.calculate(purchases, testItemId)

            assertThat(pattern).isNotNull()
            assertThat(pattern!!.avgIntervalDays).isWithin(0.1).of(7.0)
        }

        @Test
        fun `sorts purchases by date before calculation`() {
            // Purchases in random order
            val purchases =
                listOf(
                    purchase(daysAgo = 7),
                    purchase(daysAgo = 21),
                    purchase(daysAgo = 0),
                    purchase(daysAgo = 14),
                )

            val pattern = calculator.calculate(purchases, testItemId)

            assertThat(pattern).isNotNull()
            assertThat(pattern!!.avgIntervalDays).isWithin(0.1).of(7.0)
        }
    }

    @Nested
    @DisplayName("Confidence score calculation")
    inner class ConfidenceScoreTests {
        @Test
        fun `high confidence for perfectly consistent intervals`() {
            val purchases =
                listOf(
                    purchase(daysAgo = 21),
                    purchase(daysAgo = 14),
                    purchase(daysAgo = 7),
                    purchase(daysAgo = 0),
                )

            val pattern = calculator.calculate(purchases, testItemId)

            assertThat(pattern).isNotNull()
            // Perfect consistency: CV = 0, Confidence = 1.0
            assertThat(pattern!!.confidenceScore).isGreaterThan(0.95)
        }

        @Test
        fun `moderate confidence for somewhat regular intervals`() {
            // Intervals: 5, 7, 9, 7 days -> slight variance
            val purchases =
                listOf(
                    purchase(daysAgo = 28),
                    purchase(daysAgo = 23),
                    purchase(daysAgo = 16),
                    purchase(daysAgo = 7),
                    purchase(daysAgo = 0),
                )

            val pattern = calculator.calculate(purchases, testItemId)

            assertThat(pattern).isNotNull()
            assertThat(pattern!!.confidenceScore).isGreaterThan(0.7)
            assertThat(pattern.confidenceScore).isLessThan(0.95)
        }

        @Test
        fun `low confidence for irregular intervals`() {
            // Intervals: 15, 3, 20, 7 days -> high variance
            val purchases =
                listOf(
                    purchase(daysAgo = 45),
                    purchase(daysAgo = 30),
                    purchase(daysAgo = 27),
                    purchase(daysAgo = 7),
                    purchase(daysAgo = 0),
                )

            val pattern = calculator.calculate(purchases, testItemId)

            assertThat(pattern).isNotNull()
            assertThat(pattern!!.confidenceScore).isLessThan(0.6)
        }

        @Test
        fun `very low confidence for extremely irregular intervals`() {
            // Intervals: 1, 100, 1 days -> extreme variance
            val purchases =
                listOf(
                    purchase(daysAgo = 102),
                    purchase(daysAgo = 101),
                    purchase(daysAgo = 1),
                    purchase(daysAgo = 0),
                )

            val pattern = calculator.calculate(purchases, testItemId)

            assertThat(pattern).isNotNull()
            assertThat(pattern!!.confidenceScore).isLessThan(0.35)
        }

        @Test
        fun `confidence score is bounded between 0 and 1`() {
            val purchases =
                listOf(
                    purchase(daysAgo = 100),
                    purchase(daysAgo = 1),
                    purchase(daysAgo = 0),
                )

            val pattern = calculator.calculate(purchases, testItemId)

            assertThat(pattern).isNotNull()
            assertThat(pattern!!.confidenceScore).isAtLeast(0.0)
            assertThat(pattern.confidenceScore).isAtMost(1.0)
        }
    }

    @Nested
    @DisplayName("Edge cases")
    inner class EdgeCaseTests {
        @Test
        fun `filters out same-day purchases`() {
            // Same day counted as one shopping trip
            val purchases =
                listOf(
                    purchase(daysAgo = 14, hoursAgo = 0),
                    purchase(daysAgo = 14, hoursAgo = 2),
                    purchase(daysAgo = 7),
                    purchase(daysAgo = 0),
                )

            val pattern = calculator.calculate(purchases, testItemId)

            assertThat(pattern).isNotNull()
            // After filtering same-day, intervals are 7, 7 days
            assertThat(pattern!!.avgIntervalDays).isWithin(0.1).of(7.0)
        }

        @Test
        fun `returns null when all purchases are same day`() {
            val sameDayPurchases =
                listOf(
                    purchase(daysAgo = 0, hoursAgo = 0),
                    purchase(daysAgo = 0, hoursAgo = 1),
                    purchase(daysAgo = 0, hoursAgo = 2),
                )

            val pattern = calculator.calculate(sameDayPurchases, testItemId)

            assertThat(pattern).isNull()
        }

        @Test
        fun `handles long gap without crashing`() {
            // Year-long gap
            val purchases =
                listOf(
                    purchase(daysAgo = 365),
                    purchase(daysAgo = 7),
                    purchase(daysAgo = 0),
                )

            val pattern = calculator.calculate(purchases, testItemId)

            assertThat(pattern).isNotNull()
            // Confidence should be very low due to inconsistency
            assertThat(pattern!!.confidenceScore).isLessThan(0.5)
        }

        @Test
        fun `handles very short intervals`() {
            // Daily purchases
            val purchases =
                listOf(
                    purchase(daysAgo = 3),
                    purchase(daysAgo = 2),
                    purchase(daysAgo = 1),
                    purchase(daysAgo = 0),
                )

            val pattern = calculator.calculate(purchases, testItemId)

            assertThat(pattern).isNotNull()
            assertThat(pattern!!.avgIntervalDays).isWithin(0.1).of(1.0)
        }

        @Test
        fun `preserves item id in pattern`() {
            val customItemId = "custom-item-id-123"
            val purchases =
                listOf(
                    purchase(daysAgo = 14),
                    purchase(daysAgo = 7),
                    purchase(daysAgo = 0),
                )

            val pattern = calculator.calculate(purchases, customItemId)

            assertThat(pattern).isNotNull()
            assertThat(pattern!!.itemId).isEqualTo(customItemId)
        }

        @Test
        fun `sets lastCalculated to current time`() {
            val beforeCalculation = Instant.now()
            val purchases =
                listOf(
                    purchase(daysAgo = 14),
                    purchase(daysAgo = 7),
                    purchase(daysAgo = 0),
                )

            val pattern = calculator.calculate(purchases, testItemId)
            val afterCalculation = Instant.now()

            assertThat(pattern).isNotNull()
            assertThat(pattern!!.lastCalculated).isAtLeast(beforeCalculation)
            assertThat(pattern.lastCalculated).isAtMost(afterCalculation)
        }
    }

    @Nested
    @DisplayName("shouldSuggest logic")
    inner class ShouldSuggestTests {
        @Test
        fun `returns false when pattern confidence is too low`() {
            // Below 0.6 threshold
            val lowConfidencePattern =
                Pattern(
                    itemId = testItemId,
                    avgIntervalDays = 7.0,
                    confidenceScore = 0.5,
                    sampleCount = 5,
                    lastCalculated = now,
                )
            val lastPurchase = purchase(daysAgo = 7)

            val shouldSuggest = calculator.shouldSuggest(lowConfidencePattern, lastPurchase)

            assertThat(shouldSuggest).isFalse()
        }

        @Test
        fun `returns false when sample count is too low`() {
            // Below 3 threshold
            val lowSamplePattern =
                Pattern(
                    itemId = testItemId,
                    avgIntervalDays = 7.0,
                    confidenceScore = 0.8,
                    sampleCount = 2,
                    lastCalculated = now,
                )
            val lastPurchase = purchase(daysAgo = 7)

            val shouldSuggest = calculator.shouldSuggest(lowSamplePattern, lastPurchase)

            assertThat(shouldSuggest).isFalse()
        }

        @Test
        fun `returns false when last purchase is null`() {
            val validPattern = createHighConfidencePattern()

            val shouldSuggest = calculator.shouldSuggest(validPattern, null)

            assertThat(shouldSuggest).isFalse()
        }

        @Test
        fun `returns false when purchased recently`() {
            val pattern = createHighConfidencePattern(avgIntervalDays = 7.0)
            // Only 3 days since last purchase, but interval is 7 days
            val lastPurchase = purchase(daysAgo = 3)

            val shouldSuggest = calculator.shouldSuggest(pattern, lastPurchase)

            assertThat(shouldSuggest).isFalse()
        }

        @Test
        fun `returns true when due for restock with buffer`() {
            val pattern = createHighConfidencePattern(avgIntervalDays = 7.0)
            // 6 days since last purchase (interval - 1 buffer day)
            val lastPurchase = purchase(daysAgo = 6)

            val shouldSuggest = calculator.shouldSuggest(pattern, lastPurchase)

            assertThat(shouldSuggest).isTrue()
        }

        @Test
        fun `returns true when overdue for restock`() {
            val pattern = createHighConfidencePattern(avgIntervalDays = 7.0)
            // 10 days since last purchase, overdue
            val lastPurchase = purchase(daysAgo = 10)

            val shouldSuggest = calculator.shouldSuggest(pattern, lastPurchase)

            assertThat(shouldSuggest).isTrue()
        }
    }

    @Nested
    @DisplayName("calculatePriority logic")
    inner class CalculatePriorityTests {
        @Test
        fun `higher confidence gives higher priority`() {
            val highConfidencePattern =
                createHighConfidencePattern(
                    confidenceScore = 0.9,
                    avgIntervalDays = 7.0,
                )
            val lowConfidencePattern =
                createHighConfidencePattern(
                    confidenceScore = 0.6,
                    avgIntervalDays = 7.0,
                )
            val daysSincePurchase = 7L

            val highPriority = calculator.calculatePriority(highConfidencePattern, daysSincePurchase)
            val lowPriority = calculator.calculatePriority(lowConfidencePattern, daysSincePurchase)

            assertThat(highPriority).isGreaterThan(lowPriority)
        }

        @Test
        fun `more overdue gives higher priority`() {
            val pattern = createHighConfidencePattern(avgIntervalDays = 7.0)
            val slightlyOverdue = 8L
            val veryOverdue = 14L

            val slightlyOverduePriority = calculator.calculatePriority(pattern, slightlyOverdue)
            val veryOverduePriority = calculator.calculatePriority(pattern, veryOverdue)

            assertThat(veryOverduePriority).isGreaterThan(slightlyOverduePriority)
        }

        @Test
        fun `priority is always positive`() {
            val pattern = createHighConfidencePattern(avgIntervalDays = 7.0)

            // Even when not yet due
            val priority = calculator.calculatePriority(pattern, 0L)

            assertThat(priority).isAtLeast(0.0)
        }

        @Test
        fun `urgency is capped at maximum`() {
            val pattern =
                createHighConfidencePattern(
                    confidenceScore = 1.0,
                    avgIntervalDays = 7.0,
                )
            // Extremely overdue (100 days for 7-day interval)
            val extremelyOverdue = 100L

            val priority = calculator.calculatePriority(pattern, extremelyOverdue)

            // Maximum priority = confidence * (1 + maxUrgency) = 1.0 * (1 + 2.0) = 3.0
            assertThat(priority).isAtMost(3.0)
        }
    }

    // Helper functions
    private fun purchase(
        daysAgo: Int,
        hoursAgo: Int = 0,
        id: String = "purchase-${System.nanoTime()}",
    ): Purchase {
        val purchasedAt =
            now
                .minus(daysAgo.toLong(), ChronoUnit.DAYS)
                .minus(hoursAgo.toLong(), ChronoUnit.HOURS)

        return Purchase(
            id = id,
            itemId = testItemId,
            purchasedAt = purchasedAt,
            quantity = 1,
        )
    }

    private fun createHighConfidencePattern(
        avgIntervalDays: Double = 7.0,
        confidenceScore: Double = 0.85,
        sampleCount: Int = 5,
    ) = Pattern(
        itemId = testItemId,
        avgIntervalDays = avgIntervalDays,
        confidenceScore = confidenceScore,
        sampleCount = sampleCount,
        lastCalculated = now,
    )
}
