package com.restock.core.data.database.dao

import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.restock.core.data.database.entity.CurrentListEntity
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for [CurrentListDao].
 *
 * Tests CRUD operations, item checking/unchecking, position management,
 * and Flow-based observations with item details.
 */
@RunWith(AndroidJUnit4::class)
class CurrentListDaoTest : BaseDaoTest() {

    private fun createTestCurrentListItem(
        id: String = "list-item-1",
        itemId: String = TEST_ITEM_ID,
        quantity: Int = 1,
        unit: String? = null,
        addedAt: Long = TEST_CREATED_AT,
        checkedAt: Long? = null,
        position: Int = 0,
        isChecked: Boolean = false
    ) = CurrentListEntity(
        id = id,
        itemId = itemId,
        quantity = quantity,
        unit = unit,
        addedAt = addedAt,
        checkedAt = checkedAt,
        position = position,
        isChecked = isChecked
    )

    // region Insert Tests

    @Test
    fun insert_item_can_be_retrieved_by_id() = runTest {
        itemDao.insert(createTestItem())
        val listItem = createTestCurrentListItem()

        currentListDao.insert(listItem)
        val retrieved = currentListDao.getById("list-item-1")

        assertThat(retrieved).isNotNull()
        assertThat(retrieved?.itemId).isEqualTo(TEST_ITEM_ID)
        assertThat(retrieved?.quantity).isEqualTo(1)
    }

    @Test
    fun insertAll_inserts_multiple_items() = runTest {
        itemDao.insert(createTestItem(id = "item-1", normalizedName = "item1"))
        itemDao.insert(createTestItem(id = "item-2", normalizedName = "item2"))

        val listItems = listOf(
            createTestCurrentListItem(id = "list-1", itemId = "item-1", position = 0),
            createTestCurrentListItem(id = "list-2", itemId = "item-2", position = 1)
        )

        currentListDao.insertAll(listItems)
        val count = currentListDao.getCount()

        assertThat(count).isEqualTo(2)
    }

    // endregion

    // region Query Tests

    @Test
    fun getAll_returns_items_sorted_by_position() = runTest {
        itemDao.insert(createTestItem(id = "item-1", normalizedName = "item1"))
        itemDao.insert(createTestItem(id = "item-2", normalizedName = "item2"))
        itemDao.insert(createTestItem(id = "item-3", normalizedName = "item3"))

        currentListDao.insert(createTestCurrentListItem(id = "list-1", itemId = "item-1", position = 2))
        currentListDao.insert(createTestCurrentListItem(id = "list-2", itemId = "item-2", position = 0))
        currentListDao.insert(createTestCurrentListItem(id = "list-3", itemId = "item-3", position = 1))

        val items = currentListDao.getAll()

        assertThat(items.map { it.position }).containsExactly(0, 1, 2).inOrder()
    }

    @Test
    fun findByItemId_returns_existing_list_entry() = runTest {
        itemDao.insert(createTestItem())
        currentListDao.insert(createTestCurrentListItem())

        val found = currentListDao.findByItemId(TEST_ITEM_ID)

        assertThat(found).isNotNull()
        assertThat(found?.id).isEqualTo("list-item-1")
    }

    @Test
    fun findByItemId_returns_null_when_not_in_list() = runTest {
        itemDao.insert(createTestItem())

        val found = currentListDao.findByItemId(TEST_ITEM_ID)

        assertThat(found).isNull()
    }

    @Test
    fun getMaxPosition_returns_highest_position() = runTest {
        itemDao.insert(createTestItem(id = "item-1", normalizedName = "item1"))
        itemDao.insert(createTestItem(id = "item-2", normalizedName = "item2"))

        currentListDao.insert(createTestCurrentListItem(id = "list-1", itemId = "item-1", position = 5))
        currentListDao.insert(createTestCurrentListItem(id = "list-2", itemId = "item-2", position = 10))

        val maxPos = currentListDao.getMaxPosition()

        assertThat(maxPos).isEqualTo(10)
    }

    @Test
    fun getMaxPosition_returns_negative_one_when_empty() = runTest {
        val maxPos = currentListDao.getMaxPosition()

        assertThat(maxPos).isEqualTo(-1)
    }

    // endregion

    // region Check/Uncheck Tests

    @Test
    fun checkItem_marks_item_as_checked_with_timestamp() = runTest {
        itemDao.insert(createTestItem())
        currentListDao.insert(createTestCurrentListItem(isChecked = false))

        val checkedTime = System.currentTimeMillis()
        currentListDao.checkItem("list-item-1", checkedTime)

        val retrieved = currentListDao.getById("list-item-1")
        assertThat(retrieved?.isChecked).isTrue()
        assertThat(retrieved?.checkedAt).isEqualTo(checkedTime)
    }

    @Test
    fun uncheckItem_clears_checked_state() = runTest {
        itemDao.insert(createTestItem())
        currentListDao.insert(createTestCurrentListItem(isChecked = true, checkedAt = TEST_CREATED_AT))

        currentListDao.uncheckItem("list-item-1")

        val retrieved = currentListDao.getById("list-item-1")
        assertThat(retrieved?.isChecked).isFalse()
        assertThat(retrieved?.checkedAt).isNull()
    }

    // endregion

    // region Update Tests

    @Test
    fun updateQuantity_changes_item_quantity() = runTest {
        itemDao.insert(createTestItem())
        currentListDao.insert(createTestCurrentListItem(quantity = 1))

        currentListDao.updateQuantity("list-item-1", 5)

        val retrieved = currentListDao.getById("list-item-1")
        assertThat(retrieved?.quantity).isEqualTo(5)
    }

    @Test
    fun updatePosition_changes_item_position() = runTest {
        itemDao.insert(createTestItem())
        currentListDao.insert(createTestCurrentListItem(position = 0))

        currentListDao.updatePosition("list-item-1", 10)

        val retrieved = currentListDao.getById("list-item-1")
        assertThat(retrieved?.position).isEqualTo(10)
    }

    @Test
    fun update_modifies_entire_item() = runTest {
        itemDao.insert(createTestItem())
        val original = createTestCurrentListItem(quantity = 1, unit = null)
        currentListDao.insert(original)

        val updated = original.copy(quantity = 3, unit = "lbs")
        currentListDao.update(updated)

        val retrieved = currentListDao.getById("list-item-1")
        assertThat(retrieved?.quantity).isEqualTo(3)
        assertThat(retrieved?.unit).isEqualTo("lbs")
    }

    // endregion

    // region Delete Tests

    @Test
    fun delete_removes_single_item() = runTest {
        itemDao.insert(createTestItem())
        currentListDao.insert(createTestCurrentListItem())

        currentListDao.delete("list-item-1")

        val retrieved = currentListDao.getById("list-item-1")
        assertThat(retrieved).isNull()
    }

    @Test
    fun deleteCheckedItems_removes_only_checked_items() = runTest {
        itemDao.insert(createTestItem(id = "item-1", normalizedName = "item1"))
        itemDao.insert(createTestItem(id = "item-2", normalizedName = "item2"))

        currentListDao.insert(createTestCurrentListItem(id = "list-1", itemId = "item-1", isChecked = true))
        currentListDao.insert(createTestCurrentListItem(id = "list-2", itemId = "item-2", isChecked = false, position = 1))

        currentListDao.deleteCheckedItems()

        val count = currentListDao.getCount()
        assertThat(count).isEqualTo(1)
        assertThat(currentListDao.getById("list-2")).isNotNull()
    }

    @Test
    fun clearAll_removes_all_items() = runTest {
        itemDao.insert(createTestItem(id = "item-1", normalizedName = "item1"))
        itemDao.insert(createTestItem(id = "item-2", normalizedName = "item2"))

        currentListDao.insert(createTestCurrentListItem(id = "list-1", itemId = "item-1"))
        currentListDao.insert(createTestCurrentListItem(id = "list-2", itemId = "item-2", position = 1))

        currentListDao.clearAll()

        val count = currentListDao.getCount()
        assertThat(count).isEqualTo(0)
    }

    // endregion

    // region Count Tests

    @Test
    fun getCount_returns_total_items() = runTest {
        itemDao.insert(createTestItem(id = "item-1", normalizedName = "item1"))
        itemDao.insert(createTestItem(id = "item-2", normalizedName = "item2"))

        currentListDao.insert(createTestCurrentListItem(id = "list-1", itemId = "item-1"))
        currentListDao.insert(createTestCurrentListItem(id = "list-2", itemId = "item-2", position = 1))

        val count = currentListDao.getCount()
        assertThat(count).isEqualTo(2)
    }

    @Test
    fun getUncheckedCount_returns_only_unchecked_items() = runTest {
        itemDao.insert(createTestItem(id = "item-1", normalizedName = "item1"))
        itemDao.insert(createTestItem(id = "item-2", normalizedName = "item2"))
        itemDao.insert(createTestItem(id = "item-3", normalizedName = "item3"))

        currentListDao.insert(createTestCurrentListItem(id = "list-1", itemId = "item-1", isChecked = false))
        currentListDao.insert(createTestCurrentListItem(id = "list-2", itemId = "item-2", isChecked = true, position = 1))
        currentListDao.insert(createTestCurrentListItem(id = "list-3", itemId = "item-3", isChecked = false, position = 2))

        val count = currentListDao.getUncheckedCount()
        assertThat(count).isEqualTo(2)
    }

    // endregion

    // region Flow Tests

    @Test
    fun observeCurrentList_emits_items_with_details() = runTest {
        itemDao.insert(createTestItem(name = "Milk"))

        currentListDao.observeCurrentList().test {
            assertThat(awaitItem()).isEmpty()

            currentListDao.insert(createTestCurrentListItem())
            val items = awaitItem()

            assertThat(items).hasSize(1)
            assertThat(items[0].itemName).isEqualTo("Milk")
            assertThat(items[0].itemId).isEqualTo(TEST_ITEM_ID)
            assertThat(items[0].normalizedName).isEqualTo(TEST_NORMALIZED_NAME)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun observeCurrentList_sorts_unchecked_first_then_by_position() = runTest {
        itemDao.insert(createTestItem(id = "item-1", name = "A", normalizedName = "a"))
        itemDao.insert(createTestItem(id = "item-2", name = "B", normalizedName = "b"))
        itemDao.insert(createTestItem(id = "item-3", name = "C", normalizedName = "c"))

        currentListDao.insert(createTestCurrentListItem(id = "list-1", itemId = "item-1", position = 1, isChecked = false))
        currentListDao.insert(createTestCurrentListItem(id = "list-2", itemId = "item-2", position = 0, isChecked = true))
        currentListDao.insert(createTestCurrentListItem(id = "list-3", itemId = "item-3", position = 2, isChecked = false))

        currentListDao.observeCurrentList().test {
            val items = awaitItem()

            // Unchecked items come first (sorted by position), then checked items
            assertThat(items[0].itemName).isEqualTo("A")  // unchecked, position 1
            assertThat(items[1].itemName).isEqualTo("C")  // unchecked, position 2
            assertThat(items[2].itemName).isEqualTo("B")  // checked, position 0

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun observeUncheckedItems_emits_only_unchecked() = runTest {
        itemDao.insert(createTestItem(id = "item-1", name = "Unchecked", normalizedName = "unchecked"))
        itemDao.insert(createTestItem(id = "item-2", name = "Checked", normalizedName = "checked"))

        currentListDao.insert(createTestCurrentListItem(id = "list-1", itemId = "item-1", isChecked = false))
        currentListDao.insert(createTestCurrentListItem(id = "list-2", itemId = "item-2", isChecked = true, position = 1))

        currentListDao.observeUncheckedItems().test {
            val items = awaitItem()

            assertThat(items).hasSize(1)
            assertThat(items[0].itemName).isEqualTo("Unchecked")

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun observeCheckedItems_emits_only_checked_sorted_by_checked_time() = runTest {
        itemDao.insert(createTestItem(id = "item-1", name = "First", normalizedName = "first"))
        itemDao.insert(createTestItem(id = "item-2", name = "Second", normalizedName = "second"))

        currentListDao.insert(createTestCurrentListItem(
            id = "list-1",
            itemId = "item-1",
            isChecked = true,
            checkedAt = 1000L
        ))
        currentListDao.insert(createTestCurrentListItem(
            id = "list-2",
            itemId = "item-2",
            isChecked = true,
            checkedAt = 2000L,
            position = 1
        ))

        currentListDao.observeCheckedItems().test {
            val items = awaitItem()

            assertThat(items).hasSize(2)
            // Sorted by checkedAt DESC
            assertThat(items[0].itemName).isEqualTo("Second")
            assertThat(items[1].itemName).isEqualTo("First")

            cancelAndIgnoreRemainingEvents()
        }
    }

    // endregion

    // region Foreign Key Tests

    @Test
    fun cascade_delete_removes_list_items_when_item_deleted() = runTest {
        itemDao.insert(createTestItem())
        currentListDao.insert(createTestCurrentListItem())

        // Verify item exists in list
        assertThat(currentListDao.getCount()).isEqualTo(1)

        // Delete the parent item
        itemDao.hardDelete(TEST_ITEM_ID)

        // List item should be cascade deleted
        assertThat(currentListDao.getCount()).isEqualTo(0)
    }

    // endregion
}
