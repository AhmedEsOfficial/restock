# Task: Implement Suggestions UI Components

**Sprint:** 7-8 (Smart Suggestions)
**Owner:** UI/UX Engineer
**Estimate:** 22 hours (D-7.1 through D-7.6, D-8.1 through D-8.5 combined)
**Priority:** P0
**Prerequisites:** Sprint 5-6 UI/UX tasks (designs prepared)

---

## Sprint Context

This sprint surfaces the intelligence built in Sprint 5-6 to users through smart suggestions. The UI must be delightful, non-intrusive, and feel like magic.

### Sprint Theme
> *"The moment when the app anticipates your needs."*

### Key UX Principles
1. **Helpful, not pushy:** Maximum 5 suggestions, easy dismiss
2. **One-tap add:** Tapping suggestion adds to list instantly
3. **Clear affordances:** Obvious how to accept/dismiss
4. **Smooth animations:** Every interaction should feel polished

---

## Task Description

Implement the SuggestionCard component, SuggestionsRow, and all associated animations.

## Implementation

### SuggestionCard.kt

```kotlin
package com.restock.feature.suggestions.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.restock.core.domain.model.Suggestion
import com.restock.core.ui.theme.RestockColors
import com.restock.core.ui.theme.RestockPreviewTheme
import com.restock.core.ui.theme.RestockSpacing

@Composable
fun SuggestionCard(
    suggestion: Suggestion,
    onAccept: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val hapticFeedback = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }

    // Press animation
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow,
        ),
        label = "cardScale"
    )

    Surface(
        modifier = modifier
            .width(140.dp)
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
            ) {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                onAccept()
            },
        shape = RoundedCornerShape(16.dp),
        color = RestockColors.Card,
        shadowElevation = 2.dp,
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
        ) {
            // Header with dismiss button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                IconButton(
                    onClick = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onDismiss()
                    },
                    modifier = Modifier.size(24.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Dismiss ${suggestion.itemName} suggestion",
                        tint = RestockColors.Muted,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }

            Spacer(Modifier.height(4.dp))

            // Item name
            Text(
                text = suggestion.itemName,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = RestockColors.Primary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(8.dp))

            // Reason text
            Text(
                text = suggestion.reason,
                style = MaterialTheme.typography.labelMedium,
                color = RestockColors.Muted,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(8.dp))

            // Accent indicator bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(
                        RestockColors.Accent.copy(alpha = 0.5f),
                        RoundedCornerShape(2.dp)
                    )
            )
        }
    }
}

// ==================== PREVIEWS ====================

@Preview(showBackground = true)
@Composable
private fun SuggestionCardPreview() {
    RestockPreviewTheme {
        SuggestionCard(
            suggestion = Suggestion(
                itemId = "1",
                itemName = "Milk",
                reason = "Usually every 7 days",
                score = 1.5,
                confidence = 0.85,
                daysSinceLastPurchase = 8,
                avgIntervalDays = 7,
            ),
            onAccept = {},
            onDismiss = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SuggestionCardLongNamePreview() {
    RestockPreviewTheme {
        SuggestionCard(
            suggestion = Suggestion(
                itemId = "2",
                itemName = "Organic Free-Range Eggs",
                reason = "Last bought 20 days ago",
                score = 1.8,
                confidence = 0.75,
                daysSinceLastPurchase = 20,
                avgIntervalDays = 14,
            ),
            onAccept = {},
            onDismiss = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}
```

### AnimatedSuggestionCard.kt

```kotlin
package com.restock.feature.suggestions.ui.components

import androidx.compose.animation.core.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import com.restock.core.domain.model.Suggestion

enum class SuggestionCardState {
    VISIBLE,
    ACCEPTING,
    DISMISSING,
    HIDDEN,
}

@Composable
fun AnimatedSuggestionCard(
    suggestion: Suggestion,
    onAccept: () -> Unit,
    onDismiss: () -> Unit,
    onAnimationComplete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var cardState by remember { mutableStateOf(SuggestionCardState.VISIBLE) }

    // Accept animation: slide up and fade out
    val acceptOffsetY by animateFloatAsState(
        targetValue = when (cardState) {
            SuggestionCardState.ACCEPTING -> -100f
            else -> 0f
        },
        animationSpec = tween(300, easing = EaseOut),
        finishedListener = {
            if (cardState == SuggestionCardState.ACCEPTING) {
                cardState = SuggestionCardState.HIDDEN
                onAnimationComplete()
            }
        },
        label = "acceptOffsetY"
    )

    // Dismiss animation: slide right and fade out
    val dismissOffsetX by animateFloatAsState(
        targetValue = when (cardState) {
            SuggestionCardState.DISMISSING -> 100f
            else -> 0f
        },
        animationSpec = tween(200, easing = EaseIn),
        finishedListener = {
            if (cardState == SuggestionCardState.DISMISSING) {
                cardState = SuggestionCardState.HIDDEN
                onAnimationComplete()
            }
        },
        label = "dismissOffsetX"
    )

    // Alpha animation
    val alpha by animateFloatAsState(
        targetValue = when (cardState) {
            SuggestionCardState.ACCEPTING,
            SuggestionCardState.DISMISSING -> 0f
            SuggestionCardState.VISIBLE -> 1f
            SuggestionCardState.HIDDEN -> 0f
        },
        animationSpec = tween(
            durationMillis = when (cardState) {
                SuggestionCardState.ACCEPTING -> 300
                SuggestionCardState.DISMISSING -> 200
                else -> 200
            }
        ),
        label = "alpha"
    )

    if (cardState != SuggestionCardState.HIDDEN) {
        SuggestionCard(
            suggestion = suggestion,
            onAccept = {
                cardState = SuggestionCardState.ACCEPTING
                onAccept()
            },
            onDismiss = {
                cardState = SuggestionCardState.DISMISSING
                onDismiss()
            },
            modifier = modifier.graphicsLayer {
                translationY = acceptOffsetY
                translationX = dismissOffsetX
                this.alpha = alpha
            }
        )
    }
}
```

### SuggestionsSection.kt

```kotlin
package com.restock.feature.suggestions.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.restock.core.domain.model.Suggestion
import com.restock.core.ui.theme.RestockColors
import com.restock.core.ui.theme.RestockSpacing
import com.restock.feature.suggestions.ui.LearningState

@Composable
fun SuggestionsSection(
    suggestions: List<Suggestion>,
    learningState: LearningState,
    onAccept: (Suggestion) -> Unit,
    onDismiss: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val showSection = suggestions.isNotEmpty() ||
        learningState in listOf(LearningState.LEARNING, LearningState.ALMOST_READY)

    AnimatedVisibility(
        visible = showSection,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
    ) {
        Column(modifier = modifier) {
            // Section header
            Text(
                text = "Suggestions",
                style = MaterialTheme.typography.titleLarge,
                color = RestockColors.Primary,
                modifier = Modifier.padding(horizontal = RestockSpacing.md),
            )

            Spacer(Modifier.height(RestockSpacing.sm))

            when {
                suggestions.isNotEmpty() -> {
                    // Horizontal scrolling suggestions
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = RestockSpacing.md),
                        horizontalArrangement = Arrangement.spacedBy(RestockSpacing.sm),
                    ) {
                        items(
                            items = suggestions,
                            key = { it.itemId }
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
                learningState == LearningState.LEARNING -> {
                    LearningStateCard(
                        message = "Learning your patterns...",
                        subtext = "Add items and shop normally. Suggestions will appear soon.",
                        modifier = Modifier.padding(horizontal = RestockSpacing.md),
                    )
                }
                learningState == LearningState.ALMOST_READY -> {
                    LearningStateCard(
                        message = "Almost ready!",
                        subtext = "One more shopping trip and suggestions will appear.",
                        modifier = Modifier.padding(horizontal = RestockSpacing.md),
                    )
                }
            }

            Spacer(Modifier.height(RestockSpacing.md))

            // Divider
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = RestockSpacing.md),
                color = RestockColors.Muted.copy(alpha = 0.2f),
            )
        }
    }
}

@Composable
fun LearningStateCard(
    message: String,
    subtext: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = RestockColors.Accent.copy(alpha = 0.1f),
    ) {
        Column(
            modifier = Modifier.padding(RestockSpacing.md),
        ) {
            Row {
                Text(
                    text = "🧠",
                    style = MaterialTheme.typography.titleMedium,
                )

                Spacer(Modifier.width(RestockSpacing.sm))

                Text(
                    text = message,
                    style = MaterialTheme.typography.titleSmall,
                    color = RestockColors.Primary,
                )
            }

            Spacer(Modifier.height(RestockSpacing.xs))

            Text(
                text = subtext,
                style = MaterialTheme.typography.bodySmall,
                color = RestockColors.Muted,
            )
        }
    }
}

// ==================== PREVIEWS ====================

@Preview(showBackground = true)
@Composable
private fun SuggestionsSectionPreview() {
    RestockPreviewTheme {
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
private fun SuggestionsSectionLearningPreview() {
    RestockPreviewTheme {
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
    RestockPreviewTheme {
        SuggestionsSection(
            suggestions = emptyList(),
            learningState = LearningState.ALMOST_READY,
            onAccept = {},
            onDismiss = {},
        )
    }
}
```

## Acceptance Criteria

- [ ] SuggestionCard component implemented
- [ ] Dismiss X button works
- [ ] Accept (tap card) works
- [ ] Animations smooth (accept: slide up, dismiss: slide right)
- [ ] SuggestionsSection with horizontal scroll
- [ ] Learning state cards implemented
- [ ] Haptic feedback on interactions
- [ ] All previews render correctly

## Definition of Done

- [ ] All components compile without errors
- [ ] Animations are smooth (60fps)
- [ ] Accessibility supported
- [ ] Touch targets meet 48dp minimum
- [ ] Previews for all states
- [ ] Integrates with ListScreen

## Notes

- Use `animateItem()` for smooth list animations
- Haptic feedback adds polish
- Keep card width fixed (140dp) for consistent layout
- Accent color bar provides visual distinction
