# Task: Implement Pattern Calculation Algorithm

**Sprint:** 5-6 (Pattern Learning Engine)
**Owner:** Tech Lead / Senior Developer
**Estimate:** 25 hours (T-6.1 through T-6.8 combined)
**Priority:** P0
**Prerequisites:** 001_purchase_recording.md

---

## Sprint Context

This sprint implements the "invisible intelligence" that makes Restock unique. The pattern calculation algorithm analyzes purchase history to determine buying patterns and confidence scores for each item.

### Sprint Theme
> *"The intelligence that learns without asking."*

### Business Rules
- Minimum 3 purchases required for pattern
- Confidence score reflects pattern consistency
- Only suggest items with confidence >= 0.6
- Patterns recalculate on new purchases

---

## Task Description

Implement the pattern calculation algorithm that analyzes purchase intervals and calculates confidence scores.

## Algorithm Specifications

### Interval Calculation

```kotlin
/**
 * Calculates the average interval between purchases.
 *
 * Given purchase timestamps: [t1, t2, t3, t4]
 * Intervals are: [(t2-t1), (t3-t2), (t4-t3)]
 *
 * Average interval = sum(intervals) / count(intervals)
 *
 * Example:
 * Purchases on: Jan 1, Jan 8, Jan 15, Jan 22
 * Intervals: [7 days, 7 days, 7 days]
 * Average: 7 days
 */
```

### Confidence Score Calculation

```kotlin
/**
 * Calculates confidence score using coefficient of variation.
 *
 * Confidence = 1 / (1 + CV)
 * where CV = σ / μ (standard deviation / mean)
 *
 * Properties:
 * - Perfect consistency (CV = 0): Confidence = 1.0
 * - High variance (CV = 1): Confidence = 0.5
 * - Very high variance (CV = 2): Confidence = 0.33
 *
 * Examples:
 * - Weekly purchases (7, 7, 7, 7 days): CV ≈ 0, Confidence ≈ 1.0
 * - Somewhat regular (5, 7, 9, 7 days): CV ≈ 0.2, Confidence ≈ 0.83
 * - Irregular (3, 14, 7, 21 days): CV ≈ 0.6, Confidence ≈ 0.63
 */
```

## Implementation

### Pattern.kt (domain model)

```kotlin
package com.restock.core.domain.model

import kotlinx.datetime.Instant

data class Pattern(
    val itemId: String,
    val avgIntervalDays: Double,
    val confidenceScore: Double,
    val sampleCount: Int,
    val lastCalculated: Instant,
    val lastSuggested: Instant? = null,
)
```

### PatternRepository.kt (interface)

```kotlin
package com.restock.core.domain.repository

import com.restock.core.domain.model.Pattern
import kotlinx.coroutines.flow.Flow

interface PatternRepository {
    suspend fun upsertPattern(pattern: Pattern): Result<Unit>
    suspend fun getPattern(itemId: String): Pattern?
    suspend fun getAllPatterns(): List<Pattern>
    suspend fun getPatternsAboveConfidence(minConfidence: Double): List<Pattern>
    suspend fun updateLastSuggested(itemId: String, timestamp: Long)
    suspend fun reduceConfidence(itemId: String, by: Float)
    fun observePatterns(): Flow<List<Pattern>>
    fun observePatternsAboveConfidence(minConfidence: Double): Flow<List<Pattern>>
}
```

### PatternEntity.kt

```kotlin
package com.restock.core.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "patterns",
    foreignKeys = [
        ForeignKey(
            entity = ItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["item_id"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [
        Index(value = ["confidence_score"]),
    ]
)
data class PatternEntity(
    @PrimaryKey
    @ColumnInfo(name = "item_id")
    val itemId: String,
    @ColumnInfo(name = "avg_interval_days")
    val avgIntervalDays: Double,
    @ColumnInfo(name = "confidence_score")
    val confidenceScore: Double,
    @ColumnInfo(name = "sample_count")
    val sampleCount: Int,
    @ColumnInfo(name = "last_calculated")
    val lastCalculated: Long,
    @ColumnInfo(name = "last_suggested")
    val lastSuggested: Long? = null,
)
```

### PatternDao.kt

```kotlin
package com.restock.core.data.database.dao

import androidx.room.*
import com.restock.core.data.database.entity.PatternEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PatternDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(pattern: PatternEntity)

    @Query("SELECT * FROM patterns WHERE item_id = :itemId")
    suspend fun getPattern(itemId: String): PatternEntity?

    @Query("SELECT * FROM patterns ORDER BY confidence_score DESC")
    suspend fun getAllPatterns(): List<PatternEntity>

    @Query("SELECT * FROM patterns WHERE confidence_score >= :minConfidence ORDER BY confidence_score DESC")
    suspend fun getPatternsAboveConfidence(minConfidence: Double): List<PatternEntity>

    @Query("SELECT * FROM patterns ORDER BY confidence_score DESC")
    fun observePatterns(): Flow<List<PatternEntity>>

    @Query("SELECT * FROM patterns WHERE confidence_score >= :minConfidence ORDER BY confidence_score DESC")
    fun observePatternsAboveConfidence(minConfidence: Double): Flow<List<PatternEntity>>

    @Query("UPDATE patterns SET last_suggested = :timestamp WHERE item_id = :itemId")
    suspend fun updateLastSuggested(itemId: String, timestamp: Long)

    @Query("""
        UPDATE patterns
        SET confidence_score = MAX(0.3, confidence_score - :reduction)
        WHERE item_id = :itemId
    """)
    suspend fun reduceConfidence(itemId: String, reduction: Float)
}
```

### PatternRepositoryImpl.kt

```kotlin
package com.restock.core.data.repository

import com.restock.core.data.database.dao.PatternDao
import com.restock.core.data.database.entity.PatternEntity
import com.restock.core.domain.model.Pattern
import com.restock.core.domain.repository.PatternRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class PatternRepositoryImpl @Inject constructor(
    private val patternDao: PatternDao,
    @IoDispatcher private val dispatcher: CoroutineDispatcher,
) : PatternRepository {

    override suspend fun upsertPattern(pattern: Pattern): Result<Unit> =
        withContext(dispatcher) {
            runCatching {
                patternDao.upsert(pattern.toEntity())
            }
        }

    override suspend fun getPattern(itemId: String): Pattern? =
        withContext(dispatcher) {
            patternDao.getPattern(itemId)?.toDomain()
        }

    override suspend fun getAllPatterns(): List<Pattern> =
        withContext(dispatcher) {
            patternDao.getAllPatterns().map { it.toDomain() }
        }

    override suspend fun getPatternsAboveConfidence(minConfidence: Double): List<Pattern> =
        withContext(dispatcher) {
            patternDao.getPatternsAboveConfidence(minConfidence).map { it.toDomain() }
        }

    override suspend fun updateLastSuggested(itemId: String, timestamp: Long) =
        withContext(dispatcher) {
            patternDao.updateLastSuggested(itemId, timestamp)
        }

    override suspend fun reduceConfidence(itemId: String, by: Float) =
        withContext(dispatcher) {
            patternDao.reduceConfidence(itemId, by)
        }

    override fun observePatterns(): Flow<List<Pattern>> =
        patternDao.observePatterns()
            .map { entities -> entities.map { it.toDomain() } }
            .flowOn(dispatcher)

    override fun observePatternsAboveConfidence(minConfidence: Double): Flow<List<Pattern>> =
        patternDao.observePatternsAboveConfidence(minConfidence)
            .map { entities -> entities.map { it.toDomain() } }
            .flowOn(dispatcher)

    private fun Pattern.toEntity() = PatternEntity(
        itemId = itemId,
        avgIntervalDays = avgIntervalDays,
        confidenceScore = confidenceScore,
        sampleCount = sampleCount,
        lastCalculated = lastCalculated.toEpochMilliseconds(),
        lastSuggested = lastSuggested?.toEpochMilliseconds(),
    )

    private fun PatternEntity.toDomain() = Pattern(
        itemId = itemId,
        avgIntervalDays = avgIntervalDays,
        confidenceScore = confidenceScore,
        sampleCount = sampleCount,
        lastCalculated = kotlinx.datetime.Instant.fromEpochMilliseconds(lastCalculated),
        lastSuggested = lastSuggested?.let {
            kotlinx.datetime.Instant.fromEpochMilliseconds(it)
        },
    )
}
```

### PatternCalculator.kt

```kotlin
package com.restock.core.ml.pattern

import com.restock.core.domain.model.Pattern
import com.restock.core.domain.model.Purchase
import kotlinx.datetime.Clock
import javax.inject.Inject
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.time.Duration.Companion.days

class PatternCalculator @Inject constructor() {

    fun calculatePattern(purchases: List<Purchase>): Pattern? {
        if (purchases.size < MIN_PURCHASES) return null

        val sortedPurchases = purchases.sortedBy { it.purchasedAt }

        val intervals = sortedPurchases
            .zipWithNext { a, b ->
                val durationMs = b.purchasedAt.toEpochMilliseconds() -
                    a.purchasedAt.toEpochMilliseconds()
                durationMs / (24 * 60 * 60 * 1000.0) // Convert to days
            }
            .filter { it > 0 } // Filter out same-day purchases

        if (intervals.isEmpty()) return null

        val avgInterval = intervals.average()
        val confidence = calculateConfidence(intervals)

        return Pattern(
            itemId = purchases.first().itemId,
            avgIntervalDays = avgInterval,
            confidenceScore = confidence,
            sampleCount = purchases.size,
            lastCalculated = Clock.System.now(),
        )
    }

    private fun calculateConfidence(intervals: List<Double>): Double {
        if (intervals.isEmpty()) return 0.0
        if (intervals.size == 1) return 0.5 // Single interval = moderate confidence

        val mean = intervals.average()
        if (mean == 0.0) return 0.0

        val variance = intervals.map { (it - mean).pow(2) }.average()
        val stdDev = sqrt(variance)
        val cv = stdDev / mean // Coefficient of variation

        return (1.0 / (1.0 + cv)).coerceIn(0.0, 1.0)
    }

    fun shouldSuggest(pattern: Pattern, lastPurchase: Purchase): Boolean {
        val daysSinceLastPurchase = run {
            val now = Clock.System.now()
            val duration = now - lastPurchase.purchasedAt
            duration.inWholeDays.toDouble()
        }

        return pattern.confidenceScore >= MIN_CONFIDENCE_THRESHOLD &&
               daysSinceLastPurchase >= (pattern.avgIntervalDays - 1) &&
               pattern.sampleCount >= MIN_PURCHASES
    }

    companion object {
        const val MIN_PURCHASES = 3
        const val MIN_CONFIDENCE_THRESHOLD = 0.6
    }
}
```

### CalculatePatternsUseCase.kt

```kotlin
package com.restock.core.domain.usecase

import com.restock.core.domain.repository.PatternRepository
import com.restock.core.domain.repository.PurchaseRepository
import com.restock.core.ml.pattern.PatternCalculator
import timber.log.Timber
import javax.inject.Inject

class CalculatePatternsUseCase @Inject constructor(
    private val purchaseRepository: PurchaseRepository,
    private val patternRepository: PatternRepository,
    private val patternCalculator: PatternCalculator,
) {
    suspend operator fun invoke(): Result<Int> = runCatching {
        val itemIds = purchaseRepository.getItemIdsWithPurchases()
        var patternsCalculated = 0

        itemIds.forEach { itemId ->
            val purchases = purchaseRepository.getPurchasesForItem(itemId)
            val pattern = patternCalculator.calculatePattern(purchases)

            if (pattern != null) {
                patternRepository.upsertPattern(pattern)
                patternsCalculated++
            }
        }

        Timber.d("Pattern calculation complete: $patternsCalculated patterns for ${itemIds.size} items")
        patternsCalculated
    }
}
```

## Edge Case Handling

| Scenario | Handling |
|----------|----------|
| First purchase ever | No pattern, no interval |
| Second purchase | One interval, confidence = 0.5 |
| Third purchase | Pattern established |
| Long gap (> 3x average) | Include but reduces confidence |
| Very short gap (< 4 hours) | Treat as same shopping trip (debounce) |
| Seasonal item | Future: seasonal weighting |

## Acceptance Criteria

- [ ] PatternRepository interface created
- [ ] PatternRepositoryImpl handles CRUD operations
- [ ] PatternCalculator calculates intervals correctly
- [ ] Confidence score reflects variance correctly
- [ ] Minimum 3 purchases required for pattern
- [ ] Patterns stored and queryable
- [ ] Unit tests for algorithm (100% coverage)
- [ ] Edge cases handled correctly

## Testing

```kotlin
class PatternCalculatorTest {

    private lateinit var calculator: PatternCalculator

    @BeforeEach
    fun setup() {
        calculator = PatternCalculator()
    }

    @Test
    fun `returns null for fewer than 3 purchases`() {
        val purchases = listOf(
            purchase(daysAgo = 14),
            purchase(daysAgo = 7),
        )

        val pattern = calculator.calculatePattern(purchases)

        assertThat(pattern).isNull()
    }

    @Test
    fun `calculates correct average interval`() {
        val purchases = listOf(
            purchase(daysAgo = 21),  // Oldest
            purchase(daysAgo = 14),  // 7 days later
            purchase(daysAgo = 7),   // 7 days later
            purchase(daysAgo = 0),   // 7 days later (today)
        )

        val pattern = calculator.calculatePattern(purchases)

        assertThat(pattern).isNotNull()
        assertThat(pattern!!.avgIntervalDays).isCloseTo(7.0, within(0.1))
    }

    @Test
    fun `high confidence for consistent intervals`() {
        val purchases = listOf(
            purchase(daysAgo = 21),
            purchase(daysAgo = 14),
            purchase(daysAgo = 7),
            purchase(daysAgo = 0),
        )

        val pattern = calculator.calculatePattern(purchases)

        assertThat(pattern!!.confidenceScore).isGreaterThan(0.9)
    }

    @Test
    fun `low confidence for irregular intervals`() {
        val purchases = listOf(
            purchase(daysAgo = 45),  // 15 days
            purchase(daysAgo = 30),  // 3 days
            purchase(daysAgo = 27),  // 20 days
            purchase(daysAgo = 7),   // 7 days
            purchase(daysAgo = 0),
        )

        val pattern = calculator.calculatePattern(purchases)

        assertThat(pattern!!.confidenceScore).isLessThan(0.6)
    }

    @Test
    fun `handles long gaps without crashing`() {
        val purchases = listOf(
            purchase(daysAgo = 365),  // 1 year ago
            purchase(daysAgo = 7),
            purchase(daysAgo = 0),
        )

        val pattern = calculator.calculatePattern(purchases)

        assertThat(pattern).isNotNull()
        // Confidence should be low due to inconsistency
        assertThat(pattern!!.confidenceScore).isLessThan(0.5)
    }

    private fun purchase(daysAgo: Int): Purchase {
        return Purchase(
            id = UUID.randomUUID().toString(),
            itemId = "test-item",
            purchasedAt = Clock.System.now().minus(daysAgo.days),
            quantity = 1,
        )
    }
}
```

## Definition of Done

- [ ] All code compiles without errors
- [ ] Pattern calculation accurate (±1 day)
- [ ] Confidence correlates with actual variance
- [ ] Edge cases handled gracefully
- [ ] Unit tests pass (100% coverage for algorithm)
- [ ] Integration tests verify end-to-end flow

## Dependent Tasks

This task blocks:
- 003_background_processing.md
- Sprint 7-8 suggestions feature

## Notes

- Keep algorithm simple for MVP (no ML yet)
- Coefficient of variation is a proven statistical measure
- Store sample count for future weighted calculations
- Consider seasonal patterns in Phase 2
