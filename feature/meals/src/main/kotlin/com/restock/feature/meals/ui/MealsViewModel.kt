package com.restock.feature.meals.ui

import androidx.lifecycle.viewModelScope
import com.restock.core.domain.model.CurrentListItem
import com.restock.core.domain.model.MealType
import com.restock.core.domain.repository.MealTagRepository
import com.restock.core.domain.usecase.CheckItemUseCase
import com.restock.core.ui.viewmodel.BaseViewModel
import com.restock.feature.meals.domain.AddMealTagUseCase
import com.restock.feature.meals.domain.GetItemsByMealUseCase
import com.restock.feature.meals.domain.RemoveMealTagUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

/**
 * ViewModel for the Meals screen.
 *
 * Follows the UDF (Unidirectional Data Flow) pattern by extending BaseViewModel.
 * Manages all meal-related operations including viewing items grouped by meal type,
 * toggling item check state, and managing meal tags.
 *
 * Key features:
 * - Reactive observation of items grouped by meal type
 * - Tag selection sheet for adding/removing meal tags
 * - Integration with CheckItemUseCase for marking items as purchased
 */
@HiltViewModel
class MealsViewModel
    @Inject
    constructor(
        private val getItemsByMealUseCase: GetItemsByMealUseCase,
        private val checkItemUseCase: CheckItemUseCase,
        private val addMealTagUseCase: AddMealTagUseCase,
        private val removeMealTagUseCase: RemoveMealTagUseCase,
        private val mealTagRepository: MealTagRepository,
    ) : BaseViewModel<MealsUiState, MealsAction, MealsEvent>(
            initialState = MealsUiState(),
        ) {
        init {
            observeItemsByMeal()
        }

        /**
         * Observes items grouped by meal type and updates the UI state.
         *
         * Items are grouped into:
         * - Breakfast, Lunch, Dinner, Snack (based on tags)
         * - Untagged (items without any meal tag)
         *
         * Items with multiple tags appear in multiple groups.
         */
        private fun observeItemsByMeal() {
            viewModelScope.launch {
                getItemsByMealUseCase()
                    .catch { e ->
                        updateState { copy(error = e.message, isLoading = false) }
                    }
                    .collect { groupedItems ->
                        updateState {
                            copy(
                                breakfastItems =
                                    (groupedItems[MealType.BREAKFAST] ?: emptyList())
                                        .toImmutableList(),
                                lunchItems =
                                    (groupedItems[MealType.LUNCH] ?: emptyList())
                                        .toImmutableList(),
                                dinnerItems =
                                    (groupedItems[MealType.DINNER] ?: emptyList())
                                        .toImmutableList(),
                                snackItems =
                                    (groupedItems[MealType.SNACK] ?: emptyList())
                                        .toImmutableList(),
                                untaggedItems =
                                    (groupedItems[null] ?: emptyList())
                                        .toImmutableList(),
                                isLoading = false,
                                error = null,
                            )
                        }
                    }
            }
        }

        override fun onAction(action: MealsAction) {
            when (action) {
                is MealsAction.ToggleItem -> toggleItem(action.id, action.isChecked)
                is MealsAction.SelectItemForTagging -> selectItemForTagging(action.item)
                is MealsAction.DismissTagSelection -> dismissTagSelection()
                is MealsAction.ToggleTag -> toggleTag(action.mealType)
                is MealsAction.DismissError -> dismissError()
                is MealsAction.Refresh -> refresh()
            }
        }

        /**
         * Toggles the checked state of an item.
         *
         * When checking: item is marked as purchased and removed from the meals view.
         * When unchecking: item returns to the meals view (if it has tags) or untagged.
         */
        private fun toggleItem(
            id: String,
            isChecked: Boolean,
        ) {
            viewModelScope.launch {
                // Find item name for event
                val itemName = findItemName(id)

                checkItemUseCase(id, isChecked)
                    .onSuccess {
                        if (isChecked && itemName != null) {
                            sendEvent(MealsEvent.ItemChecked(itemName))
                        }
                    }
                    .onFailure { e ->
                        sendEvent(MealsEvent.Error(e.message ?: ERROR_UPDATE_ITEM))
                    }
            }
        }

        /**
         * Opens the tag selection sheet for an item.
         *
         * Loads the current tags for the item so the user can toggle them.
         */
        private fun selectItemForTagging(item: CurrentListItem) {
            viewModelScope.launch {
                try {
                    val currentTags = mealTagRepository.getTagsForItem(item.itemId)
                    updateState {
                        copy(
                            selectedItemForTagging = item,
                            selectedItemTags = currentTags.toImmutableList(),
                        )
                    }
                } catch (
                    @Suppress("TooGenericExceptionCaught") e: Exception,
                ) {
                    sendEvent(MealsEvent.Error(e.message ?: ERROR_LOAD_TAGS))
                }
            }
        }

        /**
         * Dismisses the tag selection sheet.
         */
        private fun dismissTagSelection() {
            updateState {
                copy(
                    selectedItemForTagging = null,
                    selectedItemTags = persistentListOf(),
                )
            }
        }

        /**
         * Toggles a meal tag for the currently selected item.
         *
         * If the tag exists, removes it. If not, adds it.
         * Updates the UI optimistically and reverts on failure.
         */
        private fun toggleTag(mealType: MealType) {
            val item = currentState.selectedItemForTagging ?: return
            val currentTags = currentState.selectedItemTags
            val isAdding = mealType !in currentTags

            viewModelScope.launch {
                // Optimistic update
                val newTags =
                    if (isAdding) {
                        (currentTags + mealType).toImmutableList()
                    } else {
                        currentTags.filter { it != mealType }.toImmutableList()
                    }
                updateState { copy(selectedItemTags = newTags) }

                // Perform the actual operation
                val result =
                    if (isAdding) {
                        addMealTagUseCase(item.itemId, mealType)
                    } else {
                        removeMealTagUseCase(item.itemId, mealType)
                    }

                result
                    .onSuccess {
                        val event =
                            if (isAdding) {
                                MealsEvent.TagAdded(item.name, mealType)
                            } else {
                                MealsEvent.TagRemoved(item.name, mealType)
                            }
                        sendEvent(event)
                    }
                    .onFailure { e ->
                        // Revert optimistic update
                        updateState { copy(selectedItemTags = currentTags) }
                        sendEvent(MealsEvent.Error(e.message ?: ERROR_UPDATE_TAG))
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
         * Refreshes the meals data.
         */
        private fun refresh() {
            updateState { copy(isLoading = true) }
            // The observeItemsByMeal flow will automatically update the state
        }

        /**
         * Finds an item name by ID across all meal categories.
         */
        private fun findItemName(id: String): String? {
            val allItems =
                listOf(
                    currentState.breakfastItems,
                    currentState.lunchItems,
                    currentState.dinnerItems,
                    currentState.snackItems,
                    currentState.untaggedItems,
                ).flatten()

            return allItems.find { it.id == id }?.name
        }

        companion object {
            private const val ERROR_UPDATE_ITEM = "Failed to update item"
            private const val ERROR_LOAD_TAGS = "Failed to load tags"
            private const val ERROR_UPDATE_TAG = "Failed to update tag"
        }
    }
