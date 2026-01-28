package com.restock.feature.suggestions.ui.components

import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.ui.unit.IntSize

/**
 * Centralized animation specifications for suggestion UI components.
 *
 * This object provides consistent animation specs across all suggestion-related
 * components following the Sprint 7-8 design specifications. All animation
 * durations and easings are defined here to ensure visual consistency and
 * make it easy to tune animations across the feature.
 *
 * **Design Philosophy:**
 * - Accept animations use EaseOut for a snappy, satisfying feel
 * - Dismiss animations use EaseIn for a quick removal
 * - Entry animations use spring physics for a natural bounce
 * - All animations respect reduced motion accessibility settings
 *
 * **Animation Timings:**
 * | Animation | Duration | Easing |
 * |-----------|----------|--------|
 * | Card entry | Spring | MediumBouncy |
 * | Accept slide | 300ms | EaseOut |
 * | Dismiss slide | 200ms | EaseIn |
 * | Section expand | Spring | LowBouncy |
 * | Fade | 200ms | Linear |
 * | Press | Spring | MediumBouncy |
 * | Brain pulse | 1000ms | EaseInOut |
 */
object SuggestionAnimationSpecs {

    // ==================== DURATION CONSTANTS ====================

    /**
     * Duration for accept animation (slide up + fade out).
     */
    const val ACCEPT_DURATION_MS = 300

    /**
     * Duration for dismiss animation (slide right + fade out).
     */
    const val DISMISS_DURATION_MS = 200

    /**
     * Duration for section appear animation.
     */
    const val SECTION_APPEAR_DURATION_MS = 300

    /**
     * Duration for section hide animation.
     */
    const val SECTION_HIDE_DURATION_MS = 200

    /**
     * Duration for standard fade animations.
     */
    const val FADE_DURATION_MS = 200

    /**
     * Duration for brain pulsing animation cycle.
     */
    const val BRAIN_PULSE_DURATION_MS = 1000

    /**
     * Stagger delay between card entries in a row.
     */
    const val CARD_ENTRY_STAGGER_MS = 50

    // ==================== DISTANCE CONSTANTS ====================

    /**
     * Vertical slide distance when accepting a suggestion (in pixels).
     */
    const val ACCEPT_SLIDE_DISTANCE = -100f

    /**
     * Horizontal slide distance when dismissing a suggestion (in pixels).
     */
    const val DISMISS_SLIDE_DISTANCE = 100f

    // ==================== SCALE CONSTANTS ====================

    /**
     * Scale factor when card is pressed.
     */
    const val PRESSED_SCALE = 0.95f

    /**
     * Minimum scale for brain pulsing animation.
     */
    const val BRAIN_PULSE_MIN_SCALE = 1f

    /**
     * Maximum scale for brain pulsing animation.
     */
    const val BRAIN_PULSE_MAX_SCALE = 1.1f

    // ==================== SPRING ANIMATION SPECS ====================

    /**
     * Spring animation spec for card entry animations.
     *
     * Uses medium bouncy damping for a playful, natural feel.
     */
    val cardEntry = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMedium,
    )

    /**
     * Spring animation spec for card press interaction.
     *
     * Uses medium bouncy damping with low stiffness for smooth scale.
     */
    val cardPress = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow,
    )

    /**
     * Spring animation spec for section expand/collapse.
     *
     * Uses low bouncy damping for subtle spring effect.
     */
    val sectionExpand = spring<IntSize>(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessLow,
    )

    // ==================== TWEEN ANIMATION SPECS ====================

    /**
     * Tween animation spec for accept slide animation.
     */
    val acceptSlide = tween<Float>(
        durationMillis = ACCEPT_DURATION_MS,
        easing = EaseOut,
    )

    /**
     * Tween animation spec for dismiss slide animation.
     */
    val dismissSlide = tween<Float>(
        durationMillis = DISMISS_DURATION_MS,
        easing = EaseIn,
    )

    /**
     * Tween animation spec for standard fade animations.
     */
    val fade = tween<Float>(
        durationMillis = FADE_DURATION_MS,
        easing = LinearEasing,
    )

    /**
     * Tween animation spec for section appear fade.
     */
    val sectionAppearFade = tween<Float>(
        durationMillis = SECTION_APPEAR_DURATION_MS,
        easing = EaseOut,
    )

    /**
     * Tween animation spec for section hide fade.
     */
    val sectionHideFade = tween<Float>(
        durationMillis = SECTION_HIDE_DURATION_MS,
        easing = EaseIn,
    )

    /**
     * Brain pulsing animation spec for infinite pulse effect.
     */
    val brainPulse = infiniteRepeatable<Float>(
        animation = tween(
            durationMillis = BRAIN_PULSE_DURATION_MS,
            easing = EaseInOut,
        ),
        repeatMode = RepeatMode.Reverse,
    )

    // ==================== FACTORY METHODS ====================

    /**
     * Creates a tween animation spec for accept animation.
     *
     * @param reduceMotion If true, returns 0ms duration for instant transition.
     * @return Tween animation spec for accept animation.
     */
    fun acceptTween(reduceMotion: Boolean) = tween<Float>(
        durationMillis = if (reduceMotion) 0 else ACCEPT_DURATION_MS,
        easing = EaseOut,
    )

    /**
     * Creates a tween animation spec for dismiss animation.
     *
     * @param reduceMotion If true, returns 0ms duration for instant transition.
     * @return Tween animation spec for dismiss animation.
     */
    fun dismissTween(reduceMotion: Boolean) = tween<Float>(
        durationMillis = if (reduceMotion) 0 else DISMISS_DURATION_MS,
        easing = EaseIn,
    )

    /**
     * Creates a tween animation spec for section appear.
     *
     * @param reduceMotion If true, returns 0ms duration for instant transition.
     * @return Tween animation spec for section appear.
     */
    fun sectionAppearTween(reduceMotion: Boolean) = tween<Float>(
        durationMillis = if (reduceMotion) 0 else SECTION_APPEAR_DURATION_MS,
        easing = EaseOut,
    )

    /**
     * Creates a tween animation spec for section hide.
     *
     * @param reduceMotion If true, returns 0ms duration for instant transition.
     * @return Tween animation spec for section hide.
     */
    fun sectionHideTween(reduceMotion: Boolean) = tween<Float>(
        durationMillis = if (reduceMotion) 0 else SECTION_HIDE_DURATION_MS,
        easing = EaseIn,
    )

    /**
     * Creates a tween animation spec for alpha fade during accept/dismiss.
     *
     * @param cardState Current card animation state.
     * @param reduceMotion If true, returns 0ms duration for instant transition.
     * @return Tween animation spec for alpha animation.
     */
    fun alphaTween(cardState: SuggestionCardState, reduceMotion: Boolean) = tween<Float>(
        durationMillis = when (cardState) {
            SuggestionCardState.ACCEPTING -> if (reduceMotion) 0 else ACCEPT_DURATION_MS
            SuggestionCardState.DISMISSING -> if (reduceMotion) 0 else DISMISS_DURATION_MS
            else -> if (reduceMotion) 0 else DISMISS_DURATION_MS
        },
    )
}
