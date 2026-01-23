package com.restock.core.data.repository

import com.restock.core.data.database.dao.CurrentListDao
import com.restock.core.data.database.dao.ItemDao
import com.restock.core.data.database.entity.CurrentListEntity
import com.restock.core.data.di.IoDispatcher
import com.restock.core.data.mapper.toDomain
import com.restock.core.domain.model.CurrentListItem
import com.restock.core.domain.repository.CurrentListRepository
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * Implementation of [CurrentListRepository] using Room database.
 *
 * Manages the current shopping list with support for reordering,
 * checking/unchecking items, and quantity management.
 */
@Singleton
class CurrentListRepositoryImpl
    @Inject
    constructor(
        private val currentListDao: CurrentListDao,
        private val itemDao: ItemDao,
        @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    ) : CurrentListRepository {
        override fun observeCurrentList(): Flow<List<CurrentListItem>> =
            currentListDao.observeCurrentList()
                .map { items -> items.toDomain() }
                .flowOn(ioDispatcher)

        override fun observeUncheckedItems(): Flow<List<CurrentListItem>> =
            currentListDao.observeUncheckedItems()
                .map { items -> items.toDomain() }
                .flowOn(ioDispatcher)

        override fun observeCheckedItems(): Flow<List<CurrentListItem>> =
            currentListDao.observeCheckedItems()
                .map { items -> items.toDomain() }
                .flowOn(ioDispatcher)

        override suspend fun getCurrentList(): List<CurrentListItem> =
            withContext(ioDispatcher) {
                currentListDao.getAll().mapNotNull { entity ->
                    itemDao.getById(entity.itemId)?.let { item ->
                        CurrentListItem(
                            id = entity.id,
                            itemId = entity.itemId,
                            name = item.name,
                            normalizedName = item.normalizedName,
                            quantity = entity.quantity,
                            unit = entity.unit,
                            addedAt = Instant.ofEpochMilli(entity.addedAt),
                            checkedAt = entity.checkedAt?.let { Instant.ofEpochMilli(it) },
                            position = entity.position,
                            isChecked = entity.isChecked,
                        )
                    }
                }
            }

        override suspend fun getById(id: String): CurrentListItem? =
            withContext(ioDispatcher) {
                currentListDao.getById(id)?.let { entity ->
                    itemDao.getById(entity.itemId)?.let { item ->
                        CurrentListItem(
                            id = entity.id,
                            itemId = entity.itemId,
                            name = item.name,
                            normalizedName = item.normalizedName,
                            quantity = entity.quantity,
                            unit = entity.unit,
                            addedAt = Instant.ofEpochMilli(entity.addedAt),
                            checkedAt = entity.checkedAt?.let { Instant.ofEpochMilli(it) },
                            position = entity.position,
                            isChecked = entity.isChecked,
                        )
                    }
                }
            }

        override suspend fun isItemInList(itemId: String): Boolean =
            withContext(ioDispatcher) {
                currentListDao.findByItemId(itemId) != null
            }

        override suspend fun addItem(
            itemId: String,
            quantity: Int,
            unit: String?,
        ): Result<CurrentListItem> =
            withContext(ioDispatcher) {
                runCatching {
                    val item =
                        itemDao.getById(itemId)
                            ?: throw IllegalArgumentException("Item not found: $itemId")

                    val id = UUID.randomUUID().toString()
                    val now = Instant.now()
                    val position = currentListDao.getMaxPosition() + 1

                    val entity =
                        CurrentListEntity(
                            id = id,
                            itemId = itemId,
                            quantity = quantity,
                            unit = unit,
                            addedAt = now.toEpochMilli(),
                            checkedAt = null,
                            position = position,
                            isChecked = false,
                        )

                    currentListDao.insert(entity)

                    CurrentListItem(
                        id = id,
                        itemId = itemId,
                        name = item.name,
                        normalizedName = item.normalizedName,
                        quantity = quantity,
                        unit = unit,
                        addedAt = now,
                        checkedAt = null,
                        position = position,
                        isChecked = false,
                    )
                }
            }

        override suspend fun checkItem(id: String): Result<Unit> =
            withContext(ioDispatcher) {
                runCatching {
                    currentListDao.checkItem(id, Instant.now().toEpochMilli())
                }
            }

        override suspend fun uncheckItem(id: String): Result<Unit> =
            withContext(ioDispatcher) {
                runCatching {
                    currentListDao.uncheckItem(id)
                }
            }

        override suspend fun toggleChecked(id: String): Result<Unit> =
            withContext(ioDispatcher) {
                runCatching {
                    val item =
                        currentListDao.getById(id)
                            ?: throw IllegalArgumentException("List item not found: $id")

                    if (item.isChecked) {
                        currentListDao.uncheckItem(id)
                    } else {
                        currentListDao.checkItem(id, Instant.now().toEpochMilli())
                    }
                }
            }

        override suspend fun updateQuantity(
            id: String,
            quantity: Int,
        ): Result<Unit> =
            withContext(ioDispatcher) {
                runCatching {
                    require(quantity > 0) { "Quantity must be positive" }
                    currentListDao.updateQuantity(id, quantity)
                }
            }

        override suspend fun updatePosition(
            id: String,
            newPosition: Int,
        ): Result<Unit> =
            withContext(ioDispatcher) {
                runCatching {
                    currentListDao.updatePosition(id, newPosition)
                }
            }

        override suspend fun reorderItems(
            itemId: String,
            fromPosition: Int,
            toPosition: Int,
        ): Result<Unit> =
            withContext(ioDispatcher) {
                runCatching {
                    val allItems = currentListDao.getAll().sortedBy { it.position }

                    val updatedItems = allItems.toMutableList()
                    val movedItem =
                        updatedItems.find { it.id == itemId }
                            ?: throw IllegalArgumentException("Item not found in list: $itemId")

                    updatedItems.remove(movedItem)
                    val insertPosition = toPosition.coerceIn(0, updatedItems.size)
                    updatedItems.add(insertPosition, movedItem)

                    // Update positions for all affected items
                    updatedItems.forEachIndexed { index, item ->
                        if (item.position != index) {
                            currentListDao.updatePosition(item.id, index)
                        }
                    }
                }
            }

        override suspend fun removeItem(id: String): Result<Unit> =
            withContext(ioDispatcher) {
                runCatching {
                    currentListDao.delete(id)
                }
            }

        override suspend fun clearCheckedItems(): Result<Unit> =
            withContext(ioDispatcher) {
                runCatching {
                    currentListDao.deleteCheckedItems()
                }
            }

        override suspend fun clearAll(): Result<Unit> =
            withContext(ioDispatcher) {
                runCatching {
                    currentListDao.clearAll()
                }
            }

        override suspend fun getCount(): Int =
            withContext(ioDispatcher) {
                currentListDao.getCount()
            }

        override suspend fun getUncheckedCount(): Int =
            withContext(ioDispatcher) {
                currentListDao.getUncheckedCount()
            }
    }
