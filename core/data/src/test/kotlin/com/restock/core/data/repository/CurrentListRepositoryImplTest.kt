package com.restock.core.data.repository

import com.google.common.truth.Truth.assertThat
import com.restock.core.data.database.dao.CurrentListDao
import com.restock.core.data.database.dao.CurrentListItemWithDetails
import com.restock.core.data.database.dao.ItemDao
import com.restock.core.data.database.entity.CurrentListEntity
import com.restock.core.data.database.entity.ItemEntity
import com.restock.core.domain.model.PositionUpdate
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
 * Unit tests for [CurrentListRepositoryImpl].
 *
 * Tests verify correct mapping between domain models and entities,
 * proper delegation to DAOs, and business logic for list operations.
 */
class CurrentListRepositoryImplTest {
    private val currentListDao: CurrentListDao = mockk(relaxed = true)
    private val itemDao: ItemDao = mockk(relaxed = true)
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: CurrentListRepositoryImpl

    @BeforeEach
    fun setup() {
        repository = CurrentListRepositoryImpl(currentListDao, itemDao, testDispatcher)
    }

    @Nested
    @DisplayName("observeCurrentList")
    inner class ObserveCurrentListTests {
        @Test
        fun `returns flow of mapped domain models`() =
            runTest(testDispatcher) {
                val details =
                    listOf(
                        createTestDetails(currentListId = "cl-1", itemId = "item-1", itemName = "Milk"),
                        createTestDetails(currentListId = "cl-2", itemId = "item-2", itemName = "Eggs"),
                    )
                every { currentListDao.observeCurrentList() } returns flowOf(details)

                val result = repository.observeCurrentList().first()

                assertThat(result).hasSize(2)
                assertThat(result[0].id).isEqualTo("cl-1")
                assertThat(result[0].name).isEqualTo("Milk")
                assertThat(result[1].id).isEqualTo("cl-2")
                assertThat(result[1].name).isEqualTo("Eggs")
            }
    }

    @Nested
    @DisplayName("observeUncheckedItems")
    inner class ObserveUncheckedItemsTests {
        @Test
        fun `returns flow of unchecked items`() =
            runTest(testDispatcher) {
                val details =
                    listOf(
                        createTestDetails(currentListId = "cl-1", isChecked = false),
                    )
                every { currentListDao.observeUncheckedItems() } returns flowOf(details)

                val result = repository.observeUncheckedItems().first()

                assertThat(result).hasSize(1)
                assertThat(result[0].isChecked).isFalse()
            }
    }

    @Nested
    @DisplayName("observeCheckedItems")
    inner class ObserveCheckedItemsTests {
        @Test
        fun `returns flow of checked items`() =
            runTest(testDispatcher) {
                val details =
                    listOf(
                        createTestDetails(currentListId = "cl-1", isChecked = true, checkedAt = 1700000000000L),
                    )
                every { currentListDao.observeCheckedItems() } returns flowOf(details)

                val result = repository.observeCheckedItems().first()

                assertThat(result).hasSize(1)
                assertThat(result[0].isChecked).isTrue()
            }
    }

    @Nested
    @DisplayName("getById")
    inner class GetByIdTests {
        @Test
        fun `returns mapped domain model when item exists`() =
            runTest(testDispatcher) {
                val entity = createTestListEntity(id = "cl-1", itemId = "item-1")
                val itemEntity = createTestItemEntity(id = "item-1", name = "Milk")
                coEvery { currentListDao.getById("cl-1") } returns entity
                coEvery { itemDao.getById("item-1") } returns itemEntity

                val result = repository.getById("cl-1")

                assertThat(result).isNotNull()
                assertThat(result?.id).isEqualTo("cl-1")
                assertThat(result?.name).isEqualTo("Milk")
            }

        @Test
        fun `returns null when item does not exist`() =
            runTest(testDispatcher) {
                coEvery { currentListDao.getById("nonexistent") } returns null

                val result = repository.getById("nonexistent")

                assertThat(result).isNull()
            }
    }

    @Nested
    @DisplayName("isItemInList")
    inner class IsItemInListTests {
        @Test
        fun `returns true when item is in list`() =
            runTest(testDispatcher) {
                coEvery { currentListDao.findByItemId("item-1") } returns createTestListEntity()

                val result = repository.isItemInList("item-1")

                assertThat(result).isTrue()
            }

        @Test
        fun `returns false when item is not in list`() =
            runTest(testDispatcher) {
                coEvery { currentListDao.findByItemId("item-1") } returns null

                val result = repository.isItemInList("item-1")

                assertThat(result).isFalse()
            }
    }

    @Nested
    @DisplayName("findByItemId")
    inner class FindByItemIdTests {
        @Test
        fun `returns unchecked item when found`() =
            runTest(testDispatcher) {
                val entity = createTestListEntity(id = "cl-1", itemId = "item-1", isChecked = false)
                val itemEntity = createTestItemEntity(id = "item-1", name = "Milk")
                coEvery { currentListDao.findUncheckedByItemId("item-1") } returns entity
                coEvery { itemDao.getById("item-1") } returns itemEntity

                val result = repository.findByItemId("item-1")

                assertThat(result).isNotNull()
                assertThat(result?.itemId).isEqualTo("item-1")
                assertThat(result?.isChecked).isFalse()
            }

        @Test
        fun `returns null when item not found`() =
            runTest(testDispatcher) {
                coEvery { currentListDao.findUncheckedByItemId("item-1") } returns null

                val result = repository.findByItemId("item-1")

                assertThat(result).isNull()
            }
    }

    @Nested
    @DisplayName("getCurrentItemIds")
    inner class GetCurrentItemIdsTests {
        @Test
        fun `returns list of item IDs`() =
            runTest(testDispatcher) {
                coEvery { currentListDao.getCurrentItemIds() } returns listOf("item-1", "item-2", "item-3")

                val result = repository.getCurrentItemIds()

                assertThat(result).containsExactly("item-1", "item-2", "item-3")
            }

        @Test
        fun `returns empty list when no items`() =
            runTest(testDispatcher) {
                coEvery { currentListDao.getCurrentItemIds() } returns emptyList()

                val result = repository.getCurrentItemIds()

                assertThat(result).isEmpty()
            }
    }

    @Nested
    @DisplayName("addItem")
    inner class AddItemTests {
        @Test
        fun `returns success with new list item when item exists`() =
            runTest(testDispatcher) {
                val itemEntity = createTestItemEntity(id = "item-1", name = "Milk")
                coEvery { itemDao.getById("item-1") } returns itemEntity
                coEvery { currentListDao.getMaxPosition() } returns 2
                coEvery { currentListDao.insert(any()) } returns Unit

                val result = repository.addItem("item-1", quantity = 2, unit = "gal")

                assertThat(result.isSuccess).isTrue()
                val item = result.getOrNull()!!
                assertThat(item.itemId).isEqualTo("item-1")
                assertThat(item.name).isEqualTo("Milk")
                assertThat(item.quantity).isEqualTo(2)
                assertThat(item.unit).isEqualTo("gal")
                assertThat(item.position).isEqualTo(3)
                assertThat(item.isChecked).isFalse()
            }

        @Test
        fun `returns failure when item does not exist`() =
            runTest(testDispatcher) {
                coEvery { itemDao.getById("nonexistent") } returns null

                val result = repository.addItem("nonexistent")

                assertThat(result.isFailure).isTrue()
                assertThat(result.exceptionOrNull()).isInstanceOf(IllegalArgumentException::class.java)
            }
    }

    @Nested
    @DisplayName("addItemAt")
    inner class AddItemAtTests {
        @Test
        fun `inserts item at specific position`() =
            runTest(testDispatcher) {
                val itemEntity = createTestItemEntity(id = "item-1", name = "Milk")
                coEvery { itemDao.getById("item-1") } returns itemEntity
                coEvery { currentListDao.insert(any()) } returns Unit
                val insertedEntity = slot<CurrentListEntity>()
                coEvery { currentListDao.insert(capture(insertedEntity)) } returns Unit

                val result = repository.addItemAt("item-1", position = 5, quantity = 1, unit = null)

                assertThat(result.isSuccess).isTrue()
                assertThat(result.getOrNull()?.position).isEqualTo(5)
                assertThat(insertedEntity.captured.position).isEqualTo(5)
            }
    }

    @Nested
    @DisplayName("checkItem")
    inner class CheckItemTests {
        @Test
        fun `returns success when check succeeds`() =
            runTest(testDispatcher) {
                coEvery { currentListDao.checkItem(any(), any()) } returns Unit

                val result = repository.checkItem("cl-1")

                assertThat(result.isSuccess).isTrue()
                coVerify { currentListDao.checkItem("cl-1", any()) }
            }
    }

    @Nested
    @DisplayName("uncheckItem")
    inner class UncheckItemTests {
        @Test
        fun `returns success when uncheck succeeds`() =
            runTest(testDispatcher) {
                coEvery { currentListDao.uncheckItem("cl-1") } returns Unit

                val result = repository.uncheckItem("cl-1")

                assertThat(result.isSuccess).isTrue()
                coVerify { currentListDao.uncheckItem("cl-1") }
            }
    }

    @Nested
    @DisplayName("toggleChecked")
    inner class ToggleCheckedTests {
        @Test
        fun `checks item when currently unchecked`() =
            runTest(testDispatcher) {
                val entity = createTestListEntity(id = "cl-1", isChecked = false)
                coEvery { currentListDao.getById("cl-1") } returns entity
                coEvery { currentListDao.checkItem(any(), any()) } returns Unit

                val result = repository.toggleChecked("cl-1")

                assertThat(result.isSuccess).isTrue()
                coVerify { currentListDao.checkItem("cl-1", any()) }
            }

        @Test
        fun `unchecks item when currently checked`() =
            runTest(testDispatcher) {
                val entity = createTestListEntity(id = "cl-1", isChecked = true)
                coEvery { currentListDao.getById("cl-1") } returns entity
                coEvery { currentListDao.uncheckItem("cl-1") } returns Unit

                val result = repository.toggleChecked("cl-1")

                assertThat(result.isSuccess).isTrue()
                coVerify { currentListDao.uncheckItem("cl-1") }
            }

        @Test
        fun `returns failure when item not found`() =
            runTest(testDispatcher) {
                coEvery { currentListDao.getById("nonexistent") } returns null

                val result = repository.toggleChecked("nonexistent")

                assertThat(result.isFailure).isTrue()
            }
    }

    @Nested
    @DisplayName("updateQuantity")
    inner class UpdateQuantityTests {
        @Test
        fun `updates quantity and unit successfully`() =
            runTest(testDispatcher) {
                coEvery { currentListDao.updateQuantityAndUnit("cl-1", 5, "lb") } returns Unit

                val result = repository.updateQuantity("cl-1", 5, "lb")

                assertThat(result.isSuccess).isTrue()
                coVerify { currentListDao.updateQuantityAndUnit("cl-1", 5, "lb") }
            }

        @Test
        fun `returns failure when quantity is not positive`() =
            runTest(testDispatcher) {
                val result = repository.updateQuantity("cl-1", 0, null)

                assertThat(result.isFailure).isTrue()
                assertThat(result.exceptionOrNull()).isInstanceOf(IllegalArgumentException::class.java)
            }

        @Test
        fun `returns failure for negative quantity`() =
            runTest(testDispatcher) {
                val result = repository.updateQuantity("cl-1", -1, null)

                assertThat(result.isFailure).isTrue()
            }
    }

    @Nested
    @DisplayName("updatePositions")
    inner class UpdatePositionsTests {
        @Test
        fun `updates multiple positions`() =
            runTest(testDispatcher) {
                val updates =
                    listOf(
                        PositionUpdate("cl-1", 2),
                        PositionUpdate("cl-2", 0),
                        PositionUpdate("cl-3", 1),
                    )
                coEvery { currentListDao.updatePosition(any(), any()) } returns Unit

                val result = repository.updatePositions(updates)

                assertThat(result.isSuccess).isTrue()
                coVerify { currentListDao.updatePosition("cl-1", 2) }
                coVerify { currentListDao.updatePosition("cl-2", 0) }
                coVerify { currentListDao.updatePosition("cl-3", 1) }
            }

        @Test
        fun `handles empty list`() =
            runTest(testDispatcher) {
                val result = repository.updatePositions(emptyList())

                assertThat(result.isSuccess).isTrue()
            }
    }

    @Nested
    @DisplayName("reorderItems")
    inner class ReorderItemsTests {
        @Test
        fun `reorders items correctly`() =
            runTest(testDispatcher) {
                val entities =
                    listOf(
                        createTestListEntity(id = "cl-1", position = 0),
                        createTestListEntity(id = "cl-2", position = 1),
                        createTestListEntity(id = "cl-3", position = 2),
                    )
                coEvery { currentListDao.getAll() } returns entities
                coEvery { currentListDao.updatePosition(any(), any()) } returns Unit

                val result = repository.reorderItems("cl-1", 0, 2)

                assertThat(result.isSuccess).isTrue()
            }

        @Test
        fun `returns failure when item not in list`() =
            runTest(testDispatcher) {
                coEvery { currentListDao.getAll() } returns emptyList()

                val result = repository.reorderItems("nonexistent", 0, 1)

                assertThat(result.isFailure).isTrue()
            }
    }

    @Nested
    @DisplayName("removeItem")
    inner class RemoveItemTests {
        @Test
        fun `returns success when delete succeeds`() =
            runTest(testDispatcher) {
                coEvery { currentListDao.delete("cl-1") } returns Unit

                val result = repository.removeItem("cl-1")

                assertThat(result.isSuccess).isTrue()
                coVerify { currentListDao.delete("cl-1") }
            }
    }

    @Nested
    @DisplayName("clearCheckedItems")
    inner class ClearCheckedItemsTests {
        @Test
        fun `returns success when clear succeeds`() =
            runTest(testDispatcher) {
                coEvery { currentListDao.deleteCheckedItems() } returns Unit

                val result = repository.clearCheckedItems()

                assertThat(result.isSuccess).isTrue()
                coVerify { currentListDao.deleteCheckedItems() }
            }
    }

    @Nested
    @DisplayName("clearAll")
    inner class ClearAllTests {
        @Test
        fun `returns success when clear all succeeds`() =
            runTest(testDispatcher) {
                coEvery { currentListDao.clearAll() } returns Unit

                val result = repository.clearAll()

                assertThat(result.isSuccess).isTrue()
                coVerify { currentListDao.clearAll() }
            }
    }

    @Nested
    @DisplayName("getCount")
    inner class GetCountTests {
        @Test
        fun `returns correct count`() =
            runTest(testDispatcher) {
                coEvery { currentListDao.getCount() } returns 5

                val result = repository.getCount()

                assertThat(result).isEqualTo(5)
            }
    }

    @Nested
    @DisplayName("getUncheckedCount")
    inner class GetUncheckedCountTests {
        @Test
        fun `returns correct unchecked count`() =
            runTest(testDispatcher) {
                coEvery { currentListDao.getUncheckedCount() } returns 3

                val result = repository.getUncheckedCount()

                assertThat(result).isEqualTo(3)
            }
    }

    // Helper functions
    private fun createTestDetails(
        currentListId: String = "cl-1",
        itemId: String = "item-1",
        itemName: String = "Test Item",
        normalizedName: String = "test item",
        quantity: Int = 1,
        unit: String? = null,
        addedAt: Long = 1700000000000L,
        checkedAt: Long? = null,
        position: Int = 0,
        isChecked: Boolean = false,
    ) = CurrentListItemWithDetails(
        currentListId = currentListId,
        itemId = itemId,
        itemName = itemName,
        normalizedName = normalizedName,
        quantity = quantity,
        unit = unit,
        addedAt = addedAt,
        checkedAt = checkedAt,
        position = position,
        isChecked = isChecked,
    )

    private fun createTestListEntity(
        id: String = "cl-1",
        itemId: String = "item-1",
        quantity: Int = 1,
        unit: String? = null,
        addedAt: Long = 1700000000000L,
        checkedAt: Long? = null,
        position: Int = 0,
        isChecked: Boolean = false,
    ) = CurrentListEntity(
        id = id,
        itemId = itemId,
        quantity = quantity,
        unit = unit,
        addedAt = addedAt,
        checkedAt = checkedAt,
        position = position,
        isChecked = isChecked,
    )

    private fun createTestItemEntity(
        id: String = "item-1",
        name: String = "Test Item",
        normalizedName: String = "test item",
        createdAt: Long = 1700000000000L,
        isActive: Boolean = true,
    ) = ItemEntity(
        id = id,
        name = name,
        normalizedName = normalizedName,
        createdAt = createdAt,
        isActive = isActive,
    )
}
