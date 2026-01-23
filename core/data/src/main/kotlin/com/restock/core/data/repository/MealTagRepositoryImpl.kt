package com.restock.core.data.repository

import com.restock.core.data.database.dao.ItemDao
import com.restock.core.data.database.dao.MealTagDao
import com.restock.core.data.database.entity.MealTagEntity
import com.restock.core.data.di.IoDispatcher
import com.restock.core.data.mapper.toDomain
import com.restock.core.data.mapper.toGroceryItems
import com.restock.core.domain.model.GroceryItem
import com.restock.core.domain.model.MealTag
import com.restock.core.domain.model.MealType
import com.restock.core.domain.repository.MealTagRepository
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * Implementation of [MealTagRepository] using Room database.
 *
 * Manages meal-item associations, enabling users to categorize groceries
 * by meal type (breakfast, lunch, dinner, snack) and view items grouped
 * by when they're typically used.
 */
@Singleton
class MealTagRepositoryImpl
    @Inject
    constructor(
        private val mealTagDao: MealTagDao,
        private val itemDao: ItemDao,
        @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    ) : MealTagRepository {
        override fun observeAllTags(): Flow<List<MealTag>> =
            mealTagDao.observeAllTags()
                .map { entities -> entities.toDomain() }
                .flowOn(ioDispatcher)

        override fun observeTagsForItem(itemId: String): Flow<List<MealTag>> =
            mealTagDao.observeTagsForItem(itemId)
                .map { entities -> entities.toDomain() }
                .flowOn(ioDispatcher)

        override suspend fun getTagsForItem(itemId: String): List<MealTag> =
            withContext(ioDispatcher) {
                mealTagDao.getTagsForItem(itemId).toDomain()
            }

        override suspend fun getItemsByMealType(mealType: MealType): List<GroceryItem> =
            withContext(ioDispatcher) {
                mealTagDao.getItemsByMealType(mealType.name.lowercase()).toGroceryItems()
            }

        override suspend fun addTag(
            itemId: String,
            mealType: MealType,
        ): Result<Unit> =
            withContext(ioDispatcher) {
                runCatching {
                    val existingTag = mealTagDao.findTag(itemId, mealType.name.lowercase())

                    if (existingTag != null) {
                        // Increment frequency count for existing tag
                        mealTagDao.incrementFrequency(itemId, mealType.name.lowercase())
                    } else {
                        // Create new tag
                        val tag =
                            MealTagEntity(
                                id = UUID.randomUUID().toString(),
                                itemId = itemId,
                                mealType = mealType.name.lowercase(),
                                frequencyCount = 1,
                            )
                        mealTagDao.insert(tag)
                    }
                }
            }

        override suspend fun removeTag(
            itemId: String,
            mealType: MealType,
        ): Result<Unit> =
            withContext(ioDispatcher) {
                runCatching {
                    mealTagDao.delete(itemId, mealType.name.lowercase())
                }
            }

        override suspend fun removeAllTagsForItem(itemId: String): Result<Unit> =
            withContext(ioDispatcher) {
                runCatching {
                    mealTagDao.deleteAllForItem(itemId)
                }
            }
    }
