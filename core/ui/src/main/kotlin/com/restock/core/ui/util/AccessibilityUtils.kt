package com.restock.core.ui.util

import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.getSystemService

/**
 * Accessibility utilities for Restock UI components.
 *
 * These utilities help ensure the app respects user accessibility preferences,
 * particularly for animations and motion effects.
 */

/**
 * Remembers whether the user has enabled reduced motion preferences.
 *
 * This function checks two system settings:
 * 1. `ANIMATOR_DURATION_SCALE` - Set to 0 when animations are disabled
 * 2. Accessibility Manager's touch exploration (TalkBack) - May indicate motion sensitivity
 *
 * Use this to conditionally disable complex animations for users who have
 * indicated a preference for reduced motion. This is important for:
 * - Users with vestibular disorders who may experience motion sickness
 * - Users who find animations distracting
 * - Users who have explicitly disabled animations for performance
 *
 * **WCAG Guidelines:**
 * - WCAG 2.1 Success Criterion 2.3.3 (Animation from Interactions)
 * - Users should be able to disable non-essential animations
 *
 * @return `true` if the user prefers reduced motion, `false` otherwise.
 *
 * Usage:
 * ```kotlin
 * @Composable
 * fun AnimatedComponent() {
 *     val reduceMotion = rememberReducedMotion()
 *
 *     if (reduceMotion) {
 *         // Show static or simplified version
 *         StaticContent()
 *     } else {
 *         // Show animated version
 *         AnimatedContent()
 *     }
 * }
 * ```
 *
 * Alternative usage for conditional animation specs:
 * ```kotlin
 * val animationSpec = if (rememberReducedMotion()) {
 *     snap<Float>() // Instant transition
 * } else {
 *     tween<Float>(durationMillis = 300) // Animated transition
 * }
 * ```
 */
@Composable
fun rememberReducedMotion(): Boolean {
    val context = LocalContext.current

    return remember {
        // Check if animations are disabled via developer options
        val animatorDurationScale =
            try {
                Settings.Global.getFloat(
                    context.contentResolver,
                    Settings.Global.ANIMATOR_DURATION_SCALE,
                    1f,
                )
            } catch (_: Exception) {
                1f
            }

        // Check if transition animations are disabled
        val transitionAnimationScale =
            try {
                Settings.Global.getFloat(
                    context.contentResolver,
                    Settings.Global.TRANSITION_ANIMATION_SCALE,
                    1f,
                )
            } catch (_: Exception) {
                1f
            }

        // Check accessibility services that may indicate motion sensitivity
        val accessibilityManager = context.getSystemService<AccessibilityManager>()
        val isAccessibilityEnabled = accessibilityManager?.isEnabled == true

        // Reduced motion is preferred if:
        // 1. Animation scale is 0 (animations disabled)
        // 2. Both animator and transition scales are 0
        animatorDurationScale == 0f || transitionAnimationScale == 0f
    }
}

/**
 * Returns the current animation duration scale factor.
 *
 * This can be used to proportionally scale animation durations based on
 * the user's system preference. A scale of 0 means animations are disabled,
 * 1 is normal speed, and values like 0.5 or 2 indicate half or double speed.
 *
 * @return The animation duration scale factor (0.0 to 10.0, typically).
 *
 * Usage:
 * ```kotlin
 * val scale = rememberAnimationDurationScale()
 * val adjustedDuration = (baseDuration * scale).toInt()
 *
 * animateFloatAsState(
 *     targetValue = targetValue,
 *     animationSpec = tween(durationMillis = adjustedDuration)
 * )
 * ```
 */
@Composable
fun rememberAnimationDurationScale(): Float {
    val context = LocalContext.current

    return remember {
        try {
            Settings.Global.getFloat(
                context.contentResolver,
                Settings.Global.ANIMATOR_DURATION_SCALE,
                1f,
            ).coerceIn(0f, 10f)
        } catch (_: Exception) {
            1f
        }
    }
}

/**
 * Calculates an adjusted animation duration based on system settings.
 *
 * Respects the user's animation duration scale preference and applies it
 * to the given base duration. If reduced motion is enabled (scale = 0),
 * returns a minimum duration to ensure the animation completes.
 *
 * @param baseDurationMs The base animation duration in milliseconds.
 * @param minDurationMs Minimum duration to return (used when scale is 0).
 * @return Adjusted animation duration in milliseconds.
 *
 * Usage:
 * ```kotlin
 * val duration = rememberAdjustedAnimationDuration(
 *     baseDurationMs = 300,
 *     minDurationMs = 50
 * )
 *
 * animateFloatAsState(
 *     targetValue = target,
 *     animationSpec = tween(durationMillis = duration)
 * )
 * ```
 */
@Composable
fun rememberAdjustedAnimationDuration(
    baseDurationMs: Int,
    minDurationMs: Int = 1,
): Int {
    val scale = rememberAnimationDurationScale()
    return if (scale == 0f) {
        minDurationMs
    } else {
        (baseDurationMs * scale).toInt().coerceAtLeast(minDurationMs)
    }
}

/**
 * Returns whether touch exploration (typically TalkBack) is enabled.
 *
 * This can be used to provide alternative interaction patterns for
 * screen reader users, such as:
 * - Disabling gesture-only interactions
 * - Providing explicit action buttons
 * - Announcing state changes
 *
 * @return `true` if touch exploration is enabled, `false` otherwise.
 *
 * Usage:
 * ```kotlin
 * val isTouchExplorationEnabled = rememberTouchExplorationEnabled()
 *
 * if (isTouchExplorationEnabled) {
 *     // Provide explicit tap targets instead of swipe gestures
 *     ExplicitActionButtons()
 * } else {
 *     // Use swipe gestures
 *     SwipeableContent()
 * }
 * ```
 */
@Composable
fun rememberTouchExplorationEnabled(): Boolean {
    val context = LocalContext.current

    return remember {
        val accessibilityManager = context.getSystemService<AccessibilityManager>()
        accessibilityManager?.isTouchExplorationEnabled == true
    }
}
