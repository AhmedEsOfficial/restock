package com.restock.core.data.database.dao

import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.restock.core.data.database.entity.PurchaseEntity
import org.junit.Test
import org.junit.runner.RunWith
import kotlinx.coroutines.test.runTest

/**
 * Instrumented tests for [PurchaseDao].
 *
 * Tests CRUD operations, purchase history queries, pattern-related queries,
 * and Flow-based observations.
 */
@RunWith(AndroidJUnit4::class)
class PurchaseDaoTest : BaseDaoTest() {
    private fun createTestPurchase(
        id: String = "purchase-1",
        itemId: String = TEST_ITEM_ID,
        purchasedAt: Long = TEST_CREATED_AT,
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

    // region Insert Tests

    @Test
    fun insert_purchase_and_retrieve_by_item() =
        runTest {
            itemDao.insert(createTestItem())
            val purchase = createTestPurchase()

            purchaseDao.insert(purchase)
            val purchases = purchaseDao.getPurchasesForItem(TEST_ITEM_ID)

            assertThat(purchases).hasSize(1)
            assertThat(purchases[0].id).isEqualTo("purchase-1")
            assertThat(purchases[0].itemId).isEqualTo(TEST_ITEM_ID)
        }

    @Test
    fun insertAll_inserts_multiple_purchases() =
        runTest {
            itemDao.insert(createTestItem())

            val purchases =
                listOf(
                    createTestPurchase(id = "p1", purchasedAt = 1000L),
                    createTestPurchase(id = "p2", purchasedAt = 2000L),
                    createTestPurchase(id = "p3", purchasedAt = 3000L),
                )

            purchaseDao.insertAll(purchases)
            val count = purchaseDao.getTotalCount()

            assertThat(count).isEqualTo(3)
        }

    // endregion

    // region Query Tests

    @Test
    fun getPurchasesForItem_sorted_by_date_ascending() =
        runTest {
            itemDao.insert(createTestItem())

            purchaseDao.insert(createTestPurchase(id = "p1", purchasedAt = 3000L))
            purchaseDao.insert(createTestPurchase(id = "p2", purchasedAt = 1000L))
            purchaseDao.insert(createTestPurchase(id = "p3", purchasedAt = 2000L))

            val purchases = purchaseDao.getPurchasesForItem(TEST_ITEM_ID)

            assertThat(purchases.map { it.id }).containsExactly("p2", "p3", "p1").inOrder()
        }

    @Test
    fun getLastPurchaseForItem_returns_most_recent() =
        runTest {
            itemDao.insert(createTestItem())

            purchaseDao.insert(createTestPurchase(id = "p1", purchasedAt = 1000L))
            purchaseDao.insert(createTestPurchase(id = "p2", purchasedAt = 3000L))
            purchaseDao.insert(createTestPurchase(id = "p3", purchasedAt = 2000L))

            val lastPurchase = purchaseDao.getLastPurchaseForItem(TEST_ITEM_ID)

            assertThat(lastPurchase?.id).isEqualTo("p2")
            assertThat(lastPurchase?.purchasedAt).isEqualTo(3000L)
        }

    @Test
    fun getLastPurchaseForItem_returns_null_when_no_purchases() =
        runTest {
            itemDao.insert(createTestItem())

            val lastPurchase = purchaseDao.getLastPurchaseForItem(TEST_ITEM_ID)

            assertThat(lastPurchase).isNull()
        }

    @Test
    fun getPurchaseCountForItem_returns_correct_count() =
        runTest {
            itemDao.insert(createTestItem())

            purchaseDao.insert(createTestPurchase(id = "p1"))
            purchaseDao.insert(createTestPurchase(id = "p2"))
            purchaseDao.insert(createTestPurchase(id = "p3"))

            val count = purchaseDao.getPurchaseCountForItem(TEST_ITEM_ID)

            assertThat(count).isEqualTo(3)
        }

    @Test
    fun getItemsWithMinPurchases_returns_eligible_items() =
        runTest {
            itemDao.insert(createTestItem(id = "item-1", normalizedName = "item1"))
            itemDao.insert(createTestItem(id = "item-2", normalizedName = "item2"))
            itemDao.insert(createTestItem(id = "item-3", normalizedName = "item3"))

            // Item 1: 3 purchases (eligible)
            purchaseDao.insert(createTestPurchase(id = "p1", itemId = "item-1", purchasedAt = 1000L))
            purchaseDao.insert(createTestPurchase(id = "p2", itemId = "item-1", purchasedAt = 2000L))
            purchaseDao.insert(createTestPurchase(id = "p3", itemId = "item-1", purchasedAt = 3000L))

            // Item 2: 1 purchase (not eligible)
            purchaseDao.insert(createTestPurchase(id = "p4", itemId = "item-2"))

            // Item 3: 4 purchases (eligible)
            purchaseDao.insert(createTestPurchase(id = "p5", itemId = "item-3", purchasedAt = 1000L))
            purchaseDao.insert(createTestPurchase(id = "p6", itemId = "item-3", purchasedAt = 2000L))
            purchaseDao.insert(createTestPurchase(id = "p7", itemId = "item-3", purchasedAt = 3000L))
            purchaseDao.insert(createTestPurchase(id = "p8", itemId = "item-3", purchasedAt = 4000L))

            val eligibleItems = purchaseDao.getItemsWithMinPurchases(3)

            assertThat(eligibleItems).hasSize(2)
            assertThat(eligibleItems).containsExactly("item-1", "item-3")
        }

    @Test
    fun getPurchasesInRange_returns_purchases_within_timeframe() =
        runTest {
            itemDao.insert(createTestItem())

            purchaseDao.insert(createTestPurchase(id = "p1", purchasedAt = 1000L))
            purchaseDao.insert(createTestPurchase(id = "p2", purchasedAt = 2000L))
            purchaseDao.insert(createTestPurchase(id = "p3", purchasedAt = 3000L))
            purchaseDao.insert(createTestPurchase(id = "p4", purchasedAt = 4000L))

            val purchases = purchaseDao.getPurchasesInRange(1500L, 3500L)

            assertThat(purchases).hasSize(2)
            assertThat(purchases.map { it.id }).containsExactly("p3", "p2").inOrder() // DESC order
        }

    // endregion

    // region Delete Tests

    @Test
    fun delete_removes_single_purchase() =
        runTest {
            itemDao.insert(createTestItem())
            purchaseDao.insert(createTestPurchase())

            purchaseDao.delete("purchase-1")

            val count = purchaseDao.getTotalCount()
            assertThat(count).isEqualTo(0)
        }

    @Test
    fun deleteForItem_removes_all_purchases_for_item() =
        runTest {
            itemDao.insert(createTestItem(id = "item-1", normalizedName = "item1"))
            itemDao.insert(createTestItem(id = "item-2", normalizedName = "item2"))

            purchaseDao.insert(createTestPurchase(id = "p1", itemId = "item-1"))
            purchaseDao.insert(createTestPurchase(id = "p2", itemId = "item-1"))
            purchaseDao.insert(createTestPurchase(id = "p3", itemId = "item-2"))

            purchaseDao.deleteForItem("item-1")

            val total = purchaseDao.getTotalCount()
            assertThat(total).isEqualTo(1)

            val item2Purchases = purchaseDao.getPurchaseCountForItem("item-2")
            assertThat(item2Purchases).isEqualTo(1)
        }

    // endregion

    // region Flow Tests

    @Test
    fun observeAllPurchases_emits_sorted_by_date_descending() =
        runTest {
            itemDao.insert(createTestItem())

            purchaseDao.observeAllPurchases().test {
                assertThat(awaitItem()).isEmpty()

                purchaseDao.insert(createTestPurchase(id = "p1", purchasedAt = 1000L))
                assertThat(awaitItem()).hasSize(1)

                purchaseDao.insert(createTestPurchase(id = "p2", purchasedAt = 3000L))
                val items = awaitItem()

                assertThat(items).hasSize(2)
                assertThat(items[0].id).isEqualTo("p2") // Most recent first
                assertThat(items[1].id).isEqualTo("p1")

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun observePurchasesForItem_emits_filtered_by_item() =
        runTest {
            itemDao.insert(createTestItem(id = "item-1", normalizedName = "item1"))
            itemDao.insert(createTestItem(id = "item-2", normalizedName = "item2"))

            purchaseDao.insert(createTestPurchase(id = "p1", itemId = "item-1"))
            purchaseDao.insert(createTestPurchase(id = "p2", itemId = "item-2"))

            purchaseDao.observePurchasesForItem("item-1").test {
                val items = awaitItem()

                assertThat(items).hasSize(1)
                assertThat(items[0].itemId).isEqualTo("item-1")

                cancelAndIgnoreRemainingEvents()
            }
        }

    // endregion

    // region Foreign Key Tests

    @Test
    fun cascade_delete_removes_purchases_when_item_deleted() =
        runTest {
            itemDao.insert(createTestItem())
            purchaseDao.insert(createTestPurchase(id = "p1"))
            purchaseDao.insert(createTestPurchase(id = "p2"))

            assertThat(purchaseDao.getTotalCount()).isEqualTo(2)

            itemDao.hardDelete(TEST_ITEM_ID)

            assertThat(purchaseDao.getTotalCount()).isEqualTo(0)
        }

    // endregion

    // region Edge Cases

    @Test
    fun purchase_with_optional_fields() =
        runTest {
            itemDao.insert(createTestItem())

            val purchase =
                createTestPurchase(
                    priceCents = 599,
                    storeId = "store-123",
                )
            purchaseDao.insert(purchase)

            val purchases = purchaseDao.getPurchasesForItem(TEST_ITEM_ID)
            assertThat(purchases[0].priceCents).isEqualTo(599)
            assertThat(purchases[0].storeId).isEqualTo("store-123")
        }

    @Test
    fun purchase_with_null_optional_fields() =
        runTest {
            itemDao.insert(createTestItem())

            val purchase =
                createTestPurchase(
                    priceCents = null,
                    storeId = null,
                )
            purchaseDao.insert(purchase)

            val purchases = purchaseDao.getPurchasesForItem(TEST_ITEM_ID)
            assertThat(purchases[0].priceCents).isNull()
            assertThat(purchases[0].storeId).isNull()
        }

    // endregion
}
