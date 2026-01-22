package com.restock.core.data.database.dao

import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for [ItemDao].
 *
 * Tests CRUD operations, search functionality, and Flow-based observations.
 */
@RunWith(AndroidJUnit4::class)
class ItemDaoTest : BaseDaoTest() {

    // region Insert Tests

    @Test
    fun insert_item_can_be_retrieved_by_id() = runTest {
        val item = createTestItem()

        itemDao.insert(item)
        val retrieved = itemDao.getById(TEST_ITEM_ID)

        assertThat(retrieved).isNotNull()
        assertThat(retrieved?.id).isEqualTo(TEST_ITEM_ID)
        assertThat(retrieved?.name).isEqualTo(TEST_ITEM_NAME)
        assertThat(retrieved?.normalizedName).isEqualTo(TEST_NORMALIZED_NAME)
    }

    @Test
    fun insert_replaces_on_conflict() = runTest {
        val item = createTestItem(name = "Milk")
        itemDao.insert(item)

        val updatedItem = item.copy(name = "Whole Milk")
        itemDao.insert(updatedItem)

        val retrieved = itemDao.getById(TEST_ITEM_ID)
        assertThat(retrieved?.name).isEqualTo("Whole Milk")
    }

    @Test
    fun insertAll_inserts_multiple_items() = runTest {
        val items = listOf(
            createTestItem(id = "1", name = "Milk", normalizedName = "milk"),
            createTestItem(id = "2", name = "Bread", normalizedName = "bread"),
            createTestItem(id = "3", name = "Eggs", normalizedName = "eggs")
        )

        itemDao.insertAll(items)
        val count = itemDao.getCount()

        assertThat(count).isEqualTo(3)
    }

    // endregion

    // region Query Tests

    @Test
    fun getActiveItems_returns_only_active_items() = runTest {
        itemDao.insert(createTestItem(id = "1", name = "Active Item", isActive = true))
        itemDao.insert(createTestItem(id = "2", name = "Inactive Item", normalizedName = "inactive", isActive = false))

        val activeItems = itemDao.getActiveItems()

        assertThat(activeItems).hasSize(1)
        assertThat(activeItems[0].name).isEqualTo("Active Item")
    }

    @Test
    fun getActiveItems_sorted_by_name_ascending() = runTest {
        itemDao.insert(createTestItem(id = "1", name = "Zebra", normalizedName = "zebra"))
        itemDao.insert(createTestItem(id = "2", name = "Apple", normalizedName = "apple"))
        itemDao.insert(createTestItem(id = "3", name = "Milk", normalizedName = "milk"))

        val items = itemDao.getActiveItems()

        assertThat(items.map { it.name }).containsExactly("Apple", "Milk", "Zebra").inOrder()
    }

    @Test
    fun findByNormalizedName_returns_matching_item() = runTest {
        itemDao.insert(createTestItem())

        val found = itemDao.findByNormalizedName(TEST_NORMALIZED_NAME)

        assertThat(found).isNotNull()
        assertThat(found?.name).isEqualTo(TEST_ITEM_NAME)
    }

    @Test
    fun findByNormalizedName_returns_null_when_not_found() = runTest {
        itemDao.insert(createTestItem())

        val found = itemDao.findByNormalizedName("nonexistent")

        assertThat(found).isNull()
    }

    @Test
    fun getById_returns_null_for_nonexistent_id() = runTest {
        val result = itemDao.getById("nonexistent-id")

        assertThat(result).isNull()
    }

    // endregion

    // region Search Tests

    @Test
    fun searchByNamePrefix_finds_matching_items() = runTest {
        itemDao.insert(createTestItem(id = "1", name = "Milk", normalizedName = "milk"))
        itemDao.insert(createTestItem(id = "2", name = "Milkshake", normalizedName = "milkshake"))
        itemDao.insert(createTestItem(id = "3", name = "Bread", normalizedName = "bread"))

        val results = itemDao.searchByNamePrefix("Milk")

        assertThat(results).hasSize(2)
        assertThat(results.map { it.name }).containsExactly("Milk", "Milkshake")
    }

    @Test
    fun searchByNamePrefix_respects_limit() = runTest {
        repeat(15) { i ->
            itemDao.insert(createTestItem(id = "item-$i", name = "Milk $i", normalizedName = "milk$i"))
        }

        val results = itemDao.searchByNamePrefix("Milk", limit = 5)

        assertThat(results).hasSize(5)
    }

    @Test
    fun searchByNamePrefix_excludes_inactive_items() = runTest {
        itemDao.insert(createTestItem(id = "1", name = "Milk", normalizedName = "milk", isActive = true))
        itemDao.insert(createTestItem(id = "2", name = "Milkshake", normalizedName = "milkshake", isActive = false))

        val results = itemDao.searchByNamePrefix("Milk")

        assertThat(results).hasSize(1)
    }

    @Test
    fun searchByName_finds_items_containing_query() = runTest {
        itemDao.insert(createTestItem(id = "1", name = "Whole Milk", normalizedName = "whole milk"))
        itemDao.insert(createTestItem(id = "2", name = "Skim Milk", normalizedName = "skim milk"))
        itemDao.insert(createTestItem(id = "3", name = "Bread", normalizedName = "bread"))

        val results = itemDao.searchByName("Milk")

        assertThat(results).hasSize(2)
    }

    // endregion

    // region Update Tests

    @Test
    fun update_modifies_existing_item() = runTest {
        val item = createTestItem()
        itemDao.insert(item)

        val updated = item.copy(name = "Skim Milk")
        itemDao.update(updated)

        val retrieved = itemDao.getById(TEST_ITEM_ID)
        assertThat(retrieved?.name).isEqualTo("Skim Milk")
    }

    // endregion

    // region Delete Tests

    @Test
    fun softDelete_marks_item_inactive() = runTest {
        itemDao.insert(createTestItem())

        itemDao.softDelete(TEST_ITEM_ID)

        val retrieved = itemDao.getById(TEST_ITEM_ID)
        assertThat(retrieved?.isActive).isFalse()
    }

    @Test
    fun softDelete_item_not_in_active_list() = runTest {
        itemDao.insert(createTestItem())

        itemDao.softDelete(TEST_ITEM_ID)

        val activeItems = itemDao.getActiveItems()
        assertThat(activeItems).isEmpty()
    }

    @Test
    fun hardDelete_removes_item_completely() = runTest {
        itemDao.insert(createTestItem())

        itemDao.hardDelete(TEST_ITEM_ID)

        val retrieved = itemDao.getById(TEST_ITEM_ID)
        assertThat(retrieved).isNull()
    }

    // endregion

    // region Count Tests

    @Test
    fun getCount_returns_active_item_count() = runTest {
        itemDao.insert(createTestItem(id = "1", name = "Item 1", normalizedName = "item1", isActive = true))
        itemDao.insert(createTestItem(id = "2", name = "Item 2", normalizedName = "item2", isActive = true))
        itemDao.insert(createTestItem(id = "3", name = "Item 3", normalizedName = "item3", isActive = false))

        val count = itemDao.getCount()

        assertThat(count).isEqualTo(2)
    }

    @Test
    fun getCount_returns_zero_for_empty_database() = runTest {
        val count = itemDao.getCount()

        assertThat(count).isEqualTo(0)
    }

    // endregion

    // region Flow Tests

    @Test
    fun observeActiveItems_emits_on_insert() = runTest {
        itemDao.observeActiveItems().test {
            assertThat(awaitItem()).isEmpty()

            itemDao.insert(createTestItem())
            val items = awaitItem()

            assertThat(items).hasSize(1)
            assertThat(items[0].name).isEqualTo(TEST_ITEM_NAME)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun observeActiveItems_emits_on_update() = runTest {
        val item = createTestItem()
        itemDao.insert(item)

        itemDao.observeActiveItems().test {
            val initial = awaitItem()
            assertThat(initial[0].name).isEqualTo(TEST_ITEM_NAME)

            itemDao.update(item.copy(name = "Updated Name"))
            val updated = awaitItem()

            assertThat(updated[0].name).isEqualTo("Updated Name")

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun observeActiveItems_emits_on_delete() = runTest {
        itemDao.insert(createTestItem())

        itemDao.observeActiveItems().test {
            val initial = awaitItem()
            assertThat(initial).hasSize(1)

            itemDao.softDelete(TEST_ITEM_ID)
            val afterDelete = awaitItem()

            assertThat(afterDelete).isEmpty()

            cancelAndIgnoreRemainingEvents()
        }
    }

    // endregion

    // region Edge Cases

    @Test
    fun normalized_name_unique_constraint() = runTest {
        // First item with normalized name "milk"
        itemDao.insert(createTestItem(id = "1", name = "Milk", normalizedName = "milk"))

        // Second item with same normalized name should replace (REPLACE conflict strategy)
        itemDao.insert(createTestItem(id = "1", name = "2% Milk", normalizedName = "milk"))

        val items = itemDao.getActiveItems()
        assertThat(items).hasSize(1)
        assertThat(items[0].name).isEqualTo("2% Milk")
    }

    // endregion
}
