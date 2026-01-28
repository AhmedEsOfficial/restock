package com.restock.core.data.repository

import com.restock.core.data.database.dao.CurrentListDao
import com.restock.core.data.database.dao.ItemDao
import com.restock.core.data.database.dao.MealTagDao
import com.restock.core.data.database.entity.MealTagEntity
import com.restock.core.data.di.IoDispatcher
import com.restock.core.data.mapper.toDomain
import com.restock.core.data.mapper.toGroceryItems
import com.restock.core.domain.model.GroceryItem
import com.restock.core.domain.model.ItemWithMealTags
import com.restock.core.domain.model.MealTag
import com.restock.core.domain.model.MealType
import com.restock.core.domain.model.normalizeItemName
import com.restock.core.domain.repository.MealTagRepository
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

/**
 * Implementation of [MealTagRepository] using Room database.
 *
 * Manages meal-item associations, enabling users to categorize groceries
 * by meal type (breakfast, lunch, dinner, snack) and view items grouped
 * by when they're typically used.
 *
 * Also implements tag suggestion based on:
 * 1. Keyword matching for common meal items (breakfast foods, etc.)
 * 2. Historical patterns from similar items the user has tagged
 */
@Singleton
class MealTagRepositoryImpl
    @Inject
    constructor(
        private val mealTagDao: MealTagDao,
        private val itemDao: ItemDao,
        private val currentListDao: CurrentListDao,
        @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    ) : MealTagRepository {
        override suspend fun addTag(
            itemId: String,
            mealType: MealType,
        ): Result<MealTag> =
            withContext(ioDispatcher) {
                runCatching {
                    val existing = mealTagDao.getTag(itemId, mealType.name.lowercase())

                    if (existing != null) {
                        // Increment frequency for existing tag
                        mealTagDao.incrementFrequency(itemId, mealType.name.lowercase())
                        MealTag(
                            id = existing.id,
                            itemId = existing.itemId,
                            mealType = mealType,
                            frequencyCount = existing.frequencyCount + 1,
                        )
                    } else {
                        // Create new tag
                        val entity =
                            MealTagEntity(
                                id = UUID.randomUUID().toString(),
                                itemId = itemId,
                                mealType = mealType.name.lowercase(),
                                frequencyCount = 1,
                            )
                        mealTagDao.insert(entity)
                        MealTag(
                            id = entity.id,
                            itemId = entity.itemId,
                            mealType = mealType,
                            frequencyCount = 1,
                        )
                    }
                }
            }

        override suspend fun removeTag(
            itemId: String,
            mealType: MealType,
        ): Result<Unit> =
            withContext(ioDispatcher) {
                runCatching {
                    mealTagDao.deleteTag(itemId, mealType.name.lowercase())
                }
            }

        override suspend fun getTagsForItem(itemId: String): List<MealType> =
            withContext(ioDispatcher) {
                mealTagDao.getTagsForItem(itemId)
                    .mapNotNull { MealType.fromString(it.mealType) }
            }

        override suspend fun getItemsByMeal(mealType: MealType): List<GroceryItem> =
            withContext(ioDispatcher) {
                mealTagDao.getItemsByMealType(mealType.name.lowercase()).toGroceryItems()
            }

        override fun observeItemsWithTags(): Flow<List<ItemWithMealTags>> =
            combine(
                currentListDao.observeCurrentList(),
                mealTagDao.observeAllTags(),
            ) { currentList, allTags ->
                val tagsByItem = allTags.groupBy { it.itemId }

                currentList.map { listItem ->
                    val tags =
                        tagsByItem[listItem.itemId]
                            ?.mapNotNull { MealType.fromString(it.mealType) }
                            ?: emptyList()

                    ItemWithMealTags(
                        item = listItem.toDomain(),
                        mealTags = tags,
                    )
                }
            }.flowOn(ioDispatcher)

        override suspend fun suggestTag(itemName: String): MealType? =
            withContext(ioDispatcher) {
                val normalized = itemName.normalizeItemName()

                // Check common breakfast items
                if (BREAKFAST_KEYWORDS.any { normalized.contains(it) }) {
                    return@withContext MealType.BREAKFAST
                }

                // Check common lunch items
                if (LUNCH_KEYWORDS.any { normalized.contains(it) }) {
                    return@withContext MealType.LUNCH
                }

                // Check common dinner items
                if (DINNER_KEYWORDS.any { normalized.contains(it) }) {
                    return@withContext MealType.DINNER
                }

                // Check common snack items
                if (SNACK_KEYWORDS.any { normalized.contains(it) }) {
                    return@withContext MealType.SNACK
                }

                // Check historical tags for similar items
                val historicalTag = mealTagDao.getMostFrequentTagForSimilarItem(normalized)
                if (historicalTag != null && historicalTag.frequencyCount >= MIN_FREQUENCY_FOR_SUGGESTION) {
                    return@withContext MealType.fromString(historicalTag.mealType)
                }

                null
            }

        companion object {
            /**
             * Minimum frequency count required for historical tag suggestions.
             * Ensures we have enough data before making suggestions.
             */
            const val MIN_FREQUENCY_FOR_SUGGESTION = 3

            /**
             * Keywords commonly associated with breakfast items.
             */
            val BREAKFAST_KEYWORDS =
                listOf(
                    "egg", "bacon", "cereal", "oatmeal", "pancake", "waffle",
                    "toast", "yogurt", "juice", "coffee", "milk", "bagel",
                    "muffin", "croissant", "granola",
                )

            /**
             * Keywords commonly associated with lunch items.
             */
            val LUNCH_KEYWORDS =
                listOf(
                    "sandwich", "salad", "soup", "wrap", "deli", "bread",
                    "cheese", "turkey", "ham", "hummus", "pita",
                )

            /**
             * Keywords commonly associated with dinner items.
             */
            val DINNER_KEYWORDS =
                listOf(
                    "chicken", "beef", "steak", "fish", "salmon", "pasta",
                    "rice", "potato", "vegetable", "broccoli", "carrot",
                )

            /**
             * Keywords commonly associated with snack items.
             */
            val SNACK_KEYWORDS =
                listOf(
                    "chip", "cracker", "cookie", "candy", "nut", "pretzel",
                    "popcorn", "fruit", "apple", "banana", "grape",
                )
        }
    }
