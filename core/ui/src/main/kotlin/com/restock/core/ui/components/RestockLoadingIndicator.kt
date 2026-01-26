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
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.restock.core.ui.preview.LightDarkPreview
import com.restock.core.ui.preview.LightPreview
import com.restock.core.ui.preview.RestockPreviewTheme
import com.restock.core.ui.theme.RestockColors
import com.restock.core.ui.theme.RestockSpacing

/**
 * Size variants for loading indicators.
 *
 * Design specs from the design system:
 * - SMALL: 20dp diameter, 2dp stroke - for inline use in buttons/list items
 * - MEDIUM: 32dp diameter, 3dp stroke - for moderate loading states
 * - LARGE: 48dp diameter, 4dp stroke - for full-screen loading states
 */
enum class LoadingIndicatorSize(
    val diameter: Dp,
    val strokeWidth: Dp,
) {
    SMALL(diameter = 20.dp, strokeWidth = 2.dp),
    MEDIUM(diameter = 32.dp, strokeWidth = 3.dp),
    LARGE(diameter = 48.dp, strokeWidth = 4.dp),
}

/**
 * Loading indicator with configurable size using the design system size variants.
 *
 * Uses the accent color by default for consistency with the design system.
 * Preferred over the Dp-based variant for design system compliance.
 *
 * @param modifier Modifier to be applied to the indicator.
 * @param size The size variant of the indicator. Default is MEDIUM.
 * @param color The color of the indicator. Default is accent color.
 */
@Composable
fun LoadingIndicator(
    modifier: Modifier = Modifier,
    size: LoadingIndicatorSize = LoadingIndicatorSize.MEDIUM,
    color: Color = RestockColors.Accent,
) {
    CircularProgressIndicator(
        modifier = modifier.size(size.diameter),
        color = color,
        strokeWidth = size.strokeWidth,
        trackColor = color.copy(alpha = 0.2f),
    )
}

/**
 * Standard circular loading indicator with raw Dp sizing.
 *
 * Uses the accent color by default for consistency with the design system.
 * For design system compliance, prefer using [LoadingIndicator] with [LoadingIndicatorSize].
 *
 * @param modifier Modifier to be applied to the indicator.
 * @param size The size of the indicator. Default is 48dp (LARGE).
 * @param color The color of the indicator. Default is accent color.
 * @param strokeWidth The width of the progress stroke.
 */
@Composable
fun RestockLoadingIndicator(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    color: Color = RestockColors.Accent,
    strokeWidth: Dp = 4.dp,
) {
    CircularProgressIndicator(
        modifier = modifier.size(size),
        color = color,
        strokeWidth = strokeWidth,
        trackColor = color.copy(alpha = 0.2f),
    )
}

/**
 * Small inline loading indicator for buttons or list items.
 *
 * @param modifier Modifier to be applied to the indicator.
 * @param color The color of the indicator.
 */
@Composable
fun RestockLoadingIndicatorSmall(
    modifier: Modifier = Modifier,
    color: Color = RestockColors.Accent,
) {
    CircularProgressIndicator(
        modifier = modifier.size(24.dp),
        color = color,
        strokeWidth = 2.dp,
        trackColor = color.copy(alpha = 0.2f),
    )
}

private const val PULSING_DOT_ANIMATION_DURATION_MS = 600
private const val PULSING_DOT_DELAY_MS = 150

/**
 * Pulsing dot loading indicator for subtle loading states.
 *
 * Alternative to the circular progress indicator for more subtle
 * loading feedback. Dots pulse with a staggered animation.
 *
 * @param modifier Modifier to be applied to the indicator.
 * @param color The color of the dots.
 * @param dotSize The size of each dot. Default is 8dp.
 * @param dotCount The number of dots to display. Default is 3.
 */
@Composable
fun RestockPulsingDots(
    modifier: Modifier = Modifier,
    color: Color = RestockColors.Accent,
    dotSize: Dp = 8.dp,
    dotCount: Int = 3,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulsing_dots")

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(dotCount) { index ->
            val delay = index * PULSING_DOT_DELAY_MS

            val scale by infiniteTransition.animateFloat(
                initialValue = 0.6f,
                targetValue = 1f,
                animationSpec =
                    infiniteRepeatable(
                        animation =
                            tween(
                                durationMillis = PULSING_DOT_ANIMATION_DURATION_MS,
                                delayMillis = delay,
                                easing = LinearEasing,
                            ),
                        repeatMode = RepeatMode.Reverse,
                    ),
                label = "dot_scale_$index",
            )

            val alpha by infiniteTransition.animateFloat(
                initialValue = 0.4f,
                targetValue = 1f,
                animationSpec =
                    infiniteRepeatable(
                        animation =
                            tween(
                                durationMillis = PULSING_DOT_ANIMATION_DURATION_MS,
                                delayMillis = delay,
                                easing = LinearEasing,
                            ),
                        repeatMode = RepeatMode.Reverse,
                    ),
                label = "dot_alpha_$index",
            )

            Box(
                modifier =
                    Modifier
                        .size(dotSize)
                        .scale(scale)
                        .background(
                            color = color.copy(alpha = alpha),
                            shape = CircleShape,
                        ),
            )
        }
    }
}

/**
 * Pulsing dots indicator - alias for [RestockPulsingDots].
 *
 * Provided for API consistency with the task specification.
 *
 * @param modifier Modifier to be applied to the indicator.
 * @param color The color of the dots.
 * @param dotSize The size of each dot. Default is 8dp.
 */
@Composable
fun PulsingDotsIndicator(
    modifier: Modifier = Modifier,
    color: Color = RestockColors.Accent,
    dotSize: Dp = 8.dp,
) {
    RestockPulsingDots(
        modifier = modifier,
        color = color,
        dotSize = dotSize,
    )
}

/**
 * Full-screen loading state with optional message.
 *
 * Uses the LARGE loading indicator centered on screen with optional
 * descriptive text below.
 *
 * @param modifier Modifier to be applied to the container.
 * @param message Optional loading message to display.
 */
@Composable
fun RestockLoadingState(
    modifier: Modifier = Modifier,
    message: String? = null,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            LoadingIndicator(size = LoadingIndicatorSize.LARGE)

            if (message != null) {
                Spacer(modifier = Modifier.height(RestockSpacing.md))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = RestockColors.Muted,
                )
            }
        }
    }
}

/**
 * Full-screen loading state - alias for [RestockLoadingState].
 *
 * Provided for API consistency with the task specification.
 *
 * @param modifier Modifier to be applied to the container.
 * @param message Optional loading message to display.
 */
@Composable
fun LoadingScreen(
    modifier: Modifier = Modifier,
    message: String? = null,
) {
    RestockLoadingState(
        modifier = modifier,
        message = message,
    )
}

/**
 * Shimmer loading placeholder for content that is loading.
 *
 * Use this for skeleton loading states where the content shape is known.
 *
 * @param modifier Modifier to be applied to the shimmer box.
 */
@Composable
fun RestockShimmerBox(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")

    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec =
            infiniteRepeatable(
                animation =
                    tween(
                        durationMillis = 1000,
                        easing = LinearEasing,
                    ),
                repeatMode = RepeatMode.Reverse,
            ),
        label = "shimmer_alpha",
    )

    Box(
        modifier =
            modifier
                .background(
                    color = RestockColors.MutedVariant.copy(alpha = alpha),
                    shape = MaterialTheme.shapes.small,
                ),
    )
}

// ==================== PREVIEWS ====================

@LightPreview
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

@LightPreview
@Composable
private fun RestockLoadingIndicatorPreview() {
    RestockPreviewTheme {
        RestockLoadingIndicator(
            modifier = Modifier.padding(16.dp),
        )
    }
}

@LightPreview
@Composable
private fun RestockLoadingIndicatorSmallPreview() {
    RestockPreviewTheme {
        RestockLoadingIndicatorSmall(
            modifier = Modifier.padding(16.dp),
        )
    }
}

@LightDarkPreview
@Composable
private fun RestockPulsingDotsPreview() {
    RestockPreviewTheme {
        RestockPulsingDots(
            modifier = Modifier.padding(16.dp),
        )
    }
}

@LightPreview
@Composable
private fun RestockLoadingStatePreview() {
    RestockPreviewTheme {
        RestockLoadingState(
            message = "Loading your groceries...",
        )
    }
}

@LightPreview
@Composable
private fun LoadingScreenPreview() {
    RestockPreviewTheme {
        LoadingScreen(
            message = "Loading your list...",
        )
    }
}

@LightPreview
@Composable
private fun RestockShimmerBoxPreview() {
    RestockPreviewTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            RestockShimmerBox(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(24.dp),
            )
            RestockShimmerBox(
                modifier =
                    Modifier
                        .fillMaxWidth(0.7f)
                        .height(16.dp),
            )
            RestockShimmerBox(
                modifier =
                    Modifier
                        .fillMaxWidth(0.85f)
                        .height(16.dp),
            )
        }
    }
}
