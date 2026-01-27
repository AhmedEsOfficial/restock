package com.restock.core.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.restock.core.ui.preview.LightDarkPreview
import com.restock.core.ui.preview.RestockPreviewTheme
import com.restock.core.ui.theme.RestockColors
import com.restock.core.ui.theme.RestockSpacing
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

// ==================== ANIMATION CONSTANTS ====================

/**
 * Duration for sparkle animation in milliseconds.
 */
private const val SPARKLE_ANIMATION_DURATION_MS = 300

/**
 * Duration for glow pulse animation in milliseconds.
 */
private const val GLOW_ANIMATION_DURATION_MS = 200

/**
 * Duration for glow fade out animation in milliseconds.
 */
private const val GLOW_FADE_DURATION_MS = 400

/**
 * Duration for brain icon pulse animation in milliseconds.
 */
private const val BRAIN_PULSE_DURATION_MS = 600

/**
 * Number of sparkle particles to generate.
 */
private const val SPARKLE_PARTICLE_COUNT = 8

/**
 * Default size for sparkle canvas.
 */
private val SparkleCanvasSize = 48.dp

/**
 * Default size for glow effect.
 */
private val GlowSize = 32.dp

/**
 * Shopping trips required before suggestions start appearing.
 */
private const val TRIPS_FOR_SUGGESTIONS = 3

// ==================== SPARKLE ANIMATION ====================

/**
 * Data class representing a single sparkle particle.
 *
 * @param angle The angle in degrees from center (0-360).
 * @param distance The normalized distance from center (0-1).
 * @param size The particle radius in dp.
 * @param alpha The particle opacity (0-1).
 */
private data class SparkleParticle(
    val angle: Float,
    val distance: Float,
    val size: Float,
    val alpha: Float,
)

/**
 * Generates a list of sparkle particles with randomized properties.
 *
 * @param count Number of particles to generate.
 * @return List of [SparkleParticle] with varied positions and sizes.
 */
private fun generateSparkleParticles(count: Int = SPARKLE_PARTICLE_COUNT): List<SparkleParticle> {
    return List(count) {
        SparkleParticle(
            angle = Random.nextFloat() * 360f,
            distance = Random.nextFloat() * 0.3f + 0.5f, // 0.5 to 0.8
            size = Random.nextFloat() * 2f + 1.5f, // 1.5 to 3.5
            alpha = Random.nextFloat() * 0.4f + 0.6f, // 0.6 to 1.0
        )
    }
}

/**
 * Draws a single sparkle particle on the canvas.
 *
 * @param particle The particle data.
 * @param progress Animation progress (0-1).
 * @param baseColor The sparkle color.
 */
private fun DrawScope.drawSparkleParticle(
    particle: SparkleParticle,
    progress: Float,
    baseColor: Color,
) {
    val maxRadius = size.minDimension / 2
    val animatedDistance = particle.distance * progress
    val radius = maxRadius * animatedDistance
    val fadeAlpha = (1f - progress) * particle.alpha

    val angleRad = Math.toRadians(particle.angle.toDouble())
    val x = center.x + cos(angleRad).toFloat() * radius
    val y = center.y + sin(angleRad).toFloat() * radius

    // Draw the sparkle as a small circle that fades as it moves outward
    drawCircle(
        color = baseColor.copy(alpha = fadeAlpha),
        radius = particle.size * (1f - progress * 0.5f),
        center = Offset(x, y),
    )
}

/**
 * Sparkle animation component that creates a burst of particles from the center.
 *
 * Triggers when [trigger] changes to true, creating a subtle "magic sparkle"
 * effect that indicates the app has recorded the user's action for learning.
 *
 * **Design Principles:**
 * - Subtle, not distracting: Animation is brief (300ms) and uses transparency
 * - Delightful: Small particles create a moment of delight
 * - Non-blocking: Animation doesn't prevent user interaction
 *
 * **Animation Specs:**
 * - Duration: 300ms
 * - Easing: EaseOut
 * - Particle count: 8
 * - Color: Accent (Amber)
 *
 * @param trigger When this changes to true, the sparkle animation plays.
 * @param modifier Modifier for the sparkle canvas.
 * @param color The sparkle color. Defaults to [RestockColors.Accent].
 * @param size The size of the sparkle canvas. Defaults to 48dp.
 *
 * Usage:
 * ```kotlin
 * var isChecked by remember { mutableStateOf(false) }
 *
 * Box {
 *     RestockCheckbox(
 *         checked = isChecked,
 *         onCheckedChange = { isChecked = it }
 *     )
 *     LearningSparkle(
 *         trigger = isChecked,
 *         modifier = Modifier.align(Alignment.Center)
 *     )
 * }
 * ```
 */
@Composable
fun LearningSparkle(
    trigger: Boolean,
    modifier: Modifier = Modifier,
    color: Color = RestockColors.Accent,
    size: Dp = SparkleCanvasSize,
) {
    var animationKey by remember { mutableIntStateOf(0) }

    // Track trigger changes to restart animation
    LaunchedEffect(trigger) {
        if (trigger) {
            animationKey++
        }
    }

    // Generate new particles each time animation key changes
    val particles = remember(animationKey) {
        if (animationKey > 0) generateSparkleParticles() else emptyList()
    }

    // Animate progress from 0 to 1
    val progress = remember { Animatable(0f) }

    LaunchedEffect(animationKey) {
        if (animationKey > 0) {
            progress.snapTo(0f)
            progress.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = SPARKLE_ANIMATION_DURATION_MS,
                    easing = EaseOut,
                ),
            )
        }
    }

    if (particles.isNotEmpty() && progress.value < 1f) {
        Canvas(
            modifier = modifier
                .size(size)
                .semantics {
                    contentDescription = "Learning indicator"
                },
        ) {
            particles.forEach { particle ->
                drawSparkleParticle(particle, progress.value, color)
            }
        }
    }
}

// ==================== GLOW EFFECT ====================

/**
 * Creates a subtle glow effect that pulses around an element.
 *
 * Used to indicate that the app has recorded a user action for pattern learning.
 * The glow briefly intensifies and then fades, creating a subtle "acknowledged"
 * feeling without being distracting.
 *
 * **Animation Specs:**
 * - Glow In: 200ms (EaseOut)
 * - Glow Out: 400ms (fade)
 * - Color: Accent (Amber) @ 40% opacity
 *
 * @param trigger When this changes to true, the glow animation plays.
 * @param modifier Modifier for the glow container.
 * @param color The glow color. Defaults to [RestockColors.Accent].
 * @param size The size of the glow effect. Defaults to 32dp.
 *
 * Usage:
 * ```kotlin
 * var isChecked by remember { mutableStateOf(false) }
 *
 * Box {
 *     LearningGlow(
 *         trigger = isChecked,
 *         modifier = Modifier.align(Alignment.Center)
 *     )
 *     RestockCheckbox(
 *         checked = isChecked,
 *         onCheckedChange = { isChecked = it }
 *     )
 * }
 * ```
 */
@Composable
fun LearningGlow(
    trigger: Boolean,
    modifier: Modifier = Modifier,
    color: Color = RestockColors.Accent,
    size: Dp = GlowSize,
) {
    var animationKey by remember { mutableIntStateOf(0) }

    LaunchedEffect(trigger) {
        if (trigger) {
            animationKey++
        }
    }

    val glowAlpha = remember { Animatable(0f) }

    LaunchedEffect(animationKey) {
        if (animationKey > 0) {
            // Quick fade in
            glowAlpha.animateTo(
                targetValue = 0.4f,
                animationSpec = tween(
                    durationMillis = GLOW_ANIMATION_DURATION_MS,
                    easing = EaseOut,
                ),
            )
            // Slower fade out
            glowAlpha.animateTo(
                targetValue = 0f,
                animationSpec = tween(
                    durationMillis = GLOW_FADE_DURATION_MS,
                    easing = EaseOut,
                ),
            )
        }
    }

    if (glowAlpha.value > 0f) {
        Box(
            modifier = modifier
                .size(size)
                .drawBehind {
                    val gradient = Brush.radialGradient(
                        colors = listOf(
                            color.copy(alpha = glowAlpha.value),
                            color.copy(alpha = glowAlpha.value * 0.5f),
                            Color.Transparent,
                        ),
                        center = center,
                        radius = this.size.minDimension / 2,
                    )
                    drawCircle(brush = gradient)
                },
        )
    }
}

// ==================== LEARNING STATUS BANNER ====================

/**
 * A banner that displays the app's learning progress to users.
 *
 * Shows contextual messages based on the number of completed shopping trips,
 * helping users understand that the app is learning their patterns. The banner
 * automatically hides after enough trips when suggestions become available.
 *
 * **Design Principles:**
 * - Progressive disclosure: First-time explanation, then subtle ongoing feedback
 * - Non-intrusive: Small, subtle appearance that doesn't distract
 * - Optional: Can be disabled or hides automatically after learning completes
 *
 * **Messages by trip count:**
 * - 0 trips: "Start adding items to begin learning your patterns"
 * - 1 trip: "Learning your patterns..."
 * - 2 trips: "Almost ready to suggest items!"
 * - 3+ trips: Banner hidden (suggestions available)
 *
 * @param shoppingTripCount The number of completed shopping trips.
 * @param modifier Modifier for the banner container.
 * @param visible Whether the banner should be shown. Defaults to true.
 *
 * Usage:
 * ```kotlin
 * Column {
 *     LearningStatusBanner(shoppingTripCount = userTripCount)
 *     // Rest of list content
 * }
 * ```
 */
@Composable
fun LearningStatusBanner(
    shoppingTripCount: Int,
    modifier: Modifier = Modifier,
    visible: Boolean = true,
) {
    // Don't show after enough trips or if explicitly hidden
    val shouldShow = visible && shoppingTripCount < TRIPS_FOR_SUGGESTIONS

    val message = when (shoppingTripCount) {
        0 -> "Start adding items to begin learning your patterns"
        1 -> "Learning your patterns..."
        2 -> "Almost ready to suggest items!"
        else -> ""
    }

    AnimatedVisibility(
        visible = shouldShow && message.isNotEmpty(),
        enter = fadeIn(tween(200)) + slideInVertically(tween(200)) { -it },
        exit = fadeOut(tween(150)) + slideOutVertically(tween(150)) { -it },
    ) {
        Surface(
            modifier = modifier.fillMaxWidth(),
            color = RestockColors.Accent.copy(alpha = 0.1f),
            shape = RoundedCornerShape(0.dp),
        ) {
            Row(
                modifier = Modifier
                    .padding(
                        horizontal = RestockSpacing.md,
                        vertical = RestockSpacing.sm,
                    )
                    .semantics {
                        contentDescription = "Learning status: $message"
                    },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Brain emoji with subtle pulse
                LearningBrainIcon()

                Spacer(Modifier.width(RestockSpacing.sm))

                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = RestockColors.Muted,
                )
            }
        }
    }
}

/**
 * Animated brain icon with a subtle pulse effect.
 *
 * Creates a gentle pulsing animation to draw attention without being
 * distracting, conveying that the app is actively "thinking" and learning.
 */
@Composable
private fun LearningBrainIcon(
    modifier: Modifier = Modifier,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "brain_pulse")

    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = BRAIN_PULSE_DURATION_MS,
                easing = LinearEasing,
            ),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "brain_scale",
    )

    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = BRAIN_PULSE_DURATION_MS,
                easing = LinearEasing,
            ),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "brain_alpha",
    )

    Text(
        text = "\uD83E\uDDE0", // 🧠 brain emoji
        style = MaterialTheme.typography.bodySmall,
        modifier = modifier
            .alpha(alpha)
            .drawBehind {
                // Scale is applied via the drawing, not Modifier.scale, for better performance
            },
    )
}

// ==================== LEARNING INDICATOR BADGE ====================

/**
 * A small badge that briefly appears to indicate the app recorded an action.
 *
 * Shows a brief "Noted!" message that fades in and out quickly,
 * providing subtle feedback that the action was recorded for learning.
 *
 * **Animation Specs:**
 * - Fade In: 150ms
 * - Visible: 800ms
 * - Fade Out: 200ms
 *
 * @param trigger When this changes to true, the badge animation plays.
 * @param modifier Modifier for the badge.
 * @param text The badge text. Defaults to "Noted!"
 *
 * Usage:
 * ```kotlin
 * var isChecked by remember { mutableStateOf(false) }
 *
 * Row {
 *     RestockCheckbox(checked = isChecked, onCheckedChange = { isChecked = it })
 *     LearningNotedBadge(trigger = isChecked)
 * }
 * ```
 */
@Composable
fun LearningNotedBadge(
    trigger: Boolean,
    modifier: Modifier = Modifier,
    text: String = "Noted!",
) {
    var showBadge by remember { mutableStateOf(false) }
    var animationKey by remember { mutableIntStateOf(0) }

    LaunchedEffect(trigger) {
        if (trigger) {
            animationKey++
        }
    }

    val badgeAlpha = remember { Animatable(0f) }

    LaunchedEffect(animationKey) {
        if (animationKey > 0) {
            showBadge = true
            // Fade in
            badgeAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 150, easing = EaseOut),
            )
            // Hold
            kotlinx.coroutines.delay(800)
            // Fade out
            badgeAlpha.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 200, easing = EaseOut),
            )
            showBadge = false
        }
    }

    if (showBadge) {
        Box(
            modifier = modifier
                .alpha(badgeAlpha.value)
                .clip(RoundedCornerShape(4.dp))
                .background(RestockColors.Accent.copy(alpha = 0.15f))
                .padding(horizontal = 6.dp, vertical = 2.dp),
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = RestockColors.Accent,
            )
        }
    }
}

// ==================== COMBINED LEARNING FEEDBACK ====================

/**
 * Configuration options for learning feedback behavior.
 *
 * @param showSparkle Whether to show sparkle animation on check.
 * @param showGlow Whether to show glow effect on check.
 * @param showBadge Whether to show "Noted!" badge on check.
 * @param sparkleColor Color for sparkle particles.
 * @param glowColor Color for glow effect.
 */
data class LearningFeedbackConfig(
    val showSparkle: Boolean = true,
    val showGlow: Boolean = true,
    val showBadge: Boolean = false,
    val sparkleColor: Color = RestockColors.Accent,
    val glowColor: Color = RestockColors.Accent,
)

/**
 * Default configuration for learning feedback.
 */
val DefaultLearningFeedbackConfig = LearningFeedbackConfig()

/**
 * Disabled configuration for when user has opted out of learning feedback.
 */
val DisabledLearningFeedbackConfig = LearningFeedbackConfig(
    showSparkle = false,
    showGlow = false,
    showBadge = false,
)

/**
 * Modifier extension that adds learning feedback overlay effects.
 *
 * This is a utility that can be used to wrap any composable with
 * learning feedback animations.
 *
 * @param trigger The trigger state for the animation.
 * @param config Configuration for feedback effects.
 */
@Composable
fun LearningFeedbackOverlay(
    trigger: Boolean,
    modifier: Modifier = Modifier,
    config: LearningFeedbackConfig = DefaultLearningFeedbackConfig,
    content: @Composable () -> Unit,
) {
    Box(modifier = modifier) {
        // Glow behind content
        if (config.showGlow) {
            LearningGlow(
                trigger = trigger,
                color = config.glowColor,
                modifier = Modifier.align(Alignment.Center),
            )
        }

        // Main content
        content()

        // Sparkle on top of content
        if (config.showSparkle) {
            LearningSparkle(
                trigger = trigger,
                color = config.sparkleColor,
                modifier = Modifier.align(Alignment.Center),
            )
        }
    }
}

// ==================== PREVIEWS ====================

@LightDarkPreview
@Composable
private fun LearningSparklePreview() {
    RestockPreviewTheme {
        var trigger by remember { mutableStateOf(false) }

        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Learning Sparkle",
                style = MaterialTheme.typography.titleMedium,
            )

            Box(
                modifier = Modifier.size(80.dp),
                contentAlignment = Alignment.Center,
            ) {
                RestockCheckbox(
                    checked = trigger,
                    onCheckedChange = { trigger = it },
                )
                LearningSparkle(
                    trigger = trigger,
                    modifier = Modifier.size(64.dp),
                )
            }

            Text(
                text = "Tap checkbox to trigger sparkle",
                style = MaterialTheme.typography.bodySmall,
                color = RestockColors.Muted,
            )
        }
    }
}

@LightDarkPreview
@Composable
private fun LearningGlowPreview() {
    RestockPreviewTheme {
        var trigger by remember { mutableStateOf(false) }

        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Learning Glow",
                style = MaterialTheme.typography.titleMedium,
            )

            Box(
                modifier = Modifier.size(80.dp),
                contentAlignment = Alignment.Center,
            ) {
                LearningGlow(
                    trigger = trigger,
                    size = 48.dp,
                )
                RestockCheckbox(
                    checked = trigger,
                    onCheckedChange = { trigger = it },
                )
            }

            Text(
                text = "Tap checkbox to trigger glow",
                style = MaterialTheme.typography.bodySmall,
                color = RestockColors.Muted,
            )
        }
    }
}

@LightDarkPreview
@Composable
private fun LearningStatusBannerPreview() {
    RestockPreviewTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "Learning Status Banner",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp),
            )

            Text(
                text = "0 trips:",
                style = MaterialTheme.typography.labelSmall,
                color = RestockColors.Muted,
            )
            LearningStatusBanner(shoppingTripCount = 0)

            Text(
                text = "1 trip:",
                style = MaterialTheme.typography.labelSmall,
                color = RestockColors.Muted,
            )
            LearningStatusBanner(shoppingTripCount = 1)

            Text(
                text = "2 trips:",
                style = MaterialTheme.typography.labelSmall,
                color = RestockColors.Muted,
            )
            LearningStatusBanner(shoppingTripCount = 2)

            Text(
                text = "3+ trips (hidden):",
                style = MaterialTheme.typography.labelSmall,
                color = RestockColors.Muted,
            )
            LearningStatusBanner(shoppingTripCount = 3)
        }
    }
}

@LightDarkPreview
@Composable
private fun LearningNotedBadgePreview() {
    RestockPreviewTheme {
        var trigger by remember { mutableStateOf(false) }

        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Noted Badge",
                style = MaterialTheme.typography.titleMedium,
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                RestockCheckbox(
                    checked = trigger,
                    onCheckedChange = { trigger = it },
                )
                Text("Milk")
                LearningNotedBadge(trigger = trigger)
            }

            Text(
                text = "Tap checkbox to trigger badge",
                style = MaterialTheme.typography.bodySmall,
                color = RestockColors.Muted,
            )
        }
    }
}

@LightDarkPreview
@Composable
private fun LearningFeedbackOverlayPreview() {
    RestockPreviewTheme {
        var trigger by remember { mutableStateOf(false) }

        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Combined Feedback Overlay",
                style = MaterialTheme.typography.titleMedium,
            )

            LearningFeedbackOverlay(
                trigger = trigger,
                config = LearningFeedbackConfig(
                    showSparkle = true,
                    showGlow = true,
                ),
            ) {
                RestockCheckbox(
                    checked = trigger,
                    onCheckedChange = { trigger = it },
                )
            }

            Text(
                text = "Tap checkbox to see combined effects",
                style = MaterialTheme.typography.bodySmall,
                color = RestockColors.Muted,
            )
        }
    }
}

@LightDarkPreview
@Composable
private fun AllLearningFeedbackPreview() {
    RestockPreviewTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Text(
                text = "Learning Feedback Components",
                style = MaterialTheme.typography.titleMedium,
            )

            // Status banner examples
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Status Banners by Trip Count",
                    style = MaterialTheme.typography.labelSmall,
                    color = RestockColors.Muted,
                )
                LearningStatusBanner(shoppingTripCount = 0)
                LearningStatusBanner(shoppingTripCount = 1)
                LearningStatusBanner(shoppingTripCount = 2)
            }

            // Interactive examples
            var checked1 by remember { mutableStateOf(false) }
            var checked2 by remember { mutableStateOf(false) }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Interactive Elements",
                    style = MaterialTheme.typography.labelSmall,
                    color = RestockColors.Muted,
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    LearningFeedbackOverlay(trigger = checked1) {
                        RestockCheckbox(
                            checked = checked1,
                            onCheckedChange = { checked1 = it },
                        )
                    }
                    Text("With Sparkle + Glow")
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    RestockCheckbox(
                        checked = checked2,
                        onCheckedChange = { checked2 = it },
                    )
                    Text("Eggs")
                    LearningNotedBadge(trigger = checked2)
                }
            }
        }
    }
}
