# Task: Implement List Feature Use Cases

**Sprint:** 3-4 (The List Feature)
**Owner:** Tech Lead / Senior Developer
**Estimate:** 12 hours (T-3.3 through T-3.8 combined)
**Priority:** P0
**Prerequisites:** 001_repository_implementations.md

---

## Sprint Context

Use cases encapsulate business logic for list operations. Each use case is single-responsibility and injectable, following Clean Architecture principles.

### Business Rules
- Duplicate items increment quantity instead of adding new
- Empty item names are rejected
- Checked items record timestamp
- Position updates maintain ordering integrity
- Undo requires storing deleted item state temporarily

---

## Task Description

Implement all use cases for list operations: Add, Check, Delete, Reorder, UpdateQuantity, and GetCurrentList.

## Use Case Implementations

### AddItemUseCase.kt

```kotlin
package com.restock.core.domain.usecase

import com.restock.core.domain.model.CurrentListItem
import com.restock.core.domain.repository.CurrentListRepository
import com.restock.core.domain.repository.ItemRepository
import javax.inject.Inject

class AddItemUseCase @Inject constructor(
    private val itemRepository: ItemRepository,
    private val currentListRepository: CurrentListRepository,
) {
    suspend operator fun invoke(
        name: String,
        quantity: Int = 1,
        unit: String? = null
    ): Result<CurrentListItem> {
        // Validate input
        if (name.isBlank()) {
            return Result.failure(IllegalArgumentException("Item name cannot be empty"))
        }

        // Get or create the item
        val item = itemRepository.getOrCreate(name)

        // Check if already on list
        val existingOnList = currentListRepository.findByItemId(item.id)
        if (existingOnList != null) {
            // Update quantity instead of adding duplicate
            val newQuantity = existingOnList.quantity + quantity
            return currentListRepository.updateQuantity(
                id = existingOnList.id,
                quantity = newQuantity,
                unit = unit ?: existingOnList.unit
            ).map {
                existingOnList.copy(
                    quantity = newQuantity,
                    unit = unit ?: existingOnList.unit
                )
            }
        }

        // Add to current list
        return currentListRepository.addItem(
            itemId = item.id,
            quantity = quantity,
            unit = unit
        ).map { listItem ->
            listItem.copy(name = item.name)
        }
    }
}
```

### CheckItemUseCase.kt

```kotlin
package com.restock.core.domain.usecase

import com.restock.core.domain.repository.CurrentListRepository
import javax.inject.Inject

class CheckItemUseCase @Inject constructor(
    private val currentListRepository: CurrentListRepository,
) {
    suspend operator fun invoke(id: String, isChecked: Boolean): Result<Unit> {
        return if (isChecked) {
            currentListRepository.checkItem(id)
        } else {
            currentListRepository.uncheckItem(id)
        }
    }
}
```

### DeleteItemUseCase.kt

```kotlin
package com.restock.core.domain.usecase

import com.restock.core.domain.model.CurrentListItem
import com.restock.core.domain.model.DeletedItem
import com.restock.core.domain.repository.CurrentListRepository
import javax.inject.Inject

data class DeletedItem(
    val listItemId: String,
    val itemId: String,
    val name: String,
    val quantity: Int,
    val unit: String?,
    val position: Int,
)

class DeleteItemUseCase @Inject constructor(
    private val currentListRepository: CurrentListRepository,
) {
    suspend operator fun invoke(id: String): Result<DeletedItem> {
        val item = currentListRepository.getById(id)
            ?: return Result.failure(IllegalArgumentException("Item not found"))

        return currentListRepository.deleteItem(id).map {
            DeletedItem(
                listItemId = item.id,
                itemId = item.itemId,
                name = item.name,
                quantity = item.quantity,
                unit = item.unit,
                position = item.position,
            )
        }
    }

    suspend fun undoDelete(deletedItem: DeletedItem): Result<Unit> {
        return currentListRepository.addItemAt(
            itemId = deletedItem.itemId,
            position = deletedItem.position,
            quantity = deletedItem.quantity,
            unit = deletedItem.unit,
        ).map { }
    }
}
```

### ReorderItemsUseCase.kt

```kotlin
package com.restock.core.domain.usecase

import com.restock.core.domain.model.CurrentListItem
import com.restock.core.domain.model.PositionUpdate
import com.restock.core.domain.repository.CurrentListRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class ReorderItemsUseCase @Inject constructor(
    private val currentListRepository: CurrentListRepository,
) {
    suspend operator fun invoke(fromIndex: Int, toIndex: Int): Result<Unit> {
        if (fromIndex == toIndex) return Result.success(Unit)

        // Get current unchecked items
        val currentList = currentListRepository.observeCurrentList()
            .first()
            .filter { !it.isChecked }
            .sortedBy { it.position }

        if (fromIndex < 0 || fromIndex >= currentList.size ||
            toIndex < 0 || toIndex >= currentList.size) {
            return Result.failure(IndexOutOfBoundsException("Invalid reorder indices"))
        }

        // Calculate new positions
        val updates = mutableListOf<PositionUpdate>()
        val movingItem = currentList[fromIndex]

        if (fromIndex < toIndex) {
            // Moving down: shift items up
            for (i in fromIndex + 1..toIndex) {
                updates.add(PositionUpdate(currentList[i].id, i - 1))
            }
        } else {
            // Moving up: shift items down
            for (i in toIndex until fromIndex) {
                updates.add(PositionUpdate(currentList[i].id, i + 1))
            }
        }

        // Update moving item to target position
        updates.add(PositionUpdate(movingItem.id, toIndex))

        return currentListRepository.updatePositions(updates)
    }
}
```

### UpdateQuantityUseCase.kt

```kotlin
package com.restock.core.domain.usecase

import com.restock.core.domain.repository.CurrentListRepository
import javax.inject.Inject

class UpdateQuantityUseCase @Inject constructor(
    private val currentListRepository: CurrentListRepository,
) {
    suspend operator fun invoke(
        id: String,
        quantity: Int,
        unit: String? = null
    ): Result<Unit> {
        if (quantity < 1) {
            return Result.failure(IllegalArgumentException("Quantity must be at least 1"))
        }

        return currentListRepository.updateQuantity(id, quantity, unit)
    }
}
```

### GetCurrentListUseCase.kt

```kotlin
package com.restock.core.domain.usecase

import com.restock.core.domain.model.CurrentListItem
import com.restock.core.domain.repository.CurrentListRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCurrentListUseCase @Inject constructor(
    private val currentListRepository: CurrentListRepository,
) {
    operator fun invoke(): Flow<List<CurrentListItem>> =
        currentListRepository.observeCurrentList()
}
```

### SearchItemsUseCase.kt

```kotlin
package com.restock.core.domain.usecase

import com.restock.core.domain.model.GroceryItem
import com.restock.core.domain.repository.ItemRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import javax.inject.Inject

class SearchItemsUseCase @Inject constructor(
    private val itemRepository: ItemRepository,
) {
    @OptIn(FlowPreview::class)
    operator fun invoke(query: String): Flow<List<GroceryItem>> = flow {
        if (query.length < 1) {
            emit(emptyList())
            return@flow
        }

        val results = itemRepository.searchByName(query, limit = 5)
        emit(results)
    }

    // For debounced search from ViewModel
    fun searchWithDebounce(queryFlow: Flow<String>, debounceMs: Long = 200): Flow<List<GroceryItem>> =
        queryFlow
            .debounce(debounceMs)
            .distinctUntilChanged()
            .flatMapLatest { query ->
                invoke(query)
            }
}
```

## Common Items Seeding

### SeedCommonItemsUseCase.kt

```kotlin
package com.restock.core.domain.usecase

import com.restock.core.domain.model.GroceryItem
import com.restock.core.domain.repository.ItemRepository
import javax.inject.Inject

class SeedCommonItemsUseCase @Inject constructor(
    private val itemRepository: ItemRepository,
) {
    suspend operator fun invoke() {
        COMMON_ITEMS.forEach { name ->
            itemRepository.getOrCreate(name)
        }
    }

    companion object {
        private val COMMON_ITEMS = listOf(
            "Milk",
            "Eggs",
            "Bread",
            "Butter",
            "Cheese",
            "Chicken",
            "Beef",
            "Rice",
            "Pasta",
            "Tomatoes",
            "Onions",
            "Potatoes",
            "Carrots",
            "Apples",
            "Bananas",
            "Oranges",
            "Yogurt",
            "Cereal",
            "Coffee",
            "Tea",
            "Sugar",
            "Salt",
            "Pepper",
            "Olive oil",
            "Garlic",
            "Lettuce",
            "Cucumber",
            "Bell peppers",
            "Mushrooms",
            "Spinach",
        )
    }
}
```

## Acceptance Criteria

- [ ] AddItemUseCase handles new items and duplicates
- [ ] CheckItemUseCase toggles checked state
- [ ] DeleteItemUseCase returns deletable item for undo
- [ ] ReorderItemsUseCase maintains position integrity
- [ ] UpdateQuantityUseCase validates quantity
- [ ] GetCurrentListUseCase returns Flow of items
- [ ] SearchItemsUseCase supports debounced queries
- [ ] All use cases have unit tests

## Testing

```kotlin
class AddItemUseCaseTest {

    private lateinit var useCase: AddItemUseCase
    private val itemRepository = mockk<ItemRepository>()
    private val currentListRepository = mockk<CurrentListRepository>()

    @Before
    fun setup() {
        useCase = AddItemUseCase(itemRepository, currentListRepository)
    }

    @Test
    fun `invoke with blank name returns failure`() = runTest {
        val result = useCase("")

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull())
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `invoke creates new item when not on list`() = runTest {
        val item = GroceryItem("1", "Milk", "milk", 1000L, true)
        coEvery { itemRepository.getOrCreate("Milk") } returns item
        coEvery { currentListRepository.findByItemId("1") } returns null
        coEvery { currentListRepository.addItem("1", 1, null) } returns
            Result.success(mockCurrentListItem())

        val result = useCase("Milk")

        assertThat(result.isSuccess).isTrue()
        coVerify { currentListRepository.addItem("1", 1, null) }
    }

    @Test
    fun `invoke updates quantity when item already on list`() = runTest {
        val item = GroceryItem("1", "Milk", "milk", 1000L, true)
        val existingListItem = mockCurrentListItem(quantity = 2)

        coEvery { itemRepository.getOrCreate("Milk") } returns item
        coEvery { currentListRepository.findByItemId("1") } returns existingListItem
        coEvery { currentListRepository.updateQuantity(any(), 3, null) } returns Result.success(Unit)

        val result = useCase("Milk")

        assertThat(result.isSuccess).isTrue()
        coVerify { currentListRepository.updateQuantity(any(), 3, null) }
    }
}
```

## Definition of Done

- [ ] All use cases implemented
- [ ] Business logic correctly encapsulated
- [ ] Unit tests for all use cases (90%+ coverage)
- [ ] Edge cases handled (empty input, duplicates, bounds)
- [ ] Use cases injectable via Hilt

## Dependent Tasks

This task blocks:
- 003_list_viewmodel.md
- Feature UI development

## Notes

- Use cases should be stateless
- Inject repositories, not DAOs
- Return Result<T> for operations that can fail
- Keep use cases focused on single responsibility
