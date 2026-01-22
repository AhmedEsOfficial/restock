# Task: Integrate Suggestions & Polish

**Sprint:** 7-8 (Smart Suggestions)
**Owner:** UI/UX Engineer
**Estimate:** 19 hours (D-9.1 through D-9.5, D-10.1 through D-10.5 combined)
**Priority:** P0
**Prerequisites:** 001_suggestions_ui_components.md

---

## Sprint Context

Integrate the suggestions components into the main list screen, implement accessibility features, and polish all animations.

### Sprint Theme
> *"The moment when the app anticipates your needs."*

---

## Task Description

Integrate suggestions into the ListScreen, implement card-to-list transitions, accessibility features, and final polish.

## Integration into ListScreen

### Updated ListScreen.kt

```kotlin
package com.restock.feature.list.ui

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.restock.core.ui.components.*
import com.restock.core.ui.theme.RestockColors
import com.restock.core.ui.theme.RestockSpacing
import com.restock.feature.list.ui.components.*
import com.restock.feature.suggestions.ui.components.SuggestionsSection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListScreen(
    viewModel: ListViewModel = hiltViewModel(),
    onNavigateToMeals: () -> Unit = {},
    onNavigateToHistory: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current

    // Handle events
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
                is ListEvent.UndoSuccessful -> {
                    snackbarHostState.showSnackbar("Restored")
                }
                is ListEvent.SuggestionAccepted -> {
                    snackbarHostState.showSnackbar("${event.itemName} added from suggestion")
                }
                is ListEvent.Error -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                else -> {}
            }
        }
    }

    Scaffold(
        snackbarHost = { RestockSnackbarHost(snackbarHostState) },
        containerColor = RestockColors.Background,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Add Item Input
            AddItemSection(
                inputText = uiState.inputText,
                suggestions = uiState.autoCompleteSuggestions,
                onInputChange = { viewModel.onAction(ListAction.UpdateInputText(it)) },
                onSubmit = {
                    viewModel.onAction(ListAction.AddItem(uiState.inputText))
                    focusManager.clearFocus()
                },
                onSuggestionClick = { item ->
                    viewModel.onAction(ListAction.SelectAutoComplete(item))
                    focusManager.clearFocus()
                },
                modifier = Modifier.padding(RestockSpacing.md)
            )

            // Smart Suggestions Section
            SuggestionsSection(
                suggestions = uiState.suggestions,
                learningState = uiState.learningState,
                onAccept = { suggestion ->
                    viewModel.onAction(ListAction.AcceptSuggestion(suggestion))
                },
                onDismiss = { itemId ->
                    viewModel.onAction(ListAction.DismissSuggestion(itemId))
                },
            )

            // List Content
            if (uiState.isLoading) {
                LoadingScreen(modifier = Modifier.weight(1f))
            } else if (uiState.isEmpty) {
                EmptyListState(modifier = Modifier.weight(1f))
            } else {
                GroceryList(
                    uncheckedItems = uiState.uncheckedItems,
                    checkedItems = uiState.checkedItems,
                    onToggleItem = { id, checked ->
                        viewModel.onAction(ListAction.ToggleItem(id, checked))
                    },
                    onDeleteItem = { id ->
                        viewModel.onAction(ListAction.DeleteItem(id))
                    },
                    onReorder = { from, to ->
                        viewModel.onAction(ListAction.ReorderItems(from, to))
                    },
                    onQuantityClick = { item ->
                        viewModel.onAction(ListAction.SelectItemForQuantity(item))
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }

    // Quantity Bottom Sheet
    uiState.selectedItemForQuantity?.let { item ->
        QuantityBottomSheet(
            item = item,
            onDismiss = {
                viewModel.onAction(ListAction.SelectItemForQuantity(null))
            },
            onConfirm = { quantity, unit ->
                viewModel.onAction(ListAction.UpdateQuantity(item.id, quantity, unit))
            }
        )
    }

    // Completion Celebration
    if (uiState.showCompletionCelebration) {
        ListCompleteCelebration(
            show = true,
            onDismiss = {
                viewModel.onAction(ListAction.DismissCompletionCelebration)
            }
        )
    }
}
```

## Accessibility Implementation

### SuggestionCard Accessibility

```kotlin
@Composable
fun SuggestionCard(
    suggestion: Suggestion,
    onAccept: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // ... existing code ...

    Surface(
        modifier = modifier
            .width(140.dp)
            .scale(scale)
            .semantics {
                // Describe the card for screen readers
                contentDescription = "${suggestion.itemName}, ${suggestion.reason}. " +
                    "Tap to add to list. Double tap and hold for more options."

                // Custom action for dismiss
                customActions = listOf(
                    CustomAccessibilityAction("Dismiss suggestion") {
                        onDismiss()
                        true
                    }
                )
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
            ) {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                onAccept()
            },
        // ... rest of card
    )
}
```

### SuggestionsSection Accessibility

```kotlin
@Composable
fun SuggestionsSection(
    // ... params
) {
    // ... existing code ...

    Column(
        modifier = modifier.semantics {
            // Group as a region
            collectionInfo = CollectionInfo(
                rowCount = 1,
                columnCount = suggestions.size
            )
        }
    ) {
        // Header announced when section becomes visible
        Text(
            text = "Suggestions",
            modifier = Modifier.semantics {
                heading()
            },
            // ... rest
        )

        // ... rest of section
    }
}
```

## Animation Timing Refinement

### Animation Specs

```kotlin
object SuggestionAnimationSpecs {
    // Card entry animation
    val cardEntry = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMedium,
    )

    // Accept animation
    val acceptSlide = tween<Float>(
        durationMillis = 300,
        easing = EaseOut,
    )

    // Dismiss animation
    val dismissSlide = tween<Float>(
        durationMillis = 200,
        easing = EaseIn,
    )

    // Section expand/collapse
    val sectionExpand = spring<IntSize>(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessLow,
    )

    // Fade animations
    val fade = tween<Float>(
        durationMillis = 200,
        easing = LinearEasing,
    )
}
```

## Scroll Performance Optimization

```kotlin
@Composable
fun SuggestionsSection(
    suggestions: List<Suggestion>,
    // ... other params
) {
    // ... existing code ...

    // Optimize LazyRow performance
    LazyRow(
        contentPadding = PaddingValues(horizontal = RestockSpacing.md),
        horizontalArrangement = Arrangement.spacedBy(RestockSpacing.sm),
        // Prefetch items for smoother scrolling
        flingBehavior = rememberSnapFlingBehavior(
            lazyListState = rememberLazyListState()
        ),
    ) {
        items(
            items = suggestions,
            key = { it.itemId }, // Stable keys for animation
        ) { suggestion ->
            // Use remember to avoid recomposition
            key(suggestion.itemId) {
                AnimatedSuggestionCard(
                    suggestion = suggestion,
                    onAccept = { onAccept(suggestion) },
                    onDismiss = { onDismiss(suggestion.itemId) },
                    onAnimationComplete = {},
                    modifier = Modifier.animateItem(),
                )
            }
        }
    }
}
```

## Visual Polish Pass

### Card Shadow Refinement

```kotlin
Surface(
    modifier = modifier.width(140.dp),
    shape = RoundedCornerShape(16.dp),
    color = RestockColors.Card,
    shadowElevation = 2.dp,
    tonalElevation = 0.dp, // Prevent tonal elevation on dark mode
)
```

### Accent Bar Gradient

```kotlin
// Enhanced accent bar with gradient
Box(
    modifier = Modifier
        .fillMaxWidth()
        .height(3.dp)
        .background(
            brush = Brush.horizontalGradient(
                colors = listOf(
                    RestockColors.Accent.copy(alpha = 0.3f),
                    RestockColors.Accent.copy(alpha = 0.6f),
                    RestockColors.Accent.copy(alpha = 0.3f),
                )
            ),
            shape = RoundedCornerShape(2.dp)
        )
)
```

## User Testing Feedback Iteration

Based on typical user testing feedback:

### Issue 1: Dismiss button too small
**Fix:** Increase touch target to 32dp while keeping icon at 16dp

```kotlin
IconButton(
    onClick = onDismiss,
    modifier = Modifier.size(32.dp), // Larger touch target
) {
    Icon(
        modifier = Modifier.size(16.dp), // Same visual size
        // ...
    )
}
```

### Issue 2: Not clear cards are tappable
**Fix:** Add subtle hover/focus state

```kotlin
val backgroundColor by animateColorAsState(
    targetValue = if (isPressed) {
        RestockColors.Card.copy(alpha = 0.95f)
    } else {
        RestockColors.Card
    },
    label = "cardBg"
)
```

### Issue 3: Learning state not noticeable
**Fix:** Add subtle animation to brain emoji

```kotlin
@Composable
fun AnimatedBrainEmoji() {
    val infiniteTransition = rememberInfiniteTransition(label = "brain")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "brainScale"
    )

    Text(
        text = "🧠",
        modifier = Modifier.scale(scale),
        style = MaterialTheme.typography.titleMedium,
    )
}
```

## Acceptance Criteria

- [ ] Suggestions integrated into ListScreen
- [ ] Animations smooth at 60fps
- [ ] Accessibility fully supported
- [ ] Touch targets meet 48dp minimum
- [ ] Scroll performance optimized
- [ ] User testing feedback addressed
- [ ] Visual polish complete

## Definition of Done

- [ ] Integration complete and tested
- [ ] Accessibility Scanner shows no errors
- [ ] TalkBack navigation works
- [ ] Performance profiled (no jank)
- [ ] All edge cases handled
- [ ] Ready for Sprint 9-10

## Notes

- Test on various device sizes
- Verify animations respect reduced motion settings
- Keep suggestions section lightweight
- Consider memory usage with many suggestions
