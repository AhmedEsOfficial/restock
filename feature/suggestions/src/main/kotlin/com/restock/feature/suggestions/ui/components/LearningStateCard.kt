package com.restock.feature.suggestions.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import com.restock.core.domain.model.LearningState
import com.restock.core.ui.theme.RestockColors
import com.restock.core.ui.theme.RestockSpacing
import com.restock.core.ui.theme.RestockTheme

// ==================== LEARNING STATE CARD ====================

/**
 * A card component for displaying learning progress when no suggestions are available.
 *
 * This component shows contextual messages to the user explaining that the app
 * is learning their patterns and will soon be able to provide suggestions.
 *
 * **Design Specifications:**
 * - Background: Accent color @ 10% opacity
 * - Corner radius: Medium (14dp)
 * - Padding: 16dp (md)
 * - Emoji: Brain (🧠) indicator
 *
 * **Messages by state:**
 * - LEARNING: "Learning your patterns..." / "Add items and shop normally. Suggestions will appear soon."
 * - ALMOST_READY: "Almost ready!" / "One more shopping trip and suggestions will appear."
 *
 * @param message The primary message to display.
 * @param subtext The secondary explanatory text.
 * @param modifier Modifier to be applied to the card.
 *
 * Usage:
 * ```kotlin
 * LearningStateCard(
 *     message = "Learning your patterns...",
 *     subtext = "Add items and shop normally. Suggestions will appear soon.",
 * )
 * ```
 */
@Composable
fun LearningStateCard(
    message: String,
    subtext: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = "$message. $subtext"
            },
        shape = MaterialTheme.shapes.medium,
        color = RestockColors.Accent.copy(alpha = 0.1f),
    ) {
        Column(
            modifier = Modifier.padding(RestockSpacing.md),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
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

/**
 * Creates a [LearningStateCard] based on the current [LearningState].
 *
 * Automatically selects the appropriate message and subtext based on
 * the learning state. Only renders for LEARNING and ALMOST_READY states.
 *
 * @param learningState The current learning state.
 * @param modifier Modifier to be applied to the card.
 * @return A composable that renders the appropriate card, or nothing for other states.
 *
 * Usage:
 * ```kotlin
 * LearningStateCardForState(
 *     learningState = LearningState.LEARNING,
 * )
 * ```
 */
@Composable
fun LearningStateCardForState(
    learningState: LearningState,
    modifier: Modifier = Modifier,
) {
    when (learningState) {
        LearningState.LEARNING -> {
            LearningStateCard(
                message = "Learning your patterns...",
                subtext = "Add items and shop normally. Suggestions will appear soon.",
                modifier = modifier,
            )
        }
        LearningState.ALMOST_READY -> {
            LearningStateCard(
                message = "Almost ready!",
                subtext = "One more shopping trip and suggestions will appear.",
                modifier = modifier,
            )
        }
        LearningState.NOT_STARTED,
        LearningState.READY,
        -> {
            // Don't render anything for these states
        }
    }
}

// ==================== PREVIEWS ====================

@Preview(showBackground = true)
@Composable
private fun LearningStateCardLearningPreview() {
    RestockTheme {
        LearningStateCard(
            message = "Learning your patterns...",
            subtext = "Add items and shop normally. Suggestions will appear soon.",
            modifier = Modifier.padding(RestockSpacing.md),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LearningStateCardAlmostReadyPreview() {
    RestockTheme {
        LearningStateCard(
            message = "Almost ready!",
            subtext = "One more shopping trip and suggestions will appear.",
            modifier = Modifier.padding(RestockSpacing.md),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LearningStateCardForStateLearningPreview() {
    RestockTheme {
        LearningStateCardForState(
            learningState = LearningState.LEARNING,
            modifier = Modifier.padding(RestockSpacing.md),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LearningStateCardForStateAlmostReadyPreview() {
    RestockTheme {
        LearningStateCardForState(
            learningState = LearningState.ALMOST_READY,
            modifier = Modifier.padding(RestockSpacing.md),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LearningStateCardForStateReadyPreview() {
    RestockTheme {
        Column(modifier = Modifier.padding(RestockSpacing.md)) {
            Text(
                text = "READY state (nothing should render below):",
                style = MaterialTheme.typography.labelSmall,
                color = RestockColors.Muted,
            )
            Spacer(Modifier.height(RestockSpacing.sm))
            LearningStateCardForState(
                learningState = LearningState.READY,
            )
            Text(
                text = "(End of preview)",
                style = MaterialTheme.typography.labelSmall,
                color = RestockColors.Muted,
            )
        }
    }
}
