package com.restock.core.domain.usecase

import com.google.common.truth.Truth.assertThat
import com.restock.core.domain.model.CurrentListItem
import com.restock.core.domain.model.PositionUpdate
import com.restock.core.domain.repository.CurrentListRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.Instant
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest

/**
 * Unit tests for [ReorderItemsUseCase].
 */
class ReorderItemsUseCaseTest {
    private val currentListRepository: CurrentListRepository = mockk()
    private lateinit var useCase: ReorderItemsUseCase

    @BeforeEach
    fun setup() {
        useCase = ReorderItemsUseCase(currentListRepository)
    }

    @Nested
    @DisplayName("No-op cases")
    inner class NoOpTests {
        @Test
        fun `returns success when fromIndex equals toIndex`() =
            runTest {
                val result = useCase(fromIndex = 2, toIndex = 2)

                assertThat(result.isSuccess).isTrue()
                coVerify(exactly = 0) { currentListRepository.updatePositions(any()) }
            }
    }

    @Nested
    @DisplayName("Index validation")
    inner class IndexValidationTests {
        @Test
        fun `returns failure when fromIndex is negative`() =
            runTest {
                val items = createOrderedList(3)
                coEvery { currentListRepository.observeUncheckedItems() } returns flowOf(items)

                val result = useCase(fromIndex = -1, toIndex = 1)

                assertThat(result.isFailure).isTrue()
                assertThat(result.exceptionOrNull())
                    .isInstanceOf(IndexOutOfBoundsException::class.java)
            }

        @Test
        fun `returns failure when toIndex is negative`() =
            runTest {
                val items = createOrderedList(3)
                coEvery { currentListRepository.observeUncheckedItems() } returns flowOf(items)

                val result = useCase(fromIndex = 1, toIndex = -1)

                assertThat(result.isFailure).isTrue()
                assertThat(result.exceptionOrNull())
                    .isInstanceOf(IndexOutOfBoundsException::class.java)
            }

        @Test
        fun `returns failure when fromIndex exceeds list size`() =
            runTest {
                val items = createOrderedList(3)
                coEvery { currentListRepository.observeUncheckedItems() } returns flowOf(items)

                val result = useCase(fromIndex = 5, toIndex = 1)

                assertThat(result.isFailure).isTrue()
                assertThat(result.exceptionOrNull())
                    .isInstanceOf(IndexOutOfBoundsException::class.java)
            }

        @Test
        fun `returns failure when toIndex exceeds list size`() =
            runTest {
                val items = createOrderedList(3)
                coEvery { currentListRepository.observeUncheckedItems() } returns flowOf(items)

                val result = useCase(fromIndex = 1, toIndex = 5)

                assertThat(result.isFailure).isTrue()
                assertThat(result.exceptionOrNull())
                    .isInstanceOf(IndexOutOfBoundsException::class.java)
            }
    }

    @Nested
    @DisplayName("Moving items down")
    inner class MoveDownTests {
        @Test
        fun `moves item from 0 to 2 correctly`() =
            runTest {
                // Items at positions 0, 1, 2
                val items = createOrderedList(3)
                val updatesSlot = slot<List<PositionUpdate>>()

                coEvery { currentListRepository.observeUncheckedItems() } returns flowOf(items)
                coEvery { currentListRepository.updatePositions(capture(updatesSlot)) } returns Result.success(Unit)

                val result = useCase(fromIndex = 0, toIndex = 2)

                assertThat(result.isSuccess).isTrue()
                val updates = updatesSlot.captured

                // Items 1 and 2 should move up, item 0 should go to position 2
                assertThat(updates).hasSize(3)
                assertThat(updates).contains(PositionUpdate("item-1", 0))
                assertThat(updates).contains(PositionUpdate("item-2", 1))
                assertThat(updates).contains(PositionUpdate("item-0", 2))
            }

        @Test
        fun `moves item from 1 to 3 correctly in 5-item list`() =
            runTest {
                val items = createOrderedList(5)
                val updatesSlot = slot<List<PositionUpdate>>()

                coEvery { currentListRepository.observeUncheckedItems() } returns flowOf(items)
                coEvery { currentListRepository.updatePositions(capture(updatesSlot)) } returns Result.success(Unit)

                val result = useCase(fromIndex = 1, toIndex = 3)

                assertThat(result.isSuccess).isTrue()
                val updates = updatesSlot.captured

                // Items 2 and 3 move up, item 1 goes to position 3
                assertThat(updates).hasSize(3)
                assertThat(updates).contains(PositionUpdate("item-2", 1))
                assertThat(updates).contains(PositionUpdate("item-3", 2))
                assertThat(updates).contains(PositionUpdate("item-1", 3))
            }
    }

    @Nested
    @DisplayName("Moving items up")
    inner class MoveUpTests {
        @Test
        fun `moves item from 2 to 0 correctly`() =
            runTest {
                val items = createOrderedList(3)
                val updatesSlot = slot<List<PositionUpdate>>()

                coEvery { currentListRepository.observeUncheckedItems() } returns flowOf(items)
                coEvery { currentListRepository.updatePositions(capture(updatesSlot)) } returns Result.success(Unit)

                val result = useCase(fromIndex = 2, toIndex = 0)

                assertThat(result.isSuccess).isTrue()
                val updates = updatesSlot.captured

                // Items 0 and 1 should move down, item 2 should go to position 0
                assertThat(updates).hasSize(3)
                assertThat(updates).contains(PositionUpdate("item-0", 1))
                assertThat(updates).contains(PositionUpdate("item-1", 2))
                assertThat(updates).contains(PositionUpdate("item-2", 0))
            }

        @Test
        fun `moves item from 3 to 1 correctly in 5-item list`() =
            runTest {
                val items = createOrderedList(5)
                val updatesSlot = slot<List<PositionUpdate>>()

                coEvery { currentListRepository.observeUncheckedItems() } returns flowOf(items)
                coEvery { currentListRepository.updatePositions(capture(updatesSlot)) } returns Result.success(Unit)

                val result = useCase(fromIndex = 3, toIndex = 1)

                assertThat(result.isSuccess).isTrue()
                val updates = updatesSlot.captured

                // Items 1 and 2 move down, item 3 goes to position 1
                assertThat(updates).hasSize(3)
                assertThat(updates).contains(PositionUpdate("item-1", 2))
                assertThat(updates).contains(PositionUpdate("item-2", 3))
                assertThat(updates).contains(PositionUpdate("item-3", 1))
            }
    }

    @Nested
    @DisplayName("Adjacent moves")
    inner class AdjacentMoveTests {
        @Test
        fun `moves item from 0 to 1 correctly`() =
            runTest {
                val items = createOrderedList(3)
                val updatesSlot = slot<List<PositionUpdate>>()

                coEvery { currentListRepository.observeUncheckedItems() } returns flowOf(items)
                coEvery { currentListRepository.updatePositions(capture(updatesSlot)) } returns Result.success(Unit)

                val result = useCase(fromIndex = 0, toIndex = 1)

                assertThat(result.isSuccess).isTrue()
                val updates = updatesSlot.captured

                assertThat(updates).hasSize(2)
                assertThat(updates).contains(PositionUpdate("item-1", 0))
                assertThat(updates).contains(PositionUpdate("item-0", 1))
            }

        @Test
        fun `moves item from 1 to 0 correctly`() =
            runTest {
                val items = createOrderedList(3)
                val updatesSlot = slot<List<PositionUpdate>>()

                coEvery { currentListRepository.observeUncheckedItems() } returns flowOf(items)
                coEvery { currentListRepository.updatePositions(capture(updatesSlot)) } returns Result.success(Unit)

                val result = useCase(fromIndex = 1, toIndex = 0)

                assertThat(result.isSuccess).isTrue()
                val updates = updatesSlot.captured

                assertThat(updates).hasSize(2)
                assertThat(updates).contains(PositionUpdate("item-0", 1))
                assertThat(updates).contains(PositionUpdate("item-1", 0))
            }
    }

    @Nested
    @DisplayName("Error handling")
    inner class ErrorHandlingTests {
        @Test
        fun `returns failure when updatePositions fails`() =
            runTest {
                val items = createOrderedList(3)
                coEvery { currentListRepository.observeUncheckedItems() } returns flowOf(items)
                coEvery { currentListRepository.updatePositions(any()) } returns
                    Result.failure(RuntimeException("DB error"))

                val result = useCase(fromIndex = 0, toIndex = 2)

                assertThat(result.isFailure).isTrue()
            }
    }

    // Helper functions
    private fun createOrderedList(count: Int): List<CurrentListItem> =
        (0 until count).map { index ->
            CurrentListItem(
                id = "item-$index",
                itemId = "grocery-$index",
                name = "Item $index",
                normalizedName = "item $index",
                quantity = 1,
                unit = null,
                addedAt = Instant.now(),
                checkedAt = null,
                position = index,
                isChecked = false,
            )
        }
}
