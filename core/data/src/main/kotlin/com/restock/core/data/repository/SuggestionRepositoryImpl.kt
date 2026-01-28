package com.restock.core.data.repository

import com.restock.core.data.database.dao.CurrentListDao
import com.restock.core.data.database.dao.PatternDao
import com.restock.core.data.database.dao.PurchaseDao
import com.restock.core.data.database.dao.SuggestionDismissalDao
import com.restock.core.data.database.entity.PatternEntity
import com.restock.core.data.database.entity.PurchaseEntity
import com.restock.core.data.database.entity.SuggestionDismissalEntity
import com.restock.core.data.di.IoDispatcher
import com.restock.core.domain.model.Suggestion
import com.restock.core.domain.repository.SuggestionRepository
import com.restock.core.data.suggestion.SuggestionScorer
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of [SuggestionRepository] using Room database.
 *
 * Generates suggestions by:
 * 1. Finding patterns with confidence >= MIN_CONFIDENCE
 * 2. Filtering out items already on current list
 * 3. Filtering out persistently dismissed items
 * 4. Filtering items not yet due (daysSince < avgInterval - 1)
 * 5. Scoring and sorting by combined confidence × urgency
 * 6. Limiting to MAX_SUGGESTIONS
 */
@Singleton
class SuggestionRepositoryImpl
    @Inject
    constructor(
        private val patternDao: PatternDao,
        private val purchaseDao: PurchaseDao,
        private val currentListDao: CurrentListDao,
        private val dismissalDao: SuggestionDismissalDao,
        private val suggestionScorer: SuggestionScorer,
        @IoDispatcher private val dispatcher: CoroutineDispatcher,
    ) : SuggestionRepository {
        override fun observeSuggestions(): Flow<List<Suggestion>> =
            combine(
                patternDao.observePatternsAboveConfidence(MIN_CONFIDENCE),
                purchaseDao.observeLatestPurchases(),
                currentListDao.observeCurrentItemIds(),
                dismissalDao.observeDismissals(),
            ) { patterns, purchases, currentItemIds, dismissals ->
                calculateSuggestions(
                    patterns = patterns,
                    purchases = purchases,
                    currentItemIds = currentItemIds.toSet(),
                    dismissals = dismissals.associateBy { it.itemId },
                )
            }.flowOn(dispatcher)

        override suspend fun getSuggestions(limit: Int): List<Suggestion> =
            withContext(dispatcher) {
                val patterns = patternDao.getPatternsAboveConfidence(MIN_CONFIDENCE)
                val currentItemIds = currentListDao.getCurrentItemIds().toSet()
                val dismissedIds = dismissalDao.getPersistentlyDismissedIds().toSet()

                patterns
                    .filter { it.itemId !in currentItemIds }
                    .filter { it.itemId !in dismissedIds }
                    .mapNotNull { pattern ->
                        val lastPurchase =
                            purchaseDao.getLastPurchaseForItem(pattern.itemId)
                                ?: return@mapNotNull null

                        val daysSince = daysSince(lastPurchase.purchasedAt)
                        if (daysSince < pattern.avgIntervalDays - DUE_BUFFER_DAYS) {
                            return@mapNotNull null // Not due yet
                        }

                        suggestionScorer.score(pattern, lastPurchase)
                    }
                    .sortedByDescending { it.score }
                    .take(limit)
            }

        private suspend fun calculateSuggestions(
            patterns: List<PatternEntity>,
            purchases: List<PurchaseEntity>,
            currentItemIds: Set<String>,
            dismissals: Map<String, SuggestionDismissalEntity>,
        ): List<Suggestion> {
            val purchaseMap = purchases.associateBy { it.itemId }

            return patterns
                .filter { it.itemId !in currentItemIds }
                .filter { dismissals[it.itemId]?.isPersistentlyDismissed != true }
                .mapNotNull { pattern ->
                    val lastPurchase =
                        purchaseMap[pattern.itemId]
                            ?: return@mapNotNull null

                    val daysSince = daysSince(lastPurchase.purchasedAt)
                    if (daysSince < pattern.avgIntervalDays - DUE_BUFFER_DAYS) {
                        return@mapNotNull null // Not due yet
                    }

                    suggestionScorer.score(pattern, lastPurchase)
                }
                .sortedByDescending { it.score }
                .take(MAX_SUGGESTIONS)
        }

        override suspend fun dismissSuggestion(itemId: String) =
            withContext(dispatcher) {
                val current = dismissalDao.getDismissal(itemId)
                val newCount = (current?.dismissCount ?: 0) + 1

                val dismissal =
                    SuggestionDismissalEntity(
                        itemId = itemId,
                        dismissCount = newCount,
                        lastDismissedAt = Clock.System.now().toEpochMilliseconds(),
                        isPersistentlyDismissed = newCount >= PERSISTENT_DISMISS_THRESHOLD,
                    )

                dismissalDao.upsert(dismissal)

                // Reduce pattern confidence for non-persistent dismissals
                if (newCount < PERSISTENT_DISMISS_THRESHOLD) {
                    patternDao.reduceConfidence(itemId, CONFIDENCE_REDUCTION)
                }
            }

        override suspend fun acceptSuggestion(itemId: String) =
            withContext(dispatcher) {
                // Update last_suggested timestamp
                patternDao.updateLastSuggested(
                    itemId,
                    Clock.System.now().toEpochMilliseconds(),
                )
            }

        override suspend fun isDismissed(itemId: String): Boolean =
            withContext(dispatcher) {
                dismissalDao.getDismissal(itemId)?.isPersistentlyDismissed == true
            }

        override suspend fun clearDismissalOnPurchase(itemId: String) =
            withContext(dispatcher) {
                dismissalDao.resetDismissal(itemId)
            }

        override suspend fun getShoppingTripCount(): Int =
            withContext(dispatcher) {
                // Count distinct days with purchases
                purchaseDao.getDistinctPurchaseDays()
            }

        private fun daysSince(timestampMs: Long): Double {
            val now = Clock.System.now().toEpochMilliseconds()
            return (now - timestampMs) / MS_PER_DAY
        }

        companion object {
            /**
             * Minimum confidence score required for suggestion eligibility.
             */
            const val MIN_CONFIDENCE = 0.6

            /**
             * Maximum number of suggestions to return.
             */
            const val MAX_SUGGESTIONS = 5

            /**
             * Number of dismissals before permanently hiding from suggestions.
             */
            const val PERSISTENT_DISMISS_THRESHOLD = 3

            /**
             * Confidence reduction per dismissal.
             */
            const val CONFIDENCE_REDUCTION = 0.05f

            /**
             * Buffer days before item is due (suggest slightly early).
             * If avgInterval is 7, suggest when daysSince >= 6.
             */
            private const val DUE_BUFFER_DAYS = 1.0

            /**
             * Milliseconds per day.
             */
            private const val MS_PER_DAY = 24.0 * 60.0 * 60.0 * 1000.0
        }
    }
