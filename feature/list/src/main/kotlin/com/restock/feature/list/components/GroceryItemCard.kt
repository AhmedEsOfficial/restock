package com.restock.feature.list.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import com.restock.core.domain.model.CurrentListItem
import com.restock.core.ui.components.MealTagChip
import com.restock.core.ui.components.MealType
import com.restock.core.ui.components.RestockCheckbox
import com.restock.core.ui.preview.LightDarkPreview
import com.restock.core.ui.preview.RestockPreviewTheme
import com.restock.core.ui.theme.RestockColors
import com.restock.core.ui.theme.RestockElevation
import com.restock.core.ui.theme.RestockSpacing
import java.time.Instant

/**
 * Animation duration for card elevation changes.
 */
private const val ELEVATION_ANIMATION_DURATION_MS = 150

/**
 * Animation duration for color transitions.
 */
private const val COLOR_ANIMATION_DURATION_MS = 200

/**
 * Swipe threshold percentage for triggering delete action.
 */
private const val SWIPE_THRESHOLD_PERCENT = 0.4f

/**
 * Card component for displaying a grocery list item.
 *
 * Features:
 * - Checkbox for marking items as checked/unchecked
 * - Swipe-to-delete with animated background and haptic feedback
 * - Long-press for quantity adjustment with haptic feedback
 * - Quantity badge display when quantity > 1
 * - Visual distinction for checked items (muted, strikethrough)
 * - Meal tag chips display (max 2 shown)
 *
 * **Design Specifications:**
 * - Padding: 12dp horizontal, 14dp vertical
 * - Min height: 56dp (touch target compliance)
 * - Corner radius: 14dp
 * - Elevation: 1dp (default), 4dp (swiping)
 * - Background: White (#FFFFFF)
 * - Checked item: 60% opacity, muted text with strikethrough
 * - Delete background: Error color with white delete icon
 * - Swipe threshold: 40% of card width
 * - Haptic feedback on threshold reached and long-press
 *
 * @param item The list item to display.
 * @param onCheckedChange Called when the checkbox is toggled.
 * @param onDelete Called when the item is swiped to delete.
 * @param onQuantityClick Called when the user long-presses for quantity adjustment.
 * @param modifier Modifier to be applied to the card.
 * @param isCheckedSection Whether this item is displayed in the checked/In Cart section.
 * @param mealTags List of meal types to display as chips below the item name.
 */
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
    var isBeingDismissed by remember { mutableStateOf(false) }
    var hasTriggeredHaptic by remember { mutableStateOf(false) }

    val dismissState =
        rememberSwipeToDismissBoxState(
            confirmValueChange = { value ->
                if (value == SwipeToDismissBoxValue.EndToStart) {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    isBeingDismissed = true
                    onDelete()
                    true
                } else {
                    false
                }
            },
            positionalThreshold = { totalDistance -> totalDistance * SWIPE_THRESHOLD_PERCENT },
        )

    // Trigger haptic feedback when crossing the threshold
    LaunchedEffect(dismissState.targetValue) {
        if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart && !hasTriggeredHaptic) {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
            hasTriggeredHaptic = true
        } else if (dismissState.targetValue == SwipeToDismissBoxValue.Settled) {
            hasTriggeredHaptic = false
        }
    }

    // Reset dismiss state when item changes
    LaunchedEffect(item.id) {
        isBeingDismissed = false
        hasTriggeredHaptic = false
    }

    // Animate elevation based on swipe progress
    val elevation by animateDpAsState(
        targetValue =
            when {
                dismissState.progress > 0f && dismissState.progress < 1f -> RestockElevation.level2
                else -> RestockElevation.level1
            },
        animationSpec = tween(durationMillis = ELEVATION_ANIMATION_DURATION_MS),
        label = "card_elevation",
    )

    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier.fillMaxWidth(),
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true,
        backgroundContent = {
            DeleteSwipeBackground(
                dismissState = dismissState,
                modifier = Modifier.fillMaxSize(),
            )
        },
    ) {
        GroceryItemCardContent(
            item = item,
            onCheckedChange = onCheckedChange,
            onQuantityClick = {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                onQuantityClick()
            },
            isCheckedSection = isCheckedSection,
            elevation = elevation,
            mealTags = mealTags,
        )
    }
}

/**
 * The actual content of the grocery item card.
 *
 * Separated from the swipe container for better composition and reusability.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun GroceryItemCardContent(
    item: CurrentListItem,
    onCheckedChange: (Boolean) -> Unit,
    onQuantityClick: () -> Unit,
    isCheckedSection: Boolean,
    elevation: androidx.compose.ui.unit.Dp,
    mealTags: List<MealType>,
    modifier: Modifier = Modifier,
) {
    // Animate card appearance when checked/unchecked
    val contentAlpha by animateFloatAsState(
        targetValue = if (isCheckedSection) 0.6f else 1f,
        animationSpec = tween(durationMillis = COLOR_ANIMATION_DURATION_MS),
        label = "contentAlpha",
    )

    // Animate text color based on checked state
    val textColor by animateColorAsState(
        targetValue =
            if (item.isChecked || isCheckedSection) {
                RestockColors.Muted
            } else {
                RestockColors.Primary
            },
        animationSpec = tween(durationMillis = COLOR_ANIMATION_DURATION_MS),
        label = "text_color",
    )

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = RestockColors.Card,
        shadowElevation = elevation,
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .combinedClickable(
                        onClick = { onCheckedChange(!item.isChecked) },
                        onLongClick = onQuantityClick,
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

            // Item name and meal tags
            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = textColor,
                    textDecoration =
                        if (item.isChecked || isCheckedSection) {
                            TextDecoration.LineThrough
                        } else {
                            null
                        },
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                // Meal tags (show max 2)
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

            // Quantity badge (if > 1)
            if (item.quantity > 1) {
                Spacer(modifier = Modifier.width(RestockSpacing.sm))
                QuantityBadge(
                    quantity = item.quantity,
                    unit = item.unit,
                    onClick = onQuantityClick,
                    isMuted = item.isChecked || isCheckedSection,
                )
            }
        }
    }
}

/**
 * Badge displaying the quantity and optional unit.
 *
 * **Specifications:**
 * - Background: Accent @ 15% opacity (muted when checked)
 * - Text: Accent color (muted when checked)
 * - Format: "×2" (no unit) or "2 lbs" (with unit)
 * - Clickable to open quantity adjustment
 *
 * @param quantity The item quantity.
 * @param unit Optional unit string (e.g., "lb", "oz").
 * @param onClick Called when the badge is clicked.
 * @param isMuted Whether to show the badge in muted colors (for checked items).
 * @param modifier Modifier to be applied to the badge.
 */
@Composable
fun QuantityBadge(
    quantity: Int,
    unit: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isMuted: Boolean = false,
) {
    val backgroundColor by animateColorAsState(
        targetValue =
            if (isMuted) {
                RestockColors.Muted.copy(alpha = 0.15f)
            } else {
                RestockColors.Accent.copy(alpha = 0.15f)
            },
        animationSpec = tween(durationMillis = COLOR_ANIMATION_DURATION_MS),
        label = "badge_background",
    )

    val textColor by animateColorAsState(
        targetValue =
            if (isMuted) {
                RestockColors.Muted
            } else {
                RestockColors.Accent
            },
        animationSpec = tween(durationMillis = COLOR_ANIMATION_DURATION_MS),
        label = "badge_text_color",
    )

    // Format: "×2" or "2 lbs"
    val displayText = if (unit != null) "$quantity $unit" else "×$quantity"

    Surface(
        onClick = onClick,
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
 * Background shown when swiping to delete.
 *
 * Shows a red background with a delete icon that animates based on swipe progress.
 * The icon scales up when the threshold is reached.
 *
 * @param dismissState The swipe-to-dismiss state for tracking progress.
 * @param modifier Modifier to be applied to the background.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DeleteSwipeBackground(
    dismissState: androidx.compose.material3.SwipeToDismissBoxState,
    modifier: Modifier = Modifier,
) {
    // Animate background color based on target value (when threshold is crossed)
    val backgroundColor by animateColorAsState(
        targetValue =
            when (dismissState.targetValue) {
                SwipeToDismissBoxValue.EndToStart -> RestockColors.Error
                else -> Color.Transparent
            },
        animationSpec = tween(durationMillis = COLOR_ANIMATION_DURATION_MS),
        label = "delete_background",
    )

    // Animate icon scale based on target value
    val iconScale by animateFloatAsState(
        targetValue =
            when (dismissState.targetValue) {
                SwipeToDismissBoxValue.EndToStart -> 1f
                else -> 0.5f
            },
        animationSpec = tween(durationMillis = COLOR_ANIMATION_DURATION_MS),
        label = "delete_icon_scale",
    )

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(
                    color = backgroundColor,
                    shape = MaterialTheme.shapes.medium,
                )
                .padding(horizontal = RestockSpacing.lg),
        contentAlignment = Alignment.CenterEnd,
    ) {
        Icon(
            imageVector = Icons.Default.Delete,
            contentDescription = "Delete item",
            tint = Color.White,
            modifier = Modifier.scale(iconScale),
        )
    }
}

// ==================== PREVIEWS ====================

@LightDarkPreview
@Composable
private fun GroceryItemCardUncheckedPreview() {
    RestockPreviewTheme {
        Surface(color = RestockColors.Background) {
            GroceryItemCard(
                item =
                    CurrentListItem(
                        id = "1",
                        itemId = "item1",
                        name = "Milk",
                        normalizedName = "milk",
                        quantity = 1,
                        addedAt = Instant.now(),
                        position = 0,
                        isChecked = false,
                    ),
                onCheckedChange = {},
                onDelete = {},
                onQuantityClick = {},
                modifier = Modifier.padding(RestockSpacing.md),
            )
        }
    }
}

@LightDarkPreview
@Composable
private fun GroceryItemCardCheckedPreview() {
    RestockPreviewTheme {
        Surface(color = RestockColors.Background) {
            GroceryItemCard(
                item =
                    CurrentListItem(
                        id = "1",
                        itemId = "item1",
                        name = "Milk",
                        normalizedName = "milk",
                        quantity = 1,
                        addedAt = Instant.now(),
                        checkedAt = Instant.now(),
                        position = 0,
                        isChecked = true,
                    ),
                onCheckedChange = {},
                onDelete = {},
                onQuantityClick = {},
                isCheckedSection = true,
                modifier = Modifier.padding(RestockSpacing.md),
            )
        }
    }
}

@LightDarkPreview
@Composable
private fun GroceryItemCardWithQuantityPreview() {
    RestockPreviewTheme {
        Surface(color = RestockColors.Background) {
            GroceryItemCard(
                item =
                    CurrentListItem(
                        id = "1",
                        itemId = "item1",
                        name = "Eggs",
                        normalizedName = "eggs",
                        quantity = 2,
                        addedAt = Instant.now(),
                        position = 0,
                        isChecked = false,
                    ),
                onCheckedChange = {},
                onDelete = {},
                onQuantityClick = {},
                modifier = Modifier.padding(RestockSpacing.md),
            )
        }
    }
}

@LightDarkPreview
@Composable
private fun GroceryItemCardWithUnitPreview() {
    RestockPreviewTheme {
        Surface(color = RestockColors.Background) {
            GroceryItemCard(
                item =
                    CurrentListItem(
                        id = "1",
                        itemId = "item1",
                        name = "Chicken Breast",
                        normalizedName = "chicken breast",
                        quantity = 2,
                        unit = "lb",
                        addedAt = Instant.now(),
                        position = 0,
                        isChecked = false,
                    ),
                onCheckedChange = {},
                onDelete = {},
                onQuantityClick = {},
                modifier = Modifier.padding(RestockSpacing.md),
            )
        }
    }
}

@LightDarkPreview
@Composable
private fun GroceryItemCardLongNamePreview() {
    RestockPreviewTheme {
        Surface(color = RestockColors.Background) {
            GroceryItemCard(
                item =
                    CurrentListItem(
                        id = "1",
                        itemId = "item1",
                        name = "Organic Free-Range Chicken Breast Boneless Skinless",
                        normalizedName = "organic free-range chicken breast boneless skinless",
                        quantity = 3,
                        unit = "lb",
                        addedAt = Instant.now(),
                        position = 0,
                        isChecked = false,
                    ),
                onCheckedChange = {},
                onDelete = {},
                onQuantityClick = {},
                modifier = Modifier.padding(RestockSpacing.md),
            )
        }
    }
}

@LightDarkPreview
@Composable
private fun GroceryItemCardWithTagsPreview() {
    RestockPreviewTheme {
        Surface(color = RestockColors.Background) {
            GroceryItemCard(
                item =
                    CurrentListItem(
                        id = "1",
                        itemId = "item1",
                        name = "Eggs",
                        normalizedName = "eggs",
                        quantity = 12,
                        addedAt = Instant.now(),
                        position = 0,
                        isChecked = false,
                    ),
                onCheckedChange = {},
                onDelete = {},
                onQuantityClick = {},
                mealTags = listOf(MealType.BREAKFAST),
                modifier = Modifier.padding(RestockSpacing.md),
            )
        }
    }
}

@LightDarkPreview
@Composable
private fun GroceryItemCardWithMultipleTagsPreview() {
    RestockPreviewTheme {
        Surface(color = RestockColors.Background) {
            GroceryItemCard(
                item =
                    CurrentListItem(
                        id = "1",
                        itemId = "item1",
                        name = "Chicken Breast",
                        normalizedName = "chicken breast",
                        quantity = 2,
                        unit = "lb",
                        addedAt = Instant.now(),
                        position = 0,
                        isChecked = false,
                    ),
                onCheckedChange = {},
                onDelete = {},
                onQuantityClick = {},
                mealTags = listOf(MealType.LUNCH, MealType.DINNER),
                modifier = Modifier.padding(RestockSpacing.md),
            )
        }
    }
}

@LightDarkPreview
@Composable
private fun GroceryItemCardListPreview() {
    RestockPreviewTheme {
        Surface(color = RestockColors.Background) {
            Column(
                modifier = Modifier.padding(RestockSpacing.md),
                verticalArrangement = Arrangement.spacedBy(RestockSpacing.sm),
            ) {
                GroceryItemCard(
                    item =
                        CurrentListItem(
                            id = "1",
                            itemId = "item1",
                            name = "Milk",
                            normalizedName = "milk",
                            quantity = 1,
                            addedAt = Instant.now(),
                            position = 0,
                            isChecked = false,
                        ),
                    onCheckedChange = {},
                    onDelete = {},
                    onQuantityClick = {},
                )
                GroceryItemCard(
                    item =
                        CurrentListItem(
                            id = "2",
                            itemId = "item2",
                            name = "Eggs",
                            normalizedName = "eggs",
                            quantity = 12,
                            addedAt = Instant.now(),
                            position = 1,
                            isChecked = false,
                        ),
                    onCheckedChange = {},
                    onDelete = {},
                    onQuantityClick = {},
                    mealTags = listOf(MealType.BREAKFAST),
                )
                GroceryItemCard(
                    item =
                        CurrentListItem(
                            id = "3",
                            itemId = "item3",
                            name = "Bread",
                            normalizedName = "bread",
                            quantity = 1,
                            addedAt = Instant.now(),
                            position = 2,
                            isChecked = false,
                        ),
                    onCheckedChange = {},
                    onDelete = {},
                    onQuantityClick = {},
                )
            }
        }
    }
}
