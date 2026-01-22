package com.restock.core.data.repository

import com.restock.core.data.database.dao.ItemDao
import com.restock.core.data.mapper.toDomain
import com.restock.core.data.mapper.toEntity
import com.restock.core.domain.model.GroceryItem
import com.restock.core.domain.repository.ItemRepository
import com.restock.core.data.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of [ItemRepository] using Room database.
 *
 * Provides access to the grocery item catalog with proper error handling
 * and dispatcher management for database operations.
 */
@Singleton
class ItemRepositoryImpl @Inject constructor(
    private val itemDao: ItemDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ItemRepository {

    override fun observeActiveItems(): Flow<List<GroceryItem>> =
        itemDao.observeActiveItems()
            .map { entities -> entities.toDomain() }
            .flowOn(ioDispatcher)

    override suspend fun getActiveItems(): List<GroceryItem> =
        withContext(ioDispatcher) {
            itemDao.getActiveItems().toDomain()
        }

    override suspend fun getById(id: String): GroceryItem? =
        withContext(ioDispatcher) {
            itemDao.getById(id)?.toDomain()
        }

    override suspend fun findByNormalizedName(name: String): GroceryItem? =
        withContext(ioDispatcher) {
            itemDao.findByNormalizedName(name)?.toDomain()
        }

    override suspend fun searchByNamePrefix(query: String, limit: Int): List<GroceryItem> =
        withContext(ioDispatcher) {
            itemDao.searchByNamePrefix(query, limit).toDomain()
        }

    override suspend fun searchByName(query: String, limit: Int): List<GroceryItem> =
        withContext(ioDispatcher) {
            itemDao.searchByName(query, limit).toDomain()
        }

    override suspend fun insert(item: GroceryItem): Result<GroceryItem> =
        withContext(ioDispatcher) {
            runCatching {
                itemDao.insert(item.toEntity())
                item
            }
        }

    override suspend fun update(item: GroceryItem): Result<Unit> =
        withContext(ioDispatcher) {
            runCatching {
                itemDao.update(item.toEntity())
            }
        }

    override suspend fun softDelete(id: String): Result<Unit> =
        withContext(ioDispatcher) {
            runCatching {
                itemDao.softDelete(id)
            }
        }

    override suspend fun hardDelete(id: String): Result<Unit> =
        withContext(ioDispatcher) {
            runCatching {
                itemDao.hardDelete(id)
            }
        }
}
