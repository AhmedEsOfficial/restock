package com.restock.feature.suggestions.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.restock.core.domain.model.Suggestion
import com.restock.core.ui.theme.RestockColors
import com.restock.core.ui.theme.RestockCornerRadius
import com.restock.core.ui.theme.RestockElevation
import com.restock.core.ui.theme.RestockSpacing
import com.restock.core.ui.theme.RestockTheme

// ==================== DESIGN CONSTANTS ====================

/**
 * Default width for suggestion cards (140dp as per design spec).
 */
val SuggestionCardWidth = 140.dp

/**
 * Height of the accent indicator bar at bottom of card.
 */
private val AccentBarHeight = 3.dp

/**
 * Size of the dismiss icon.
 */
private val DismissIconSize = 16.dp

/**
 * Touch target size for dismiss button (accessibility).
 */
private val DismissButtonTouchTarget = 24.dp

/**
 * Scale factor when card is pressed (0.95 as per task spec).
 */
private const val PRESSED_SCALE = 0.95f

// ==================== SUGGESTION CARD COMPONENT ====================

/**
 * A suggestion card component for displaying smart item suggestions.
 *
 * The SuggestionCard displays a suggested grocery item with:
 * - Item name prominently displayed
 * - Reason text explaining why it's suggested (e.g., "Usually every 7 days")
 * - A dismiss button for removing unwanted suggestions
 * - An accent bar indicator at the bottom
 * - Haptic feedback on interactions
 *
 * **Design Specifications:**
 * - Width: 140dp (configurable)
 * - Height: Auto (content-based)
 * - Corner radius: 16dp
 * - Padding: 12dp
 * - Shadow: 2dp elevation
 *
 * **Interaction Specs:**
 * - Press: Scale to 0.95 with spring animation
 * - Tap card: Accept suggestion (haptic: LongPress)
 * - Tap X: Dismiss suggestion (haptic: TextHandleMove)
 *
 * @param suggestion The domain suggestion model to display.
 * @param onAccept Called when the user taps the card to accept the suggestion.
 * @param onDismiss Called when the user taps the dismiss button.
 * @param modifier Modifier to be applied to the card.
 * @param width The width of the card. Defaults to 140dp.
 *
 * Usage:
 * ```kotlin
 * SuggestionCard(
 *     suggestion = Suggestion(
 *         itemId = "1",
 *         itemName = "Milk",
 *         reason = "Usually every 7 days",
 *         score = 1.5,
 *         confidence = 0.85,
 *         daysSinceLastPurchase = 8,
 *         avgIntervalDays = 7,
 *     ),
 *     onAccept = { /* add to list */ },
 *     onDismiss = { /* dismiss suggestion */ },
 * )
 * ```
 */
@Composable
fun SuggestionCard(
    suggestion: Suggestion,
    onAccept: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    width: Dp = SuggestionCardWidth,
) {
    val hapticFeedback = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Press animation with spring physics
    val scale by animateFloatAsState(
        targetValue = if (isPressed) PRESSED_SCALE else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow,
        ),
        label = "cardScale",
    )

    Surface(
        modifier = modifier
            .width(width)
            .scale(scale)
            .clip(RoundedCornerShape(RestockCornerRadius.large))
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = true),
                role = Role.Button,
                onClick = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    onAccept()
                },
            )
            .semantics {
                contentDescription = "Add ${suggestion.itemName} to list. ${suggestion.reason}"
            },
        shape = RoundedCornerShape(RestockCornerRadius.large),
        color = RestockColors.Card,
        shadowElevation = RestockElevation.suggestionCard,
    ) {
        Column(
            modifier = Modifier.padding(RestockSpacing.cardPaddingHorizontal),
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
                    modifier = Modifier.size(DismissButtonTouchTarget),
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = RestockColors.Muted,
                    ),
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Dismiss ${suggestion.itemName} suggestion",
                        modifier = Modifier.size(DismissIconSize),
                    )
                }
            }

            Spacer(Modifier.height(RestockSpacing.xs))

            // Item name
            androidx.compose.material3.Text(
                text = suggestion.itemName,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = RestockColors.Primary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(RestockSpacing.sm))

            // Reason text
            androidx.compose.material3.Text(
                text = suggestion.reason,
                style = MaterialTheme.typography.labelMedium,
                color = RestockColors.Muted,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(RestockSpacing.sm))

            // Accent indicator bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(AccentBarHeight)
                    .background(
                        RestockColors.Accent.copy(alpha = 0.5f),
                        RoundedCornerShape(2.dp),
                    ),
            )
        }
    }
}

// ==================== PREVIEWS ====================

@Preview(showBackground = true)
@Composable
private fun SuggestionCardPreview() {
    RestockTheme {
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
    RestockTheme {
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

@Preview(showBackground = true)
@Composable
private fun SuggestionCardMultiplePreview() {
    RestockTheme {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
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
            )
            SuggestionCard(
                suggestion = Suggestion(
                    itemId = "2",
                    itemName = "Eggs",
                    reason = "Last bought 14 days ago",
                    score = 1.8,
                    confidence = 0.75,
                    daysSinceLastPurchase = 14,
                    avgIntervalDays = 10,
                ),
                onAccept = {},
                onDismiss = {},
            )
            SuggestionCard(
                suggestion = Suggestion(
                    itemId = "3",
                    itemName = "Bread",
                    reason = "Usually every 5 days",
                    score = 1.2,
                    confidence = 0.80,
                    daysSinceLastPurchase = 6,
                    avgIntervalDays = 5,
                ),
                onAccept = {},
                onDismiss = {},
            )
        }
    }
}
