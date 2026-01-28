package com.restock.feature.suggestions.ui

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.restock.core.domain.model.CurrentListItem
import com.restock.core.domain.model.Suggestion
import com.restock.core.domain.usecase.AcceptSuggestionUseCase
import com.restock.core.domain.usecase.DismissSuggestionUseCase
import com.restock.core.domain.usecase.GetShoppingTripCountUseCase
import com.restock.core.domain.usecase.GetSuggestionsUseCase
import com.restock.feature.suggestions.MainDispatcherRule
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
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest

/**
 * Unit tests for [SuggestionsViewModel].
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SuggestionsViewModelTest {
    @JvmField
    @RegisterExtension
    val mainDispatcherRule = MainDispatcherRule()

    private val getSuggestionsUseCase: GetSuggestionsUseCase = mockk()
    private val acceptSuggestionUseCase: AcceptSuggestionUseCase = mockk()
    private val dismissSuggestionUseCase: DismissSuggestionUseCase = mockk()
    private val getShoppingTripCountUseCase: GetShoppingTripCountUseCase = mockk()

    private lateinit var viewModel: SuggestionsViewModel

    private val testSuggestion =
        Suggestion(
            itemId = "item-1",
            itemName = "Milk",
            reason = "Usually every 7 days",
            score = 1.5,
            confidence = 0.8,
            daysSinceLastPurchase = 8,
            avgIntervalDays = 7,
        )

    private val testListItem =
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

    @BeforeEach
    fun setup() {
        // Default behavior for use cases
        every { getSuggestionsUseCase() } returns flowOf(emptyList())
        coEvery { getShoppingTripCountUseCase() } returns 0
    }

    private fun createViewModel(): SuggestionsViewModel =
        SuggestionsViewModel(
            getSuggestionsUseCase = getSuggestionsUseCase,
            acceptSuggestionUseCase = acceptSuggestionUseCase,
            dismissSuggestionUseCase = dismissSuggestionUseCase,
            getShoppingTripCountUseCase = getShoppingTripCountUseCase,
        )

    @Nested
    @DisplayName("Initial state")
    inner class InitialStateTests {
        @Test
        fun `initial state has empty suggestions and loading true`() {
            viewModel = createViewModel()
            val state = viewModel.uiState.value

            assertThat(state.suggestions).isEmpty()
            assertThat(state.isLoading).isTrue()
            assertThat(state.error).isNull()
        }

        @Test
        fun `initial state has NOT_STARTED learning state`() {
            viewModel = createViewModel()
            val state = viewModel.uiState.value

            assertThat(state.learningState).isEqualTo(LearningState.NOT_STARTED)
        }
    }

    @Nested
    @DisplayName("Observing suggestions")
    inner class ObserveSuggestionsTests {
        @Test
        fun `loads suggestions from use case`() =
            runTest {
                val suggestions = listOf(testSuggestion)
                every { getSuggestionsUseCase() } returns flowOf(suggestions)

                viewModel = createViewModel()

                viewModel.uiState.test {
                    val state = awaitItem()
                    assertThat(state.suggestions).hasSize(1)
                    assertThat(state.suggestions.first()).isEqualTo(testSuggestion)
                    assertThat(state.isLoading).isFalse()
                    cancelAndIgnoreRemainingEvents()
                }
            }

        @Test
        fun `updates when suggestions flow emits`() =
            runTest {
                val suggestions1 = listOf(testSuggestion)
                val suggestions2 =
                    listOf(
                        testSuggestion,
                        testSuggestion.copy(itemId = "item-2", itemName = "Eggs"),
                    )
                every { getSuggestionsUseCase() } returns flowOf(suggestions1, suggestions2)

                viewModel = createViewModel()

                viewModel.uiState.test {
                    val state1 = awaitItem()
                    assertThat(state1.suggestions).hasSize(1)

                    val state2 = awaitItem()
                    assertThat(state2.suggestions).hasSize(2)
                    cancelAndIgnoreRemainingEvents()
                }
            }

        @Test
        fun `handles error from suggestions flow`() =
            runTest {
                val errorFlow = flow<List<Suggestion>> { throw RuntimeException("DB error") }
                every { getSuggestionsUseCase() } returns errorFlow

                viewModel = createViewModel()

                viewModel.events.test {
                    val event = awaitItem()
                    assertThat(event).isInstanceOf(SuggestionsEvent.Error::class.java)
                    cancelAndIgnoreRemainingEvents()
                }
            }
    }

    @Nested
    @DisplayName("Learning state")
    inner class LearningStateTests {
        @Test
        fun `sets NOT_STARTED when 0 trips`() =
            runTest {
                coEvery { getShoppingTripCountUseCase() } returns 0

                viewModel = createViewModel()

                viewModel.uiState.test {
                    val state = awaitItem()
                    assertThat(state.learningState).isEqualTo(LearningState.NOT_STARTED)
                    cancelAndIgnoreRemainingEvents()
                }
            }

        @Test
        fun `sets LEARNING when 1 trip`() =
            runTest {
                coEvery { getShoppingTripCountUseCase() } returns 1

                viewModel = createViewModel()

                viewModel.uiState.test {
                    awaitItem() // Initial
                    val state = awaitItem()
                    assertThat(state.learningState).isEqualTo(LearningState.LEARNING)
                    cancelAndIgnoreRemainingEvents()
                }
            }

        @Test
        fun `sets ALMOST_READY when 2 trips`() =
            runTest {
                coEvery { getShoppingTripCountUseCase() } returns 2

                viewModel = createViewModel()

                viewModel.uiState.test {
                    awaitItem() // Initial
                    val state = awaitItem()
                    assertThat(state.learningState).isEqualTo(LearningState.ALMOST_READY)
                    cancelAndIgnoreRemainingEvents()
                }
            }

        @Test
        fun `sets READY when 3 trips`() =
            runTest {
                coEvery { getShoppingTripCountUseCase() } returns 3

                viewModel = createViewModel()

                viewModel.uiState.test {
                    awaitItem() // Initial
                    val state = awaitItem()
                    assertThat(state.learningState).isEqualTo(LearningState.READY)
                    cancelAndIgnoreRemainingEvents()
                }
            }

        @Test
        fun `sets READY when more than 3 trips`() =
            runTest {
                coEvery { getShoppingTripCountUseCase() } returns 10

                viewModel = createViewModel()

                viewModel.uiState.test {
                    awaitItem() // Initial
                    val state = awaitItem()
                    assertThat(state.learningState).isEqualTo(LearningState.READY)
                    cancelAndIgnoreRemainingEvents()
                }
            }

        @Test
        fun `refreshLearningState reloads trip count`() =
            runTest {
                coEvery { getShoppingTripCountUseCase() } returns 1

                viewModel = createViewModel()

                viewModel.uiState.test {
                    awaitItem() // Initial
                    awaitItem() // After first load (LEARNING)

                    coEvery { getShoppingTripCountUseCase() } returns 3
                    viewModel.onAction(SuggestionsAction.RefreshLearningState)

                    val state = awaitItem()
                    assertThat(state.learningState).isEqualTo(LearningState.READY)
                    cancelAndIgnoreRemainingEvents()
                }
            }
    }

    @Nested
    @DisplayName("Accepting suggestions")
    inner class AcceptSuggestionTests {
        @Test
        fun `optimistically removes suggestion from UI`() =
            runTest {
                val suggestions =
                    listOf(
                        testSuggestion,
                        testSuggestion.copy(itemId = "item-2", itemName = "Eggs"),
                    )
                every { getSuggestionsUseCase() } returns flowOf(suggestions)
                coEvery { acceptSuggestionUseCase(any()) } returns Result.success(testListItem)

                viewModel = createViewModel()

                viewModel.uiState.test {
                    awaitItem() // Initial with suggestions

                    viewModel.onAction(SuggestionsAction.AcceptSuggestion(testSuggestion))
                    val state = awaitItem()

                    assertThat(state.suggestions.map { it.itemId }).doesNotContain("item-1")
                    assertThat(state.suggestions).hasSize(1)
                    cancelAndIgnoreRemainingEvents()
                }
            }

        @Test
        fun `emits SuggestionAccepted event on success`() =
            runTest {
                every { getSuggestionsUseCase() } returns flowOf(listOf(testSuggestion))
                coEvery { acceptSuggestionUseCase(any()) } returns Result.success(testListItem)

                viewModel = createViewModel()

                viewModel.events.test {
                    viewModel.onAction(SuggestionsAction.AcceptSuggestion(testSuggestion))
                    val event = awaitItem()
                    assertThat(event).isInstanceOf(SuggestionsEvent.SuggestionAccepted::class.java)
                    assertThat((event as SuggestionsEvent.SuggestionAccepted).itemName).isEqualTo("Milk")
                    cancelAndIgnoreRemainingEvents()
                }
            }

        @Test
        fun `calls acceptSuggestionUseCase with suggestion`() =
            runTest {
                every { getSuggestionsUseCase() } returns flowOf(listOf(testSuggestion))
                coEvery { acceptSuggestionUseCase(any()) } returns Result.success(testListItem)

                viewModel = createViewModel()
                viewModel.onAction(SuggestionsAction.AcceptSuggestion(testSuggestion))

                coVerify { acceptSuggestionUseCase(testSuggestion) }
            }

        @Test
        fun `restores suggestions on failure`() =
            runTest {
                val suggestions = listOf(testSuggestion)
                every { getSuggestionsUseCase() } returns flowOf(suggestions)
                coEvery { acceptSuggestionUseCase(any()) } returns Result.failure(RuntimeException("Add failed"))

                viewModel = createViewModel()

                viewModel.uiState.test {
                    awaitItem() // Initial with suggestions

                    viewModel.onAction(SuggestionsAction.AcceptSuggestion(testSuggestion))

                    // First update: optimistic removal
                    val optimisticState = awaitItem()
                    assertThat(optimisticState.suggestions).isEmpty()

                    // Second update: restoration after error
                    val restoredState = awaitItem()
                    assertThat(restoredState.suggestions).hasSize(1)
                    assertThat(restoredState.suggestions.first().itemId).isEqualTo("item-1")
                    cancelAndIgnoreRemainingEvents()
                }
            }

        @Test
        fun `emits Error event on failure`() =
            runTest {
                every { getSuggestionsUseCase() } returns flowOf(listOf(testSuggestion))
                coEvery { acceptSuggestionUseCase(any()) } returns Result.failure(RuntimeException("Add failed"))

                viewModel = createViewModel()

                viewModel.events.test {
                    viewModel.onAction(SuggestionsAction.AcceptSuggestion(testSuggestion))
                    val event = awaitItem()
                    assertThat(event).isInstanceOf(SuggestionsEvent.Error::class.java)
                    cancelAndIgnoreRemainingEvents()
                }
            }
    }

    @Nested
    @DisplayName("Dismissing suggestions")
    inner class DismissSuggestionTests {
        @Test
        fun `optimistically removes suggestion from UI`() =
            runTest {
                val suggestions =
                    listOf(
                        testSuggestion,
                        testSuggestion.copy(itemId = "item-2", itemName = "Eggs"),
                    )
                every { getSuggestionsUseCase() } returns flowOf(suggestions)
                coEvery { dismissSuggestionUseCase(any()) } returns Unit

                viewModel = createViewModel()

                viewModel.uiState.test {
                    awaitItem() // Initial with suggestions

                    viewModel.onAction(SuggestionsAction.DismissSuggestion("item-1"))
                    val state = awaitItem()

                    assertThat(state.suggestions.map { it.itemId }).doesNotContain("item-1")
                    assertThat(state.suggestions).hasSize(1)
                    cancelAndIgnoreRemainingEvents()
                }
            }

        @Test
        fun `calls dismissSuggestionUseCase with itemId`() =
            runTest {
                every { getSuggestionsUseCase() } returns flowOf(listOf(testSuggestion))
                coEvery { dismissSuggestionUseCase(any()) } returns Unit

                viewModel = createViewModel()
                viewModel.onAction(SuggestionsAction.DismissSuggestion("item-1"))

                coVerify { dismissSuggestionUseCase("item-1") }
            }

        @Test
        fun `emits SuggestionDismissed event`() =
            runTest {
                every { getSuggestionsUseCase() } returns flowOf(listOf(testSuggestion))
                coEvery { dismissSuggestionUseCase(any()) } returns Unit

                viewModel = createViewModel()

                viewModel.events.test {
                    viewModel.onAction(SuggestionsAction.DismissSuggestion("item-1"))
                    val event = awaitItem()
                    assertThat(event).isInstanceOf(SuggestionsEvent.SuggestionDismissed::class.java)
                    assertThat((event as SuggestionsEvent.SuggestionDismissed).itemName).isEqualTo("Milk")
                    cancelAndIgnoreRemainingEvents()
                }
            }

        @Test
        fun `does not restore on dismiss even if use case throws`() =
            runTest {
                // Dismiss is fire-and-forget - we don't restore on failure
                every { getSuggestionsUseCase() } returns flowOf(listOf(testSuggestion))
                coEvery { dismissSuggestionUseCase(any()) } throws RuntimeException("DB error")

                viewModel = createViewModel()

                viewModel.uiState.test {
                    awaitItem() // Initial with suggestions

                    viewModel.onAction(SuggestionsAction.DismissSuggestion("item-1"))

                    // Item should be removed and stay removed
                    val state = awaitItem()
                    assertThat(state.suggestions).isEmpty()
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
                val errorFlow = flow<List<Suggestion>> { throw RuntimeException("DB error") }
                every { getSuggestionsUseCase() } returns errorFlow

                viewModel = createViewModel()

                viewModel.uiState.test {
                    val stateWithError = awaitItem()
                    assertThat(stateWithError.error).isNotNull()

                    viewModel.onAction(SuggestionsAction.DismissError)
                    val stateAfterDismiss = awaitItem()
                    assertThat(stateAfterDismiss.error).isNull()
                    cancelAndIgnoreRemainingEvents()
                }
            }

        @Test
        fun `retry reloads suggestions`() =
            runTest {
                var callCount = 0
                val errorThenSuccessFlow =
                    flow {
                        callCount++
                        if (callCount == 1) {
                            throw RuntimeException("DB error")
                        } else {
                            emit(listOf(testSuggestion))
                        }
                    }
                every { getSuggestionsUseCase() } returns errorThenSuccessFlow

                viewModel = createViewModel()

                viewModel.uiState.test {
                    awaitItem() // Error state

                    viewModel.onAction(SuggestionsAction.Retry)
                    val loadingState = awaitItem()
                    assertThat(loadingState.isLoading).isTrue()

                    val successState = awaitItem()
                    assertThat(successState.suggestions).hasSize(1)
                    cancelAndIgnoreRemainingEvents()
                }
            }
    }

    @Nested
    @DisplayName("SuggestionsUiState computed properties")
    inner class UiStateComputedPropertiesTests {
        @Test
        fun `hasSuggestions returns true when suggestions exist`() {
            val state =
                SuggestionsUiState(
                    suggestions = persistentListOf(testSuggestion),
                )
            assertThat(state.hasSuggestions).isTrue()
        }

        @Test
        fun `hasSuggestions returns false when empty`() {
            val state = SuggestionsUiState()
            assertThat(state.hasSuggestions).isFalse()
        }

        @Test
        fun `suggestionCount returns correct count`() {
            val state =
                SuggestionsUiState(
                    suggestions =
                        persistentListOf(
                            testSuggestion,
                            testSuggestion.copy(itemId = "item-2"),
                        ),
                )
            assertThat(state.suggestionCount).isEqualTo(2)
        }

        @Test
        fun `showSuggestionsSection returns true when suggestions exist`() {
            val state =
                SuggestionsUiState(
                    suggestions = persistentListOf(testSuggestion),
                    learningState = LearningState.READY,
                )
            assertThat(state.showSuggestionsSection).isTrue()
        }

        @Test
        fun `showSuggestionsSection returns true when learning in progress`() {
            val state =
                SuggestionsUiState(
                    suggestions = persistentListOf(),
                    learningState = LearningState.LEARNING,
                )
            assertThat(state.showSuggestionsSection).isTrue()
        }

        @Test
        fun `showSuggestionsSection returns false when READY and no suggestions`() {
            val state =
                SuggestionsUiState(
                    suggestions = persistentListOf(),
                    learningState = LearningState.READY,
                )
            assertThat(state.showSuggestionsSection).isFalse()
        }

        @Test
        fun `showLearningProgress returns true when learning and no suggestions`() {
            val state =
                SuggestionsUiState(
                    suggestions = persistentListOf(),
                    learningState = LearningState.LEARNING,
                )
            assertThat(state.showLearningProgress).isTrue()
        }

        @Test
        fun `showLearningProgress returns true when almost ready and no suggestions`() {
            val state =
                SuggestionsUiState(
                    suggestions = persistentListOf(),
                    learningState = LearningState.ALMOST_READY,
                )
            assertThat(state.showLearningProgress).isTrue()
        }

        @Test
        fun `showLearningProgress returns false when suggestions exist`() {
            val state =
                SuggestionsUiState(
                    suggestions = persistentListOf(testSuggestion),
                    learningState = LearningState.LEARNING,
                )
            assertThat(state.showLearningProgress).isFalse()
        }

        @Test
        fun `showLearningProgress returns false when NOT_STARTED`() {
            val state =
                SuggestionsUiState(
                    suggestions = persistentListOf(),
                    learningState = LearningState.NOT_STARTED,
                )
            assertThat(state.showLearningProgress).isFalse()
        }

        @Test
        fun `showLearningProgress returns false when READY`() {
            val state =
                SuggestionsUiState(
                    suggestions = persistentListOf(),
                    learningState = LearningState.READY,
                )
            assertThat(state.showLearningProgress).isFalse()
        }
    }
}
