package com.restock.feature.list.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.restock.core.domain.model.GroceryItem
import com.restock.core.ui.preview.LightDarkPreview
import com.restock.core.ui.preview.RestockPreviewTheme
import com.restock.core.ui.theme.RestockColors
import com.restock.core.ui.theme.RestockElevation
import com.restock.core.ui.theme.RestockSpacing
import java.time.Instant

/**
 * Maximum height for the autocomplete dropdown.
 * Limits to approximately 5 items visible.
 */
private val MAX_DROPDOWN_HEIGHT = 280.dp

/**
 * Maximum number of suggestions to display.
 */
private const val MAX_SUGGESTIONS = 5

/**
 * Animation duration for show/hide transitions.
 */
private const val ANIMATION_DURATION_MS = 150

/**
 * Autocomplete dropdown for item suggestions.
 *
 * Displays a list of suggestions that match the user's input.
 * Suggestions are shown in a floating card with shadow, appearing
 * below the input field with smooth animations.
 *
 * Features:
 * - Smooth expand/collapse animations
 * - Highlighted matching text
 * - Limited to 5 visible suggestions
 * - Shadow elevation for visual prominence
 *
 * @param suggestions List of grocery items to show as suggestions.
 * @param searchQuery The current search query (used for highlighting).
 * @param onSuggestionClick Called when a suggestion is tapped.
 * @param modifier Modifier to be applied to the dropdown container.
 * @param visible Whether the dropdown should be visible.
 */
@Composable
fun AutoCompleteDropdown(
    suggestions: List<GroceryItem>,
    searchQuery: String,
    onSuggestionClick: (GroceryItem) -> Unit,
    modifier: Modifier = Modifier,
    visible: Boolean = true,
) {
    AnimatedVisibility(
        visible = visible && suggestions.isNotEmpty(),
        enter =
            fadeIn(
                animationSpec = androidx.compose.animation.core.tween(ANIMATION_DURATION_MS),
            ) +
                expandVertically(
                    animationSpec = androidx.compose.animation.core.tween(ANIMATION_DURATION_MS),
                ),
        exit =
            fadeOut(
                animationSpec = androidx.compose.animation.core.tween(ANIMATION_DURATION_MS),
            ) +
                shrinkVertically(
                    animationSpec = androidx.compose.animation.core.tween(ANIMATION_DURATION_MS),
                ),
        modifier = modifier,
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            color = RestockColors.Card,
            shadowElevation = RestockElevation.level2,
        ) {
            LazyColumn(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .heightIn(max = MAX_DROPDOWN_HEIGHT),
            ) {
                val displaySuggestions = suggestions.take(MAX_SUGGESTIONS)
                items(
                    items = displaySuggestions,
                    key = { it.id },
                ) { item ->
                    AutoCompleteSuggestionItem(
                        item = item,
                        searchQuery = searchQuery,
                        onClick = { onSuggestionClick(item) },
                    )

                    // Divider between items (except last)
                    if (item != displaySuggestions.last()) {
                        HorizontalDivider(
                            color = RestockColors.Divider,
                            modifier = Modifier.padding(horizontal = RestockSpacing.md),
                        )
                    }
                }
            }
        }
    }
}

/**
 * Individual suggestion item in the autocomplete dropdown.
 *
 * @param item The grocery item to display.
 * @param searchQuery The current search query for highlighting.
 * @param onClick Called when the item is tapped.
 * @param modifier Modifier to be applied to the item.
 */
@Composable
private fun AutoCompleteSuggestionItem(
    item: GroceryItem,
    searchQuery: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val annotatedString =
        buildAnnotatedString {
            val name = item.name
            val normalizedQuery = searchQuery.lowercase().trim()
            val normalizedName = name.lowercase()

            val startIndex = normalizedName.indexOf(normalizedQuery)
            if (startIndex >= 0 && normalizedQuery.isNotEmpty()) {
                // Text before match
                append(name.substring(0, startIndex))

                // Highlighted match
                withStyle(
                    style =
                        SpanStyle(
                            color = RestockColors.Accent,
                            fontWeight = FontWeight.SemiBold,
                        ),
                ) {
                    append(name.substring(startIndex, startIndex + normalizedQuery.length))
                }

                // Text after match
                append(name.substring(startIndex + normalizedQuery.length))
            } else {
                // No match found, show plain text
                append(name)
            }
        }

    Text(
        text = annotatedString,
        style = MaterialTheme.typography.bodyLarge,
        color = RestockColors.Primary,
        modifier =
            modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(
                    horizontal = RestockSpacing.md,
                    vertical = RestockSpacing.cardPaddingVertical,
                ),
    )
}

// ==================== PREVIEWS ====================

private val sampleSuggestions =
    listOf(
        GroceryItem(
            id = "1",
            name = "Milk",
            normalizedName = "milk",
            createdAt = Instant.now(),
        ),
        GroceryItem(
            id = "2",
            name = "Milk chocolate",
            normalizedName = "milk chocolate",
            createdAt = Instant.now(),
        ),
        GroceryItem(
            id = "3",
            name = "Milk alternatives",
            normalizedName = "milk alternatives",
            createdAt = Instant.now(),
        ),
        GroceryItem(
            id = "4",
            name = "Almond milk",
            normalizedName = "almond milk",
            createdAt = Instant.now(),
        ),
        GroceryItem(
            id = "5",
            name = "Oat milk",
            normalizedName = "oat milk",
            createdAt = Instant.now(),
        ),
    )

@LightDarkPreview
@Composable
private fun AutoCompleteDropdownPreview() {
    RestockPreviewTheme {
        Surface(color = RestockColors.Background) {
            Column(modifier = Modifier.padding(RestockSpacing.md)) {
                AutoCompleteDropdown(
                    suggestions = sampleSuggestions,
                    searchQuery = "milk",
                    onSuggestionClick = {},
                )
            }
        }
    }
}

@LightDarkPreview
@Composable
private fun AutoCompleteDropdownSingleItemPreview() {
    RestockPreviewTheme {
        Surface(color = RestockColors.Background) {
            Column(modifier = Modifier.padding(RestockSpacing.md)) {
                AutoCompleteDropdown(
                    suggestions = listOf(sampleSuggestions.first()),
                    searchQuery = "mil",
                    onSuggestionClick = {},
                )
            }
        }
    }
}

@LightDarkPreview
@Composable
private fun AutoCompleteDropdownNoMatchPreview() {
    RestockPreviewTheme {
        Surface(color = RestockColors.Background) {
            Column(modifier = Modifier.padding(RestockSpacing.md)) {
                AutoCompleteDropdown(
                    suggestions = sampleSuggestions.take(3),
                    searchQuery = "xyz",
                    onSuggestionClick = {},
                )
            }
        }
    }
}

@LightDarkPreview
@Composable
private fun AutoCompleteDropdownEmptyPreview() {
    RestockPreviewTheme {
        Surface(color = RestockColors.Background) {
            Column(modifier = Modifier.padding(RestockSpacing.md)) {
                Text(
                    text = "Empty dropdown (nothing rendered):",
                    style = MaterialTheme.typography.bodyMedium,
                    color = RestockColors.Muted,
                )
                AutoCompleteDropdown(
                    suggestions = emptyList(),
                    searchQuery = "",
                    onSuggestionClick = {},
                )
            }
        }
    }
}
