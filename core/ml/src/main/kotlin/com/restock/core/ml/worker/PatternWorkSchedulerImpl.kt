package com.restock.core.ml.worker

import androidx.lifecycle.LiveData
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkRequest
import com.restock.core.domain.scheduler.PatternWorkScheduler
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * WorkManager-based implementation of [PatternWorkScheduler].
 *
 * Manages background processing of purchase patterns using Android's WorkManager,
 * which provides reliable, battery-efficient background execution that survives
 * app restarts and device reboots.
 *
 * Scheduling strategies:
 * - **Periodic**: Daily calculation with KEEP policy (won't replace existing schedule)
 * - **Immediate**: On-demand calculation with REPLACE policy (avoids queue buildup)
 *
 * Constraints:
 * - Both work types require battery not low to preserve device resources
 *
 * Backoff policies:
 * - Periodic work: Exponential backoff (doubles each retry)
 * - Immediate work: Linear backoff (consistent retry interval)
 *
 * Thread safety: All WorkManager operations are thread-safe. This class is
 * annotated with [Singleton] to ensure consistent work scheduling across
 * the application.
 */
@Singleton
class PatternWorkSchedulerImpl
    @Inject
    constructor(
        private val workManager: WorkManager,
    ) : PatternWorkScheduler {
        /**
         * Schedules periodic pattern calculation (daily).
         *
         * Called on app startup in [com.restock.RestockApplication.onCreate].
         * Uses [ExistingPeriodicWorkPolicy.KEEP] to maintain the existing schedule
         * if one already exists, preventing schedule drift.
         *
         * The work is constrained to run when battery is not low to avoid
         * draining the device during low-power situations.
         */
        override fun schedulePeriodicCalculation() {
            val constraints =
                Constraints
                    .Builder()
                    .setRequiresBatteryNotLow(true)
                    .build()

            val workRequest =
                PeriodicWorkRequestBuilder<PatternCalculationWorker>(
                    repeatInterval = PERIODIC_INTERVAL_HOURS,
                    repeatIntervalTimeUnit = TimeUnit.HOURS,
                ).setConstraints(constraints)
                    .setBackoffCriteria(
                        BackoffPolicy.EXPONENTIAL,
                        WorkRequest.MIN_BACKOFF_MILLIS,
                        TimeUnit.MILLISECONDS,
                    )
                    .addTag(PatternCalculationWorker.TAG_PERIODIC)
                    .build()

            workManager.enqueueUniquePeriodicWork(
                PatternCalculationWorker.PERIODIC_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest,
            )
        }

        /**
         * Triggers immediate pattern calculation.
         *
         * Called after a purchase is recorded to update patterns with the
         * latest data. Uses [ExistingWorkPolicy.REPLACE] to cancel any pending
         * immediate calculation and start a fresh one, preventing queue buildup
         * when multiple purchases happen in quick succession.
         *
         * The work is constrained to run when battery is not low. If the battery
         * is low, the calculation will be deferred until conditions improve.
         */
        override fun triggerImmediateCalculation() {
            val constraints =
                Constraints
                    .Builder()
                    .setRequiresBatteryNotLow(true)
                    .build()

            val workRequest =
                OneTimeWorkRequestBuilder<PatternCalculationWorker>()
                    .setConstraints(constraints)
                    .setBackoffCriteria(
                        BackoffPolicy.LINEAR,
                        WorkRequest.MIN_BACKOFF_MILLIS,
                        TimeUnit.MILLISECONDS,
                    )
                    .addTag(PatternCalculationWorker.TAG_IMMEDIATE)
                    .build()

            workManager.enqueueUniqueWork(
                PatternCalculationWorker.IMMEDIATE_WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                workRequest,
            )
        }

        /**
         * Cancels all scheduled pattern calculation work.
         *
         * Useful for:
         * - User opt-out of pattern learning (future settings feature)
         * - Testing scenarios requiring clean state
         * - App data reset
         */
        override fun cancelAll() {
            workManager.cancelUniqueWork(PatternCalculationWorker.PERIODIC_WORK_NAME)
            workManager.cancelUniqueWork(PatternCalculationWorker.IMMEDIATE_WORK_NAME)
        }

        /**
         * Gets the status of periodic pattern calculation work.
         *
         * Useful for debugging and monitoring work execution.
         *
         * @return LiveData of work info list for the periodic work.
         */
        fun getPeriodicWorkInfo(): LiveData<List<WorkInfo>> =
            workManager.getWorkInfosForUniqueWorkLiveData(
                PatternCalculationWorker.PERIODIC_WORK_NAME,
            )

        /**
         * Gets the status of immediate pattern calculation work.
         *
         * @return LiveData of work info list for the immediate work.
         */
        fun getImmediateWorkInfo(): LiveData<List<WorkInfo>> =
            workManager.getWorkInfosForUniqueWorkLiveData(
                PatternCalculationWorker.IMMEDIATE_WORK_NAME,
            )

        companion object {
            /**
             * Interval for periodic pattern calculation in hours.
             * Default: 24 hours (daily).
             */
            const val PERIODIC_INTERVAL_HOURS = 24L
        }
    }
