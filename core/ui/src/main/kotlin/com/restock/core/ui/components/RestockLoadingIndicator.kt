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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.restock.core.ui.theme.RestockColors
import com.restock.core.ui.theme.RestockTheme

/**
 * Standard circular loading indicator.
 *
 * Uses the accent color by default for consistency with the design system.
 *
 * @param modifier Modifier to be applied to the indicator.
 * @param size The size of the indicator. Default is 48dp.
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

private const val PULSING_DOT_DELAY_MS = 150

/**
 * Pulsing dot loading indicator for subtle loading states.
 *
 * @param modifier Modifier to be applied to the indicator.
 * @param color The color of the dots.
 * @param dotSize The size of each dot.
 * @param dotCount The number of dots to display.
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
                                durationMillis = 600,
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
                                durationMillis = 600,
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
 * Full-screen loading state with optional message.
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
            RestockLoadingIndicator()

            if (message != null) {
                Spacer(modifier = Modifier.height(16.dp))
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

// Previews
@Preview(showBackground = true, backgroundColor = 0xFFFFFBF5)
@Composable
private fun RestockLoadingIndicatorPreview() {
    RestockTheme {
        RestockLoadingIndicator(
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFBF5)
@Composable
private fun RestockLoadingIndicatorSmallPreview() {
    RestockTheme {
        RestockLoadingIndicatorSmall(
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFBF5)
@Composable
private fun RestockPulsingDotsPreview() {
    RestockTheme {
        RestockPulsingDots(
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFBF5)
@Composable
private fun RestockLoadingStatePreview() {
    RestockTheme {
        RestockLoadingState(
            message = "Loading your groceries...",
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFBF5)
@Composable
private fun RestockShimmerBoxPreview() {
    RestockTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            RestockShimmerBox(
                modifier =
                    Modifier
                        .size(width = 200.dp, height = 24.dp),
            )
            RestockShimmerBox(
                modifier =
                    Modifier
                        .size(width = 150.dp, height = 16.dp),
            )
            RestockShimmerBox(
                modifier =
                    Modifier
                        .size(width = 180.dp, height = 16.dp),
            )
        }
    }
}
