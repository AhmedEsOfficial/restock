package com.restock.core.domain.usecase

import com.restock.core.domain.repository.PatternRepository
import com.restock.core.domain.repository.PurchaseRepository
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * Learning status information for the UI.
 *
 * Contains all the information needed to display learning feedback
 * components, including trip count and pattern availability.
 *
 * @property shoppingTripCount The number of completed shopping trips.
 *           A trip is counted as a distinct calendar day with at least one purchase.
 * @property totalPurchaseCount Total number of individual purchases recorded.
 * @property patternCount Number of patterns that have been learned.
 * @property hasSuggestions Whether there are patterns ready for suggestions.
 * @property isLearningComplete Whether enough data exists for reliable suggestions.
 */
data class LearningStatus(
    val shoppingTripCount: Int,
    val totalPurchaseCount: Int,
    val patternCount: Int,
    val hasSuggestions: Boolean,
    val isLearningComplete: Boolean,
) {
    /**
     * Whether to show the learning status banner.
     *
     * The banner is shown during the onboarding period (< 3 trips)
     * when the app is still learning the user's patterns.
     */
    val showLearningBanner: Boolean
        get() = shoppingTripCount < TRIPS_NEEDED_FOR_SUGGESTIONS

    /**
     * Progress percentage toward suggestions being available (0-100).
     */
    val progressPercent: Int
        get() = ((shoppingTripCount.coerceAtMost(TRIPS_NEEDED_FOR_SUGGESTIONS).toFloat() /
            TRIPS_NEEDED_FOR_SUGGESTIONS) * 100).toInt()

    companion object {
        /**
         * Number of shopping trips needed before suggestions start appearing.
         */
        const val TRIPS_NEEDED_FOR_SUGGESTIONS = 3

        /**
         * Empty learning status for initial state.
         */
        val EMPTY = LearningStatus(
            shoppingTripCount = 0,
            totalPurchaseCount = 0,
            patternCount = 0,
            hasSuggestions = false,
            isLearningComplete = false,
        )
    }
}

/**
 * Use case for getting the current learning status.
 *
 * Combines purchase history and pattern data to determine:
 * - How many shopping trips the user has completed
 * - How many patterns have been learned
 * - Whether suggestions are available
 *
 * This information is used by the Learning Feedback UI components:
 * - [LearningStatusBanner] displays progress during onboarding
 * - [LearningCheckbox] shows feedback animations on item check
 *
 * A shopping trip is defined as a calendar day on which at least one
 * purchase was recorded. This groups multiple check-offs on the same
 * shopping trip together.
 *
 * Usage:
 * ```kotlin
 * class ListViewModel @Inject constructor(
 *     private val getLearningStatusUseCase: GetLearningStatusUseCase,
 * ) {
 *     val learningStatus = getLearningStatusUseCase()
 *         .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), LearningStatus.EMPTY)
 * }
 * ```
 */
class GetLearningStatusUseCase @Inject constructor(
    private val purchaseRepository: PurchaseRepository,
    private val patternRepository: PatternRepository,
) {
    /**
     * Observe the learning status as a Flow.
     *
     * Updates automatically when purchases or patterns change.
     */
    operator fun invoke(): Flow<LearningStatus> {
        return combine(
            purchaseRepository.observeAllPurchases(),
            patternRepository.observePatternsWithItems(),
        ) { purchases, patterns ->
            // Count unique shopping days (trips)
            val uniqueDays = purchases
                .map { purchase ->
                    purchase.purchasedAt
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()
                }
                .distinct()
                .size

            // Check if there are suggestion-ready patterns
            val hasSuggestions = patterns.any { pattern ->
                pattern.confidenceScore >= MIN_CONFIDENCE_FOR_SUGGESTIONS &&
                    pattern.sampleCount >= MIN_SAMPLES_FOR_SUGGESTIONS
            }

            LearningStatus(
                shoppingTripCount = uniqueDays,
                totalPurchaseCount = purchases.size,
                patternCount = patterns.size,
                hasSuggestions = hasSuggestions,
                isLearningComplete = uniqueDays >= LearningStatus.TRIPS_NEEDED_FOR_SUGGESTIONS,
            )
        }
    }

    /**
     * Get the current learning status as a snapshot.
     *
     * Use this for one-time checks, prefer the Flow version for reactive UI.
     */
    suspend fun getSnapshot(): LearningStatus {
        val purchases = purchaseRepository.getItemIdsWithPurchases()
        val patterns = patternRepository.getAllPatterns()

        // For snapshot, we need to get all purchases to count unique days
        val allPurchases = mutableListOf<java.time.Instant>()
        purchases.forEach { itemId ->
            purchaseRepository.getPurchasesForItem(itemId).forEach { purchase ->
                allPurchases.add(purchase.purchasedAt)
            }
        }

        val uniqueDays = allPurchases
            .map { instant ->
                instant.atZone(ZoneId.systemDefault()).toLocalDate()
            }
            .distinct()
            .size

        val hasSuggestions = patterns.any { pattern ->
            pattern.confidenceScore >= MIN_CONFIDENCE_FOR_SUGGESTIONS &&
                pattern.sampleCount >= MIN_SAMPLES_FOR_SUGGESTIONS
        }

        return LearningStatus(
            shoppingTripCount = uniqueDays,
            totalPurchaseCount = allPurchases.size,
            patternCount = patterns.size,
            hasSuggestions = hasSuggestions,
            isLearningComplete = uniqueDays >= LearningStatus.TRIPS_NEEDED_FOR_SUGGESTIONS,
        )
    }

    companion object {
        /**
         * Minimum confidence score for a pattern to be suggested.
         */
        private const val MIN_CONFIDENCE_FOR_SUGGESTIONS = 0.6

        /**
         * Minimum sample count for a pattern to be suggested.
         */
        private const val MIN_SAMPLES_FOR_SUGGESTIONS = 3
    }
}
