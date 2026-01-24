package com.restock.feature.list

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.restock.core.domain.model.CurrentListItem
import com.restock.core.domain.model.GroceryItem
import com.restock.core.ui.components.AddItemTextField
import com.restock.core.ui.components.EmptyListState
import com.restock.core.ui.components.LoadingScreen
import com.restock.core.ui.components.RestockSnackbarHost
import com.restock.core.ui.preview.LightDarkPreview
import com.restock.core.ui.preview.RestockPreviewTheme
import com.restock.core.ui.theme.RestockColors
import com.restock.core.ui.theme.RestockSpacing
import com.restock.feature.list.components.AutoCompleteDropdown
import com.restock.feature.list.components.GroceryItemCard
import com.restock.feature.list.components.QuantityBottomSheet
import com.restock.feature.list.ui.ListAction
import com.restock.feature.list.ui.ListEvent
import com.restock.feature.list.ui.ListUiState
import com.restock.feature.list.ui.ListViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import java.time.Instant

/**
 * Main grocery list screen.
 *
 * This is the primary screen of the Restock app where users:
 * - View their current shopping list
 * - Add new items with autocomplete suggestions
 * - Check off items as they shop (moves to "In Cart" section)
 * - Swipe to delete items with undo
 * - Adjust quantities via long-press
 *
 * **Key UX Goals:**
 * - Time to first item added: < 15 seconds
 * - Add item interaction: < 2 taps
 * - Check item interaction: 1 tap
 * - List scroll performance: 60 fps
 *
 * @param viewModel The ViewModel managing list state and actions.
 * @param modifier Modifier to be applied to the screen.
 */
@Composable
fun ListScreen(
    viewModel: ListViewModel = hiltViewModel(),
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current

    // Handle one-shot events from ViewModel
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is ListEvent.ItemAdded -> {
                    snackbarHostState.showSnackbar(
                        message = "${event.name} added",
                        duration = SnackbarDuration.Short,
                    )
                }

                is ListEvent.ItemDeleted -> {
                    if (event.canUndo) {
                        val result = snackbarHostState.showSnackbar(
                            message = "${event.name} deleted",
                            actionLabel = "Undo",
                            duration = SnackbarDuration.Long,
                        )
                        if (result == SnackbarResult.ActionPerformed) {
                            viewModel.onAction(ListAction.UndoDelete)
                        }
                    } else {
                        snackbarHostState.showSnackbar(
                            message = "${event.name} deleted",
                            duration = SnackbarDuration.Short,
                        )
                    }
                }

                is ListEvent.UndoSuccessful -> {
                    snackbarHostState.showSnackbar(
                        message = "Restored",
                        duration = SnackbarDuration.Short,
                    )
                }

                is ListEvent.UndoExpired -> {
                    // No action needed, just acknowledge
                }

                is ListEvent.Error -> {
                    snackbarHostState.showSnackbar(
                        message = event.message,
                        duration = SnackbarDuration.Short,
                    )
                }

                is ListEvent.CheckedItemsCleared -> {
                    snackbarHostState.showSnackbar(
                        message = "Checked items cleared",
                        duration = SnackbarDuration.Short,
                    )
                }

                is ListEvent.QuantityUpdated -> {
                    snackbarHostState.showSnackbar(
                        message = "Quantity updated",
                        duration = SnackbarDuration.Short,
                    )
                }
            }
        }
    }

    ListScreenContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onAction = viewModel::onAction,
        onSubmitItem = {
            viewModel.onAction(ListAction.AddItem(uiState.inputText))
            focusManager.clearFocus()
        },
        onSuggestionClick = { item ->
            viewModel.onAction(ListAction.SelectAutoComplete(item))
            focusManager.clearFocus()
        },
        modifier = modifier,
    )
}

/**
 * Stateless content composable for the list screen.
 *
 * Separated from [ListScreen] for easier testing and previews.
 */
@Composable
private fun ListScreenContent(
    uiState: ListUiState,
    snackbarHostState: SnackbarHostState,
    onAction: (ListAction) -> Unit,
    onSubmitItem: () -> Unit,
    onSuggestionClick: (GroceryItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        snackbarHost = { RestockSnackbarHost(snackbarHostState) },
        containerColor = RestockColors.Background,
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            // Add Item Input Section
            AddItemSection(
                inputText = uiState.inputText,
                suggestions = uiState.autoCompleteSuggestions,
                showAutoComplete = uiState.showAutoComplete,
                onInputChange = { onAction(ListAction.UpdateInputText(it)) },
                onSubmit = onSubmitItem,
                onSuggestionClick = onSuggestionClick,
                modifier = Modifier.padding(
                    horizontal = RestockSpacing.screenPadding,
                    vertical = RestockSpacing.md,
                ),
            )

            // List Content
            when {
                uiState.isLoading -> {
                    LoadingScreen(
                        message = "Loading your list...",
                        modifier = Modifier.weight(1f),
                    )
                }

                uiState.isEmpty -> {
                    EmptyListState(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                    )
                }

                else -> {
                    GroceryList(
                        uncheckedItems = uiState.uncheckedItems,
                        checkedItems = uiState.checkedItems,
                        onToggleItem = { id, checked ->
                            onAction(ListAction.ToggleItem(id, checked))
                        },
                        onDeleteItem = { id ->
                            onAction(ListAction.DeleteItem(id))
                        },
                        onQuantityClick = { item ->
                            onAction(ListAction.SelectItemForQuantity(item))
                        },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }

        // Quantity Bottom Sheet
        uiState.selectedItemForQuantity?.let { item ->
            QuantityBottomSheet(
                item = item,
                onDismiss = {
                    onAction(ListAction.SelectItemForQuantity(null))
                },
                onConfirm = { quantity, unit ->
                    onAction(ListAction.UpdateQuantity(item.id, quantity, unit))
                },
            )
        }
    }
}

/**
 * Add item input section with autocomplete dropdown.
 */
@Composable
private fun AddItemSection(
    inputText: String,
    suggestions: ImmutableList<GroceryItem>,
    showAutoComplete: Boolean,
    onInputChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onSuggestionClick: (GroceryItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        AddItemTextField(
            value = inputText,
            onValueChange = onInputChange,
            onSubmit = {
                if (inputText.isNotBlank()) {
                    onSubmit()
                }
            },
        )

        // Auto-complete dropdown
        AutoCompleteDropdown(
            suggestions = suggestions,
            searchQuery = inputText,
            onSuggestionClick = onSuggestionClick,
            visible = showAutoComplete,
            modifier = Modifier.padding(top = RestockSpacing.xs),
        )
    }
}

/**
 * Main grocery list with unchecked and checked sections.
 */
@Composable
private fun GroceryList(
    uncheckedItems: ImmutableList<CurrentListItem>,
    checkedItems: ImmutableList<CurrentListItem>,
    onToggleItem: (String, Boolean) -> Unit,
    onDeleteItem: (String) -> Unit,
    onQuantityClick: (CurrentListItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(
            horizontal = RestockSpacing.screenPadding,
            vertical = RestockSpacing.sm,
        ),
        verticalArrangement = Arrangement.spacedBy(RestockSpacing.listItemGap),
    ) {
        // Unchecked items (main shopping list)
        items(
            items = uncheckedItems,
            key = { it.id },
        ) { item ->
            GroceryItemCard(
                item = item,
                onCheckedChange = { checked -> onToggleItem(item.id, checked) },
                onDelete = { onDeleteItem(item.id) },
                onQuantityClick = { onQuantityClick(item) },
                modifier = Modifier.animateItem(),
            )
        }

        // In Cart section (checked items)
        if (checkedItems.isNotEmpty()) {
            item(key = "in_cart_header") {
                InCartHeader(
                    count = checkedItems.size,
                    modifier = Modifier
                        .padding(
                            top = RestockSpacing.lg,
                            bottom = RestockSpacing.sm,
                        )
                        .animateItem(),
                )
            }

            items(
                items = checkedItems,
                key = { it.id },
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

        // Bottom padding for scroll comfort
        item(key = "bottom_spacer") {
            Spacer(modifier = Modifier.height(RestockSpacing.xxxl))
        }
    }
}

/**
 * Section header for the "In Cart" section.
 *
 * Displays a divider with centered text showing the count of checked items.
 *
 * @param count Number of items in cart.
 * @param modifier Modifier to be applied to the header.
 */
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

// ==================== PREVIEWS ====================

private val sampleUncheckedItems = listOf(
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
    CurrentListItem(
        id = "2",
        itemId = "item2",
        name = "Bread",
        normalizedName = "bread",
        quantity = 1,
        addedAt = Instant.now(),
        position = 1,
        isChecked = false,
    ),
    CurrentListItem(
        id = "3",
        itemId = "item3",
        name = "Butter",
        normalizedName = "butter",
        quantity = 1,
        addedAt = Instant.now(),
        position = 2,
        isChecked = false,
    ),
).toImmutableList()

private val sampleCheckedItems = listOf(
    CurrentListItem(
        id = "4",
        itemId = "item4",
        name = "Milk",
        normalizedName = "milk",
        quantity = 1,
        addedAt = Instant.now(),
        checkedAt = Instant.now(),
        position = 3,
        isChecked = true,
    ),
    CurrentListItem(
        id = "5",
        itemId = "item5",
        name = "Cheese",
        normalizedName = "cheese",
        quantity = 1,
        addedAt = Instant.now(),
        checkedAt = Instant.now(),
        position = 4,
        isChecked = true,
    ),
).toImmutableList()

private val sampleSuggestions = listOf(
    GroceryItem(
        id = "s1",
        name = "Milk",
        normalizedName = "milk",
        createdAt = Instant.now(),
    ),
    GroceryItem(
        id = "s2",
        name = "Milk chocolate",
        normalizedName = "milk chocolate",
        createdAt = Instant.now(),
    ),
    GroceryItem(
        id = "s3",
        name = "Milk alternatives",
        normalizedName = "milk alternatives",
        createdAt = Instant.now(),
    ),
).toImmutableList()

@LightDarkPreview
@Composable
private fun ListScreenWithItemsPreview() {
    RestockPreviewTheme {
        ListScreenContent(
            uiState = ListUiState(
                uncheckedItems = sampleUncheckedItems,
                checkedItems = sampleCheckedItems,
                isLoading = false,
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onAction = {},
            onSubmitItem = {},
            onSuggestionClick = {},
        )
    }
}

@LightDarkPreview
@Composable
private fun ListScreenEmptyPreview() {
    RestockPreviewTheme {
        ListScreenContent(
            uiState = ListUiState(
                isLoading = false,
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onAction = {},
            onSubmitItem = {},
            onSuggestionClick = {},
        )
    }
}

@LightDarkPreview
@Composable
private fun ListScreenLoadingPreview() {
    RestockPreviewTheme {
        ListScreenContent(
            uiState = ListUiState(
                isLoading = true,
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onAction = {},
            onSubmitItem = {},
            onSuggestionClick = {},
        )
    }
}

@LightDarkPreview
@Composable
private fun ListScreenWithAutoCompletePreview() {
    RestockPreviewTheme {
        ListScreenContent(
            uiState = ListUiState(
                uncheckedItems = sampleUncheckedItems,
                checkedItems = persistentListOf(),
                autoCompleteSuggestions = sampleSuggestions,
                inputText = "mil",
                isLoading = false,
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onAction = {},
            onSubmitItem = {},
            onSuggestionClick = {},
        )
    }
}

@LightDarkPreview
@Composable
private fun ListScreenOnlyUncheckedPreview() {
    RestockPreviewTheme {
        ListScreenContent(
            uiState = ListUiState(
                uncheckedItems = sampleUncheckedItems,
                checkedItems = persistentListOf(),
                isLoading = false,
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onAction = {},
            onSubmitItem = {},
            onSuggestionClick = {},
        )
    }
}

@LightDarkPreview
@Composable
private fun InCartHeaderPreview() {
    RestockPreviewTheme {
        Surface(color = RestockColors.Background) {
            InCartHeader(
                count = 5,
                modifier = Modifier.padding(RestockSpacing.md),
            )
        }
    }
}

@LightDarkPreview
@Composable
private fun AddItemSectionPreview() {
    RestockPreviewTheme {
        Surface(color = RestockColors.Background) {
            AddItemSection(
                inputText = "",
                suggestions = persistentListOf(),
                showAutoComplete = false,
                onInputChange = {},
                onSubmit = {},
                onSuggestionClick = {},
                modifier = Modifier.padding(RestockSpacing.md),
            )
        }
    }
}

@LightDarkPreview
@Composable
private fun AddItemSectionWithSuggestionsPreview() {
    RestockPreviewTheme {
        Surface(color = RestockColors.Background) {
            AddItemSection(
                inputText = "mil",
                suggestions = sampleSuggestions,
                showAutoComplete = true,
                onInputChange = {},
                onSubmit = {},
                onSuggestionClick = {},
                modifier = Modifier.padding(RestockSpacing.md),
            )
        }
    }
}
