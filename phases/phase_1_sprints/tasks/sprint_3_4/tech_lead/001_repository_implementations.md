# Task: Implement Item and CurrentList Repositories

**Sprint:** 3-4 (The List Feature)
**Owner:** Tech Lead / Senior Developer
**Estimate:** 8 hours (T-3.1 + T-3.2 combined)
**Priority:** P0
**Prerequisites:** Sprint 1-2 completed (database, DI setup)

---

## Sprint Context

Sprint 3-4 delivers the core list functionality—the primary user-facing feature of Restock. This task implements the repository layer that connects use cases to the Room database.

### Sprint Theme
> *"Make the simple list feel magical."*

### Key Metrics
- Time to first item added: < 15 seconds
- Add item interaction: < 2 taps
- Check item interaction: 1 tap
- List scroll performance: 60 fps
- Offline functionality: 100%

---

## Task Description

Implement ItemRepository and CurrentListRepository with Room integration, following the repository pattern established in the architecture.

## Repository Interfaces (in :core:domain)

### ItemRepository.kt

```kotlin
package com.restock.core.domain.repository

import com.restock.core.domain.model.GroceryItem
import kotlinx.coroutines.flow.Flow

interface ItemRepository {
    fun observeActiveItems(): Flow<List<GroceryItem>>
    suspend fun getById(id: String): GroceryItem?
    suspend fun findByNormalizedName(name: String): GroceryItem?
    suspend fun searchByName(query: String, limit: Int = 10): List<GroceryItem>
    suspend fun insert(item: GroceryItem): Result<GroceryItem>
    suspend fun update(item: GroceryItem): Result<Unit>
    suspend fun getOrCreate(name: String): GroceryItem
    suspend fun softDelete(id: String): Result<Unit>
}
```

### CurrentListRepository.kt

```kotlin
package com.restock.core.domain.repository

import com.restock.core.domain.model.CurrentListItem
import com.restock.core.domain.model.PositionUpdate
import kotlinx.coroutines.flow.Flow

interface CurrentListRepository {
    fun observeCurrentList(): Flow<List<CurrentListItem>>
    suspend fun getById(id: String): CurrentListItem?
    suspend fun findByItemId(itemId: String): CurrentListItem?
    suspend fun getCurrentItemIds(): List<String>
    suspend fun addItem(itemId: String, quantity: Int = 1, unit: String? = null): Result<CurrentListItem>
    suspend fun addItemAt(itemId: String, position: Int, quantity: Int = 1, unit: String? = null): Result<CurrentListItem>
    suspend fun checkItem(id: String): Result<Unit>
    suspend fun uncheckItem(id: String): Result<Unit>
    suspend fun updateQuantity(id: String, quantity: Int, unit: String?): Result<Unit>
    suspend fun updatePositions(updates: List<PositionUpdate>): Result<Unit>
    suspend fun deleteItem(id: String): Result<Unit>
    suspend fun deleteAllChecked(): Result<Unit>
}
```

## Repository Implementations (in :core:data)

### ItemRepositoryImpl.kt

```kotlin
package com.restock.core.data.repository

import com.restock.core.data.database.dao.ItemDao
import com.restock.core.data.database.entity.ItemEntity
import com.restock.core.data.di.IoDispatcher
import com.restock.core.domain.model.GroceryItem
import com.restock.core.domain.repository.ItemRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

class ItemRepositoryImpl @Inject constructor(
    private val itemDao: ItemDao,
    @IoDispatcher private val dispatcher: CoroutineDispatcher,
) : ItemRepository {

    override fun observeActiveItems(): Flow<List<GroceryItem>> =
        itemDao.observeActiveItems()
            .map { entities -> entities.map { it.toDomain() } }
            .flowOn(dispatcher)

    override suspend fun getById(id: String): GroceryItem? =
        withContext(dispatcher) {
            itemDao.getById(id)?.toDomain()
        }

    override suspend fun findByNormalizedName(name: String): GroceryItem? =
        withContext(dispatcher) {
            itemDao.findByNormalizedName(name.normalizeItemName())?.toDomain()
        }

    override suspend fun searchByName(query: String, limit: Int): List<GroceryItem> =
        withContext(dispatcher) {
            itemDao.searchByName(query.normalizeItemName(), limit)
                .map { it.toDomain() }
        }

    override suspend fun insert(item: GroceryItem): Result<GroceryItem> =
        withContext(dispatcher) {
            runCatching {
                itemDao.insert(item.toEntity())
                item
            }
        }

    override suspend fun update(item: GroceryItem): Result<Unit> =
        withContext(dispatcher) {
            runCatching {
                itemDao.update(item.toEntity())
            }
        }

    override suspend fun getOrCreate(name: String): GroceryItem =
        withContext(dispatcher) {
            val normalized = name.normalizeItemName()
            itemDao.findByNormalizedName(normalized)?.toDomain()
                ?: run {
                    val newItem = GroceryItem(
                        id = UUID.randomUUID().toString(),
                        name = name.trim(),
                        normalizedName = normalized,
                        createdAt = System.currentTimeMillis(),
                        isActive = true,
                    )
                    itemDao.insert(newItem.toEntity())
                    newItem
                }
        }

    override suspend fun softDelete(id: String): Result<Unit> =
        withContext(dispatcher) {
            runCatching {
                itemDao.softDelete(id)
            }
        }
}

// Extension functions for mapping
private fun ItemEntity.toDomain() = GroceryItem(
    id = id,
    name = name,
    normalizedName = normalizedName,
    createdAt = createdAt,
    isActive = isActive,
)

private fun GroceryItem.toEntity() = ItemEntity(
    id = id,
    name = name,
    normalizedName = normalizedName,
    createdAt = createdAt,
    isActive = isActive,
)

private fun String.normalizeItemName(): String =
    this.trim().lowercase().replace(Regex("\\s+"), " ")
```

### CurrentListRepositoryImpl.kt

```kotlin
package com.restock.core.data.repository

import com.restock.core.data.database.dao.CurrentListDao
import com.restock.core.data.database.entity.CurrentListEntity
import com.restock.core.data.di.IoDispatcher
import com.restock.core.domain.model.CurrentListItem
import com.restock.core.domain.model.PositionUpdate
import com.restock.core.domain.repository.CurrentListRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

class CurrentListRepositoryImpl @Inject constructor(
    private val currentListDao: CurrentListDao,
    @IoDispatcher private val dispatcher: CoroutineDispatcher,
) : CurrentListRepository {

    override fun observeCurrentList(): Flow<List<CurrentListItem>> =
        currentListDao.observeCurrentListWithItems()
            .map { entities -> entities.map { it.toDomain() } }
            .flowOn(dispatcher)

    override suspend fun getById(id: String): CurrentListItem? =
        withContext(dispatcher) {
            currentListDao.getById(id)?.let { entity ->
                // Need to fetch item name for full domain object
                // This is handled by the join query in observeCurrentList
                null // For single item lookup, use a different approach
            }
        }

    override suspend fun findByItemId(itemId: String): CurrentListItem? =
        withContext(dispatcher) {
            currentListDao.findUncheckedByItemId(itemId)?.let { entity ->
                // Return basic info, name populated from join
                CurrentListItem(
                    id = entity.id,
                    itemId = entity.itemId,
                    name = "", // Will be populated in UI layer
                    quantity = entity.quantity,
                    unit = entity.unit,
                    addedAt = entity.addedAt,
                    checkedAt = entity.checkedAt,
                    position = entity.position,
                    isChecked = entity.isChecked,
                )
            }
        }

    override suspend fun getCurrentItemIds(): List<String> =
        withContext(dispatcher) {
            currentListDao.getCurrentItemIds()
        }

    override suspend fun addItem(
        itemId: String,
        quantity: Int,
        unit: String?
    ): Result<CurrentListItem> = withContext(dispatcher) {
        runCatching {
            val maxPosition = currentListDao.getMaxPosition() ?: -1
            val entry = CurrentListEntity(
                id = UUID.randomUUID().toString(),
                itemId = itemId,
                quantity = quantity,
                unit = unit,
                addedAt = System.currentTimeMillis(),
                checkedAt = null,
                position = maxPosition + 1,
                isChecked = false,
            )
            currentListDao.insert(entry)
            CurrentListItem(
                id = entry.id,
                itemId = entry.itemId,
                name = "", // Populated by caller
                quantity = entry.quantity,
                unit = entry.unit,
                addedAt = entry.addedAt,
                checkedAt = entry.checkedAt,
                position = entry.position,
                isChecked = entry.isChecked,
            )
        }
    }

    override suspend fun addItemAt(
        itemId: String,
        position: Int,
        quantity: Int,
        unit: String?
    ): Result<CurrentListItem> = withContext(dispatcher) {
        runCatching {
            val entry = CurrentListEntity(
                id = UUID.randomUUID().toString(),
                itemId = itemId,
                quantity = quantity,
                unit = unit,
                addedAt = System.currentTimeMillis(),
                checkedAt = null,
                position = position,
                isChecked = false,
            )
            currentListDao.insert(entry)
            CurrentListItem(
                id = entry.id,
                itemId = entry.itemId,
                name = "",
                quantity = entry.quantity,
                unit = entry.unit,
                addedAt = entry.addedAt,
                checkedAt = entry.checkedAt,
                position = entry.position,
                isChecked = entry.isChecked,
            )
        }
    }

    override suspend fun checkItem(id: String): Result<Unit> =
        withContext(dispatcher) {
            runCatching {
                currentListDao.updateCheckedStatus(
                    id = id,
                    isChecked = true,
                    checkedAt = System.currentTimeMillis()
                )
            }
        }

    override suspend fun uncheckItem(id: String): Result<Unit> =
        withContext(dispatcher) {
            runCatching {
                currentListDao.updateCheckedStatus(
                    id = id,
                    isChecked = false,
                    checkedAt = null
                )
            }
        }

    override suspend fun updateQuantity(id: String, quantity: Int, unit: String?): Result<Unit> =
        withContext(dispatcher) {
            runCatching {
                currentListDao.updateQuantity(id, quantity, unit)
            }
        }

    override suspend fun updatePositions(updates: List<PositionUpdate>): Result<Unit> =
        withContext(dispatcher) {
            runCatching {
                currentListDao.updatePositions(
                    updates.map { it.id to it.newPosition }
                )
            }
        }

    override suspend fun deleteItem(id: String): Result<Unit> =
        withContext(dispatcher) {
            runCatching {
                currentListDao.delete(id)
            }
        }

    override suspend fun deleteAllChecked(): Result<Unit> =
        withContext(dispatcher) {
            runCatching {
                currentListDao.deleteAllChecked()
            }
        }
}

// Extension function for mapping
private fun CurrentListWithItem.toDomain() = CurrentListItem(
    id = id,
    itemId = itemId,
    name = name,
    quantity = quantity,
    unit = unit,
    addedAt = addedAt,
    checkedAt = checkedAt,
    position = position,
    isChecked = isChecked,
)
```

## Domain Models

### GroceryItem.kt

```kotlin
package com.restock.core.domain.model

data class GroceryItem(
    val id: String,
    val name: String,
    val normalizedName: String,
    val createdAt: Long,
    val isActive: Boolean = true,
)
```

### CurrentListItem.kt

```kotlin
package com.restock.core.domain.model

data class CurrentListItem(
    val id: String,
    val itemId: String,
    val name: String,
    val quantity: Int = 1,
    val unit: String? = null,
    val addedAt: Long,
    val checkedAt: Long? = null,
    val position: Int,
    val isChecked: Boolean = false,
)

data class PositionUpdate(
    val id: String,
    val newPosition: Int,
)
```

## Acceptance Criteria

- [ ] ItemRepository interface defined in domain layer
- [ ] CurrentListRepository interface defined in domain layer
- [ ] ItemRepositoryImpl implements all methods
- [ ] CurrentListRepositoryImpl implements all methods
- [ ] All operations use IO dispatcher
- [ ] Result type used for operations that can fail
- [ ] Flow used for observable data
- [ ] Unit tests cover all repository methods

## Testing

```kotlin
@RunWith(AndroidJUnit4::class)
class ItemRepositoryImplTest {

    private lateinit var repository: ItemRepositoryImpl
    private lateinit var dao: ItemDao
    private lateinit var database: RestockDatabase

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RestockDatabase::class.java
        ).build()
        dao = database.itemDao()
        repository = ItemRepositoryImpl(dao, Dispatchers.IO)
    }

    @Test
    fun getOrCreate_createsNewItem_whenNotExists() = runTest {
        val item = repository.getOrCreate("Milk")

        assertThat(item.name).isEqualTo("Milk")
        assertThat(item.normalizedName).isEqualTo("milk")
    }

    @Test
    fun getOrCreate_returnsExisting_whenExists() = runTest {
        val first = repository.getOrCreate("Milk")
        val second = repository.getOrCreate("milk") // Different case

        assertThat(first.id).isEqualTo(second.id)
    }

    @Test
    fun searchByName_findsMatchingItems() = runTest {
        repository.getOrCreate("Milk")
        repository.getOrCreate("Almond Milk")
        repository.getOrCreate("Eggs")

        val results = repository.searchByName("mil")

        assertThat(results).hasSize(2)
    }
}
```

## Definition of Done

- [ ] All repository interfaces defined
- [ ] All repository implementations complete
- [ ] Domain models created
- [ ] Mapping functions implemented
- [ ] Unit tests pass (90%+ coverage)
- [ ] Works with offline database

## Dependent Tasks

This task blocks:
- 002_use_case_implementations.md
- All list feature development

## Notes

- Use `runCatching` for operations that can fail
- Keep mapping functions as extensions for clarity
- Normalize item names for consistent matching
- Position management is critical for drag-drop
