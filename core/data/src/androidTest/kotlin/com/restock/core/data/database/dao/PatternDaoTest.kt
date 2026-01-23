package com.restock.core.data.database.dao

import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.restock.core.data.database.entity.PatternEntity
import com.restock.core.data.database.entity.PurchaseEntity
import org.junit.Test
import org.junit.runner.RunWith
import kotlinx.coroutines.test.runTest

/**
 * Instrumented tests for [PatternDao].
 *
 * Tests CRUD operations, pattern queries with confidence thresholds,
 * and Flow-based observations with item details.
 */
@RunWith(AndroidJUnit4::class)
class PatternDaoTest : BaseDaoTest() {
    private fun createTestPattern(
        itemId: String = TEST_ITEM_ID,
        avgIntervalDays: Double = 7.0,
        confidenceScore: Double = 0.8,
        sampleCount: Int = 5,
        lastCalculated: Long = TEST_CREATED_AT,
        lastSuggested: Long? = null,
    ) = PatternEntity(
        itemId = itemId,
        avgIntervalDays = avgIntervalDays,
        confidenceScore = confidenceScore,
        sampleCount = sampleCount,
        lastCalculated = lastCalculated,
        lastSuggested = lastSuggested,
    )

    private fun createTestPurchase(
        id: String,
        itemId: String = TEST_ITEM_ID,
        purchasedAt: Long,
    ) = PurchaseEntity(
        id = id,
        itemId = itemId,
        purchasedAt = purchasedAt,
        quantity = 1,
    )

    // region Insert/Upsert Tests

    @Test
    fun upsert_inserts_new_pattern() =
        runTest {
            itemDao.insert(createTestItem())
            val pattern = createTestPattern()

            patternDao.upsert(pattern)
            val retrieved = patternDao.getPatternForItem(TEST_ITEM_ID)

            assertThat(retrieved).isNotNull()
            assertThat(retrieved?.avgIntervalDays).isEqualTo(7.0)
            assertThat(retrieved?.confidenceScore).isEqualTo(0.8)
        }

    @Test
    fun upsert_updates_existing_pattern() =
        runTest {
            itemDao.insert(createTestItem())
            patternDao.upsert(createTestPattern(confidenceScore = 0.5))

            patternDao.upsert(createTestPattern(confidenceScore = 0.9))
            val retrieved = patternDao.getPatternForItem(TEST_ITEM_ID)

            assertThat(retrieved?.confidenceScore).isEqualTo(0.9)
        }

    @Test
    fun upsertAll_inserts_multiple_patterns() =
        runTest {
            itemDao.insert(createTestItem(id = "item-1", normalizedName = "item1"))
            itemDao.insert(createTestItem(id = "item-2", normalizedName = "item2"))

            val patterns =
                listOf(
                    createTestPattern(itemId = "item-1"),
                    createTestPattern(itemId = "item-2"),
                )

            patternDao.upsertAll(patterns)
            val count = patternDao.getCount()

            assertThat(count).isEqualTo(2)
        }

    // endregion

    // region Query Tests

    @Test
    fun getPatternForItem_returns_null_when_not_found() =
        runTest {
            val pattern = patternDao.getPatternForItem("nonexistent")

            assertThat(pattern).isNull()
        }

    @Test
    fun getAllPatterns_sorted_by_confidence_descending() =
        runTest {
            itemDao.insert(createTestItem(id = "item-1", normalizedName = "item1"))
            itemDao.insert(createTestItem(id = "item-2", normalizedName = "item2"))
            itemDao.insert(createTestItem(id = "item-3", normalizedName = "item3"))

            patternDao.upsert(createTestPattern(itemId = "item-1", confidenceScore = 0.5))
            patternDao.upsert(createTestPattern(itemId = "item-2", confidenceScore = 0.9))
            patternDao.upsert(createTestPattern(itemId = "item-3", confidenceScore = 0.7))

            val patterns = patternDao.getAllPatterns()

            assertThat(patterns.map { it.confidenceScore }).containsExactly(0.9, 0.7, 0.5).inOrder()
        }

    @Test
    fun getHighConfidencePatterns_filters_by_confidence_and_samples() =
        runTest {
            itemDao.insert(createTestItem(id = "item-1", name = "High Confidence", normalizedName = "item1"))
            itemDao.insert(createTestItem(id = "item-2", name = "Low Confidence", normalizedName = "item2"))
            itemDao.insert(createTestItem(id = "item-3", name = "Low Samples", normalizedName = "item3"))
            itemDao.insert(createTestItem(id = "item-4", name = "Inactive", normalizedName = "item4", isActive = false))

            // High confidence, enough samples (eligible)
            patternDao.upsert(createTestPattern(itemId = "item-1", confidenceScore = 0.8, sampleCount = 5))
            // Low confidence (not eligible)
            patternDao.upsert(createTestPattern(itemId = "item-2", confidenceScore = 0.4, sampleCount = 5))
            // Too few samples (not eligible)
            patternDao.upsert(createTestPattern(itemId = "item-3", confidenceScore = 0.8, sampleCount = 2))
            // Item is inactive (not eligible)
            patternDao.upsert(createTestPattern(itemId = "item-4", confidenceScore = 0.9, sampleCount = 10))

            val patterns = patternDao.getHighConfidencePatterns(minConfidence = 0.6, minSamples = 3)

            assertThat(patterns).hasSize(1)
            assertThat(patterns[0].itemName).isEqualTo("High Confidence")
        }

    @Test
    fun getHighConfidencePatterns_includes_last_purchase_date() =
        runTest {
            itemDao.insert(createTestItem())
            patternDao.upsert(createTestPattern())

            // Add purchases
            purchaseDao.insert(createTestPurchase(id = "p1", purchasedAt = 1000L))
            purchaseDao.insert(createTestPurchase(id = "p2", purchasedAt = 3000L))
            purchaseDao.insert(createTestPurchase(id = "p3", purchasedAt = 2000L))

            val patterns = patternDao.getHighConfidencePatterns()

            assertThat(patterns).hasSize(1)
            assertThat(patterns[0].lastPurchasedAt).isEqualTo(3000L) // MAX(purchased_at)
        }

    // endregion

    // region Update Tests

    @Test
    fun updateLastSuggested_sets_timestamp() =
        runTest {
            itemDao.insert(createTestItem())
            patternDao.upsert(createTestPattern(lastSuggested = null))

            val suggestedTime = 9999L
            patternDao.updateLastSuggested(TEST_ITEM_ID, suggestedTime)

            val pattern = patternDao.getPatternForItem(TEST_ITEM_ID)
            assertThat(pattern?.lastSuggested).isEqualTo(suggestedTime)
        }

    // endregion

    // region Delete Tests

    @Test
    fun delete_removes_pattern() =
        runTest {
            itemDao.insert(createTestItem())
            patternDao.upsert(createTestPattern())

            patternDao.delete(TEST_ITEM_ID)

            val pattern = patternDao.getPatternForItem(TEST_ITEM_ID)
            assertThat(pattern).isNull()
        }

    // endregion

    // region Count Tests

    @Test
    fun getCount_returns_total_patterns() =
        runTest {
            itemDao.insert(createTestItem(id = "item-1", normalizedName = "item1"))
            itemDao.insert(createTestItem(id = "item-2", normalizedName = "item2"))

            patternDao.upsert(createTestPattern(itemId = "item-1"))
            patternDao.upsert(createTestPattern(itemId = "item-2"))

            val count = patternDao.getCount()
            assertThat(count).isEqualTo(2)
        }

    // endregion

    // region Flow Tests

    @Test
    fun observePatternsWithItems_emits_patterns_with_details() =
        runTest {
            itemDao.insert(createTestItem(name = "Milk"))

            patternDao.observePatternsWithItems().test {
                assertThat(awaitItem()).isEmpty()

                patternDao.upsert(createTestPattern())
                val patterns = awaitItem()

                assertThat(patterns).hasSize(1)
                assertThat(patterns[0].itemName).isEqualTo("Milk")
                assertThat(patterns[0].avgIntervalDays).isEqualTo(7.0)
                assertThat(patterns[0].confidenceScore).isEqualTo(0.8)

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun observePatternsWithItems_sorted_by_confidence_descending() =
        runTest {
            itemDao.insert(createTestItem(id = "item-1", name = "Low", normalizedName = "item1"))
            itemDao.insert(createTestItem(id = "item-2", name = "High", normalizedName = "item2"))

            patternDao.upsert(createTestPattern(itemId = "item-1", confidenceScore = 0.5))
            patternDao.upsert(createTestPattern(itemId = "item-2", confidenceScore = 0.9))

            patternDao.observePatternsWithItems().test {
                val patterns = awaitItem()

                assertThat(patterns[0].itemName).isEqualTo("High")
                assertThat(patterns[1].itemName).isEqualTo("Low")

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun observePatternsWithItems_excludes_inactive_items() =
        runTest {
            itemDao.insert(createTestItem(id = "item-1", name = "Active", normalizedName = "item1", isActive = true))
            itemDao.insert(createTestItem(id = "item-2", name = "Inactive", normalizedName = "item2", isActive = false))

            patternDao.upsert(createTestPattern(itemId = "item-1"))
            patternDao.upsert(createTestPattern(itemId = "item-2"))

            patternDao.observePatternsWithItems().test {
                val patterns = awaitItem()

                assertThat(patterns).hasSize(1)
                assertThat(patterns[0].itemName).isEqualTo("Active")

                cancelAndIgnoreRemainingEvents()
            }
        }

    // endregion

    // region Foreign Key Tests

    @Test
    fun cascade_delete_removes_patterns_when_item_deleted() =
        runTest {
            itemDao.insert(createTestItem())
            patternDao.upsert(createTestPattern())

            assertThat(patternDao.getCount()).isEqualTo(1)

            itemDao.hardDelete(TEST_ITEM_ID)

            assertThat(patternDao.getCount()).isEqualTo(0)
        }

    // endregion

    // region Edge Cases

    @Test
    fun pattern_with_high_precision_values() =
        runTest {
            itemDao.insert(createTestItem())

            val pattern =
                createTestPattern(
                    avgIntervalDays = 7.142857142857143,
                    confidenceScore = 0.8571428571428571,
                )
            patternDao.upsert(pattern)

            val retrieved = patternDao.getPatternForItem(TEST_ITEM_ID)
            assertThat(retrieved?.avgIntervalDays).isWithin(0.0001).of(7.142857142857143)
            assertThat(retrieved?.confidenceScore).isWithin(0.0001).of(0.8571428571428571)
        }

    @Test
    fun pattern_last_suggested_nullable() =
        runTest {
            itemDao.insert(createTestItem())

            // Initially null
            patternDao.upsert(createTestPattern(lastSuggested = null))
            var pattern = patternDao.getPatternForItem(TEST_ITEM_ID)
            assertThat(pattern?.lastSuggested).isNull()

            // Set to value
            patternDao.updateLastSuggested(TEST_ITEM_ID, 12345L)
            pattern = patternDao.getPatternForItem(TEST_ITEM_ID)
            assertThat(pattern?.lastSuggested).isEqualTo(12345L)
        }

    // endregion
}
