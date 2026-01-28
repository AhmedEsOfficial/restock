package com.restock.feature.suggestions.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import com.restock.core.domain.model.Suggestion
import com.restock.core.ui.theme.RestockTheme
import com.restock.core.ui.util.rememberReducedMotion

// ==================== ANIMATION STATE ====================

/**
 * Represents the current animation state of a suggestion card.
 *
 * Follows the state machine:
 * VISIBLE -> ACCEPTING -> HIDDEN (on accept)
 * VISIBLE -> DISMISSING -> HIDDEN (on dismiss)
 */
enum class SuggestionCardState {
    /** Card is visible and interactive. */
    VISIBLE,

    /** Card is animating acceptance (sliding up). */
    ACCEPTING,

    /** Card is animating dismissal (sliding right). */
    DISMISSING,

    /** Card has been removed from view. */
    HIDDEN,
}

// ==================== ANIMATED SUGGESTION CARD ====================

/**
 * A wrapper component that adds accept/dismiss animations to SuggestionCard.
 *
 * This component manages the animation state and transitions for suggestion cards:
 * - **Accept animation:** Slides up and fades out (uses SuggestionAnimationSpecs)
 * - **Dismiss animation:** Slides right and fades out (uses SuggestionAnimationSpecs)
 *
 * The component respects reduced motion accessibility settings and will
 * skip animations when the user has enabled reduced motion.
 *
 * **Animation Specifications (from SuggestionAnimationSpecs):**
 * | Animation | Duration | Easing | Movement |
 * |-----------|----------|--------|----------|
 * | Accept | 300ms | EaseOut | Slide up -100px |
 * | Dismiss | 200ms | EaseIn | Slide right +100px |
 *
 * @param suggestion The domain suggestion model to display.
 * @param onAccept Called when the user accepts the suggestion (tap card).
 * @param onDismiss Called when the user dismisses the suggestion (tap X).
 * @param onAnimationComplete Called when accept/dismiss animation completes.
 * @param modifier Modifier to be applied to the card.
 *
 * Usage:
 * ```kotlin
 * AnimatedSuggestionCard(
 *     suggestion = suggestion,
 *     onAccept = { viewModel.acceptSuggestion(suggestion) },
 *     onDismiss = { viewModel.dismissSuggestion(suggestion.itemId) },
 *     onAnimationComplete = { /* Remove from list */ },
 * )
 * ```
 */
@Composable
fun AnimatedSuggestionCard(
    suggestion: Suggestion,
    onAccept: () -> Unit,
    onDismiss: () -> Unit,
    onAnimationComplete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val reduceMotion = rememberReducedMotion()
    var cardState by remember { mutableStateOf(SuggestionCardState.VISIBLE) }

    // Accept animation: slide up and fade out
    val acceptOffsetY by animateFloatAsState(
        targetValue = when (cardState) {
            SuggestionCardState.ACCEPTING -> SuggestionAnimationSpecs.ACCEPT_SLIDE_DISTANCE
            else -> 0f
        },
        animationSpec = SuggestionAnimationSpecs.acceptTween(reduceMotion),
        finishedListener = {
            if (cardState == SuggestionCardState.ACCEPTING) {
                cardState = SuggestionCardState.HIDDEN
                onAnimationComplete()
            }
        },
        label = "acceptOffsetY",
    )

    // Dismiss animation: slide right and fade out
    val dismissOffsetX by animateFloatAsState(
        targetValue = when (cardState) {
            SuggestionCardState.DISMISSING -> SuggestionAnimationSpecs.DISMISS_SLIDE_DISTANCE
            else -> 0f
        },
        animationSpec = SuggestionAnimationSpecs.dismissTween(reduceMotion),
        finishedListener = {
            if (cardState == SuggestionCardState.DISMISSING) {
                cardState = SuggestionCardState.HIDDEN
                onAnimationComplete()
            }
        },
        label = "dismissOffsetX",
    )

    // Alpha animation: fade out during accept/dismiss
    val alpha by animateFloatAsState(
        targetValue = when (cardState) {
            SuggestionCardState.ACCEPTING,
            SuggestionCardState.DISMISSING,
            -> 0f
            SuggestionCardState.VISIBLE -> 1f
            SuggestionCardState.HIDDEN -> 0f
        },
        animationSpec = SuggestionAnimationSpecs.alphaTween(cardState, reduceMotion),
        label = "alpha",
    )

    // Don't render if hidden
    if (cardState == SuggestionCardState.HIDDEN) {
        return
    }

    SuggestionCard(
        suggestion = suggestion,
        onAccept = {
            if (cardState == SuggestionCardState.VISIBLE) {
                if (reduceMotion) {
                    // Skip animation, call immediately
                    onAccept()
                    cardState = SuggestionCardState.HIDDEN
                    onAnimationComplete()
                } else {
                    cardState = SuggestionCardState.ACCEPTING
                    onAccept()
                }
            }
        },
        onDismiss = {
            if (cardState == SuggestionCardState.VISIBLE) {
                if (reduceMotion) {
                    // Skip animation, call immediately
                    onDismiss()
                    cardState = SuggestionCardState.HIDDEN
                    onAnimationComplete()
                } else {
                    cardState = SuggestionCardState.DISMISSING
                    onDismiss()
                }
            }
        },
        modifier = modifier.graphicsLayer {
            translationY = acceptOffsetY
            translationX = dismissOffsetX
            this.alpha = alpha
        },
    )
}

// ==================== PREVIEWS ====================

@Preview(showBackground = true)
@Composable
private fun AnimatedSuggestionCardPreview() {
    RestockTheme {
        AnimatedSuggestionCard(
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
            onAnimationComplete = {},
        )
    }
}

@Preview(showBackground = true, name = "Long Name")
@Composable
private fun AnimatedSuggestionCardLongNamePreview() {
    RestockTheme {
        AnimatedSuggestionCard(
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
            onAnimationComplete = {},
        )
    }
}
