package com.restock.core.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.restock.core.ui.theme.RestockColors
import com.restock.core.ui.theme.RestockTheme

/**
 * Empty state component for screens with no content.
 *
 * Displays a centered icon, title, description, and optional action button.
 *
 * @param title The main title text.
 * @param description The descriptive text explaining the empty state.
 * @param modifier Modifier to be applied to the container.
 * @param icon Optional icon to display above the title.
 * @param action Optional action button content.
 */
@Composable
fun RestockEmptyState(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    action: (@Composable () -> Unit)? = null,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = RestockColors.MutedVariant,
            )
            Spacer(modifier = Modifier.height(24.dp))
        }

        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = RestockColors.OnBackground,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = RestockColors.Muted,
            textAlign = TextAlign.Center,
        )

        if (action != null) {
            Spacer(modifier = Modifier.height(24.dp))
            action()
        }
    }
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
        description = "Add items to start building your grocery list. We'll learn your patterns to make suggestions.",
        modifier = modifier,
        icon = Icons.Outlined.ShoppingCart,
        action =
            if (onAddClick != null) {
                {
                    RestockButton(
                        text = "Add your first item",
                        onClick = onAddClick,
                    )
                }
            } else {
                null
            },
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
        modifier = modifier.padding(16.dp),
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
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "No $mealName items",
            style = MaterialTheme.typography.bodyMedium,
            color = RestockColors.Muted,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(4.dp))
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
        icon = null,
    )
}

/**
 * Error state component for displaying errors with retry option.
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
                .fillMaxSize()
                .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = RestockColors.Error,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = RestockColors.Muted,
            textAlign = TextAlign.Center,
        )

        if (onRetry != null) {
            Spacer(modifier = Modifier.height(24.dp))
            RestockButton(
                text = "Try again",
                onClick = onRetry,
            )
        }
    }
}

// Previews
@Preview(showBackground = true, backgroundColor = 0xFFFFFBF5)
@Composable
private fun RestockEmptyStatePreview() {
    RestockTheme {
        RestockEmptyState(
            title = "Nothing here yet",
            description = "This is a generic empty state that can be customized.",
            icon = Icons.Outlined.ShoppingCart,
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFBF5)
@Composable
private fun RestockListEmptyStatePreview() {
    RestockTheme {
        RestockListEmptyState(
            onAddClick = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFBF5)
@Composable
private fun RestockSuggestionsEmptyStatePreview() {
    RestockTheme {
        RestockSuggestionsEmptyState()
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFBF5)
@Composable
private fun RestockMealEmptyStatePreview() {
    RestockTheme {
        RestockMealEmptyState(mealName = "breakfast")
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFBF5)
@Composable
private fun RestockHistoryEmptyStatePreview() {
    RestockTheme {
        RestockHistoryEmptyState()
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFBF5)
@Composable
private fun RestockErrorStatePreview() {
    RestockTheme {
        RestockErrorState(
            title = "Something went wrong",
            description = "We couldn't load your items. Please try again.",
            onRetry = {},
        )
    }
}
