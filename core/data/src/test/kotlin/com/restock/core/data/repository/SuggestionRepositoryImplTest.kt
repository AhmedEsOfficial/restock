package com.restock.core.data.repository

import com.google.common.truth.Truth.assertThat
import com.restock.core.data.database.dao.CurrentListDao
import com.restock.core.data.database.dao.PatternDao
import com.restock.core.data.database.dao.PurchaseDao
import com.restock.core.data.database.dao.SuggestionDismissalDao
import com.restock.core.data.database.entity.PatternEntity
import com.restock.core.data.database.entity.PurchaseEntity
import com.restock.core.data.database.entity.SuggestionDismissalEntity
import com.restock.core.data.suggestion.SuggestionScorer
import com.restock.core.domain.model.Suggestion
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest

/**
 * Unit tests for [SuggestionRepositoryImpl].
 *
 * Tests cover:
 * - Suggestion generation with filtering and prioritization
 * - Dismiss functionality with confidence reduction
 * - Accept functionality with timestamp updates
 * - Shopping trip counting for learning state
 */
class SuggestionRepositoryImplTest {
    private val patternDao: PatternDao = mockk(relaxed = true)
    private val purchaseDao: PurchaseDao = mockk(relaxed = true)
    private val currentListDao: CurrentListDao = mockk(relaxed = true)
    private val dismissalDao: SuggestionDismissalDao = mockk(relaxed = true)
    private val suggestionScorer: SuggestionScorer = mockk(relaxed = true)
    private val testDispatcher = StandardTestDispatcher()

    private lateinit var repository: SuggestionRepositoryImpl

    private val now = System.currentTimeMillis()

    @BeforeEach
    fun setup() {
        repository =
            SuggestionRepositoryImpl(
                patternDao = patternDao,
                purchaseDao = purchaseDao,
                currentListDao = currentListDao,
                dismissalDao = dismissalDao,
                suggestionScorer = suggestionScorer,
                dispatcher = testDispatcher,
            )
    }

    @Nested
    @DisplayName("getSuggestions")
    inner class GetSuggestionsTests {
        @Test
        fun `returns empty list when no patterns above confidence`() =
            runTest(testDispatcher) {
                coEvery { patternDao.getPatternsAboveConfidence(any()) } returns emptyList()
                coEvery { currentListDao.getCurrentItemIds() } returns emptyList()
                coEvery { dismissalDao.getPersistentlyDismissedIds() } returns emptyList()

                val result = repository.getSuggestions()

                assertThat(result).isEmpty()
            }

        @Test
        fun `filters out items already on current list`() =
            runTest(testDispatcher) {
                val pattern = createPattern("item-1")
                coEvery { patternDao.getPatternsAboveConfidence(any()) } returns listOf(pattern)
                coEvery { currentListDao.getCurrentItemIds() } returns listOf("item-1")
                coEvery { dismissalDao.getPersistentlyDismissedIds() } returns emptyList()

                val result = repository.getSuggestions()

                assertThat(result).isEmpty()
            }

        @Test
        fun `filters out persistently dismissed items`() =
            runTest(testDispatcher) {
                val pattern = createPattern("item-1")
                coEvery { patternDao.getPatternsAboveConfidence(any()) } returns listOf(pattern)
                coEvery { currentListDao.getCurrentItemIds() } returns emptyList()
                coEvery { dismissalDao.getPersistentlyDismissedIds() } returns listOf("item-1")

                val result = repository.getSuggestions()

                assertThat(result).isEmpty()
            }

        @Test
        fun `filters out items not yet due`() =
            runTest(testDispatcher) {
                // Pattern with 7-day interval, last purchased 2 days ago (not due)
                val pattern = createPattern("item-1", avgIntervalDays = 7.0)
                val purchase = createPurchase("item-1", daysAgo = 2)

                coEvery { patternDao.getPatternsAboveConfidence(any()) } returns listOf(pattern)
                coEvery { currentListDao.getCurrentItemIds() } returns emptyList()
                coEvery { dismissalDao.getPersistentlyDismissedIds() } returns emptyList()
                coEvery { purchaseDao.getLastPurchaseForItem("item-1") } returns purchase

                val result = repository.getSuggestions()

                assertThat(result).isEmpty()
            }

        @Test
        fun `includes items that are due`() =
            runTest(testDispatcher) {
                // Pattern with 7-day interval, last purchased 7 days ago (due)
                val pattern = createPattern("item-1", avgIntervalDays = 7.0)
                val purchase = createPurchase("item-1", daysAgo = 7)
                val suggestion = createSuggestion("item-1")

                coEvery { patternDao.getPatternsAboveConfidence(any()) } returns listOf(pattern)
                coEvery { currentListDao.getCurrentItemIds() } returns emptyList()
                coEvery { dismissalDao.getPersistentlyDismissedIds() } returns emptyList()
                coEvery { purchaseDao.getLastPurchaseForItem("item-1") } returns purchase
                coEvery { suggestionScorer.score(pattern, purchase) } returns suggestion

                val result = repository.getSuggestions()

                assertThat(result).hasSize(1)
                assertThat(result[0].itemId).isEqualTo("item-1")
            }

        @Test
        fun `sorts by score descending`() =
            runTest(testDispatcher) {
                val pattern1 = createPattern("item-1")
                val pattern2 = createPattern("item-2")
                val purchase1 = createPurchase("item-1", daysAgo = 7)
                val purchase2 = createPurchase("item-2", daysAgo = 7)
                val suggestion1 = createSuggestion("item-1", score = 0.8)
                val suggestion2 = createSuggestion("item-2", score = 1.2)

                coEvery { patternDao.getPatternsAboveConfidence(any()) } returns listOf(pattern1, pattern2)
                coEvery { currentListDao.getCurrentItemIds() } returns emptyList()
                coEvery { dismissalDao.getPersistentlyDismissedIds() } returns emptyList()
                coEvery { purchaseDao.getLastPurchaseForItem("item-1") } returns purchase1
                coEvery { purchaseDao.getLastPurchaseForItem("item-2") } returns purchase2
                coEvery { suggestionScorer.score(pattern1, purchase1) } returns suggestion1
                coEvery { suggestionScorer.score(pattern2, purchase2) } returns suggestion2

                val result = repository.getSuggestions()

                assertThat(result).hasSize(2)
                assertThat(result[0].itemId).isEqualTo("item-2") // Higher score first
                assertThat(result[1].itemId).isEqualTo("item-1")
            }

        @Test
        fun `limits to specified count`() =
            runTest(testDispatcher) {
                val patterns = (1..10).map { createPattern("item-$it") }
                val purchases = (1..10).map { createPurchase("item-$it", daysAgo = 7) }
                val suggestions = (1..10).map { createSuggestion("item-$it", score = it.toDouble()) }

                coEvery { patternDao.getPatternsAboveConfidence(any()) } returns patterns
                coEvery { currentListDao.getCurrentItemIds() } returns emptyList()
                coEvery { dismissalDao.getPersistentlyDismissedIds() } returns emptyList()
                patterns.forEachIndexed { index, pattern ->
                    coEvery { purchaseDao.getLastPurchaseForItem("item-${index + 1}") } returns purchases[index]
                    coEvery { suggestionScorer.score(pattern, purchases[index]) } returns suggestions[index]
                }

                val result = repository.getSuggestions(limit = 5)

                assertThat(result).hasSize(5)
            }

        @Test
        fun `skips items without purchase history`() =
            runTest(testDispatcher) {
                val pattern = createPattern("item-1")

                coEvery { patternDao.getPatternsAboveConfidence(any()) } returns listOf(pattern)
                coEvery { currentListDao.getCurrentItemIds() } returns emptyList()
                coEvery { dismissalDao.getPersistentlyDismissedIds() } returns emptyList()
                coEvery { purchaseDao.getLastPurchaseForItem("item-1") } returns null

                val result = repository.getSuggestions()

                assertThat(result).isEmpty()
            }
    }

    @Nested
    @DisplayName("observeSuggestions")
    inner class ObserveSuggestionsTests {
        @Test
        fun `combines all data sources reactively`() =
            runTest(testDispatcher) {
                val pattern = createPattern("item-1")
                val purchase = createPurchase("item-1", daysAgo = 7)
                val suggestion = createSuggestion("item-1")

                every { patternDao.observePatternsAboveConfidence(any()) } returns flowOf(listOf(pattern))
                every { purchaseDao.observeLatestPurchases() } returns flowOf(listOf(purchase))
                every { currentListDao.observeCurrentItemIds() } returns flowOf(emptyList())
                every { dismissalDao.observeDismissals() } returns flowOf(emptyList())
                coEvery { suggestionScorer.score(pattern, purchase) } returns suggestion

                val result = repository.observeSuggestions().first()

                assertThat(result).hasSize(1)
                assertThat(result[0].itemId).isEqualTo("item-1")
            }

        @Test
        fun `filters out current list items reactively`() =
            runTest(testDispatcher) {
                val pattern = createPattern("item-1")
                val purchase = createPurchase("item-1", daysAgo = 7)

                every { patternDao.observePatternsAboveConfidence(any()) } returns flowOf(listOf(pattern))
                every { purchaseDao.observeLatestPurchases() } returns flowOf(listOf(purchase))
                every { currentListDao.observeCurrentItemIds() } returns flowOf(listOf("item-1"))
                every { dismissalDao.observeDismissals() } returns flowOf(emptyList())

                val result = repository.observeSuggestions().first()

                assertThat(result).isEmpty()
            }
    }

    @Nested
    @DisplayName("dismissSuggestion")
    inner class DismissSuggestionTests {
        @Test
        fun `increments dismiss count for first dismissal`() =
            runTest(testDispatcher) {
                coEvery { dismissalDao.getDismissal("item-1") } returns null

                val dismissalSlot = slot<SuggestionDismissalEntity>()
                coEvery { dismissalDao.upsert(capture(dismissalSlot)) } returns Unit

                repository.dismissSuggestion("item-1")

                assertThat(dismissalSlot.captured.dismissCount).isEqualTo(1)
                assertThat(dismissalSlot.captured.isPersistentlyDismissed).isFalse()
            }

        @Test
        fun `increments dismiss count for subsequent dismissals`() =
            runTest(testDispatcher) {
                val existing = createDismissal("item-1", dismissCount = 1)
                coEvery { dismissalDao.getDismissal("item-1") } returns existing

                val dismissalSlot = slot<SuggestionDismissalEntity>()
                coEvery { dismissalDao.upsert(capture(dismissalSlot)) } returns Unit

                repository.dismissSuggestion("item-1")

                assertThat(dismissalSlot.captured.dismissCount).isEqualTo(2)
                assertThat(dismissalSlot.captured.isPersistentlyDismissed).isFalse()
            }

        @Test
        fun `marks as persistently dismissed after 3 dismissals`() =
            runTest(testDispatcher) {
                val existing = createDismissal("item-1", dismissCount = 2)
                coEvery { dismissalDao.getDismissal("item-1") } returns existing

                val dismissalSlot = slot<SuggestionDismissalEntity>()
                coEvery { dismissalDao.upsert(capture(dismissalSlot)) } returns Unit

                repository.dismissSuggestion("item-1")

                assertThat(dismissalSlot.captured.dismissCount).isEqualTo(3)
                assertThat(dismissalSlot.captured.isPersistentlyDismissed).isTrue()
            }

        @Test
        fun `reduces pattern confidence on non-persistent dismissal`() =
            runTest(testDispatcher) {
                coEvery { dismissalDao.getDismissal("item-1") } returns null

                repository.dismissSuggestion("item-1")

                coVerify {
                    patternDao.reduceConfidence("item-1", SuggestionRepositoryImpl.CONFIDENCE_REDUCTION)
                }
            }

        @Test
        fun `does not reduce confidence on persistent dismissal`() =
            runTest(testDispatcher) {
                val existing = createDismissal("item-1", dismissCount = 2)
                coEvery { dismissalDao.getDismissal("item-1") } returns existing

                repository.dismissSuggestion("item-1")

                coVerify(exactly = 0) {
                    patternDao.reduceConfidence(any(), any())
                }
            }
    }

    @Nested
    @DisplayName("acceptSuggestion")
    inner class AcceptSuggestionTests {
        @Test
        fun `updates last suggested timestamp`() =
            runTest(testDispatcher) {
                repository.acceptSuggestion("item-1")

                coVerify {
                    patternDao.updateLastSuggested("item-1", any())
                }
            }
    }

    @Nested
    @DisplayName("isDismissed")
    inner class IsDismissedTests {
        @Test
        fun `returns true when persistently dismissed`() =
            runTest(testDispatcher) {
                val dismissal = createDismissal("item-1", dismissCount = 3, isPersistentlyDismissed = true)
                coEvery { dismissalDao.getDismissal("item-1") } returns dismissal

                val result = repository.isDismissed("item-1")

                assertThat(result).isTrue()
            }

        @Test
        fun `returns false when not persistently dismissed`() =
            runTest(testDispatcher) {
                val dismissal = createDismissal("item-1", dismissCount = 2, isPersistentlyDismissed = false)
                coEvery { dismissalDao.getDismissal("item-1") } returns dismissal

                val result = repository.isDismissed("item-1")

                assertThat(result).isFalse()
            }

        @Test
        fun `returns false when no dismissal record`() =
            runTest(testDispatcher) {
                coEvery { dismissalDao.getDismissal("item-1") } returns null

                val result = repository.isDismissed("item-1")

                assertThat(result).isFalse()
            }
    }

    @Nested
    @DisplayName("clearDismissalOnPurchase")
    inner class ClearDismissalOnPurchaseTests {
        @Test
        fun `resets dismissal state`() =
            runTest(testDispatcher) {
                repository.clearDismissalOnPurchase("item-1")

                coVerify {
                    dismissalDao.resetDismissal("item-1")
                }
            }
    }

    @Nested
    @DisplayName("getShoppingTripCount")
    inner class GetShoppingTripCountTests {
        @Test
        fun `returns count of distinct purchase days`() =
            runTest(testDispatcher) {
                coEvery { purchaseDao.getDistinctPurchaseDays() } returns 5

                val result = repository.getShoppingTripCount()

                assertThat(result).isEqualTo(5)
            }

        @Test
        fun `returns zero when no purchases`() =
            runTest(testDispatcher) {
                coEvery { purchaseDao.getDistinctPurchaseDays() } returns 0

                val result = repository.getShoppingTripCount()

                assertThat(result).isEqualTo(0)
            }
    }

    // Helper functions
    private fun createPattern(
        itemId: String,
        avgIntervalDays: Double = 7.0,
        confidenceScore: Double = 0.8,
    ) = PatternEntity(
        itemId = itemId,
        avgIntervalDays = avgIntervalDays,
        confidenceScore = confidenceScore,
        sampleCount = 5,
        lastCalculated = now,
        lastSuggested = null,
    )

    private fun createPurchase(
        itemId: String,
        daysAgo: Int = 0,
    ) = PurchaseEntity(
        id = "purchase-${System.nanoTime()}",
        itemId = itemId,
        purchasedAt = now - (daysAgo * 24L * 60 * 60 * 1000),
        quantity = 1,
    )

    private fun createSuggestion(
        itemId: String,
        score: Double = 1.0,
    ) = Suggestion(
        itemId = itemId,
        itemName = "Item $itemId",
        reason = "Usually every 7 days",
        score = score,
        confidence = 0.8,
        daysSinceLastPurchase = 7,
        avgIntervalDays = 7,
    )

    private fun createDismissal(
        itemId: String,
        dismissCount: Int = 1,
        isPersistentlyDismissed: Boolean = false,
    ) = SuggestionDismissalEntity(
        itemId = itemId,
        dismissCount = dismissCount,
        lastDismissedAt = now,
        isPersistentlyDismissed = isPersistentlyDismissed,
    )
}
