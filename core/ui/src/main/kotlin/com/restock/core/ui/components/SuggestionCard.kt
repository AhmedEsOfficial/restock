package com.restock.core.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.tween
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
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.restock.core.ui.preview.LightDarkPreview
import com.restock.core.ui.preview.RestockPreviewTheme
import com.restock.core.ui.theme.RestockColors
import com.restock.core.ui.theme.RestockCornerRadius
import com.restock.core.ui.theme.RestockElevation
import com.restock.core.ui.theme.RestockSpacing
import com.restock.core.ui.util.rememberReducedMotion
import kotlinx.coroutines.launch

// ==================== ANIMATION CONSTANTS ====================

/**
 * Duration for card accept animation (slide up + fade).
 */
private const val ACCEPT_ANIMATION_DURATION_MS = 300

/**
 * Duration for card dismiss animation (slide right + fade).
 */
private const val DISMISS_ANIMATION_DURATION_MS = 200

/**
 * Duration for card entry animation (fade in + scale).
 */
private const val ENTRY_ANIMATION_DURATION_MS = 200

/**
 * Distance to slide up when accepting a suggestion.
 */
private const val ACCEPT_SLIDE_DISTANCE_DP = 100f

/**
 * Distance to slide right when dismissing a suggestion.
 */
private const val DISMISS_SLIDE_DISTANCE_DP = 100f

/**
 * Scale factor when card is pressed.
 */
private const val PRESSED_SCALE = 0.98f

/**
 * Initial scale for entry animation.
 */
private const val ENTRY_INITIAL_SCALE = 0.95f

/**
 * Default width for suggestion cards.
 */
private val SuggestionCardWidth = 140.dp

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
private val DismissButtonTouchTarget = 32.dp

// ==================== ANIMATION STATE ====================

/**
 * Represents the current animation state of a suggestion card.
 */
enum class SuggestionCardAnimationState {
    /** Card is visible and idle. */
    IDLE,

    /** Card is entering the screen. */
    ENTERING,

    /** Card is being accepted (moving to list). */
    ACCEPTING,

    /** Card is being dismissed. */
    DISMISSING,

    /** Card has been removed. */
    REMOVED,
}

// ==================== SUGGESTION CARD COMPONENT ====================

/**
 * A suggestion card component for displaying smart item suggestions.
 *
 * The SuggestionCard displays a suggested grocery item with:
 * - Item name prominently displayed
 * - Reason text explaining why it's suggested (e.g., "Usually every 7 days")
 * - A dismiss button for removing unwanted suggestions
 * - An accent bar indicator at the bottom
 *
 * **Design Specifications:**
 * - Width: 140dp
 * - Height: Auto (content-based)
 * - Corner radius: 16dp
 * - Padding: 12dp
 * - Shadow: 2dp elevation (level 2)
 *
 * **Animation Specifications:**
 * | Animation | Duration | Easing | Description |
 * |-----------|----------|--------|-------------|
 * | Accept | 300ms | EaseOut | Slide up 100dp + fade |
 * | Dismiss | 200ms | EaseIn | Slide right 100dp + fade |
 * | Entry | 200ms | EaseOut | Fade in + scale from 0.95 |
 * | Pressed | Instant | - | Scale to 0.98 |
 *
 * **States:**
 * - Default: Normal appearance
 * - Pressed: Slight scale (0.98) + ripple effect
 * - Dismissing: Slide right animation
 * - Accepting: Slide up animation
 *
 * @param itemName The name of the suggested item.
 * @param reason The reason for the suggestion (e.g., "Usually every 7 days").
 * @param onAccept Called when the user taps the card to accept the suggestion.
 * @param onDismiss Called when the user taps the dismiss button.
 * @param modifier Modifier to be applied to the card.
 * @param width The width of the card. Defaults to 140dp.
 * @param animateEntry Whether to animate the card entry. Defaults to true.
 * @param entryDelayMs Delay before entry animation starts (for staggered effects).
 * @param contentDescription Accessibility description for the card.
 *
 * Usage:
 * ```kotlin
 * SuggestionCard(
 *     itemName = "Milk",
 *     reason = "Usually every 7 days",
 *     onAccept = { /* add to list */ },
 *     onDismiss = { /* dismiss suggestion */ }
 * )
 * ```
 */
@Composable
fun SuggestionCard(
    itemName: String,
    reason: String,
    onAccept: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    width: Dp = SuggestionCardWidth,
    animateEntry: Boolean = true,
    entryDelayMs: Int = 0,
    contentDescription: String? = null,
) {
    val scope = rememberCoroutineScope()
    val reduceMotion = rememberReducedMotion()
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Animation state
    var animationState by remember { mutableStateOf(SuggestionCardAnimationState.IDLE) }

    // Entry animation
    val entryProgress = remember { Animatable(if (animateEntry && !reduceMotion) 0f else 1f) }

    // Accept/dismiss animation progress (0 = visible, 1 = removed)
    val exitProgress = remember { Animatable(0f) }

    // Trigger entry animation
    LaunchedEffect(Unit) {
        if (animateEntry && !reduceMotion) {
            animationState = SuggestionCardAnimationState.ENTERING
            kotlinx.coroutines.delay(entryDelayMs.toLong())
            entryProgress.animateTo(
                targetValue = 1f,
                animationSpec =
                    tween(
                        durationMillis = ENTRY_ANIMATION_DURATION_MS,
                        easing = EaseOut,
                    ),
            )
            animationState = SuggestionCardAnimationState.IDLE
        }
    }

    // Calculate visual transformations
    val currentScale =
        when {
            reduceMotion -> 1f
            animationState == SuggestionCardAnimationState.ENTERING -> {
                ENTRY_INITIAL_SCALE + (1f - ENTRY_INITIAL_SCALE) * entryProgress.value
            }
            isPressed -> PRESSED_SCALE
            else -> 1f
        }

    val alpha =
        when {
            reduceMotion -> 1f
            animationState == SuggestionCardAnimationState.ENTERING -> entryProgress.value
            animationState == SuggestionCardAnimationState.ACCEPTING ||
                animationState == SuggestionCardAnimationState.DISMISSING -> 1f - exitProgress.value
            else -> 1f
        }

    val translationY =
        when {
            reduceMotion -> 0f
            animationState == SuggestionCardAnimationState.ACCEPTING -> {
                -ACCEPT_SLIDE_DISTANCE_DP * exitProgress.value
            }
            else -> 0f
        }

    val translationX =
        when {
            reduceMotion -> 0f
            animationState == SuggestionCardAnimationState.DISMISSING -> {
                DISMISS_SLIDE_DISTANCE_DP * exitProgress.value
            }
            else -> 0f
        }

    // Handle accept animation
    fun animateAndAccept() {
        if (animationState != SuggestionCardAnimationState.IDLE) return

        scope.launch {
            if (reduceMotion) {
                onAccept()
                return@launch
            }

            animationState = SuggestionCardAnimationState.ACCEPTING
            exitProgress.snapTo(0f)
            exitProgress.animateTo(
                targetValue = 1f,
                animationSpec =
                    tween(
                        durationMillis = ACCEPT_ANIMATION_DURATION_MS,
                        easing = EaseOut,
                    ),
            )
            animationState = SuggestionCardAnimationState.REMOVED
            onAccept()
        }
    }

    // Handle dismiss animation
    fun animateAndDismiss() {
        if (animationState != SuggestionCardAnimationState.IDLE) return

        scope.launch {
            if (reduceMotion) {
                onDismiss()
                return@launch
            }

            animationState = SuggestionCardAnimationState.DISMISSING
            exitProgress.snapTo(0f)
            exitProgress.animateTo(
                targetValue = 1f,
                animationSpec =
                    tween(
                        durationMillis = DISMISS_ANIMATION_DURATION_MS,
                        easing = EaseIn,
                    ),
            )
            animationState = SuggestionCardAnimationState.REMOVED
            onDismiss()
        }
    }

    // Don't render if removed
    if (animationState == SuggestionCardAnimationState.REMOVED) {
        return
    }

    Surface(
        modifier =
            modifier
                .width(width)
                .graphicsLayer {
                    scaleX = currentScale
                    scaleY = currentScale
                    this.alpha = alpha
                    this.translationX = translationX
                    this.translationY = translationY
                }
                .clip(RoundedCornerShape(RestockCornerRadius.large))
                .clickable(
                    interactionSource = interactionSource,
                    indication = ripple(bounded = true),
                    role = Role.Button,
                    onClick = { animateAndAccept() },
                )
                .semantics {
                    contentDescription?.let {
                        this.contentDescription = it
                    } ?: run {
                        this.contentDescription = "Add $itemName to list. $reason"
                    }
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
                    onClick = { animateAndDismiss() },
                    modifier = Modifier.size(DismissButtonTouchTarget),
                    colors =
                        IconButtonDefaults.iconButtonColors(
                            contentColor = RestockColors.Muted,
                        ),
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Dismiss $itemName suggestion",
                        modifier = Modifier.size(DismissIconSize),
                    )
                }
            }

            // Item name
            Text(
                text = itemName,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(RestockSpacing.sm))

            // Reason text
            Text(
                text = reason,
                style = MaterialTheme.typography.labelMedium,
                color = RestockColors.Muted,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(RestockSpacing.sm))

            // Accent bar
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(AccentBarHeight)
                        .background(
                            color = RestockColors.Accent.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(2.dp),
                        ),
            )
        }
    }
}

/**
 * Stateless preview-friendly version of SuggestionCard without animations.
 *
 * Use this for Compose previews and testing where animations would cause issues.
 *
 * @param itemName The name of the suggested item.
 * @param reason The reason for the suggestion.
 * @param onAccept Called when the user taps the card.
 * @param onDismiss Called when the user taps the dismiss button.
 * @param modifier Modifier to be applied to the card.
 * @param width The width of the card.
 */
@Composable
fun SuggestionCardStateless(
    itemName: String,
    reason: String,
    onAccept: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    width: Dp = SuggestionCardWidth,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale = if (isPressed) PRESSED_SCALE else 1f

    Surface(
        modifier =
            modifier
                .width(width)
                .scale(scale)
                .clip(RoundedCornerShape(RestockCornerRadius.large))
                .clickable(
                    interactionSource = interactionSource,
                    indication = ripple(bounded = true),
                    role = Role.Button,
                    onClick = onAccept,
                )
                .semantics {
                    contentDescription = "Add $itemName to list. $reason"
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
                    onClick = onDismiss,
                    modifier = Modifier.size(DismissButtonTouchTarget),
                    colors =
                        IconButtonDefaults.iconButtonColors(
                            contentColor = RestockColors.Muted,
                        ),
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Dismiss $itemName suggestion",
                        modifier = Modifier.size(DismissIconSize),
                    )
                }
            }

            // Item name
            Text(
                text = itemName,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(RestockSpacing.sm))

            // Reason text
            Text(
                text = reason,
                style = MaterialTheme.typography.labelMedium,
                color = RestockColors.Muted,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(RestockSpacing.sm))

            // Accent bar
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(AccentBarHeight)
                        .background(
                            color = RestockColors.Accent.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(2.dp),
                        ),
            )
        }
    }
}

// ==================== PREVIEWS ====================

@LightDarkPreview
@Composable
private fun SuggestionCardPreview() {
    RestockPreviewTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            SuggestionCardStateless(
                itemName = "Milk",
                reason = "Usually every 7 days",
                onAccept = {},
                onDismiss = {},
            )
        }
    }
}

@LightDarkPreview
@Composable
private fun SuggestionCardLongNamePreview() {
    RestockPreviewTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            SuggestionCardStateless(
                itemName = "Extra Large Organic Free-Range Eggs",
                reason = "Last bought 20 days ago",
                onAccept = {},
                onDismiss = {},
            )
        }
    }
}

@LightDarkPreview
@Composable
private fun SuggestionCardMultiplePreview() {
    RestockPreviewTheme {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SuggestionCardStateless(
                itemName = "Milk",
                reason = "Usually every 7 days",
                onAccept = {},
                onDismiss = {},
            )
            SuggestionCardStateless(
                itemName = "Eggs",
                reason = "Last bought 20 days ago",
                onAccept = {},
                onDismiss = {},
            )
            SuggestionCardStateless(
                itemName = "Bread",
                reason = "Usually every 5 days",
                onAccept = {},
                onDismiss = {},
            )
        }
    }
}

@LightDarkPreview
@Composable
private fun SuggestionCardInteractivePreview() {
    RestockPreviewTheme {
        var cards by remember {
            mutableStateOf(
                listOf(
                    "Milk" to "Usually every 7 days",
                    "Eggs" to "Last bought 20 days ago",
                    "Bread" to "Usually every 5 days",
                ),
            )
        }

        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Interactive Suggestion Cards",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 16.dp),
            )

            if (cards.isEmpty()) {
                Text(
                    text = "All suggestions handled!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = RestockColors.Muted,
                )
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    cards.forEach { (name, reason) ->
                        SuggestionCard(
                            itemName = name,
                            reason = reason,
                            onAccept = {
                                cards = cards.filter { it.first != name }
                            },
                            onDismiss = {
                                cards = cards.filter { it.first != name }
                            },
                            animateEntry = false,
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Text(
                text = "Tap card to accept, tap X to dismiss",
                style = MaterialTheme.typography.bodySmall,
                color = RestockColors.Muted,
            )
        }
    }
}

@LightDarkPreview
@Composable
private fun SuggestionCardSizesPreview() {
    RestockPreviewTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Card Sizes",
                style = MaterialTheme.typography.titleMedium,
            )

            Text(
                text = "Default (140dp)",
                style = MaterialTheme.typography.labelSmall,
                color = RestockColors.Muted,
            )
            SuggestionCardStateless(
                itemName = "Milk",
                reason = "Usually every 7 days",
                onAccept = {},
                onDismiss = {},
            )

            Text(
                text = "Wide (180dp)",
                style = MaterialTheme.typography.labelSmall,
                color = RestockColors.Muted,
            )
            SuggestionCardStateless(
                itemName = "Milk",
                reason = "Usually every 7 days",
                onAccept = {},
                onDismiss = {},
                width = 180.dp,
            )
        }
    }
}
