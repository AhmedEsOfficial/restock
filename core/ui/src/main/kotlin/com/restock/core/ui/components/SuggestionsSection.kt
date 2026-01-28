package com.restock.core.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.restock.core.ui.preview.LightDarkPreview
import com.restock.core.ui.preview.RestockPreviewTheme
import com.restock.core.ui.theme.RestockColors
import com.restock.core.ui.theme.RestockSpacing
import com.restock.core.ui.util.rememberReducedMotion
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

// ==================== ANIMATION CONSTANTS ====================

/**
 * Duration for section appear animation.
 */
private const val SECTION_APPEAR_DURATION_MS = 300

/**
 * Duration for section hide animation.
 */
private const val SECTION_HIDE_DURATION_MS = 200

/**
 * Stagger delay between card entry animations.
 */
private const val CARD_ENTRY_STAGGER_MS = 50

/**
 * Maximum number of suggestions to display.
 */
private const val MAX_VISIBLE_SUGGESTIONS = 5

// ==================== DATA MODELS ====================

/**
 * UI model for a suggestion displayed in the suggestions section.
 *
 * This is a simplified representation for the UI layer that contains
 * only the fields needed for display.
 *
 * @property id Unique identifier for the suggestion.
 * @property itemName Display name of the suggested item.
 * @property reason Human-readable reason for the suggestion.
 */
data class SuggestionUiModel(
    val id: String,
    val itemName: String,
    val reason: String,
)

// ==================== SUGGESTIONS SECTION COMPONENT ====================

/**
 * A horizontally scrollable section displaying smart suggestions.
 *
 * The SuggestionsSection displays up to 5 suggestion cards in a horizontal
 * scroll layout. It includes:
 * - Section header ("Suggestions")
 * - Horizontal row of suggestion cards
 * - Section divider
 * - Animated visibility when suggestions appear/disappear
 *
 * **Layout Specification:**
 * ```
 * ┌─────────────────────────────────────────────────────────────┐
 * │  Suggestions                                                 │
 * │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  →       │
 * │  │             │  │             │  │             │          │
 * │  │    Milk     │  │    Eggs     │  │   Bread     │          │
 * │  │  [× dismiss]│  │  [× dismiss]│  │  [× dismiss]│          │
 * │  │             │  │             │  │             │          │
 * │  │ Usually     │  │ Last bought │  │ Usually     │          │
 * │  │ every 7 days│  │ 20 days ago │  │ every 5 days│          │
 * │  └─────────────┘  └─────────────┘  └─────────────┘          │
 * │                                                             │
 * │  ─────────────────────────────────────────────────────────  │
 * └─────────────────────────────────────────────────────────────┘
 * ```
 *
 * **Animation Specifications:**
 * | Animation | Duration | Easing |
 * |-----------|----------|--------|
 * | Section appear | 300ms | EaseOut |
 * | Section hide | 200ms | EaseIn |
 * | Card entry | 200ms | EaseOut (staggered 50ms each) |
 *
 * **Design Requirements:**
 * - Position: Above the main list, below the input field
 * - Scroll: Horizontal scrolling for 3+ suggestions
 * - Maximum: 5 suggestions visible at once
 * - Visibility: Section hidden when no suggestions
 *
 * @param suggestions List of suggestions to display (max 5 will be shown).
 * @param onAccept Called when a suggestion is accepted with its ID.
 * @param onDismiss Called when a suggestion is dismissed with its ID.
 * @param modifier Modifier to be applied to the section.
 * @param showDivider Whether to show a divider below the section.
 * @param title The section header title. Defaults to "Suggestions".
 *
 * Usage:
 * ```kotlin
 * SuggestionsSection(
 *     suggestions = listOf(
 *         SuggestionUiModel("1", "Milk", "Usually every 7 days"),
 *         SuggestionUiModel("2", "Eggs", "Last bought 20 days ago"),
 *     ),
 *     onAccept = { id -> viewModel.acceptSuggestion(id) },
 *     onDismiss = { id -> viewModel.dismissSuggestion(id) }
 * )
 * ```
 */
@Composable
fun SuggestionsSection(
    suggestions: ImmutableList<SuggestionUiModel>,
    onAccept: (String) -> Unit,
    onDismiss: (String) -> Unit,
    modifier: Modifier = Modifier,
    showDivider: Boolean = true,
    title: String = "Suggestions",
) {
    val reduceMotion = rememberReducedMotion()
    val visibleSuggestions = suggestions.take(MAX_VISIBLE_SUGGESTIONS)
    val hasSuggestions = visibleSuggestions.isNotEmpty()

    AnimatedVisibility(
        visible = hasSuggestions,
        enter =
            fadeIn(
                animationSpec =
                    tween(
                        durationMillis = if (reduceMotion) 0 else SECTION_APPEAR_DURATION_MS,
                        easing = EaseOut,
                    ),
            ) +
                expandVertically(
                    animationSpec =
                        tween(
                            durationMillis = if (reduceMotion) 0 else SECTION_APPEAR_DURATION_MS,
                            easing = EaseOut,
                        ),
                    expandFrom = Alignment.Top,
                ),
        exit =
            fadeOut(
                animationSpec =
                    tween(
                        durationMillis = if (reduceMotion) 0 else SECTION_HIDE_DURATION_MS,
                        easing = EaseIn,
                    ),
            ) +
                shrinkVertically(
                    animationSpec =
                        tween(
                            durationMillis = if (reduceMotion) 0 else SECTION_HIDE_DURATION_MS,
                            easing = EaseIn,
                        ),
                    shrinkTowards = Alignment.Top,
                ),
        modifier = modifier,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Section header
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = RestockColors.Primary,
                modifier =
                    Modifier
                        .padding(
                            horizontal = RestockSpacing.screenPadding,
                            vertical = RestockSpacing.sm,
                        )
                        .semantics { heading() },
            )

            // Horizontal scrolling cards
            LazyRow(
                contentPadding =
                    PaddingValues(
                        horizontal = RestockSpacing.screenPadding,
                    ),
                horizontalArrangement = Arrangement.spacedBy(RestockSpacing.cardPaddingHorizontal),
                state = rememberLazyListState(),
                modifier = Modifier.fillMaxWidth(),
            ) {
                itemsIndexed(
                    items = visibleSuggestions,
                    key = { _, suggestion -> suggestion.id },
                ) { index, suggestion ->
                    SuggestionCard(
                        itemName = suggestion.itemName,
                        reason = suggestion.reason,
                        onAccept = { onAccept(suggestion.id) },
                        onDismiss = { onDismiss(suggestion.id) },
                        animateEntry = !reduceMotion,
                        entryDelayMs = index * CARD_ENTRY_STAGGER_MS,
                    )
                }
            }

            Spacer(Modifier.height(RestockSpacing.md))

            // Section divider
            if (showDivider) {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = RestockSpacing.screenPadding),
                    color = RestockColors.Divider,
                    thickness = 1.dp,
                )
            }
        }
    }
}

/**
 * Simplified suggestions row without section header and divider.
 *
 * Use this when you need just the horizontally scrolling cards
 * without the section wrapper (e.g., in custom layouts).
 *
 * @param suggestions List of suggestions to display.
 * @param onAccept Called when a suggestion is accepted.
 * @param onDismiss Called when a suggestion is dismissed.
 * @param modifier Modifier to be applied to the row.
 */
@Composable
fun SuggestionsRow(
    suggestions: ImmutableList<SuggestionUiModel>,
    onAccept: (String) -> Unit,
    onDismiss: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val reduceMotion = rememberReducedMotion()
    val visibleSuggestions = suggestions.take(MAX_VISIBLE_SUGGESTIONS)

    if (visibleSuggestions.isEmpty()) {
        return
    }

    LazyRow(
        contentPadding = PaddingValues(horizontal = RestockSpacing.screenPadding),
        horizontalArrangement = Arrangement.spacedBy(RestockSpacing.cardPaddingHorizontal),
        modifier = modifier,
    ) {
        itemsIndexed(
            items = visibleSuggestions,
            key = { _, suggestion -> suggestion.id },
        ) { index, suggestion ->
            SuggestionCard(
                itemName = suggestion.itemName,
                reason = suggestion.reason,
                onAccept = { onAccept(suggestion.id) },
                onDismiss = { onDismiss(suggestion.id) },
                animateEntry = !reduceMotion,
                entryDelayMs = index * CARD_ENTRY_STAGGER_MS,
            )
        }
    }
}

/**
 * Stateless preview-friendly version of SuggestionsSection.
 *
 * Use this for Compose previews where animation state isn't desired.
 */
@Composable
private fun SuggestionsSectionStateless(
    suggestions: List<SuggestionUiModel>,
    onAccept: (String) -> Unit,
    onDismiss: (String) -> Unit,
    modifier: Modifier = Modifier,
    showDivider: Boolean = true,
    title: String = "Suggestions",
) {
    val visibleSuggestions = suggestions.take(MAX_VISIBLE_SUGGESTIONS)

    if (visibleSuggestions.isEmpty()) {
        return
    }

    Column(modifier = modifier.fillMaxWidth()) {
        // Section header
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = RestockColors.Primary,
            modifier =
                Modifier
                    .padding(
                        horizontal = RestockSpacing.screenPadding,
                        vertical = RestockSpacing.sm,
                    )
                    .semantics { heading() },
        )

        // Horizontal scrolling cards
        Row(
            modifier =
                Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = RestockSpacing.screenPadding),
            horizontalArrangement = Arrangement.spacedBy(RestockSpacing.cardPaddingHorizontal),
        ) {
            visibleSuggestions.forEach { suggestion ->
                key(suggestion.id) {
                    SuggestionCardStateless(
                        itemName = suggestion.itemName,
                        reason = suggestion.reason,
                        onAccept = { onAccept(suggestion.id) },
                        onDismiss = { onDismiss(suggestion.id) },
                    )
                }
            }
        }

        Spacer(Modifier.height(RestockSpacing.md))

        // Section divider
        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = RestockSpacing.screenPadding),
                color = RestockColors.Divider,
                thickness = 1.dp,
            )
        }
    }
}

// ==================== PREVIEWS ====================

@LightDarkPreview
@Composable
private fun SuggestionsSectionPreview() {
    RestockPreviewTheme {
        Box(modifier = Modifier.padding(vertical = 16.dp)) {
            SuggestionsSectionStateless(
                suggestions =
                    listOf(
                        SuggestionUiModel("1", "Milk", "Usually every 7 days"),
                        SuggestionUiModel("2", "Eggs", "Last bought 20 days ago"),
                        SuggestionUiModel("3", "Bread", "Usually every 5 days"),
                    ),
                onAccept = {},
                onDismiss = {},
            )
        }
    }
}

@LightDarkPreview
@Composable
private fun SuggestionsSectionSingleCardPreview() {
    RestockPreviewTheme {
        Box(modifier = Modifier.padding(vertical = 16.dp)) {
            SuggestionsSectionStateless(
                suggestions =
                    listOf(
                        SuggestionUiModel("1", "Milk", "Usually every 7 days"),
                    ),
                onAccept = {},
                onDismiss = {},
            )
        }
    }
}

@LightDarkPreview
@Composable
private fun SuggestionsSectionManyCardsPreview() {
    RestockPreviewTheme {
        Box(modifier = Modifier.padding(vertical = 16.dp)) {
            SuggestionsSectionStateless(
                suggestions =
                    listOf(
                        SuggestionUiModel("1", "Milk", "Usually every 7 days"),
                        SuggestionUiModel("2", "Eggs", "Last bought 20 days ago"),
                        SuggestionUiModel("3", "Bread", "Usually every 5 days"),
                        SuggestionUiModel("4", "Butter", "Usually every 2 weeks"),
                        SuggestionUiModel("5", "Cheese", "Usually every 10 days"),
                    ),
                onAccept = {},
                onDismiss = {},
            )
        }
    }
}

@LightDarkPreview
@Composable
private fun SuggestionsSectionNoDividerPreview() {
    RestockPreviewTheme {
        Box(modifier = Modifier.padding(vertical = 16.dp)) {
            SuggestionsSectionStateless(
                suggestions =
                    listOf(
                        SuggestionUiModel("1", "Milk", "Usually every 7 days"),
                        SuggestionUiModel("2", "Eggs", "Last bought 20 days ago"),
                    ),
                onAccept = {},
                onDismiss = {},
                showDivider = false,
            )
        }
    }
}

@LightDarkPreview
@Composable
private fun SuggestionsSectionEmptyPreview() {
    RestockPreviewTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Empty state (section should not be visible):",
                style = MaterialTheme.typography.bodySmall,
                color = RestockColors.Muted,
            )
            Spacer(Modifier.height(8.dp))
            SuggestionsSectionStateless(
                suggestions = emptyList(),
                onAccept = {},
                onDismiss = {},
            )
            Text(
                text = "(Nothing rendered above)",
                style = MaterialTheme.typography.bodySmall,
                color = RestockColors.Muted,
            )
        }
    }
}

@LightDarkPreview
@Composable
private fun SuggestionsSectionCustomTitlePreview() {
    RestockPreviewTheme {
        Box(modifier = Modifier.padding(vertical = 16.dp)) {
            SuggestionsSectionStateless(
                suggestions =
                    listOf(
                        SuggestionUiModel("1", "Milk", "Usually every 7 days"),
                        SuggestionUiModel("2", "Eggs", "Last bought 20 days ago"),
                    ),
                onAccept = {},
                onDismiss = {},
                title = "Smart Picks",
            )
        }
    }
}

@LightDarkPreview
@Composable
private fun SuggestionsSectionInteractivePreview() {
    RestockPreviewTheme {
        var suggestions by remember {
            mutableStateOf(
                listOf(
                    SuggestionUiModel("1", "Milk", "Usually every 7 days"),
                    SuggestionUiModel("2", "Eggs", "Last bought 20 days ago"),
                    SuggestionUiModel("3", "Bread", "Usually every 5 days"),
                    SuggestionUiModel("4", "Butter", "Usually every 2 weeks"),
                ),
            )
        }

        Column(modifier = Modifier.padding(vertical = 16.dp)) {
            if (suggestions.isNotEmpty()) {
                SuggestionsSection(
                    suggestions = suggestions.toImmutableList(),
                    onAccept = { id ->
                        suggestions = suggestions.filter { it.id != id }
                    },
                    onDismiss = { id ->
                        suggestions = suggestions.filter { it.id != id }
                    },
                )
            } else {
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(RestockSpacing.xl),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "All suggestions handled!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = RestockColors.Muted,
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = "Tap card to accept, tap X to dismiss",
                style = MaterialTheme.typography.bodySmall,
                color = RestockColors.Muted,
                modifier = Modifier.padding(horizontal = RestockSpacing.screenPadding),
            )
        }
    }
}

@LightDarkPreview
@Composable
private fun SuggestionsRowPreview() {
    RestockPreviewTheme {
        Box(modifier = Modifier.padding(vertical = 16.dp)) {
            SuggestionsRow(
                suggestions =
                    persistentListOf(
                        SuggestionUiModel("1", "Milk", "Usually every 7 days"),
                        SuggestionUiModel("2", "Eggs", "Last bought 20 days ago"),
                        SuggestionUiModel("3", "Bread", "Usually every 5 days"),
                    ),
                onAccept = {},
                onDismiss = {},
            )
        }
    }
}

@LightDarkPreview
@Composable
private fun SuggestionsSectionWithLearningStatePreview() {
    RestockPreviewTheme {
        Column {
            // Section with suggestions
            SuggestionsSectionStateless(
                suggestions =
                    listOf(
                        SuggestionUiModel("1", "Milk", "Usually every 7 days"),
                        SuggestionUiModel("2", "Eggs", "Last bought 20 days ago"),
                    ),
                onAccept = {},
                onDismiss = {},
            )

            Spacer(Modifier.height(24.dp))

            // Learning state
            Column(modifier = Modifier.padding(horizontal = RestockSpacing.screenPadding)) {
                Text(
                    text = "Learning State (trip count = 1):",
                    style = MaterialTheme.typography.labelSmall,
                    color = RestockColors.Muted,
                )
                Spacer(Modifier.height(8.dp))
                NoSuggestionsState(tripCount = 1)
            }

            Spacer(Modifier.height(24.dp))

            // All stocked up state
            Column(modifier = Modifier.padding(horizontal = RestockSpacing.screenPadding)) {
                Text(
                    text = "Stocked Up State (trip count = 5):",
                    style = MaterialTheme.typography.labelSmall,
                    color = RestockColors.Muted,
                )
                Spacer(Modifier.height(8.dp))
                NoSuggestionsState(tripCount = 5)
            }
        }
    }
}
