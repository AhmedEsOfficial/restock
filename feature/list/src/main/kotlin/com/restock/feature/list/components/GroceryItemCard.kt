package com.restock.feature.list.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.restock.core.domain.model.CurrentListItem
import com.restock.core.ui.components.RestockCheckbox
import com.restock.core.ui.preview.LightDarkPreview
import com.restock.core.ui.preview.RestockPreviewTheme
import com.restock.core.ui.theme.RestockColors
import com.restock.core.ui.theme.RestockElevation
import com.restock.core.ui.theme.RestockShapeTokens
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
 * Card component for displaying a grocery list item.
 *
 * Features:
 * - Checkbox for marking items as checked/unchecked
 * - Swipe-to-delete with animated background
 * - Long-press for quantity adjustment
 * - Quantity badge display when quantity > 1
 * - Visual distinction for checked items (muted, strikethrough)
 *
 * **Design Specifications:**
 * - Corner radius: 14dp (medium)
 * - Elevation: 1dp (default), 2dp (swiping)
 * - Background: White (#FFFFFF)
 * - Checked item: Muted text with strikethrough
 * - Delete background: Error color
 *
 * @param item The list item to display.
 * @param onCheckedChange Called when the checkbox is toggled.
 * @param onDelete Called when the item is swiped to delete.
 * @param onQuantityClick Called when the user long-presses for quantity adjustment.
 * @param modifier Modifier to be applied to the card.
 * @param isCheckedSection Whether this item is displayed in the checked/In Cart section.
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
) {
    var isBeingDismissed by remember { mutableStateOf(false) }

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                isBeingDismissed = true
                onDelete()
                true
            } else {
                false
            }
        },
        positionalThreshold = { totalDistance -> totalDistance * 0.4f },
    )

    // Reset dismiss state when item changes
    LaunchedEffect(item.id) {
        isBeingDismissed = false
    }

    // Animate elevation based on swipe progress
    val elevation by animateDpAsState(
        targetValue = when {
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
                progress = dismissState.progress,
                modifier = Modifier.fillMaxSize(),
            )
        },
    ) {
        GroceryItemCardContent(
            item = item,
            onCheckedChange = onCheckedChange,
            onQuantityClick = onQuantityClick,
            isCheckedSection = isCheckedSection,
            elevation = elevation,
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
    modifier: Modifier = Modifier,
) {
    // Animate text color based on checked state
    val textColor by animateColorAsState(
        targetValue = if (item.isChecked || isCheckedSection) {
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
            modifier = Modifier
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
                contentDescription = if (item.isChecked) {
                    "Uncheck ${item.name}"
                } else {
                    "Check ${item.name}"
                },
            )

            Spacer(modifier = Modifier.width(RestockSpacing.sm))

            // Item name
            Text(
                text = item.name,
                style = MaterialTheme.typography.bodyLarge,
                color = textColor,
                textDecoration = if (item.isChecked || isCheckedSection) {
                    TextDecoration.LineThrough
                } else {
                    null
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )

            // Quantity badge (if > 1)
            item.quantityDisplay?.let { display ->
                Spacer(modifier = Modifier.width(RestockSpacing.sm))
                QuantityBadge(
                    display = display,
                    isMuted = item.isChecked || isCheckedSection,
                )
            }
        }
    }
}

/**
 * Badge displaying the quantity and optional unit.
 *
 * @param display The formatted quantity string (e.g., "×2" or "2 lb").
 * @param isMuted Whether to show the badge in muted colors (for checked items).
 * @param modifier Modifier to be applied to the badge.
 */
@Composable
private fun QuantityBadge(
    display: String,
    isMuted: Boolean,
    modifier: Modifier = Modifier,
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isMuted) {
            RestockColors.Muted.copy(alpha = 0.15f)
        } else {
            RestockColors.Accent.copy(alpha = 0.15f)
        },
        animationSpec = tween(durationMillis = COLOR_ANIMATION_DURATION_MS),
        label = "badge_background",
    )

    val textColor by animateColorAsState(
        targetValue = if (isMuted) {
            RestockColors.Muted
        } else {
            RestockColors.Accent
        },
        animationSpec = tween(durationMillis = COLOR_ANIMATION_DURATION_MS),
        label = "badge_text_color",
    )

    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = backgroundColor,
    ) {
        Text(
            text = display,
            style = MaterialTheme.typography.labelMedium,
            color = textColor,
            modifier = Modifier.padding(
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
 *
 * @param progress The current swipe progress (0 to 1).
 * @param modifier Modifier to be applied to the background.
 */
@Composable
private fun DeleteSwipeBackground(
    progress: Float,
    modifier: Modifier = Modifier,
) {
    // Animate background color based on progress
    val backgroundColor by animateColorAsState(
        targetValue = if (progress > 0f) {
            RestockColors.Error
        } else {
            Color.Transparent
        },
        animationSpec = tween(durationMillis = COLOR_ANIMATION_DURATION_MS),
        label = "delete_background",
    )

    // Animate icon alpha based on progress
    val iconAlpha = (progress * 2f).coerceIn(0f, 1f)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                color = backgroundColor,
                shape = MaterialTheme.shapes.medium,
            )
            .padding(end = RestockSpacing.lg),
        contentAlignment = Alignment.CenterEnd,
    ) {
        Icon(
            imageVector = Icons.Default.Delete,
            contentDescription = "Delete item",
            tint = RestockColors.OnError.copy(alpha = iconAlpha),
            modifier = Modifier.size(RestockSpacing.iconSize),
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
                item = CurrentListItem(
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
                item = CurrentListItem(
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
                item = CurrentListItem(
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
                item = CurrentListItem(
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
                item = CurrentListItem(
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
private fun GroceryItemCardListPreview() {
    RestockPreviewTheme {
        Surface(color = RestockColors.Background) {
            androidx.compose.foundation.layout.Column(
                modifier = Modifier.padding(RestockSpacing.md),
                verticalArrangement = Arrangement.spacedBy(RestockSpacing.sm),
            ) {
                GroceryItemCard(
                    item = CurrentListItem(
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
                    item = CurrentListItem(
                        id = "2",
                        itemId = "item2",
                        name = "Eggs",
                        normalizedName = "eggs",
                        quantity = 2,
                        addedAt = Instant.now(),
                        position = 1,
                        isChecked = false,
                    ),
                    onCheckedChange = {},
                    onDelete = {},
                    onQuantityClick = {},
                )
                GroceryItemCard(
                    item = CurrentListItem(
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
