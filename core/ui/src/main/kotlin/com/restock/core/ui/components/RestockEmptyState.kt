package com.restock.core.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.restock.core.ui.preview.LightDarkPreview
import com.restock.core.ui.preview.LightPreview
import com.restock.core.ui.preview.RestockPreviewTheme
import com.restock.core.ui.theme.RestockColors
import com.restock.core.ui.theme.RestockSpacing

/**
 * Empty state component for screens with no content.
 *
 * Displays a centered icon, title, optional description, and optional action button.
 * Following the design spec:
 * - Icon size: 64dp
 * - Icon color: Muted @ 50% opacity (MutedVariant)
 * - Title: TitleMedium
 * - Description: BodyMedium, Muted
 * - Padding: 32dp (xl)
 *
 * @param title The main title text.
 * @param modifier Modifier to be applied to the container.
 * @param description Optional descriptive text explaining the empty state.
 * @param icon Optional icon to display above the title.
 * @param action Optional action button content.
 */
@Composable
fun RestockEmptyState(
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    icon: ImageVector? = null,
    action: (@Composable () -> Unit)? = null,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(RestockSpacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = RestockColors.Muted.copy(alpha = 0.5f),
            )
            Spacer(modifier = Modifier.height(RestockSpacing.lg))
        }

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = RestockColors.Primary,
            textAlign = TextAlign.Center,
        )

        if (description != null) {
            Spacer(modifier = Modifier.height(RestockSpacing.sm))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = RestockColors.Muted,
                textAlign = TextAlign.Center,
            )
        }

        if (action != null) {
            Spacer(modifier = Modifier.height(RestockSpacing.lg))
            action()
        }
    }
}

/**
 * Empty state component - alias matching task specification API.
 *
 * Wrapper for [RestockEmptyState] with slightly different parameter names.
 *
 * @param title The main title text.
 * @param modifier Modifier to be applied to the container.
 * @param description Optional descriptive text explaining the empty state.
 * @param icon Optional icon to display above the title.
 * @param action Optional action button content.
 */
@Composable
fun EmptyState(
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    icon: ImageVector? = null,
    action: (@Composable () -> Unit)? = null,
) {
    RestockEmptyState(
        title = title,
        modifier = modifier,
        description = description,
        icon = icon,
        action = action,
    )
}

/**
 * Empty state for the grocery list screen.
 *
 * @param modifier Modifier to be applied to the container.
 * @param onAddClick Called when the add button is clicked.
 */
@Composable
fun RestockListEmptyState(
    modifier: Modifier = Modifier,
    onAddClick: (() -> Unit)? = null,
) {
    RestockEmptyState(
        title = "Your list is empty",
        description = "Add items by typing above",
        modifier = modifier,
        icon = Icons.Outlined.ShoppingCart,
        action =
            if (onAddClick != null) {
                {
                    RestockButton(
                        text = "Add First Item",
                        onClick = onAddClick,
                    )
                }
            } else {
                null
            },
    )
}

/**
 * Alias for [RestockListEmptyState] matching task specification API.
 */
@Composable
fun EmptyListState(modifier: Modifier = Modifier) {
    RestockEmptyState(
        title = "Your list is empty",
        description = "Add items by typing above",
        icon = Icons.Outlined.ShoppingCart,
        modifier = modifier,
    )
}

/**
 * Empty state for the suggestions section.
 *
 * Shows when there are no suggestions yet because the app is still learning patterns.
 *
 * @param modifier Modifier to be applied to the container.
 */
@Composable
fun RestockSuggestionsEmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(RestockSpacing.md),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Learning your patterns...",
            style = MaterialTheme.typography.bodySmall,
            color = RestockColors.Muted,
            textAlign = TextAlign.Center,
        )
    }
}

/**
 * Empty state for suggestions section with trip count awareness.
 *
 * Displays different messages based on how many shopping trips the user has completed,
 * providing appropriate context and encouragement:
 * - 0 trips: "Learning your patterns..." with explanation
 * - 1-2 trips: "Almost ready!" with countdown to suggestions
 * - 3+ trips: "No suggestions right now" with reassurance
 *
 * The icon is optional and defaults to a lightbulb for visual consistency with
 * the "smart suggestions" concept.
 *
 * @param modifier Modifier to be applied to the container.
 * @param tripCount The number of shopping trips completed.
 * @param showIcon Whether to display the lightbulb icon. Defaults to true.
 */
@Composable
fun NoSuggestionsState(
    modifier: Modifier = Modifier,
    tripCount: Int = 0,
    showIcon: Boolean = true,
) {
    val (title, description) =
        when {
            tripCount < 1 ->
                Pair(
                    "Learning your patterns...",
                    "Add items and shop normally. Suggestions will appear soon.",
                )
            tripCount < 3 -> {
                val remaining = 3 - tripCount
                val suffix = if (remaining == 1) "" else "s"
                Pair(
                    "Almost ready!",
                    "After $remaining more shopping trip$suffix, suggestions will appear.",
                )
            }
            else ->
                Pair(
                    "No suggestions right now",
                    "You're all stocked up! Suggestions will appear when items are due.",
                )
        }

    RestockEmptyState(
        title = title,
        description = description,
        icon = if (showIcon) Icons.Outlined.Lightbulb else null,
        modifier = modifier,
    )
}

/**
 * Compact version of NoSuggestionsState for inline use.
 *
 * Use this when you need a smaller, inline empty state for the suggestions
 * section without the full empty state layout.
 *
 * @param modifier Modifier to be applied to the container.
 * @param tripCount The number of shopping trips completed.
 */
@Composable
fun NoSuggestionsStateCompact(
    modifier: Modifier = Modifier,
    tripCount: Int = 0,
) {
    val message =
        when {
            tripCount < 1 -> "Learning your patterns..."
            tripCount < 3 -> {
                val remaining = 3 - tripCount
                val suffix = if (remaining == 1) "" else "s"
                "After $remaining more trip$suffix, suggestions will appear"
            }
            else -> "You're all stocked up!"
        }

    Column(
        modifier = modifier.padding(RestockSpacing.md),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = RestockColors.Muted,
            textAlign = TextAlign.Center,
        )
    }
}

/**
 * Empty state for search results when no items match the query.
 *
 * @param query The search query that returned no results.
 * @param modifier Modifier to be applied to the container.
 */
@Composable
fun NoSearchResultsState(
    query: String,
    modifier: Modifier = Modifier,
) {
    RestockEmptyState(
        title = "No results for \"$query\"",
        description = "Try a different search term",
        icon = Icons.Outlined.Search,
        modifier = modifier,
    )
}

/**
 * Empty state for meal categories with no items.
 *
 * @param mealName The name of the meal category.
 * @param modifier Modifier to be applied to the container.
 */
@Composable
fun RestockMealEmptyState(
    mealName: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(RestockSpacing.md),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "No $mealName items",
            style = MaterialTheme.typography.bodyMedium,
            color = RestockColors.Muted,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(RestockSpacing.xs))
        Text(
            text = "Tag items from your list",
            style = MaterialTheme.typography.bodySmall,
            color = RestockColors.MutedVariant,
            textAlign = TextAlign.Center,
        )
    }
}

/**
 * Empty state for purchase history.
 *
 * @param modifier Modifier to be applied to the container.
 */
@Composable
fun RestockHistoryEmptyState(modifier: Modifier = Modifier) {
    RestockEmptyState(
        title = "No purchase history",
        description = "Check off items from your list to start tracking your shopping patterns.",
        modifier = modifier,
    )
}

/**
 * Error state component for displaying errors with retry option.
 *
 * Uses a red title color to indicate error state, with muted description
 * text and an optional retry action button.
 *
 * @param title The error title.
 * @param description The error description.
 * @param modifier Modifier to be applied to the container.
 * @param onRetry Called when the retry button is clicked.
 */
@Composable
fun RestockErrorState(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(RestockSpacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = RestockColors.Error,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(RestockSpacing.sm))

        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = RestockColors.Muted,
            textAlign = TextAlign.Center,
        )

        if (onRetry != null) {
            Spacer(modifier = Modifier.height(RestockSpacing.lg))
            RestockButton(
                text = "Try again",
                onClick = onRetry,
            )
        }
    }
}

// ==================== PREVIEWS ====================

@LightPreview
@Composable
private fun RestockEmptyStatePreview() {
    RestockPreviewTheme {
        RestockEmptyState(
            title = "Nothing here yet",
            description = "This is a generic empty state that can be customized.",
            icon = Icons.Outlined.ShoppingCart,
        )
    }
}

@LightPreview
@Composable
private fun EmptyListStatePreview() {
    RestockPreviewTheme {
        EmptyListState()
    }
}

@LightPreview
@Composable
private fun RestockListEmptyStatePreview() {
    RestockPreviewTheme {
        RestockListEmptyState(
            onAddClick = {},
        )
    }
}

@LightDarkPreview
@Composable
private fun EmptyStateWithActionPreview() {
    RestockPreviewTheme {
        RestockEmptyState(
            title = "Your list is empty",
            description = "Start adding items to get started",
            icon = Icons.Outlined.ShoppingCart,
            action = {
                RestockButton(
                    text = "Add First Item",
                    onClick = {},
                )
            },
        )
    }
}

@LightPreview
@Composable
private fun RestockSuggestionsEmptyStatePreview() {
    RestockPreviewTheme {
        RestockSuggestionsEmptyState()
    }
}

@LightPreview
@Composable
private fun NoSuggestionsStateLearningPreview() {
    RestockPreviewTheme {
        NoSuggestionsState(tripCount = 0)
    }
}

@LightPreview
@Composable
private fun NoSuggestionsStateAlmostReadyPreview() {
    RestockPreviewTheme {
        NoSuggestionsState(tripCount = 2)
    }
}

@LightPreview
@Composable
private fun NoSuggestionsStateStockedUpPreview() {
    RestockPreviewTheme {
        NoSuggestionsState(tripCount = 5)
    }
}

@LightPreview
@Composable
private fun NoSuggestionsStateNoIconPreview() {
    RestockPreviewTheme {
        NoSuggestionsState(tripCount = 1, showIcon = false)
    }
}

@LightDarkPreview
@Composable
private fun NoSuggestionsStateCompactPreview() {
    RestockPreviewTheme {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            NoSuggestionsStateCompact(tripCount = 0)
            NoSuggestionsStateCompact(tripCount = 2)
            NoSuggestionsStateCompact(tripCount = 5)
        }
    }
}

@LightPreview
@Composable
private fun NoSearchResultsStatePreview() {
    RestockPreviewTheme {
        NoSearchResultsState(query = "chocolate")
    }
}

@LightPreview
@Composable
private fun RestockMealEmptyStatePreview() {
    RestockPreviewTheme {
        RestockMealEmptyState(mealName = "breakfast")
    }
}

@LightPreview
@Composable
private fun RestockHistoryEmptyStatePreview() {
    RestockPreviewTheme {
        RestockHistoryEmptyState()
    }
}

@LightDarkPreview
@Composable
private fun RestockErrorStatePreview() {
    RestockPreviewTheme {
        RestockErrorState(
            title = "Something went wrong",
            description = "We couldn't load your items. Please try again.",
            onRetry = {},
        )
    }
}
