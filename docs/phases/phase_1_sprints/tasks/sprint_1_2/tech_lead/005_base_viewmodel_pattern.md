# Task: Create BaseViewModel with UDF Pattern

**Sprint:** 1-2 (Project Setup & Core Infrastructure)
**Owner:** Tech Lead / Senior Developer
**Estimate:** 6 hours (T-3.1 through T-3.4 combined)
**Priority:** P0
**Prerequisites:** 003_hilt_dependency_injection.md

---

## Sprint Context

The ViewModel pattern establishes the standard for all UI state management in Restock. Using Unidirectional Data Flow (UDF) ensures predictable state updates, easy testing, and clear separation between UI and business logic.

### Architecture Pattern
```
User Action → ViewModel.onAction() → Use Case → Repository → Data Source
                   ↓
              State Update (StateFlow)
                   ↓
              UI Recomposes (Compose)
```

---

## Task Description

Create the BaseViewModel class and establish patterns for UiState, Actions, and Events that all feature ViewModels will follow.

## Implementation

### BaseViewModel.kt

```kotlin
package com.restock.core.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * Base ViewModel implementing Unidirectional Data Flow (UDF) pattern.
 *
 * @param State The immutable state object representing UI state
 * @param Action User actions that can trigger state changes
 * @param Event One-shot events (navigation, snackbars, etc.)
 */
abstract class BaseViewModel<State : Any, Action : Any, Event : Any>(
    initialState: State
) : ViewModel() {

    private val _uiState = MutableStateFlow(initialState)
    val uiState: StateFlow<State> = _uiState.asStateFlow()

    private val _events = Channel<Event>(Channel.BUFFERED)
    val events: Flow<Event> = _events.receiveAsFlow()

    /**
     * Returns the current state value.
     * Prefer using uiState.value in composables for reactivity.
     */
    protected val currentState: State
        get() = _uiState.value

    /**
     * Updates the state using a reducer function.
     * Thread-safe and can be called from any coroutine context.
     *
     * Usage:
     * ```
     * updateState { copy(isLoading = true) }
     * ```
     */
    protected fun updateState(reducer: State.() -> State) {
        _uiState.update { it.reducer() }
    }

    /**
     * Sends a one-shot event to the UI.
     * Events are consumed exactly once (not persisted on configuration change).
     *
     * Usage:
     * ```
     * sendEvent(ListEvent.ItemAdded)
     * ```
     */
    protected fun sendEvent(event: Event) {
        viewModelScope.launch {
            _events.send(event)
        }
    }

    /**
     * Handles user actions. Override in concrete ViewModels.
     *
     * Usage:
     * ```
     * override fun onAction(action: ListAction) {
     *     when (action) {
     *         is ListAction.AddItem -> addItem(action.name)
     *         is ListAction.DeleteItem -> deleteItem(action.id)
     *     }
     * }
     * ```
     */
    abstract fun onAction(action: Action)
}
```

### UiState Pattern Example

```kotlin
package com.restock.feature.list.ui

/**
 * UI State should be:
 * - Immutable (data class)
 * - Complete (contains all data needed to render UI)
 * - Flat (avoid deep nesting)
 */
data class ListUiState(
    val uncheckedItems: List<CurrentListItem> = emptyList(),
    val checkedItems: List<CurrentListItem> = emptyList(),
    val autoCompleteSuggestions: List<GroceryItem> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = true,
    val error: String? = null,
) {
    // Derived properties for convenience
    val isEmpty: Boolean get() = uncheckedItems.isEmpty() && checkedItems.isEmpty()
    val totalItemCount: Int get() = uncheckedItems.size + checkedItems.size
}
```

### Action Pattern Example

```kotlin
package com.restock.feature.list.ui

/**
 * Actions should be:
 * - Sealed interface/class for exhaustive when
 * - Named from user's perspective (what they did)
 * - Contain only the data needed for that action
 */
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
```

### Event Pattern Example

```kotlin
package com.restock.feature.list.ui

/**
 * Events should be:
 * - One-shot (navigation, snackbars, toasts)
 * - Not state (don't store in UiState)
 * - Consumed exactly once
 */
sealed interface ListEvent {
    data class ItemAdded(val name: String) : ListEvent
    data class ItemDeleted(val name: String) : ListEvent
    data object UndoSuccessful : ListEvent
    data class Error(val message: String) : ListEvent
    data class NavigateTo(val destination: String) : ListEvent
}
```

### Complete ViewModel Example

```kotlin
package com.restock.feature.list.ui

import androidx.lifecycle.viewModelScope
import com.restock.core.ui.viewmodel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ListViewModel @Inject constructor(
    private val getCurrentListUseCase: GetCurrentListUseCase,
    private val addItemUseCase: AddItemUseCase,
    private val checkItemUseCase: CheckItemUseCase,
    private val deleteItemUseCase: DeleteItemUseCase,
) : BaseViewModel<ListUiState, ListAction, ListEvent>(
    initialState = ListUiState()
) {

    private var deletedItem: DeletedItem? = null
    private var undoJob: Job? = null

    init {
        observeCurrentList()
    }

    private fun observeCurrentList() {
        viewModelScope.launch {
            getCurrentListUseCase()
                .catch { e ->
                    updateState { copy(error = e.message, isLoading = false) }
                }
                .collect { items ->
                    updateState {
                        copy(
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

    override fun onAction(action: ListAction) {
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
        if (name.isBlank()) return

        viewModelScope.launch {
            addItemUseCase(name)
                .onSuccess { item ->
                    sendEvent(ListEvent.ItemAdded(item.name))
                }
                .onFailure { e ->
                    sendEvent(ListEvent.Error(e.message ?: "Failed to add item"))
                }
        }
    }

    private fun toggleItem(id: String, isChecked: Boolean) {
        viewModelScope.launch {
            checkItemUseCase(id, isChecked)
                .onFailure { e ->
                    sendEvent(ListEvent.Error(e.message ?: "Failed to update item"))
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
                    sendEvent(ListEvent.ItemDeleted(deleted.name))
                }
                .onFailure { e ->
                    sendEvent(ListEvent.Error(e.message ?: "Failed to delete item"))
                }
        }
    }

    private fun undoDelete() {
        viewModelScope.launch {
            undoJob?.cancel()
            deletedItem?.let { item ->
                deleteItemUseCase.undoDelete(item)
                    .onSuccess {
                        sendEvent(ListEvent.UndoSuccessful)
                    }
                deletedItem = null
            }
        }
    }

    // ... other action handlers
}
```

### UI Integration Example

```kotlin
@Composable
fun ListScreen(
    viewModel: ListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Handle one-shot events
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is ListEvent.ItemAdded -> {
                    snackbarHostState.showSnackbar("${event.name} added")
                }
                is ListEvent.ItemDeleted -> {
                    val result = snackbarHostState.showSnackbar(
                        message = "${event.name} deleted",
                        actionLabel = "Undo",
                        duration = SnackbarDuration.Short
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        viewModel.onAction(ListAction.UndoDelete)
                    }
                }
                is ListEvent.Error -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                // ... handle other events
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (uiState.isLoading) {
            LoadingIndicator()
        } else {
            GroceryList(
                items = uiState.uncheckedItems,
                checkedItems = uiState.checkedItems,
                onToggleItem = { id, checked ->
                    viewModel.onAction(ListAction.ToggleItem(id, checked))
                },
                onDeleteItem = { id ->
                    viewModel.onAction(ListAction.DeleteItem(id))
                },
                modifier = Modifier.padding(padding)
            )
        }
    }
}
```

## Acceptance Criteria

- [ ] BaseViewModel class created with StateFlow and Channel
- [ ] updateState() method is thread-safe
- [ ] sendEvent() delivers events exactly once
- [ ] Example UiState pattern documented
- [ ] Example Action pattern documented
- [ ] Example Event pattern documented
- [ ] Sample ViewModel with Hilt injection works
- [ ] UI integration pattern documented

## Testing Pattern

```kotlin
class ListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: ListViewModel
    private val getCurrentListUseCase = mockk<GetCurrentListUseCase>()
    private val addItemUseCase = mockk<AddItemUseCase>()

    @Before
    fun setup() {
        every { getCurrentListUseCase() } returns flowOf(emptyList())
        viewModel = ListViewModel(
            getCurrentListUseCase = getCurrentListUseCase,
            addItemUseCase = addItemUseCase,
            // ... other use cases
        )
    }

    @Test
    fun `addItem success emits ItemAdded event`() = runTest {
        coEvery { addItemUseCase(any()) } returns Result.success(mockItem)

        viewModel.events.test {
            viewModel.onAction(ListAction.AddItem("Milk"))

            val event = awaitItem()
            assertThat(event).isInstanceOf(ListEvent.ItemAdded::class.java)
        }
    }

    @Test
    fun `initial state is loading`() {
        assertThat(viewModel.uiState.value.isLoading).isTrue()
    }
}
```

## Definition of Done

- [ ] BaseViewModel class implemented
- [ ] Pattern examples documented
- [ ] Sample ViewModel compiles and runs
- [ ] Unit test pattern established
- [ ] Architecture documentation updated

## Dependent Tasks

This task blocks:
- All feature ViewModels (Sprint 3-4+)
- UI development requiring state management

## Notes

- Always use `copy()` for state updates to maintain immutability
- Events are for one-shot actions; state is for persistent UI data
- Prefer sealed interfaces over sealed classes for actions/events
- Keep state flat - avoid deeply nested objects
- Use `derivedStateOf` in composables for expensive computations
