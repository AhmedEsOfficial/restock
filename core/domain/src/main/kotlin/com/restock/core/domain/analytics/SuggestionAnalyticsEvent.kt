package com.restock.core.domain.analytics

/**
 * Analytics events for the suggestions feature.
 *
 * These events are designed for tracking suggestion effectiveness and user behavior.
 * Implementation of the actual analytics tracking is deferred to Sprint 11-12.
 *
 * Key metrics to track:
 * - **Suggestion acceptance rate**: Accepted / (Accepted + Dismissed)
 * - **Suggestion relevance**: How many suggestions are acted upon
 * - **Position bias**: Whether users prefer first vs later suggestions
 * - **Dismissal patterns**: Which items get dismissed repeatedly
 */
sealed interface SuggestionAnalyticsEvent {
    /**
     * A suggestion was shown to the user.
     *
     * @param itemId Unique identifier of the suggested item.
     * @param position Position in the suggestions list (0-indexed).
     * @param score The calculated score that determined ranking.
     */
    data class SuggestionShown(
        val itemId: String,
        val position: Int,
        val score: Double,
    ) : SuggestionAnalyticsEvent

    /**
     * A suggestion was accepted (item added to list).
     *
     * @param itemId Unique identifier of the accepted item.
     * @param position Position in the suggestions list when accepted.
     */
    data class SuggestionAccepted(
        val itemId: String,
        val position: Int,
    ) : SuggestionAnalyticsEvent

    /**
     * A suggestion was dismissed.
     *
     * @param itemId Unique identifier of the dismissed item.
     * @param dismissCount Total number of times this item has been dismissed.
     * @param isPersistent True if this was the third dismissal (permanently hidden).
     */
    data class SuggestionDismissed(
        val itemId: String,
        val dismissCount: Int,
        val isPersistent: Boolean = false,
    ) : SuggestionAnalyticsEvent

    /**
     * Learning state changed.
     *
     * @param newState The new learning state (NOT_STARTED, LEARNING, ALMOST_READY, READY).
     * @param shoppingTripCount Number of shopping trips that triggered this state.
     */
    data class LearningStateChanged(
        val newState: String,
        val shoppingTripCount: Int,
    ) : SuggestionAnalyticsEvent

    /**
     * Suggestions section viewed.
     *
     * @param suggestionCount Number of suggestions shown.
     * @param learningState Current learning state.
     */
    data class SuggestionsSectionViewed(
        val suggestionCount: Int,
        val learningState: String,
    ) : SuggestionAnalyticsEvent
}
