package com.restock.core.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Base ViewModel implementing Unidirectional Data Flow (UDF) pattern.
 *
 * This base class establishes the standard for all UI state management in Restock,
 * ensuring predictable state updates, easy testing, and clear separation between
 * UI and business logic.
 *
 * ## Architecture Pattern
 * ```
 * User Action → ViewModel.onAction() → Use Case → Repository → Data Source
 *                   ↓
 *              State Update (StateFlow)
 *                   ↓
 *              UI Recomposes (Compose)
 * ```
 *
 * ## Usage
 *
 * ### 1. Define UiState
 * ```kotlin
 * data class ListUiState(
 *     val items: List<GroceryItem> = emptyList(),
 *     val isLoading: Boolean = true,
 *     val error: String? = null
 * )
 * ```
 *
 * ### 2. Define Actions
 * ```kotlin
 * sealed interface ListAction {
 *     data class AddItem(val name: String) : ListAction
 *     data class DeleteItem(val id: String) : ListAction
 * }
 * ```
 *
 * ### 3. Define Events
 * ```kotlin
 * sealed interface ListEvent {
 *     data class ItemAdded(val name: String) : ListEvent
 *     data class Error(val message: String) : ListEvent
 * }
 * ```
 *
 * ### 4. Implement ViewModel
 * ```kotlin
 * @HiltViewModel
 * class ListViewModel @Inject constructor(
 *     private val addItemUseCase: AddItemUseCase
 * ) : BaseViewModel<ListUiState, ListAction, ListEvent>(
 *     initialState = ListUiState()
 * ) {
 *     override fun onAction(action: ListAction) {
 *         when (action) {
 *             is ListAction.AddItem -> addItem(action.name)
 *             is ListAction.DeleteItem -> deleteItem(action.id)
 *         }
 *     }
 *
 *     private fun addItem(name: String) {
 *         viewModelScope.launch {
 *             updateState { copy(isLoading = true) }
 *             addItemUseCase(name)
 *                 .onSuccess { sendEvent(ListEvent.ItemAdded(name)) }
 *                 .onFailure { sendEvent(ListEvent.Error(it.message ?: "Failed")) }
 *             updateState { copy(isLoading = false) }
 *         }
 *     }
 * }
 * ```
 *
 * @param State The immutable state object representing UI state. Should be a data class.
 * @param Action User actions that can trigger state changes. Should be a sealed interface.
 * @param Event One-shot events (navigation, snackbars, etc.). Should be a sealed interface.
 */
abstract class BaseViewModel<State : Any, Action : Any, Event : Any>(
    initialState: State
) : ViewModel() {

    private val _uiState = MutableStateFlow(initialState)

    /**
     * The current UI state as a read-only StateFlow.
     *
     * Collect this in Compose using `collectAsStateWithLifecycle()`:
     * ```kotlin
     * val uiState by viewModel.uiState.collectAsStateWithLifecycle()
     * ```
     */
    val uiState: StateFlow<State> = _uiState.asStateFlow()

    private val _events = Channel<Event>(Channel.BUFFERED)

    /**
     * One-shot events flow for actions that should happen exactly once.
     *
     * Events are consumed exactly once and not persisted on configuration change.
     * Use for navigation, snackbars, toasts, and other transient UI effects.
     *
     * Collect in Compose using LaunchedEffect:
     * ```kotlin
     * LaunchedEffect(Unit) {
     *     viewModel.events.collect { event ->
     *         when (event) {
     *             is ListEvent.ItemAdded -> snackbarHostState.showSnackbar("Added")
     *             is ListEvent.Error -> snackbarHostState.showSnackbar(event.message)
     *         }
     *     }
     * }
     * ```
     */
    val events: Flow<Event> = _events.receiveAsFlow()

    /**
     * Returns the current state value synchronously.
     *
     * Prefer using [uiState] in composables for reactivity.
     * This is useful for accessing current state within ViewModel methods.
     */
    protected val currentState: State
        get() = _uiState.value

    /**
     * Updates the state using a reducer function.
     *
     * Thread-safe and can be called from any coroutine context.
     * Uses [MutableStateFlow.update] which atomically updates the value.
     *
     * @param reducer A function that receives the current state and returns the new state.
     *
     * Usage:
     * ```kotlin
     * updateState { copy(isLoading = true) }
     * updateState { copy(items = items + newItem, isLoading = false) }
     * ```
     */
    protected fun updateState(reducer: State.() -> State) {
        _uiState.update { currentState -> currentState.reducer() }
    }

    /**
     * Sends a one-shot event to the UI.
     *
     * Events are consumed exactly once and not persisted on configuration change.
     * Use for navigation, snackbars, toasts, and other transient UI effects.
     *
     * @param event The event to send to the UI layer.
     *
     * Usage:
     * ```kotlin
     * sendEvent(ListEvent.ItemAdded(item.name))
     * sendEvent(ListEvent.NavigateTo("details/${item.id}"))
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
     * This is the single entry point for all user interactions.
     * Use exhaustive `when` to ensure all actions are handled.
     *
     * @param action The action triggered by the user.
     *
     * Usage:
     * ```kotlin
     * override fun onAction(action: ListAction) {
     *     when (action) {
     *         is ListAction.AddItem -> addItem(action.name)
     *         is ListAction.DeleteItem -> deleteItem(action.id)
     *         is ListAction.ToggleItem -> toggleItem(action.id, action.isChecked)
     *     }
     * }
     * ```
     *
     * In Compose:
     * ```kotlin
     * Button(onClick = { viewModel.onAction(ListAction.AddItem("Milk")) })
     * ```
     */
    abstract fun onAction(action: Action)
}
