# Task: Implement Learning Feedback & Polish

**Sprint:** 5-6 (Pattern Learning Engine)
**Owner:** UI/UX Engineer
**Estimate:** 21 hours (D-6.1 through D-6.6 combined)
**Priority:** P0/P1
**Prerequisites:** 001_learning_feedback_design.md

---

## Sprint Context

After the design phase, implement the selected learning feedback mechanism and polish the existing list feature based on any feedback from Sprint 3-4.

### Sprint Theme
> *"The intelligence that learns without asking."*

---

## Task Description

Implement learning feedback animations, celebration states, and polish the list feature.

## Implementation Tasks

### D-6.1: Implement Check Animation Enhancement (4h)

Enhance the checkbox animation to provide subtle learning feedback.

```kotlin
package com.restock.core.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.restock.core.ui.theme.RestockColors
import com.restock.core.ui.theme.RestockPreviewTheme

@Composable
fun RestockCheckboxWithLearning(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    showLearningEffect: Boolean = true,
) {
    val hapticFeedback = LocalHapticFeedback.current
    var wasJustChecked by remember { mutableStateOf(false) }

    // Track when transitioning to checked
    LaunchedEffect(checked) {
        if (checked) {
            wasJustChecked = true
        }
    }

    // Reset learning effect after animation
    LaunchedEffect(wasJustChecked) {
        if (wasJustChecked) {
            kotlinx.coroutines.delay(500)
            wasJustChecked = false
        }
    }

    // Scale animation for "pop" effect
    val scale by animateFloatAsState(
        targetValue = if (wasJustChecked) 1.2f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow,
        ),
        label = "checkboxScale"
    )

    // Glow animation
    val glowAlpha by animateFloatAsState(
        targetValue = if (wasJustChecked && showLearningEffect) 0.4f else 0f,
        animationSpec = tween(
            durationMillis = 400,
            easing = EaseOut,
        ),
        label = "glowAlpha"
    )

    Box(
        modifier = modifier
            .size(48.dp) // Touch target
            .clip(CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true),
            ) {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                onCheckedChange(!checked)
            },
        contentAlignment = Alignment.Center,
    ) {
        // Glow effect behind checkbox
        if (glowAlpha > 0 && showLearningEffect) {
            Canvas(modifier = Modifier.size(40.dp)) {
                drawCircle(
                    color = RestockColors.Accent.copy(alpha = glowAlpha),
                    radius = size.minDimension / 2,
                )
            }
        }

        // Sparkle particles
        if (wasJustChecked && showLearningEffect) {
            SparkleOverlay(
                modifier = Modifier.size(48.dp),
            )
        }

        // Checkbox itself
        Box(
            modifier = Modifier
                .size(24.dp)
                .scale(scale)
                .background(
                    color = if (checked) RestockColors.Success else Color.Transparent,
                    shape = CircleShape,
                )
                .then(
                    if (!checked) {
                        Modifier.background(
                            color = Color.Transparent,
                            shape = CircleShape,
                        )
                    } else Modifier
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (!checked) {
                // Unchecked state - ring
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(
                        color = RestockColors.Muted,
                        radius = size.minDimension / 2 - 1.dp.toPx(),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(
                            width = 2.dp.toPx()
                        ),
                    )
                }
            } else {
                // Checked state - checkmark
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = contentDescription,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}

@Composable
private fun SparkleOverlay(
    modifier: Modifier = Modifier,
) {
    val particles = remember { generateParticles() }

    val progress by rememberInfiniteTransition(label = "sparkle").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = EaseOut),
            repeatMode = RepeatMode.Restart,
        ),
        label = "sparkleProgress"
    )

    Canvas(modifier = modifier) {
        particles.forEach { particle ->
            drawParticle(particle, progress)
        }
    }
}

private data class Particle(
    val angle: Float,
    val distance: Float,
    val size: Float,
)

private fun generateParticles(): List<Particle> {
    return List(8) { i ->
        Particle(
            angle = i * 45f + kotlin.random.Random.nextFloat() * 20f,
            distance = 0.8f + kotlin.random.Random.nextFloat() * 0.4f,
            size = 1.5f + kotlin.random.Random.nextFloat() * 1.5f,
        )
    }
}

private fun DrawScope.drawParticle(particle: Particle, progress: Float) {
    val maxRadius = size.minDimension / 2
    val currentRadius = maxRadius * particle.distance * progress
    val alpha = (1f - progress).coerceIn(0f, 1f)

    val angleRad = Math.toRadians(particle.angle.toDouble())
    val x = center.x + kotlin.math.cos(angleRad).toFloat() * currentRadius
    val y = center.y + kotlin.math.sin(angleRad).toFloat() * currentRadius

    drawCircle(
        color = RestockColors.Accent.copy(alpha = alpha),
        radius = particle.size * (1f - progress * 0.3f),
        center = Offset(x, y),
    )
}

// ==================== PREVIEWS ====================

@Preview(showBackground = true)
@Composable
private fun RestockCheckboxWithLearningPreview() {
    RestockPreviewTheme {
        var checked by remember { mutableStateOf(false) }
        RestockCheckboxWithLearning(
            checked = checked,
            onCheckedChange = { checked = it },
        )
    }
}
```

### D-6.2: Create Celebration Animation (4h)

Animation when user completes their list (all items checked).

```kotlin
package com.restock.core.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.restock.core.ui.theme.RestockColors
import com.restock.core.ui.theme.RestockPreviewTheme
import kotlin.random.Random

@Composable
fun ListCompleteCelebration(
    show: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (!show) return

    val confettiPieces = remember { generateConfetti() }

    val progress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(2000, easing = EaseOut),
        finishedListener = { onDismiss() },
        label = "celebrationProgress"
    )

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        // Confetti animation
        Canvas(modifier = Modifier.fillMaxSize()) {
            confettiPieces.forEach { piece ->
                val y = piece.startY + (size.height * 1.2f * progress * piece.speed)
                val x = piece.startX + (piece.drift * progress * 100)
                val rotation = piece.rotation + (progress * 360 * piece.rotationSpeed)
                val alpha = (1f - progress).coerceIn(0f, 1f)

                drawRect(
                    color = piece.color.copy(alpha = alpha),
                    topLeft = Offset(x, y),
                    size = androidx.compose.ui.geometry.Size(
                        piece.width,
                        piece.height
                    ),
                )
            }
        }

        // Success message
        if (progress < 0.8f) {
            Text(
                text = "🎉 All done!",
                style = MaterialTheme.typography.headlineMedium,
                color = RestockColors.Success,
            )
        }
    }
}

private data class ConfettiPiece(
    val startX: Float,
    val startY: Float,
    val width: Float,
    val height: Float,
    val color: Color,
    val speed: Float,
    val drift: Float,
    val rotation: Float,
    val rotationSpeed: Float,
)

private fun generateConfetti(): List<ConfettiPiece> {
    val colors = listOf(
        RestockColors.Accent,
        RestockColors.Success,
        RestockColors.Breakfast,
        RestockColors.Lunch,
        RestockColors.Dinner,
        RestockColors.Snack,
    )

    return List(50) {
        ConfettiPiece(
            startX = Random.nextFloat() * 1000,
            startY = -Random.nextFloat() * 200,
            width = Random.nextFloat() * 10 + 5,
            height = Random.nextFloat() * 15 + 10,
            color = colors.random(),
            speed = Random.nextFloat() * 0.5f + 0.5f,
            drift = Random.nextFloat() * 2f - 1f,
            rotation = Random.nextFloat() * 360,
            rotationSpeed = Random.nextFloat() * 2f - 1f,
        )
    }
}

// ==================== PREVIEWS ====================

@Preview(showBackground = true)
@Composable
private fun ListCompleteCelebrationPreview() {
    RestockPreviewTheme {
        ListCompleteCelebration(
            show = true,
            onDismiss = {},
        )
    }
}
```

### D-6.3: Loading States for Pattern Processing (2h)

Subtle loading indicator when pattern calculation is running.

```kotlin
@Composable
fun PatternProcessingIndicator(
    isProcessing: Boolean,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = isProcessing,
        enter = fadeIn() + slideInVertically(),
        exit = fadeOut() + slideOutVertically(),
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(RestockSpacing.sm),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(12.dp),
                strokeWidth = 2.dp,
                color = RestockColors.Muted,
            )

            Spacer(Modifier.width(RestockSpacing.xs))

            Text(
                text = "Updating patterns...",
                style = MaterialTheme.typography.labelSmall,
                color = RestockColors.Muted,
            )
        }
    }
}
```

### D-6.4: Refine List Feature Based on Feedback (6h)

Address any feedback from Sprint 3-4 testing:

**Common refinements:**
- Adjust swipe-to-delete sensitivity
- Improve drag handle visibility
- Refine item card shadows
- Adjust spacing between elements
- Fix any edge cases with animations

### D-6.5: Performance Optimization of Animations (3h)

Ensure all animations run at 60fps:

**Optimization techniques:**
- Use `graphicsLayer` for GPU-accelerated transformations
- Avoid allocations in animation lambdas
- Use `remember` for stable references
- Disable animations for users with reduced motion setting

```kotlin
@Composable
fun rememberReducedMotion(): Boolean {
    val context = LocalContext.current
    return remember {
        val resolver = context.contentResolver
        val setting = Settings.Global.getFloat(
            resolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1f
        )
        setting == 0f
    }
}
```

### D-6.6: Accessibility Verification (2h)

Ensure all new components are accessible:

- [ ] All animations respect reduced motion
- [ ] Learning feedback has content descriptions
- [ ] Celebration dismissible via accessibility action
- [ ] No information conveyed by color alone

---

## Acceptance Criteria

- [ ] Enhanced check animation implemented
- [ ] Celebration animation for completed list
- [ ] Loading state for pattern processing
- [ ] List feature polished based on feedback
- [ ] Animations performant (60fps)
- [ ] Accessibility verified

## Definition of Done

- [ ] All components compile without errors
- [ ] Animations smooth (60fps verified)
- [ ] Previews render correctly
- [ ] Accessibility supported
- [ ] User testing validates improvements

## Notes

- Use Compose animation preview to verify smoothness
- Test on lower-end devices
- Consider battery impact of animations
- All new components should have preview functions
