# Task: Implement Accept & Dismiss Logic

**Sprint:** 7-8 (Smart Suggestions)
**Owner:** Tech Lead / Senior Developer
**Estimate:** 17 hours (T-8.1 through T-8.7 combined)
**Priority:** P0
**Prerequisites:** 001_suggestions_data_layer.md

---

## Sprint Context

Accept and dismiss actions are critical for the suggestions feature. Accept adds the item to the list, dismiss reduces confidence and can eventually stop suggesting the item.

### Sprint Theme
> *"The moment when the app anticipates your needs."*

### Key Behaviors
- **Accept:** One tap adds item to list
- **Dismiss:** Reduces confidence, 3 dismissals = stop suggesting
- **Purchase resets:** Buying a dismissed item resets dismissal state

---

## Task Description

Implement use cases for accepting and dismissing suggestions with proper state management and analytics preparation.

## Implementation

### AcceptSuggestionUseCase.kt

```kotlin
package com.restock.feature.suggestions.domain

import com.restock.core.domain.model.CurrentListItem
import com.restock.core.domain.model.Suggestion
import com.restock.core.domain.repository.SuggestionRepository
import com.restock.core.domain.usecase.AddItemUseCase
import javax.inject.Inject

class AcceptSuggestionUseCase @Inject constructor(
    private val addItemUseCase: AddItemUseCase,
    private val suggestionRepository: SuggestionRepository,
) {
    /**
     * Accepts a suggestion by adding the item to the current list.
     *
     * @param suggestion The suggestion to accept
     * @return Result containing the added item, or failure
     */
    suspend operator fun invoke(suggestion: Suggestion): Result<CurrentListItem> {
        // Add item to list
        val result = addItemUseCase(suggestion.itemName)

        // Record acceptance for analytics/learning
        if (result.isSuccess) {
            suggestionRepository.acceptSuggestion(suggestion.itemId)
        }

        return result
    }
}
```

### DismissSuggestionUseCase.kt

```kotlin
package com.restock.feature.suggestions.domain

import com.restock.core.domain.repository.SuggestionRepository
import javax.inject.Inject

class DismissSuggestionUseCase @Inject constructor(
    private val suggestionRepository: SuggestionRepository,
) {
    /**
     * Dismisses a suggestion.
     *
     * Effects:
     * - First dismiss: reduces confidence by 0.05
     * - Second dismiss: reduces confidence by 0.05
     * - Third dismiss: marks as persistently dismissed (won't suggest again)
     *
     * @param itemId The ID of the item to dismiss
     */
    suspend operator fun invoke(itemId: String) {
        suggestionRepository.dismissSuggestion(itemId)
    }
}
```

### GetSuggestionsUseCase.kt

```kotlin
package com.restock.feature.suggestions.domain

import com.restock.core.domain.model.Suggestion
import com.restock.core.domain.repository.SuggestionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetSuggestionsUseCase @Inject constructor(
    private val suggestionRepository: SuggestionRepository,
) {
    /**
     * Observes suggestions as a Flow.
     * Updates automatically when patterns or list changes.
     */
    operator fun invoke(): Flow<List<Suggestion>> =
        suggestionRepository.observeSuggestions()

    /**
     * Gets suggestions once (non-reactive).
     */
    suspend fun getOnce(limit: Int = 5): List<Suggestion> =
        suggestionRepository.getSuggestions(limit)
}
```

### GetShoppingTripCountUseCase.kt

```kotlin
package com.restock.feature.suggestions.domain

import com.restock.core.domain.repository.SuggestionRepository
import javax.inject.Inject

class GetShoppingTripCountUseCase @Inject constructor(
    private val suggestionRepository: SuggestionRepository,
) {
    /**
     * Returns the number of shopping trips (distinct days with purchases).
     * Used to determine learning state.
     */
    suspend operator fun invoke(): Int =
        suggestionRepository.getShoppingTripCount()
}
```

### Integration with Purchase Recording

Update RecordPurchaseUseCase to clear dismissals:

```kotlin
package com.restock.core.domain.usecase

import com.restock.core.domain.model.PurchaseEvent
import com.restock.core.domain.repository.PurchaseRepository
import com.restock.core.domain.repository.SuggestionRepository
import com.restock.core.ml.worker.PatternWorkScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import timber.log.Timber
import javax.inject.Inject

class RecordPurchaseUseCase @Inject constructor(
    private val purchaseRepository: PurchaseRepository,
    private val suggestionRepository: SuggestionRepository,
    private val patternWorkScheduler: PatternWorkScheduler,
    @ApplicationScope private val scope: CoroutineScope,
) {
    operator fun invoke(itemId: String, quantity: Int) {
        scope.launch {
            val event = PurchaseEvent(
                itemId = itemId,
                quantity = quantity,
                purchasedAt = Clock.System.now(),
            )

            purchaseRepository.recordPurchase(event)
                .onSuccess {
                    // Clear any dismissal state for this item
                    suggestionRepository.clearDismissalOnPurchase(itemId)

                    // Trigger pattern recalculation
                    patternWorkScheduler.triggerImmediateCalculation()
                }
                .onFailure { e ->
                    Timber.e(e, "Failed to record purchase")
                }
        }
    }
}
```

### SuggestionsViewModel.kt

```kotlin
package com.restock.feature.suggestions.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.restock.core.domain.model.Suggestion
import com.restock.feature.suggestions.domain.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SuggestionsViewModel @Inject constructor(
    private val getSuggestionsUseCase: GetSuggestionsUseCase,
    private val acceptSuggestionUseCase: AcceptSuggestionUseCase,
    private val dismissSuggestionUseCase: DismissSuggestionUseCase,
    private val getShoppingTripCountUseCase: GetShoppingTripCountUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SuggestionsUiState())
    val uiState: StateFlow<SuggestionsUiState> = _uiState.asStateFlow()

    private val _events = Channel<SuggestionsEvent>(Channel.BUFFERED)
    val events: Flow<SuggestionsEvent> = _events.receiveAsFlow()

    init {
        observeSuggestions()
        loadLearningState()
    }

    private fun observeSuggestions() {
        viewModelScope.launch {
            getSuggestionsUseCase()
                .catch { e ->
                    _events.send(SuggestionsEvent.Error(e.message ?: "Failed to load suggestions"))
                }
                .collect { suggestions ->
                    _uiState.update { it.copy(suggestions = suggestions) }
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

    fun onAction(action: SuggestionsAction) {
        when (action) {
            is SuggestionsAction.AcceptSuggestion -> acceptSuggestion(action.suggestion)
            is SuggestionsAction.DismissSuggestion -> dismissSuggestion(action.itemId)
            is SuggestionsAction.RefreshLearningState -> loadLearningState()
        }
    }

    private fun acceptSuggestion(suggestion: Suggestion) {
        viewModelScope.launch {
            // Optimistically remove from UI
            _uiState.update { state ->
                state.copy(
                    suggestions = state.suggestions.filter { it.itemId != suggestion.itemId }
                )
            }

            acceptSuggestionUseCase(suggestion)
                .onSuccess {
                    _events.send(SuggestionsEvent.SuggestionAccepted(suggestion.itemName))
                }
                .onFailure { e ->
                    _events.send(SuggestionsEvent.Error(e.message ?: "Failed to add item"))
                    // Refresh suggestions to restore state on error
                    loadLearningState()
                }
        }
    }

    private fun dismissSuggestion(itemId: String) {
        viewModelScope.launch {
            // Optimistically remove from UI
            _uiState.update { state ->
                state.copy(
                    suggestions = state.suggestions.filter { it.itemId != itemId }
                )
            }

            dismissSuggestionUseCase(itemId)
        }
    }
}

data class SuggestionsUiState(
    val suggestions: List<Suggestion> = emptyList(),
    val learningState: LearningState = LearningState.NOT_STARTED,
)

enum class LearningState {
    NOT_STARTED,
    LEARNING,
    ALMOST_READY,
    READY,
}

sealed interface SuggestionsAction {
    data class AcceptSuggestion(val suggestion: Suggestion) : SuggestionsAction
    data class DismissSuggestion(val itemId: String) : SuggestionsAction
    data object RefreshLearningState : SuggestionsAction
}

sealed interface SuggestionsEvent {
    data class SuggestionAccepted(val itemName: String) : SuggestionsEvent
    data class Error(val message: String) : SuggestionsEvent
}
```

### Analytics Event Preparation

```kotlin
package com.restock.core.analytics

/**
 * Analytics events for suggestions feature.
 * Implementation deferred to Sprint 11-12.
 */
sealed interface SuggestionAnalyticsEvent {
    data class SuggestionShown(
        val itemId: String,
        val position: Int,
        val score: Double,
    ) : SuggestionAnalyticsEvent

    data class SuggestionAccepted(
        val itemId: String,
        val position: Int,
    ) : SuggestionAnalyticsEvent

    data class SuggestionDismissed(
        val itemId: String,
        val dismissCount: Int,
    ) : SuggestionAnalyticsEvent
}
```

## Acceptance Criteria

- [ ] AcceptSuggestionUseCase adds item to list
- [ ] DismissSuggestionUseCase reduces confidence
- [ ] 3 dismissals marks as persistently dismissed
- [ ] Purchase clears dismissal state
- [ ] SuggestionsViewModel manages UI state
- [ ] Optimistic updates for smooth UX
- [ ] Analytics events prepared
- [ ] Unit tests for all use cases

## Testing

```kotlin
class AcceptSuggestionUseCaseTest {

    @Test
    fun `invoke adds item to list`() = runTest {
        val addItemUseCase = mockk<AddItemUseCase>()
        val suggestionRepository = mockk<SuggestionRepository>()
        val useCase = AcceptSuggestionUseCase(addItemUseCase, suggestionRepository)

        coEvery { addItemUseCase(any()) } returns Result.success(mockListItem())
        coEvery { suggestionRepository.acceptSuggestion(any()) } just Runs

        val suggestion = Suggestion(
            itemId = "milk-id",
            itemName = "Milk",
            reason = "Usually every 7 days",
            score = 1.5,
            confidence = 0.8,
            daysSinceLastPurchase = 8,
            avgIntervalDays = 7,
        )

        val result = useCase(suggestion)

        assertThat(result.isSuccess).isTrue()
        coVerify { addItemUseCase("Milk") }
        coVerify { suggestionRepository.acceptSuggestion("milk-id") }
    }
}

class DismissSuggestionUseCaseTest {

    @Test
    fun `invoke calls repository dismiss`() = runTest {
        val repository = mockk<SuggestionRepository>()
        coEvery { repository.dismissSuggestion(any()) } just Runs

        val useCase = DismissSuggestionUseCase(repository)
        useCase("milk-id")

        coVerify { repository.dismissSuggestion("milk-id") }
    }
}

class SuggestionsViewModelTest {

    @Test
    fun `acceptSuggestion optimistically removes from UI`() = runTest {
        val viewModel = createViewModel(
            initialSuggestions = listOf(milkSuggestion, eggsSuggestion)
        )

        viewModel.onAction(SuggestionsAction.AcceptSuggestion(milkSuggestion))

        // Immediately removed from UI
        assertThat(viewModel.uiState.value.suggestions)
            .doesNotContain(milkSuggestion)
    }

    @Test
    fun `dismissSuggestion removes from UI`() = runTest {
        val viewModel = createViewModel(
            initialSuggestions = listOf(milkSuggestion, eggsSuggestion)
        )

        viewModel.onAction(SuggestionsAction.DismissSuggestion("milk-id"))

        assertThat(viewModel.uiState.value.suggestions.map { it.itemId })
            .doesNotContain("milk-id")
    }
}
```

## Definition of Done

- [ ] All use cases implemented
- [ ] ViewModel manages state correctly
- [ ] Optimistic updates work
- [ ] Dismissal → purchase flow works
- [ ] Unit tests pass (90%+ coverage)
- [ ] Integration with UI ready

## Notes

- Optimistic updates make UI feel instant
- Restore state on error for resilience
- Analytics events are defined but not sent (Sprint 11-12)
- Learning state based on distinct shopping days
