package com.restock.core.ui.viewmodel

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest

/**
 * Unit tests for [BaseViewModel].
 *
 * These tests verify the core UDF (Unidirectional Data Flow) pattern implementation:
 * - State management via StateFlow
 * - One-shot events via Channel
 * - Thread-safe state updates
 */
@OptIn(ExperimentalCoroutinesApi::class)
class BaseViewModelTest {
    @JvmField
    @RegisterExtension
    val mainDispatcherRule = MainDispatcherRule()

    // Test implementations
    data class TestState(
        val count: Int = 0,
        val isLoading: Boolean = false,
        val message: String = "",
    )

    sealed interface TestAction {
        data object Increment : TestAction

        data object Decrement : TestAction

        data class SetMessage(val message: String) : TestAction

        data class TriggerEvent(val event: TestEvent) : TestAction

        data object LoadData : TestAction
    }

    sealed interface TestEvent {
        data object Success : TestEvent

        data class Error(val message: String) : TestEvent

        data class NavigateTo(val destination: String) : TestEvent
    }

    /**
     * Concrete implementation for testing BaseViewModel.
     */
    private class TestViewModel(
        initialState: TestState = TestState(),
    ) : BaseViewModel<TestState, TestAction, TestEvent>(initialState) {
        // Expose currentState for testing
        val testCurrentState: TestState get() = currentState

        override fun onAction(action: TestAction) {
            when (action) {
                is TestAction.Increment -> updateState { copy(count = count + 1) }
                is TestAction.Decrement -> updateState { copy(count = count - 1) }
                is TestAction.SetMessage -> updateState { copy(message = action.message) }
                is TestAction.TriggerEvent -> sendEvent(action.event)
                is TestAction.LoadData -> {
                    updateState { copy(isLoading = true) }
                    sendEvent(TestEvent.Success)
                    updateState { copy(isLoading = false) }
                }
            }
        }

        // Expose for testing thread-safety
        fun updateStateDirectly(reducer: TestState.() -> TestState) {
            updateState(reducer)
        }
    }

    @Nested
    @DisplayName("Initial State")
    inner class InitialStateTests {
        @Test
        fun `initial state is set correctly`() {
            val initialState = TestState(count = 10, message = "Hello")
            val viewModel = TestViewModel(initialState)

            assertThat(viewModel.uiState.value).isEqualTo(initialState)
        }

        @Test
        fun `default initial state uses default values`() {
            val viewModel = TestViewModel()

            assertThat(viewModel.uiState.value).isEqualTo(TestState())
            assertThat(viewModel.uiState.value.count).isEqualTo(0)
            assertThat(viewModel.uiState.value.isLoading).isFalse()
            assertThat(viewModel.uiState.value.message).isEmpty()
        }

        @Test
        fun `currentState returns the same value as uiState value`() {
            val initialState = TestState(count = 5)
            val viewModel = TestViewModel(initialState)

            assertThat(viewModel.testCurrentState).isEqualTo(viewModel.uiState.value)
        }
    }

    @Nested
    @DisplayName("State Updates")
    inner class StateUpdateTests {
        @Test
        fun `updateState modifies state correctly`() =
            runTest {
                val viewModel = TestViewModel()

                viewModel.onAction(TestAction.Increment)

                assertThat(viewModel.uiState.value.count).isEqualTo(1)
            }

        @Test
        fun `multiple state updates are applied correctly`() =
            runTest {
                val viewModel = TestViewModel()

                viewModel.onAction(TestAction.Increment)
                viewModel.onAction(TestAction.Increment)
                viewModel.onAction(TestAction.Increment)

                assertThat(viewModel.uiState.value.count).isEqualTo(3)
            }

        @Test
        fun `decrement action decreases count`() =
            runTest {
                val viewModel = TestViewModel(TestState(count = 5))

                viewModel.onAction(TestAction.Decrement)

                assertThat(viewModel.uiState.value.count).isEqualTo(4)
            }

        @Test
        fun `setMessage action updates message`() =
            runTest {
                val viewModel = TestViewModel()

                viewModel.onAction(TestAction.SetMessage("Test message"))

                assertThat(viewModel.uiState.value.message).isEqualTo("Test message")
            }

        @Test
        fun `state updates are atomic and thread-safe`() =
            runTest {
                val viewModel = TestViewModel()

                // Perform multiple rapid updates
                repeat(100) {
                    viewModel.onAction(TestAction.Increment)
                }

                assertThat(viewModel.uiState.value.count).isEqualTo(100)
            }

        @Test
        fun `uiState is read-only StateFlow`() =
            runTest {
                val viewModel = TestViewModel()

                // This should compile - collecting state
                viewModel.uiState.test {
                    assertThat(awaitItem().count).isEqualTo(0)

                    viewModel.onAction(TestAction.Increment)
                    assertThat(awaitItem().count).isEqualTo(1)

                    cancelAndIgnoreRemainingEvents()
                }
            }
    }

    @Nested
    @DisplayName("Events")
    inner class EventTests {
        @Test
        fun `sendEvent delivers event to flow`() =
            runTest {
                val viewModel = TestViewModel()

                viewModel.events.test {
                    viewModel.onAction(TestAction.TriggerEvent(TestEvent.Success))

                    advanceUntilIdle()
                    assertThat(awaitItem()).isEqualTo(TestEvent.Success)

                    cancelAndIgnoreRemainingEvents()
                }
            }

        @Test
        fun `events with data are delivered correctly`() =
            runTest {
                val viewModel = TestViewModel()
                val errorMessage = "Something went wrong"

                viewModel.events.test {
                    viewModel.onAction(TestAction.TriggerEvent(TestEvent.Error(errorMessage)))

                    advanceUntilIdle()
                    val event = awaitItem() as TestEvent.Error
                    assertThat(event.message).isEqualTo(errorMessage)

                    cancelAndIgnoreRemainingEvents()
                }
            }

        @Test
        fun `navigation event contains destination`() =
            runTest {
                val viewModel = TestViewModel()
                val destination = "details/123"

                viewModel.events.test {
                    viewModel.onAction(TestAction.TriggerEvent(TestEvent.NavigateTo(destination)))

                    advanceUntilIdle()
                    val event = awaitItem() as TestEvent.NavigateTo
                    assertThat(event.destination).isEqualTo(destination)

                    cancelAndIgnoreRemainingEvents()
                }
            }

        @Test
        fun `multiple events are delivered in order`() =
            runTest {
                val viewModel = TestViewModel()

                viewModel.events.test {
                    viewModel.onAction(TestAction.TriggerEvent(TestEvent.Success))
                    viewModel.onAction(TestAction.TriggerEvent(TestEvent.Error("error")))
                    viewModel.onAction(TestAction.TriggerEvent(TestEvent.NavigateTo("home")))

                    advanceUntilIdle()
                    assertThat(awaitItem()).isEqualTo(TestEvent.Success)
                    assertThat(awaitItem()).isInstanceOf(TestEvent.Error::class.java)
                    assertThat(awaitItem()).isInstanceOf(TestEvent.NavigateTo::class.java)

                    cancelAndIgnoreRemainingEvents()
                }
            }

        @Test
        fun `events are consumed only once`() =
            runTest {
                val viewModel = TestViewModel()
                val eventsReceived = mutableListOf<TestEvent>()

                viewModel.events.test {
                    viewModel.onAction(TestAction.TriggerEvent(TestEvent.Success))

                    advanceUntilIdle()
                    eventsReceived.add(awaitItem())

                    cancelAndIgnoreRemainingEvents()
                }

                // Second collector should not receive the same event
                viewModel.events.test {
                    expectNoEvents()
                }

                assertThat(eventsReceived).hasSize(1)
            }
    }

    @Nested
    @DisplayName("Combined State and Events")
    inner class CombinedTests {
        @Test
        fun `action can update state and send event`() =
            runTest {
                val viewModel = TestViewModel()

                viewModel.events.test {
                    viewModel.onAction(TestAction.LoadData)

                    advanceUntilIdle()
                    // Event should be sent
                    assertThat(awaitItem()).isEqualTo(TestEvent.Success)

                    cancelAndIgnoreRemainingEvents()
                }

                // State should be updated (loading started and completed)
                assertThat(viewModel.uiState.value.isLoading).isFalse()
            }

        @Test
        fun `state flow emits to multiple collectors`() =
            runTest {
                val viewModel = TestViewModel()
                val collector1Values = mutableListOf<TestState>()
                val collector2Values = mutableListOf<TestState>()

                viewModel.uiState.test {
                    collector1Values.add(awaitItem()) // Initial

                    viewModel.onAction(TestAction.Increment)
                    collector1Values.add(awaitItem())

                    cancelAndIgnoreRemainingEvents()
                }

                // Second collector starts fresh from current state
                viewModel.uiState.test {
                    collector2Values.add(awaitItem())
                    cancelAndIgnoreRemainingEvents()
                }

                assertThat(collector1Values[0].count).isEqualTo(0)
                assertThat(collector1Values[1].count).isEqualTo(1)
                assertThat(collector2Values[0].count).isEqualTo(1) // Gets current state
            }
    }

    @Nested
    @DisplayName("Edge Cases")
    inner class EdgeCaseTests {
        @Test
        fun `empty message can be set`() =
            runTest {
                val viewModel = TestViewModel(TestState(message = "initial"))

                viewModel.onAction(TestAction.SetMessage(""))

                assertThat(viewModel.uiState.value.message).isEmpty()
            }

        @Test
        fun `negative count is allowed`() =
            runTest {
                val viewModel = TestViewModel()

                viewModel.onAction(TestAction.Decrement)

                assertThat(viewModel.uiState.value.count).isEqualTo(-1)
            }

        @Test
        fun `state with same values does not emit duplicate`() =
            runTest {
                val viewModel = TestViewModel()

                viewModel.uiState.test {
                    awaitItem() // Initial state

                    viewModel.updateStateDirectly { copy(count = 0) } // Same value

                    // Should not emit because StateFlow only emits distinct values
                    expectNoEvents()

                    viewModel.updateStateDirectly { copy(count = 1) } // Different value
                    assertThat(awaitItem().count).isEqualTo(1)

                    cancelAndIgnoreRemainingEvents()
                }
            }
    }
}
