package com.restock.feature.suggestions.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.CollectionInfo
import androidx.compose.ui.semantics.collectionInfo
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.restock.core.domain.model.LearningState
import com.restock.core.domain.model.Suggestion
import com.restock.core.ui.theme.RestockColors
import com.restock.core.ui.theme.RestockSpacing
import com.restock.core.ui.theme.RestockTheme
import com.restock.core.ui.util.rememberReducedMotion

// ==================== CONSTANTS ====================

/**
 * Maximum number of suggestions to display (per UX spec).
 */
private const val MAX_SUGGESTIONS = 5

// ==================== SUGGESTIONS SECTION ====================

/**
 * A section component for displaying smart suggestions with learning state integration.
 *
 * The SuggestionsSection displays either:
 * - A horizontal row of suggestion cards (when suggestions are available)
 * - A learning state card (when in LEARNING or ALMOST_READY state)
 *
 * **Layout Structure:**
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
 * **Animation Specifications (from SuggestionAnimationSpecs):**
 * | Animation | Duration | Easing |
 * |-----------|----------|--------|
 * | Section appear | 300ms | EaseOut |
 * | Section hide | 200ms | EaseIn |
 *
 * **Accessibility:**
 * - Section header uses heading semantics
 * - LazyRow uses CollectionInfo for screen reader context
 * - Respects reduced motion preferences
 *
 * **Performance:**
 * - Uses stable keys for LazyRow items
 * - Uses rememberLazyListState for scroll state preservation
 * - Applies animateItem modifier for smooth layout changes
 *
 * **Visibility Rules:**
 * - Shows when suggestions.isNotEmpty()
 * - Shows when learningState is LEARNING or ALMOST_READY
 * - Hidden when NOT_STARTED or (READY with no suggestions)
 *
 * @param suggestions List of suggestions to display (max 5 will be shown).
 * @param learningState Current learning state for the feature.
 * @param onAccept Called when a suggestion is accepted.
 * @param onDismiss Called when a suggestion is dismissed (receives itemId).
 * @param modifier Modifier to be applied to the section.
 *
 * Usage:
 * ```kotlin
 * SuggestionsSection(
 *     suggestions = listOf(
 *         Suggestion("1", "Milk", "Usually every 7 days", 1.5, 0.85, 8, 7),
 *         Suggestion("2", "Eggs", "Last bought 14 days ago", 1.8, 0.75, 14, 10),
 *     ),
 *     learningState = LearningState.READY,
 *     onAccept = { suggestion -> viewModel.accept(suggestion) },
 *     onDismiss = { itemId -> viewModel.dismiss(itemId) },
 * )
 * ```
 */
@Composable
fun SuggestionsSection(
    suggestions: List<Suggestion>,
    learningState: LearningState,
    onAccept: (Suggestion) -> Unit,
    onDismiss: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val reduceMotion = rememberReducedMotion()

    // Determine visibility: show when suggestions exist OR learning is in progress
    val showSection = suggestions.isNotEmpty() ||
        learningState in listOf(LearningState.LEARNING, LearningState.ALMOST_READY)

    // Animation duration based on reduced motion preference
    val appearDuration = if (reduceMotion) 0 else SuggestionAnimationSpecs.SECTION_APPEAR_DURATION_MS
    val hideDuration = if (reduceMotion) 0 else SuggestionAnimationSpecs.SECTION_HIDE_DURATION_MS

    AnimatedVisibility(
        visible = showSection,
        enter = fadeIn(
            animationSpec = SuggestionAnimationSpecs.sectionAppearTween(reduceMotion),
        ) + expandVertically(
            animationSpec = tween(durationMillis = appearDuration),
            expandFrom = Alignment.Top,
        ),
        exit = fadeOut(
            animationSpec = SuggestionAnimationSpecs.sectionHideTween(reduceMotion),
        ) + shrinkVertically(
            animationSpec = tween(durationMillis = hideDuration),
            shrinkTowards = Alignment.Top,
        ),
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .semantics {
                    // Group as a region for accessibility
                    if (suggestions.isNotEmpty()) {
                        collectionInfo = CollectionInfo(
                            rowCount = 1,
                            columnCount = suggestions.take(MAX_SUGGESTIONS).size,
                        )
                    }
                },
        ) {
            // Section header with heading semantics
            Text(
                text = "Suggestions",
                style = MaterialTheme.typography.titleLarge,
                color = RestockColors.Primary,
                modifier = Modifier
                    .padding(horizontal = RestockSpacing.md)
                    .semantics { heading() },
            )

            Spacer(Modifier.height(RestockSpacing.sm))

            when {
                // Show suggestion cards when available
                suggestions.isNotEmpty() -> {
                    val visibleSuggestions = suggestions.take(MAX_SUGGESTIONS)
                    val lazyListState = rememberLazyListState()

                    LazyRow(
                        state = lazyListState,
                        contentPadding = PaddingValues(horizontal = RestockSpacing.md),
                        horizontalArrangement = Arrangement.spacedBy(RestockSpacing.sm),
                    ) {
                        items(
                            items = visibleSuggestions,
                            key = { it.itemId }, // Stable keys for animation
                        ) { suggestion ->
                            AnimatedSuggestionCard(
                                suggestion = suggestion,
                                onAccept = { onAccept(suggestion) },
                                onDismiss = { onDismiss(suggestion.itemId) },
                                onAnimationComplete = { /* Handled by ViewModel */ },
                                modifier = Modifier.animateItem(),
                            )
                        }
                    }
                }
                // Show learning state when no suggestions but still learning
                learningState == LearningState.LEARNING -> {
                    LearningStateCard(
                        message = "Learning your patterns...",
                        subtext = "Add items and shop normally. Suggestions will appear soon.",
                        modifier = Modifier.padding(horizontal = RestockSpacing.md),
                    )
                }
                // Show almost ready state
                learningState == LearningState.ALMOST_READY -> {
                    LearningStateCard(
                        message = "Almost ready!",
                        subtext = "One more shopping trip and suggestions will appear.",
                        modifier = Modifier.padding(horizontal = RestockSpacing.md),
                    )
                }
            }

            Spacer(Modifier.height(RestockSpacing.md))

            // Divider at bottom
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = RestockSpacing.md),
                color = RestockColors.Muted.copy(alpha = 0.2f),
            )
        }
    }
}

// ==================== PREVIEWS ====================

@Preview(showBackground = true)
@Composable
private fun SuggestionsSectionWithSuggestionsPreview() {
    RestockTheme {
        SuggestionsSection(
            suggestions = listOf(
                Suggestion("1", "Milk", "Usually every 7 days", 1.5, 0.85, 8, 7),
                Suggestion("2", "Eggs", "Last bought 14 days ago", 1.8, 0.75, 14, 10),
                Suggestion("3", "Bread", "Usually every 5 days", 1.2, 0.80, 6, 5),
            ),
            learningState = LearningState.READY,
            onAccept = {},
            onDismiss = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SuggestionsSectionManySuggestionsPreview() {
    RestockTheme {
        SuggestionsSection(
            suggestions = listOf(
                Suggestion("1", "Milk", "Usually every 7 days", 1.5, 0.85, 8, 7),
                Suggestion("2", "Eggs", "Last bought 14 days ago", 1.8, 0.75, 14, 10),
                Suggestion("3", "Bread", "Usually every 5 days", 1.2, 0.80, 6, 5),
                Suggestion("4", "Butter", "Usually every 14 days", 1.3, 0.82, 15, 14),
                Suggestion("5", "Cheese", "Last bought 10 days ago", 1.4, 0.78, 10, 8),
            ),
            learningState = LearningState.READY,
            onAccept = {},
            onDismiss = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SuggestionsSectionLearningPreview() {
    RestockTheme {
        SuggestionsSection(
            suggestions = emptyList(),
            learningState = LearningState.LEARNING,
            onAccept = {},
            onDismiss = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SuggestionsSectionAlmostReadyPreview() {
    RestockTheme {
        SuggestionsSection(
            suggestions = emptyList(),
            learningState = LearningState.ALMOST_READY,
            onAccept = {},
            onDismiss = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SuggestionsSectionEmptyReadyPreview() {
    RestockTheme {
        Column {
            Text(
                text = "Empty READY state (section should not appear below):",
                style = MaterialTheme.typography.labelSmall,
                color = RestockColors.Muted,
                modifier = Modifier.padding(RestockSpacing.md),
            )
            SuggestionsSection(
                suggestions = emptyList(),
                learningState = LearningState.READY,
                onAccept = {},
                onDismiss = {},
            )
            Text(
                text = "(End of preview)",
                style = MaterialTheme.typography.labelSmall,
                color = RestockColors.Muted,
                modifier = Modifier.padding(RestockSpacing.md),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SuggestionsSectionNotStartedPreview() {
    RestockTheme {
        Column {
            Text(
                text = "NOT_STARTED state (section should not appear below):",
                style = MaterialTheme.typography.labelSmall,
                color = RestockColors.Muted,
                modifier = Modifier.padding(RestockSpacing.md),
            )
            SuggestionsSection(
                suggestions = emptyList(),
                learningState = LearningState.NOT_STARTED,
                onAccept = {},
                onDismiss = {},
            )
            Text(
                text = "(End of preview)",
                style = MaterialTheme.typography.labelSmall,
                color = RestockColors.Muted,
                modifier = Modifier.padding(RestockSpacing.md),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SuggestionsSectionSingleSuggestionPreview() {
    RestockTheme {
        SuggestionsSection(
            suggestions = listOf(
                Suggestion("1", "Milk", "Usually every 7 days", 1.5, 0.85, 8, 7),
            ),
            learningState = LearningState.READY,
            onAccept = {},
            onDismiss = {},
        )
    }
}
