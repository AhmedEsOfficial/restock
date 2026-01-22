# Task: Implement Additional UI Components

**Sprint:** 1-2 (Project Setup & Core Infrastructure)
**Owner:** UI/UX Engineer
**Estimate:** 8 hours (D-4.1 through D-4.5 combined)
**Priority:** P0
**Prerequisites:** 002_theme_implementation.md

---

## Sprint Context

Additional components complete the design system foundation: snackbars for feedback, loading indicators, and empty states. These components ensure consistent UX patterns throughout the app.

---

## Task Description

Implement RestockSnackbar, LoadingIndicator, and EmptyState components.

## RestockSnackbar Implementation

### RestockSnackbar.kt

```kotlin
package com.restock.core.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.restock.core.ui.theme.RestockColors
import com.restock.core.ui.theme.RestockCornerRadius
import com.restock.core.ui.theme.RestockPreviewTheme
import kotlinx.coroutines.launch

@Composable
fun RestockSnackbarHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    SnackbarHost(
        hostState = hostState,
        modifier = modifier,
        snackbar = { snackbarData ->
            RestockSnackbar(snackbarData = snackbarData)
        }
    )
}

@Composable
fun RestockSnackbar(
    snackbarData: SnackbarData,
    modifier: Modifier = Modifier,
) {
    Snackbar(
        modifier = modifier.padding(16.dp),
        shape = MaterialTheme.shapes.small,
        containerColor = RestockColors.Primary,
        contentColor = Color.White,
        actionColor = RestockColors.Accent,
        actionContentColor = RestockColors.Accent,
        action = snackbarData.visuals.actionLabel?.let { actionLabel ->
            {
                TextButton(
                    onClick = { snackbarData.performAction() },
                ) {
                    Text(
                        text = actionLabel,
                        color = RestockColors.Accent,
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        },
    ) {
        Text(
            text = snackbarData.visuals.message,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

// ==================== PREVIEWS ====================

@Preview(showBackground = true)
@Composable
private fun SnackbarPreview() {
    RestockPreviewTheme {
        // Snackbar preview requires SnackbarData which is complex to mock
        // Using a simplified visual preview
        Snackbar(
            modifier = Modifier.padding(16.dp),
            containerColor = RestockColors.Primary,
            contentColor = Color.White,
            action = {
                TextButton(onClick = {}) {
                    Text("Undo", color = RestockColors.Accent)
                }
            }
        ) {
            Text("Item deleted")
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SnackbarNoActionPreview() {
    RestockPreviewTheme {
        Snackbar(
            modifier = Modifier.padding(16.dp),
            containerColor = RestockColors.Primary,
            contentColor = Color.White,
        ) {
            Text("Milk added to list")
        }
    }
}
```

## LoadingIndicator Implementation

### LoadingIndicator.kt

```kotlin
package com.restock.core.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.restock.core.ui.theme.RestockColors
import com.restock.core.ui.theme.RestockPreviewTheme
import com.restock.core.ui.theme.RestockSpacing

enum class LoadingIndicatorSize {
    SMALL,
    MEDIUM,
    LARGE,
}

@Composable
fun LoadingIndicator(
    modifier: Modifier = Modifier,
    size: LoadingIndicatorSize = LoadingIndicatorSize.MEDIUM,
    color: Color = RestockColors.Accent,
) {
    val indicatorSize = when (size) {
        LoadingIndicatorSize.SMALL -> 20.dp
        LoadingIndicatorSize.MEDIUM -> 32.dp
        LoadingIndicatorSize.LARGE -> 48.dp
    }

    val strokeWidth = when (size) {
        LoadingIndicatorSize.SMALL -> 2.dp
        LoadingIndicatorSize.MEDIUM -> 3.dp
        LoadingIndicatorSize.LARGE -> 4.dp
    }

    CircularProgressIndicator(
        modifier = modifier.size(indicatorSize),
        color = color,
        strokeWidth = strokeWidth,
    )
}

// Alternative: Pulsing dots indicator
@Composable
fun PulsingDotsIndicator(
    modifier: Modifier = Modifier,
    color: Color = RestockColors.Accent,
    dotSize: Dp = 8.dp,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "dots")

    val scales = listOf(0, 150, 300).map { delay ->
        infiniteTransition.animateFloat(
            initialValue = 0.6f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 600,
                    delayMillis = delay,
                    easing = LinearEasing
                ),
                repeatMode = RepeatMode.Reverse
            ),
            label = "dot_$delay"
        )
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        scales.forEach { scale ->
            Box(
                modifier = Modifier
                    .size(dotSize)
                    .scale(scale.value)
                    .background(color, CircleShape)
            )
        }
    }
}

// Full screen loading state
@Composable
fun LoadingScreen(
    modifier: Modifier = Modifier,
    message: String? = null,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            LoadingIndicator(size = LoadingIndicatorSize.LARGE)

            message?.let {
                Spacer(Modifier.height(RestockSpacing.md))
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = RestockColors.Muted,
                )
            }
        }
    }
}

// ==================== PREVIEWS ====================

@Preview(showBackground = true)
@Composable
private fun LoadingIndicatorSizesPreview() {
    RestockPreviewTheme {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LoadingIndicator(size = LoadingIndicatorSize.SMALL)
            LoadingIndicator(size = LoadingIndicatorSize.MEDIUM)
            LoadingIndicator(size = LoadingIndicatorSize.LARGE)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PulsingDotsPreview() {
    RestockPreviewTheme {
        PulsingDotsIndicator(
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LoadingScreenPreview() {
    RestockPreviewTheme {
        LoadingScreen(message = "Loading your list...")
    }
}
```

## EmptyState Implementation

### EmptyState.kt

```kotlin
package com.restock.core.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.restock.core.ui.theme.RestockPreviewTheme
import com.restock.core.ui.theme.RestockSpacing

@Composable
fun EmptyState(
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    icon: ImageVector? = null,
    action: @Composable (() -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(RestockSpacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        icon?.let {
            Icon(
                imageVector = it,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = RestockColors.Muted.copy(alpha = 0.5f),
            )
            Spacer(Modifier.height(RestockSpacing.lg))
        }

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = RestockColors.Primary,
            textAlign = TextAlign.Center,
        )

        description?.let {
            Spacer(Modifier.height(RestockSpacing.sm))
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                color = RestockColors.Muted,
                textAlign = TextAlign.Center,
            )
        }

        action?.let {
            Spacer(Modifier.height(RestockSpacing.lg))
            it()
        }
    }
}

// Pre-built empty states for common scenarios
@Composable
fun EmptyListState(
    modifier: Modifier = Modifier,
) {
    EmptyState(
        title = "Your list is empty",
        description = "Add items by typing above",
        icon = Icons.Outlined.ShoppingCart,
        modifier = modifier,
    )
}

@Composable
fun NoSuggestionsState(
    modifier: Modifier = Modifier,
    tripCount: Int = 0,
) {
    val (title, description) = when {
        tripCount < 1 -> Pair(
            "Learning your patterns...",
            "Add items and shop normally. Suggestions will appear soon."
        )
        tripCount < 3 -> Pair(
            "Almost ready!",
            "After ${3 - tripCount} more shopping trip${if (tripCount < 2) "s" else ""}, suggestions will appear."
        )
        else -> Pair(
            "No suggestions right now",
            "You're all stocked up! Suggestions will appear when items are due."
        )
    }

    EmptyState(
        title = title,
        description = description,
        modifier = modifier,
    )
}

@Composable
fun NoSearchResultsState(
    query: String,
    modifier: Modifier = Modifier,
) {
    EmptyState(
        title = "No results for \"$query\"",
        description = "Try a different search term",
        modifier = modifier,
    )
}

// ==================== PREVIEWS ====================

@Preview(showBackground = true)
@Composable
private fun EmptyListStatePreview() {
    RestockPreviewTheme {
        EmptyListState()
    }
}

@Preview(showBackground = true)
@Composable
private fun EmptyStateWithActionPreview() {
    RestockPreviewTheme {
        EmptyState(
            title = "Your list is empty",
            description = "Start adding items to get started",
            icon = Icons.Outlined.ShoppingCart,
            action = {
                RestockButton(
                    text = "Add First Item",
                    onClick = {},
                )
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun NoSuggestionsStateLearningPreview() {
    RestockPreviewTheme {
        NoSuggestionsState(tripCount = 0)
    }
}

@Preview(showBackground = true)
@Composable
private fun NoSuggestionsStateAlmostReadyPreview() {
    RestockPreviewTheme {
        NoSuggestionsState(tripCount = 2)
    }
}

@Preview(showBackground = true)
@Composable
private fun NoSearchResultsStatePreview() {
    RestockPreviewTheme {
        NoSearchResultsState(query = "chocolate")
    }
}
```

## Specifications

### Snackbar
- Background: Primary (#1F1F1F)
- Text: White
- Action: Accent (#F59E0B)
- Corner radius: 8dp
- Margin: 16dp

### Loading Indicator
| Size | Diameter | Stroke |
|------|----------|--------|
| Small | 20dp | 2dp |
| Medium | 32dp | 3dp |
| Large | 48dp | 4dp |

### Empty State
- Icon size: 64dp
- Icon color: Muted @ 50% opacity
- Title: Title Medium
- Description: Body Medium, Muted
- Padding: 32dp (xl)

## Acceptance Criteria

### Snackbar
- [ ] Primary background with white text
- [ ] Optional action button in Accent
- [ ] Proper corner radius
- [ ] Integrates with SnackbarHostState

### Loading Indicator
- [ ] Three sizes (small, medium, large)
- [ ] Accent color by default
- [ ] Optional pulsing dots variant
- [ ] Full screen loading with message

### Empty State
- [ ] Configurable icon, title, description
- [ ] Optional action button
- [ ] Pre-built variants for common cases
- [ ] Centered layout

## Definition of Done

- [ ] All components compile without errors
- [ ] Match design specifications
- [ ] Previews render correctly
- [ ] Documentation comments added
- [ ] Accessibility considered

## Notes

- Snackbars should auto-dismiss after 4-5 seconds
- Loading indicators should be subtle, not distracting
- Empty states should be encouraging, not discouraging
- Test on various screen sizes
