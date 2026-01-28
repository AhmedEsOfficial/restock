package com.restock.core.data.suggestion

import com.google.common.truth.Truth.assertThat
import com.restock.core.data.database.dao.ItemDao
import com.restock.core.data.database.entity.ItemEntity
import com.restock.core.data.database.entity.PatternEntity
import com.restock.core.data.database.entity.PurchaseEntity
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

/**
 * Unit tests for [SuggestionScorer].
 *
 * Tests cover:
 * - Scoring formula accuracy
 * - Urgency factor calculation and capping
 * - Reason text generation
 * - Edge cases
 */
class SuggestionScorerTest {
    private val itemDao: ItemDao = mockk()
    private lateinit var scorer: SuggestionScorer

    private val testItemId = "test-item-1"
    private val testItemName = "Milk"
    private val now = Clock.System.now().toEpochMilliseconds()

    @BeforeEach
    fun setup() {
        scorer = SuggestionScorer(itemDao)

        // Default mock - return test item for any ID
        coEvery { itemDao.getById(any()) } returns createItemEntity()
    }

    @Nested
    @DisplayName("Scoring formula")
    inner class ScoringTests {
        @Test
        fun `calculates correct score for on-time item`() =
            runTest {
                // Item bought 7 days ago with 7-day interval - exactly on time
                val pattern = createPattern(avgIntervalDays = 7.0, confidenceScore = 0.8)
                val purchase = createPurchase(daysAgo = 7)

                val suggestion = scorer.score(pattern, purchase)

                // On time means 0 days overdue, urgency_factor = 0, score = 0.8 * (1 + 0) = 0.8
                assertThat(suggestion.score).isWithin(0.01).of(0.8)
            }

        @Test
        fun `higher score for overdue items`() =
            runTest {
                // Item bought 10 days ago with 7-day interval - 3 days overdue
                val pattern = createPattern(avgIntervalDays = 7.0, confidenceScore = 0.8)
                val overduePurchase = createPurchase(daysAgo = 10)
                val onTimePurchase = createPurchase(daysAgo = 7)

                val overdueScore = scorer.score(pattern, overduePurchase)
                val onTimeScore = scorer.score(pattern, onTimePurchase)

                assertThat(overdueScore.score).isGreaterThan(onTimeScore.score)
            }

        @Test
        fun `higher confidence gives higher score when same urgency`() =
            runTest {
                val highConfPattern = createPattern(avgIntervalDays = 7.0, confidenceScore = 0.9)
                val lowConfPattern = createPattern(avgIntervalDays = 7.0, confidenceScore = 0.6)
                val purchase = createPurchase(daysAgo = 7)

                val highScore = scorer.score(highConfPattern, purchase)
                val lowScore = scorer.score(lowConfPattern, purchase)

                assertThat(highScore.score).isGreaterThan(lowScore.score)
            }

        @Test
        fun `urgency factor capped at 2x`() =
            runTest {
                // Very overdue - 100 days late on a 7-day interval
                val pattern = createPattern(avgIntervalDays = 7.0, confidenceScore = 0.8)
                val veryOverdue = createPurchase(daysAgo = 100)

                val suggestion = scorer.score(pattern, veryOverdue)

                // Max urgency_factor = 2.0, so max score = 0.8 * (1 + 2.0) = 2.4
                assertThat(suggestion.score).isWithin(0.01).of(2.4)
            }

        @Test
        fun `score never negative for early items`() =
            runTest {
                // Item bought only 2 days ago with 7-day interval - 5 days early
                val pattern = createPattern(avgIntervalDays = 7.0, confidenceScore = 0.8)
                val earlyPurchase = createPurchase(daysAgo = 2)

                val suggestion = scorer.score(pattern, earlyPurchase)

                // Early item should have 0 urgency (clamped), score = 0.8 * 1 = 0.8
                assertThat(suggestion.score).isGreaterThan(0.0)
                assertThat(suggestion.score).isWithin(0.01).of(0.8)
            }

        @Test
        fun `calculated urgency factor is correct`() =
            runTest {
                // 10 days since last purchase, 7 day interval
                // Days overdue = 10 - 7 = 3
                // Urgency factor = 3 / 7 ≈ 0.43
                // Score = 0.8 * (1 + 0.43) ≈ 1.14
                val pattern = createPattern(avgIntervalDays = 7.0, confidenceScore = 0.8)
                val purchase = createPurchase(daysAgo = 10)

                val suggestion = scorer.score(pattern, purchase)

                val expectedUrgency = 3.0 / 7.0
                val expectedScore = 0.8 * (1 + expectedUrgency)
                assertThat(suggestion.score).isWithin(0.05).of(expectedScore)
            }
    }

    @Nested
    @DisplayName("Suggestion properties")
    inner class SuggestionPropertiesTests {
        @Test
        fun `populates all suggestion fields correctly`() =
            runTest {
                val pattern = createPattern(avgIntervalDays = 7.0, confidenceScore = 0.85)
                val purchase = createPurchase(daysAgo = 8)

                val suggestion = scorer.score(pattern, purchase)

                assertThat(suggestion.itemId).isEqualTo(testItemId)
                assertThat(suggestion.itemName).isEqualTo(testItemName)
                assertThat(suggestion.confidence).isEqualTo(0.85)
                assertThat(suggestion.avgIntervalDays).isEqualTo(7)
                assertThat(suggestion.daysSinceLastPurchase).isEqualTo(8)
            }

        @Test
        fun `gets item name from DAO`() =
            runTest {
                val customName = "Organic Milk"
                coEvery { itemDao.getById(testItemId) } returns createItemEntity(name = customName)

                val pattern = createPattern()
                val purchase = createPurchase(daysAgo = 7)

                val suggestion = scorer.score(pattern, purchase)

                assertThat(suggestion.itemName).isEqualTo(customName)
            }

        @Test
        fun `rounds days since purchase to int`() =
            runTest {
                // Create purchase that would result in fractional days
                val pattern = createPattern(avgIntervalDays = 7.0, confidenceScore = 0.8)
                val purchase = createPurchase(daysAgo = 7) // Will be slightly more due to timing

                val suggestion = scorer.score(pattern, purchase)

                // Should be an integer, not fractional
                assertThat(suggestion.daysSinceLastPurchase).isEqualTo(7)
            }

        @Test
        fun `rounds avg interval to int`() =
            runTest {
                val pattern = createPattern(avgIntervalDays = 7.5, confidenceScore = 0.8)
                val purchase = createPurchase(daysAgo = 8)

                val suggestion = scorer.score(pattern, purchase)

                assertThat(suggestion.avgIntervalDays).isEqualTo(8)
            }
    }

    @Nested
    @DisplayName("Reason text generation")
    inner class ReasonTextTests {
        @Test
        fun `shows usual interval for on-time items`() =
            runTest {
                val pattern = createPattern(avgIntervalDays = 7.0, confidenceScore = 0.8)
                val purchase = createPurchase(daysAgo = 7)

                val suggestion = scorer.score(pattern, purchase)

                assertThat(suggestion.reason).isEqualTo("Usually every 7 days")
            }

        @Test
        fun `shows usual interval for slightly overdue items`() =
            runTest {
                // 2 days past due threshold is still "usually every X days"
                val pattern = createPattern(avgIntervalDays = 7.0, confidenceScore = 0.8)
                val purchase = createPurchase(daysAgo = 9) // Only 2 days overdue

                val suggestion = scorer.score(pattern, purchase)

                assertThat(suggestion.reason).isEqualTo("Usually every 7 days")
            }

        @Test
        fun `shows last bought for significantly overdue items`() =
            runTest {
                // More than 2 days past due shows "Last bought X days ago"
                val pattern = createPattern(avgIntervalDays = 7.0, confidenceScore = 0.8)
                val purchase = createPurchase(daysAgo = 10) // 3 days overdue

                val suggestion = scorer.score(pattern, purchase)

                assertThat(suggestion.reason).isEqualTo("Last bought 10 days ago")
            }

        @Test
        fun `handles long intervals in reason text`() =
            runTest {
                val pattern = createPattern(avgIntervalDays = 30.0, confidenceScore = 0.8)
                val purchase = createPurchase(daysAgo = 30)

                val suggestion = scorer.score(pattern, purchase)

                assertThat(suggestion.reason).isEqualTo("Usually every 30 days")
            }
    }

    @Nested
    @DisplayName("Error handling")
    inner class ErrorHandlingTests {
        @Test
        fun `throws exception when item not found`() =
            runTest {
                coEvery { itemDao.getById(testItemId) } returns null

                val pattern = createPattern()
                val purchase = createPurchase(daysAgo = 7)

                assertThrows<IllegalStateException> {
                    scorer.score(pattern, purchase)
                }
            }

        @Test
        fun `exception message contains item ID`() =
            runTest {
                coEvery { itemDao.getById(testItemId) } returns null

                val pattern = createPattern()
                val purchase = createPurchase(daysAgo = 7)

                val exception =
                    assertThrows<IllegalStateException> {
                        scorer.score(pattern, purchase)
                    }

                assertThat(exception.message).contains(testItemId)
            }
    }

    @Nested
    @DisplayName("Edge cases")
    inner class EdgeCaseTests {
        @Test
        fun `handles very short intervals`() =
            runTest {
                val pattern = createPattern(avgIntervalDays = 1.0, confidenceScore = 0.8)
                val purchase = createPurchase(daysAgo = 2)

                val suggestion = scorer.score(pattern, purchase)

                assertThat(suggestion.avgIntervalDays).isEqualTo(1)
                assertThat(suggestion.score).isGreaterThan(0.8) // Should have urgency boost
            }

        @Test
        fun `handles very long intervals`() =
            runTest {
                val pattern = createPattern(avgIntervalDays = 365.0, confidenceScore = 0.8)
                val purchase = createPurchase(daysAgo = 365)

                val suggestion = scorer.score(pattern, purchase)

                assertThat(suggestion.avgIntervalDays).isEqualTo(365)
                assertThat(suggestion.reason).isEqualTo("Usually every 365 days")
            }

        @Test
        fun `handles zero days since purchase`() =
            runTest {
                val pattern = createPattern(avgIntervalDays = 7.0, confidenceScore = 0.8)
                val purchase = createPurchase(daysAgo = 0)

                val suggestion = scorer.score(pattern, purchase)

                assertThat(suggestion.daysSinceLastPurchase).isEqualTo(0)
                assertThat(suggestion.score).isWithin(0.01).of(0.8) // No urgency boost
            }

        @Test
        fun `handles perfect confidence`() =
            runTest {
                val pattern = createPattern(avgIntervalDays = 7.0, confidenceScore = 1.0)
                val purchase = createPurchase(daysAgo = 7)

                val suggestion = scorer.score(pattern, purchase)

                assertThat(suggestion.score).isWithin(0.01).of(1.0)
                assertThat(suggestion.confidence).isEqualTo(1.0)
            }

        @Test
        fun `handles minimum confidence`() =
            runTest {
                val pattern = createPattern(avgIntervalDays = 7.0, confidenceScore = 0.1)
                val purchase = createPurchase(daysAgo = 7)

                val suggestion = scorer.score(pattern, purchase)

                assertThat(suggestion.score).isWithin(0.01).of(0.1)
                assertThat(suggestion.confidence).isEqualTo(0.1)
            }
    }

    // Helper functions
    private fun createPattern(
        itemId: String = testItemId,
        avgIntervalDays: Double = 7.0,
        confidenceScore: Double = 0.8,
        sampleCount: Int = 5,
    ) = PatternEntity(
        itemId = itemId,
        avgIntervalDays = avgIntervalDays,
        confidenceScore = confidenceScore,
        sampleCount = sampleCount,
        lastCalculated = now,
        lastSuggested = null,
    )

    private fun createPurchase(
        itemId: String = testItemId,
        daysAgo: Int = 0,
    ): PurchaseEntity {
        val purchasedAt = now - (daysAgo * 24L * 60 * 60 * 1000)
        return PurchaseEntity(
            id = "purchase-${System.nanoTime()}",
            itemId = itemId,
            purchasedAt = purchasedAt,
            quantity = 1,
        )
    }

    private fun createItemEntity(
        id: String = testItemId,
        name: String = testItemName,
    ) = ItemEntity(
        id = id,
        name = name,
        normalizedName = name.lowercase(),
        createdAt = now - (30L * 24 * 60 * 60 * 1000),
        isActive = true,
    )
}
