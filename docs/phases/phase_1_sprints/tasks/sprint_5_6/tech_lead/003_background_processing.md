# Task: Implement Background Processing with WorkManager

**Sprint:** 5-6 (Pattern Learning Engine)
**Owner:** Tech Lead / Senior Developer
**Estimate:** 16 hours (T-7.1 through T-7.7 combined)
**Priority:** P0
**Prerequisites:** 002_pattern_calculation.md

---

## Sprint Context

Pattern calculation must run in the background to avoid blocking the UI thread and to continue processing even when the app is not active. WorkManager provides reliable, battery-efficient background processing.

### Sprint Theme
> *"The intelligence that learns without asking."*

### Key Requirements
- Pattern calculation runs via WorkManager
- Processing doesn't block UI thread
- Battery-efficient scheduling
- Handles app killed during processing

---

## Task Description

Implement WorkManager integration for background pattern calculation with periodic scheduling and on-demand triggers.

## Implementation

### PatternCalculationWorker.kt

```kotlin
package com.restock.core.ml.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.restock.core.domain.repository.PatternRepository
import com.restock.core.domain.repository.PurchaseRepository
import com.restock.core.ml.pattern.PatternCalculator
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber

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
            Timber.d("Starting pattern calculation work")

            val itemIds = purchaseRepository.getItemIdsWithPurchases()
            var patternsCalculated = 0
            var errors = 0

            itemIds.forEach { itemId ->
                try {
                    val purchases = purchaseRepository.getPurchasesForItem(itemId)
                    val pattern = patternCalculator.calculatePattern(purchases)

                    if (pattern != null) {
                        patternRepository.upsertPattern(pattern)
                        patternsCalculated++
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Error calculating pattern for item: $itemId")
                    errors++
                }
            }

            Timber.d("Pattern calculation complete: $patternsCalculated patterns, $errors errors")

            if (errors > itemIds.size / 2) {
                // More than half failed, retry
                Result.retry()
            } else {
                Result.success()
            }
        } catch (e: Exception) {
            Timber.e(e, "Pattern calculation work failed")
            Result.retry()
        }
    }

    companion object {
        const val WORK_NAME = "pattern_calculation"
        const val IMMEDIATE_WORK_NAME = "pattern_calculation_immediate"
    }
}
```

### PatternWorkScheduler.kt

```kotlin
package com.restock.core.ml.worker

import androidx.work.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PatternWorkScheduler @Inject constructor(
    private val workManager: WorkManager,
) {
    /**
     * Schedules periodic pattern calculation (daily).
     * Called on app startup.
     */
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
            .addTag(TAG_PERIODIC)
            .build()

        workManager.enqueueUniquePeriodicWork(
            PatternCalculationWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest,
        )
    }

    /**
     * Triggers immediate pattern calculation.
     * Called after a purchase is recorded.
     */
    fun triggerImmediateCalculation() {
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<PatternCalculationWorker>()
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.LINEAR,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .addTag(TAG_IMMEDIATE)
            .build()

        workManager.enqueueUniqueWork(
            PatternCalculationWorker.IMMEDIATE_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            workRequest,
        )
    }

    /**
     * Cancels all pattern calculation work.
     * Useful for testing or user opt-out.
     */
    fun cancelAll() {
        workManager.cancelUniqueWork(PatternCalculationWorker.WORK_NAME)
        workManager.cancelUniqueWork(PatternCalculationWorker.IMMEDIATE_WORK_NAME)
    }

    /**
     * Gets the status of pattern calculation work.
     */
    fun getWorkInfo() = workManager.getWorkInfosForUniqueWorkLiveData(
        PatternCalculationWorker.WORK_NAME
    )

    companion object {
        private const val TAG_PERIODIC = "pattern_periodic"
        private const val TAG_IMMEDIATE = "pattern_immediate"
    }
}
```

### WorkManager Hilt Module

```kotlin
package com.restock.core.di

import android.content.Context
import androidx.work.WorkManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object WorkManagerModule {

    @Provides
    @Singleton
    fun provideWorkManager(
        @ApplicationContext context: Context,
    ): WorkManager = WorkManager.getInstance(context)
}
```

### HiltWorkerFactory Configuration

```kotlin
// In RestockApplication.kt
@HiltAndroidApp
class RestockApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var patternWorkScheduler: PatternWorkScheduler

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(if (BuildConfig.DEBUG) Log.DEBUG else Log.INFO)
            .build()

    override fun onCreate() {
        super.onCreate()

        // Initialize WorkManager with Hilt
        WorkManager.initialize(this, workManagerConfiguration)

        // Schedule periodic pattern calculation
        patternWorkScheduler.schedulePeriodicCalculation()
    }
}
```

### Gradle Dependencies

```kotlin
// build.gradle.kts (core:ml or app module)
dependencies {
    implementation("androidx.work:work-runtime-ktx:2.9.0")
    implementation("androidx.hilt:hilt-work:1.2.0")
    kapt("androidx.hilt:hilt-compiler:1.2.0")
}
```

### Manifest Configuration

```xml
<!-- AndroidManifest.xml -->
<application>
    <!-- Disable default WorkManager initialization -->
    <provider
        android:name="androidx.startup.InitializationProvider"
        android:authorities="${applicationId}.androidx-startup"
        android:exported="false"
        tools:node="merge">
        <meta-data
            android:name="androidx.work.WorkManagerInitializer"
            android:value="androidx.startup"
            tools:node="remove" />
    </provider>
</application>
```

## Acceptance Criteria

- [ ] WorkManager set up with Hilt
- [ ] PatternCalculationWorker created
- [ ] Periodic scheduling (daily) works
- [ ] On-purchase trigger works
- [ ] Battery constraints respected
- [ ] Worker handles failures gracefully
- [ ] Logging and monitoring in place
- [ ] Unit tests for worker

## Testing

```kotlin
@HiltAndroidTest
class PatternCalculationWorkerTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    private lateinit var context: Context
    private lateinit var purchaseRepository: PurchaseRepository
    private lateinit var patternRepository: PatternRepository

    @Before
    fun setup() {
        hiltRule.inject()
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun `worker calculates patterns successfully`() = runTest {
        // Setup: Add test purchases
        repeat(4) { i ->
            purchaseRepository.recordPurchase(
                PurchaseEvent(
                    itemId = "milk",
                    quantity = 1,
                    purchasedAt = Clock.System.now().minus((i * 7).days),
                )
            )
        }

        // Execute worker
        val worker = TestListenableWorkerBuilder<PatternCalculationWorker>(context)
            .build()
        val result = worker.doWork()

        // Verify
        assertThat(result).isEqualTo(ListenableWorker.Result.success())

        val pattern = patternRepository.getPattern("milk")
        assertThat(pattern).isNotNull()
        assertThat(pattern!!.avgIntervalDays).isCloseTo(7.0, within(0.5))
    }

    @Test
    fun `worker retries on failure`() = runTest {
        // Setup: Mock repository to throw exception
        coEvery { purchaseRepository.getItemIdsWithPurchases() } throws Exception("DB error")

        // Execute worker
        val worker = TestListenableWorkerBuilder<PatternCalculationWorker>(context)
            .build()
        val result = worker.doWork()

        // Verify
        assertThat(result).isEqualTo(ListenableWorker.Result.retry())
    }
}

class PatternWorkSchedulerTest {

    @Test
    fun `schedulePeriodicCalculation enqueues periodic work`() {
        val workManager = mockk<WorkManager>(relaxed = true)
        val scheduler = PatternWorkScheduler(workManager)

        scheduler.schedulePeriodicCalculation()

        verify {
            workManager.enqueueUniquePeriodicWork(
                eq("pattern_calculation"),
                eq(ExistingPeriodicWorkPolicy.KEEP),
                any<PeriodicWorkRequest>()
            )
        }
    }

    @Test
    fun `triggerImmediateCalculation replaces existing work`() {
        val workManager = mockk<WorkManager>(relaxed = true)
        val scheduler = PatternWorkScheduler(workManager)

        scheduler.triggerImmediateCalculation()

        verify {
            workManager.enqueueUniqueWork(
                eq("pattern_calculation_immediate"),
                eq(ExistingWorkPolicy.REPLACE),
                any<OneTimeWorkRequest>()
            )
        }
    }
}
```

## Definition of Done

- [ ] WorkManager properly configured with Hilt
- [ ] Worker executes pattern calculation
- [ ] Periodic scheduling works (daily)
- [ ] Immediate trigger works after purchase
- [ ] Constraints prevent battery drain
- [ ] Retry logic handles failures
- [ ] Unit tests pass
- [ ] Integration test verifies end-to-end

## Dependent Tasks

This task completes:
- Pattern learning backend for Sprint 7-8

## Notes

- Use ExistingWorkPolicy.REPLACE for immediate to avoid queue buildup
- Use ExistingPeriodicWorkPolicy.KEEP to maintain schedule
- Battery constraint prevents calculation when battery low
- Exponential backoff for periodic, linear for immediate
- Log work status for debugging
