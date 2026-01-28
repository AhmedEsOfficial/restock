package com.restock.core.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.restock.core.ui.preview.LightDarkPreview
import com.restock.core.ui.preview.RestockPreviewTheme
import com.restock.core.ui.theme.RestockColors
import com.restock.core.ui.theme.RestockSpacing
import com.restock.core.ui.util.rememberReducedMotion
import kotlin.math.sin
import kotlin.random.Random

// ==================== ANIMATION CONSTANTS ====================

/**
 * Total duration of the celebration animation in milliseconds.
 */
private const val CELEBRATION_DURATION_MS = 2500

/**
 * Duration for the message fade out in milliseconds.
 */
private const val MESSAGE_FADE_OUT_MS = 400

/**
 * Number of confetti pieces to generate.
 */
private const val CONFETTI_COUNT = 60

/**
 * Gravity acceleration for confetti fall.
 */
private const val GRAVITY = 0.15f

/**
 * Maximum horizontal drift for confetti.
 */
private const val MAX_DRIFT = 3f

// ==================== CONFETTI DATA ====================

/**
 * Represents a single confetti piece with physics properties.
 *
 * @param x Initial X position (normalized 0-1 of screen width).
 * @param y Initial Y position (starts above screen).
 * @param velocityX Horizontal velocity.
 * @param velocityY Initial vertical velocity.
 * @param size Size of the confetti piece.
 * @param color Color of the confetti piece.
 * @param rotation Initial rotation angle.
 * @param rotationSpeed Speed of rotation.
 * @param shape Shape type (0 = rectangle, 1 = square, 2 = circle).
 */
private data class ConfettiPiece(
    val x: Float,
    val y: Float,
    val velocityX: Float,
    val velocityY: Float,
    val size: Float,
    val color: Color,
    val rotation: Float,
    val rotationSpeed: Float,
    val shape: Int,
)

/**
 * Generates a list of confetti pieces with varied colors and properties.
 *
 * @param screenWidth The width of the screen in pixels.
 * @return List of [ConfettiPiece] with randomized properties.
 */
private fun generateConfetti(screenWidth: Float): List<ConfettiPiece> {
    val colors =
        listOf(
            RestockColors.Success,
            RestockColors.Accent,
            RestockColors.Breakfast,
            RestockColors.Lunch,
            RestockColors.Dinner,
            RestockColors.Snack,
            Color(0xFF60A5FA), // Light blue
            Color(0xFFA78BFA), // Purple
        )

    return List(CONFETTI_COUNT) {
        ConfettiPiece(
            x = Random.nextFloat() * screenWidth,
            y = -Random.nextFloat() * 200f - 50f,
            velocityX = Random.nextFloat() * MAX_DRIFT * 2 - MAX_DRIFT,
            velocityY = Random.nextFloat() * 2f + 1f,
            size = Random.nextFloat() * 10f + 6f,
            color = colors.random(),
            rotation = Random.nextFloat() * 360f,
            rotationSpeed = Random.nextFloat() * 8f - 4f,
            shape = Random.nextInt(3),
        )
    }
}

/**
 * Calculates the position of a confetti piece at a given progress.
 *
 * @param piece The confetti piece.
 * @param progress Animation progress (0-1).
 * @param screenHeight Screen height in pixels.
 * @return Pair of (x, y) position and rotation angle.
 */
private fun calculateConfettiPosition(
    piece: ConfettiPiece,
    progress: Float,
    screenHeight: Float,
): Triple<Float, Float, Float> {
    val time = progress * 3f // Scale time for natural fall speed

    // Apply physics: y = y0 + v*t + 0.5*g*t^2
    val y = piece.y + piece.velocityY * time * 200f + 0.5f * GRAVITY * time * time * 1000f

    // Horizontal drift with sinusoidal wobble
    val wobble = sin(progress * 10f + piece.rotation) * 20f
    val x = piece.x + piece.velocityX * progress * 100f + wobble

    // Rotation
    val rotation = piece.rotation + piece.rotationSpeed * progress * 360f

    return Triple(x, y.coerceAtMost(screenHeight + 50f), rotation)
}

// ==================== CELEBRATION COMPONENT ====================

/**
 * A celebration animation that plays when the user completes their shopping list.
 *
 * Displays a confetti shower with a brief congratulatory message when all items
 * have been checked off. The animation is non-blocking and auto-dismisses after
 * completion, or can be dismissed early by tapping anywhere.
 *
 * **Design Principles:**
 * - Delightful: Creates a moment of celebration and accomplishment
 * - Non-blocking: Can be dismissed immediately, doesn't prevent interaction
 * - Accessible: Respects reduced motion settings, provides content description
 * - Subtle: Uses app brand colors, not overwhelming
 *
 * **Animation Specs:**
 * - Total duration: 2.5 seconds
 * - Confetti count: 60 pieces
 * - Physics: Gravity-based fall with horizontal drift
 * - Message: Shows for first 80% of animation, then fades
 *
 * @param show Whether to show the celebration animation.
 * @param onDismiss Called when the animation completes or is dismissed.
 * @param modifier Modifier to be applied to the celebration overlay.
 * @param message The congratulatory message to display. Defaults to "All done!".
 *
 * Usage:
 * ```kotlin
 * var showCelebration by remember { mutableStateOf(false) }
 *
 * Box {
 *     // Your list content
 *     GroceryList(...)
 *
 *     // Celebration overlay
 *     ListCompleteCelebration(
 *         show = showCelebration,
 *         onDismiss = { showCelebration = false }
 *     )
 * }
 *
 * // Trigger when list is complete
 * LaunchedEffect(allItemsChecked) {
 *     if (allItemsChecked) {
 *         showCelebration = true
 *     }
 * }
 * ```
 */
@Composable
fun ListCompleteCelebration(
    show: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    message: String = "All done!",
) {
    val reduceMotion = rememberReducedMotion()

    // If reduced motion is enabled, show a simple message without animation
    if (reduceMotion) {
        ReducedMotionCelebration(
            show = show,
            onDismiss = onDismiss,
            message = message,
            modifier = modifier,
        )
        return
    }

    // Full animation
    FullCelebration(
        show = show,
        onDismiss = onDismiss,
        message = message,
        modifier = modifier,
    )
}

/**
 * Full celebration with confetti animation.
 */
@Composable
private fun FullCelebration(
    show: Boolean,
    onDismiss: () -> Unit,
    message: String,
    modifier: Modifier = Modifier,
) {
    if (!show) return

    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val screenWidth = with(density) { configuration.screenWidthDp.dp.toPx() }
    val screenHeight = with(density) { configuration.screenHeightDp.dp.toPx() }

    // Generate confetti pieces
    val confettiPieces = remember { generateConfetti(screenWidth) }

    // Animation progress
    val progress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec =
                tween(
                    durationMillis = CELEBRATION_DURATION_MS,
                    easing = EaseOut,
                ),
        )
        onDismiss()
    }

    // Message visibility
    val showMessage = progress.value < 0.8f
    val messageAlpha =
        if (progress.value > 0.7f) {
            1f - ((progress.value - 0.7f) / 0.1f).coerceIn(0f, 1f)
        } else {
            1f
        }

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss,
                )
                .semantics {
                    contentDescription = "Celebration: $message. Tap to dismiss."
                },
        contentAlignment = Alignment.Center,
    ) {
        // Confetti layer
        Canvas(modifier = Modifier.fillMaxSize()) {
            confettiPieces.forEach { piece ->
                val (x, y, rotation) =
                    calculateConfettiPosition(
                        piece = piece,
                        progress = progress.value,
                        screenHeight = screenHeight,
                    )

                // Fade out as confetti falls off screen
                val alpha =
                    if (y > screenHeight * 0.8f) {
                        1f - ((y - screenHeight * 0.8f) / (screenHeight * 0.2f)).coerceIn(0f, 1f)
                    } else {
                        (1f - progress.value * 0.3f).coerceIn(0f, 1f)
                    }

                // Draw based on shape
                when (piece.shape) {
                    0 -> { // Rectangle
                        drawRect(
                            color = piece.color.copy(alpha = alpha),
                            topLeft = Offset(x, y),
                            size = Size(piece.size, piece.size * 1.5f),
                        )
                    }
                    1 -> { // Square
                        drawRect(
                            color = piece.color.copy(alpha = alpha),
                            topLeft = Offset(x, y),
                            size = Size(piece.size, piece.size),
                        )
                    }
                    2 -> { // Circle
                        drawCircle(
                            color = piece.color.copy(alpha = alpha),
                            radius = piece.size / 2,
                            center = Offset(x + piece.size / 2, y + piece.size / 2),
                        )
                    }
                }
            }
        }

        // Message overlay
        if (showMessage) {
            CelebrationMessage(
                message = message,
                alpha = messageAlpha,
            )
        }
    }
}

/**
 * Reduced motion alternative - simple fade in/out message.
 */
@Composable
private fun ReducedMotionCelebration(
    show: Boolean,
    onDismiss: () -> Unit,
    message: String,
    modifier: Modifier = Modifier,
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(show) {
        if (show) {
            visible = true
            kotlinx.coroutines.delay(2000)
            visible = false
            kotlinx.coroutines.delay(MESSAGE_FADE_OUT_MS.toLong())
            onDismiss()
        }
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(200)),
        exit = fadeOut(tween(MESSAGE_FADE_OUT_MS)),
        modifier = modifier.fillMaxSize(),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {
                            visible = false
                            onDismiss()
                        },
                    )
                    .semantics {
                        contentDescription = "Celebration: $message. Tap to dismiss."
                    },
            contentAlignment = Alignment.Center,
        ) {
            CelebrationMessage(message = message, alpha = 1f)
        }
    }
}

/**
 * The congratulatory message displayed during celebration.
 */
@Composable
private fun CelebrationMessage(
    message: String,
    alpha: Float,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .graphicsLayer { this.alpha = alpha }
                .clip(RoundedCornerShape(16.dp))
                .background(RestockColors.Surface.copy(alpha = 0.95f))
                .padding(horizontal = RestockSpacing.lg, vertical = RestockSpacing.md),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(RestockSpacing.xs),
        ) {
            Text(
                text = "\uD83C\uDF89", // 🎉 party popper
                style = MaterialTheme.typography.headlineLarge,
            )
            Text(
                text = message,
                style = MaterialTheme.typography.titleLarge,
                color = RestockColors.Success,
                textAlign = TextAlign.Center,
            )
        }
    }
}

// ==================== PREVIEWS ====================

@LightDarkPreview
@Composable
private fun ListCompleteCelebrationPreview() {
    RestockPreviewTheme {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(RestockColors.Background),
        ) {
            ListCompleteCelebration(
                show = true,
                onDismiss = {},
            )
        }
    }
}

@LightDarkPreview
@Composable
private fun CelebrationMessagePreview() {
    RestockPreviewTheme {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(RestockColors.Background),
            contentAlignment = Alignment.Center,
        ) {
            CelebrationMessage(
                message = "All done!",
                alpha = 1f,
            )
        }
    }
}

@LightDarkPreview
@Composable
private fun CelebrationMessageCustomPreview() {
    RestockPreviewTheme {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(RestockColors.Background),
            contentAlignment = Alignment.Center,
        ) {
            CelebrationMessage(
                message = "Shopping complete!",
                alpha = 1f,
            )
        }
    }
}
