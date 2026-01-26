package com.restock.core.data.scheduler

import com.restock.core.domain.scheduler.PatternWorkScheduler
import javax.inject.Inject
import javax.inject.Singleton

/**
 * No-op implementation of [PatternWorkScheduler].
 *
 * This is a placeholder implementation that doesn't perform actual work
 * scheduling. It will be replaced with a WorkManager-based implementation
 * in Task 003 (Background Processing).
 *
 * This allows the purchase recording infrastructure to be fully
 * functional without blocking on the background processing implementation.
 */
@Singleton
class NoOpPatternWorkScheduler
    @Inject
    constructor() : PatternWorkScheduler {
        override fun schedulePeriodicCalculation() {
            // No-op: Will be implemented with WorkManager in Task 003
        }

        override fun triggerImmediateCalculation() {
            // No-op: Will be implemented with WorkManager in Task 003
        }

        override fun cancelAll() {
            // No-op: Will be implemented with WorkManager in Task 003
        }
    }
