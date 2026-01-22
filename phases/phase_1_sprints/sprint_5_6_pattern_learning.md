# Sprint 5-6: Pattern Learning Engine
## Restock Android Application - Phase 1

**Duration:** Weeks 9-12 (4 weeks)
**Sprint Dates:** [TBD based on kickoff]
**Team:** Tech Lead/Senior Dev (1), UI/UX Engineer (1)
**Prerequisites:** Sprint 3-4 completed (list feature functional)

---

## Table of Contents

1. [Sprint Overview](#1-sprint-overview)
2. [Sprint Goals](#2-sprint-goals)
3. [User Stories](#3-user-stories)
4. [Technical Tasks](#4-technical-tasks)
5. [Design Tasks](#5-design-tasks)
6. [Algorithm Specifications](#6-algorithm-specifications)
7. [Technical Implementation](#7-technical-implementation)
8. [Testing Requirements](#8-testing-requirements)
9. [Risks & Mitigation](#9-risks--mitigation)
10. [Acceptance Criteria](#10-acceptance-criteria)

---

## 1. Sprint Overview

This sprint implements the "invisible intelligence" that makes Restock unique. The pattern learning engine observes user behavior silently, recording purchase events and calculating buying patterns. No user-facing features change significantly, but the foundation for smart suggestions is built.

### Sprint Theme
> *"The intelligence that learns without asking."*

### Why This Matters
- This is the core differentiator of Restock
- Pattern accuracy determines suggestion quality
- Must be completely invisible to users
- Privacy-preserving (all processing on-device)

### Key Principles
1. **Passive Learning:** User takes no action to train the system
2. **On-Device:** All calculations happen locally
3. **Non-Blocking:** Never slow down the main UI
4. **Minimum Data:** Only 3 purchases needed to establish pattern

---

## 2. Sprint Goals

### Primary Goals

| Goal | Owner | Success Metric |
|------|-------|----------------|
| Purchase event recording | Tech Lead | Every check-off creates purchase record |
| Interval calculation | Tech Lead | Accurate avg days between purchases |
| Confidence scoring | Tech Lead | Score reflects pattern consistency |
| Background processing | Tech Lead | WorkManager processes patterns |
| TensorFlow Lite integration | Tech Lead | Model loads and runs inference |
| Check animation enhancement | UI/UX | Subtle "learned" feedback |

### Success Metrics

| Metric | Target |
|--------|--------|
| Purchase events recorded | 100% of check-offs |
| Pattern calculation accuracy | ±1 day of actual average |
| Confidence score accuracy | Correlates with actual variance |
| Background processing time | < 5 seconds for 100 items |
| UI thread blocking | 0ms |

---

## 3. User Stories

### US-5.1: Purchase Event Recording
**As a** system
**I want** to record every item check-off as a purchase
**So that** I can learn user buying patterns

**Acceptance Criteria:**
- [ ] When user checks off item, purchase record created
- [ ] Purchase includes item ID, timestamp, quantity
- [ ] Recording happens asynchronously (no UI delay)
- [ ] Works offline
- [ ] No duplicate purchases for same check-off

**Technical Notes:**
- Trigger from CheckItemUseCase
- Only record when transitioning to checked (not unchecking)
- Include current quantity from list item

---

### US-5.2: Pattern Calculation
**As a** system
**I want** to calculate purchase patterns for items
**So that** I can predict when users need to restock

**Acceptance Criteria:**
- [ ] Pattern calculated after 3+ purchases
- [ ] Average interval in days computed correctly
- [ ] Confidence score computed from consistency
- [ ] Patterns stored and updated regularly
- [ ] Old patterns updated with new data

**Technical Notes:**
- Use coefficient of variation for confidence
- Recalculate patterns daily or on new purchase
- Store sample count for weighted calculations

---

### US-5.3: Confidence Scoring
**As a** system
**I want** to know how reliable each pattern is
**So that** I only suggest items with confident patterns

**Acceptance Criteria:**
- [ ] Confidence score range: 0.0 to 1.0
- [ ] Regular purchases = high confidence
- [ ] Irregular purchases = low confidence
- [ ] Score updates with each new purchase
- [ ] Minimum threshold for suggestions: 0.6

**Technical Notes:**
- Confidence = 1 / (1 + CV)
- CV = standard deviation / mean
- Higher CV = lower confidence

---

### US-5.4: Background Processing
**As a** system
**I want** patterns calculated in background
**So that** main app performance is not affected

**Acceptance Criteria:**
- [ ] Pattern calculation runs via WorkManager
- [ ] Processing doesn't block UI thread
- [ ] Battery-efficient scheduling
- [ ] Handles app killed during processing
- [ ] Logs processing results

**Technical Notes:**
- Use WorkManager with constraints (battery not low)
- Schedule periodic work (daily) + on new purchase
- Use ExistingWorkPolicy.REPLACE to avoid duplicates

---

### US-5.5: TensorFlow Lite Integration
**As a** system
**I want** to run ML models on-device
**So that** I can use advanced pattern recognition in future

**Acceptance Criteria:**
- [ ] TFLite runtime integrated
- [ ] Model loading works
- [ ] Inference produces results
- [ ] Model versioning system in place
- [ ] Fallback to statistical model if TFLite fails

**Technical Notes:**
- Initial model can be simple (or placeholder)
- Focus on infrastructure, not model quality
- GPU delegate optional (CPU is fine for MVP)

---

### US-5.6: Learning Feedback (Subtle)
**As a** user
**I want** subtle indication that the app is learning
**So that** I understand the app gets smarter over time

**Acceptance Criteria:**
- [ ] Very subtle visual cue on check-off (optional)
- [ ] Never interrupts or slows interaction
- [ ] Can be disabled in settings (stretch)
- [ ] First-time explanation in onboarding (stretch)

**Technical Notes:**
- Could be tiny sparkle animation on checkbox
- Or brief icon pulse
- Must not distract from primary action

---

## 4. Technical Tasks

### Tech Lead Tasks

#### Week 9: Purchase Recording Infrastructure

| Task ID | Task | Estimate | Priority |
|---------|------|----------|----------|
| T-5.1 | Create PurchaseRepository interface | 2h | P0 |
| T-5.2 | Implement PurchaseRepositoryImpl | 3h | P0 |
| T-5.3 | Create RecordPurchaseUseCase | 3h | P0 |
| T-5.4 | Integrate with CheckItemUseCase | 2h | P0 |
| T-5.5 | Add purchase queries to PurchaseDao | 2h | P0 |
| T-5.6 | Write purchase recording tests | 3h | P0 |
| T-5.7 | Verify offline recording | 2h | P0 |
| T-5.8 | Add duplicate prevention logic | 2h | P0 |

#### Week 10: Pattern Calculation Algorithm

| Task ID | Task | Estimate | Priority |
|---------|------|----------|----------|
| T-6.1 | Create PatternRepository interface | 2h | P0 |
| T-6.2 | Implement PatternRepositoryImpl | 3h | P0 |
| T-6.3 | Create PatternCalculator class | 4h | P0 |
| T-6.4 | Implement interval calculation | 3h | P0 |
| T-6.5 | Implement confidence scoring | 3h | P0 |
| T-6.6 | Create CalculatePatternsUseCase | 3h | P0 |
| T-6.7 | Write algorithm unit tests | 4h | P0 |
| T-6.8 | Test edge cases (gaps, irregular) | 3h | P0 |

#### Week 11: Background Processing

| Task ID | Task | Estimate | Priority |
|---------|------|----------|----------|
| T-7.1 | Set up WorkManager | 2h | P0 |
| T-7.2 | Create PatternCalculationWorker | 4h | P0 |
| T-7.3 | Implement periodic scheduling | 2h | P0 |
| T-7.4 | Implement on-purchase trigger | 2h | P0 |
| T-7.5 | Add work constraints | 1h | P1 |
| T-7.6 | Implement worker testing | 3h | P0 |
| T-7.7 | Add logging and monitoring | 2h | P1 |

#### Week 12: TensorFlow Lite & Polish

| Task ID | Task | Estimate | Priority |
|---------|------|----------|----------|
| T-8.1 | Add TFLite dependencies | 1h | P0 |
| T-8.2 | Create TFLiteModelManager | 3h | P0 |
| T-8.3 | Implement model loading | 2h | P0 |
| T-8.4 | Create inference wrapper | 3h | P0 |
| T-8.5 | Implement fallback mechanism | 2h | P0 |
| T-8.6 | Integration testing | 4h | P0 |
| T-8.7 | Performance profiling | 2h | P0 |
| T-8.8 | Documentation update | 2h | P1 |

---

## 5. Design Tasks

### UI/UX Engineer Tasks

#### Week 9-10: Learning Feedback Design

| Task ID | Task | Estimate | Priority |
|---------|------|----------|----------|
| D-5.1 | Research subtle feedback patterns | 3h | P1 |
| D-5.2 | Design learning indicator concepts | 3h | P1 |
| D-5.3 | Prototype animation options | 4h | P1 |
| D-5.4 | User testing of feedback options | 3h | P1 |
| D-5.5 | Select final feedback approach | 2h | P1 |

#### Week 11-12: Implementation & Polish

| Task ID | Task | Estimate | Priority |
|---------|------|----------|----------|
| D-6.1 | Implement check animation enhancement | 4h | P1 |
| D-6.2 | Create celebration animation (list complete) | 4h | P2 |
| D-6.3 | Loading states for pattern processing | 2h | P2 |
| D-6.4 | Refine list feature based on feedback | 6h | P0 |
| D-6.5 | Performance optimization of animations | 3h | P1 |
| D-6.6 | Accessibility verification | 2h | P0 |

#### Supporting Work

| Task ID | Task | Estimate | Priority |
|---------|------|----------|----------|
| D-7.1 | Prepare suggestions section design | 4h | P0 |
| D-7.2 | Design suggestion card component | 4h | P0 |
| D-7.3 | Plan suggestions animation | 2h | P1 |
| D-7.4 | Document design decisions | 2h | P1 |

---

## 6. Algorithm Specifications

### Purchase Recording

```kotlin
/**
 * Records a purchase event when an item is checked off.
 *
 * A purchase is only recorded when:
 * 1. Item transitions from unchecked to checked
 * 2. No purchase exists within last 4 hours for same item (debounce)
 *
 * @param listItemId The ID of the current list item
 * @param itemId The ID of the grocery item
 * @param quantity The quantity being purchased
 * @param timestamp When the purchase occurred
 */
data class PurchaseEvent(
    val id: String = UUID.randomUUID().toString(),
    val itemId: String,
    val quantity: Int,
    val purchasedAt: Instant,
    val priceCents: Int? = null,
    val storeId: String? = null,
)
```

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
fun calculateAverageInterval(purchases: List<Purchase>): Double {
    if (purchases.size < 2) return 0.0

    val sortedPurchases = purchases.sortedBy { it.purchasedAt }
    val intervals = sortedPurchases
        .zipWithNext { a, b ->
            Duration.between(a.purchasedAt, b.purchasedAt).toDays().toDouble()
        }

    return intervals.average()
}
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
 *
 * @param intervals List of intervals between purchases
 * @return Confidence score between 0 and 1
 */
fun calculateConfidence(intervals: List<Double>): Double {
    if (intervals.isEmpty()) return 0.0
    if (intervals.size == 1) return 0.5 // Single interval, moderate confidence

    val mean = intervals.average()
    if (mean == 0.0) return 0.0

    val variance = intervals.map { (it - mean).pow(2) }.average()
    val stdDev = sqrt(variance)
    val cv = stdDev / mean

    return (1.0 / (1.0 + cv)).coerceIn(0.0, 1.0)
}
```

### Pattern Update Logic

```kotlin
/**
 * Updates or creates pattern for an item.
 *
 * Rules:
 * 1. Minimum 3 purchases required for pattern
 * 2. Recalculate on each new purchase
 * 3. Use all historical purchases (no windowing for MVP)
 * 4. Store sample count for future weighted calculations
 */
data class Pattern(
    val itemId: String,
    val avgIntervalDays: Double,
    val confidenceScore: Double,
    val sampleCount: Int,
    val lastCalculated: Instant,
    val lastSuggested: Instant? = null,
)

fun shouldSuggest(pattern: Pattern, lastPurchase: Purchase): Boolean {
    val daysSinceLastPurchase = Duration.between(
        lastPurchase.purchasedAt,
        Clock.System.now()
    ).toDays()

    return pattern.confidenceScore >= MIN_CONFIDENCE_THRESHOLD &&
           daysSinceLastPurchase >= (pattern.avgIntervalDays - 1) &&
           pattern.sampleCount >= MIN_PURCHASES_FOR_PATTERN
}

companion object {
    const val MIN_CONFIDENCE_THRESHOLD = 0.6
    const val MIN_PURCHASES_FOR_PATTERN = 3
}
```

### Edge Case Handling

| Scenario | Handling |
|----------|----------|
| First purchase ever | No pattern, no interval |
| Second purchase | One interval, confidence = 0.5 |
| Third purchase | Pattern established |
| Long gap (> 3× average) | Include but reduces confidence |
| Very short gap (< 4 hours) | Treat as same shopping trip (debounce) |
| Seasonal item | Future: seasonal weighting |

---

## 7. Technical Implementation

### Purchase Repository

```kotlin
// PurchaseRepository.kt (domain layer)
interface PurchaseRepository {
    suspend fun recordPurchase(event: PurchaseEvent): Result<Purchase>
    suspend fun getPurchasesForItem(itemId: String): List<Purchase>
    suspend fun getRecentPurchase(itemId: String, withinHours: Int = 4): Purchase?
    suspend fun getAllPurchases(): List<Purchase>
    fun observePurchasesForItem(itemId: String): Flow<List<Purchase>>
}

// PurchaseRepositoryImpl.kt (data layer)
class PurchaseRepositoryImpl @Inject constructor(
    private val purchaseDao: PurchaseDao,
    @IoDispatcher private val dispatcher: CoroutineDispatcher,
) : PurchaseRepository {

    override suspend fun recordPurchase(event: PurchaseEvent): Result<Purchase> =
        withContext(dispatcher) {
            runCatching {
                // Check for recent purchase (debounce)
                val recent = purchaseDao.getRecentPurchase(
                    itemId = event.itemId,
                    afterTimestamp = Clock.System.now()
                        .minus(4.hours)
                        .toEpochMilliseconds()
                )

                if (recent != null) {
                    // Update quantity instead of new record
                    val updated = recent.copy(
                        quantity = recent.quantity + event.quantity
                    )
                    purchaseDao.update(updated)
                    return@runCatching updated.toDomain()
                }

                // Create new purchase record
                val entity = event.toEntity()
                purchaseDao.insert(entity)
                entity.toDomain()
            }
        }

    override suspend fun getPurchasesForItem(itemId: String): List<Purchase> =
        withContext(dispatcher) {
            purchaseDao.getPurchasesForItem(itemId).map { it.toDomain() }
        }
}
```

### Pattern Calculator

```kotlin
// PatternCalculator.kt (in :core:ml module)
class PatternCalculator @Inject constructor() {

    fun calculatePattern(purchases: List<Purchase>): Pattern? {
        if (purchases.size < MIN_PURCHASES) return null

        val sortedPurchases = purchases.sortedBy { it.purchasedAt }

        val intervals = sortedPurchases
            .zipWithNext { a, b ->
                Duration.between(a.purchasedAt, b.purchasedAt)
                    .toDays()
                    .toDouble()
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
        if (intervals.size == 1) return 0.5

        val mean = intervals.average()
        if (mean == 0.0) return 0.0

        val variance = intervals.map { (it - mean).pow(2) }.average()
        val stdDev = sqrt(variance)
        val cv = stdDev / mean

        return (1.0 / (1.0 + cv)).coerceIn(0.0, 1.0)
    }

    companion object {
        const val MIN_PURCHASES = 3
    }
}
```

### WorkManager Integration

```kotlin
// PatternCalculationWorker.kt
@HiltWorker
class PatternCalculationWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val purchaseRepository: PurchaseRepository,
    private val patternRepository: PatternRepository,
    private val patternCalculator: PatternCalculator,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val itemIds = purchaseRepository.getItemIdsWithPurchases()

            itemIds.forEach { itemId ->
                val purchases = purchaseRepository.getPurchasesForItem(itemId)
                val pattern = patternCalculator.calculatePattern(purchases)

                if (pattern != null) {
                    patternRepository.upsertPattern(pattern)
                }
            }

            Timber.d("Pattern calculation complete for ${itemIds.size} items")
            Result.success()
        } catch (e: Exception) {
            Timber.e(e, "Pattern calculation failed")
            Result.retry()
        }
    }

    companion object {
        const val WORK_NAME = "pattern_calculation"
    }
}

// PatternWorkScheduler.kt
class PatternWorkScheduler @Inject constructor(
    private val workManager: WorkManager,
) {
    fun schedulePeriodicCalculation() {
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .build()

        val workRequest = PeriodicWorkRequestBuilder<PatternCalculationWorker>(
            repeatInterval = 1,
            repeatIntervalTimeUnit = TimeUnit.DAYS,
        )
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()

        workManager.enqueueUniquePeriodicWork(
            PatternCalculationWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest,
        )
    }

    fun triggerImmediateCalculation() {
        val workRequest = OneTimeWorkRequestBuilder<PatternCalculationWorker>()
            .build()

        workManager.enqueueUniqueWork(
            "${PatternCalculationWorker.WORK_NAME}_immediate",
            ExistingWorkPolicy.REPLACE,
            workRequest,
        )
    }
}
```

### TensorFlow Lite Setup

```kotlin
// TFLiteModelManager.kt (in :core:ml module)
class TFLiteModelManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private var interpreter: Interpreter? = null

    fun loadModel(modelFileName: String = "pattern_model.tflite"): Boolean {
        return try {
            val modelFile = loadModelFile(modelFileName)
            val options = Interpreter.Options().apply {
                setNumThreads(2)
                // GPU delegate optional for future
            }
            interpreter = Interpreter(modelFile, options)
            true
        } catch (e: Exception) {
            Timber.e(e, "Failed to load TFLite model")
            false
        }
    }

    private fun loadModelFile(fileName: String): MappedByteBuffer {
        val assetFileDescriptor = context.assets.openFd(fileName)
        val fileInputStream = FileInputStream(assetFileDescriptor.fileDescriptor)
        val fileChannel = fileInputStream.channel
        return fileChannel.map(
            FileChannel.MapMode.READ_ONLY,
            assetFileDescriptor.startOffset,
            assetFileDescriptor.declaredLength
        )
    }

    fun runInference(input: FloatArray): FloatArray? {
        val interpreter = this.interpreter ?: return null

        val outputShape = interpreter.getOutputTensor(0).shape()
        val output = Array(1) { FloatArray(outputShape[1]) }

        interpreter.run(arrayOf(input), output)

        return output[0]
    }

    fun close() {
        interpreter?.close()
        interpreter = null
    }
}

// PatternMLModel.kt
class PatternMLModel @Inject constructor(
    private val modelManager: TFLiteModelManager,
    private val statisticalCalculator: PatternCalculator,
) {
    private var isModelLoaded = false

    fun initialize() {
        isModelLoaded = modelManager.loadModel()
        if (!isModelLoaded) {
            Timber.w("ML model not available, using statistical fallback")
        }
    }

    fun predictNextPurchase(
        itemId: String,
        purchaseHistory: List<Purchase>
    ): PredictionResult {
        // For MVP, always use statistical model
        // ML model can enhance predictions in future
        val pattern = statisticalCalculator.calculatePattern(purchaseHistory)

        return if (pattern != null) {
            PredictionResult(
                itemId = itemId,
                predictedDaysUntilNeeded = pattern.avgIntervalDays,
                confidence = pattern.confidenceScore,
                source = PredictionSource.STATISTICAL,
            )
        } else {
            PredictionResult(
                itemId = itemId,
                predictedDaysUntilNeeded = null,
                confidence = 0.0,
                source = PredictionSource.INSUFFICIENT_DATA,
            )
        }
    }
}

enum class PredictionSource {
    STATISTICAL,
    ML_MODEL,
    INSUFFICIENT_DATA,
}

data class PredictionResult(
    val itemId: String,
    val predictedDaysUntilNeeded: Double?,
    val confidence: Double,
    val source: PredictionSource,
)
```

### Integration with Check Flow

```kotlin
// Updated CheckItemUseCase.kt
class CheckItemUseCase @Inject constructor(
    private val currentListRepository: CurrentListRepository,
    private val recordPurchaseUseCase: RecordPurchaseUseCase,
) {
    suspend operator fun invoke(id: String, isChecked: Boolean): Result<Unit> {
        return if (isChecked) {
            // Get item details before checking
            val listItem = currentListRepository.getById(id)
                ?: return Result.failure(IllegalArgumentException("Item not found"))

            // Check the item
            currentListRepository.checkItem(id)
                .onSuccess {
                    // Record purchase asynchronously
                    // Don't wait for this to complete
                    recordPurchaseUseCase(
                        itemId = listItem.itemId,
                        quantity = listItem.quantity,
                    )
                }
        } else {
            currentListRepository.uncheckItem(id)
        }
    }
}

// RecordPurchaseUseCase.kt
class RecordPurchaseUseCase @Inject constructor(
    private val purchaseRepository: PurchaseRepository,
    private val patternWorkScheduler: PatternWorkScheduler,
    @ApplicationScope private val scope: CoroutineScope,
) {
    operator fun invoke(itemId: String, quantity: Int) {
        scope.launch {
            val event = PurchaseEvent(
                itemId = itemId,
                quantity = quantity,
                purchasedAt = Clock.System.now(),
            )

            purchaseRepository.recordPurchase(event)
                .onSuccess {
                    // Trigger pattern recalculation
                    patternWorkScheduler.triggerImmediateCalculation()
                }
                .onFailure { e ->
                    Timber.e(e, "Failed to record purchase")
                }
        }
    }
}
```

---

## 8. Testing Requirements

### Unit Tests

| Component | Test Cases | Coverage |
|-----------|------------|----------|
| PatternCalculator | Valid patterns, edge cases, confidence | 100% |
| PurchaseRepository | Record, query, debounce | 90% |
| PatternRepository | CRUD, updates | 90% |
| RecordPurchaseUseCase | Success, debounce, failure | 90% |
| TFLiteModelManager | Load, inference, fallback | 80% |

### Algorithm Tests

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

### Integration Tests

```kotlin
@HiltAndroidTest
class PatternIntegrationTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var checkItemUseCase: CheckItemUseCase

    @Inject
    lateinit var purchaseRepository: PurchaseRepository

    @Inject
    lateinit var patternRepository: PatternRepository

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun `checking item records purchase`() = runTest {
        // Add item to list
        val listItem = addItemToList("Milk")

        // Check item
        checkItemUseCase(listItem.id, isChecked = true)

        // Verify purchase recorded
        advanceUntilIdle()
        val purchases = purchaseRepository.getPurchasesForItem(listItem.itemId)
        assertThat(purchases).hasSize(1)
    }

    @Test
    fun `pattern calculated after 3 purchases`() = runTest {
        val itemId = "milk-item"

        // Record 3 purchases over time
        repeat(3) { i ->
            purchaseRepository.recordPurchase(
                PurchaseEvent(
                    itemId = itemId,
                    quantity = 1,
                    purchasedAt = Clock.System.now().minus((21 - i * 7).days),
                )
            )
        }

        // Trigger pattern calculation
        // (In real app, this happens via WorkManager)
        patternWorkScheduler.triggerImmediateCalculation()
        advanceUntilIdle()

        // Verify pattern exists
        val pattern = patternRepository.getPattern(itemId)
        assertThat(pattern).isNotNull()
        assertThat(pattern!!.avgIntervalDays).isCloseTo(7.0, within(0.5))
    }
}
```

---

## 9. Risks & Mitigation

| Risk | Probability | Impact | Mitigation |
|------|-------------|--------|------------|
| Algorithm accuracy issues | Medium | High | Extensive unit tests, conservative thresholds |
| WorkManager reliability | Low | Medium | Fallback to app foreground calculation |
| TFLite model loading fails | Medium | Low | Statistical fallback always available |
| Performance impact | Low | Medium | Profile and optimize, async processing |
| Battery drain from processing | Low | Medium | WorkManager constraints, efficient queries |

### Known Limitations (MVP)

1. **No seasonal adjustment:** Summer vs. winter patterns not distinguished
2. **No weekday patterns:** Weekend vs. weekday shopping not tracked
3. **No quantity learning:** Only interval, not quantity changes
4. **Single household:** Assumes one shopper's patterns

These can be addressed in Phase 2+ with more sophisticated ML models.

---

## 10. Acceptance Criteria

### Sprint Demo Checklist

1. [ ] Check off "Milk" - purchase recorded in database
2. [ ] Check off same item within 1 hour - debounced (no duplicate)
3. [ ] View purchases table - shows all recorded purchases
4. [ ] After 3 purchases, pattern appears in patterns table
5. [ ] Pattern shows correct average interval (±1 day)
6. [ ] Confidence score reflects consistency
7. [ ] WorkManager shows scheduled pattern work
8. [ ] Force trigger calculation - completes without error
9. [ ] TFLite model loads (or graceful fallback)
10. [ ] Check animation shows subtle learning indicator

### Technical Acceptance

- [ ] All algorithm unit tests pass
- [ ] Integration tests verify end-to-end flow
- [ ] No UI thread blocking during recording
- [ ] Battery usage acceptable (< 1% per day)
- [ ] Offline recording works correctly

### Data Verification

```sql
-- Verify purchases recorded
SELECT * FROM purchases ORDER BY purchased_at DESC LIMIT 10;

-- Verify patterns calculated
SELECT
    p.item_id,
    i.name,
    p.avg_interval_days,
    p.confidence_score,
    p.sample_count
FROM patterns p
JOIN items i ON p.item_id = i.id
ORDER BY p.confidence_score DESC;
```

---

## Handoff to Sprint 7-8

### Completed Artifacts
- Purchase recording system
- Pattern calculation algorithm
- Confidence scoring
- Background processing with WorkManager
- TensorFlow Lite infrastructure

### Ready for Sprint 7-8
- PatternRepository can query patterns
- Confidence scores available for filtering
- Last purchase dates trackable
- Suggestion logic can be built on top

### Data Available for Suggestions
```kotlin
// Query for suggestion candidates
suspend fun getSuggestionCandidates(): List<SuggestionCandidate> {
    return patternRepository.getPatternsAboveConfidence(0.6)
        .filter { pattern ->
            val lastPurchase = purchaseRepository.getLastPurchase(pattern.itemId)
            val daysSincePurchase = daysSince(lastPurchase?.purchasedAt)
            daysSincePurchase >= pattern.avgIntervalDays - 1
        }
        .sortedByDescending { it.confidenceScore }
}
```

---

*Sprint 5-6 Document v1.0 | Phase 1 - Restock Android*
