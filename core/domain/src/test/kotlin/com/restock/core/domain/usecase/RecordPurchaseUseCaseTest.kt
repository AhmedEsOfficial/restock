package com.restock.core.domain.usecase

import com.google.common.truth.Truth.assertThat
import com.restock.core.domain.model.Purchase
import com.restock.core.domain.model.PurchaseEvent
import com.restock.core.domain.repository.PurchaseRepository
import com.restock.core.domain.repository.SuggestionRepository
import com.restock.core.domain.scheduler.PatternWorkScheduler
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.Instant
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest

/**
 * Unit tests for [RecordPurchaseUseCase].
 */
@OptIn(ExperimentalCoroutinesApi::class)
class RecordPurchaseUseCaseTest {
    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)
    private val purchaseRepository: PurchaseRepository = mockk()
    private val suggestionRepository: SuggestionRepository = mockk(relaxed = true)
    private val patternWorkScheduler: PatternWorkScheduler = mockk(relaxed = true)
    private lateinit var useCase: RecordPurchaseUseCase

    @BeforeEach
    fun setup() {
        useCase =
            RecordPurchaseUseCase(
                purchaseRepository = purchaseRepository,
                suggestionRepository = suggestionRepository,
                patternWorkScheduler = patternWorkScheduler,
                scope = testScope,
            )
    }

    @Nested
    @DisplayName("Recording purchases")
    inner class RecordPurchaseTests {
        @Test
        fun `creates purchase event with correct item id and quantity`() =
            testScope.runTest {
                val eventSlot = slot<PurchaseEvent>()
                val purchase = createTestPurchase(itemId = "item-1", quantity = 3)
                coEvery { purchaseRepository.recordPurchaseEvent(capture(eventSlot)) } returns
                    Result.success(purchase)

                useCase(itemId = "item-1", quantity = 3)
                advanceUntilIdle()

                assertThat(eventSlot.captured.itemId).isEqualTo("item-1")
                assertThat(eventSlot.captured.quantity).isEqualTo(3)
            }

        @Test
        fun `sets current timestamp on purchase event`() =
            testScope.runTest {
                val eventSlot = slot<PurchaseEvent>()
                val purchase = createTestPurchase(itemId = "item-1")
                coEvery { purchaseRepository.recordPurchaseEvent(capture(eventSlot)) } returns
                    Result.success(purchase)
                val beforeInvoke = Instant.now()

                useCase(itemId = "item-1", quantity = 1)
                advanceUntilIdle()

                val afterInvoke = Instant.now()
                assertThat(eventSlot.captured.purchasedAt).isAtLeast(beforeInvoke)
                assertThat(eventSlot.captured.purchasedAt).isAtMost(afterInvoke)
            }

        @Test
        fun `generates unique id for each purchase event`() =
            testScope.runTest {
                val events = mutableListOf<PurchaseEvent>()
                val purchase = createTestPurchase(itemId = "item-1")
                coEvery { purchaseRepository.recordPurchaseEvent(capture(events)) } returns
                    Result.success(purchase)

                useCase(itemId = "item-1", quantity = 1)
                useCase(itemId = "item-1", quantity = 2)
                advanceUntilIdle()

                assertThat(events[0].id).isNotEqualTo(events[1].id)
            }

        @Test
        fun `calls recordPurchaseEvent on repository`() =
            testScope.runTest {
                val purchase = createTestPurchase(itemId = "item-1")
                coEvery { purchaseRepository.recordPurchaseEvent(any()) } returns
                    Result.success(purchase)

                useCase(itemId = "item-1", quantity = 1)
                advanceUntilIdle()

                coVerify { purchaseRepository.recordPurchaseEvent(any()) }
            }
    }

    @Nested
    @DisplayName("Pattern calculation trigger")
    inner class PatternTriggerTests {
        @Test
        fun `triggers pattern calculation on success`() =
            testScope.runTest {
                val purchase = createTestPurchase(itemId = "item-1")
                coEvery { purchaseRepository.recordPurchaseEvent(any()) } returns
                    Result.success(purchase)

                useCase(itemId = "item-1", quantity = 1)
                advanceUntilIdle()

                verify { patternWorkScheduler.triggerImmediateCalculation() }
            }

        @Test
        fun `does not trigger pattern calculation on failure`() =
            testScope.runTest {
                coEvery { purchaseRepository.recordPurchaseEvent(any()) } returns
                    Result.failure(RuntimeException("DB error"))

                useCase(itemId = "item-1", quantity = 1)
                advanceUntilIdle()

                verify(exactly = 0) { patternWorkScheduler.triggerImmediateCalculation() }
            }
    }

    @Nested
    @DisplayName("Dismissal clearing")
    inner class DismissalClearingTests {
        @Test
        fun `clears dismissal on purchase success`() =
            testScope.runTest {
                val purchase = createTestPurchase(itemId = "item-1")
                coEvery { purchaseRepository.recordPurchaseEvent(any()) } returns
                    Result.success(purchase)

                useCase(itemId = "item-1", quantity = 1)
                advanceUntilIdle()

                coVerify { suggestionRepository.clearDismissalOnPurchase("item-1") }
            }

        @Test
        fun `does not clear dismissal on purchase failure`() =
            testScope.runTest {
                coEvery { purchaseRepository.recordPurchaseEvent(any()) } returns
                    Result.failure(RuntimeException("DB error"))

                useCase(itemId = "item-1", quantity = 1)
                advanceUntilIdle()

                coVerify(exactly = 0) { suggestionRepository.clearDismissalOnPurchase(any()) }
            }

        @Test
        fun `clears correct item dismissal when custom timestamp used`() =
            testScope.runTest {
                val customTimestamp = Instant.parse("2024-01-15T10:30:00Z")
                val purchase = createTestPurchase(itemId = "item-xyz", purchasedAt = customTimestamp)
                coEvery { purchaseRepository.recordPurchaseEvent(any()) } returns
                    Result.success(purchase)

                useCase(itemId = "item-xyz", quantity = 2, purchasedAt = customTimestamp)
                advanceUntilIdle()

                coVerify { suggestionRepository.clearDismissalOnPurchase("item-xyz") }
            }

        @Test
        fun `clears dismissals for multiple rapid purchases`() =
            testScope.runTest {
                val purchase = createTestPurchase(itemId = "item-1")
                coEvery { purchaseRepository.recordPurchaseEvent(any()) } returns
                    Result.success(purchase)

                repeat(3) { index ->
                    useCase(itemId = "item-$index", quantity = 1)
                }
                advanceUntilIdle()

                coVerify { suggestionRepository.clearDismissalOnPurchase("item-0") }
                coVerify { suggestionRepository.clearDismissalOnPurchase("item-1") }
                coVerify { suggestionRepository.clearDismissalOnPurchase("item-2") }
            }
    }

    @Nested
    @DisplayName("Custom timestamp")
    inner class CustomTimestampTests {
        @Test
        fun `uses provided timestamp when specified`() =
            testScope.runTest {
                val eventSlot = slot<PurchaseEvent>()
                val customTimestamp = Instant.parse("2024-01-15T10:30:00Z")
                val purchase = createTestPurchase(itemId = "item-1", purchasedAt = customTimestamp)
                coEvery { purchaseRepository.recordPurchaseEvent(capture(eventSlot)) } returns
                    Result.success(purchase)

                useCase(itemId = "item-1", quantity = 2, purchasedAt = customTimestamp)
                advanceUntilIdle()

                assertThat(eventSlot.captured.purchasedAt).isEqualTo(customTimestamp)
            }
    }

    @Nested
    @DisplayName("Error handling")
    inner class ErrorHandlingTests {
        @Test
        fun `continues execution on repository failure`() =
            testScope.runTest {
                // Recording fails but should not throw
                coEvery { purchaseRepository.recordPurchaseEvent(any()) } returns
                    Result.failure(RuntimeException("DB error"))

                // Should not throw exception
                useCase(itemId = "item-1", quantity = 1)
                advanceUntilIdle()

                // Test passes if no exception was thrown
            }

        @Test
        fun `handles multiple rapid calls`() =
            testScope.runTest {
                val purchase = createTestPurchase(itemId = "item-1")
                coEvery { purchaseRepository.recordPurchaseEvent(any()) } returns
                    Result.success(purchase)

                // Multiple rapid calls
                repeat(5) {
                    useCase(itemId = "item-$it", quantity = 1)
                }
                advanceUntilIdle()

                coVerify(exactly = 5) { purchaseRepository.recordPurchaseEvent(any()) }
            }
    }

    // Helper function
    private fun createTestPurchase(
        id: String = "purchase-id",
        itemId: String = "item-id",
        purchasedAt: Instant = Instant.now(),
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
