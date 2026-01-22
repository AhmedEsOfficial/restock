# Task: Implement Meal Tags UI

**Sprint:** 9-10 (Meal Tags & Polish)
**Owner:** UI/UX Engineer
**Estimate:** 23 hours (D-9.1 through D-9.6, D-10.1 through D-10.6 combined)
**Priority:** P0
**Prerequisites:** Sprint 7-8 completed

---

## Sprint Context

This sprint adds the meal tagging feature with a clean, intuitive UI. Meal tags provide lightweight categorization without requiring rigid meal planning.

### Sprint Theme
> *"Refine every detail until it delights."*

### Meal Tag Colors

| Meal | Color | Hex | Emoji |
|------|-------|-----|-------|
| Breakfast | Amber | `#F59E0B` | 🌅 |
| Lunch | Emerald | `#10B981` | ☀️ |
| Dinner | Indigo | `#6366F1` | 🌙 |
| Snack | Pink | `#EC4899` | ✨ |

---

## Task Description

Design and implement meal tag chips, tag selection sheet, and the Meals tab screen.

## MealTagChip Component

```kotlin
package com.restock.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.restock.core.domain.model.MealType
import com.restock.core.ui.theme.RestockColors
import com.restock.core.ui.theme.RestockPreviewTheme
import com.restock.core.ui.theme.RestockSpacing

val MealType.color: Color
    get() = when (this) {
        MealType.BREAKFAST -> RestockColors.Breakfast
        MealType.LUNCH -> RestockColors.Lunch
        MealType.DINNER -> RestockColors.Dinner
        MealType.SNACK -> RestockColors.Snack
    }

@Composable
fun MealTagChip(
    mealType: MealType,
    modifier: Modifier = Modifier,
    showEmoji: Boolean = true,
    onRemove: (() -> Unit)? = null,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = mealType.color.copy(alpha = 0.15f),
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = RestockSpacing.sm,
                vertical = RestockSpacing.xs,
            ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (showEmoji) {
                Text(
                    text = mealType.emoji,
                    style = MaterialTheme.typography.labelSmall,
                )

                Spacer(Modifier.width(4.dp))
            }

            Text(
                text = mealType.displayName,
                style = MaterialTheme.typography.labelSmall,
                color = mealType.color,
            )

            if (onRemove != null) {
                Spacer(Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remove ${mealType.displayName} tag",
                    tint = mealType.color,
                    modifier = Modifier
                        .size(14.dp)
                        .clickable { onRemove() }
                )
            }
        }
    }
}

// ==================== PREVIEWS ====================

@Preview(showBackground = true)
@Composable
private fun MealTagChipBreakfastPreview() {
    RestockPreviewTheme {
        MealTagChip(
            mealType = MealType.BREAKFAST,
            modifier = Modifier.padding(8.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MealTagChipWithRemovePreview() {
    RestockPreviewTheme {
        MealTagChip(
            mealType = MealType.LUNCH,
            onRemove = {},
            modifier = Modifier.padding(8.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AllMealTagChipsPreview() {
    RestockPreviewTheme {
        Row(
            modifier = Modifier.padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            MealType.entries.forEach { mealType ->
                MealTagChip(mealType = mealType)
            }
        }
    }
}
```

## TagSelectionSheet Component

```kotlin
package com.restock.feature.meals.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.restock.core.domain.model.MealType
import com.restock.core.ui.components.color
import com.restock.core.ui.theme.RestockColors
import com.restock.core.ui.theme.RestockPreviewTheme
import com.restock.core.ui.theme.RestockSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagSelectionSheet(
    itemName: String,
    currentTags: List<MealType>,
    onTagToggle: (MealType) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = RestockColors.Card,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(RestockSpacing.lg),
        ) {
            Text(
                text = "Tag \"$itemName\"",
                style = MaterialTheme.typography.titleLarge,
                color = RestockColors.Primary,
            )

            Spacer(Modifier.height(RestockSpacing.xs))

            Text(
                text = "Select meal categories for this item",
                style = MaterialTheme.typography.bodyMedium,
                color = RestockColors.Muted,
            )

            Spacer(Modifier.height(RestockSpacing.lg))

            MealType.entries.forEach { mealType ->
                val isSelected = mealType in currentTags
                val color = mealType.color

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = RestockSpacing.xs)
                        .clickable { onTagToggle(mealType) },
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) color.copy(alpha = 0.15f) else Color.Transparent,
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (isSelected) color else RestockColors.Muted.copy(alpha = 0.3f)
                    ),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(RestockSpacing.md),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = mealType.emoji,
                            style = MaterialTheme.typography.titleLarge,
                        )

                        Spacer(Modifier.width(RestockSpacing.sm))

                        Text(
                            text = mealType.displayName,
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (isSelected) color else RestockColors.Primary,
                        )

                        Spacer(Modifier.weight(1f))

                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = color,
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(RestockSpacing.xl))
        }
    }
}

// ==================== PREVIEWS ====================

@Preview(showBackground = true)
@Composable
private fun TagSelectionSheetContentPreview() {
    RestockPreviewTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
        ) {
            Text("Tag \"Eggs\"", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(16.dp))

            MealType.entries.forEachIndexed { index, mealType ->
                val isSelected = index == 0 // Breakfast selected
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) mealType.color.copy(alpha = 0.15f) else Color.Transparent,
                    border = BorderStroke(1.dp, if (isSelected) mealType.color else RestockColors.Muted.copy(alpha = 0.3f)),
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(mealType.emoji)
                        Spacer(Modifier.width(12.dp))
                        Text(mealType.displayName)
                        Spacer(Modifier.weight(1f))
                        if (isSelected) {
                            Icon(Icons.Default.Check, null, tint = mealType.color)
                        }
                    }
                }
            }
        }
    }
}
```

## MealsScreen

```kotlin
package com.restock.feature.meals.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.restock.core.domain.model.CurrentListItem
import com.restock.core.domain.model.MealType
import com.restock.core.ui.components.MealTagChip
import com.restock.core.ui.theme.RestockColors
import com.restock.core.ui.theme.RestockSpacing
import com.restock.feature.list.ui.components.GroceryItemCard

@Composable
fun MealsScreen(
    viewModel: MealsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = RestockSpacing.md),
    ) {
        // Breakfast section
        if (uiState.breakfastItems.isNotEmpty()) {
            item(key = "breakfast_header") {
                MealSectionHeader(
                    mealType = MealType.BREAKFAST,
                    count = uiState.breakfastItems.size,
                )
            }
            items(
                items = uiState.breakfastItems,
                key = { "breakfast_${it.id}" }
            ) { item ->
                MealItemCard(
                    item = item,
                    mealType = MealType.BREAKFAST,
                    onCheckedChange = { checked ->
                        viewModel.onAction(MealsAction.ToggleItem(item.id, checked))
                    },
                )
            }
        }

        // Lunch section
        if (uiState.lunchItems.isNotEmpty()) {
            item(key = "lunch_header") {
                MealSectionHeader(
                    mealType = MealType.LUNCH,
                    count = uiState.lunchItems.size,
                )
            }
            items(
                items = uiState.lunchItems,
                key = { "lunch_${it.id}" }
            ) { item ->
                MealItemCard(
                    item = item,
                    mealType = MealType.LUNCH,
                    onCheckedChange = { checked ->
                        viewModel.onAction(MealsAction.ToggleItem(item.id, checked))
                    },
                )
            }
        }

        // Dinner section
        if (uiState.dinnerItems.isNotEmpty()) {
            item(key = "dinner_header") {
                MealSectionHeader(
                    mealType = MealType.DINNER,
                    count = uiState.dinnerItems.size,
                )
            }
            items(
                items = uiState.dinnerItems,
                key = { "dinner_${it.id}" }
            ) { item ->
                MealItemCard(
                    item = item,
                    mealType = MealType.DINNER,
                    onCheckedChange = { checked ->
                        viewModel.onAction(MealsAction.ToggleItem(item.id, checked))
                    },
                )
            }
        }

        // Snack section
        if (uiState.snackItems.isNotEmpty()) {
            item(key = "snack_header") {
                MealSectionHeader(
                    mealType = MealType.SNACK,
                    count = uiState.snackItems.size,
                )
            }
            items(
                items = uiState.snackItems,
                key = { "snack_${it.id}" }
            ) { item ->
                MealItemCard(
                    item = item,
                    mealType = MealType.SNACK,
                    onCheckedChange = { checked ->
                        viewModel.onAction(MealsAction.ToggleItem(item.id, checked))
                    },
                )
            }
        }

        // Untagged section
        if (uiState.untaggedItems.isNotEmpty()) {
            item(key = "untagged_header") {
                UntaggedSectionHeader(count = uiState.untaggedItems.size)
            }
            items(
                items = uiState.untaggedItems,
                key = { "untagged_${it.id}" }
            ) { item ->
                MealItemCard(
                    item = item,
                    mealType = null,
                    onCheckedChange = { checked ->
                        viewModel.onAction(MealsAction.ToggleItem(item.id, checked))
                    },
                )
            }
        }
    }
}

@Composable
private fun MealSectionHeader(
    mealType: MealType,
    count: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = RestockSpacing.md,
                vertical = RestockSpacing.sm,
            ),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
    ) {
        Text(
            text = mealType.emoji,
            style = MaterialTheme.typography.titleMedium,
        )

        Spacer(Modifier.width(RestockSpacing.sm))

        Text(
            text = "${mealType.displayName} ($count)",
            style = MaterialTheme.typography.titleMedium,
            color = mealType.color,
        )
    }
}

@Composable
private fun UntaggedSectionHeader(
    count: Int,
    modifier: Modifier = Modifier,
) {
    Text(
        text = "Untagged ($count)",
        style = MaterialTheme.typography.titleMedium,
        color = RestockColors.Muted,
        modifier = modifier.padding(
            horizontal = RestockSpacing.md,
            vertical = RestockSpacing.sm,
        ),
    )
}

@Composable
private fun MealItemCard(
    item: CurrentListItem,
    mealType: MealType?,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Simplified version - can reuse GroceryItemCard
    GroceryItemCard(
        item = item,
        onCheckedChange = onCheckedChange,
        onDelete = { /* Not supported in meals view */ },
        onQuantityClick = { /* Not supported in meals view */ },
        mealTags = mealType?.let { listOf(it) } ?: emptyList(),
        modifier = modifier.padding(
            horizontal = RestockSpacing.md,
            vertical = RestockSpacing.xs,
        ),
    )
}
```

## Acceptance Criteria

- [ ] MealTagChip component with all 4 colors
- [ ] TagSelectionSheet with multi-select
- [ ] MealsScreen with grouped items
- [ ] Section headers with emoji and count
- [ ] Bottom navigation updated with Meals tab
- [ ] Tags display on GroceryItemCard
- [ ] All previews render correctly

## Definition of Done

- [ ] All components compile without errors
- [ ] Visual design matches spec
- [ ] Interactions smooth and responsive
- [ ] Accessibility supported
- [ ] Integration with ViewModels complete

## Notes

- Keep meal colors consistent throughout app
- Emoji adds personality but keep optional
- Section order: Breakfast → Lunch → Dinner → Snack → Untagged
