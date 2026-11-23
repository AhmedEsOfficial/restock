# Sprint 7-8: Smart Suggestions
## Restock Android Application - Phase 1

**Duration:** Weeks 13-16 (4 weeks)
**Sprint Dates:** [TBD based on kickoff]
**Team:** Tech Lead/Senior Dev (1), UI/UX Engineer (1)
**Prerequisites:** Sprint 5-6 completed (pattern learning operational)

---

## Table of Contents

1. [Sprint Overview](#1-sprint-overview)
2. [Sprint Goals](#2-sprint-goals)
3. [User Stories](#3-user-stories)
4. [Technical Tasks](#4-technical-tasks)
5. [Design Tasks](#5-design-tasks)
6. [Suggestion Algorithm](#6-suggestion-algorithm)
7. [Technical Implementation](#7-technical-implementation)
8. [UI Specifications](#8-ui-specifications)
9. [Testing Requirements](#9-testing-requirements)
10. [Acceptance Criteria](#10-acceptance-criteria)

---

## 1. Sprint Overview

This sprint surfaces the intelligence built in Sprint 5-6 to users through smart suggestions. The suggestions section appears above the main list, proactively recommending items the user might need based on their learned purchase patterns. This is the "magic moment" that differentiates Restock.

### Sprint Theme
> *"The moment when the app anticipates your needs."*

### Why This Matters
- This is the core value proposition of Restock
- Suggestions appearing = proof that learning works
- User delight comes from relevant, timely suggestions
- Acceptance rate is our key quality metric

### Key Principles
1. **Helpful, not pushy:** Maximum 5 suggestions, easy dismiss
2. **Confidence-based:** Only suggest what we're confident about
3. **Learning from feedback:** Dismissals reduce confidence
4. **Timing matters:** Suggest when item is actually needed

---

## 2. Sprint Goals

### Primary Goals

| Goal | Owner | Success Metric |
|------|-------|----------------|
| Suggestions repository & use cases | Tech Lead | Queries return ranked suggestions |
| Suggestion prioritization algorithm | Tech Lead | Correct ordering by confidence × urgency |
| Suggestions UI section | UI/UX | Horizontal scrollable cards |
| One-tap add to list | Both | Suggestion → list item instantly |
| Dismiss with confidence reduction | Tech Lead | Pattern confidence decreases |
| Maximum 5 suggestions | Tech Lead | UI never shows more than 5 |

### Success Metrics

| Metric | Target |
|--------|--------|
| Suggestions appearing | After 3 shopping trips |
| Suggestion relevance | >50% acceptance rate |
| Add-to-list time | < 0.5 seconds |
| Dismiss feedback recorded | 100% of dismissals |
| UI scroll performance | 60fps |

---

## 3. User Stories

### US-7.1: View Suggestions
**As a** user
**I want** to see items I might need
**So that** I don't forget regular purchases

**Acceptance Criteria:**
- [ ] Suggestions section appears above main list
- [ ] Only shows after patterns are established (3+ purchases)
- [ ] Shows item name prominently
- [ ] Shows reason ("Usually every 7 days")
- [ ] Maximum 5 suggestions visible
- [ ] Horizontally scrollable if > 3

**Technical Notes:**
- Query patterns with confidence > 0.6
- Filter items already on current list
- Sort by urgency × confidence

---

### US-7.2: Accept Suggestion
**As a** user
**I want** to add a suggested item to my list with one tap
**So that** adding items is effortless

**Acceptance Criteria:**
- [ ] Tap suggestion → item added to list
- [ ] Suggestion card animates out
- [ ] Item appears in list (top of unchecked)
- [ ] Subtle success feedback
- [ ] Acceptance recorded for analytics

**Technical Notes:**
- Reuse AddItemUseCase
- Animation: card slides up and fades
- Update last_suggested timestamp

---

### US-7.3: Dismiss Suggestion
**As a** user
**I want** to dismiss suggestions I don't need
**So that** the app learns my actual preferences

**Acceptance Criteria:**
- [ ] X button dismisses suggestion
- [ ] Card animates away
- [ ] Dismissed item doesn't reappear immediately
- [ ] Pattern confidence slightly reduced
- [ ] Persistent dismissal removes from suggestions

**Technical Notes:**
- Single dismiss: reduce confidence by 0.05
- 3 consecutive dismisses: mark as "dismissed" (don't suggest)
- Reset dismissal after next actual purchase

---

### US-7.4: Suggestion Prioritization
**As a** user
**I want** the most relevant suggestions first
**So that** I see what I most likely need

**Acceptance Criteria:**
- [ ] Most urgent items appear first
- [ ] Higher confidence items preferred
- [ ] Recently dismissed items deprioritized
- [ ] Items further past due date ranked higher
- [ ] Consistent ordering (no random shuffling)

**Technical Notes:**
- Score = confidence × urgency_factor
- Urgency = days_overdue / avg_interval (capped at 2.0)
- Stable sort by score descending

---

### US-7.5: Empty Suggestions State
**As a** user
**I want** to understand when suggestions will appear
**So that** I know the app is learning

**Acceptance Criteria:**
- [ ] No suggestions section if no patterns exist
- [ ] After 1 shopping trip: subtle "Learning your patterns..."
- [ ] After 2 trips: "Almost ready to suggest..."
- [ ] After 3 trips: suggestions appear (or empty if none due)

**Technical Notes:**
- Track shopping trip count (distinct days with check-offs)
- Progressive disclosure of learning status

---

### US-7.6: Suggestion Reason Display
**As a** user
**I want** to understand why items are suggested
**So that** I trust the suggestions

**Acceptance Criteria:**
- [ ] Each suggestion shows reason text
- [ ] Format: "Usually every X days"
- [ ] Alternative: "Last bought X days ago"
- [ ] Reason is subtle, not distracting

**Technical Notes:**
- Primary: "Usually every {avg_interval} days"
- If overdue: "Last bought {days_ago} days ago"

---

## 4. Technical Tasks

### Tech Lead Tasks

#### Week 13: Suggestions Data Layer

| Task ID | Task | Estimate | Priority |
|---------|------|----------|----------|
| T-7.1 | Create SuggestionRepository interface | 2h | P0 |
| T-7.2 | Implement SuggestionRepositoryImpl | 4h | P0 |
| T-7.3 | Create GetSuggestionsUseCase | 4h | P0 |
| T-7.4 | Implement prioritization algorithm | 4h | P0 |
| T-7.5 | Filter items already on list | 2h | P0 |
| T-7.6 | Write repository unit tests | 3h | P0 |
| T-7.7 | Write prioritization algorithm tests | 3h | P0 |

#### Week 14: Accept & Dismiss Logic

| Task ID | Task | Estimate | Priority |
|---------|------|----------|----------|
| T-8.1 | Create AcceptSuggestionUseCase | 3h | P0 |
| T-8.2 | Create DismissSuggestionUseCase | 3h | P0 |
| T-8.3 | Implement confidence reduction logic | 2h | P0 |
| T-8.4 | Implement persistent dismissal tracking | 3h | P0 |
| T-8.5 | Update last_suggested timestamp | 1h | P0 |
| T-8.6 | Write accept/dismiss tests | 3h | P0 |
| T-8.7 | Analytics event tracking (prep) | 2h | P1 |

#### Week 15: ViewModel & Integration

| Task ID | Task | Estimate | Priority |
|---------|------|----------|----------|
| T-9.1 | Create SuggestionsViewModel | 4h | P0 |
| T-9.2 | Integrate with ListViewModel | 3h | P0 |
| T-9.3 | Handle suggestion → list transition | 2h | P0 |
| T-9.4 | Implement real-time suggestion updates | 3h | P0 |
| T-9.5 | Shopping trip tracking | 2h | P0 |
| T-9.6 | Integration testing | 4h | P0 |

#### Week 16: Polish & Edge Cases

| Task ID | Task | Estimate | Priority |
|---------|------|----------|----------|
| T-10.1 | Handle edge cases (no patterns, all dismissed) | 3h | P0 |
| T-10.2 | Performance optimization | 3h | P0 |
| T-10.3 | Code review | 4h | P0 |
| T-10.4 | Fix code review issues | 4h | P0 |
| T-10.5 | Documentation update | 2h | P1 |
| T-10.6 | End-to-end testing | 3h | P0 |

---

## 5. Design Tasks

### UI/UX Engineer Tasks

#### Week 13: Suggestions Design

| Task ID | Task | Estimate | Priority |
|---------|------|----------|----------|
| D-7.1 | Design suggestions section layout | 3h | P0 |
| D-7.2 | Design SuggestionCard component | 4h | P0 |
| D-7.3 | Design empty/learning states | 3h | P0 |
| D-7.4 | Create Figma prototypes | 3h | P0 |
| D-7.5 | Design accept/dismiss animations | 3h | P0 |
| D-7.6 | Plan transition animations | 2h | P0 |

#### Week 14: Component Implementation

| Task ID | Task | Estimate | Priority |
|---------|------|----------|----------|
| D-8.1 | Implement SuggestionCard | 5h | P0 |
| D-8.2 | Implement SuggestionsRow (horizontal scroll) | 4h | P0 |
| D-8.3 | Implement dismiss X button | 2h | P0 |
| D-8.4 | Implement add-to-list animation | 4h | P0 |
| D-8.5 | Implement dismiss animation | 3h | P0 |

#### Week 15: Integration & States

| Task ID | Task | Estimate | Priority |
|---------|------|----------|----------|
| D-9.1 | Integrate suggestions into list screen | 4h | P0 |
| D-9.2 | Implement learning state UI | 3h | P0 |
| D-9.3 | Implement empty suggestions state | 2h | P0 |
| D-9.4 | Card → list item transition | 4h | P0 |
| D-9.5 | Accessibility for suggestions | 3h | P0 |

#### Week 16: Polish

| Task ID | Task | Estimate | Priority |
|---------|------|----------|----------|
| D-10.1 | Animation timing refinement | 3h | P0 |
| D-10.2 | Scroll performance optimization | 2h | P0 |
| D-10.3 | Visual polish pass | 3h | P0 |
| D-10.4 | Dark mode support (stretch) | 3h | P2 |
| D-10.5 | User testing feedback iteration | 4h | P0 |

---

## 6. Suggestion Algorithm

### Suggestion Selection

```kotlin
/**
 * Selects and prioritizes suggestions to show to user.
 *
 * Selection criteria:
 * 1. Pattern confidence >= 0.6
 * 2. Not already on current list
 * 3. Not persistently dismissed
 * 4. Days since last purchase >= avg_interval - 1
 *
 * Prioritization:
 * score = confidence * urgency_factor
 * urgency_factor = min(2.0, days_overdue / avg_interval)
 *
 * @return Top 5 suggestions, ordered by score descending
 */
```

### Scoring Formula

```
score = confidence × urgency_factor

where:
  confidence = pattern.confidenceScore (0.0 to 1.0)
  urgency_factor = clamp(days_overdue / avg_interval, 0.0, 2.0)
  days_overdue = days_since_last_purchase - avg_interval

Examples:
┌─────────────┬──────────┬───────────┬─────────┬─────────┬───────┐
│ Item        │ Avg Days │ Last Buy  │ Overdue │ Conf    │ Score │
├─────────────┼──────────┼───────────┼─────────┼─────────┼───────┤
│ Milk        │ 7        │ 8 days    │ 1       │ 0.9     │ 1.03  │
│ Eggs        │ 14       │ 20 days   │ 6       │ 0.8     │ 1.14  │
│ Bread       │ 5        │ 4 days    │ -1      │ 0.85    │ 0.68  │
│ Butter      │ 21       │ 25 days   │ 4       │ 0.7     │ 0.83  │
└─────────────┴──────────┴───────────┴─────────┴─────────┴───────┘

Sorted by score: Eggs (1.14), Milk (1.03), Butter (0.83), Bread (0.68)
```

### Confidence Reduction on Dismiss

```kotlin
/**
 * Reduces pattern confidence when user dismisses suggestion.
 *
 * Single dismiss: confidence -= 0.05
 * Minimum confidence after dismiss: 0.3
 *
 * If dismissed 3 times consecutively without a purchase:
 * - Mark item as "dismissed" in suggestions
 * - Don't suggest until next actual purchase
 *
 * After next purchase of dismissed item:
 * - Reset dismiss count
 * - Restore to calculated confidence
 */
fun onDismiss(itemId: String) {
    val dismissCount = incrementDismissCount(itemId)

    if (dismissCount >= 3) {
        markAsPersistentlyDismissed(itemId)
    } else {
        reduceConfidence(itemId, by = 0.05)
    }
}

fun onPurchase(itemId: String) {
    resetDismissCount(itemId)
    clearPersistentDismissal(itemId)
}
```

### Reason Text Generation

```kotlin
fun generateReasonText(pattern: Pattern, lastPurchase: Purchase): String {
    val daysSincePurchase = daysSince(lastPurchase.purchasedAt)
    val avgInterval = pattern.avgIntervalDays.roundToInt()

    return when {
        daysSincePurchase > avgInterval + 2 ->
            "Last bought ${daysSincePurchase} days ago"
        else ->
            "Usually every ${avgInterval} days"
    }
}
```

---

## 7. Technical Implementation

### Data Models

```kotlin
// Suggestion.kt (domain model)
data class Suggestion(
    val itemId: String,
    val itemName: String,
    val reason: String,
    val score: Double,
    val confidence: Double,
    val daysSinceLastPurchase: Int,
    val avgIntervalDays: Int,
)

// SuggestionDismissal.kt (database entity)
@Entity(tableName = "suggestion_dismissals")
data class SuggestionDismissalEntity(
    @PrimaryKey
    val itemId: String,
    val dismissCount: Int,
    val lastDismissedAt: Long,
    val isPersistentlyDismissed: Boolean,
)
```

### Repository Implementation

```kotlin
// SuggestionRepository.kt
interface SuggestionRepository {
    fun observeSuggestions(): Flow<List<Suggestion>>
    suspend fun getSuggestions(limit: Int = 5): List<Suggestion>
    suspend fun dismissSuggestion(itemId: String)
    suspend fun acceptSuggestion(itemId: String)
    suspend fun isDismissed(itemId: String): Boolean
    suspend fun getShoppingTripCount(): Int
}

// SuggestionRepositoryImpl.kt
class SuggestionRepositoryImpl @Inject constructor(
    private val patternDao: PatternDao,
    private val purchaseDao: PurchaseDao,
    private val currentListDao: CurrentListDao,
    private val dismissalDao: SuggestionDismissalDao,
    private val suggestionScorer: SuggestionScorer,
    @IoDispatcher private val dispatcher: CoroutineDispatcher,
) : SuggestionRepository {

    override fun observeSuggestions(): Flow<List<Suggestion>> =
        combine(
            patternDao.observePatterns(),
            purchaseDao.observeLatestPurchases(),
            currentListDao.observeCurrentItemIds(),
            dismissalDao.observeDismissals(),
        ) { patterns, purchases, currentItemIds, dismissals ->
            calculateSuggestions(patterns, purchases, currentItemIds, dismissals)
        }.flowOn(dispatcher)

    override suspend fun getSuggestions(limit: Int): List<Suggestion> =
        withContext(dispatcher) {
            val patterns = patternDao.getPatternsAboveConfidence(MIN_CONFIDENCE)
            val currentItemIds = currentListDao.getCurrentItemIds().toSet()
            val dismissedIds = dismissalDao.getPersistentlyDismissedIds().toSet()

            patterns
                .filter { it.itemId !in currentItemIds }
                .filter { it.itemId !in dismissedIds }
                .mapNotNull { pattern ->
                    val lastPurchase = purchaseDao.getLastPurchase(pattern.itemId)
                        ?: return@mapNotNull null

                    val daysSince = daysSince(lastPurchase.purchasedAt)
                    if (daysSince < pattern.avgIntervalDays - 1) {
                        return@mapNotNull null  // Not due yet
                    }

                    suggestionScorer.score(pattern, lastPurchase)
                }
                .sortedByDescending { it.score }
                .take(limit)
        }

    override suspend fun dismissSuggestion(itemId: String) =
        withContext(dispatcher) {
            val current = dismissalDao.getDismissal(itemId)
            val newCount = (current?.dismissCount ?: 0) + 1

            val dismissal = SuggestionDismissalEntity(
                itemId = itemId,
                dismissCount = newCount,
                lastDismissedAt = Clock.System.now().toEpochMilliseconds(),
                isPersistentlyDismissed = newCount >= 3,
            )

            dismissalDao.upsert(dismissal)

            // Also reduce pattern confidence
            if (newCount < 3) {
                patternDao.reduceConfidence(itemId, by = 0.05f)
            }
        }

    override suspend fun acceptSuggestion(itemId: String) =
        withContext(dispatcher) {
            // Update last_suggested timestamp
            patternDao.updateLastSuggested(itemId, Clock.System.now().toEpochMilliseconds())
        }

    companion object {
        const val MIN_CONFIDENCE = 0.6f
    }
}
```

### Suggestion Scorer

```kotlin
// SuggestionScorer.kt
class SuggestionScorer @Inject constructor(
    private val itemDao: ItemDao,
) {
    suspend fun score(pattern: PatternEntity, lastPurchase: PurchaseEntity): Suggestion {
        val item = itemDao.getById(pattern.itemId)!!
        val daysSince = daysSince(lastPurchase.purchasedAt)
        val avgInterval = pattern.avgIntervalDays

        val daysOverdue = (daysSince - avgInterval).coerceAtLeast(0.0)
        val urgencyFactor = (daysOverdue / avgInterval).coerceIn(0.0, 2.0)
        val score = pattern.confidenceScore * (1 + urgencyFactor)

        val reason = generateReason(daysSince.toInt(), avgInterval.toInt())

        return Suggestion(
            itemId = pattern.itemId,
            itemName = item.name,
            reason = reason,
            score = score,
            confidence = pattern.confidenceScore,
            daysSinceLastPurchase = daysSince.toInt(),
            avgIntervalDays = avgInterval.toInt(),
        )
    }

    private fun generateReason(daysSince: Int, avgInterval: Int): String {
        return if (daysSince > avgInterval + 2) {
            "Last bought $daysSince days ago"
        } else {
            "Usually every $avgInterval days"
        }
    }

    private fun daysSince(timestamp: Long): Double {
        val now = Clock.System.now().toEpochMilliseconds()
        return (now - timestamp) / (24 * 60 * 60 * 1000.0)
    }
}
```

### Use Cases

```kotlin
// GetSuggestionsUseCase.kt
class GetSuggestionsUseCase @Inject constructor(
    private val suggestionRepository: SuggestionRepository,
) {
    operator fun invoke(): Flow<List<Suggestion>> =
        suggestionRepository.observeSuggestions()

    suspend fun getOnce(limit: Int = 5): List<Suggestion> =
        suggestionRepository.getSuggestions(limit)
}

// AcceptSuggestionUseCase.kt
class AcceptSuggestionUseCase @Inject constructor(
    private val addItemUseCase: AddItemUseCase,
    private val suggestionRepository: SuggestionRepository,
) {
    suspend operator fun invoke(suggestion: Suggestion): Result<CurrentListItem> {
        // Add to list
        val result = addItemUseCase(suggestion.itemName)

        // Record acceptance
        if (result.isSuccess) {
            suggestionRepository.acceptSuggestion(suggestion.itemId)
        }

        return result
    }
}

// DismissSuggestionUseCase.kt
class DismissSuggestionUseCase @Inject constructor(
    private val suggestionRepository: SuggestionRepository,
) {
    suspend operator fun invoke(itemId: String) {
        suggestionRepository.dismissSuggestion(itemId)
    }
}
```

### ViewModel

```kotlin
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
        observeLearningState()
    }

    private fun observeSuggestions() {
        viewModelScope.launch {
            getSuggestionsUseCase()
                .collect { suggestions ->
                    _uiState.update { it.copy(suggestions = suggestions) }
                }
        }
    }

    private fun observeLearningState() {
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
        }
    }

    private fun acceptSuggestion(suggestion: Suggestion) {
        viewModelScope.launch {
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
                }
        }
    }

    private fun dismissSuggestion(itemId: String) {
        viewModelScope.launch {
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
}

sealed interface SuggestionsEvent {
    data class SuggestionAccepted(val itemName: String) : SuggestionsEvent
    data class Error(val message: String) : SuggestionsEvent
}
```

---

## 8. UI Specifications

### Suggestions Section Layout

```
┌─────────────────────────────────────────────────────────────┐
│  Suggestions                                                 │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  →       │
│  │             │  │             │  │             │          │
│  │    Milk     │  │    Eggs     │  │   Bread     │          │
│  │  [× dismiss]│  │  [× dismiss]│  │  [× dismiss]│          │
│  │             │  │             │  │             │          │
│  │ Usually     │  │ Last bought │  │ Usually     │          │
│  │ every 7 days│  │ 20 days ago │  │ every 5 days│          │
│  └─────────────┘  └─────────────┘  └─────────────┘          │
│                                                             │
│  ─────────────────────────────────────────────────────────  │
│                                                             │
│  [Main list continues below...]                             │
└─────────────────────────────────────────────────────────────┘
```

### SuggestionCard Component

```kotlin
@Composable
fun SuggestionCard(
    suggestion: Suggestion,
    onAccept: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .width(140.dp)
            .clickable { onAccept() },
        shape = RoundedCornerShape(16.dp),
        color = RestockColors.Card,
        shadowElevation = 2.dp,
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
        ) {
            // Header with dismiss button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                // Spacer for balance
                Spacer(Modifier.width(24.dp))

                // Dismiss button
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(24.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Dismiss suggestion",
                        tint = RestockColors.Muted,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }

            Spacer(Modifier.height(4.dp))

            // Item name
            Text(
                text = suggestion.itemName,
                style = RestockTypography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = RestockColors.Primary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(8.dp))

            // Reason text
            Text(
                text = suggestion.reason,
                style = RestockTypography.labelMedium,
                color = RestockColors.Muted,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(8.dp))

            // Visual indicator (subtle accent line)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(
                        RestockColors.Accent.copy(alpha = 0.5f),
                        RoundedCornerShape(2.dp)
                    )
            )
        }
    }
}
```

### SuggestionsRow Component

```kotlin
@Composable
fun SuggestionsSection(
    suggestions: List<Suggestion>,
    learningState: LearningState,
    onAccept: (Suggestion) -> Unit,
    onDismiss: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = suggestions.isNotEmpty() || learningState != LearningState.READY,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
    ) {
        Column(modifier = modifier) {
            // Section header
            Text(
                text = "Suggestions",
                style = RestockTypography.titleLarge,
                color = RestockColors.Primary,
                modifier = Modifier.padding(horizontal = 16.dp),
            )

            Spacer(Modifier.height(12.dp))

            when {
                suggestions.isNotEmpty() -> {
                    // Horizontal scrolling suggestions
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(
                            items = suggestions,
                            key = { it.itemId }
                        ) { suggestion ->
                            SuggestionCard(
                                suggestion = suggestion,
                                onAccept = { onAccept(suggestion) },
                                onDismiss = { onDismiss(suggestion.itemId) },
                                modifier = Modifier.animateItem(),
                            )
                        }
                    }
                }
                learningState == LearningState.LEARNING -> {
                    LearningStateCard(
                        message = "Learning your patterns...",
                        subtext = "Add items and shop normally. Suggestions will appear soon.",
                    )
                }
                learningState == LearningState.ALMOST_READY -> {
                    LearningStateCard(
                        message = "Almost ready!",
                        subtext = "One more shopping trip and suggestions will appear.",
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Divider
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                color = RestockColors.Muted.copy(alpha = 0.2f),
            )
        }
    }
}
```

### Animation Specifications

| Animation | Duration | Easing | Description |
|-----------|----------|--------|-------------|
| Card accept | 300ms | EaseOut | Slide up + fade out |
| Card dismiss | 200ms | EaseIn | Slide right + fade out |
| Section appear | 300ms | EaseOut | Fade in + expand |
| Section hide | 200ms | EaseIn | Fade out + collapse |
| Card entry | 200ms | EaseOut | Fade in + scale from 0.95 |

### Accept Animation

```kotlin
@Composable
fun AnimatedSuggestionCard(
    suggestion: Suggestion,
    onAccept: () -> Unit,
    onDismiss: () -> Unit,
    onAnimationComplete: () -> Unit,
) {
    var isAccepted by remember { mutableStateOf(false) }
    var isDismissed by remember { mutableStateOf(false) }

    val offsetY by animateFloatAsState(
        targetValue = if (isAccepted) -100f else 0f,
        animationSpec = tween(300, easing = EaseOut),
        finishedListener = { if (isAccepted) onAnimationComplete() }
    )

    val offsetX by animateFloatAsState(
        targetValue = if (isDismissed) 100f else 0f,
        animationSpec = tween(200, easing = EaseIn),
        finishedListener = { if (isDismissed) onAnimationComplete() }
    )

    val alpha by animateFloatAsState(
        targetValue = if (isAccepted || isDismissed) 0f else 1f,
        animationSpec = tween(if (isAccepted) 300 else 200)
    )

    SuggestionCard(
        suggestion = suggestion,
        onAccept = {
            isAccepted = true
            onAccept()
        },
        onDismiss = {
            isDismissed = true
            onDismiss()
        },
        modifier = Modifier
            .graphicsLayer {
                translationY = offsetY
                translationX = offsetX
                this.alpha = alpha
            }
    )
}
```

---

## 9. Testing Requirements

### Unit Tests

| Component | Test Cases | Coverage |
|-----------|------------|----------|
| SuggestionScorer | Scoring formula, edge cases | 100% |
| SuggestionRepository | Query, filter, dismiss | 90% |
| GetSuggestionsUseCase | Limit, ordering | 90% |
| AcceptSuggestionUseCase | Add to list, track acceptance | 90% |
| DismissSuggestionUseCase | Single dismiss, persistent | 90% |
| SuggestionsViewModel | Actions, state updates | 90% |

### Algorithm Tests

```kotlin
class SuggestionScorerTest {

    @Test
    fun `overdue items have higher score than on-time items`() {
        val overduePattern = pattern(avgDays = 7, confidence = 0.8)
        val overduePurchase = purchase(daysAgo = 10)  // 3 days overdue

        val onTimePattern = pattern(avgDays = 7, confidence = 0.8)
        val onTimePurchase = purchase(daysAgo = 6)  // 1 day early

        val overdueScore = scorer.score(overduePattern, overduePurchase)
        val onTimeScore = scorer.score(onTimePattern, onTimePurchase)

        assertThat(overdueScore.score).isGreaterThan(onTimeScore.score)
    }

    @Test
    fun `higher confidence items preferred when same urgency`() {
        val highConfPattern = pattern(avgDays = 7, confidence = 0.9)
        val lowConfPattern = pattern(avgDays = 7, confidence = 0.6)
        val purchase = purchase(daysAgo = 7)

        val highScore = scorer.score(highConfPattern, purchase)
        val lowScore = scorer.score(lowConfPattern, purchase)

        assertThat(highScore.score).isGreaterThan(lowScore.score)
    }

    @Test
    fun `urgency factor capped at 2x`() {
        val pattern = pattern(avgDays = 7, confidence = 0.8)
        val veryOverdue = purchase(daysAgo = 100)

        val score = scorer.score(pattern, veryOverdue)

        // Max urgency = 2.0, so max score = 0.8 * 3.0 = 2.4
        assertThat(score.score).isLessThanOrEqualTo(2.4)
    }
}
```

### UI Tests

```kotlin
@Test
fun suggestionCard_displaysItemNameAndReason() {
    composeTestRule.setContent {
        SuggestionCard(
            suggestion = Suggestion(
                itemId = "1",
                itemName = "Milk",
                reason = "Usually every 7 days",
                score = 1.0,
                confidence = 0.9,
                daysSinceLastPurchase = 8,
                avgIntervalDays = 7,
            ),
            onAccept = {},
            onDismiss = {},
        )
    }

    composeTestRule.onNodeWithText("Milk").assertIsDisplayed()
    composeTestRule.onNodeWithText("Usually every 7 days").assertIsDisplayed()
}

@Test
fun tappingSuggestionCard_callsOnAccept() {
    var acceptCalled = false

    composeTestRule.setContent {
        SuggestionCard(
            suggestion = testSuggestion,
            onAccept = { acceptCalled = true },
            onDismiss = {},
        )
    }

    composeTestRule.onNodeWithText("Milk").performClick()

    assertThat(acceptCalled).isTrue()
}

@Test
fun tappingDismissButton_callsOnDismiss() {
    var dismissCalled = false

    composeTestRule.setContent {
        SuggestionCard(
            suggestion = testSuggestion,
            onAccept = {},
            onDismiss = { dismissCalled = true },
        )
    }

    composeTestRule.onNodeWithContentDescription("Dismiss suggestion").performClick()

    assertThat(dismissCalled).isTrue()
}
```

---

## 10. Acceptance Criteria

### Sprint Demo Checklist

1. [ ] No suggestions for new user (learning state shown)
2. [ ] Simulate 3 shopping trips (check off items 3 times)
3. [ ] Suggestions appear after 3 trips
4. [ ] Suggestions show correct reason text
5. [ ] Tap suggestion → item added to list
6. [ ] Suggestion card animates out
7. [ ] Dismiss suggestion → card disappears
8. [ ] Dismiss same item 3 times → stops appearing
9. [ ] Buy dismissed item → can be suggested again
10. [ ] Maximum 5 suggestions shown
11. [ ] Horizontal scroll works smoothly
12. [ ] Offline: suggestions still appear

### Algorithm Verification

```sql
-- Check suggestions are calculated correctly
SELECT
    s.item_name,
    p.avg_interval_days,
    p.confidence_score,
    pu.last_purchase_days_ago,
    (pu.last_purchase_days_ago - p.avg_interval_days) as days_overdue
FROM suggestions_view s
JOIN patterns p ON s.item_id = p.item_id
JOIN (
    SELECT item_id, JULIANDAY('now') - JULIANDAY(MAX(purchased_at)/1000, 'unixepoch') as last_purchase_days_ago
    FROM purchases
    GROUP BY item_id
) pu ON s.item_id = pu.item_id
ORDER BY s.score DESC;
```

### Performance Acceptance

- [ ] Suggestions load in < 100ms
- [ ] Horizontal scroll at 60fps
- [ ] Accept animation smooth (no jank)
- [ ] Memory stable with 100+ patterns

---

## Handoff to Sprint 9-10

### Completed Artifacts
- Suggestions data layer
- Prioritization algorithm
- Accept/dismiss functionality
- Suggestions UI components
- Animations

### Ready for Sprint 9-10
- Suggestion cards can show meal tag chips
- Tag-based filtering possible for meal view
- Core list + suggestions feature complete

### Known Items for Future
- No meal-based suggestion boost yet (add in Sprint 9-10)
- No "Don't suggest this week" option (consider for post-launch)
- Analytics events prepared but not sent (Sprint 11-12)

---

*Sprint 7-8 Document v1.0 | Phase 1 - Restock Android*
