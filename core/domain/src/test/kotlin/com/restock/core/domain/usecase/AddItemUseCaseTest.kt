package com.restock.core.domain.usecase

import com.google.common.truth.Truth.assertThat
import com.restock.core.domain.model.CurrentListItem
import com.restock.core.domain.model.GroceryItem
import com.restock.core.domain.repository.CurrentListRepository
import com.restock.core.domain.repository.ItemRepository
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
 * Unit tests for [AddItemUseCase].
 */
class AddItemUseCaseTest {
    private val itemRepository: ItemRepository = mockk()
    private val currentListRepository: CurrentListRepository = mockk()
    private lateinit var useCase: AddItemUseCase

    @BeforeEach
    fun setup() {
        useCase = AddItemUseCase(itemRepository, currentListRepository)
    }

    @Nested
    @DisplayName("Input validation")
    inner class InputValidationTests {
        @Test
        fun `returns failure when name is blank`() =
            runTest {
                val result = useCase("")

                assertThat(result.isFailure).isTrue()
                assertThat(result.exceptionOrNull())
                    .isInstanceOf(IllegalArgumentException::class.java)
                assertThat(result.exceptionOrNull()?.message)
                    .contains("empty")
            }

        @Test
        fun `returns failure when name is only whitespace`() =
            runTest {
                val result = useCase("   ")

                assertThat(result.isFailure).isTrue()
                assertThat(result.exceptionOrNull())
                    .isInstanceOf(IllegalArgumentException::class.java)
            }

        @Test
        fun `returns failure when quantity is less than 1`() =
            runTest {
                val result = useCase("Milk", quantity = 0)

                assertThat(result.isFailure).isTrue()
                assertThat(result.exceptionOrNull()?.message)
                    .contains("Quantity must be at least 1")
            }

        @Test
        fun `returns failure when quantity is negative`() =
            runTest {
                val result = useCase("Milk", quantity = -5)

                assertThat(result.isFailure).isTrue()
            }
    }

    @Nested
    @DisplayName("Adding new items")
    inner class AddNewItemTests {
        @Test
        fun `creates new item when not on list`() =
            runTest {
                val item = createTestGroceryItem(id = "item-1", name = "Milk")
                val listItem = createTestListItem(id = "list-1", itemId = "item-1", name = "Milk")

                coEvery { itemRepository.getOrCreate("Milk") } returns item
                coEvery { currentListRepository.findByItemId("item-1") } returns null
                coEvery { currentListRepository.addItem("item-1", 1, null) } returns Result.success(listItem)

                val result = useCase("Milk")

                assertThat(result.isSuccess).isTrue()
                assertThat(result.getOrNull()?.name).isEqualTo("Milk")
                coVerify { currentListRepository.addItem("item-1", 1, null) }
            }

        @Test
        fun `adds item with specified quantity`() =
            runTest {
                val item = createTestGroceryItem(id = "item-1", name = "Eggs")
                val listItem = createTestListItem(id = "list-1", itemId = "item-1", name = "Eggs", quantity = 12)

                coEvery { itemRepository.getOrCreate("Eggs") } returns item
                coEvery { currentListRepository.findByItemId("item-1") } returns null
                coEvery { currentListRepository.addItem("item-1", 12, null) } returns Result.success(listItem)

                val result = useCase("Eggs", quantity = 12)

                assertThat(result.isSuccess).isTrue()
                assertThat(result.getOrNull()?.quantity).isEqualTo(12)
                coVerify { currentListRepository.addItem("item-1", 12, null) }
            }

        @Test
        fun `adds item with unit`() =
            runTest {
                val item = createTestGroceryItem(id = "item-1", name = "Beef")
                val listItem =
                    createTestListItem(id = "list-1", itemId = "item-1", name = "Beef", quantity = 2, unit = "lb")

                coEvery { itemRepository.getOrCreate("Beef") } returns item
                coEvery { currentListRepository.findByItemId("item-1") } returns null
                coEvery { currentListRepository.addItem("item-1", 2, "lb") } returns Result.success(listItem)

                val result = useCase("Beef", quantity = 2, unit = "lb")

                assertThat(result.isSuccess).isTrue()
                assertThat(result.getOrNull()?.unit).isEqualTo("lb")
                coVerify { currentListRepository.addItem("item-1", 2, "lb") }
            }
    }

    @Nested
    @DisplayName("Handling duplicates")
    inner class DuplicateHandlingTests {
        @Test
        fun `updates quantity when item already on list`() =
            runTest {
                val item = createTestGroceryItem(id = "item-1", name = "Milk")
                val existingListItem =
                    createTestListItem(
                        id = "list-1",
                        itemId = "item-1",
                        name = "Milk",
                        quantity = 2,
                    )

                coEvery { itemRepository.getOrCreate("Milk") } returns item
                coEvery { currentListRepository.findByItemId("item-1") } returns existingListItem
                coEvery { currentListRepository.updateQuantity("list-1", 3, null) } returns Result.success(Unit)

                val result = useCase("Milk")

                assertThat(result.isSuccess).isTrue()
                assertThat(result.getOrNull()?.quantity).isEqualTo(3)
                coVerify { currentListRepository.updateQuantity("list-1", 3, null) }
                coVerify(exactly = 0) { currentListRepository.addItem(any(), any(), any()) }
            }

        @Test
        fun `adds specified quantity to existing quantity`() =
            runTest {
                val item = createTestGroceryItem(id = "item-1", name = "Eggs")
                val existingListItem =
                    createTestListItem(
                        id = "list-1",
                        itemId = "item-1",
                        name = "Eggs",
                        quantity = 6,
                    )

                coEvery { itemRepository.getOrCreate("Eggs") } returns item
                coEvery { currentListRepository.findByItemId("item-1") } returns existingListItem
                coEvery { currentListRepository.updateQuantity("list-1", 18, null) } returns Result.success(Unit)

                val result = useCase("Eggs", quantity = 12)

                assertThat(result.isSuccess).isTrue()
                assertThat(result.getOrNull()?.quantity).isEqualTo(18)
            }

        @Test
        fun `preserves existing unit when new unit is null`() =
            runTest {
                val item = createTestGroceryItem(id = "item-1", name = "Beef")
                val existingListItem =
                    createTestListItem(
                        id = "list-1",
                        itemId = "item-1",
                        name = "Beef",
                        quantity = 1,
                        unit = "lb",
                    )

                coEvery { itemRepository.getOrCreate("Beef") } returns item
                coEvery { currentListRepository.findByItemId("item-1") } returns existingListItem
                coEvery { currentListRepository.updateQuantity("list-1", 2, "lb") } returns Result.success(Unit)

                val result = useCase("Beef", quantity = 1, unit = null)

                assertThat(result.isSuccess).isTrue()
                assertThat(result.getOrNull()?.unit).isEqualTo("lb")
            }

        @Test
        fun `overrides unit when new unit is specified`() =
            runTest {
                val item = createTestGroceryItem(id = "item-1", name = "Beef")
                val existingListItem =
                    createTestListItem(
                        id = "list-1",
                        itemId = "item-1",
                        name = "Beef",
                        quantity = 1,
                        unit = "lb",
                    )

                coEvery { itemRepository.getOrCreate("Beef") } returns item
                coEvery { currentListRepository.findByItemId("item-1") } returns existingListItem
                coEvery { currentListRepository.updateQuantity("list-1", 2, "kg") } returns Result.success(Unit)

                val result = useCase("Beef", quantity = 1, unit = "kg")

                assertThat(result.isSuccess).isTrue()
                assertThat(result.getOrNull()?.unit).isEqualTo("kg")
            }
    }

    @Nested
    @DisplayName("Error handling")
    inner class ErrorHandlingTests {
        @Test
        fun `returns failure when addItem fails`() =
            runTest {
                val item = createTestGroceryItem(id = "item-1", name = "Milk")

                coEvery { itemRepository.getOrCreate("Milk") } returns item
                coEvery { currentListRepository.findByItemId("item-1") } returns null
                coEvery { currentListRepository.addItem(any(), any(), any()) } returns
                    Result.failure(RuntimeException("DB error"))

                val result = useCase("Milk")

                assertThat(result.isFailure).isTrue()
            }

        @Test
        fun `returns failure when updateQuantity fails`() =
            runTest {
                val item = createTestGroceryItem(id = "item-1", name = "Milk")
                val existingListItem = createTestListItem(id = "list-1", itemId = "item-1", name = "Milk")

                coEvery { itemRepository.getOrCreate("Milk") } returns item
                coEvery { currentListRepository.findByItemId("item-1") } returns existingListItem
                coEvery { currentListRepository.updateQuantity(any(), any(), any()) } returns
                    Result.failure(RuntimeException("DB error"))

                val result = useCase("Milk")

                assertThat(result.isFailure).isTrue()
            }
    }

    // Helper functions
    private fun createTestGroceryItem(
        id: String = "test-id",
        name: String = "Test Item",
        normalizedName: String = name.lowercase(),
        createdAt: Instant = Instant.now(),
        isActive: Boolean = true,
    ) = GroceryItem(
        id = id,
        name = name,
        normalizedName = normalizedName,
        createdAt = createdAt,
        isActive = isActive,
    )

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
