# Task: Implement Suggestions Data Layer

**Sprint:** 7-8 (Smart Suggestions)
**Owner:** Tech Lead / Senior Developer
**Estimate:** 22 hours (T-7.1 through T-7.7 combined)
**Priority:** P0
**Prerequisites:** Sprint 5-6 completed (pattern learning operational)

---

## Sprint Context

This sprint surfaces the intelligence built in Sprint 5-6 to users through smart suggestions. The suggestions section appears above the main list, proactively recommending items the user might need based on their learned purchase patterns.

### Sprint Theme
> *"The moment when the app anticipates your needs."*

### Key Principles
1. **Helpful, not pushy:** Maximum 5 suggestions, easy dismiss
2. **Confidence-based:** Only suggest what we're confident about
3. **Learning from feedback:** Dismissals reduce confidence
4. **Timing matters:** Suggest when item is actually needed

---

## Task Description

Implement the suggestions data layer including repository, scorer, and prioritization algorithm.

## Implementation

### Suggestion.kt (domain model)

```kotlin
package com.restock.core.domain.model

data class Suggestion(
    val itemId: String,
    val itemName: String,
    val reason: String,
    val score: Double,
    val confidence: Double,
    val daysSinceLastPurchase: Int,
    val avgIntervalDays: Int,
)
```

### SuggestionDismissalEntity.kt

```kotlin
package com.restock.core.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "suggestion_dismissals")
data class SuggestionDismissalEntity(
    @PrimaryKey
    @ColumnInfo(name = "item_id")
    val itemId: String,
    @ColumnInfo(name = "dismiss_count")
    val dismissCount: Int,
    @ColumnInfo(name = "last_dismissed_at")
    val lastDismissedAt: Long,
    @ColumnInfo(name = "is_persistently_dismissed")
    val isPersistentlyDismissed: Boolean,
)
```

### SuggestionDismissalDao.kt

```kotlin
package com.restock.core.data.database.dao

import androidx.room.*
import com.restock.core.data.database.entity.SuggestionDismissalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SuggestionDismissalDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(dismissal: SuggestionDismissalEntity)

    @Query("SELECT * FROM suggestion_dismissals WHERE item_id = :itemId")
    suspend fun getDismissal(itemId: String): SuggestionDismissalEntity?

    @Query("SELECT item_id FROM suggestion_dismissals WHERE is_persistently_dismissed = 1")
    suspend fun getPersistentlyDismissedIds(): List<String>

    @Query("SELECT * FROM suggestion_dismissals")
    fun observeDismissals(): Flow<List<SuggestionDismissalEntity>>

    @Query("DELETE FROM suggestion_dismissals WHERE item_id = :itemId")
    suspend fun clearDismissal(itemId: String)

    @Query("UPDATE suggestion_dismissals SET dismiss_count = 0, is_persistently_dismissed = 0 WHERE item_id = :itemId")
    suspend fun resetDismissal(itemId: String)
}
```

### SuggestionRepository.kt (interface)

```kotlin
package com.restock.core.domain.repository

import com.restock.core.domain.model.Suggestion
import kotlinx.coroutines.flow.Flow

interface SuggestionRepository {
    fun observeSuggestions(): Flow<List<Suggestion>>
    suspend fun getSuggestions(limit: Int = 5): List<Suggestion>
    suspend fun dismissSuggestion(itemId: String)
    suspend fun acceptSuggestion(itemId: String)
    suspend fun isDismissed(itemId: String): Boolean
    suspend fun clearDismissalOnPurchase(itemId: String)
    suspend fun getShoppingTripCount(): Int
}
```

### SuggestionScorer.kt

```kotlin
package com.restock.core.ml.suggestion

import com.restock.core.data.database.dao.ItemDao
import com.restock.core.data.database.entity.PatternEntity
import com.restock.core.data.database.entity.PurchaseEntity
import com.restock.core.domain.model.Suggestion
import kotlinx.datetime.Clock
import javax.inject.Inject
import kotlin.math.roundToInt

class SuggestionScorer @Inject constructor(
    private val itemDao: ItemDao,
) {
    /**
     * Scores a pattern/purchase pair to create a Suggestion.
     *
     * Scoring formula:
     * score = confidence × (1 + urgency_factor)
     * urgency_factor = clamp(days_overdue / avg_interval, 0.0, 2.0)
     * days_overdue = days_since_last_purchase - avg_interval
     */
    suspend fun score(
        pattern: PatternEntity,
        lastPurchase: PurchaseEntity,
    ): Suggestion {
        val item = itemDao.getById(pattern.itemId)
            ?: throw IllegalStateException("Item not found: ${pattern.itemId}")

        val daysSince = daysSince(lastPurchase.purchasedAt)
        val avgInterval = pattern.avgIntervalDays

        val daysOverdue = (daysSince - avgInterval).coerceAtLeast(0.0)
        val urgencyFactor = (daysOverdue / avgInterval).coerceIn(0.0, 2.0)
        val score = pattern.confidenceScore * (1 + urgencyFactor)

        val reason = generateReason(daysSince.roundToInt(), avgInterval.roundToInt())

        return Suggestion(
            itemId = pattern.itemId,
            itemName = item.name,
            reason = reason,
            score = score,
            confidence = pattern.confidenceScore,
            daysSinceLastPurchase = daysSince.roundToInt(),
            avgIntervalDays = avgInterval.roundToInt(),
        )
    }

    private fun generateReason(daysSince: Int, avgInterval: Int): String {
        return if (daysSince > avgInterval + 2) {
            "Last bought $daysSince days ago"
        } else {
            "Usually every $avgInterval days"
        }
    }

    private fun daysSince(timestampMs: Long): Double {
        val now = Clock.System.now().toEpochMilliseconds()
        return (now - timestampMs) / (24 * 60 * 60 * 1000.0)
    }
}
```

### SuggestionRepositoryImpl.kt

```kotlin
package com.restock.core.data.repository

import com.restock.core.data.database.dao.*
import com.restock.core.data.database.entity.SuggestionDismissalEntity
import com.restock.core.domain.model.Suggestion
import com.restock.core.domain.repository.SuggestionRepository
import com.restock.core.ml.suggestion.SuggestionScorer
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import javax.inject.Inject

class SuggestionRepositoryImpl @Inject constructor(
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
                dismissals = dismissals.associate { it.itemId to it },
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
                    val lastPurchase = purchaseDao.getLastPurchase(pattern.itemId)
                        ?: return@mapNotNull null

                    val daysSince = daysSince(lastPurchase.purchasedAt)
                    if (daysSince < pattern.avgIntervalDays - 1) {
                        return@mapNotNull null  // Not due yet
                    }

                    suggestionScorer.score(pattern, lastPurchase)
                }
                .sortedByDescending { it.score }
                .take(limit)
        }

    private suspend fun calculateSuggestions(
        patterns: List<com.restock.core.data.database.entity.PatternEntity>,
        purchases: List<com.restock.core.data.database.entity.PurchaseEntity>,
        currentItemIds: Set<String>,
        dismissals: Map<String, SuggestionDismissalEntity>,
    ): List<Suggestion> {
        val purchaseMap = purchases.associateBy { it.itemId }

        return patterns
            .filter { it.itemId !in currentItemIds }
            .filter { dismissals[it.itemId]?.isPersistentlyDismissed != true }
            .mapNotNull { pattern ->
                val lastPurchase = purchaseMap[pattern.itemId]
                    ?: return@mapNotNull null

                val daysSince = daysSince(lastPurchase.purchasedAt)
                if (daysSince < pattern.avgIntervalDays - 1) {
                    return@mapNotNull null
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

            val dismissal = SuggestionDismissalEntity(
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
                Clock.System.now().toEpochMilliseconds()
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
        return (now - timestampMs) / (24 * 60 * 60 * 1000.0)
    }

    companion object {
        const val MIN_CONFIDENCE = 0.6
        const val MAX_SUGGESTIONS = 5
        const val PERSISTENT_DISMISS_THRESHOLD = 3
        const val CONFIDENCE_REDUCTION = 0.05f
    }
}
```

### Additional DAO Query

```kotlin
// Add to PurchaseDao.kt
@Query("""
    SELECT COUNT(DISTINCT DATE(purchased_at / 1000, 'unixepoch'))
    FROM purchases
""")
suspend fun getDistinctPurchaseDays(): Int

// Add to CurrentListDao.kt
@Query("SELECT DISTINCT item_id FROM current_list")
suspend fun getCurrentItemIds(): List<String>

@Query("SELECT DISTINCT item_id FROM current_list")
fun observeCurrentItemIds(): Flow<List<String>>
```

## Acceptance Criteria

- [ ] SuggestionRepository interface created
- [ ] SuggestionRepositoryImpl handles all queries
- [ ] SuggestionScorer calculates scores correctly
- [ ] Prioritization algorithm sorts by score
- [ ] Items on current list filtered out
- [ ] Dismissed items filtered out
- [ ] Maximum 5 suggestions returned
- [ ] Unit tests for repository and scorer

## Testing

```kotlin
class SuggestionScorerTest {

    @Test
    fun `overdue items have higher score than on-time items`() = runTest {
        val scorer = SuggestionScorer(mockItemDao())

        val overduePattern = pattern(avgDays = 7.0, confidence = 0.8)
        val overduePurchase = purchase(daysAgo = 10)  // 3 days overdue

        val onTimePattern = pattern(avgDays = 7.0, confidence = 0.8)
        val onTimePurchase = purchase(daysAgo = 6)  // 1 day early

        val overdueScore = scorer.score(overduePattern, overduePurchase)
        val onTimeScore = scorer.score(onTimePattern, onTimePurchase)

        assertThat(overdueScore.score).isGreaterThan(onTimeScore.score)
    }

    @Test
    fun `higher confidence preferred when same urgency`() = runTest {
        val scorer = SuggestionScorer(mockItemDao())

        val highConfPattern = pattern(avgDays = 7.0, confidence = 0.9)
        val lowConfPattern = pattern(avgDays = 7.0, confidence = 0.6)
        val purchase = purchase(daysAgo = 7)

        val highScore = scorer.score(highConfPattern, purchase)
        val lowScore = scorer.score(lowConfPattern, purchase)

        assertThat(highScore.score).isGreaterThan(lowScore.score)
    }

    @Test
    fun `urgency factor capped at 2x`() = runTest {
        val scorer = SuggestionScorer(mockItemDao())

        val pattern = pattern(avgDays = 7.0, confidence = 0.8)
        val veryOverdue = purchase(daysAgo = 100)

        val score = scorer.score(pattern, veryOverdue)

        // Max urgency = 2.0, so max score = 0.8 * 3.0 = 2.4
        assertThat(score.score).isLessThanOrEqualTo(2.4)
    }
}

class SuggestionRepositoryTest {

    @Test
    fun `getSuggestions filters items on current list`() = runTest {
        val repository = createRepository()

        // Item on current list
        currentListDao.addItem("milk")

        val suggestions = repository.getSuggestions()

        assertThat(suggestions.map { it.itemId }).doesNotContain("milk")
    }

    @Test
    fun `dismissSuggestion increments dismiss count`() = runTest {
        val repository = createRepository()

        repository.dismissSuggestion("milk")
        repository.dismissSuggestion("milk")

        val dismissal = dismissalDao.getDismissal("milk")
        assertThat(dismissal?.dismissCount).isEqualTo(2)
    }

    @Test
    fun `three dismissals makes item persistently dismissed`() = runTest {
        val repository = createRepository()

        repeat(3) { repository.dismissSuggestion("milk") }

        assertThat(repository.isDismissed("milk")).isTrue()
    }
}
```

## Definition of Done

- [ ] All code compiles without errors
- [ ] Scoring algorithm correct
- [ ] Filtering works (current list, dismissed)
- [ ] Maximum 5 suggestions enforced
- [ ] Unit tests pass (90%+ coverage)
- [ ] Integration tests verify flow

## Dependent Tasks

This task blocks:
- 002_accept_dismiss_logic.md
- UI implementation

## Notes

- Score = confidence × (1 + urgency_factor)
- Urgency capped at 2x to prevent runaway scores
- 3 dismissals = persistent (won't suggest again until purchased)
- Clear dismissal state when item is actually purchased
