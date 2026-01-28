package com.restock.core.data.repository

import com.google.common.truth.Truth.assertThat
import com.restock.core.data.database.dao.PatternDao
import com.restock.core.data.database.entity.PatternEntity
import com.restock.core.domain.model.Pattern
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.Instant
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import com.restock.core.data.database.dao.PatternWithItem as PatternWithItemDao

/**
 * Unit tests for [PatternRepositoryImpl].
 *
 * Tests verify correct mapping between domain models and entities,
 * proper delegation to DAO, and error handling.
 */
class PatternRepositoryImplTest {
    private val patternDao: PatternDao = mockk(relaxed = true)
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: PatternRepositoryImpl

    @BeforeEach
    fun setup() {
        repository = PatternRepositoryImpl(patternDao, testDispatcher)
    }

    @Nested
    @DisplayName("observePatternsWithItems")
    inner class ObservePatternsWithItemsTests {
        @Test
        fun `returns flow of mapped domain models`() =
            runTest(testDispatcher) {
                val daoPatterns =
                    listOf(
                        createDaoPatternWithItem(itemId = "1", itemName = "Milk", avgIntervalDays = 7.0),
                        createDaoPatternWithItem(itemId = "2", itemName = "Eggs", avgIntervalDays = 14.0),
                    )
                every { patternDao.observePatternsWithItems() } returns flowOf(daoPatterns)

                val result = repository.observePatternsWithItems().first()

                assertThat(result).hasSize(2)
                assertThat(result[0].itemId).isEqualTo("1")
                assertThat(result[0].itemName).isEqualTo("Milk")
                assertThat(result[0].avgIntervalDays).isEqualTo(7.0)
                assertThat(result[1].itemId).isEqualTo("2")
                assertThat(result[1].itemName).isEqualTo("Eggs")
                assertThat(result[1].avgIntervalDays).isEqualTo(14.0)
            }

        @Test
        fun `returns empty list when no patterns exist`() =
            runTest(testDispatcher) {
                every { patternDao.observePatternsWithItems() } returns flowOf(emptyList())

                val result = repository.observePatternsWithItems().first()

                assertThat(result).isEmpty()
            }

        @Test
        fun `maps nullable lastPurchasedAt correctly`() =
            runTest(testDispatcher) {
                val withPurchase =
                    createDaoPatternWithItem(
                        itemId = "1",
                        lastPurchasedAt = 1_700_000_000_000L,
                    )
                val withoutPurchase =
                    createDaoPatternWithItem(
                        itemId = "2",
                        lastPurchasedAt = null,
                    )
                every { patternDao.observePatternsWithItems() } returns flowOf(listOf(withPurchase, withoutPurchase))

                val result = repository.observePatternsWithItems().first()

                assertThat(result[0].lastPurchasedAt).isNotNull()
                assertThat(result[1].lastPurchasedAt).isNull()
            }
    }

    @Nested
    @DisplayName("getSuggestionReadyPatterns")
    inner class GetSuggestionReadyPatternsTests {
        @Test
        fun `returns mapped patterns with high confidence`() =
            runTest(testDispatcher) {
                val daoPatterns =
                    listOf(
                        createDaoPatternWithItem(itemId = "1", confidenceScore = 0.9),
                        createDaoPatternWithItem(itemId = "2", confidenceScore = 0.7),
                    )
                coEvery { patternDao.getHighConfidencePatterns(any(), any()) } returns daoPatterns

                val result = repository.getSuggestionReadyPatterns()

                assertThat(result).hasSize(2)
                assertThat(result[0].confidenceScore).isEqualTo(0.9)
                assertThat(result[1].confidenceScore).isEqualTo(0.7)
            }

        @Test
        fun `passes correct threshold values to DAO`() =
            runTest(testDispatcher) {
                coEvery { patternDao.getHighConfidencePatterns(any(), any()) } returns emptyList()

                repository.getSuggestionReadyPatterns()

                coVerify {
                    patternDao.getHighConfidencePatterns(
                        minConfidence = Pattern.SUGGESTION_CONFIDENCE_THRESHOLD,
                        minSamples = Pattern.MIN_PURCHASES,
                    )
                }
            }
    }

    @Nested
    @DisplayName("getPatternForItem")
    inner class GetPatternForItemTests {
        @Test
        fun `returns mapped domain model when pattern exists`() =
            runTest(testDispatcher) {
                val entity = createPatternEntity(itemId = "item-1", avgIntervalDays = 7.0)
                coEvery { patternDao.getPatternForItem("item-1") } returns entity

                val result = repository.getPatternForItem("item-1")

                assertThat(result).isNotNull()
                assertThat(result?.itemId).isEqualTo("item-1")
                assertThat(result?.avgIntervalDays).isEqualTo(7.0)
            }

        @Test
        fun `returns null when pattern does not exist`() =
            runTest(testDispatcher) {
                coEvery { patternDao.getPatternForItem("nonexistent") } returns null

                val result = repository.getPatternForItem("nonexistent")

                assertThat(result).isNull()
            }
    }

    @Nested
    @DisplayName("getAllPatterns")
    inner class GetAllPatternsTests {
        @Test
        fun `returns all patterns mapped to domain models`() =
            runTest(testDispatcher) {
                val entities =
                    listOf(
                        createPatternEntity(itemId = "1"),
                        createPatternEntity(itemId = "2"),
                        createPatternEntity(itemId = "3"),
                    )
                coEvery { patternDao.getAllPatterns() } returns entities

                val result = repository.getAllPatterns()

                assertThat(result).hasSize(3)
                assertThat(result.map { it.itemId }).containsExactly("1", "2", "3")
            }

        @Test
        fun `returns empty list when no patterns exist`() =
            runTest(testDispatcher) {
                coEvery { patternDao.getAllPatterns() } returns emptyList()

                val result = repository.getAllPatterns()

                assertThat(result).isEmpty()
            }
    }

    @Nested
    @DisplayName("savePattern")
    inner class SavePatternTests {
        @Test
        fun `returns success when upsert succeeds`() =
            runTest(testDispatcher) {
                val entitySlot = slot<PatternEntity>()
                coEvery { patternDao.upsert(capture(entitySlot)) } returns Unit
                val pattern = createDomainPattern(itemId = "item-1", avgIntervalDays = 7.0)

                val result = repository.savePattern(pattern)

                assertThat(result.isSuccess).isTrue()
                assertThat(entitySlot.captured.itemId).isEqualTo("item-1")
                assertThat(entitySlot.captured.avgIntervalDays).isEqualTo(7.0)
            }

        @Test
        fun `returns failure when upsert throws`() =
            runTest(testDispatcher) {
                coEvery { patternDao.upsert(any()) } throws RuntimeException("DB error")
                val pattern = createDomainPattern()

                val result = repository.savePattern(pattern)

                assertThat(result.isFailure).isTrue()
            }

        @Test
        fun `correctly maps domain instant to entity long`() =
            runTest(testDispatcher) {
                val entitySlot = slot<PatternEntity>()
                coEvery { patternDao.upsert(capture(entitySlot)) } returns Unit
                val timestamp = Instant.ofEpochMilli(1_700_000_000_000L)
                val pattern = createDomainPattern(lastCalculated = timestamp)

                repository.savePattern(pattern)

                assertThat(entitySlot.captured.lastCalculated).isEqualTo(1_700_000_000_000L)
            }

        @Test
        fun `correctly maps nullable lastSuggested`() =
            runTest(testDispatcher) {
                val entitySlot = slot<PatternEntity>()
                coEvery { patternDao.upsert(capture(entitySlot)) } returns Unit

                // With lastSuggested
                val patternWithSuggested =
                    createDomainPattern(
                        lastSuggested = Instant.ofEpochMilli(1_700_000_000_000L),
                    )
                repository.savePattern(patternWithSuggested)
                assertThat(entitySlot.captured.lastSuggested).isEqualTo(1_700_000_000_000L)

                // Without lastSuggested
                val patternWithoutSuggested = createDomainPattern(lastSuggested = null)
                repository.savePattern(patternWithoutSuggested)
                assertThat(entitySlot.captured.lastSuggested).isNull()
            }
    }

    @Nested
    @DisplayName("savePatterns")
    inner class SavePatternsTests {
        @Test
        fun `returns success when batch upsert succeeds`() =
            runTest(testDispatcher) {
                val entitiesSlot = slot<List<PatternEntity>>()
                coEvery { patternDao.upsertAll(capture(entitiesSlot)) } returns Unit
                val patterns =
                    listOf(
                        createDomainPattern(itemId = "1"),
                        createDomainPattern(itemId = "2"),
                    )

                val result = repository.savePatterns(patterns)

                assertThat(result.isSuccess).isTrue()
                assertThat(entitiesSlot.captured).hasSize(2)
                assertThat(entitiesSlot.captured.map { it.itemId }).containsExactly("1", "2")
            }

        @Test
        fun `returns success for empty list`() =
            runTest(testDispatcher) {
                coEvery { patternDao.upsertAll(emptyList()) } returns Unit

                val result = repository.savePatterns(emptyList())

                assertThat(result.isSuccess).isTrue()
            }

        @Test
        fun `returns failure when batch upsert throws`() =
            runTest(testDispatcher) {
                coEvery { patternDao.upsertAll(any()) } throws RuntimeException("DB error")

                val result = repository.savePatterns(listOf(createDomainPattern()))

                assertThat(result.isFailure).isTrue()
            }
    }

    @Nested
    @DisplayName("updateLastSuggested")
    inner class UpdateLastSuggestedTests {
        @Test
        fun `returns success when update succeeds`() =
            runTest(testDispatcher) {
                coEvery { patternDao.updateLastSuggested(any(), any()) } returns Unit

                val result = repository.updateLastSuggested("item-1")

                assertThat(result.isSuccess).isTrue()
                coVerify { patternDao.updateLastSuggested("item-1", any()) }
            }

        @Test
        fun `passes current timestamp to DAO`() =
            runTest(testDispatcher) {
                val timestampSlot = slot<Long>()
                coEvery { patternDao.updateLastSuggested(any(), capture(timestampSlot)) } returns Unit
                val before = Instant.now().toEpochMilli()

                repository.updateLastSuggested("item-1")

                val after = Instant.now().toEpochMilli()
                assertThat(timestampSlot.captured).isAtLeast(before)
                assertThat(timestampSlot.captured).isAtMost(after)
            }

        @Test
        fun `returns failure when update throws`() =
            runTest(testDispatcher) {
                coEvery { patternDao.updateLastSuggested(any(), any()) } throws RuntimeException("DB error")

                val result = repository.updateLastSuggested("item-1")

                assertThat(result.isFailure).isTrue()
            }
    }

    @Nested
    @DisplayName("deletePattern")
    inner class DeletePatternTests {
        @Test
        fun `returns success when delete succeeds`() =
            runTest(testDispatcher) {
                coEvery { patternDao.delete("item-1") } returns Unit

                val result = repository.deletePattern("item-1")

                assertThat(result.isSuccess).isTrue()
                coVerify { patternDao.delete("item-1") }
            }

        @Test
        fun `returns failure when delete throws`() =
            runTest(testDispatcher) {
                coEvery { patternDao.delete(any()) } throws RuntimeException("DB error")

                val result = repository.deletePattern("item-1")

                assertThat(result.isFailure).isTrue()
            }
    }

    // Helper functions
    private fun createPatternEntity(
        itemId: String = "test-item",
        avgIntervalDays: Double = 7.0,
        confidenceScore: Double = 0.85,
        sampleCount: Int = 5,
        lastCalculated: Long = 1_700_000_000_000L,
        lastSuggested: Long? = null,
    ) = PatternEntity(
        itemId = itemId,
        avgIntervalDays = avgIntervalDays,
        confidenceScore = confidenceScore,
        sampleCount = sampleCount,
        lastCalculated = lastCalculated,
        lastSuggested = lastSuggested,
    )

    private fun createDaoPatternWithItem(
        itemId: String = "test-item",
        itemName: String = "Test Item",
        avgIntervalDays: Double = 7.0,
        confidenceScore: Double = 0.85,
        sampleCount: Int = 5,
        lastCalculated: Long = 1_700_000_000_000L,
        lastSuggested: Long? = null,
        lastPurchasedAt: Long? = 1_700_000_000_000L,
    ) = PatternWithItemDao(
        itemId = itemId,
        itemName = itemName,
        avgIntervalDays = avgIntervalDays,
        confidenceScore = confidenceScore,
        sampleCount = sampleCount,
        lastCalculated = lastCalculated,
        lastSuggested = lastSuggested,
        lastPurchasedAt = lastPurchasedAt,
    )

    private fun createDomainPattern(
        itemId: String = "test-item",
        avgIntervalDays: Double = 7.0,
        confidenceScore: Double = 0.85,
        sampleCount: Int = 5,
        lastCalculated: Instant = Instant.ofEpochMilli(1_700_000_000_000L),
        lastSuggested: Instant? = null,
    ) = Pattern(
        itemId = itemId,
        avgIntervalDays = avgIntervalDays,
        confidenceScore = confidenceScore,
        sampleCount = sampleCount,
        lastCalculated = lastCalculated,
        lastSuggested = lastSuggested,
    )
}
