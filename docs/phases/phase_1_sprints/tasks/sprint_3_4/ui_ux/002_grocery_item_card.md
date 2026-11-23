# Task: Implement GroceryItemCard Component

**Sprint:** 3-4 (The List Feature)
**Owner:** UI/UX Engineer
**Estimate:** 9 hours (D-4.3 + D-4.4 + D-4.5 combined)
**Priority:** P0
**Prerequisites:** Sprint 1-2 components (RestockCard, RestockCheckbox)

---

## Sprint Context

The GroceryItemCard is the primary interactive element in the list. It must support checking, swiping to delete, and long-press for quantity adjustment while feeling smooth and responsive.

---

## Task Description

Implement GroceryItemCard with swipe-to-delete, check animation, and quantity badge.

## Implementation

### GroceryItemCard.kt

```kotlin
package com.restock.feature.list.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.restock.core.domain.model.CurrentListItem
import com.restock.core.ui.components.MealTagChip
import com.restock.core.ui.components.MealType
import com.restock.core.ui.components.RestockCard
import com.restock.core.ui.components.RestockCheckbox
import com.restock.core.ui.theme.RestockColors
import com.restock.core.ui.theme.RestockPreviewTheme
import com.restock.core.ui.theme.RestockSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroceryItemCard(
    item: CurrentListItem,
    onCheckedChange: (Boolean) -> Unit,
    onDelete: () -> Unit,
    onQuantityClick: () -> Unit,
    modifier: Modifier = Modifier,
    isCheckedSection: Boolean = false,
    mealTags: List<MealType> = emptyList(),
) {
    val hapticFeedback = LocalHapticFeedback.current

    // Swipe to dismiss state
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                onDelete()
                true
            } else {
                false
            }
        },
        positionalThreshold = { totalDistance -> totalDistance * 0.4f }
    )

    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier,
        backgroundContent = {
            DeleteBackground(dismissState = dismissState)
        },
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true,
    ) {
        CardContent(
            item = item,
            onCheckedChange = onCheckedChange,
            onQuantityClick = onQuantityClick,
            isCheckedSection = isCheckedSection,
            mealTags = mealTags,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DeleteBackground(
    dismissState: SwipeToDismissBoxState,
) {
    val color by animateColorAsState(
        targetValue = when (dismissState.targetValue) {
            SwipeToDismissBoxValue.EndToStart -> RestockColors.Error
            else -> Color.Transparent
        },
        animationSpec = tween(200),
        label = "deleteBackground"
    )

    val iconScale by animateFloatAsState(
        targetValue = when (dismissState.targetValue) {
            SwipeToDismissBoxValue.EndToStart -> 1f
            else -> 0.5f
        },
        animationSpec = tween(200),
        label = "deleteIconScale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color, shape = MaterialTheme.shapes.large)
            .padding(horizontal = RestockSpacing.lg),
        contentAlignment = Alignment.CenterEnd,
    ) {
        Icon(
            imageVector = Icons.Default.Delete,
            contentDescription = "Delete",
            tint = Color.White,
            modifier = Modifier.scale(iconScale),
        )
    }
}

@Composable
private fun CardContent(
    item: CurrentListItem,
    onCheckedChange: (Boolean) -> Unit,
    onQuantityClick: () -> Unit,
    isCheckedSection: Boolean,
    mealTags: List<MealType>,
) {
    val hapticFeedback = LocalHapticFeedback.current

    // Animate card appearance when checked/unchecked
    val contentAlpha by animateFloatAsState(
        targetValue = if (isCheckedSection) 0.6f else 1f,
        animationSpec = tween(200),
        label = "contentAlpha"
    )

    RestockCard(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(contentAlpha)
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        onQuantityClick()
                    }
                )
            },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = RestockSpacing.sm,
                    vertical = RestockSpacing.sm + RestockSpacing.xs,
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Checkbox
            RestockCheckbox(
                checked = item.isChecked,
                onCheckedChange = onCheckedChange,
                contentDescription = if (item.isChecked) {
                    "Uncheck ${item.name}"
                } else {
                    "Check off ${item.name}"
                },
            )

            Spacer(Modifier.width(RestockSpacing.xs))

            // Item name and meal tags
            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isCheckedSection) RestockColors.Muted else RestockColors.Primary,
                    textDecoration = if (isCheckedSection) TextDecoration.LineThrough else null,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                // Meal tags
                if (mealTags.isNotEmpty()) {
                    Spacer(Modifier.height(RestockSpacing.xs))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(RestockSpacing.xs),
                    ) {
                        mealTags.take(2).forEach { mealType ->
                            MealTagChip(
                                mealType = mealType,
                                showEmoji = false,
                            )
                        }
                    }
                }
            }

            // Quantity badge
            if (item.quantity > 1) {
                Spacer(Modifier.width(RestockSpacing.sm))
                QuantityBadge(
                    quantity = item.quantity,
                    unit = item.unit,
                    onClick = onQuantityClick,
                )
            }
        }
    }
}

@Composable
fun QuantityBadge(
    quantity: Int,
    unit: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = RestockColors.Accent.copy(alpha = 0.15f),
    ) {
        Text(
            text = if (unit != null) "$quantity $unit" else "×$quantity",
            style = MaterialTheme.typography.labelSmall,
            color = RestockColors.Accent,
            modifier = Modifier.padding(
                horizontal = RestockSpacing.sm,
                vertical = RestockSpacing.xs,
            ),
        )
    }
}

// ==================== PREVIEWS ====================

@Preview(showBackground = true)
@Composable
private fun GroceryItemCardPreview() {
    RestockPreviewTheme {
        GroceryItemCard(
            item = CurrentListItem(
                id = "1",
                itemId = "item1",
                name = "Eggs",
                quantity = 1,
                unit = null,
                addedAt = 1000L,
                checkedAt = null,
                position = 0,
                isChecked = false,
            ),
            onCheckedChange = {},
            onDelete = {},
            onQuantityClick = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun GroceryItemCardWithQuantityPreview() {
    RestockPreviewTheme {
        GroceryItemCard(
            item = CurrentListItem(
                id = "2",
                itemId = "item2",
                name = "Milk",
                quantity = 2,
                unit = "gallons",
                addedAt = 1000L,
                checkedAt = null,
                position = 1,
                isChecked = false,
            ),
            onCheckedChange = {},
            onDelete = {},
            onQuantityClick = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun GroceryItemCardCheckedPreview() {
    RestockPreviewTheme {
        GroceryItemCard(
            item = CurrentListItem(
                id = "3",
                itemId = "item3",
                name = "Bread",
                quantity = 1,
                unit = null,
                addedAt = 1000L,
                checkedAt = 2000L,
                position = 2,
                isChecked = true,
            ),
            onCheckedChange = {},
            onDelete = {},
            onQuantityClick = {},
            isCheckedSection = true,
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun GroceryItemCardWithTagsPreview() {
    RestockPreviewTheme {
        GroceryItemCard(
            item = CurrentListItem(
                id = "4",
                itemId = "item4",
                name = "Eggs",
                quantity = 12,
                unit = null,
                addedAt = 1000L,
                checkedAt = null,
                position = 0,
                isChecked = false,
            ),
            onCheckedChange = {},
            onDelete = {},
            onQuantityClick = {},
            mealTags = listOf(MealType.BREAKFAST),
            modifier = Modifier.padding(16.dp),
        )
    }
}
```

## Specifications

### Card Layout
- Padding: 12dp horizontal, 14dp vertical
- Min height: 56dp (touch target compliance)
- Corner radius: 14dp

### Checkbox
- Size: 24dp visual, 48dp touch target
- Unchecked: Muted border, transparent fill
- Checked: Success fill, white checkmark
- Animation: 200ms

### Swipe to Delete
- Threshold: 40% of card width
- Background: Error color
- Icon: Delete, white
- Haptic feedback on threshold reached

### Quantity Badge
- Background: Accent @ 15%
- Text: Accent
- Format: "×2" or "2 lbs"

### Checked State
- Opacity: 60%
- Text: Strikethrough
- Color: Muted

## Acceptance Criteria

- [ ] Swipe left reveals delete action
- [ ] Swipe threshold triggers delete
- [ ] Haptic feedback on interactions
- [ ] Check animation smooth (200ms)
- [ ] Long-press triggers quantity action
- [ ] Quantity badge shows when quantity > 1
- [ ] Checked items appear muted with strikethrough
- [ ] Meal tags display correctly
- [ ] Touch targets minimum 48dp
- [ ] Previews for all states

## Definition of Done

- [ ] Component compiles without errors
- [ ] All interactions working correctly
- [ ] Animations are smooth (60fps)
- [ ] Accessibility compliant
- [ ] Previews render correctly
- [ ] Integrates with ListScreen

## Notes

- Use SwipeToDismissBox from Material3
- Haptic feedback improves perceived quality
- Long-press detection via pointerInput
- Test swipe on various device sizes
