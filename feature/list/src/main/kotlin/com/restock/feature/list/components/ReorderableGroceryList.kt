package com.restock.feature.list.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.zIndex
import com.restock.core.domain.model.CurrentListItem
import com.restock.core.ui.components.MealType
import com.restock.core.ui.preview.LightDarkPreview
import com.restock.core.ui.preview.RestockPreviewTheme
import com.restock.core.ui.theme.RestockColors
import com.restock.core.ui.theme.RestockSpacing
import java.time.Instant
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch

/**
 * Animation specification for drag elevation transitions.
 */
private val DRAG_ELEVATION_SPEC =
    spring<Float>(
        stiffness = Spring.StiffnessHigh,
    )

/**
 * Elevation applied to an item while being dragged.
 */
private const val DRAGGING_ELEVATION = 8f

/**
 * Default elevation for items not being dragged.
 */
private const val DEFAULT_ELEVATION = 0f

/**
 * Scale factor applied to the dragged item for visual lift effect.
 */
private const val DRAGGING_SCALE = 1.02f

/**
 * Default scale for items not being dragged.
 */
private const val DEFAULT_SCALE = 1.0f

/**
 * A reorderable grocery list that supports drag-and-drop reordering.
 *
 * This component wraps a LazyColumn with drag gesture detection to allow
 * users to reorder items by long-pressing and dragging. It provides:
 *
 * - **Long-press to initiate drag**: User holds on an item to start dragging
 * - **Visual lift effect**: Dragged item gains elevation and slight scale
 * - **Haptic feedback**: Feedback on drag start and when crossing item boundaries
 * - **Smooth animations**: Items animate to new positions during drag
 * - **Auto-scroll**: List scrolls when dragging near edges
 *
 * **Usage:**
 * ```kotlin
 * ReorderableGroceryList(
 *     items = uncheckedItems,
 *     onReorder = { fromIndex, toIndex ->
 *         viewModel.onAction(ListAction.ReorderItems(fromIndex, toIndex))
 *     },
 *     onItemCheckedChange = { id, checked -> ... },
 *     onItemDelete = { id -> ... },
 *     onItemQuantityClick = { item -> ... },
 * )
 * ```
 *
 * @param items The list of grocery items to display.
 * @param onReorder Called when an item is dropped at a new position.
 * @param onItemCheckedChange Called when an item's checkbox is toggled.
 * @param onItemDelete Called when an item is swiped to delete.
 * @param onItemQuantityClick Called when an item is long-pressed (outside drag).
 * @param modifier Modifier to be applied to the list.
 * @param contentPadding Padding around the list content.
 * @param getMealTags Function to retrieve meal tags for an item.
 */
@Composable
fun ReorderableGroceryList(
    items: ImmutableList<CurrentListItem>,
    onReorder: (fromIndex: Int, toIndex: Int) -> Unit,
    onItemCheckedChange: (String, Boolean) -> Unit,
    onItemDelete: (String) -> Unit,
    onItemQuantityClick: (CurrentListItem) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues =
        PaddingValues(
            horizontal = RestockSpacing.screenPadding,
            vertical = RestockSpacing.sm,
        ),
    getMealTags: (CurrentListItem) -> List<MealType> = { emptyList() },
) {
    val hapticFeedback = LocalHapticFeedback.current
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // Drag state
    var draggedItemIndex by remember { mutableStateOf<Int?>(null) }
    var draggedOverItemIndex by remember { mutableIntStateOf(-1) }
    var dragOffset by remember { mutableFloatStateOf(0f) }
    var initialDragOffset by remember { mutableFloatStateOf(0f) }

    // Track last haptic index to avoid repeated feedback
    var lastHapticIndex by remember { mutableIntStateOf(-1) }

    // Calculate current target index during drag
    val currentTargetIndex =
        remember(draggedItemIndex, draggedOverItemIndex) {
            draggedOverItemIndex.takeIf { it >= 0 } ?: draggedItemIndex
        }

    LazyColumn(
        state = listState,
        modifier = modifier,
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(RestockSpacing.listItemGap),
    ) {
        itemsIndexed(
            items = items,
            key = { _, item -> item.id },
        ) { index, item ->
            val isDragging = draggedItemIndex == index

            // Animation values for smooth transitions
            val elevation = remember { Animatable(DEFAULT_ELEVATION) }
            val scale = remember { Animatable(DEFAULT_SCALE) }

            // Animate elevation and scale based on drag state
            LaunchedEffect(isDragging) {
                if (isDragging) {
                    launch { elevation.animateTo(DRAGGING_ELEVATION, DRAG_ELEVATION_SPEC) }
                    launch { scale.animateTo(DRAGGING_SCALE, DRAG_ELEVATION_SPEC) }
                } else {
                    launch { elevation.animateTo(DEFAULT_ELEVATION, DRAG_ELEVATION_SPEC) }
                    launch { scale.animateTo(DEFAULT_SCALE, DRAG_ELEVATION_SPEC) }
                }
            }

            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .zIndex(if (isDragging) 1f else 0f)
                        .graphicsLayer {
                            if (isDragging) {
                                translationY = dragOffset
                                shadowElevation = elevation.value
                                scaleX = scale.value
                                scaleY = scale.value
                            }
                        }
                        .pointerInput(items) {
                            detectDragGesturesAfterLongPress(
                                onDragStart = { offset ->
                                    // Initiate drag
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                    draggedItemIndex = index
                                    initialDragOffset = 0f
                                    dragOffset = 0f
                                    lastHapticIndex = index
                                    draggedOverItemIndex = index
                                },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    dragOffset += dragAmount.y

                                    // Calculate the target index based on drag position
                                    val currentItem =
                                        listState.layoutInfo.visibleItemsInfo
                                            .find { it.index == index }

                                    if (currentItem != null) {
                                        val centerY = currentItem.offset + currentItem.size / 2 + dragOffset
                                        val targetIndex =
                                            findTargetIndex(
                                                listState = listState,
                                                centerY = centerY.toInt(),
                                                currentIndex = index,
                                                totalItems = items.size,
                                            )

                                        if (targetIndex != draggedOverItemIndex && targetIndex >= 0) {
                                            draggedOverItemIndex = targetIndex

                                            // Haptic feedback when crossing item boundaries
                                            if (targetIndex != lastHapticIndex) {
                                                hapticFeedback.performHapticFeedback(
                                                    HapticFeedbackType.TextHandleMove,
                                                )
                                                lastHapticIndex = targetIndex
                                            }
                                        }

                                        // Auto-scroll when dragging near edges
                                        scope.launch {
                                            autoScrollIfNeeded(
                                                listState = listState,
                                                currentY = centerY.toInt(),
                                            )
                                        }
                                    }
                                },
                                onDragEnd = {
                                    // Complete the reorder
                                    val from = draggedItemIndex
                                    val to = draggedOverItemIndex

                                    if (from != null && to >= 0 && from != to) {
                                        onReorder(from, to)
                                    }

                                    // Reset drag state
                                    draggedItemIndex = null
                                    draggedOverItemIndex = -1
                                    dragOffset = 0f
                                    lastHapticIndex = -1
                                },
                                onDragCancel = {
                                    // Reset drag state on cancel
                                    draggedItemIndex = null
                                    draggedOverItemIndex = -1
                                    dragOffset = 0f
                                    lastHapticIndex = -1
                                },
                            )
                        }
                        .animateItem(),
            ) {
                GroceryItemCard(
                    item = item,
                    onCheckedChange = { checked -> onItemCheckedChange(item.id, checked) },
                    onDelete = { onItemDelete(item.id) },
                    onQuantityClick = { onItemQuantityClick(item) },
                    mealTags = getMealTags(item),
                )
            }
        }
    }
}

/**
 * Finds the target index for the dragged item based on its current vertical position.
 *
 * @param listState The LazyListState for accessing visible items.
 * @param centerY The center Y position of the dragged item.
 * @param currentIndex The current index of the dragged item.
 * @param totalItems Total number of items in the list.
 * @return The target index where the item should be dropped.
 */
private fun findTargetIndex(
    listState: LazyListState,
    centerY: Int,
    currentIndex: Int,
    totalItems: Int,
): Int {
    val visibleItems = listState.layoutInfo.visibleItemsInfo

    // Find which item's bounds contain the center point
    for (itemInfo in visibleItems) {
        val itemTop = itemInfo.offset
        val itemBottom = itemInfo.offset + itemInfo.size

        if (centerY in itemTop..itemBottom) {
            return itemInfo.index.coerceIn(0, totalItems - 1)
        }
    }

    // If above all items, return first index
    visibleItems.firstOrNull()?.let { first ->
        if (centerY < first.offset) {
            return 0
        }
    }

    // If below all items, return last index
    visibleItems.lastOrNull()?.let { last ->
        if (centerY > last.offset + last.size) {
            return (totalItems - 1).coerceAtLeast(0)
        }
    }

    return currentIndex
}

/**
 * Auto-scrolls the list when dragging near the top or bottom edges.
 *
 * @param listState The LazyListState for scrolling.
 * @param currentY The current Y position of the dragged item center.
 */
private suspend fun autoScrollIfNeeded(
    listState: LazyListState,
    currentY: Int,
) {
    val viewportStart = listState.layoutInfo.viewportStartOffset
    val viewportEnd = listState.layoutInfo.viewportEndOffset
    val viewportHeight = viewportEnd - viewportStart

    // Auto-scroll threshold (20% of viewport)
    val scrollThreshold = viewportHeight * 0.2f

    // Calculate scroll amount based on distance from edge
    val scrollAmount =
        when {
            currentY < viewportStart + scrollThreshold -> {
                // Near top - scroll up
                val distance = viewportStart + scrollThreshold - currentY
                (-distance / scrollThreshold * 50).toInt().coerceIn(-100, 0)
            }
            currentY > viewportEnd - scrollThreshold -> {
                // Near bottom - scroll down
                val distance = currentY - (viewportEnd - scrollThreshold)
                (distance / scrollThreshold * 50).toInt().coerceIn(0, 100)
            }
            else -> 0
        }

    if (scrollAmount != 0) {
        listState.scrollBy(scrollAmount.toFloat())
    }
}

/**
 * Extension function to scroll by a specific amount.
 */
private suspend fun LazyListState.scrollBy(amount: Float) {
    val currentOffset = firstVisibleItemScrollOffset
    scroll {
        scrollBy(amount)
    }
}

// ==================== PREVIEWS ====================

private val sampleItems =
    listOf(
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
        CurrentListItem(
            id = "4",
            itemId = "item4",
            name = "Butter",
            normalizedName = "butter",
            quantity = 1,
            addedAt = Instant.now(),
            position = 3,
            isChecked = false,
        ),
        CurrentListItem(
            id = "5",
            itemId = "item5",
            name = "Cheese",
            normalizedName = "cheese",
            quantity = 2,
            unit = "lb",
            addedAt = Instant.now(),
            position = 4,
            isChecked = false,
        ),
    ).toImmutableList()

@LightDarkPreview
@Composable
private fun ReorderableGroceryListPreview() {
    RestockPreviewTheme {
        Surface(color = RestockColors.Background) {
            ReorderableGroceryList(
                items = sampleItems,
                onReorder = { _, _ -> },
                onItemCheckedChange = { _, _ -> },
                onItemDelete = { },
                onItemQuantityClick = { },
            )
        }
    }
}

@LightDarkPreview
@Composable
private fun ReorderableGroceryListEmptyPreview() {
    RestockPreviewTheme {
        Surface(color = RestockColors.Background) {
            ReorderableGroceryList(
                items = persistentListOf(),
                onReorder = { _, _ -> },
                onItemCheckedChange = { _, _ -> },
                onItemDelete = { },
                onItemQuantityClick = { },
                modifier = Modifier.padding(RestockSpacing.md),
            )
        }
    }
}

@LightDarkPreview
@Composable
private fun ReorderableGroceryListWithTagsPreview() {
    RestockPreviewTheme {
        Surface(color = RestockColors.Background) {
            ReorderableGroceryList(
                items = sampleItems,
                onReorder = { _, _ -> },
                onItemCheckedChange = { _, _ -> },
                onItemDelete = { },
                onItemQuantityClick = { },
                getMealTags = { item ->
                    when (item.id) {
                        "1" -> listOf(MealType.BREAKFAST)
                        "2" -> listOf(MealType.BREAKFAST, MealType.LUNCH)
                        "4" -> listOf(MealType.DINNER)
                        else -> emptyList()
                    }
                },
            )
        }
    }
}
