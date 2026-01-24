package com.restock.core.domain.usecase

import com.google.common.truth.Truth.assertThat
import com.restock.core.domain.model.CurrentListItem
import com.restock.core.domain.repository.CurrentListRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.Instant
import kotlinx.coroutines.test.runTest

/**
 * Unit tests for [DeleteItemUseCase].
 */
class DeleteItemUseCaseTest {
    private val currentListRepository: CurrentListRepository = mockk()
    private lateinit var useCase: DeleteItemUseCase

    @BeforeEach
    fun setup() {
        useCase = DeleteItemUseCase(currentListRepository)
    }

    @Nested
    @DisplayName("Deleting items")
    inner class DeleteItemTests {
        @Test
        fun `returns DeletedItem with correct data on success`() =
            runTest {
                val listItem =
                    createTestListItem(
                        id = "list-1",
                        itemId = "item-1",
                        name = "Milk",
                        quantity = 2,
                        unit = "gal",
                        position = 3,
                    )
                coEvery { currentListRepository.getById("list-1") } returns listItem
                coEvery { currentListRepository.removeItem("list-1") } returns Result.success(Unit)

                val result = useCase("list-1")

                assertThat(result.isSuccess).isTrue()
                val deletedItem = result.getOrNull()!!
                assertThat(deletedItem.listItemId).isEqualTo("list-1")
                assertThat(deletedItem.itemId).isEqualTo("item-1")
                assertThat(deletedItem.name).isEqualTo("Milk")
                assertThat(deletedItem.quantity).isEqualTo(2)
                assertThat(deletedItem.unit).isEqualTo("gal")
                assertThat(deletedItem.position).isEqualTo(3)
            }

        @Test
        fun `returns failure when item not found`() =
            runTest {
                coEvery { currentListRepository.getById("nonexistent") } returns null

                val result = useCase("nonexistent")

                assertThat(result.isFailure).isTrue()
                assertThat(result.exceptionOrNull())
                    .isInstanceOf(IllegalArgumentException::class.java)
                assertThat(result.exceptionOrNull()?.message)
                    .contains("not found")
            }

        @Test
        fun `returns failure when removeItem fails`() =
            runTest {
                val listItem = createTestListItem(id = "list-1")
                coEvery { currentListRepository.getById("list-1") } returns listItem
                coEvery { currentListRepository.removeItem("list-1") } returns
                    Result.failure(RuntimeException("DB error"))

                val result = useCase("list-1")

                assertThat(result.isFailure).isTrue()
            }

        @Test
        fun `preserves null unit in DeletedItem`() =
            runTest {
                val listItem = createTestListItem(id = "list-1", unit = null)
                coEvery { currentListRepository.getById("list-1") } returns listItem
                coEvery { currentListRepository.removeItem("list-1") } returns Result.success(Unit)

                val result = useCase("list-1")

                assertThat(result.isSuccess).isTrue()
                assertThat(result.getOrNull()?.unit).isNull()
            }
    }

    @Nested
    @DisplayName("Undo delete")
    inner class UndoDeleteTests {
        @Test
        fun `restores item at original position`() =
            runTest {
                val deletedItem =
                    DeletedItem(
                        listItemId = "list-1",
                        itemId = "item-1",
                        name = "Milk",
                        quantity = 2,
                        unit = "gal",
                        position = 3,
                    )
                val restoredItem =
                    createTestListItem(
                        id = "list-2",
                        itemId = "item-1",
                        name = "Milk",
                        quantity = 2,
                        unit = "gal",
                        position = 3,
                    )
                coEvery {
                    currentListRepository.addItemAt(
                        itemId = "item-1",
                        position = 3,
                        quantity = 2,
                        unit = "gal",
                    )
                } returns Result.success(restoredItem)

                val result = useCase.undoDelete(deletedItem)

                assertThat(result.isSuccess).isTrue()
                coVerify {
                    currentListRepository.addItemAt(
                        itemId = "item-1",
                        position = 3,
                        quantity = 2,
                        unit = "gal",
                    )
                }
            }

        @Test
        fun `handles null unit correctly`() =
            runTest {
                val deletedItem =
                    DeletedItem(
                        listItemId = "list-1",
                        itemId = "item-1",
                        name = "Milk",
                        quantity = 1,
                        unit = null,
                        position = 0,
                    )
                val restoredItem = createTestListItem(id = "list-2")
                coEvery {
                    currentListRepository.addItemAt(
                        itemId = "item-1",
                        position = 0,
                        quantity = 1,
                        unit = null,
                    )
                } returns Result.success(restoredItem)

                val result = useCase.undoDelete(deletedItem)

                assertThat(result.isSuccess).isTrue()
            }

        @Test
        fun `returns failure when addItemAt fails`() =
            runTest {
                val deletedItem =
                    DeletedItem(
                        listItemId = "list-1",
                        itemId = "item-1",
                        name = "Milk",
                        quantity = 1,
                        unit = null,
                        position = 0,
                    )
                coEvery {
                    currentListRepository.addItemAt(any(), any(), any(), any())
                } returns Result.failure(RuntimeException("DB error"))

                val result = useCase.undoDelete(deletedItem)

                assertThat(result.isFailure).isTrue()
            }
    }

    // Helper function
    private fun createTestListItem(
        id: String = "list-id",
        itemId: String = "item-id",
        name: String = "Test Item",
        normalizedName: String = name.lowercase(),
        quantity: Int = 1,
        unit: String? = null,
        addedAt: Instant = Instant.now(),
        checkedAt: Instant? = null,
        position: Int = 0,
        isChecked: Boolean = false,
    ) = CurrentListItem(
        id = id,
        itemId = itemId,
        name = name,
        normalizedName = normalizedName,
        quantity = quantity,
        unit = unit,
        addedAt = addedAt,
        checkedAt = checkedAt,
        position = position,
        isChecked = isChecked,
    )
}
