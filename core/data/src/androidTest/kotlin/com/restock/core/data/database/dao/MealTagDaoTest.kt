package com.restock.core.data.database.dao

import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.restock.core.data.database.entity.MealTagEntity
import com.restock.core.data.database.entity.MealTypes
import org.junit.Test
import org.junit.runner.RunWith
import kotlinx.coroutines.test.runTest

/**
 * Instrumented tests for [MealTagDao].
 *
 * Tests CRUD operations, meal type queries, frequency tracking,
 * and Flow-based observations.
 */
@RunWith(AndroidJUnit4::class)
class MealTagDaoTest : BaseDaoTest() {
    private fun createTestMealTag(
        id: String = "tag-1",
        itemId: String = TEST_ITEM_ID,
        mealType: String = MealTypes.BREAKFAST,
        frequencyCount: Int = 1,
    ) = MealTagEntity(
        id = id,
        itemId = itemId,
        mealType = mealType,
        frequencyCount = frequencyCount,
    )

    // region Insert Tests

    @Test
    fun insert_tag_can_be_retrieved() =
        runTest {
            itemDao.insert(createTestItem())
            val tag = createTestMealTag()

            mealTagDao.insert(tag)
            val tags = mealTagDao.getTagsForItem(TEST_ITEM_ID)

            assertThat(tags).hasSize(1)
            assertThat(tags[0].mealType).isEqualTo(MealTypes.BREAKFAST)
        }

    @Test
    fun insert_replaces_on_conflict() =
        runTest {
            itemDao.insert(createTestItem())
            mealTagDao.insert(createTestMealTag(frequencyCount = 1))

            mealTagDao.insert(createTestMealTag(frequencyCount = 5))

            val tags = mealTagDao.getTagsForItem(TEST_ITEM_ID)
            assertThat(tags[0].frequencyCount).isEqualTo(5)
        }

    @Test
    fun insert_multiple_meal_types_for_same_item() =
        runTest {
            itemDao.insert(createTestItem())

            mealTagDao.insert(createTestMealTag(id = "tag-1", mealType = MealTypes.BREAKFAST))
            mealTagDao.insert(createTestMealTag(id = "tag-2", mealType = MealTypes.LUNCH))
            mealTagDao.insert(createTestMealTag(id = "tag-3", mealType = MealTypes.DINNER))

            val tags = mealTagDao.getTagsForItem(TEST_ITEM_ID)
            assertThat(tags).hasSize(3)
        }

    // endregion

    // region Query Tests

    @Test
    fun getTagsForItem_returns_empty_when_no_tags() =
        runTest {
            itemDao.insert(createTestItem())

            val tags = mealTagDao.getTagsForItem(TEST_ITEM_ID)

            assertThat(tags).isEmpty()
        }

    @Test
    fun findTag_returns_matching_tag() =
        runTest {
            itemDao.insert(createTestItem())
            mealTagDao.insert(createTestMealTag(mealType = MealTypes.BREAKFAST))

            val tag = mealTagDao.findTag(TEST_ITEM_ID, MealTypes.BREAKFAST)

            assertThat(tag).isNotNull()
            assertThat(tag?.mealType).isEqualTo(MealTypes.BREAKFAST)
        }

    @Test
    fun findTag_returns_null_when_not_found() =
        runTest {
            itemDao.insert(createTestItem())
            mealTagDao.insert(createTestMealTag(mealType = MealTypes.BREAKFAST))

            val tag = mealTagDao.findTag(TEST_ITEM_ID, MealTypes.DINNER)

            assertThat(tag).isNull()
        }

    @Test
    fun getItemsByMealType_returns_items_with_tag() =
        runTest {
            itemDao.insert(createTestItem(id = "item-1", name = "Eggs", normalizedName = "eggs"))
            itemDao.insert(createTestItem(id = "item-2", name = "Bacon", normalizedName = "bacon"))
            itemDao.insert(createTestItem(id = "item-3", name = "Coffee", normalizedName = "coffee"))

            mealTagDao.insert(createTestMealTag(id = "tag-1", itemId = "item-1", mealType = MealTypes.BREAKFAST))
            mealTagDao.insert(createTestMealTag(id = "tag-2", itemId = "item-2", mealType = MealTypes.BREAKFAST))
            mealTagDao.insert(createTestMealTag(id = "tag-3", itemId = "item-3", mealType = MealTypes.LUNCH))

            val breakfastItems = mealTagDao.getItemsByMealType(MealTypes.BREAKFAST)

            assertThat(breakfastItems).hasSize(2)
            assertThat(breakfastItems.map { it.name }).containsExactly("Eggs", "Bacon")
        }

    @Test
    fun getItemsByMealType_excludes_inactive_items() =
        runTest {
            itemDao.insert(createTestItem(id = "item-1", name = "Active", normalizedName = "active", isActive = true))
            itemDao.insert(
                createTestItem(id = "item-2", name = "Inactive", normalizedName = "inactive", isActive = false),
            )

            mealTagDao.insert(createTestMealTag(id = "tag-1", itemId = "item-1", mealType = MealTypes.BREAKFAST))
            mealTagDao.insert(createTestMealTag(id = "tag-2", itemId = "item-2", mealType = MealTypes.BREAKFAST))

            val items = mealTagDao.getItemsByMealType(MealTypes.BREAKFAST)

            assertThat(items).hasSize(1)
            assertThat(items[0].name).isEqualTo("Active")
        }

    @Test
    fun getItemsByMealType_sorted_by_frequency_descending() =
        runTest {
            itemDao.insert(createTestItem(id = "item-1", name = "Low Freq", normalizedName = "low"))
            itemDao.insert(createTestItem(id = "item-2", name = "High Freq", normalizedName = "high"))
            itemDao.insert(createTestItem(id = "item-3", name = "Med Freq", normalizedName = "med"))

            mealTagDao.insert(
                createTestMealTag(id = "tag-1", itemId = "item-1", mealType = MealTypes.BREAKFAST, frequencyCount = 1),
            )
            mealTagDao.insert(
                createTestMealTag(id = "tag-2", itemId = "item-2", mealType = MealTypes.BREAKFAST, frequencyCount = 10),
            )
            mealTagDao.insert(
                createTestMealTag(id = "tag-3", itemId = "item-3", mealType = MealTypes.BREAKFAST, frequencyCount = 5),
            )

            val items = mealTagDao.getItemsByMealType(MealTypes.BREAKFAST)

            assertThat(items.map { it.name }).containsExactly("High Freq", "Med Freq", "Low Freq").inOrder()
        }

    @Test
    fun getCountByMealType_returns_correct_count() =
        runTest {
            itemDao.insert(createTestItem(id = "item-1", normalizedName = "item1"))
            itemDao.insert(createTestItem(id = "item-2", normalizedName = "item2"))
            itemDao.insert(createTestItem(id = "item-3", normalizedName = "item3"))

            mealTagDao.insert(createTestMealTag(id = "tag-1", itemId = "item-1", mealType = MealTypes.BREAKFAST))
            mealTagDao.insert(createTestMealTag(id = "tag-2", itemId = "item-2", mealType = MealTypes.BREAKFAST))
            mealTagDao.insert(createTestMealTag(id = "tag-3", itemId = "item-3", mealType = MealTypes.LUNCH))

            val breakfastCount = mealTagDao.getCountByMealType(MealTypes.BREAKFAST)
            val lunchCount = mealTagDao.getCountByMealType(MealTypes.LUNCH)
            val dinnerCount = mealTagDao.getCountByMealType(MealTypes.DINNER)

            assertThat(breakfastCount).isEqualTo(2)
            assertThat(lunchCount).isEqualTo(1)
            assertThat(dinnerCount).isEqualTo(0)
        }

    // endregion

    // region Update Tests

    @Test
    fun incrementFrequency_increases_count() =
        runTest {
            itemDao.insert(createTestItem())
            mealTagDao.insert(createTestMealTag(frequencyCount = 1))

            mealTagDao.incrementFrequency(TEST_ITEM_ID, MealTypes.BREAKFAST)
            mealTagDao.incrementFrequency(TEST_ITEM_ID, MealTypes.BREAKFAST)

            val tags = mealTagDao.getTagsForItem(TEST_ITEM_ID)
            assertThat(tags[0].frequencyCount).isEqualTo(3)
        }

    // endregion

    // region Delete Tests

    @Test
    fun delete_removes_specific_tag() =
        runTest {
            itemDao.insert(createTestItem())
            mealTagDao.insert(createTestMealTag(id = "tag-1", mealType = MealTypes.BREAKFAST))
            mealTagDao.insert(createTestMealTag(id = "tag-2", mealType = MealTypes.LUNCH))

            mealTagDao.delete(TEST_ITEM_ID, MealTypes.BREAKFAST)

            val tags = mealTagDao.getTagsForItem(TEST_ITEM_ID)
            assertThat(tags).hasSize(1)
            assertThat(tags[0].mealType).isEqualTo(MealTypes.LUNCH)
        }

    @Test
    fun deleteAllForItem_removes_all_tags_for_item() =
        runTest {
            itemDao.insert(createTestItem(id = "item-1", normalizedName = "item1"))
            itemDao.insert(createTestItem(id = "item-2", normalizedName = "item2"))

            mealTagDao.insert(createTestMealTag(id = "tag-1", itemId = "item-1", mealType = MealTypes.BREAKFAST))
            mealTagDao.insert(createTestMealTag(id = "tag-2", itemId = "item-1", mealType = MealTypes.LUNCH))
            mealTagDao.insert(createTestMealTag(id = "tag-3", itemId = "item-2", mealType = MealTypes.BREAKFAST))

            mealTagDao.deleteAllForItem("item-1")

            val item1Tags = mealTagDao.getTagsForItem("item-1")
            val item2Tags = mealTagDao.getTagsForItem("item-2")

            assertThat(item1Tags).isEmpty()
            assertThat(item2Tags).hasSize(1)
        }

    // endregion

    // region Flow Tests

    @Test
    fun observeAllTags_emits_sorted_by_frequency() =
        runTest {
            itemDao.insert(createTestItem(id = "item-1", normalizedName = "item1"))
            itemDao.insert(createTestItem(id = "item-2", normalizedName = "item2"))

            mealTagDao.observeAllTags().test {
                assertThat(awaitItem()).isEmpty()

                mealTagDao.insert(createTestMealTag(id = "tag-1", itemId = "item-1", frequencyCount = 1))
                assertThat(awaitItem()).hasSize(1)

                mealTagDao.insert(createTestMealTag(id = "tag-2", itemId = "item-2", frequencyCount = 5))
                val tags = awaitItem()

                assertThat(tags).hasSize(2)
                assertThat(tags[0].frequencyCount).isEqualTo(5) // Sorted DESC
                assertThat(tags[1].frequencyCount).isEqualTo(1)

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun observeTagsForItem_emits_filtered_tags() =
        runTest {
            itemDao.insert(createTestItem(id = "item-1", normalizedName = "item1"))
            itemDao.insert(createTestItem(id = "item-2", normalizedName = "item2"))

            mealTagDao.insert(createTestMealTag(id = "tag-1", itemId = "item-1", mealType = MealTypes.BREAKFAST))
            mealTagDao.insert(createTestMealTag(id = "tag-2", itemId = "item-2", mealType = MealTypes.LUNCH))

            mealTagDao.observeTagsForItem("item-1").test {
                val tags = awaitItem()

                assertThat(tags).hasSize(1)
                assertThat(tags[0].mealType).isEqualTo(MealTypes.BREAKFAST)

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun observeTagsForItem_updates_on_frequency_change() =
        runTest {
            itemDao.insert(createTestItem())
            mealTagDao.insert(createTestMealTag(frequencyCount = 1))

            mealTagDao.observeTagsForItem(TEST_ITEM_ID).test {
                val initial = awaitItem()
                assertThat(initial[0].frequencyCount).isEqualTo(1)

                mealTagDao.incrementFrequency(TEST_ITEM_ID, MealTypes.BREAKFAST)
                val updated = awaitItem()

                assertThat(updated[0].frequencyCount).isEqualTo(2)

                cancelAndIgnoreRemainingEvents()
            }
        }

    // endregion

    // region Foreign Key Tests

    @Test
    fun cascade_delete_removes_tags_when_item_deleted() =
        runTest {
            itemDao.insert(createTestItem())
            mealTagDao.insert(createTestMealTag(id = "tag-1", mealType = MealTypes.BREAKFAST))
            mealTagDao.insert(createTestMealTag(id = "tag-2", mealType = MealTypes.LUNCH))

            assertThat(mealTagDao.getTagsForItem(TEST_ITEM_ID)).hasSize(2)

            itemDao.hardDelete(TEST_ITEM_ID)

            assertThat(mealTagDao.getTagsForItem(TEST_ITEM_ID)).isEmpty()
        }

    // endregion

    // region Meal Types Tests

    @Test
    fun all_meal_types_valid() =
        runTest {
            itemDao.insert(createTestItem())

            MealTypes.ALL.forEachIndexed { index, mealType ->
                mealTagDao.insert(createTestMealTag(id = "tag-$index", mealType = mealType))
            }

            val tags = mealTagDao.getTagsForItem(TEST_ITEM_ID)
            assertThat(tags).hasSize(4)
            assertThat(tags.map { it.mealType }).containsExactlyElementsIn(MealTypes.ALL)
        }

    // endregion

    // region Unique Constraint Tests

    @Test
    fun unique_constraint_on_item_meal_type_combination() =
        runTest {
            itemDao.insert(createTestItem())

            // Insert first tag
            mealTagDao.insert(createTestMealTag(id = "tag-1", mealType = MealTypes.BREAKFAST, frequencyCount = 1))

            // Insert with same item_id and meal_type but different id - should replace due to unique index
            mealTagDao.insert(createTestMealTag(id = "tag-1", mealType = MealTypes.BREAKFAST, frequencyCount = 10))

            val tags = mealTagDao.getTagsForItem(TEST_ITEM_ID)
            assertThat(tags).hasSize(1)
            assertThat(tags[0].frequencyCount).isEqualTo(10)
        }

    // endregion
}
