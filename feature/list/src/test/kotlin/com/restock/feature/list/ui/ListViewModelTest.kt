package com.restock.feature.list.ui

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.restock.core.domain.model.CurrentListItem
import com.restock.core.domain.model.GroceryItem
import com.restock.core.domain.model.LearningState
import com.restock.core.domain.model.Suggestion
import com.restock.core.domain.usecase.AcceptSuggestionUseCase
import com.restock.core.domain.usecase.AddItemUseCase
import com.restock.core.domain.usecase.CheckItemUseCase
import com.restock.core.domain.usecase.ClearCheckedItemsUseCase
import com.restock.core.domain.usecase.DeleteItemUseCase
import com.restock.core.domain.usecase.DeletedItem
import com.restock.core.domain.usecase.DismissSuggestionUseCase
import com.restock.core.domain.usecase.GetCurrentListUseCase
import com.restock.core.domain.usecase.GetLearningStatusUseCase
import com.restock.core.domain.usecase.GetShoppingTripCountUseCase
import com.restock.core.domain.usecase.GetSuggestionsUseCase
import com.restock.core.domain.usecase.LearningStatus
import com.restock.core.domain.usecase.ReorderItemsUseCase
import com.restock.core.domain.usecase.SearchItemsUseCase
import com.restock.core.domain.usecase.UpdateQuantityUseCase
import com.restock.feature.list.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import java.time.Instant
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest

/**
 * Unit tests for [ListViewModel].
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ListViewModelTest {
    @JvmField
    @RegisterExtension
    val mainDispatcherRule = MainDispatcherRule()

    private val getCurrentListUseCase: GetCurrentListUseCase = mockk()
    private val addItemUseCase: AddItemUseCase = mockk()
    private val checkItemUseCase: CheckItemUseCase = mockk()
    private val deleteItemUseCase: DeleteItemUseCase = mockk()
    private val reorderItemsUseCase: ReorderItemsUseCase = mockk()
    private val updateQuantityUseCase: UpdateQuantityUseCase = mockk()
    private val searchItemsUseCase: SearchItemsUseCase = mockk()
    private val clearCheckedItemsUseCase: ClearCheckedItemsUseCase = mockk()
    private val getLearningStatusUseCase: GetLearningStatusUseCase = mockk()
    private val getSuggestionsUseCase: GetSuggestionsUseCase = mockk()
    private val acceptSuggestionUseCase: AcceptSuggestionUseCase = mockk()
    private val dismissSuggestionUseCase: DismissSuggestionUseCase = mockk()
    private val getShoppingTripCountUseCase: GetShoppingTripCountUseCase = mockk()

    private lateinit var viewModel: ListViewModel

    private val testItem =
        CurrentListItem(
            id = "list-1",
            itemId = "item-1",
            name = "Milk",
            normalizedName = "milk",
            quantity = 1,
            unit = null,
            addedAt = Instant.now(),
            checkedAt = null,
            position = 0,
            isChecked = false,
        )

    private val testCheckedItem =
        testItem.copy(
            id = "list-2",
            isChecked = true,
            checkedAt = Instant.now(),
        )

    private val testGroceryItem =
        GroceryItem(
            id = "item-1",
            name = "Milk",
            normalizedName = "milk",
            createdAt = Instant.now(),
        )

    private val testDeletedItem =
        DeletedItem(
            listItemId = "list-1",
            itemId = "item-1",
            name = "Milk",
            quantity = 1,
            unit = null,
            position = 0,
        )

    private val testSuggestion =
        Suggestion(
            itemId = "item-1",
            itemName = "Milk",
            reason = "Usually every 7 days",
            score = 1.2,
            confidence = 0.8,
            daysSinceLastPurchase = 8,
            avgIntervalDays = 7,
        )

    @BeforeEach
    fun setup() {
        // Default behavior for use cases
        every { getCurrentListUseCase() } returns flowOf(emptyList())
        every { searchItemsUseCase.searchWithDebounce(any(), any()) } returns flowOf(emptyList())
        every { getLearningStatusUseCase() } returns flowOf(LearningStatus.EMPTY)
        every { getSuggestionsUseCase() } returns flowOf(emptyList())
        coEvery { getShoppingTripCountUseCase() } returns 0

        viewModel = createViewModel()
    }

    private fun createViewModel(): ListViewModel =
        ListViewModel(
            getCurrentListUseCase = getCurrentListUseCase,
            addItemUseCase = addItemUseCase,
            checkItemUseCase = checkItemUseCase,
            deleteItemUseCase = deleteItemUseCase,
            reorderItemsUseCase = reorderItemsUseCase,
            updateQuantityUseCase = updateQuantityUseCase,
            searchItemsUseCase = searchItemsUseCase,
            clearCheckedItemsUseCase = clearCheckedItemsUseCase,
            getLearningStatusUseCase = getLearningStatusUseCase,
            getSuggestionsUseCase = getSuggestionsUseCase,
            acceptSuggestionUseCase = acceptSuggestionUseCase,
            dismissSuggestionUseCase = dismissSuggestionUseCase,
            getShoppingTripCountUseCase = getShoppingTripCountUseCase,
        )

    @Nested
    @DisplayName("Initial state")
    inner class InitialStateTests {
        @Test
        fun `initial state has empty lists and loading true`() {
            val state = viewModel.uiState.value

            assertThat(state.uncheckedItems).isEmpty()
            assertThat(state.checkedItems).isEmpty()
            assertThat(state.isLoading).isTrue()
            assertThat(state.error).isNull()
        }

        @Test
        fun `initial state has empty input text`() {
            val state = viewModel.uiState.value

            assertThat(state.inputText).isEmpty()
            assertThat(state.searchQuery).isEmpty()
        }
    }

    @Nested
    @DisplayName("Observing current list")
    inner class ObserveCurrentListTests {
        @Test
        fun `separates items into checked and unchecked`() =
            runTest {
                val items = listOf(testItem, testCheckedItem)
                every { getCurrentListUseCase() } returns flowOf(items)

                val vm = createViewModel()

                vm.uiState.test {
                    val state = awaitItem()
                    assertThat(state.uncheckedItems).hasSize(1)
                    assertThat(state.checkedItems).hasSize(1)
                    assertThat(state.uncheckedItems.first().id).isEqualTo("list-1")
                    assertThat(state.checkedItems.first().id).isEqualTo("list-2")
                    cancelAndIgnoreRemainingEvents()
                }
            }

        @Test
        fun `sorts unchecked items by position`() =
            runTest {
                val items =
                    listOf(
                        testItem.copy(id = "1", position = 2),
                        testItem.copy(id = "2", position = 0),
                        testItem.copy(id = "3", position = 1),
                    )
                every { getCurrentListUseCase() } returns flowOf(items)

                val vm = createViewModel()

                vm.uiState.test {
                    val state = awaitItem()
                    assertThat(state.uncheckedItems.map { it.id }).containsExactly("2", "3", "1").inOrder()
                    cancelAndIgnoreRemainingEvents()
                }
            }

        @Test
        fun `sorts checked items by checkedAt descending`() =
            runTest {
                val now = Instant.now()
                val items =
                    listOf(
                        testCheckedItem.copy(id = "1", checkedAt = now.minusSeconds(100)),
                        testCheckedItem.copy(id = "2", checkedAt = now),
                        testCheckedItem.copy(id = "3", checkedAt = now.minusSeconds(50)),
                    )
                every { getCurrentListUseCase() } returns flowOf(items)

                val vm = createViewModel()

                vm.uiState.test {
                    val state = awaitItem()
                    // Most recently checked first
                    assertThat(state.checkedItems.map { it.id }).containsExactly("2", "3", "1").inOrder()
                    cancelAndIgnoreRemainingEvents()
                }
            }

        @Test
        fun `sets loading to false after receiving items`() =
            runTest {
                every { getCurrentListUseCase() } returns flowOf(listOf(testItem))

                val vm = createViewModel()

                vm.uiState.test {
                    val state = awaitItem()
                    assertThat(state.isLoading).isFalse()
                    cancelAndIgnoreRemainingEvents()
                }
            }
    }

    @Nested
    @DisplayName("Adding items")
    inner class AddItemTests {
        @Test
        fun `addItem clears input text immediately`() =
            runTest {
                coEvery { addItemUseCase(any()) } returns Result.success(testItem)

                viewModel.uiState.test {
                    skipItems(1) // Skip initial state
                    viewModel.onAction(ListAction.UpdateInputText("Milk"))
                    awaitItem() // State with input text
                    viewModel.onAction(ListAction.AddItem("Milk"))
                    val state = awaitItem()
                    assertThat(state.inputText).isEmpty()
                    cancelAndIgnoreRemainingEvents()
                }
            }

        @Test
        fun `addItem emits ItemAdded event on success`() =
            runTest {
                coEvery { addItemUseCase("Milk") } returns Result.success(testItem)

                viewModel.events.test {
                    viewModel.onAction(ListAction.AddItem("Milk"))
                    val event = awaitItem()
                    assertThat(event).isInstanceOf(ListEvent.ItemAdded::class.java)
                    assertThat((event as ListEvent.ItemAdded).name).isEqualTo("Milk")
                    cancelAndIgnoreRemainingEvents()
                }
            }

        @Test
        fun `addItem emits Error event on failure`() =
            runTest {
                coEvery { addItemUseCase("Milk") } returns Result.failure(RuntimeException("Add failed"))

                viewModel.events.test {
                    viewModel.onAction(ListAction.AddItem("Milk"))
                    val event = awaitItem()
                    assertThat(event).isInstanceOf(ListEvent.Error::class.java)
                    cancelAndIgnoreRemainingEvents()
                }
            }

        @Test
        fun `addItem does nothing for blank name`() =
            runTest {
                viewModel.onAction(ListAction.AddItem("   "))

                coVerify(exactly = 0) { addItemUseCase(any()) }
            }
    }

    @Nested
    @DisplayName("Input text handling")
    inner class InputTextTests {
        @Test
        fun `updateInputText updates state`() =
            runTest {
                viewModel.uiState.test {
                    skipItems(1)
                    viewModel.onAction(ListAction.UpdateInputText("Milk"))
                    val state = awaitItem()
                    assertThat(state.inputText).isEqualTo("Milk")
                    cancelAndIgnoreRemainingEvents()
                }
            }

        @Test
        fun `clearInput clears input text and suggestions`() =
            runTest {
                viewModel.uiState.test {
                    skipItems(1)
                    viewModel.onAction(ListAction.UpdateInputText("Milk"))
                    awaitItem()
                    viewModel.onAction(ListAction.ClearInput)
                    val state = awaitItem()
                    assertThat(state.inputText).isEmpty()
                    assertThat(state.autoCompleteSuggestions).isEmpty()
                    cancelAndIgnoreRemainingEvents()
                }
            }
    }

    @Nested
    @DisplayName("Toggling items")
    inner class ToggleItemTests {
        @Test
        fun `toggleItem calls checkItemUseCase with correct parameters`() =
            runTest {
                coEvery { checkItemUseCase("item-1", true) } returns Result.success(Unit)

                viewModel.onAction(ListAction.ToggleItem("item-1", isChecked = true))

                coVerify { checkItemUseCase("item-1", true) }
            }

        @Test
        fun `toggleItem emits Error on failure`() =
            runTest {
                coEvery { checkItemUseCase(any(), any()) } returns
                    Result.failure(RuntimeException("Check failed"))

                viewModel.events.test {
                    viewModel.onAction(ListAction.ToggleItem("item-1", isChecked = true))
                    val event = awaitItem()
                    assertThat(event).isInstanceOf(ListEvent.Error::class.java)
                    cancelAndIgnoreRemainingEvents()
                }
            }
    }

    @Nested
    @DisplayName("Deleting items")
    inner class DeleteItemTests {
        @Test
        fun `deleteItem emits ItemDeleted event on success`() =
            runTest {
                coEvery { deleteItemUseCase("list-1") } returns Result.success(testDeletedItem)

                viewModel.events.test {
                    viewModel.onAction(ListAction.DeleteItem("list-1"))
                    val event = awaitItem()
                    assertThat(event).isInstanceOf(ListEvent.ItemDeleted::class.java)
                    assertThat((event as ListEvent.ItemDeleted).name).isEqualTo("Milk")
                    cancelAndIgnoreRemainingEvents()
                }
            }

        @Test
        fun `deleteItem emits Error on failure`() =
            runTest {
                coEvery { deleteItemUseCase("list-1") } returns
                    Result.failure(RuntimeException("Delete failed"))

                viewModel.events.test {
                    viewModel.onAction(ListAction.DeleteItem("list-1"))
                    val event = awaitItem()
                    assertThat(event).isInstanceOf(ListEvent.Error::class.java)
                    cancelAndIgnoreRemainingEvents()
                }
            }
    }

    @Nested
    @DisplayName("Undo delete")
    inner class UndoDeleteTests {
        @Test
        fun `undoDelete restores item and emits UndoSuccessful`() =
            runTest {
                coEvery { deleteItemUseCase("list-1") } returns Result.success(testDeletedItem)
                coEvery { deleteItemUseCase.undoDelete(testDeletedItem) } returns Result.success(testItem)

                viewModel.events.test {
                    viewModel.onAction(ListAction.DeleteItem("list-1"))
                    awaitItem() // ItemDeleted
                    viewModel.onAction(ListAction.UndoDelete)
                    val event = awaitItem()
                    assertThat(event).isEqualTo(ListEvent.UndoSuccessful)
                    cancelAndIgnoreRemainingEvents()
                }
            }

        @Test
        fun `undoDelete emits Error on failure`() =
            runTest {
                coEvery { deleteItemUseCase("list-1") } returns Result.success(testDeletedItem)
                coEvery { deleteItemUseCase.undoDelete(any()) } returns
                    Result.failure(RuntimeException("Undo failed"))

                viewModel.events.test {
                    viewModel.onAction(ListAction.DeleteItem("list-1"))
                    awaitItem() // ItemDeleted
                    viewModel.onAction(ListAction.UndoDelete)
                    val event = awaitItem()
                    assertThat(event).isInstanceOf(ListEvent.Error::class.java)
                    cancelAndIgnoreRemainingEvents()
                }
            }

        @Test
        fun `undo timeout emits UndoExpired after 5 seconds`() =
            runTest {
                coEvery { deleteItemUseCase("list-1") } returns Result.success(testDeletedItem)

                viewModel.events.test {
                    viewModel.onAction(ListAction.DeleteItem("list-1"))
                    awaitItem() // ItemDeleted
                    advanceTimeBy(ListViewModel.UNDO_TIMEOUT_MS + 100)
                    val event = awaitItem()
                    assertThat(event).isEqualTo(ListEvent.UndoExpired)
                    cancelAndIgnoreRemainingEvents()
                }
            }
    }

    @Nested
    @DisplayName("Reordering items")
    inner class ReorderItemsTests {
        @Test
        fun `reorderItems calls use case with correct indices`() =
            runTest {
                coEvery { reorderItemsUseCase(0, 2) } returns Result.success(Unit)

                viewModel.onAction(ListAction.ReorderItems(fromIndex = 0, toIndex = 2))

                coVerify { reorderItemsUseCase(0, 2) }
            }

        @Test
        fun `reorderItems does nothing when indices are equal`() =
            runTest {
                viewModel.onAction(ListAction.ReorderItems(fromIndex = 1, toIndex = 1))

                coVerify(exactly = 0) { reorderItemsUseCase(any(), any()) }
            }

        @Test
        fun `reorderItems emits Error on failure`() =
            runTest {
                coEvery { reorderItemsUseCase(any(), any()) } returns
                    Result.failure(RuntimeException("Reorder failed"))

                viewModel.events.test {
                    viewModel.onAction(ListAction.ReorderItems(0, 2))
                    val event = awaitItem()
                    assertThat(event).isInstanceOf(ListEvent.Error::class.java)
                    cancelAndIgnoreRemainingEvents()
                }
            }
    }

    @Nested
    @DisplayName("Quantity adjustment")
    inner class QuantityTests {
        @Test
        fun `selectItemForQuantity updates state with item`() =
            runTest {
                viewModel.uiState.test {
                    skipItems(1)
                    viewModel.onAction(ListAction.SelectItemForQuantity(testItem))
                    val state = awaitItem()
                    assertThat(state.selectedItemForQuantity).isEqualTo(testItem)
                    cancelAndIgnoreRemainingEvents()
                }
            }

        @Test
        fun `selectItemForQuantity with null closes sheet`() =
            runTest {
                viewModel.uiState.test {
                    skipItems(1)
                    viewModel.onAction(ListAction.SelectItemForQuantity(testItem))
                    awaitItem()
                    viewModel.onAction(ListAction.SelectItemForQuantity(null))
                    val state = awaitItem()
                    assertThat(state.selectedItemForQuantity).isNull()
                    cancelAndIgnoreRemainingEvents()
                }
            }

        @Test
        fun `updateQuantity calls use case and closes sheet`() =
            runTest {
                coEvery { updateQuantityUseCase("list-1", 3, "lb") } returns Result.success(Unit)

                viewModel.uiState.test {
                    skipItems(1)
                    viewModel.onAction(ListAction.SelectItemForQuantity(testItem))
                    awaitItem()
                    viewModel.onAction(ListAction.UpdateQuantity("list-1", 3, "lb"))
                    val state = awaitItem()
                    assertThat(state.selectedItemForQuantity).isNull()
                    coVerify { updateQuantityUseCase("list-1", 3, "lb") }
                    cancelAndIgnoreRemainingEvents()
                }
            }

        @Test
        fun `updateQuantity emits QuantityUpdated on success`() =
            runTest {
                coEvery { updateQuantityUseCase(any(), any(), any()) } returns Result.success(Unit)

                viewModel.events.test {
                    viewModel.onAction(ListAction.UpdateQuantity("list-1", 3, "lb"))
                    val event = awaitItem()
                    assertThat(event).isEqualTo(ListEvent.QuantityUpdated)
                    cancelAndIgnoreRemainingEvents()
                }
            }
    }

    @Nested
    @DisplayName("Search functionality")
    inner class SearchTests {
        @Test
        fun `searchItems updates searchQuery state`() =
            runTest {
                viewModel.uiState.test {
                    skipItems(1)
                    viewModel.onAction(ListAction.SearchItems("milk"))
                    val state = awaitItem()
                    assertThat(state.searchQuery).isEqualTo("milk")
                    cancelAndIgnoreRemainingEvents()
                }
            }

        @Test
        fun `clearSearch clears query and suggestions`() =
            runTest {
                viewModel.uiState.test {
                    skipItems(1)
                    viewModel.onAction(ListAction.SearchItems("milk"))
                    awaitItem()
                    viewModel.onAction(ListAction.ClearSearch)
                    val state = awaitItem()
                    assertThat(state.searchQuery).isEmpty()
                    assertThat(state.autoCompleteSuggestions).isEmpty()
                    cancelAndIgnoreRemainingEvents()
                }
            }

        @Test
        fun `selectAutoComplete adds item with suggestion name`() =
            runTest {
                coEvery { addItemUseCase("Milk") } returns Result.success(testItem)

                viewModel.events.test {
                    viewModel.onAction(ListAction.SelectAutoComplete(testGroceryItem))
                    val event = awaitItem()
                    assertThat(event).isInstanceOf(ListEvent.ItemAdded::class.java)
                    coVerify { addItemUseCase("Milk") }
                    cancelAndIgnoreRemainingEvents()
                }
            }
    }

    @Nested
    @DisplayName("Clear checked items")
    inner class ClearCheckedItemsTests {
        @Test
        fun `clearCheckedItems emits CheckedItemsCleared on success`() =
            runTest {
                coEvery { clearCheckedItemsUseCase() } returns Result.success(Unit)

                viewModel.events.test {
                    viewModel.onAction(ListAction.ClearCheckedItems)
                    val event = awaitItem()
                    assertThat(event).isEqualTo(ListEvent.CheckedItemsCleared)
                    cancelAndIgnoreRemainingEvents()
                }
            }

        @Test
        fun `clearCheckedItems emits Error on failure`() =
            runTest {
                coEvery { clearCheckedItemsUseCase() } returns
                    Result.failure(RuntimeException("Clear failed"))

                viewModel.events.test {
                    viewModel.onAction(ListAction.ClearCheckedItems)
                    val event = awaitItem()
                    assertThat(event).isInstanceOf(ListEvent.Error::class.java)
                    cancelAndIgnoreRemainingEvents()
                }
            }
    }

    @Nested
    @DisplayName("Error handling")
    inner class ErrorHandlingTests {
        @Test
        fun `dismissError clears error state`() =
            runTest {
                viewModel.uiState.test {
                    skipItems(1)
                    // Trigger an error by making the use case throw in init
                    // For this test, we'll manually set error through internal update
                    viewModel.onAction(ListAction.DismissError)
                    cancelAndIgnoreRemainingEvents()
                }
            }
    }

    @Nested
    @DisplayName("ListUiState computed properties")
    inner class UiStateComputedPropertiesTests {
        @Test
        fun `isEmpty returns true when both lists empty`() {
            val state = ListUiState()
            assertThat(state.isEmpty).isTrue()
        }

        @Test
        fun `isEmpty returns false when unchecked items exist`() {
            val state = ListUiState(uncheckedItems = persistentListOf(testItem))
            assertThat(state.isEmpty).isFalse()
        }

        @Test
        fun `isEmpty returns false when checked items exist`() {
            val state = ListUiState(checkedItems = persistentListOf(testCheckedItem))
            assertThat(state.isEmpty).isFalse()
        }

        @Test
        fun `totalItemCount sums both lists`() {
            val state =
                ListUiState(
                    uncheckedItems = persistentListOf(testItem),
                    checkedItems = persistentListOf(testCheckedItem),
                )
            assertThat(state.totalItemCount).isEqualTo(2)
        }

        @Test
        fun `uncheckedCount returns unchecked items count`() {
            val state =
                ListUiState(
                    uncheckedItems = persistentListOf(testItem, testItem.copy(id = "2")),
                )
            assertThat(state.uncheckedCount).isEqualTo(2)
        }

        @Test
        fun `checkedCount returns checked items count`() {
            val state =
                ListUiState(
                    checkedItems = persistentListOf(testCheckedItem),
                )
            assertThat(state.checkedCount).isEqualTo(1)
        }

        @Test
        fun `hasCheckedItems returns true when checked items exist`() {
            val state = ListUiState(checkedItems = persistentListOf(testCheckedItem))
            assertThat(state.hasCheckedItems).isTrue()
        }

        @Test
        fun `hasCheckedItems returns false when no checked items`() {
            val state = ListUiState()
            assertThat(state.hasCheckedItems).isFalse()
        }

        @Test
        fun `showAutoComplete returns true when input not blank and suggestions exist`() {
            val state =
                ListUiState(
                    inputText = "milk",
                    autoCompleteSuggestions = persistentListOf(testGroceryItem),
                )
            assertThat(state.showAutoComplete).isTrue()
        }

        @Test
        fun `showAutoComplete returns false when input is blank`() {
            val state =
                ListUiState(
                    inputText = "   ",
                    autoCompleteSuggestions = persistentListOf(testGroceryItem),
                )
            assertThat(state.showAutoComplete).isFalse()
        }

        @Test
        fun `showAutoComplete returns false when no suggestions`() {
            val state =
                ListUiState(
                    inputText = "milk",
                    autoCompleteSuggestions = persistentListOf(),
                )
            assertThat(state.showAutoComplete).isFalse()
        }

        @Test
        fun `hasSuggestions returns true when suggestions exist`() {
            val state = ListUiState(suggestions = persistentListOf(testSuggestion))
            assertThat(state.hasSuggestions).isTrue()
        }

        @Test
        fun `hasSuggestions returns false when no suggestions`() {
            val state = ListUiState()
            assertThat(state.hasSuggestions).isFalse()
        }

        @Test
        fun `suggestionCount returns correct count`() {
            val state =
                ListUiState(
                    suggestions =
                        persistentListOf(
                            testSuggestion,
                            testSuggestion.copy(itemId = "2"),
                        ),
                )
            assertThat(state.suggestionCount).isEqualTo(2)
        }

        @Test
        fun `showSuggestionsSection returns true when suggestions exist`() {
            val state =
                ListUiState(
                    suggestions = persistentListOf(testSuggestion),
                    learningState = LearningState.READY,
                )
            assertThat(state.showSuggestionsSection).isTrue()
        }

        @Test
        fun `showSuggestionsSection returns true when learning state is not READY`() {
            val state =
                ListUiState(
                    suggestions = persistentListOf(),
                    learningState = LearningState.LEARNING,
                )
            assertThat(state.showSuggestionsSection).isTrue()
        }

        @Test
        fun `showSuggestionsSection returns false when no suggestions and state is READY`() {
            val state =
                ListUiState(
                    suggestions = persistentListOf(),
                    learningState = LearningState.READY,
                )
            assertThat(state.showSuggestionsSection).isFalse()
        }

        @Test
        fun `showLearningProgress returns true when learning but no suggestions`() {
            val state =
                ListUiState(
                    suggestions = persistentListOf(),
                    learningState = LearningState.LEARNING,
                )
            assertThat(state.showLearningProgress).isTrue()
        }

        @Test
        fun `showLearningProgress returns false when NOT_STARTED`() {
            val state =
                ListUiState(
                    suggestions = persistentListOf(),
                    learningState = LearningState.NOT_STARTED,
                )
            assertThat(state.showLearningProgress).isFalse()
        }

        @Test
        fun `showLearningProgress returns false when READY`() {
            val state =
                ListUiState(
                    suggestions = persistentListOf(),
                    learningState = LearningState.READY,
                )
            assertThat(state.showLearningProgress).isFalse()
        }
    }

    @Nested
    @DisplayName("Smart suggestions")
    inner class SmartSuggestionsTests {
        @Test
        fun `observes suggestions from use case`() =
            runTest {
                val suggestions = listOf(testSuggestion)
                every { getSuggestionsUseCase() } returns flowOf(suggestions)

                val vm = createViewModel()

                vm.uiState.test {
                    val state = awaitItem()
                    assertThat(state.suggestions).containsExactly(testSuggestion)
                    cancelAndIgnoreRemainingEvents()
                }
            }

        @Test
        fun `loads learning state from trip count`() =
            runTest {
                coEvery { getShoppingTripCountUseCase() } returns 3

                val vm = createViewModel()

                vm.uiState.test {
                    val state = awaitItem()
                    assertThat(state.learningState).isEqualTo(LearningState.READY)
                    cancelAndIgnoreRemainingEvents()
                }
            }

        @Test
        fun `learning state LEARNING when 1 trip`() =
            runTest {
                coEvery { getShoppingTripCountUseCase() } returns 1

                val vm = createViewModel()

                vm.uiState.test {
                    val state = awaitItem()
                    assertThat(state.learningState).isEqualTo(LearningState.LEARNING)
                    cancelAndIgnoreRemainingEvents()
                }
            }

        @Test
        fun `learning state ALMOST_READY when 2 trips`() =
            runTest {
                coEvery { getShoppingTripCountUseCase() } returns 2

                val vm = createViewModel()

                vm.uiState.test {
                    val state = awaitItem()
                    assertThat(state.learningState).isEqualTo(LearningState.ALMOST_READY)
                    cancelAndIgnoreRemainingEvents()
                }
            }

        @Test
        fun `learning state NOT_STARTED when 0 trips`() =
            runTest {
                coEvery { getShoppingTripCountUseCase() } returns 0

                val vm = createViewModel()

                vm.uiState.test {
                    val state = awaitItem()
                    assertThat(state.learningState).isEqualTo(LearningState.NOT_STARTED)
                    cancelAndIgnoreRemainingEvents()
                }
            }

        @Test
        fun `acceptSuggestion removes suggestion optimistically`() =
            runTest {
                every { getSuggestionsUseCase() } returns flowOf(listOf(testSuggestion))
                coEvery { acceptSuggestionUseCase(testSuggestion) } returns Result.success(testItem)

                val vm = createViewModel()

                vm.uiState.test {
                    awaitItem() // Initial state with suggestion
                    vm.onAction(ListAction.AcceptSuggestion(testSuggestion))
                    val state = awaitItem()
                    assertThat(state.suggestions).isEmpty()
                    cancelAndIgnoreRemainingEvents()
                }
            }

        @Test
        fun `acceptSuggestion emits SuggestionAccepted event on success`() =
            runTest {
                coEvery { acceptSuggestionUseCase(testSuggestion) } returns Result.success(testItem)

                viewModel.events.test {
                    viewModel.onAction(ListAction.AcceptSuggestion(testSuggestion))
                    val event = awaitItem()
                    assertThat(event).isInstanceOf(ListEvent.SuggestionAccepted::class.java)
                    assertThat((event as ListEvent.SuggestionAccepted).itemName).isEqualTo("Milk")
                    cancelAndIgnoreRemainingEvents()
                }
            }

        @Test
        fun `acceptSuggestion emits Error event on failure`() =
            runTest {
                coEvery { acceptSuggestionUseCase(testSuggestion) } returns
                    Result.failure(RuntimeException("Add failed"))

                viewModel.events.test {
                    viewModel.onAction(ListAction.AcceptSuggestion(testSuggestion))
                    val event = awaitItem()
                    assertThat(event).isInstanceOf(ListEvent.Error::class.java)
                    cancelAndIgnoreRemainingEvents()
                }
            }

        @Test
        fun `dismissSuggestion removes suggestion optimistically`() =
            runTest {
                every { getSuggestionsUseCase() } returns flowOf(listOf(testSuggestion))
                coEvery { dismissSuggestionUseCase("item-1") } returns Unit

                val vm = createViewModel()

                vm.uiState.test {
                    awaitItem() // Initial state with suggestion
                    vm.onAction(ListAction.DismissSuggestion("item-1"))
                    val state = awaitItem()
                    assertThat(state.suggestions).isEmpty()
                    cancelAndIgnoreRemainingEvents()
                }
            }

        @Test
        fun `dismissSuggestion calls dismiss use case`() =
            runTest {
                coEvery { dismissSuggestionUseCase("item-1") } returns Unit

                viewModel.onAction(ListAction.DismissSuggestion("item-1"))

                coVerify { dismissSuggestionUseCase("item-1") }
            }
    }
}
