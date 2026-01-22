# Sprint 3-4: The List Feature
## Restock Android Application - Phase 1

**Duration:** Weeks 5-8 (4 weeks)
**Sprint Dates:** [TBD based on kickoff]
**Team:** Tech Lead/Senior Dev (1), UI/UX Engineer (1)
**Prerequisites:** Sprint 1-2 completed (project setup, database, design system)

---

## Table of Contents

1. [Sprint Overview](#1-sprint-overview)
2. [Sprint Goals](#2-sprint-goals)
3. [User Stories](#3-user-stories)
4. [Technical Tasks](#4-technical-tasks)
5. [Design Tasks](#5-design-tasks)
6. [Definition of Done](#6-definition-of-done)
7. [Daily Breakdown](#7-daily-breakdown)
8. [Technical Specifications](#8-technical-specifications)
9. [UI Specifications](#9-ui-specifications)
10. [Testing Requirements](#10-testing-requirements)
11. [Risks & Mitigation](#11-risks--mitigation)
12. [Acceptance Criteria](#12-acceptance-criteria)

---

## 1. Sprint Overview

This sprint delivers the core list functionality—the primary user-facing feature of Restock. Users will be able to add items, check them off, delete, and reorder their grocery list. This is the "day one" experience that must feel instant and delightful.

### Sprint Theme
> *"Make the simple list feel magical."*

### Why This Matters
- This is the first feature users interact with
- First impression determines retention
- "Time to first item" is our key metric (<15 seconds)
- Foundation for all subsequent features

### Key Metrics
| Metric | Target |
|--------|--------|
| Time to first item added | < 15 seconds |
| Add item interaction | < 2 taps |
| Check item interaction | 1 tap |
| List scroll performance | 60 fps |
| Offline functionality | 100% |

---

## 2. Sprint Goals

### Primary Goals

| Goal | Owner | Success Metric |
|------|-------|----------------|
| Add items via text input | Both | Item appears in list instantly |
| Auto-complete from history | Tech Lead | Suggestions appear while typing |
| Check/uncheck items | Both | Single tap toggles state |
| Swipe-to-delete with undo | Both | 5-second undo window |
| Drag-and-drop reordering | Both | Position persists after reorder |
| "In Cart" section | UI/UX | Checked items grouped |
| Quantity adjustment | Both | Long-press shows adjustment UI |

### Stretch Goals

| Goal | Owner | Condition |
|------|-------|-----------|
| Haptic feedback on interactions | UI/UX | If primary complete by day 14 |
| Keyboard shortcuts | Tech Lead | If primary complete by day 14 |
| List statistics (item count) | UI/UX | If primary complete by day 14 |

---

## 3. User Stories

### US-3.1: Add Item to List
**As a** user
**I want** to quickly add items to my grocery list
**So that** I don't forget what I need to buy

**Acceptance Criteria:**
- [ ] Text input is visible and accessible
- [ ] Pressing enter/return adds item to list
- [ ] Item appears at top of unchecked section
- [ ] Input clears after adding
- [ ] Keyboard remains open for rapid entry
- [ ] Works offline

**Technical Notes:**
- Normalize item names (trim, lowercase for matching)
- Create new Item entity if not exists
- Create CurrentListItem entry
- Update position indices

---

### US-3.2: Auto-Complete Suggestions
**As a** user
**I want** to see suggestions as I type
**So that** I can add items faster

**Acceptance Criteria:**
- [ ] Suggestions appear after 1 character typed
- [ ] Suggestions come from personal history
- [ ] Suggestions come from common items database
- [ ] Tap suggestion to add item
- [ ] Maximum 5 suggestions shown
- [ ] Suggestions update as user types

**Technical Notes:**
- Query items table by normalized_name LIKE
- Include pre-seeded common items
- Debounce input (200ms)
- Order by frequency of use

---

### US-3.3: Check Off Items
**As a** user
**I want** to check off items as I shop
**So that** I know what I've already picked up

**Acceptance Criteria:**
- [ ] Single tap on checkbox toggles state
- [ ] Checked items move to "In Cart" section
- [ ] Unchecking moves item back to main list
- [ ] Check animation is smooth and satisfying
- [ ] State persists across app restarts

**Technical Notes:**
- Update CurrentListItem.isChecked
- Update CurrentListItem.checkedAt timestamp
- Animate item position change
- This check-off will later trigger purchase recording (Sprint 5-6)

---

### US-3.4: Delete Items
**As a** user
**I want** to remove items I don't need
**So that** my list stays clean

**Acceptance Criteria:**
- [ ] Swipe left to reveal delete action
- [ ] Item slides out with animation
- [ ] Undo snackbar appears for 5 seconds
- [ ] Tapping undo restores item
- [ ] After 5 seconds, deletion is permanent

**Technical Notes:**
- Soft delete initially (during undo window)
- Hard delete after undo expires
- Use Snackbar with action
- Store deleted item temporarily

---

### US-3.5: Reorder Items
**As a** user
**I want** to arrange my list in shopping order
**So that** I can shop efficiently

**Acceptance Criteria:**
- [ ] Long-press initiates drag mode
- [ ] Item follows finger during drag
- [ ] Other items shift to make room
- [ ] Dropping saves new order
- [ ] Order persists across restarts
- [ ] Haptic feedback on grab (stretch)

**Technical Notes:**
- Use reorderable library or custom implementation
- Update position field for all affected items
- Batch update in transaction
- Only reorder within unchecked section

---

### US-3.6: Quantity Adjustment
**As a** user
**I want** to specify quantities for items
**So that** I know how much to buy

**Acceptance Criteria:**
- [ ] Long-press on item shows quantity sheet
- [ ] Can increment/decrement quantity
- [ ] Can enter custom number
- [ ] Optional unit selection (each, lb, oz, etc.)
- [ ] Quantity badge shows on item card
- [ ] Default quantity is 1 (not shown)

**Technical Notes:**
- Bottom sheet with quantity controls
- Update CurrentListItem.quantity and .unit
- Only show badge if quantity > 1

---

### US-3.7: In Cart Section
**As a** user
**I want** checked items grouped separately
**So that** I can focus on what's left to get

**Acceptance Criteria:**
- [ ] "In Cart" section appears below main list
- [ ] Section header shows count
- [ ] Checked items appear in check order
- [ ] Section is collapsible (stretch)
- [ ] Clear visual distinction from main list

**Technical Notes:**
- Use sticky header in LazyColumn
- Sort by checkedAt timestamp
- Muted styling for checked items

---

## 4. Technical Tasks

### Tech Lead Tasks

#### Week 5: Repository & Use Case Layer

| Task ID | Task | Estimate | Priority |
|---------|------|----------|----------|
| T-3.1 | Implement ItemRepository | 4h | P0 |
| T-3.2 | Implement CurrentListRepository | 4h | P0 |
| T-3.3 | Create AddItemUseCase | 3h | P0 |
| T-3.4 | Create CheckItemUseCase | 2h | P0 |
| T-3.5 | Create DeleteItemUseCase | 2h | P0 |
| T-3.6 | Create ReorderItemsUseCase | 3h | P0 |
| T-3.7 | Create UpdateQuantityUseCase | 2h | P0 |
| T-3.8 | Create GetCurrentListUseCase | 2h | P0 |
| T-3.9 | Write repository unit tests | 4h | P0 |
| T-3.10 | Write use case unit tests | 4h | P0 |

#### Week 6: ViewModel & Auto-Complete

| Task ID | Task | Estimate | Priority |
|---------|------|----------|----------|
| T-4.1 | Create ListViewModel | 4h | P0 |
| T-4.2 | Implement UiState for list screen | 2h | P0 |
| T-4.3 | Implement Actions for list screen | 2h | P0 |
| T-4.4 | Create auto-complete data source | 3h | P0 |
| T-4.5 | Implement search/filter logic | 3h | P0 |
| T-4.6 | Seed common items database | 2h | P1 |
| T-4.7 | Implement debounced search | 2h | P0 |
| T-4.8 | Write ViewModel tests | 4h | P0 |

#### Week 7: Position Management & Sync

| Task ID | Task | Estimate | Priority |
|---------|------|----------|----------|
| T-5.1 | Position calculation for new items | 2h | P0 |
| T-5.2 | Position update for reordering | 3h | P0 |
| T-5.3 | Batch position updates | 2h | P0 |
| T-5.4 | Undo mechanism implementation | 3h | P0 |
| T-5.5 | Offline-first data flow verification | 2h | P0 |
| T-5.6 | Database transaction handling | 2h | P0 |
| T-5.7 | Integration testing | 4h | P0 |

#### Week 8: Integration & Polish

| Task ID | Task | Estimate | Priority |
|---------|------|----------|----------|
| T-6.1 | Connect ViewModel to UI | 3h | P0 |
| T-6.2 | End-to-end flow testing | 4h | P0 |
| T-6.3 | Performance profiling | 3h | P0 |
| T-6.4 | Fix performance issues | 4h | P0 |
| T-6.5 | Code review all feature code | 4h | P0 |
| T-6.6 | Fix code review issues | 4h | P0 |
| T-6.7 | Update documentation | 2h | P1 |

---

## 5. Design Tasks

### UI/UX Engineer Tasks

#### Week 5: List Screen Layout

| Task ID | Task | Estimate | Priority |
|---------|------|----------|----------|
| D-3.1 | Finalize list screen wireframes | 3h | P0 |
| D-3.2 | Design add item input component | 3h | P0 |
| D-3.3 | Design auto-complete dropdown | 3h | P0 |
| D-3.4 | Design GroceryItemCard component | 4h | P0 |
| D-3.5 | Design In Cart section header | 2h | P0 |
| D-3.6 | Design empty state | 2h | P0 |
| D-3.7 | Create Figma prototypes | 3h | P0 |

#### Week 6: Component Implementation

| Task ID | Task | Estimate | Priority |
|---------|------|----------|----------|
| D-4.1 | Implement AddItemTextField | 4h | P0 |
| D-4.2 | Implement AutoCompleteDropdown | 4h | P0 |
| D-4.3 | Implement GroceryItemCard | 5h | P0 |
| D-4.4 | Implement check animation | 3h | P0 |
| D-4.5 | Implement swipe-to-delete | 4h | P0 |

#### Week 7: Interactions & Animations

| Task ID | Task | Estimate | Priority |
|---------|------|----------|----------|
| D-5.1 | Implement drag-and-drop | 6h | P0 |
| D-5.2 | Design quantity adjustment sheet | 2h | P0 |
| D-5.3 | Implement QuantityBottomSheet | 4h | P0 |
| D-5.4 | Implement In Cart section | 3h | P0 |
| D-5.5 | Implement list empty state | 2h | P0 |
| D-5.6 | Add haptic feedback | 2h | P1 |

#### Week 8: Polish & Accessibility

| Task ID | Task | Estimate | Priority |
|---------|------|----------|----------|
| D-6.1 | Assemble full ListScreen | 4h | P0 |
| D-6.2 | Animation timing polish | 3h | P0 |
| D-6.3 | Accessibility audit | 3h | P0 |
| D-6.4 | Fix accessibility issues | 3h | P0 |
| D-6.5 | Responsive layout testing | 2h | P0 |
| D-6.6 | Dark mode support (stretch) | 3h | P2 |
| D-6.7 | Final visual polish | 3h | P0 |

---

## 6. Definition of Done

### Sprint-Level DoD

- [ ] All user stories accepted
- [ ] All P0 tasks completed
- [ ] Unit test coverage > 80% for use cases
- [ ] UI tests for critical paths
- [ ] Performance: 60fps scroll
- [ ] Accessibility: TalkBack navigable
- [ ] Offline: All features work offline
- [ ] Code reviewed and merged

### User Story DoD

- [ ] Acceptance criteria met
- [ ] Unit tests passing
- [ ] No regressions in existing tests
- [ ] Code reviewed
- [ ] Documented (if API changes)
- [ ] Accessibility verified

---

## 7. Daily Breakdown

### Week 5

| Day | Tech Lead | UI/UX Engineer |
|-----|-----------|----------------|
| Mon | T-3.1: ItemRepository impl | D-3.1: List screen wireframes |
| Tue | T-3.2: CurrentListRepository | D-3.2, D-3.3: Input, dropdown design |
| Wed | T-3.3, T-3.4: Add/Check use cases | D-3.4: GroceryItemCard design |
| Thu | T-3.5, T-3.6: Delete/Reorder use cases | D-3.5, D-3.6: In Cart, empty state |
| Fri | T-3.7, T-3.8, T-3.9: Quantity, tests | D-3.7: Figma prototypes |

### Week 6

| Day | Tech Lead | UI/UX Engineer |
|-----|-----------|----------------|
| Mon | T-4.1: ListViewModel | D-4.1: AddItemTextField impl |
| Tue | T-4.2, T-4.3: UiState, Actions | D-4.2: AutoCompleteDropdown impl |
| Wed | T-4.4, T-4.5: Auto-complete logic | D-4.3: GroceryItemCard impl (start) |
| Thu | T-4.6, T-4.7: Seed data, debounce | D-4.3: GroceryItemCard impl (complete) |
| Fri | T-4.8: ViewModel tests | D-4.4, D-4.5: Check animation, swipe |

### Week 7

| Day | Tech Lead | UI/UX Engineer |
|-----|-----------|----------------|
| Mon | T-5.1, T-5.2: Position logic | D-5.1: Drag-and-drop (start) |
| Tue | T-5.3: Batch updates | D-5.1: Drag-and-drop (complete) |
| Wed | T-5.4: Undo mechanism | D-5.2, D-5.3: Quantity sheet |
| Thu | T-5.5, T-5.6: Offline, transactions | D-5.4, D-5.5: In Cart, empty state |
| Fri | T-5.7: Integration testing | D-5.6: Haptic feedback |

### Week 8

| Day | Tech Lead | UI/UX Engineer |
|-----|-----------|----------------|
| Mon | T-6.1: Connect ViewModel to UI | D-6.1: Assemble ListScreen |
| Tue | T-6.2: E2E testing | D-6.2: Animation polish |
| Wed | T-6.3, T-6.4: Performance | D-6.3, D-6.4: Accessibility |
| Thu | T-6.5: Code review | D-6.5: Responsive testing |
| Fri | T-6.6, T-6.7: Fixes, docs | D-6.7: Final polish |

---

## 8. Technical Specifications

### Repository Implementations

```kotlin
// ItemRepositoryImpl.kt
class ItemRepositoryImpl @Inject constructor(
    private val itemDao: ItemDao,
    @IoDispatcher private val dispatcher: CoroutineDispatcher,
) : ItemRepository {

    override fun observeActiveItems(): Flow<List<GroceryItem>> =
        itemDao.observeActiveItems()
            .map { entities -> entities.map { it.toDomain() } }
            .flowOn(dispatcher)

    override suspend fun findByNormalizedName(name: String): GroceryItem? =
        withContext(dispatcher) {
            itemDao.findByNormalizedName(name)?.toDomain()
        }

    override suspend fun insert(item: GroceryItem): Result<GroceryItem> =
        withContext(dispatcher) {
            runCatching {
                itemDao.insert(item.toEntity())
                item
            }
        }

    override suspend fun getOrCreate(name: String): GroceryItem =
        withContext(dispatcher) {
            val normalized = name.normalizeItemName()
            itemDao.findByNormalizedName(normalized)?.toDomain()
                ?: GroceryItem(
                    id = UUID.randomUUID().toString(),
                    name = name.trim(),
                    normalizedName = normalized,
                    createdAt = Clock.System.now(),
                ).also { itemDao.insert(it.toEntity()) }
        }
}

// CurrentListRepositoryImpl.kt
class CurrentListRepositoryImpl @Inject constructor(
    private val currentListDao: CurrentListDao,
    @IoDispatcher private val dispatcher: CoroutineDispatcher,
) : CurrentListRepository {

    override fun observeCurrentList(): Flow<List<CurrentListItem>> =
        currentListDao.observeCurrentListWithItems()
            .map { entities -> entities.map { it.toDomain() } }
            .flowOn(dispatcher)

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
                addedAt = Clock.System.now().toEpochMilliseconds(),
                checkedAt = null,
                position = maxPosition + 1,
                isChecked = false,
            )
            currentListDao.insert(entry)
            entry.toDomain()
        }
    }

    override suspend fun checkItem(id: String): Result<Unit> =
        withContext(dispatcher) {
            runCatching {
                currentListDao.updateCheckedStatus(
                    id = id,
                    isChecked = true,
                    checkedAt = Clock.System.now().toEpochMilliseconds()
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

    override suspend fun deleteItem(id: String): Result<Unit> =
        withContext(dispatcher) {
            runCatching {
                currentListDao.delete(id)
            }
        }

    override suspend fun updatePositions(
        updates: List<PositionUpdate>
    ): Result<Unit> = withContext(dispatcher) {
        runCatching {
            currentListDao.updatePositions(
                updates.map { it.id to it.newPosition }
            )
        }
    }
}
```

### Use Case Implementations

```kotlin
// AddItemUseCase.kt
class AddItemUseCase @Inject constructor(
    private val itemRepository: ItemRepository,
    private val currentListRepository: CurrentListRepository,
) {
    suspend operator fun invoke(
        name: String,
        quantity: Int = 1,
        unit: String? = null
    ): Result<CurrentListItem> {
        if (name.isBlank()) {
            return Result.failure(IllegalArgumentException("Item name cannot be empty"))
        }

        val item = itemRepository.getOrCreate(name)

        // Check if already on list
        val existingOnList = currentListRepository.findByItemId(item.id)
        if (existingOnList != null) {
            // Update quantity instead of adding duplicate
            return currentListRepository.updateQuantity(
                id = existingOnList.id,
                quantity = existingOnList.quantity + quantity
            ).map { existingOnList.copy(quantity = existingOnList.quantity + quantity) }
        }

        return currentListRepository.addItem(
            itemId = item.id,
            quantity = quantity,
            unit = unit
        )
    }
}

// CheckItemUseCase.kt
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

// DeleteItemUseCase.kt
class DeleteItemUseCase @Inject constructor(
    private val currentListRepository: CurrentListRepository,
) {
    suspend operator fun invoke(id: String): Result<DeletedItem> {
        val item = currentListRepository.getById(id)
            ?: return Result.failure(IllegalArgumentException("Item not found"))

        return currentListRepository.deleteItem(id).map {
            DeletedItem(
                listItemId = id,
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
            quantity = deletedItem.quantity,
            unit = deletedItem.unit,
            position = deletedItem.position,
        ).map { }
    }
}

data class DeletedItem(
    val listItemId: String,
    val itemId: String,
    val name: String,
    val quantity: Int,
    val unit: String?,
    val position: Int,
)
```

### ViewModel Implementation

```kotlin
@HiltViewModel
class ListViewModel @Inject constructor(
    private val getCurrentListUseCase: GetCurrentListUseCase,
    private val addItemUseCase: AddItemUseCase,
    private val checkItemUseCase: CheckItemUseCase,
    private val deleteItemUseCase: DeleteItemUseCase,
    private val reorderItemsUseCase: ReorderItemsUseCase,
    private val updateQuantityUseCase: UpdateQuantityUseCase,
    private val searchItemsUseCase: SearchItemsUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ListUiState())
    val uiState: StateFlow<ListUiState> = _uiState.asStateFlow()

    private val _events = Channel<ListEvent>(Channel.BUFFERED)
    val events: Flow<ListEvent> = _events.receiveAsFlow()

    private var deletedItem: DeletedItem? = null
    private var undoJob: Job? = null

    init {
        observeCurrentList()
    }

    private fun observeCurrentList() {
        viewModelScope.launch {
            getCurrentListUseCase()
                .catch { e ->
                    _uiState.update { it.copy(error = e.message) }
                }
                .collect { items ->
                    _uiState.update { state ->
                        state.copy(
                            uncheckedItems = items.filter { !it.isChecked }
                                .sortedBy { it.position },
                            checkedItems = items.filter { it.isChecked }
                                .sortedByDescending { it.checkedAt },
                            isLoading = false,
                        )
                    }
                }
        }
    }

    fun onAction(action: ListAction) {
        when (action) {
            is ListAction.AddItem -> addItem(action.name)
            is ListAction.ToggleItem -> toggleItem(action.id, action.isChecked)
            is ListAction.DeleteItem -> deleteItem(action.id)
            is ListAction.UndoDelete -> undoDelete()
            is ListAction.ReorderItems -> reorderItems(action.fromIndex, action.toIndex)
            is ListAction.UpdateQuantity -> updateQuantity(action.id, action.quantity, action.unit)
            is ListAction.SearchItems -> searchItems(action.query)
            is ListAction.ClearSearch -> clearSearch()
        }
    }

    private fun addItem(name: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(inputText = "") }
            addItemUseCase(name)
                .onSuccess {
                    _events.send(ListEvent.ItemAdded(it.name))
                }
                .onFailure { e ->
                    _events.send(ListEvent.Error(e.message ?: "Failed to add item"))
                }
        }
    }

    private fun toggleItem(id: String, isChecked: Boolean) {
        viewModelScope.launch {
            checkItemUseCase(id, isChecked)
                .onFailure { e ->
                    _events.send(ListEvent.Error(e.message ?: "Failed to update item"))
                }
        }
    }

    private fun deleteItem(id: String) {
        viewModelScope.launch {
            deleteItemUseCase(id)
                .onSuccess { deleted ->
                    deletedItem = deleted
                    undoJob?.cancel()
                    undoJob = launch {
                        delay(5000)
                        deletedItem = null
                    }
                    _events.send(ListEvent.ItemDeleted(deleted.name))
                }
                .onFailure { e ->
                    _events.send(ListEvent.Error(e.message ?: "Failed to delete item"))
                }
        }
    }

    private fun undoDelete() {
        viewModelScope.launch {
            undoJob?.cancel()
            deletedItem?.let { item ->
                deleteItemUseCase.undoDelete(item)
                    .onSuccess {
                        _events.send(ListEvent.UndoSuccessful)
                    }
                deletedItem = null
            }
        }
    }

    private fun reorderItems(fromIndex: Int, toIndex: Int) {
        viewModelScope.launch {
            reorderItemsUseCase(fromIndex, toIndex)
        }
    }

    private fun updateQuantity(id: String, quantity: Int, unit: String?) {
        viewModelScope.launch {
            updateQuantityUseCase(id, quantity, unit)
        }
    }

    private fun searchItems(query: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(searchQuery = query) }
            if (query.length >= 1) {
                searchItemsUseCase(query).collect { suggestions ->
                    _uiState.update { it.copy(autoCompleteSuggestions = suggestions) }
                }
            } else {
                _uiState.update { it.copy(autoCompleteSuggestions = emptyList()) }
            }
        }
    }

    private fun clearSearch() {
        _uiState.update {
            it.copy(searchQuery = "", autoCompleteSuggestions = emptyList())
        }
    }
}

data class ListUiState(
    val uncheckedItems: List<CurrentListItem> = emptyList(),
    val checkedItems: List<CurrentListItem> = emptyList(),
    val autoCompleteSuggestions: List<GroceryItem> = emptyList(),
    val searchQuery: String = "",
    val inputText: String = "",
    val isLoading: Boolean = true,
    val error: String? = null,
)

sealed interface ListAction {
    data class AddItem(val name: String) : ListAction
    data class ToggleItem(val id: String, val isChecked: Boolean) : ListAction
    data class DeleteItem(val id: String) : ListAction
    data object UndoDelete : ListAction
    data class ReorderItems(val fromIndex: Int, val toIndex: Int) : ListAction
    data class UpdateQuantity(val id: String, val quantity: Int, val unit: String?) : ListAction
    data class SearchItems(val query: String) : ListAction
    data object ClearSearch : ListAction
}

sealed interface ListEvent {
    data class ItemAdded(val name: String) : ListEvent
    data class ItemDeleted(val name: String) : ListEvent
    data object UndoSuccessful : ListEvent
    data class Error(val message: String) : ListEvent
}
```

---

## 9. UI Specifications

### List Screen Layout

```
┌─────────────────────────────────────────┐
│  ┌─────────────────────────────────┐    │
│  │ Add an item...            [mic] │    │  <- AddItemTextField
│  └─────────────────────────────────┘    │
│  ┌─────────────────────────────────┐    │
│  │ Milk                            │    │  <- Auto-complete dropdown
│  │ Milk chocolate                  │    │     (when typing)
│  │ Milk alternatives               │    │
│  └─────────────────────────────────┘    │
│                                         │
│  ┌─────────────────────────────────┐    │
│  │ ☐  Eggs                    ×2   │    │  <- GroceryItemCard
│  └─────────────────────────────────┘    │     (swipeable)
│  ┌─────────────────────────────────┐    │
│  │ ☐  Bread                        │    │
│  └─────────────────────────────────┘    │
│  ┌─────────────────────────────────┐    │
│  │ ☐  Butter                       │    │
│  └─────────────────────────────────┘    │
│                                         │
│  ─────────── In Cart (2) ───────────    │  <- Section header
│                                         │
│  ┌─────────────────────────────────┐    │
│  │ ☑  Milk                         │    │  <- Checked item (muted)
│  └─────────────────────────────────┘    │
│  ┌─────────────────────────────────┐    │
│  │ ☑  Cheese                       │    │
│  └─────────────────────────────────┘    │
│                                         │
└─────────────────────────────────────────┘
```

### Component Specifications

#### AddItemTextField
```kotlin
@Composable
fun AddItemTextField(
    value: String,
    onValueChange: (String) -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = RestockColors.Card,
        shadowElevation = 2.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f),
                textStyle = RestockTypography.bodyLarge,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done,
                    capitalization = KeyboardCapitalization.Sentences,
                ),
                keyboardActions = KeyboardActions(onDone = { onSubmit() }),
                decorationBox = { innerTextField ->
                    if (value.isEmpty()) {
                        Text(
                            text = "Add an item...",
                            style = RestockTypography.bodyLarge,
                            color = RestockColors.Muted,
                        )
                    }
                    innerTextField()
                }
            )
            // Optional: Voice input button (stretch)
        }
    }
}
```

#### GroceryItemCard
```kotlin
@Composable
fun GroceryItemCard(
    item: CurrentListItem,
    onCheckedChange: (Boolean) -> Unit,
    onDelete: () -> Unit,
    onQuantityClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onDelete()
                true
            } else false
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            DeleteBackground(dismissState)
        },
        modifier = modifier,
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            color = RestockColors.Card,
            shadowElevation = 1.dp,
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Checkbox
                RestockCheckbox(
                    checked = item.isChecked,
                    onCheckedChange = onCheckedChange,
                    modifier = Modifier.size(24.dp),
                )

                Spacer(Modifier.width(12.dp))

                // Item name
                Text(
                    text = item.name,
                    style = RestockTypography.bodyLarge,
                    color = if (item.isChecked) RestockColors.Muted else RestockColors.Primary,
                    textDecoration = if (item.isChecked) TextDecoration.LineThrough else null,
                    modifier = Modifier.weight(1f),
                )

                // Quantity badge (if > 1)
                if (item.quantity > 1) {
                    Spacer(Modifier.width(8.dp))
                    QuantityBadge(
                        quantity = item.quantity,
                        unit = item.unit,
                        onClick = onQuantityClick,
                    )
                }
            }
        }
    }
}
```

### Animation Specifications

| Animation | Duration | Easing | Trigger |
|-----------|----------|--------|---------|
| Check toggle | 200ms | EaseInOut | Checkbox tap |
| Item add | 300ms | EaseOut | New item inserted |
| Item delete | 250ms | EaseIn | Swipe threshold reached |
| Reorder | 150ms | EaseInOut | During drag |
| Auto-complete show | 150ms | EaseOut | Search results available |

---

## 10. Testing Requirements

### Unit Tests

| Component | Test Cases | Coverage |
|-----------|------------|----------|
| AddItemUseCase | Valid name, empty name, duplicate | 100% |
| CheckItemUseCase | Check, uncheck, invalid id | 100% |
| DeleteItemUseCase | Delete, undo, timeout | 100% |
| ReorderItemsUseCase | Reorder, bounds check | 100% |
| ListViewModel | All actions, state updates | 90% |
| ItemRepository | CRUD operations | 90% |
| CurrentListRepository | CRUD, position updates | 90% |

### UI Tests

```kotlin
@Test
fun addItem_showsInList() {
    composeTestRule.setContent {
        ListScreen(viewModel = testViewModel)
    }

    composeTestRule
        .onNodeWithText("Add an item...")
        .performTextInput("Milk")

    composeTestRule
        .onNodeWithImeAction(ImeAction.Done)
        .performImeAction()

    composeTestRule
        .onNodeWithText("Milk")
        .assertIsDisplayed()
}

@Test
fun checkItem_movesToInCartSection() {
    // Setup: Add "Milk" to list
    composeTestRule.setContent {
        ListScreen(viewModel = testViewModel)
    }

    composeTestRule
        .onNodeWithText("Milk")
        .onSibling(hasTestTag("checkbox"))
        .performClick()

    composeTestRule
        .onNodeWithText("In Cart")
        .assertIsDisplayed()

    composeTestRule
        .onNodeWithText("Milk")
        .assertExists() // Should be under In Cart
}

@Test
fun swipeToDelete_showsUndo() {
    composeTestRule.setContent {
        ListScreen(viewModel = testViewModel)
    }

    composeTestRule
        .onNodeWithText("Milk")
        .performTouchInput { swipeLeft() }

    composeTestRule
        .onNodeWithText("Undo")
        .assertIsDisplayed()
}
```

### Performance Tests

| Scenario | Target |
|----------|--------|
| List with 100 items scroll | 60fps, no jank |
| Add item response time | < 100ms |
| Check item response time | < 50ms |
| Cold start to input ready | < 2 seconds |

---

## 11. Risks & Mitigation

| Risk | Probability | Impact | Mitigation |
|------|-------------|--------|------------|
| Drag-drop complexity | Medium | High | Use established library (reorderable) |
| Swipe gesture conflicts | Medium | Medium | Careful gesture detection tuning |
| Auto-complete performance | Low | Medium | Debounce and limit results |
| Position sync issues | Medium | Medium | Transactional updates |
| Undo race conditions | Low | Medium | Cancel previous undo on new delete |

### Technical Debt Awareness

- Auto-complete currently doesn't consider frequency (add in Sprint 7-8)
- Purchase recording not implemented (deferred to Sprint 5-6)
- No analytics events yet (deferred to Sprint 11-12)

---

## 12. Acceptance Criteria

### Sprint Demo Checklist

1. [ ] Add "Milk" to empty list - appears instantly
2. [ ] Add "Eggs" with quantity 2 - badge shows "×2"
3. [ ] Type "mil" - auto-complete shows "Milk"
4. [ ] Tap suggestion - adds to list
5. [ ] Check "Milk" - animates to In Cart section
6. [ ] Swipe "Eggs" - deletes with undo snackbar
7. [ ] Tap Undo - "Eggs" restored
8. [ ] Long-press "Eggs" - quantity sheet appears
9. [ ] Drag "Eggs" above "Milk" - reorders and persists
10. [ ] Force close app, reopen - list intact
11. [ ] Enable airplane mode - all features work

### Performance Acceptance

- [ ] Scroll 100-item list at 60fps
- [ ] Time to first item < 15 seconds
- [ ] No visible jank during animations
- [ ] Memory usage < 100MB with 100 items

### Accessibility Acceptance

- [ ] TalkBack can navigate entire list
- [ ] All items have content descriptions
- [ ] Swipe actions have accessible alternatives
- [ ] Touch targets minimum 48dp

---

## Handoff to Sprint 5-6

### Completed Artifacts
- Fully functional list screen
- Add, check, delete, reorder working
- Auto-complete operational
- Quantity adjustment available
- In Cart section implemented

### Ready for Sprint 5-6
- Check item action ready to trigger purchase recording
- Item data structure supports pattern analysis
- Repository layer ready for purchase data

### Known Issues to Address
- (Track any bugs found)
- (Note performance items to revisit)

---

*Sprint 3-4 Document v1.0 | Phase 1 - Restock Android*
