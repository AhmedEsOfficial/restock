package com.restock.core.data.repository

import com.google.common.truth.Truth.assertThat
import com.restock.core.data.database.dao.PurchaseDao
import com.restock.core.data.database.entity.PurchaseEntity
import com.restock.core.domain.model.Purchase
import com.restock.core.domain.model.PurchaseEvent
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

/**
 * Unit tests for [PurchaseRepositoryImpl].
 *
 * Tests verify correct mapping between domain models and entities,
 * proper delegation to DAOs, debounce logic, and error handling.
 */
class PurchaseRepositoryImplTest {
    private val purchaseDao: PurchaseDao = mockk(relaxed = true)
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: PurchaseRepositoryImpl

    @BeforeEach
    fun setup() {
        repository = PurchaseRepositoryImpl(purchaseDao, testDispatcher)
    }

    @Nested
    @DisplayName("observeAllPurchases")
    inner class ObserveAllPurchasesTests {
        @Test
        fun `returns flow of mapped domain models`() =
            runTest(testDispatcher) {
                val entities =
                    listOf(
                        createTestEntity(id = "1", itemId = "item-1"),
                        createTestEntity(id = "2", itemId = "item-2"),
                    )
                every { purchaseDao.observeAllPurchases() } returns flowOf(entities)

                val result = repository.observeAllPurchases().first()

                assertThat(result).hasSize(2)
                assertThat(result[0].id).isEqualTo("1")
                assertThat(result[1].id).isEqualTo("2")
            }

        @Test
        fun `returns empty list when no purchases`() =
            runTest(testDispatcher) {
                every { purchaseDao.observeAllPurchases() } returns flowOf(emptyList())

                val result = repository.observeAllPurchases().first()

                assertThat(result).isEmpty()
            }
    }

    @Nested
    @DisplayName("observeLatestPurchases")
    inner class ObserveLatestPurchasesTests {
        @Test
        fun `returns flow of latest purchases per item`() =
            runTest(testDispatcher) {
                val entities =
                    listOf(
                        createTestEntity(id = "latest-1", itemId = "item-1"),
                        createTestEntity(id = "latest-2", itemId = "item-2"),
                    )
                every { purchaseDao.observeLatestPurchases() } returns flowOf(entities)

                val result = repository.observeLatestPurchases().first()

                assertThat(result).hasSize(2)
                assertThat(result.map { it.id }).containsExactly("latest-1", "latest-2")
            }
    }

    @Nested
    @DisplayName("getPurchasesForItem")
    inner class GetPurchasesForItemTests {
        @Test
        fun `returns all purchases for specified item`() =
            runTest(testDispatcher) {
                val entities =
                    listOf(
                        createTestEntity(id = "1", itemId = "item-1"),
                        createTestEntity(id = "2", itemId = "item-1"),
                    )
                coEvery { purchaseDao.getPurchasesForItem("item-1") } returns entities

                val result = repository.getPurchasesForItem("item-1")

                assertThat(result).hasSize(2)
                assertThat(result.all { it.itemId == "item-1" }).isTrue()
            }

        @Test
        fun `returns empty list when no purchases for item`() =
            runTest(testDispatcher) {
                coEvery { purchaseDao.getPurchasesForItem("nonexistent") } returns emptyList()

                val result = repository.getPurchasesForItem("nonexistent")

                assertThat(result).isEmpty()
            }
    }

    @Nested
    @DisplayName("getRecentPurchase")
    inner class GetRecentPurchaseTests {
        @Test
        fun `returns purchase when exists within time window`() =
            runTest(testDispatcher) {
                val entity = createTestEntity(id = "recent", itemId = "item-1")
                coEvery { purchaseDao.getRecentPurchase("item-1", any()) } returns entity

                val result = repository.getRecentPurchase("item-1", withinHours = 4)

                assertThat(result).isNotNull()
                assertThat(result?.id).isEqualTo("recent")
            }

        @Test
        fun `returns null when no purchase within time window`() =
            runTest(testDispatcher) {
                coEvery { purchaseDao.getRecentPurchase("item-1", any()) } returns null

                val result = repository.getRecentPurchase("item-1", withinHours = 4)

                assertThat(result).isNull()
            }

        @Test
        fun `uses correct cutoff time for query`() =
            runTest(testDispatcher) {
                val timestampSlot = slot<Long>()
                coEvery {
                    purchaseDao.getRecentPurchase("item-1", capture(timestampSlot))
                } returns null

                val beforeCall = Instant.now()
                repository.getRecentPurchase("item-1", withinHours = 4)
                val afterCall = Instant.now()

                // Cutoff should be approximately 4 hours ago
                val expectedCutoffMin = beforeCall.minusSeconds(4 * 60 * 60).toEpochMilli()
                val expectedCutoffMax = afterCall.minusSeconds(4 * 60 * 60).toEpochMilli()
                assertThat(timestampSlot.captured).isAtLeast(expectedCutoffMin)
                assertThat(timestampSlot.captured).isAtMost(expectedCutoffMax)
            }
    }

    @Nested
    @DisplayName("getItemIdsWithPurchases")
    inner class GetItemIdsWithPurchasesTests {
        @Test
        fun `returns all item IDs with purchases`() =
            runTest(testDispatcher) {
                coEvery { purchaseDao.getItemIdsWithPurchases() } returns listOf("item-1", "item-2", "item-3")

                val result = repository.getItemIdsWithPurchases()

                assertThat(result).containsExactly("item-1", "item-2", "item-3")
            }

        @Test
        fun `returns empty list when no purchases`() =
            runTest(testDispatcher) {
                coEvery { purchaseDao.getItemIdsWithPurchases() } returns emptyList()

                val result = repository.getItemIdsWithPurchases()

                assertThat(result).isEmpty()
            }
    }

    @Nested
    @DisplayName("recordPurchaseEvent - debounce logic")
    inner class RecordPurchaseEventTests {
        @Test
        fun `creates new purchase when no recent purchase exists`() =
            runTest(testDispatcher) {
                coEvery { purchaseDao.getRecentPurchase(any(), any()) } returns null
                val insertedEntity = slot<PurchaseEntity>()
                coEvery { purchaseDao.insert(capture(insertedEntity)) } returns Unit

                val event = createTestEvent(itemId = "item-1", quantity = 2)
                val result = repository.recordPurchaseEvent(event)

                assertThat(result.isSuccess).isTrue()
                assertThat(insertedEntity.captured.itemId).isEqualTo("item-1")
                assertThat(insertedEntity.captured.quantity).isEqualTo(2)
                coVerify(exactly = 0) { purchaseDao.update(any()) }
            }

        @Test
        fun `updates existing purchase when recent purchase exists`() =
            runTest(testDispatcher) {
                val existingEntity =
                    createTestEntity(
                        id = "existing",
                        itemId = "item-1",
                        quantity = 1,
                    )
                coEvery { purchaseDao.getRecentPurchase("item-1", any()) } returns existingEntity
                val updatedEntity = slot<PurchaseEntity>()
                coEvery { purchaseDao.update(capture(updatedEntity)) } returns Unit

                val event = createTestEvent(itemId = "item-1", quantity = 2)
                val result = repository.recordPurchaseEvent(event)

                assertThat(result.isSuccess).isTrue()
                assertThat(result.getOrNull()?.quantity).isEqualTo(3) // 1 + 2
                assertThat(updatedEntity.captured.quantity).isEqualTo(3)
                coVerify(exactly = 0) { purchaseDao.insert(any()) }
            }

        @Test
        fun `returns the aggregated purchase on update`() =
            runTest(testDispatcher) {
                val existingEntity =
                    createTestEntity(
                        id = "existing",
                        itemId = "item-1",
                        quantity = 5,
                    )
                coEvery { purchaseDao.getRecentPurchase("item-1", any()) } returns existingEntity
                coEvery { purchaseDao.update(any()) } returns Unit

                val event = createTestEvent(itemId = "item-1", quantity = 3)
                val result = repository.recordPurchaseEvent(event)

                assertThat(result.isSuccess).isTrue()
                assertThat(result.getOrNull()?.id).isEqualTo("existing")
                assertThat(result.getOrNull()?.quantity).isEqualTo(8) // 5 + 3
            }

        @Test
        fun `returns failure when insert fails`() =
            runTest(testDispatcher) {
                coEvery { purchaseDao.getRecentPurchase(any(), any()) } returns null
                coEvery { purchaseDao.insert(any()) } throws RuntimeException("DB error")

                val event = createTestEvent(itemId = "item-1", quantity = 1)
                val result = repository.recordPurchaseEvent(event)

                assertThat(result.isFailure).isTrue()
            }

        @Test
        fun `returns failure when update fails`() =
            runTest(testDispatcher) {
                val existingEntity = createTestEntity(id = "existing", itemId = "item-1", quantity = 1)
                coEvery { purchaseDao.getRecentPurchase("item-1", any()) } returns existingEntity
                coEvery { purchaseDao.update(any()) } throws RuntimeException("DB error")

                val event = createTestEvent(itemId = "item-1", quantity = 2)
                val result = repository.recordPurchaseEvent(event)

                assertThat(result.isFailure).isTrue()
            }

        @Test
        fun `uses correct cutoff time from event timestamp`() =
            runTest(testDispatcher) {
                val eventTimestamp = Instant.parse("2024-01-15T12:00:00Z")
                val timestampSlot = slot<Long>()
                coEvery {
                    purchaseDao.getRecentPurchase("item-1", capture(timestampSlot))
                } returns null
                coEvery { purchaseDao.insert(any()) } returns Unit

                val event =
                    createTestEvent(
                        itemId = "item-1",
                        quantity = 1,
                        purchasedAt = eventTimestamp,
                    )
                repository.recordPurchaseEvent(event)

                // Cutoff should be 4 hours before event timestamp
                val expectedCutoff = eventTimestamp.minusSeconds(4 * 60 * 60).toEpochMilli()
                assertThat(timestampSlot.captured).isEqualTo(expectedCutoff)
            }
    }

    @Nested
    @DisplayName("recordPurchase")
    inner class RecordPurchaseTests {
        @Test
        fun `returns success when insert succeeds`() =
            runTest(testDispatcher) {
                coEvery { purchaseDao.insert(any()) } returns Unit
                val purchase = createTestDomainPurchase()

                val result = repository.recordPurchase(purchase)

                assertThat(result.isSuccess).isTrue()
                coVerify { purchaseDao.insert(any()) }
            }

        @Test
        fun `returns failure when insert throws`() =
            runTest(testDispatcher) {
                coEvery { purchaseDao.insert(any()) } throws RuntimeException("DB error")
                val purchase = createTestDomainPurchase()

                val result = repository.recordPurchase(purchase)

                assertThat(result.isFailure).isTrue()
            }
    }

    @Nested
    @DisplayName("updatePurchase")
    inner class UpdatePurchaseTests {
        @Test
        fun `returns success when update succeeds`() =
            runTest(testDispatcher) {
                coEvery { purchaseDao.update(any()) } returns Unit
                val purchase = createTestDomainPurchase()

                val result = repository.updatePurchase(purchase)

                assertThat(result.isSuccess).isTrue()
                coVerify { purchaseDao.update(any()) }
            }

        @Test
        fun `returns failure when update throws`() =
            runTest(testDispatcher) {
                coEvery { purchaseDao.update(any()) } throws RuntimeException("DB error")
                val purchase = createTestDomainPurchase()

                val result = repository.updatePurchase(purchase)

                assertThat(result.isFailure).isTrue()
            }
    }

    @Nested
    @DisplayName("getLastPurchaseForItem")
    inner class GetLastPurchaseForItemTests {
        @Test
        fun `returns last purchase when exists`() =
            runTest(testDispatcher) {
                val entity = createTestEntity(id = "last", itemId = "item-1")
                coEvery { purchaseDao.getLastPurchaseForItem("item-1") } returns entity

                val result = repository.getLastPurchaseForItem("item-1")

                assertThat(result).isNotNull()
                assertThat(result?.id).isEqualTo("last")
            }

        @Test
        fun `returns null when no purchases for item`() =
            runTest(testDispatcher) {
                coEvery { purchaseDao.getLastPurchaseForItem("nonexistent") } returns null

                val result = repository.getLastPurchaseForItem("nonexistent")

                assertThat(result).isNull()
            }
    }

    @Nested
    @DisplayName("getPurchaseCountForItem")
    inner class GetPurchaseCountForItemTests {
        @Test
        fun `returns correct count`() =
            runTest(testDispatcher) {
                coEvery { purchaseDao.getPurchaseCountForItem("item-1") } returns 5

                val result = repository.getPurchaseCountForItem("item-1")

                assertThat(result).isEqualTo(5)
            }

        @Test
        fun `returns zero when no purchases`() =
            runTest(testDispatcher) {
                coEvery { purchaseDao.getPurchaseCountForItem("nonexistent") } returns 0

                val result = repository.getPurchaseCountForItem("nonexistent")

                assertThat(result).isEqualTo(0)
            }
    }

    @Nested
    @DisplayName("deletePurchase")
    inner class DeletePurchaseTests {
        @Test
        fun `returns success when delete succeeds`() =
            runTest(testDispatcher) {
                coEvery { purchaseDao.delete("test-id") } returns Unit

                val result = repository.deletePurchase("test-id")

                assertThat(result.isSuccess).isTrue()
                coVerify { purchaseDao.delete("test-id") }
            }
    }

    @Nested
    @DisplayName("deletePurchasesForItem")
    inner class DeletePurchasesForItemTests {
        @Test
        fun `returns success when delete succeeds`() =
            runTest(testDispatcher) {
                coEvery { purchaseDao.deleteForItem("item-1") } returns Unit

                val result = repository.deletePurchasesForItem("item-1")

                assertThat(result.isSuccess).isTrue()
                coVerify { purchaseDao.deleteForItem("item-1") }
            }
    }

    // Helper functions
    private fun createTestEntity(
        id: String = "test-id",
        itemId: String = "item-id",
        purchasedAt: Long = 1_700_000_000_000L,
        quantity: Int = 1,
        priceCents: Int? = null,
        storeId: String? = null,
    ) = PurchaseEntity(
        id = id,
        itemId = itemId,
        purchasedAt = purchasedAt,
        quantity = quantity,
        priceCents = priceCents,
        storeId = storeId,
    )

    private fun createTestEvent(
        id: String = "event-id",
        itemId: String = "item-id",
        purchasedAt: Instant = Instant.now(),
        quantity: Int = 1,
        priceCents: Int? = null,
        storeId: String? = null,
    ) = PurchaseEvent(
        id = id,
        itemId = itemId,
        purchasedAt = purchasedAt,
        quantity = quantity,
        priceCents = priceCents,
        storeId = storeId,
    )

    private fun createTestDomainPurchase(
        id: String = "test-id",
        itemId: String = "item-id",
        purchasedAt: Instant = Instant.ofEpochMilli(1_700_000_000_000L),
        quantity: Int = 1,
        priceCents: Int? = null,
        storeId: String? = null,
    ) = Purchase(
        id = id,
        itemId = itemId,
        purchasedAt = purchasedAt,
        quantity = quantity,
        priceCents = priceCents,
        storeId = storeId,
    )
}
