package com.restock.feature.list.ui

import androidx.lifecycle.viewModelScope
import com.restock.core.domain.usecase.AddItemUseCase
import com.restock.core.domain.usecase.CheckItemUseCase
import com.restock.core.domain.usecase.ClearCheckedItemsUseCase
import com.restock.core.domain.usecase.DeleteItemUseCase
import com.restock.core.domain.usecase.DeletedItem
import com.restock.core.domain.usecase.GetCurrentListUseCase
import com.restock.core.domain.usecase.GetLearningStatusUseCase
import com.restock.core.domain.usecase.ReorderItemsUseCase
import com.restock.core.domain.usecase.SearchItemsUseCase
import com.restock.core.domain.usecase.UpdateQuantityUseCase
import com.restock.core.ui.viewmodel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

/**
 * ViewModel for the main grocery list screen.
 *
 * Follows the UDF (Unidirectional Data Flow) pattern by extending BaseViewModel.
 * Manages all list operations including adding, checking, deleting, reordering,
 * and quantity adjustments.
 *
 * Key features:
 * - Reactive list observation with automatic checked/unchecked separation
 * - Debounced autocomplete search as user types
 * - 5-second undo window for deletions
 * - Quantity adjustment via bottom sheet
 * - Drag-and-drop reordering for unchecked items
 */
@HiltViewModel
class ListViewModel
    @Inject
    constructor(
        private val getCurrentListUseCase: GetCurrentListUseCase,
        private val addItemUseCase: AddItemUseCase,
        private val checkItemUseCase: CheckItemUseCase,
        private val deleteItemUseCase: DeleteItemUseCase,
        private val reorderItemsUseCase: ReorderItemsUseCase,
        private val updateQuantityUseCase: UpdateQuantityUseCase,
        private val searchItemsUseCase: SearchItemsUseCase,
        private val clearCheckedItemsUseCase: ClearCheckedItemsUseCase,
        private val getLearningStatusUseCase: GetLearningStatusUseCase,
    ) : BaseViewModel<ListUiState, ListAction, ListEvent>(
            initialState = ListUiState(),
        ) {
        /** Stores the last deleted item for undo functionality */
        private var deletedItem: DeletedItem? = null

        /** Job for the undo timeout - cancelled when undoing or deleting another item */
        private var undoJob: Job? = null

        /** SharedFlow for debounced search queries */
        private val searchQueryFlow = MutableSharedFlow<String>(replay = 1)

        init {
            observeCurrentList()
            observeSearchResults()
            observeLearningStatus()
        }

        /**
         * Observes the current list and splits into checked/unchecked sections.
         *
         * Unchecked items are sorted by position (drag-drop order).
         * Checked items are sorted by checkedAt descending (most recently checked first).
         * Also detects list completion to trigger celebration.
         */
        private fun observeCurrentList() {
            viewModelScope.launch {
                getCurrentListUseCase()
                    .catch { e ->
                        updateState { copy(error = e.message, isLoading = false) }
                    }
                    .collect { items ->
                        val previousState = uiState.value
                        val hadUncheckedItems = previousState.uncheckedItems.isNotEmpty()
                        val wasLoading = previousState.isLoading

                        val newUncheckedItems = items
                            .filter { !it.isChecked }
                            .sortedBy { it.position }
                            .toImmutableList()

                        val newCheckedItems = items
                            .filter { it.isChecked }
                            .sortedByDescending { it.checkedAt }
                            .toImmutableList()

                        updateState {
                            copy(
                                uncheckedItems = newUncheckedItems,
                                checkedItems = newCheckedItems,
                                isLoading = false,
                                error = null,
                            )
                        }

                        // Trigger celebration when list goes from having unchecked items
                        // to having none (all items checked off), but not on initial load
                        if (!wasLoading &&
                            hadUncheckedItems &&
                            newUncheckedItems.isEmpty() &&
                            newCheckedItems.isNotEmpty()
                        ) {
                            triggerCelebration()
                        }
                    }
            }
        }

        /**
         * Observes the search query flow and emits debounced suggestions.
         */
        private fun observeSearchResults() {
            viewModelScope.launch {
                searchItemsUseCase
                    .searchWithDebounce(searchQueryFlow)
                    .collect { suggestions ->
                        updateState {
                            copy(autoCompleteSuggestions = suggestions.toImmutableList())
                        }
                    }
            }
        }

        /**
         * Observes the learning status for UI feedback components.
         *
         * Updates the state with shopping trip count and pattern information,
         * which is used by [LearningStatusBanner] and [LearningCheckbox].
         */
        private fun observeLearningStatus() {
            viewModelScope.launch {
                getLearningStatusUseCase()
                    .catch { /* Silently ignore learning status errors - non-critical */ }
                    .collect { status ->
                        updateState { copy(learningStatus = status) }
                    }
            }
        }

        override fun onAction(action: ListAction) {
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
                is ListAction.DismissError -> dismissError()
                is ListAction.SetLearningFeedback -> setLearningFeedback(action.enabled)
                is ListAction.DismissCelebration -> dismissCelebration()
            }
        }

        /**
         * Updates the input text and triggers autocomplete search.
         */
        private fun updateInputText(text: String) {
            updateState { copy(inputText = text) }
            if (text.isNotBlank()) {
                viewModelScope.launch {
                    searchQueryFlow.emit(text)
                }
            } else {
                updateState { copy(autoCompleteSuggestions = persistentListOf()) }
            }
        }

        /**
         * Adds a new item to the list.
         *
         * Clears the input and search suggestions on success.
         * Emits ItemAdded event for snackbar feedback.
         */
        private fun addItem(name: String) {
            if (name.isBlank()) return

            viewModelScope.launch {
                // Clear input immediately for responsive UX
                updateState {
                    copy(
                        inputText = "",
                        autoCompleteSuggestions = persistentListOf(),
                    )
                }

                addItemUseCase(name)
                    .onSuccess { item ->
                        sendEvent(ListEvent.ItemAdded(item.name))
                    }
                    .onFailure { e ->
                        sendEvent(ListEvent.Error(e.message ?: ERROR_ADD_ITEM))
                    }
            }
        }

        /**
         * Clears the input text and autocomplete suggestions.
         */
        private fun clearInput() {
            updateState {
                copy(
                    inputText = "",
                    autoCompleteSuggestions = persistentListOf(),
                )
            }
        }

        /**
         * Toggles the checked state of an item.
         *
         * When checking: item moves to "In Cart" section.
         * When unchecking: item moves back to main list.
         */
        private fun toggleItem(
            id: String,
            isChecked: Boolean,
        ) {
            viewModelScope.launch {
                checkItemUseCase(id, isChecked)
                    .onFailure { e ->
                        sendEvent(ListEvent.Error(e.message ?: ERROR_UPDATE_ITEM))
                    }
            }
        }

        /**
         * Deletes an item and starts the undo timer.
         *
         * Stores the deleted item for potential undo.
         * Cancels any existing undo timer.
         * Starts a new 5-second undo window.
         */
        private fun deleteItem(id: String) {
            viewModelScope.launch {
                deleteItemUseCase(id)
                    .onSuccess { deleted ->
                        deletedItem = deleted
                        undoJob?.cancel()
                        undoJob =
                            launch {
                                delay(UNDO_TIMEOUT_MS)
                                deletedItem = null
                                sendEvent(ListEvent.UndoExpired)
                            }
                        sendEvent(ListEvent.ItemDeleted(deleted.name))
                    }
                    .onFailure { e ->
                        sendEvent(ListEvent.Error(e.message ?: ERROR_DELETE_ITEM))
                    }
            }
        }

        /**
         * Restores the last deleted item.
         *
         * Cancels the undo timer and clears the deleted item reference.
         */
        private fun undoDelete() {
            viewModelScope.launch {
                undoJob?.cancel()
                deletedItem?.let { item ->
                    deleteItemUseCase
                        .undoDelete(item)
                        .onSuccess {
                            sendEvent(ListEvent.UndoSuccessful)
                        }
                        .onFailure { e ->
                            sendEvent(ListEvent.Error(e.message ?: ERROR_UNDO))
                        }
                    deletedItem = null
                }
            }
        }

        /**
         * Reorders items via drag-and-drop.
         *
         * Only affects unchecked items (checked items cannot be reordered).
         */
        private fun reorderItems(
            fromIndex: Int,
            toIndex: Int,
        ) {
            if (fromIndex == toIndex) return

            viewModelScope.launch {
                reorderItemsUseCase(fromIndex, toIndex)
                    .onFailure { e ->
                        sendEvent(ListEvent.Error(e.message ?: ERROR_REORDER))
                    }
            }
        }

        /**
         * Selects an item for quantity adjustment.
         *
         * Pass null to close the quantity bottom sheet.
         */
        private fun selectItemForQuantity(item: com.restock.core.domain.model.CurrentListItem?) {
            updateState { copy(selectedItemForQuantity = item) }
        }

        /**
         * Updates the quantity and unit of an item.
         *
         * Closes the quantity sheet after update.
         */
        private fun updateQuantity(
            id: String,
            quantity: Int,
            unit: String?,
        ) {
            viewModelScope.launch {
                updateQuantityUseCase(id, quantity, unit)
                    .onSuccess {
                        sendEvent(ListEvent.QuantityUpdated)
                    }
                    .onFailure { e ->
                        sendEvent(ListEvent.Error(e.message ?: ERROR_UPDATE_QUANTITY))
                    }
                updateState { copy(selectedItemForQuantity = null) }
            }
        }

        /**
         * Updates the search/filter query.
         */
        private fun searchItems(query: String) {
            updateState { copy(searchQuery = query) }
            viewModelScope.launch {
                searchQueryFlow.emit(query)
            }
        }

        /**
         * Clears the search query and suggestions.
         */
        private fun clearSearch() {
            updateState {
                copy(
                    searchQuery = "",
                    autoCompleteSuggestions = persistentListOf(),
                )
            }
        }

        /**
         * Adds the selected autocomplete suggestion to the list.
         */
        private fun selectAutoComplete(item: com.restock.core.domain.model.GroceryItem) {
            addItem(item.name)
        }

        /**
         * Clears all checked items from the list.
         */
        private fun clearCheckedItems() {
            viewModelScope.launch {
                clearCheckedItemsUseCase()
                    .onSuccess {
                        sendEvent(ListEvent.CheckedItemsCleared)
                    }
                    .onFailure { e ->
                        sendEvent(ListEvent.Error(e.message ?: ERROR_CLEAR_CHECKED))
                    }
            }
        }

        /**
         * Dismisses the current error.
         */
        private fun dismissError() {
            updateState { copy(error = null) }
        }

        /**
         * Sets whether to show learning feedback animations.
         *
         * When disabled, checkboxes will use standard animations without
         * sparkle/glow effects. This setting can be controlled from user preferences.
         */
        private fun setLearningFeedback(enabled: Boolean) {
            updateState { copy(showLearningFeedback = enabled) }
        }

        /**
         * Dismisses the list completion celebration.
         */
        private fun dismissCelebration() {
            updateState { copy(showCelebration = false) }
        }

        /**
         * Triggers the celebration when the list is complete.
         *
         * Only triggers if there are checked items and no unchecked items.
         * Sends a ListCompleted event for any additional handling.
         */
        private fun triggerCelebration() {
            val currentState = uiState.value
            if (currentState.checkedItems.isNotEmpty() && currentState.uncheckedItems.isEmpty()) {
                updateState { copy(showCelebration = true) }
                viewModelScope.launch {
                    sendEvent(ListEvent.ListCompleted)
                }
            }
        }

        companion object {
            /** Undo window duration in milliseconds */
            const val UNDO_TIMEOUT_MS = 5000L

            // Error messages
            private const val ERROR_ADD_ITEM = "Failed to add item"
            private const val ERROR_UPDATE_ITEM = "Failed to update item"
            private const val ERROR_DELETE_ITEM = "Failed to delete item"
            private const val ERROR_UNDO = "Failed to undo"
            private const val ERROR_REORDER = "Failed to reorder"
            private const val ERROR_UPDATE_QUANTITY = "Failed to update quantity"
            private const val ERROR_CLEAR_CHECKED = "Failed to clear items"
        }
    }
