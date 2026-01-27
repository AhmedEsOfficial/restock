package com.restock.core.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.restock.core.ui.preview.LightDarkPreview
import com.restock.core.ui.preview.RestockPreviewTheme
import com.restock.core.ui.theme.RestockColors
import com.restock.core.ui.theme.RestockSpacing
import com.restock.core.ui.util.rememberReducedMotion

// ==================== ANIMATION CONSTANTS ====================

/**
 * Duration for the brain icon pulse animation.
 */
private const val PULSE_ANIMATION_DURATION_MS = 800

/**
 * Duration for the dot animation.
 */
private const val DOT_ANIMATION_DURATION_MS = 600

/**
 * Stagger delay between dots.
 */
private const val DOT_STAGGER_DELAY_MS = 150

// ==================== PATTERN PROCESSING INDICATOR ====================

/**
 * A subtle indicator that shows when pattern learning/processing is active.
 *
 * This component communicates to users that the app is actively learning
 * from their behavior in the background. It's designed to be non-intrusive
 * while providing transparency about the app's learning activity.
 *
 * **Design Principles:**
 * - Subtle, not distracting: Small size, muted colors
 * - Informative: Explains what's happening ("Learning patterns...")
 * - Non-blocking: Appears inline, doesn't interrupt user flow
 * - Accessible: Includes content description for screen readers
 *
 * **When to show:**
 * - During background pattern calculation (WorkManager processing)
 * - When TensorFlow Lite model is running inference
 * - While syncing learned patterns (future feature)
 *
 * @param isProcessing Whether pattern processing is currently active.
 * @param modifier Modifier to be applied to the indicator container.
 * @param message The message to display. Defaults to "Learning patterns..."
 *
 * Usage:
 * ```kotlin
 * Column {
 *     PatternProcessingIndicator(
 *         isProcessing = viewModel.isCalculatingPatterns
 *     )
 *     // Rest of your content
 *     ListContent()
 * }
 * ```
 */
@Composable
fun PatternProcessingIndicator(
    isProcessing: Boolean,
    modifier: Modifier = Modifier,
    message: String = "Learning patterns...",
) {
    val reduceMotion = rememberReducedMotion()

    AnimatedVisibility(
        visible = isProcessing,
        enter = fadeIn(tween(200)) + slideInVertically(tween(200)) { -it / 2 },
        exit = fadeOut(tween(150)) + slideOutVertically(tween(150)) { -it / 2 },
        modifier = modifier,
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = RestockColors.Accent.copy(alpha = 0.08f),
            shape = RoundedCornerShape(0.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = RestockSpacing.md,
                        vertical = RestockSpacing.sm,
                    )
                    .semantics {
                        contentDescription = "Processing: $message"
                    },
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (reduceMotion) {
                    // Static indicator for reduced motion
                    StaticProcessingIndicator()
                } else {
                    // Animated indicator
                    AnimatedProcessingIndicator()
                }

                Spacer(Modifier.width(RestockSpacing.sm))

                Text(
                    text = message,
                    style = MaterialTheme.typography.labelSmall,
                    color = RestockColors.Muted,
                )
            }
        }
    }
}

/**
 * Animated processing indicator with pulsing brain icon.
 */
@Composable
private fun AnimatedProcessingIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "processing_indicator")

    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = PULSE_ANIMATION_DURATION_MS,
                easing = LinearEasing,
            ),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "scale",
    )

    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = PULSE_ANIMATION_DURATION_MS,
                easing = LinearEasing,
            ),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "alpha",
    )

    Text(
        text = "\uD83E\uDDE0", // 🧠 brain emoji
        style = MaterialTheme.typography.labelMedium,
        modifier = Modifier
            .scale(scale)
            .alpha(alpha),
    )
}

/**
 * Static processing indicator for reduced motion preference.
 */
@Composable
private fun StaticProcessingIndicator() {
    CircularProgressIndicator(
        modifier = Modifier.size(14.dp),
        strokeWidth = 2.dp,
        color = RestockColors.Accent,
        trackColor = RestockColors.Accent.copy(alpha = 0.2f),
    )
}

// ==================== COMPACT VARIANT ====================

/**
 * A more compact pattern processing indicator for inline use.
 *
 * Useful when you need a smaller indicator that fits within other components,
 * such as in a list item or button.
 *
 * @param isProcessing Whether pattern processing is currently active.
 * @param modifier Modifier to be applied to the indicator.
 *
 * Usage:
 * ```kotlin
 * Row {
 *     Text("Patterns")
 *     CompactPatternProcessingIndicator(
 *         isProcessing = isCalculating
 *     )
 * }
 * ```
 */
@Composable
fun CompactPatternProcessingIndicator(
    isProcessing: Boolean,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = isProcessing,
        enter = fadeIn(tween(150)),
        exit = fadeOut(tween(150)),
        modifier = modifier,
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(12.dp),
            strokeWidth = 1.5.dp,
            color = RestockColors.Muted,
            trackColor = RestockColors.Muted.copy(alpha = 0.2f),
        )
    }
}

// ==================== PULSING DOTS VARIANT ====================

/**
 * A subtle pulsing dots indicator for pattern processing.
 *
 * This variant uses three dots that pulse in sequence, providing a
 * gentle visual indication of background activity.
 *
 * @param isProcessing Whether pattern processing is currently active.
 * @param modifier Modifier to be applied to the indicator.
 *
 * Usage:
 * ```kotlin
 * Row(verticalAlignment = Alignment.CenterVertically) {
 *     Text("Updating")
 *     PatternProcessingDots(isProcessing = true)
 * }
 * ```
 */
@Composable
fun PatternProcessingDots(
    isProcessing: Boolean,
    modifier: Modifier = Modifier,
) {
    val reduceMotion = rememberReducedMotion()

    AnimatedVisibility(
        visible = isProcessing,
        enter = fadeIn(tween(100)),
        exit = fadeOut(tween(100)),
        modifier = modifier,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            repeat(3) { index ->
                ProcessingDot(
                    index = index,
                    animate = !reduceMotion,
                )
            }
        }
    }
}

/**
 * A single pulsing dot for the dots indicator.
 */
@Composable
private fun ProcessingDot(
    index: Int,
    animate: Boolean,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "dot_$index")

    val alpha by infiniteTransition.animateFloat(
        initialValue = if (animate) 0.3f else 0.6f,
        targetValue = if (animate) 1f else 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = DOT_ANIMATION_DURATION_MS,
                delayMillis = index * DOT_STAGGER_DELAY_MS,
                easing = LinearEasing,
            ),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "dot_alpha_$index",
    )

    Box(
        modifier = Modifier
            .size(4.dp)
            .alpha(alpha)
            .background(
                color = RestockColors.Accent,
                shape = CircleShape,
            ),
    )
}

// ==================== PREVIEWS ====================

@LightDarkPreview
@Composable
private fun PatternProcessingIndicatorActivePreview() {
    RestockPreviewTheme {
        Column(
            modifier = Modifier.background(RestockColors.Background),
        ) {
            PatternProcessingIndicator(isProcessing = true)
            Spacer(Modifier.height(16.dp))
            Text(
                text = "List content below...",
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}

@LightDarkPreview
@Composable
private fun PatternProcessingIndicatorHiddenPreview() {
    RestockPreviewTheme {
        Column(
            modifier = Modifier.background(RestockColors.Background),
        ) {
            PatternProcessingIndicator(isProcessing = false)
            Text(
                text = "No indicator shown",
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}

@LightDarkPreview
@Composable
private fun PatternProcessingIndicatorCustomMessagePreview() {
    RestockPreviewTheme {
        Column(
            modifier = Modifier.background(RestockColors.Background),
        ) {
            PatternProcessingIndicator(
                isProcessing = true,
                message = "Updating patterns...",
            )
        }
    }
}

@LightDarkPreview
@Composable
private fun CompactPatternProcessingIndicatorPreview() {
    RestockPreviewTheme {
        Row(
            modifier = Modifier
                .background(RestockColors.Background)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("Patterns")
            CompactPatternProcessingIndicator(isProcessing = true)
        }
    }
}

@LightDarkPreview
@Composable
private fun PatternProcessingDotsPreview() {
    RestockPreviewTheme {
        Row(
            modifier = Modifier
                .background(RestockColors.Background)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = "Processing",
                style = MaterialTheme.typography.labelSmall,
                color = RestockColors.Muted,
            )
            PatternProcessingDots(isProcessing = true)
        }
    }
}

@LightDarkPreview
@Composable
private fun AllProcessingIndicatorsPreview() {
    RestockPreviewTheme {
        Column(
            modifier = Modifier
                .background(RestockColors.Background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Text(
                text = "Pattern Processing Indicators",
                style = MaterialTheme.typography.titleMedium,
            )

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Full Banner",
                    style = MaterialTheme.typography.labelSmall,
                    color = RestockColors.Muted,
                )
                PatternProcessingIndicator(isProcessing = true)
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Compact Inline",
                    style = MaterialTheme.typography.labelSmall,
                    color = RestockColors.Muted,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text("Patterns")
                    CompactPatternProcessingIndicator(isProcessing = true)
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Pulsing Dots",
                    style = MaterialTheme.typography.labelSmall,
                    color = RestockColors.Muted,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = "Updating",
                        style = MaterialTheme.typography.labelSmall,
                    )
                    PatternProcessingDots(isProcessing = true)
                }
            }
        }
    }
}
