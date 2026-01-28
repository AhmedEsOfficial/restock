package com.restock.core.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.restock.core.ui.preview.LightDarkPreview
import com.restock.core.ui.preview.RestockPreviewTheme
import com.restock.core.ui.theme.RestockColors
import com.restock.core.ui.util.rememberReducedMotion
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

// ==================== ANIMATION CONSTANTS ====================

/**
 * Animation duration for checkbox state changes.
 */
private const val CHECKBOX_ANIMATION_DURATION_MS = 200

/**
 * Duration for sparkle animation.
 */
private const val SPARKLE_ANIMATION_DURATION_MS = 350

/**
 * Duration for glow animation.
 */
private const val GLOW_ANIMATION_DURATION_MS = 250

/**
 * Duration for glow fade out.
 */
private const val GLOW_FADE_OUT_DURATION_MS = 400

/**
 * Default visual size of the checkbox.
 */
private val CheckboxSize = 24.dp

/**
 * Default touch target size for accessibility.
 */
private val CheckboxTouchTarget = 48.dp

/**
 * Corner radius for the checkbox shape.
 */
private val CheckboxCornerRadius = 6.dp

/**
 * Border width for the checkbox outline.
 */
private val CheckboxBorderWidth = 2.dp

/**
 * Checkmark icon size relative to checkbox size.
 */
private val CheckmarkSize = 16.dp

/**
 * Number of sparkle particles.
 */
private const val PARTICLE_COUNT = 6

// ==================== CHECKBOX SPARKLE ====================

/**
 * Data class for checkbox sparkle particle properties.
 */
private data class CheckboxSparkle(
    val angle: Float,
    val distance: Float,
    val size: Float,
    val alpha: Float,
)

/**
 * Generates checkbox sparkle particles with randomized properties.
 */
private fun generateCheckboxSparkles(): List<CheckboxSparkle> {
    return List(PARTICLE_COUNT) {
        CheckboxSparkle(
            angle = Random.nextFloat() * 360f,
            distance = Random.nextFloat() * 0.35f + 0.55f,
            size = Random.nextFloat() * 1.5f + 1.5f,
            alpha = Random.nextFloat() * 0.3f + 0.7f,
        )
    }
}

/**
 * Draws a checkbox sparkle particle.
 */
private fun DrawScope.drawCheckboxSparkle(
    sparkle: CheckboxSparkle,
    progress: Float,
    color: Color,
    maxRadius: Float,
) {
    val animatedDistance = sparkle.distance * progress
    val radius = maxRadius * animatedDistance
    val fadeAlpha = (1f - progress) * sparkle.alpha

    val angleRad = Math.toRadians(sparkle.angle.toDouble())
    val x = center.x + cos(angleRad).toFloat() * radius
    val y = center.y + sin(angleRad).toFloat() * radius

    drawCircle(
        color = color.copy(alpha = fadeAlpha),
        radius = sparkle.size * (1f - progress * 0.4f),
        center = Offset(x, y),
    )
}

// ==================== LEARNING CHECKBOX COMPONENT ====================

/**
 * Enhanced checkbox with integrated learning feedback animations.
 *
 * This component combines the standard [RestockCheckbox] with subtle visual
 * feedback that indicates the app is recording the user's action for pattern
 * learning. The feedback is designed to be delightful without being distracting.
 *
 * **Features:**
 * - Standard checkbox functionality with animated check state
 * - Optional sparkle animation on check (particles radiating outward)
 * - Optional glow effect on check (brief amber glow)
 * - Enhanced checkmark animation with slight overshoot
 * - Full accessibility support with 48dp touch target
 *
 * **Design Principles:**
 * - Subtle, not distracting: Animations are brief and use transparency
 * - Delightful: Small details create emotional connection
 * - Non-blocking: Animations don't prevent interaction
 * - Progressive disclosure: Feedback appears after check action
 *
 * **Animation Specs:**
 * | Effect | Duration | Easing |
 * |--------|----------|--------|
 * | Check transition | 200ms | Default |
 * | Sparkle | 350ms | EaseOut |
 * | Glow in | 250ms | EaseOut |
 * | Glow out | 400ms | EaseOut |
 *
 * @param checked Whether the checkbox is checked.
 * @param onCheckedChange Called when the checkbox state changes.
 * @param modifier Modifier to be applied to the checkbox container.
 * @param enabled Whether the checkbox is enabled and interactive.
 * @param showLearningFeedback Whether to show learning animations. Defaults to true.
 * @param feedbackColor The color for learning feedback effects. Defaults to Accent.
 * @param size The visual size of the checkbox. Defaults to 24dp.
 * @param contentDescription Accessibility description for the checkbox.
 *
 * Usage:
 * ```kotlin
 * var isChecked by remember { mutableStateOf(false) }
 *
 * LearningCheckbox(
 *     checked = isChecked,
 *     onCheckedChange = { isChecked = it },
 *     contentDescription = "Mark milk as purchased"
 * )
 * ```
 */
@Composable
fun LearningCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    showLearningFeedback: Boolean = true,
    feedbackColor: Color = RestockColors.Accent,
    size: Dp = CheckboxSize,
    contentDescription: String? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }

    // Check if reduced motion is preferred
    val reduceMotion = rememberReducedMotion()

    // Disable learning feedback if reduced motion is enabled
    val effectiveLearningFeedback = showLearningFeedback && !reduceMotion

    // Track when checked becomes true to trigger animations
    var animationTriggerKey by remember { mutableIntStateOf(0) }
    var previousChecked by remember { mutableStateOf(checked) }

    LaunchedEffect(checked) {
        if (checked && !previousChecked) {
            animationTriggerKey++
        }
        previousChecked = checked
    }

    // Generate sparkle particles when triggered
    val sparkles =
        remember(animationTriggerKey) {
            if (animationTriggerKey > 0) generateCheckboxSparkles() else emptyList()
        }

    // Sparkle animation progress
    val sparkleProgress = remember { Animatable(0f) }

    LaunchedEffect(animationTriggerKey) {
        if (animationTriggerKey > 0 && effectiveLearningFeedback) {
            sparkleProgress.snapTo(0f)
            sparkleProgress.animateTo(
                targetValue = 1f,
                animationSpec =
                    tween(
                        durationMillis = SPARKLE_ANIMATION_DURATION_MS,
                        easing = EaseOut,
                    ),
            )
        }
    }

    // Glow animation
    val glowAlpha = remember { Animatable(0f) }

    LaunchedEffect(animationTriggerKey) {
        if (animationTriggerKey > 0 && effectiveLearningFeedback) {
            glowAlpha.snapTo(0f)
            glowAlpha.animateTo(
                targetValue = 0.35f,
                animationSpec =
                    tween(
                        durationMillis = GLOW_ANIMATION_DURATION_MS,
                        easing = EaseOut,
                    ),
            )
            glowAlpha.animateTo(
                targetValue = 0f,
                animationSpec =
                    tween(
                        durationMillis = GLOW_FADE_OUT_DURATION_MS,
                        easing = EaseOut,
                    ),
            )
        }
    }

    // Checkbox visual animations
    val backgroundColor by animateColorAsState(
        targetValue =
            when {
                !enabled -> RestockColors.Disabled
                checked -> RestockColors.Success
                else -> Color.Transparent
            },
        animationSpec = tween(durationMillis = CHECKBOX_ANIMATION_DURATION_MS),
        label = "checkbox_background",
    )

    val borderColor by animateColorAsState(
        targetValue =
            when {
                !enabled -> RestockColors.Disabled
                checked -> RestockColors.Success
                else -> RestockColors.Muted
            },
        animationSpec = tween(durationMillis = CHECKBOX_ANIMATION_DURATION_MS),
        label = "checkbox_border",
    )

    val checkmarkScale by animateFloatAsState(
        targetValue = if (checked) 1f else 0f,
        animationSpec = tween(durationMillis = CHECKBOX_ANIMATION_DURATION_MS),
        label = "checkbox_checkmark_scale",
    )

    val checkboxShape = RoundedCornerShape(CheckboxCornerRadius)

    Box(
        modifier =
            modifier
                .size(CheckboxTouchTarget)
                .clip(RoundedCornerShape(8.dp))
                .clickable(
                    interactionSource = interactionSource,
                    indication = ripple(bounded = true),
                    enabled = enabled,
                    role = Role.Checkbox,
                    onClick = { onCheckedChange(!checked) },
                )
                .semantics {
                    contentDescription?.let {
                        this.contentDescription = it
                    }
                    stateDescription = if (checked) "Checked" else "Unchecked"
                },
        contentAlignment = Alignment.Center,
    ) {
        // Glow effect behind checkbox
        if (effectiveLearningFeedback && glowAlpha.value > 0f) {
            Box(
                modifier =
                    Modifier
                        .size(size + 16.dp)
                        .drawBehind {
                            val gradient =
                                Brush.radialGradient(
                                    colors =
                                        listOf(
                                            feedbackColor.copy(alpha = glowAlpha.value),
                                            feedbackColor.copy(alpha = glowAlpha.value * 0.4f),
                                            Color.Transparent,
                                        ),
                                    center = center,
                                    radius = this.size.minDimension / 2,
                                )
                            drawCircle(brush = gradient)
                        },
            )
        }

        // Checkbox visual
        Box(
            modifier =
                Modifier
                    .size(size)
                    .clip(checkboxShape)
                    .background(backgroundColor)
                    .border(
                        width = CheckboxBorderWidth,
                        color = borderColor,
                        shape = checkboxShape,
                    ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = Color.White,
                modifier =
                    Modifier
                        .size(CheckmarkSize)
                        .scale(checkmarkScale),
            )
        }

        // Sparkle effect on top
        if (effectiveLearningFeedback && sparkles.isNotEmpty() && sparkleProgress.value < 1f) {
            Canvas(modifier = Modifier.size(CheckboxTouchTarget)) {
                val maxRadius = this.size.minDimension / 2
                sparkles.forEach { sparkle ->
                    drawCheckboxSparkle(
                        sparkle = sparkle,
                        progress = sparkleProgress.value,
                        color = feedbackColor,
                        maxRadius = maxRadius,
                    )
                }
            }
        }
    }
}

/**
 * Learning checkbox with an associated label text.
 *
 * The entire row is clickable for easier interaction. The label shows
 * strikethrough styling when checked, and optional learning feedback
 * animations on the checkbox.
 *
 * @param checked Whether the checkbox is checked.
 * @param onCheckedChange Called when the checkbox state changes.
 * @param label The label text displayed next to the checkbox.
 * @param modifier Modifier to be applied to the row container.
 * @param enabled Whether the checkbox is enabled and interactive.
 * @param showLearningFeedback Whether to show learning animations.
 * @param feedbackColor The color for learning feedback effects.
 * @param contentDescription Accessibility description for the checkbox.
 *
 * Usage:
 * ```kotlin
 * var isChecked by remember { mutableStateOf(false) }
 *
 * LearningLabeledCheckbox(
 *     checked = isChecked,
 *     onCheckedChange = { isChecked = it },
 *     label = "Milk"
 * )
 * ```
 */
@Composable
fun LearningLabeledCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    showLearningFeedback: Boolean = true,
    feedbackColor: Color = RestockColors.Accent,
    contentDescription: String? = null,
) {
    Row(
        modifier =
            modifier
                .clickable(
                    enabled = enabled,
                    onClick = { onCheckedChange(!checked) },
                    role = Role.Checkbox,
                )
                .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LearningCheckbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
            showLearningFeedback = showLearningFeedback,
            feedbackColor = feedbackColor,
            contentDescription = contentDescription,
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color =
                when {
                    !enabled && checked -> RestockColors.Muted
                    !enabled -> RestockColors.Disabled
                    checked -> RestockColors.Muted
                    else -> RestockColors.OnSurface
                },
            textDecoration =
                if (checked) {
                    androidx.compose.ui.text.style.TextDecoration.LineThrough
                } else {
                    null
                },
        )
    }
}

// ==================== PREVIEWS ====================

@LightDarkPreview
@Composable
private fun LearningCheckboxUncheckedPreview() {
    RestockPreviewTheme {
        LearningCheckbox(
            checked = false,
            onCheckedChange = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}

@LightDarkPreview
@Composable
private fun LearningCheckboxCheckedPreview() {
    RestockPreviewTheme {
        LearningCheckbox(
            checked = true,
            onCheckedChange = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}

@LightDarkPreview
@Composable
private fun LearningCheckboxDisabledPreview() {
    RestockPreviewTheme {
        Row(modifier = Modifier.padding(16.dp)) {
            LearningCheckbox(
                checked = false,
                onCheckedChange = {},
                enabled = false,
            )
            Spacer(Modifier.width(16.dp))
            LearningCheckbox(
                checked = true,
                onCheckedChange = {},
                enabled = false,
            )
        }
    }
}

@LightDarkPreview
@Composable
private fun LearningCheckboxInteractivePreview() {
    RestockPreviewTheme {
        var checked by remember { mutableStateOf(false) }

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Learning Checkbox",
                style = MaterialTheme.typography.titleMedium,
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                LearningCheckbox(
                    checked = checked,
                    onCheckedChange = { checked = it },
                )
                Spacer(Modifier.width(8.dp))
                Text("Tap to see learning feedback")
            }

            Text(
                text = "Sparkle + glow effects trigger on check",
                style = MaterialTheme.typography.bodySmall,
                color = RestockColors.Muted,
            )
        }
    }
}

@LightDarkPreview
@Composable
private fun LearningCheckboxNoFeedbackPreview() {
    RestockPreviewTheme {
        var checked by remember { mutableStateOf(false) }

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Feedback Disabled",
                style = MaterialTheme.typography.titleMedium,
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                LearningCheckbox(
                    checked = checked,
                    onCheckedChange = { checked = it },
                    showLearningFeedback = false,
                )
                Spacer(Modifier.width(8.dp))
                Text("No sparkle/glow")
            }
        }
    }
}

@LightDarkPreview
@Composable
private fun LearningLabeledCheckboxPreview() {
    RestockPreviewTheme {
        var checked1 by remember { mutableStateOf(false) }
        var checked2 by remember { mutableStateOf(true) }
        var checked3 by remember { mutableStateOf(false) }

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "Labeled Learning Checkbox",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp),
            )

            LearningLabeledCheckbox(
                checked = checked1,
                onCheckedChange = { checked1 = it },
                label = "Milk",
            )

            LearningLabeledCheckbox(
                checked = checked2,
                onCheckedChange = { checked2 = it },
                label = "Eggs (checked)",
            )

            LearningLabeledCheckbox(
                checked = checked3,
                onCheckedChange = { checked3 = it },
                label = "Bread",
                enabled = false,
            )
        }
    }
}

@LightDarkPreview
@Composable
private fun LearningCheckboxAllStatesPreview() {
    RestockPreviewTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Learning Checkbox States",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 16.dp),
            )

            Row {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Unchecked", style = MaterialTheme.typography.labelSmall)
                    LearningCheckbox(checked = false, onCheckedChange = {})
                }
                Spacer(Modifier.width(24.dp))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Checked", style = MaterialTheme.typography.labelSmall)
                    LearningCheckbox(checked = true, onCheckedChange = {})
                }
                Spacer(Modifier.width(24.dp))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Disabled", style = MaterialTheme.typography.labelSmall)
                    LearningCheckbox(
                        checked = false,
                        onCheckedChange = {},
                        enabled = false,
                    )
                }
                Spacer(Modifier.width(24.dp))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Dis+Check", style = MaterialTheme.typography.labelSmall)
                    LearningCheckbox(
                        checked = true,
                        onCheckedChange = {},
                        enabled = false,
                    )
                }
            }

            Spacer(Modifier.padding(16.dp))

            Text(
                text = "Custom Feedback Color",
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(bottom = 8.dp),
            )

            var customChecked by remember { mutableStateOf(false) }
            Row(verticalAlignment = Alignment.CenterVertically) {
                LearningCheckbox(
                    checked = customChecked,
                    onCheckedChange = { customChecked = it },
                    feedbackColor = RestockColors.Success,
                )
                Spacer(Modifier.width(8.dp))
                Text("Success color feedback")
            }
        }
    }
}

@LightDarkPreview
@Composable
private fun LearningCheckboxComparisonPreview() {
    RestockPreviewTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Text(
                text = "Standard vs Learning Checkbox",
                style = MaterialTheme.typography.titleMedium,
            )

            var standard by remember { mutableStateOf(false) }
            var learning by remember { mutableStateOf(false) }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Standard Checkbox",
                    style = MaterialTheme.typography.labelSmall,
                    color = RestockColors.Muted,
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RestockCheckbox(
                        checked = standard,
                        onCheckedChange = { standard = it },
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("No learning feedback")
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Learning Checkbox",
                    style = MaterialTheme.typography.labelSmall,
                    color = RestockColors.Muted,
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LearningCheckbox(
                        checked = learning,
                        onCheckedChange = { learning = it },
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("With sparkle + glow")
                }
            }

            Text(
                text = "Tap both to compare the experience",
                style = MaterialTheme.typography.bodySmall,
                color = RestockColors.Muted,
            )
        }
    }
}
