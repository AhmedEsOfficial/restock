package com.restock.core.domain.repository

import com.restock.core.domain.model.Suggestion
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for smart suggestions.
 *
 * Handles:
 * - Generating suggestions from learned patterns
 * - Accepting suggestions (adding to list)
 * - Dismissing suggestions (with confidence reduction)
 * - Tracking shopping trip count for learning state
 */
interface SuggestionRepository {
    /**
     * Observe suggestions as a reactive flow.
     *
     * Updates automatically when:
     * - Patterns change
     * - Items are added to/removed from current list
     * - Suggestions are dismissed
     *
     * @return Flow of suggestions, limited to [Suggestion.MAX_SUGGESTIONS].
     */
    fun observeSuggestions(): Flow<List<Suggestion>>

    /**
     * Get suggestions as a one-shot query.
     *
     * @param limit Maximum number of suggestions to return.
     * @return List of suggestions, sorted by score descending.
     */
    suspend fun getSuggestions(limit: Int = Suggestion.MAX_SUGGESTIONS): List<Suggestion>

    /**
     * Dismiss a suggestion.
     *
     * Actions taken:
     * 1. Increments dismiss count for the item
     * 2. Reduces pattern confidence by [Suggestion.CONFIDENCE_REDUCTION]
     * 3. After [Suggestion.PERSISTENT_DISMISS_THRESHOLD] dismissals,
     *    marks item as persistently dismissed
     *
     * @param itemId The ID of the item to dismiss.
     */
    suspend fun dismissSuggestion(itemId: String)

    /**
     * Accept a suggestion by updating the last suggested timestamp.
     *
     * Note: Adding the item to the current list is handled separately
     * by the use case layer to maintain separation of concerns.
     *
     * @param itemId The ID of the accepted item.
     */
    suspend fun acceptSuggestion(itemId: String)

    /**
     * Check if an item is persistently dismissed.
     *
     * @param itemId The ID of the item to check.
     * @return True if the item has been dismissed 3+ times without purchase.
     */
    suspend fun isDismissed(itemId: String): Boolean

    /**
     * Clear dismissal state when an item is purchased.
     *
     * Resets dismiss count and removes persistent dismissal flag,
     * allowing the item to be suggested again.
     *
     * @param itemId The ID of the purchased item.
     */
    suspend fun clearDismissalOnPurchase(itemId: String)

    /**
     * Get the number of shopping trips (distinct days with purchases).
     *
     * Used to determine learning state:
     * - 0 trips: NOT_STARTED
     * - 1 trip: LEARNING
     * - 2 trips: ALMOST_READY
     * - 3+ trips: READY (suggestions can appear)
     *
     * @return Number of distinct days with at least one purchase.
     */
    suspend fun getShoppingTripCount(): Int
}
