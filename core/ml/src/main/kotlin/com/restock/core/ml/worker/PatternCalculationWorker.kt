package com.restock.core.ml.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.restock.core.domain.usecase.CalculatePatternsUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * WorkManager worker that calculates purchase patterns in the background.
 *
 * This worker is triggered:
 * 1. Periodically (daily) via [PatternWorkSchedulerImpl.schedulePeriodicCalculation]
 * 2. Immediately after a purchase via [PatternWorkSchedulerImpl.triggerImmediateCalculation]
 *
 * The worker delegates the actual pattern calculation to [CalculatePatternsUseCase],
 * which processes all items with sufficient purchase history (3+ purchases) and
 * saves the computed patterns to the repository.
 *
 * Key behaviors:
 * - Uses [HiltWorker] for dependency injection
 * - Returns [Result.success] when calculation completes (even with partial failures)
 * - Returns [Result.retry] when more than half of items fail
 * - Returns [Result.retry] on unexpected exceptions (uses exponential backoff)
 * - Never returns [Result.failure] to avoid permanent cancellation
 *
 * Constraints applied by scheduler:
 * - Requires battery not low (for periodic work)
 * - Exponential backoff for periodic, linear for immediate
 */
@HiltWorker
class PatternCalculationWorker
    @AssistedInject
    constructor(
        @Assisted context: Context,
        @Assisted params: WorkerParameters,
        private val calculatePatternsUseCase: CalculatePatternsUseCase,
    ) : CoroutineWorker(context, params) {
        override suspend fun doWork(): Result {
            log("Starting pattern calculation work")

            return calculatePatternsUseCase()
                .fold(
                    onSuccess = { result ->
                        log(
                            "Pattern calculation complete: " +
                                "processed=${result.itemsProcessed}, " +
                                "new=${result.patternsCalculated}, " +
                                "updated=${result.patternsUpdated}, " +
                                "failed=${result.patternsFailed}",
                        )

                        // If more than half failed, retry
                        if (result.itemsProcessed > 0 &&
                            result.patternsFailed > result.itemsProcessed / 2
                        ) {
                            log("More than half of calculations failed, scheduling retry")
                            Result.retry()
                        } else {
                            Result.success()
                        }
                    },
                    onFailure = { exception ->
                        log("Pattern calculation work failed: ${exception.message}")
                        // Retry on failure - WorkManager will use backoff policy
                        Result.retry()
                    },
                )
        }

        private fun log(message: String) {
            if (DEBUG_LOGGING_ENABLED) {
                Log.d(TAG, message)
            }
        }

        companion object {
            private const val TAG = "PatternCalculationWorker"

            /** Work name for periodic pattern calculation (daily). */
            const val PERIODIC_WORK_NAME = "pattern_calculation_periodic"

            /** Work name for immediate pattern calculation (after purchase). */
            const val IMMEDIATE_WORK_NAME = "pattern_calculation_immediate"

            /** Tag for periodic work requests. */
            const val TAG_PERIODIC = "pattern_periodic"

            /** Tag for immediate work requests. */
            const val TAG_IMMEDIATE = "pattern_immediate"

            /** Enable debug logging for development. */
            private const val DEBUG_LOGGING_ENABLED = true
        }
    }
