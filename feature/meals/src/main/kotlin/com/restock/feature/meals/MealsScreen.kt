package com.restock.feature.meals

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.restock.core.domain.model.CurrentListItem
import com.restock.core.domain.model.MealType
import com.restock.core.ui.components.MealTagChip
import com.restock.core.ui.components.RestockCheckbox
import com.restock.core.ui.preview.LightDarkPreview
import com.restock.core.ui.preview.RestockPreviewTheme
import com.restock.core.ui.theme.RestockColors
import com.restock.core.ui.theme.RestockElevation
import com.restock.core.ui.theme.RestockSpacing
import com.restock.feature.meals.ui.MealsAction
import com.restock.feature.meals.ui.MealsEvent
import com.restock.feature.meals.ui.MealsUiState
import com.restock.feature.meals.ui.MealsViewModel
import com.restock.feature.meals.ui.components.MealSectionHeader
import com.restock.feature.meals.ui.components.TagSelectionSheet
import com.restock.feature.meals.ui.components.UntaggedSectionHeader
import java.time.Instant
import kotlinx.collections.immutable.toImmutableList
import com.restock.core.ui.components.MealType as UiMealType

/**
 * Meals screen for viewing items grouped by meal type.
 *
 * Displays items organized by:
 * - Breakfast (🌅)
 * - Lunch (☀️)
 * - Dinner (🌙)
 * - Snack (✨)
 * - Untagged (items without meal tags)
 *
 * Features:
 * - Section headers with emoji and count
 * - Tap to check/uncheck items (marks as purchased)
 * - Long-press to open tag selection sheet
 * - Items with multiple tags appear in multiple sections
 *
 * @param viewModel The MealsViewModel for state management.
 * @param modifier Modifier to be applied to the screen.
 */
@Composable
fun MealsScreen(
    viewModel: MealsViewModel = hiltViewModel(),
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Handle events
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is MealsEvent.ItemChecked -> {
                    snackbarHostState.showSnackbar("${event.itemName} checked off")
                }
                is MealsEvent.TagAdded -> {
                    snackbarHostState.showSnackbar(
                        "${event.mealType.displayName} tag added to ${event.itemName}",
                    )
                }
                is MealsEvent.TagRemoved -> {
                    snackbarHostState.showSnackbar(
                        "${event.mealType.displayName} tag removed from ${event.itemName}",
                    )
                }
                is MealsEvent.Error -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    MealsScreenContent(
        uiState = uiState,
        onAction = viewModel::onAction,
        snackbarHostState = snackbarHostState,
        modifier = modifier,
    )
}

/**
 * Content of the meals screen.
 *
 * Separated from the stateful composable for preview and testing.
 */
@Composable
private fun MealsScreenContent(
    uiState: MealsUiState,
    onAction: (MealsAction) -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = RestockColors.Background,
    ) { paddingValues ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
        ) {
            when {
                uiState.isLoading -> {
                    // Loading state
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(color = RestockColors.Accent)
                    }
                }
                uiState.isEmpty -> {
                    // Empty state
                    MealsEmptyState(
                        modifier = Modifier.fillMaxSize(),
                    )
                }
                else -> {
                    // Content
                    MealsLazyColumn(
                        uiState = uiState,
                        onAction = onAction,
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .testTag("meals_list"),
                    )
                }
            }
        }

        // Tag selection sheet
        if (uiState.showTagSelectionSheet) {
            uiState.selectedItemForTagging?.let { item ->
                TagSelectionSheet(
                    itemName = item.name,
                    currentTags = uiState.selectedItemTags,
                    onTagToggle = { mealType ->
                        onAction(MealsAction.ToggleTag(mealType))
                    },
                    onDismiss = { onAction(MealsAction.DismissTagSelection) },
                )
            }
        }
    }
}

/**
 * LazyColumn displaying items grouped by meal type.
 *
 * Section order: Breakfast → Lunch → Dinner → Snack → Untagged
 */
@Composable
private fun MealsLazyColumn(
    uiState: MealsUiState,
    onAction: (MealsAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(vertical = RestockSpacing.md),
    ) {
        // Breakfast section
        if (uiState.hasBreakfastItems) {
            item(
                key = "breakfast_header",
                contentType = "header",
            ) {
                MealSectionHeader(
                    mealType = MealType.BREAKFAST,
                    count = uiState.breakfastCount,
                )
            }
            items(
                items = uiState.breakfastItems,
                key = { "breakfast_${it.id}" },
                contentType = { "meal_item" },
            ) { item ->
                MealItemCard(
                    item = item,
                    mealType = MealType.BREAKFAST,
                    onCheckedChange = { checked ->
                        onAction(MealsAction.ToggleItem(item.id, checked))
                    },
                    onLongClick = {
                        onAction(MealsAction.SelectItemForTagging(item))
                    },
                )
            }
        }

        // Lunch section
        if (uiState.hasLunchItems) {
            item(
                key = "lunch_header",
                contentType = "header",
            ) {
                MealSectionHeader(
                    mealType = MealType.LUNCH,
                    count = uiState.lunchCount,
                )
            }
            items(
                items = uiState.lunchItems,
                key = { "lunch_${it.id}" },
                contentType = { "meal_item" },
            ) { item ->
                MealItemCard(
                    item = item,
                    mealType = MealType.LUNCH,
                    onCheckedChange = { checked ->
                        onAction(MealsAction.ToggleItem(item.id, checked))
                    },
                    onLongClick = {
                        onAction(MealsAction.SelectItemForTagging(item))
                    },
                )
            }
        }

        // Dinner section
        if (uiState.hasDinnerItems) {
            item(
                key = "dinner_header",
                contentType = "header",
            ) {
                MealSectionHeader(
                    mealType = MealType.DINNER,
                    count = uiState.dinnerCount,
                )
            }
            items(
                items = uiState.dinnerItems,
                key = { "dinner_${it.id}" },
                contentType = { "meal_item" },
            ) { item ->
                MealItemCard(
                    item = item,
                    mealType = MealType.DINNER,
                    onCheckedChange = { checked ->
                        onAction(MealsAction.ToggleItem(item.id, checked))
                    },
                    onLongClick = {
                        onAction(MealsAction.SelectItemForTagging(item))
                    },
                )
            }
        }

        // Snack section
        if (uiState.hasSnackItems) {
            item(
                key = "snack_header",
                contentType = "header",
            ) {
                MealSectionHeader(
                    mealType = MealType.SNACK,
                    count = uiState.snackCount,
                )
            }
            items(
                items = uiState.snackItems,
                key = { "snack_${it.id}" },
                contentType = { "meal_item" },
            ) { item ->
                MealItemCard(
                    item = item,
                    mealType = MealType.SNACK,
                    onCheckedChange = { checked ->
                        onAction(MealsAction.ToggleItem(item.id, checked))
                    },
                    onLongClick = {
                        onAction(MealsAction.SelectItemForTagging(item))
                    },
                )
            }
        }

        // Untagged section
        if (uiState.hasUntaggedItems) {
            item(
                key = "untagged_header",
                contentType = "header",
            ) {
                UntaggedSectionHeader(count = uiState.untaggedCount)
            }
            items(
                items = uiState.untaggedItems,
                key = { "untagged_${it.id}" },
                contentType = { "meal_item" },
            ) { item ->
                MealItemCard(
                    item = item,
                    mealType = null,
                    onCheckedChange = { checked ->
                        onAction(MealsAction.ToggleItem(item.id, checked))
                    },
                    onLongClick = {
                        onAction(MealsAction.SelectItemForTagging(item))
                    },
                )
            }
        }
    }
}

/**
 * Individual item card in the meals view.
 *
 * Shows item name, checkbox, and optional meal tag chip.
 * Supports tap to check/uncheck and long-press for tag editing.
 *
 * @param item The list item to display.
 * @param mealType The meal type section this item belongs to (null for untagged).
 * @param onCheckedChange Called when the checkbox is toggled.
 * @param onLongClick Called when the item is long-pressed.
 * @param modifier Modifier to be applied to the card.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MealItemCard(
    item: CurrentListItem,
    mealType: MealType?,
    onCheckedChange: (Boolean) -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val hapticFeedback = LocalHapticFeedback.current

    Surface(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(
                    horizontal = RestockSpacing.md,
                    vertical = RestockSpacing.xs,
                ),
        shape = MaterialTheme.shapes.medium,
        color = RestockColors.Card,
        shadowElevation = RestockElevation.level1,
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .combinedClickable(
                        onClick = { onCheckedChange(!item.isChecked) },
                        onLongClick = {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            onLongClick()
                        },
                    )
                    .padding(
                        horizontal = RestockSpacing.cardPaddingHorizontal,
                        vertical = RestockSpacing.cardPaddingVertical,
                    ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Checkbox
            RestockCheckbox(
                checked = item.isChecked,
                onCheckedChange = onCheckedChange,
                contentDescription =
                    if (item.isChecked) {
                        "Uncheck ${item.name}"
                    } else {
                        "Check ${item.name}"
                    },
            )

            Spacer(modifier = Modifier.width(RestockSpacing.xs))

            // Item name and meal tag
            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (item.isChecked) RestockColors.Muted else RestockColors.Primary,
                    textDecoration = if (item.isChecked) TextDecoration.LineThrough else null,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                // Show meal tag chip
                if (mealType != null) {
                    Spacer(Modifier.height(RestockSpacing.xs))
                    val uiMealType =
                        when (mealType) {
                            MealType.BREAKFAST -> UiMealType.BREAKFAST
                            MealType.LUNCH -> UiMealType.LUNCH
                            MealType.DINNER -> UiMealType.DINNER
                            MealType.SNACK -> UiMealType.SNACK
                        }
                    MealTagChip(
                        mealType = uiMealType,
                        showEmoji = false,
                    )
                }
            }

            // Quantity badge (if > 1)
            if (item.quantity > 1) {
                Spacer(modifier = Modifier.width(RestockSpacing.sm))
                QuantityBadge(
                    quantity = item.quantity,
                    unit = item.unit,
                    isMuted = item.isChecked,
                )
            }
        }
    }
}

/**
 * Badge displaying the quantity and optional unit.
 *
 * @param quantity The item quantity.
 * @param unit Optional unit string (e.g., "lb", "oz").
 * @param isMuted Whether to show in muted colors.
 * @param modifier Modifier to be applied.
 */
@Composable
private fun QuantityBadge(
    quantity: Int,
    unit: String?,
    modifier: Modifier = Modifier,
    isMuted: Boolean = false,
) {
    val backgroundColor =
        if (isMuted) {
            RestockColors.Muted.copy(alpha = 0.15f)
        } else {
            RestockColors.Accent.copy(alpha = 0.15f)
        }

    val textColor = if (isMuted) RestockColors.Muted else RestockColors.Accent
    val displayText = if (unit != null) "$quantity $unit" else "×$quantity"

    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = backgroundColor,
    ) {
        Text(
            text = displayText,
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            modifier =
                Modifier.padding(
                    horizontal = RestockSpacing.sm,
                    vertical = RestockSpacing.xs,
                ),
        )
    }
}

/**
 * Empty state shown when no items are available.
 */
@Composable
private fun MealsEmptyState(modifier: Modifier = Modifier) {
    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(RestockColors.Background),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "🍽️",
                style = MaterialTheme.typography.displayLarge,
            )
            Spacer(Modifier.height(RestockSpacing.md))
            Text(
                text = "No items to show",
                style = MaterialTheme.typography.titleMedium,
                color = RestockColors.Primary,
            )
            Spacer(Modifier.height(RestockSpacing.xs))
            Text(
                text = "Add items to your list and tag them with meals",
                style = MaterialTheme.typography.bodyMedium,
                color = RestockColors.Muted,
            )
        }
    }
}

// ==================== PREVIEWS ====================

@LightDarkPreview
@Composable
private fun MealsScreenContentPreview() {
    val sampleState =
        MealsUiState(
            breakfastItems =
                listOf(
                    sampleItem("1", "Eggs", 12),
                    sampleItem("2", "Milk"),
                    sampleItem("3", "Cereal"),
                ).toImmutableList(),
            lunchItems =
                listOf(
                    sampleItem("4", "Bread"),
                    sampleItem("5", "Deli Meat", 2, "lb"),
                ).toImmutableList(),
            dinnerItems =
                listOf(
                    sampleItem("6", "Chicken Breast", 2, "lb"),
                ).toImmutableList(),
            snackItems =
                listOf(
                    sampleItem("7", "Chips"),
                    sampleItem("8", "Cookies"),
                ).toImmutableList(),
            untaggedItems =
                listOf(
                    sampleItem("9", "Paper Towels"),
                    sampleItem("10", "Soap"),
                ).toImmutableList(),
            isLoading = false,
        )

    RestockPreviewTheme {
        MealsScreenContent(
            uiState = sampleState,
            onAction = {},
            snackbarHostState = remember { SnackbarHostState() },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MealsScreenEmptyPreview() {
    RestockPreviewTheme {
        MealsScreenContent(
            uiState = MealsUiState(isLoading = false),
            onAction = {},
            snackbarHostState = remember { SnackbarHostState() },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MealsScreenLoadingPreview() {
    RestockPreviewTheme {
        MealsScreenContent(
            uiState = MealsUiState(isLoading = true),
            onAction = {},
            snackbarHostState = remember { SnackbarHostState() },
        )
    }
}

@LightDarkPreview
@Composable
private fun MealItemCardPreview() {
    RestockPreviewTheme {
        Surface(color = RestockColors.Background) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                MealItemCard(
                    item = sampleItem("1", "Eggs", 12),
                    mealType = MealType.BREAKFAST,
                    onCheckedChange = {},
                    onLongClick = {},
                )
                MealItemCard(
                    item = sampleItem("2", "Chicken Breast", 2, "lb"),
                    mealType = MealType.DINNER,
                    onCheckedChange = {},
                    onLongClick = {},
                )
                MealItemCard(
                    item = sampleItem("3", "Paper Towels"),
                    mealType = null,
                    onCheckedChange = {},
                    onLongClick = {},
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MealsEmptyStatePreview() {
    RestockPreviewTheme {
        MealsEmptyState()
    }
}

/**
 * Helper function to create sample items for previews.
 */
private fun sampleItem(
    id: String,
    name: String,
    quantity: Int = 1,
    unit: String? = null,
): CurrentListItem =
    CurrentListItem(
        id = id,
        itemId = "item_$id",
        name = name,
        normalizedName = name.lowercase(),
        quantity = quantity,
        unit = unit,
        addedAt = Instant.now(),
        position = id.toInt(),
        isChecked = false,
    )
