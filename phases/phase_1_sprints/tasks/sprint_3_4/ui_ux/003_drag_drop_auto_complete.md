# Task: Implement Drag-Drop Reordering and Auto-Complete

**Sprint:** 3-4 (The List Feature)
**Owner:** UI/UX Engineer
**Estimate:** 10 hours (D-5.1 + D-4.2 combined)
**Priority:** P0
**Prerequisites:** List screen layout, GroceryItemCard

---

## Sprint Context

Drag-and-drop allows users to organize their list in shopping order. Auto-complete speeds up item entry by suggesting previously added items.

---

## Task Description

Implement drag-and-drop reordering with haptic feedback and auto-complete dropdown for the add item input.

## Drag-Drop Implementation

### ReorderableList.kt

```kotlin
package com.restock.feature.list.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.restock.core.domain.model.CurrentListItem

@Composable
fun ReorderableGroceryList(
    items: List<CurrentListItem>,
    onReorder: (fromIndex: Int, toIndex: Int) -> Unit,
    modifier: Modifier = Modifier,
    itemContent: @Composable (CurrentListItem, Boolean) -> Unit,
) {
    val listState = rememberLazyListState()
    val hapticFeedback = LocalHapticFeedback.current

    var draggedItemIndex by remember { mutableStateOf<Int?>(null) }
    var draggedOverItemIndex by remember { mutableStateOf<Int?>(null) }
    var dragOffset by remember { mutableStateOf(0f) }

    LazyColumn(
        state = listState,
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        itemsIndexed(
            items = items,
            key = { _, item -> item.id }
        ) { index, item ->
            val isDragging = draggedItemIndex == index
            val isTarget = draggedOverItemIndex == index

            val elevation by animateDpAsState(
                targetValue = if (isDragging) 8.dp else 0.dp,
                label = "dragElevation"
            )

            Box(
                modifier = Modifier
                    .zIndex(if (isDragging) 1f else 0f)
                    .graphicsLayer {
                        if (isDragging) {
                            translationY = dragOffset
                            shadowElevation = elevation.toPx()
                        }
                    }
                    .pointerInput(Unit) {
                        detectDragGesturesAfterLongPress(
                            onDragStart = {
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                draggedItemIndex = index
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                dragOffset += dragAmount.y

                                // Calculate which item we're over
                                val itemHeight = size.height
                                val currentOffset = index * itemHeight + dragOffset
                                val targetIndex = (currentOffset / itemHeight).toInt()
                                    .coerceIn(0, items.lastIndex)

                                if (targetIndex != draggedOverItemIndex) {
                                    draggedOverItemIndex = targetIndex
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
                            },
                            onDragEnd = {
                                draggedItemIndex?.let { from ->
                                    draggedOverItemIndex?.let { to ->
                                        if (from != to) {
                                            onReorder(from, to)
                                        }
                                    }
                                }
                                draggedItemIndex = null
                                draggedOverItemIndex = null
                                dragOffset = 0f
                            },
                            onDragCancel = {
                                draggedItemIndex = null
                                draggedOverItemIndex = null
                                dragOffset = 0f
                            }
                        )
                    }
            ) {
                itemContent(item, isDragging)
            }
        }
    }
}
```

## Auto-Complete Implementation

### AutoCompleteDropdown.kt

```kotlin
package com.restock.feature.list.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.restock.core.domain.model.GroceryItem
import com.restock.core.ui.components.RestockCard
import com.restock.core.ui.theme.RestockColors
import com.restock.core.ui.theme.RestockPreviewTheme
import com.restock.core.ui.theme.RestockSpacing

@Composable
fun AutoCompleteDropdown(
    suggestions: List<GroceryItem>,
    onSuggestionClick: (GroceryItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    RestockCard(
        modifier = modifier.fillMaxWidth(),
    ) {
        Column {
            suggestions.forEachIndexed { index, item ->
                AutoCompleteSuggestionRow(
                    item = item,
                    onClick = { onSuggestionClick(item) },
                )

                if (index < suggestions.lastIndex) {
                    HorizontalDivider(
                        color = RestockColors.Divider,
                        modifier = Modifier.padding(horizontal = RestockSpacing.md),
                    )
                }
            }
        }
    }
}

@Composable
private fun AutoCompleteSuggestionRow(
    item: GroceryItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(
                horizontal = RestockSpacing.md,
                vertical = RestockSpacing.sm + RestockSpacing.xs,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Default.History,
            contentDescription = null,
            tint = RestockColors.Muted,
            modifier = Modifier.size(20.dp),
        )

        Spacer(Modifier.width(RestockSpacing.sm))

        Text(
            text = item.name,
            style = MaterialTheme.typography.bodyMedium,
            color = RestockColors.Primary,
        )
    }
}

// ==================== PREVIEWS ====================

@Preview(showBackground = true)
@Composable
private fun AutoCompleteDropdownPreview() {
    RestockPreviewTheme {
        AutoCompleteDropdown(
            suggestions = listOf(
                GroceryItem("1", "Milk", "milk", 1000L, true),
                GroceryItem("2", "Milk chocolate", "milk chocolate", 1000L, true),
                GroceryItem("3", "Milk alternatives", "milk alternatives", 1000L, true),
            ),
            onSuggestionClick = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AutoCompleteSuggestionRowPreview() {
    RestockPreviewTheme {
        AutoCompleteSuggestionRow(
            item = GroceryItem("1", "Milk", "milk", 1000L, true),
            onClick = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}
```

## Quantity Bottom Sheet

### QuantityBottomSheet.kt

```kotlin
package com.restock.feature.list.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.restock.core.domain.model.CurrentListItem
import com.restock.core.ui.components.RestockButton
import com.restock.core.ui.components.RestockButtonVariant
import com.restock.core.ui.theme.RestockColors
import com.restock.core.ui.theme.RestockPreviewTheme
import com.restock.core.ui.theme.RestockSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuantityBottomSheet(
    item: CurrentListItem,
    onDismiss: () -> Unit,
    onConfirm: (quantity: Int, unit: String?) -> Unit,
) {
    var quantity by remember { mutableStateOf(item.quantity) }
    var selectedUnit by remember { mutableStateOf(item.unit) }
    val hapticFeedback = LocalHapticFeedback.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = RestockColors.Card,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(RestockSpacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Item name
            Text(
                text = item.name,
                style = MaterialTheme.typography.titleMedium,
                color = RestockColors.Primary,
            )

            Spacer(Modifier.height(RestockSpacing.lg))

            // Quantity selector
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(RestockSpacing.md),
            ) {
                // Decrease button
                FilledIconButton(
                    onClick = {
                        if (quantity > 1) {
                            quantity--
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        }
                    },
                    enabled = quantity > 1,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = RestockColors.Accent.copy(alpha = 0.15f),
                        contentColor = RestockColors.Accent,
                        disabledContainerColor = RestockColors.Disabled.copy(alpha = 0.15f),
                        disabledContentColor = RestockColors.Muted,
                    ),
                ) {
                    Icon(Icons.Default.Remove, "Decrease quantity")
                }

                // Quantity display
                Text(
                    text = quantity.toString(),
                    style = MaterialTheme.typography.displayLarge,
                    color = RestockColors.Primary,
                    modifier = Modifier.widthIn(min = 64.dp),
                )

                // Increase button
                FilledIconButton(
                    onClick = {
                        quantity++
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    },
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = RestockColors.Accent.copy(alpha = 0.15f),
                        contentColor = RestockColors.Accent,
                    ),
                ) {
                    Icon(Icons.Default.Add, "Increase quantity")
                }
            }

            Spacer(Modifier.height(RestockSpacing.lg))

            // Unit selector
            Text(
                text = "Unit (optional)",
                style = MaterialTheme.typography.labelMedium,
                color = RestockColors.Muted,
            )

            Spacer(Modifier.height(RestockSpacing.sm))

            Row(
                horizontalArrangement = Arrangement.spacedBy(RestockSpacing.sm),
            ) {
                COMMON_UNITS.forEach { unit ->
                    FilterChip(
                        selected = selectedUnit == unit,
                        onClick = {
                            selectedUnit = if (selectedUnit == unit) null else unit
                        },
                        label = { Text(unit) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = RestockColors.Accent.copy(alpha = 0.15f),
                            selectedLabelColor = RestockColors.Accent,
                        ),
                    )
                }
            }

            Spacer(Modifier.height(RestockSpacing.xl))

            // Confirm button
            RestockButton(
                text = "Update",
                onClick = { onConfirm(quantity, selectedUnit) },
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(RestockSpacing.md))
        }
    }
}

private val COMMON_UNITS = listOf("each", "lb", "oz", "gal", "L")

// ==================== PREVIEWS ====================

@Preview(showBackground = true)
@Composable
private fun QuantityBottomSheetContentPreview() {
    RestockPreviewTheme {
        // Preview just the content (not the sheet itself)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("Milk", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(16.dp))
            Text("2", style = MaterialTheme.typography.displayLarge)
            Spacer(Modifier.height(16.dp))
            Row {
                COMMON_UNITS.take(3).forEach {
                    FilterChip(selected = it == "gal", onClick = {}, label = { Text(it) })
                    Spacer(Modifier.width(8.dp))
                }
            }
        }
    }
}
```

## Acceptance Criteria

### Drag-Drop
- [ ] Long-press initiates drag
- [ ] Item visually lifts (elevation)
- [ ] Other items shift during drag
- [ ] Haptic feedback on grab
- [ ] Haptic feedback when passing items
- [ ] Drop saves new order
- [ ] Order persists across restarts

### Auto-Complete
- [ ] Dropdown appears after 1 character
- [ ] Maximum 5 suggestions
- [ ] History icon indicates previous items
- [ ] Tap adds item to list
- [ ] Dropdown dismisses after selection

### Quantity Sheet
- [ ] Shows on long-press
- [ ] Increment/decrement buttons work
- [ ] Haptic feedback on changes
- [ ] Unit selection chips
- [ ] Update button confirms changes

## Definition of Done

- [ ] All components compile without errors
- [ ] Interactions are smooth (60fps)
- [ ] Haptic feedback implemented
- [ ] Accessibility supported
- [ ] Previews render correctly

## Notes

- Consider using sh.calvin.reorderable library for production
- Haptic feedback is crucial for perceived quality
- Test drag-drop with various list lengths
