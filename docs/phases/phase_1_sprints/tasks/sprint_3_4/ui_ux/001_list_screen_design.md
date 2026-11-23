# Task: Design and Implement List Screen Layout

**Sprint:** 3-4 (The List Feature)
**Owner:** UI/UX Engineer
**Estimate:** 10 hours (D-3.1 through D-3.7 combined)
**Priority:** P0
**Prerequisites:** Sprint 1-2 design system complete

---

## Sprint Context

The list screen is the primary user interface of Restock. It must feel instant, delightful, and work perfectly offline. Users should be able to add their first item within 15 seconds of opening the app.

### Sprint Theme
> *"Make the simple list feel magical."*

### Key Metrics
- Time to first item added: < 15 seconds
- Add item interaction: < 2 taps
- Check item interaction: 1 tap
- List scroll performance: 60 fps

---

## Task Description

Design wireframes, create Figma prototypes, and implement the main list screen layout in Compose.

## Screen Layout Specification

```
┌─────────────────────────────────────────────────────────────┐
│  Status Bar                                                  │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  ┌───────────────────────────────────────────────────────┐  │
│  │ Add an item...                                  [mic] │  │  <- AddItemTextField
│  └───────────────────────────────────────────────────────┘  │
│                                                              │
│  ┌───────────────────────────────────────────────────────┐  │
│  │ Milk                                                  │  │  <- AutoComplete
│  │ Milk chocolate                                        │  │     Dropdown
│  │ Milk alternatives                                     │  │     (appears when typing)
│  └───────────────────────────────────────────────────────┘  │
│                                                              │
│  ┌───────────────────────────────────────────────────────┐  │
│  │ ☐  Eggs                                          ×2   │  │  <- GroceryItemCard
│  └───────────────────────────────────────────────────────┘  │     (unchecked)
│  ┌───────────────────────────────────────────────────────┐  │
│  │ ☐  Bread                                              │  │
│  └───────────────────────────────────────────────────────┘  │
│  ┌───────────────────────────────────────────────────────┐  │
│  │ ☐  Butter                                             │  │
│  └───────────────────────────────────────────────────────┘  │
│                                                              │
│  ───────────────── In Cart (2) ─────────────────────────    │  <- Section Header
│                                                              │
│  ┌───────────────────────────────────────────────────────┐  │
│  │ ☑  Milk                                               │  │  <- Checked item (muted)
│  └───────────────────────────────────────────────────────┘  │
│  ┌───────────────────────────────────────────────────────┐  │
│  │ ☑  Cheese                                             │  │
│  └───────────────────────────────────────────────────────┘  │
│                                                              │
│                                                              │
├─────────────────────────────────────────────────────────────┤
│  [List]              [Meals]              [History]          │  <- Bottom Nav
└─────────────────────────────────────────────────────────────┘
```

## Implementation

### ListScreen.kt

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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.restock.core.ui.components.*
import com.restock.core.ui.theme.RestockColors
import com.restock.core.ui.theme.RestockSpacing

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

    // Handle one-shot events
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
}

@Composable
private fun AddItemSection(
    inputText: String,
    suggestions: List<GroceryItem>,
    onInputChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onSuggestionClick: (GroceryItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        AddItemTextField(
            value = inputText,
            onValueChange = onInputChange,
            onSubmit = onSubmit,
        )

        // Auto-complete dropdown
        AnimatedVisibility(
            visible = suggestions.isNotEmpty(),
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
        ) {
            AutoCompleteDropdown(
                suggestions = suggestions,
                onSuggestionClick = onSuggestionClick,
                modifier = Modifier.padding(top = RestockSpacing.xs)
            )
        }
    }
}

@Composable
private fun GroceryList(
    uncheckedItems: List<CurrentListItem>,
    checkedItems: List<CurrentListItem>,
    onToggleItem: (String, Boolean) -> Unit,
    onDeleteItem: (String) -> Unit,
    onReorder: (Int, Int) -> Unit,
    onQuantityClick: (CurrentListItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(
            horizontal = RestockSpacing.md,
            vertical = RestockSpacing.sm,
        ),
        verticalArrangement = Arrangement.spacedBy(RestockSpacing.sm),
    ) {
        // Unchecked items
        items(
            items = uncheckedItems,
            key = { it.id }
        ) { item ->
            GroceryItemCard(
                item = item,
                onCheckedChange = { checked -> onToggleItem(item.id, checked) },
                onDelete = { onDeleteItem(item.id) },
                onQuantityClick = { onQuantityClick(item) },
                modifier = Modifier.animateItem(),
            )
        }

        // In Cart section header
        if (checkedItems.isNotEmpty()) {
            item(key = "in_cart_header") {
                InCartHeader(
                    count = checkedItems.size,
                    modifier = Modifier.padding(
                        top = RestockSpacing.md,
                        bottom = RestockSpacing.xs,
                    )
                )
            }

            // Checked items
            items(
                items = checkedItems,
                key = { it.id }
            ) { item ->
                GroceryItemCard(
                    item = item,
                    onCheckedChange = { checked -> onToggleItem(item.id, checked) },
                    onDelete = { onDeleteItem(item.id) },
                    onQuantityClick = { onQuantityClick(item) },
                    isCheckedSection = true,
                    modifier = Modifier.animateItem(),
                )
            }
        }
    }
}

@Composable
private fun InCartHeader(
    count: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = RestockColors.Muted.copy(alpha = 0.3f),
        )
        Text(
            text = "In Cart ($count)",
            style = MaterialTheme.typography.labelMedium,
            color = RestockColors.Muted,
            modifier = Modifier.padding(horizontal = RestockSpacing.md),
        )
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = RestockColors.Muted.copy(alpha = 0.3f),
        )
    }
}
```

## Acceptance Criteria

- [ ] List screen layout matches specification
- [ ] Add item input field at top
- [ ] Auto-complete dropdown appears when typing
- [ ] Unchecked items section displays correctly
- [ ] In Cart section with header
- [ ] Checked items appear muted
- [ ] Empty state shown when list empty
- [ ] Loading state during initial load
- [ ] Snackbar for feedback

## Figma Deliverables

1. Wireframe for empty state
2. Wireframe with items
3. Wireframe with auto-complete open
4. Wireframe with In Cart section
5. Interactive prototype for user flow

## Definition of Done

- [ ] All wireframes complete in Figma
- [ ] Interactive prototype created
- [ ] ListScreen composable implemented
- [ ] Integrates with ViewModel
- [ ] Snackbar events handled
- [ ] Empty and loading states implemented
- [ ] Matches design specification

## Dependent Tasks

This task works in parallel with:
- 002_grocery_item_card.md
- 003_auto_complete_dropdown.md
- 004_quantity_bottom_sheet.md

## Notes

- Use LazyColumn for performance
- AnimateItem modifier for smooth list animations
- Keep keyboard open for rapid item entry
- Test on various screen sizes
