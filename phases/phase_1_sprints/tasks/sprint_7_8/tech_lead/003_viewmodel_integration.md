# Task: ViewModel & Integration

**Sprint:** 7-8 (Smart Suggestions)
**Owner:** Tech Lead / Senior Developer
**Estimate:** 19 hours (T-9.1 through T-9.6, T-10.1 through T-10.6 combined)
**Priority:** P0
**Prerequisites:** 002_accept_dismiss_logic.md

---

## Sprint Context

This task integrates the suggestions feature with the existing list screen and handles edge cases, performance optimization, and final testing.

### Sprint Theme
> *"The moment when the app anticipates your needs."*

---

## Task Description

Integrate suggestions with the ListViewModel, handle edge cases, optimize performance, and complete integration testing.

## Integration with ListViewModel

### Updated ListViewModel.kt

```kotlin
package com.restock.feature.list.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.restock.core.domain.model.CurrentListItem
import com.restock.core.domain.model.GroceryItem
import com.restock.core.domain.model.Suggestion
import com.restock.core.domain.usecase.*
import com.restock.feature.suggestions.domain.*
import com.restock.feature.suggestions.ui.LearningState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ListViewModel @Inject constructor(
    // List use cases
    private val getCurrentListUseCase: GetCurrentListUseCase,
    private val addItemUseCase: AddItemUseCase,
    private val checkItemUseCase: CheckItemUseCase,
    private val deleteItemUseCase: DeleteItemUseCase,
    private val reorderItemsUseCase: ReorderItemsUseCase,
    private val updateQuantityUseCase: UpdateQuantityUseCase,
    private val searchItemsUseCase: SearchItemsUseCase,
    private val clearCheckedItemsUseCase: ClearCheckedItemsUseCase,
    // Suggestions use cases
    private val getSuggestionsUseCase: GetSuggestionsUseCase,
    private val acceptSuggestionUseCase: AcceptSuggestionUseCase,
    private val dismissSuggestionUseCase: DismissSuggestionUseCase,
    private val getShoppingTripCountUseCase: GetShoppingTripCountUseCase,
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
        observeSuggestions()
        observeSearchResults()
        loadLearningState()
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

                    // Check if list just became empty (all checked) for celebration
                    checkListCompletion(items)
                }
        }
    }

    private fun observeSuggestions() {
        viewModelScope.launch {
            getSuggestionsUseCase()
                .catch { e ->
                    // Log but don't show error - suggestions are secondary
                    timber.log.Timber.e(e, "Failed to load suggestions")
                }
                .collect { suggestions ->
                    _uiState.update { it.copy(suggestions = suggestions.toImmutableList()) }
                }
        }
    }

    private fun loadLearningState() {
        viewModelScope.launch {
            val tripCount = getShoppingTripCountUseCase()
            val learningState = when {
                tripCount >= 3 -> LearningState.READY
                tripCount >= 2 -> LearningState.ALMOST_READY
                tripCount >= 1 -> LearningState.LEARNING
                else -> LearningState.NOT_STARTED
            }
            _uiState.update { it.copy(learningState = learningState) }
        }
    }

    private fun checkListCompletion(items: List<CurrentListItem>) {
        val allChecked = items.isNotEmpty() && items.all { it.isChecked }
        if (allChecked && !_uiState.value.showCompletionCelebration) {
            _uiState.update { it.copy(showCompletionCelebration = true) }
        }
    }

    fun onAction(action: ListAction) {
        when (action) {
            // Input actions
            is ListAction.UpdateInputText -> updateInputText(action.text)
            is ListAction.AddItem -> addItem(action.name)
            is ListAction.ClearInput -> clearInput()

            // Item actions
            is ListAction.ToggleItem -> toggleItem(action.id, action.isChecked)
            is ListAction.DeleteItem -> deleteItem(action.id)
            is ListAction.UndoDelete -> undoDelete()

            // Reordering
            is ListAction.ReorderItems -> reorderItems(action.fromIndex, action.toIndex)

            // Quantity
            is ListAction.SelectItemForQuantity -> selectItemForQuantity(action.item)
            is ListAction.UpdateQuantity -> updateQuantity(action.id, action.quantity, action.unit)

            // Search
            is ListAction.SearchItems -> searchItems(action.query)
            is ListAction.ClearSearch -> clearSearch()
            is ListAction.SelectAutoComplete -> selectAutoComplete(action.item)

            // Bulk
            is ListAction.ClearCheckedItems -> clearCheckedItems()

            // Suggestions
            is ListAction.AcceptSuggestion -> acceptSuggestion(action.suggestion)
            is ListAction.DismissSuggestion -> dismissSuggestion(action.itemId)

            // Celebration
            is ListAction.DismissCompletionCelebration -> dismissCelebration()
        }
    }

    // ... existing list methods ...

    private fun acceptSuggestion(suggestion: Suggestion) {
        viewModelScope.launch {
            // Optimistically remove from UI
            _uiState.update { state ->
                state.copy(
                    suggestions = state.suggestions
                        .filter { it.itemId != suggestion.itemId }
                        .toImmutableList()
                )
            }

            acceptSuggestionUseCase(suggestion)
                .onSuccess {
                    _events.send(ListEvent.SuggestionAccepted(suggestion.itemName))
                }
                .onFailure { e ->
                    _events.send(ListEvent.Error(e.message ?: "Failed to add item"))
                }
        }
    }

    private fun dismissSuggestion(itemId: String) {
        viewModelScope.launch {
            // Optimistically remove from UI
            _uiState.update { state ->
                state.copy(
                    suggestions = state.suggestions
                        .filter { it.itemId != itemId }
                        .toImmutableList()
                )
            }

            dismissSuggestionUseCase(itemId)
        }
    }

    private fun dismissCelebration() {
        _uiState.update { it.copy(showCompletionCelebration = false) }
    }

    companion object {
        private const val UNDO_TIMEOUT_MS = 5000L
    }
}
```

### Updated ListUiState.kt

```kotlin
package com.restock.feature.list.ui

import com.restock.core.domain.model.CurrentListItem
import com.restock.core.domain.model.GroceryItem
import com.restock.core.domain.model.Suggestion
import com.restock.feature.suggestions.ui.LearningState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class ListUiState(
    // List items
    val uncheckedItems: ImmutableList<CurrentListItem> = persistentListOf(),
    val checkedItems: ImmutableList<CurrentListItem> = persistentListOf(),

    // Input
    val inputText: String = "",
    val searchQuery: String = "",
    val autoCompleteSuggestions: ImmutableList<GroceryItem> = persistentListOf(),

    // Suggestions
    val suggestions: ImmutableList<Suggestion> = persistentListOf(),
    val learningState: LearningState = LearningState.NOT_STARTED,

    // Loading/Error
    val isLoading: Boolean = true,
    val error: String? = null,

    // Dialogs
    val selectedItemForQuantity: CurrentListItem? = null,
    val showCompletionCelebration: Boolean = false,
) {
    val isEmpty: Boolean
        get() = uncheckedItems.isEmpty() && checkedItems.isEmpty()

    val totalItemCount: Int
        get() = uncheckedItems.size + checkedItems.size

    val showSuggestions: Boolean
        get() = suggestions.isNotEmpty() || learningState != LearningState.READY

    val hasSuggestions: Boolean
        get() = suggestions.isNotEmpty()
}
```

### Updated ListAction.kt

```kotlin
package com.restock.feature.list.ui

import com.restock.core.domain.model.CurrentListItem
import com.restock.core.domain.model.GroceryItem
import com.restock.core.domain.model.Suggestion

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

    // Suggestions
    data class AcceptSuggestion(val suggestion: Suggestion) : ListAction
    data class DismissSuggestion(val itemId: String) : ListAction

    // Celebration
    data object DismissCompletionCelebration : ListAction
}
```

### Updated ListEvent.kt

```kotlin
package com.restock.feature.list.ui

sealed interface ListEvent {
    data class ItemAdded(val name: String) : ListEvent
    data class ItemDeleted(val name: String, val canUndo: Boolean = true) : ListEvent
    data object UndoSuccessful : ListEvent
    data object UndoExpired : ListEvent
    data class Error(val message: String) : ListEvent
    data object CheckedItemsCleared : ListEvent

    // Suggestions
    data class SuggestionAccepted(val itemName: String) : ListEvent
}
```

## Edge Case Handling

```kotlin
// Handle edge cases in repository
class SuggestionRepositoryImpl {

    override suspend fun getSuggestions(limit: Int): List<Suggestion> =
        withContext(dispatcher) {
            try {
                // Edge case: No patterns yet
                val patterns = patternDao.getPatternsAboveConfidence(MIN_CONFIDENCE)
                if (patterns.isEmpty()) {
                    return@withContext emptyList()
                }

                // Edge case: All items already on list
                val currentItemIds = currentListDao.getCurrentItemIds().toSet()
                val availablePatterns = patterns.filter { it.itemId !in currentItemIds }
                if (availablePatterns.isEmpty()) {
                    return@withContext emptyList()
                }

                // Edge case: All items dismissed
                val dismissedIds = dismissalDao.getPersistentlyDismissedIds().toSet()
                val suggestablePattens = availablePatterns.filter { it.itemId !in dismissedIds }
                if (suggestablePattens.isEmpty()) {
                    return@withContext emptyList()
                }

                // Continue with normal logic...
            } catch (e: Exception) {
                Timber.e(e, "Error getting suggestions")
                return@withContext emptyList()
            }
        }
}
```

## Performance Optimization

```kotlin
// Optimize suggestion calculation
class SuggestionRepositoryImpl {

    override fun observeSuggestions(): Flow<List<Suggestion>> =
        combine(
            patternDao.observePatternsAboveConfidence(MIN_CONFIDENCE),
            purchaseDao.observeLatestPurchases(),
            currentListDao.observeCurrentItemIds(),
            dismissalDao.observeDismissals(),
        ) { patterns, purchases, currentItemIds, dismissals ->
            calculateSuggestions(patterns, purchases, currentItemIds.toSet(), dismissals)
        }
            // Debounce rapid changes
            .debounce(100)
            // Use conflated to skip intermediate values
            .conflate()
            .flowOn(dispatcher)

    // Use parallel processing for large datasets
    private suspend fun calculateSuggestions(...): List<Suggestion> {
        return withContext(Dispatchers.Default) {
            patterns
                .asFlow()
                .filter { it.itemId !in currentItemIds }
                .filter { dismissals[it.itemId]?.isPersistentlyDismissed != true }
                .mapNotNull { pattern ->
                    val lastPurchase = purchaseMap[pattern.itemId] ?: return@mapNotNull null
                    val daysSince = daysSince(lastPurchase.purchasedAt)
                    if (daysSince < pattern.avgIntervalDays - 1) return@mapNotNull null
                    suggestionScorer.score(pattern, lastPurchase)
                }
                .toList()
                .sortedByDescending { it.score }
                .take(MAX_SUGGESTIONS)
        }
    }
}
```

## Integration Testing

```kotlin
@HiltAndroidTest
class SuggestionsIntegrationTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject lateinit var database: RestockDatabase
    @Inject lateinit var patternWorkScheduler: PatternWorkScheduler

    @Test
    fun `full suggestions flow works end to end`() = runTest {
        // 1. Add item and check off 3 times
        val itemId = "milk-id"
        repeat(3) { i ->
            addAndCheckItem("Milk")
            advanceTimeBy(7.days)
        }

        // 2. Trigger pattern calculation
        patternWorkScheduler.triggerImmediateCalculation()
        advanceUntilIdle()

        // 3. Wait for suggestion to appear
        val viewModel = createListViewModel()
        val state = viewModel.uiState.first { it.hasSuggestions }

        // 4. Verify suggestion
        assertThat(state.suggestions).hasSize(1)
        assertThat(state.suggestions.first().itemName).isEqualTo("Milk")

        // 5. Accept suggestion
        viewModel.onAction(ListAction.AcceptSuggestion(state.suggestions.first()))
        advanceUntilIdle()

        // 6. Verify item added to list
        val listState = viewModel.uiState.first()
        assertThat(listState.uncheckedItems.map { it.name }).contains("Milk")

        // 7. Verify suggestion removed
        assertThat(listState.suggestions).isEmpty()
    }

    @Test
    fun `dismiss flow reduces confidence and eventually stops suggesting`() = runTest {
        // Setup: Item with pattern
        setupItemWithPattern("milk-id", avgDays = 7.0, confidence = 0.8)

        val viewModel = createListViewModel()

        // Dismiss 3 times
        repeat(3) {
            val state = viewModel.uiState.first { it.hasSuggestions }
            viewModel.onAction(ListAction.DismissSuggestion("milk-id"))
            advanceUntilIdle()
        }

        // Verify persistently dismissed
        val finalState = viewModel.uiState.first()
        assertThat(finalState.suggestions.map { it.itemId }).doesNotContain("milk-id")
    }
}
```

## Acceptance Criteria

- [ ] Suggestions integrated with ListViewModel
- [ ] Edge cases handled (no patterns, all dismissed, all on list)
- [ ] Performance optimized (debounce, conflate)
- [ ] Completion celebration works
- [ ] Integration tests pass
- [ ] Code review completed

## Definition of Done

- [ ] All code compiles without errors
- [ ] Edge cases handled gracefully
- [ ] Performance acceptable (< 100ms suggestion load)
- [ ] Integration tests pass
- [ ] Documentation updated
- [ ] Ready for UI implementation

## Notes

- Keep suggestions secondary - errors shouldn't break the list
- Debounce prevents rapid recalculation
- Optimistic updates make UI feel instant
- Celebration triggers when all items checked
