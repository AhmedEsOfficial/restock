# Task: Implement ListViewModel

**Sprint:** 3-4 (The List Feature)
**Owner:** Tech Lead / Senior Developer
**Estimate:** 8 hours (T-4.1 through T-4.3, T-4.8 combined)
**Priority:** P0
**Prerequisites:** 002_use_case_implementations.md, BaseViewModel pattern

---

## Sprint Context

The ListViewModel is the central state manager for the main list screen. It follows the UDF pattern established in Sprint 1-2 and coordinates all list operations.

---

## Task Description

Implement ListViewModel with state management for the grocery list, including actions for adding, checking, deleting, reordering, and updating items.

## Implementation

### ListUiState.kt

```kotlin
package com.restock.feature.list.ui

import com.restock.core.domain.model.CurrentListItem
import com.restock.core.domain.model.GroceryItem
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class ListUiState(
    val uncheckedItems: ImmutableList<CurrentListItem> = persistentListOf(),
    val checkedItems: ImmutableList<CurrentListItem> = persistentListOf(),
    val autoCompleteSuggestions: ImmutableList<GroceryItem> = persistentListOf(),
    val searchQuery: String = "",
    val inputText: String = "",
    val isLoading: Boolean = true,
    val error: String? = null,
    val selectedItemForQuantity: CurrentListItem? = null,
) {
    val isEmpty: Boolean
        get() = uncheckedItems.isEmpty() && checkedItems.isEmpty()

    val totalItemCount: Int
        get() = uncheckedItems.size + checkedItems.size

    val uncheckedCount: Int
        get() = uncheckedItems.size

    val checkedCount: Int
        get() = checkedItems.size
}
```

### ListAction.kt

```kotlin
package com.restock.feature.list.ui

sealed interface ListAction {
    // Input
    data class UpdateInputText(val text: String) : ListAction
    data class AddItem(val name: String) : ListAction
    data object ClearInput : ListAction

    // Item actions
    data class ToggleItem(val id: String, val isChecked: Boolean) : ListAction
    data class DeleteItem(val id: String) : ListAction
    data object UndoDelete : ListAction

    // Reordering
    data class ReorderItems(val fromIndex: Int, val toIndex: Int) : ListAction

    // Quantity
    data class SelectItemForQuantity(val item: CurrentListItem?) : ListAction
    data class UpdateQuantity(val id: String, val quantity: Int, val unit: String?) : ListAction

    // Search
    data class SearchItems(val query: String) : ListAction
    data object ClearSearch : ListAction
    data class SelectAutoComplete(val item: GroceryItem) : ListAction

    // Bulk
    data object ClearCheckedItems : ListAction
}
```

### ListEvent.kt

```kotlin
package com.restock.feature.list.ui

sealed interface ListEvent {
    data class ItemAdded(val name: String) : ListEvent
    data class ItemDeleted(val name: String, val canUndo: Boolean = true) : ListEvent
    data object UndoSuccessful : ListEvent
    data object UndoExpired : ListEvent
    data class Error(val message: String) : ListEvent
    data object CheckedItemsCleared : ListEvent
}
```

### ListViewModel.kt

```kotlin
package com.restock.feature.list.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.restock.core.domain.model.CurrentListItem
import com.restock.core.domain.model.GroceryItem
import com.restock.core.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ListViewModel @Inject constructor(
    private val getCurrentListUseCase: GetCurrentListUseCase,
    private val addItemUseCase: AddItemUseCase,
    private val checkItemUseCase: CheckItemUseCase,
    private val deleteItemUseCase: DeleteItemUseCase,
    private val reorderItemsUseCase: ReorderItemsUseCase,
    private val updateQuantityUseCase: UpdateQuantityUseCase,
    private val searchItemsUseCase: SearchItemsUseCase,
    private val clearCheckedItemsUseCase: ClearCheckedItemsUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ListUiState())
    val uiState: StateFlow<ListUiState> = _uiState.asStateFlow()

    private val _events = Channel<ListEvent>(Channel.BUFFERED)
    val events: Flow<ListEvent> = _events.receiveAsFlow()

    private var deletedItem: DeletedItem? = null
    private var undoJob: Job? = null

    private val searchQueryFlow = MutableSharedFlow<String>(replay = 1)

    init {
        observeCurrentList()
        observeSearchResults()
    }

    private fun observeCurrentList() {
        viewModelScope.launch {
            getCurrentListUseCase()
                .catch { e ->
                    _uiState.update { it.copy(error = e.message, isLoading = false) }
                }
                .collect { items ->
                    _uiState.update { state ->
                        state.copy(
                            uncheckedItems = items
                                .filter { !it.isChecked }
                                .sortedBy { it.position }
                                .toImmutableList(),
                            checkedItems = items
                                .filter { it.isChecked }
                                .sortedByDescending { it.checkedAt }
                                .toImmutableList(),
                            isLoading = false,
                            error = null,
                        )
                    }
                }
        }
    }

    private fun observeSearchResults() {
        viewModelScope.launch {
            searchItemsUseCase.searchWithDebounce(searchQueryFlow)
                .collect { suggestions ->
                    _uiState.update {
                        it.copy(autoCompleteSuggestions = suggestions.toImmutableList())
                    }
                }
        }
    }

    fun onAction(action: ListAction) {
        when (action) {
            is ListAction.UpdateInputText -> updateInputText(action.text)
            is ListAction.AddItem -> addItem(action.name)
            is ListAction.ClearInput -> clearInput()
            is ListAction.ToggleItem -> toggleItem(action.id, action.isChecked)
            is ListAction.DeleteItem -> deleteItem(action.id)
            is ListAction.UndoDelete -> undoDelete()
            is ListAction.ReorderItems -> reorderItems(action.fromIndex, action.toIndex)
            is ListAction.SelectItemForQuantity -> selectItemForQuantity(action.item)
            is ListAction.UpdateQuantity -> updateQuantity(action.id, action.quantity, action.unit)
            is ListAction.SearchItems -> searchItems(action.query)
            is ListAction.ClearSearch -> clearSearch()
            is ListAction.SelectAutoComplete -> selectAutoComplete(action.item)
            is ListAction.ClearCheckedItems -> clearCheckedItems()
        }
    }

    private fun updateInputText(text: String) {
        _uiState.update { it.copy(inputText = text) }
        if (text.isNotBlank()) {
            viewModelScope.launch {
                searchQueryFlow.emit(text)
            }
        } else {
            _uiState.update { it.copy(autoCompleteSuggestions = persistentListOf()) }
        }
    }

    private fun addItem(name: String) {
        if (name.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(inputText = "") }
            clearSearch()

            addItemUseCase(name)
                .onSuccess { item ->
                    _events.send(ListEvent.ItemAdded(item.name))
                }
                .onFailure { e ->
                    _events.send(ListEvent.Error(e.message ?: "Failed to add item"))
                }
        }
    }

    private fun clearInput() {
        _uiState.update {
            it.copy(
                inputText = "",
                autoCompleteSuggestions = persistentListOf()
            )
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
                        delay(UNDO_TIMEOUT_MS)
                        deletedItem = null
                        _events.send(ListEvent.UndoExpired)
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
                    .onFailure { e ->
                        _events.send(ListEvent.Error(e.message ?: "Failed to undo"))
                    }
                deletedItem = null
            }
        }
    }

    private fun reorderItems(fromIndex: Int, toIndex: Int) {
        viewModelScope.launch {
            reorderItemsUseCase(fromIndex, toIndex)
                .onFailure { e ->
                    _events.send(ListEvent.Error(e.message ?: "Failed to reorder"))
                }
        }
    }

    private fun selectItemForQuantity(item: CurrentListItem?) {
        _uiState.update { it.copy(selectedItemForQuantity = item) }
    }

    private fun updateQuantity(id: String, quantity: Int, unit: String?) {
        viewModelScope.launch {
            updateQuantityUseCase(id, quantity, unit)
                .onFailure { e ->
                    _events.send(ListEvent.Error(e.message ?: "Failed to update quantity"))
                }
            _uiState.update { it.copy(selectedItemForQuantity = null) }
        }
    }

    private fun searchItems(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        viewModelScope.launch {
            searchQueryFlow.emit(query)
        }
    }

    private fun clearSearch() {
        _uiState.update {
            it.copy(
                searchQuery = "",
                autoCompleteSuggestions = persistentListOf()
            )
        }
    }

    private fun selectAutoComplete(item: GroceryItem) {
        addItem(item.name)
    }

    private fun clearCheckedItems() {
        viewModelScope.launch {
            clearCheckedItemsUseCase()
                .onSuccess {
                    _events.send(ListEvent.CheckedItemsCleared)
                }
                .onFailure { e ->
                    _events.send(ListEvent.Error(e.message ?: "Failed to clear items"))
                }
        }
    }

    companion object {
        private const val UNDO_TIMEOUT_MS = 5000L
    }
}
```

## Additional Use Case

### ClearCheckedItemsUseCase.kt

```kotlin
package com.restock.core.domain.usecase

import com.restock.core.domain.repository.CurrentListRepository
import javax.inject.Inject

class ClearCheckedItemsUseCase @Inject constructor(
    private val currentListRepository: CurrentListRepository,
) {
    suspend operator fun invoke(): Result<Unit> =
        currentListRepository.deleteAllChecked()
}
```

## Acceptance Criteria

- [ ] ListUiState contains all necessary state
- [ ] All ListActions implemented
- [ ] Events for user feedback (snackbars)
- [ ] Undo mechanism with 5-second timeout
- [ ] Auto-complete with debounced search
- [ ] Reordering updates positions correctly
- [ ] Quantity selection state managed
- [ ] Unit tests for ViewModel

## Testing

```kotlin
class ListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: ListViewModel

    @Before
    fun setup() {
        viewModel = ListViewModel(
            getCurrentListUseCase = mockk(relaxed = true),
            addItemUseCase = mockk(relaxed = true),
            // ... other use cases
        )
    }

    @Test
    fun `addItem clears input and emits ItemAdded event`() = runTest {
        coEvery { addItemUseCase(any()) } returns Result.success(mockItem())

        viewModel.events.test {
            viewModel.onAction(ListAction.UpdateInputText("Milk"))
            viewModel.onAction(ListAction.AddItem("Milk"))

            val event = awaitItem()
            assertThat(event).isInstanceOf(ListEvent.ItemAdded::class.java)
        }

        assertThat(viewModel.uiState.value.inputText).isEmpty()
    }

    @Test
    fun `deleteItem starts undo timer and emits ItemDeleted`() = runTest {
        coEvery { deleteItemUseCase(any()) } returns Result.success(mockDeletedItem())

        viewModel.events.test {
            viewModel.onAction(ListAction.DeleteItem("1"))

            val event = awaitItem()
            assertThat(event).isInstanceOf(ListEvent.ItemDeleted::class.java)
        }
    }

    @Test
    fun `undoDelete restores item and emits UndoSuccessful`() = runTest {
        coEvery { deleteItemUseCase(any()) } returns Result.success(mockDeletedItem())
        coEvery { deleteItemUseCase.undoDelete(any()) } returns Result.success(Unit)

        viewModel.events.test {
            viewModel.onAction(ListAction.DeleteItem("1"))
            awaitItem() // ItemDeleted

            viewModel.onAction(ListAction.UndoDelete)
            val undoEvent = awaitItem()
            assertThat(undoEvent).isEqualTo(ListEvent.UndoSuccessful)
        }
    }
}
```

## Definition of Done

- [ ] ViewModel compiles without errors
- [ ] All actions produce expected state changes
- [ ] Events emitted for UI feedback
- [ ] Undo mechanism works correctly
- [ ] Search with debounce functional
- [ ] Unit tests pass (85%+ coverage)
- [ ] Integration with UI verified

## Dependent Tasks

This task blocks:
- UI implementation (ui_ux tasks)
- End-to-end testing

## Notes

- Use ImmutableList from kotlinx.collections.immutable for Compose stability
- Keep undo state in ViewModel, not in repository
- Debounce search queries to reduce database calls
- Cancel undo job when new delete happens
