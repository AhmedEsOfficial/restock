package com.restock.core.domain.scheduler

/**
 * Scheduler interface for pattern calculation work.
 *
 * Implementations manage background processing of purchase patterns.
 * The actual WorkManager implementation will be in the :core:data module
 * (Sprint 5-6, Task 003: Background Processing).
 *
 * This interface follows the dependency inversion principle, allowing
 * the domain layer to trigger pattern recalculation without depending
 * on WorkManager or other Android-specific APIs.
 */
interface PatternWorkScheduler {
    /**
     * Schedule periodic pattern calculation.
     *
     * Sets up a daily background job to recalculate patterns for all items
     * with sufficient purchase history. Uses battery-efficient scheduling
     * (runs when battery is not low).
     */
    fun schedulePeriodicCalculation()

    /**
     * Trigger an immediate pattern calculation.
     *
     * Called when a new purchase is recorded to update patterns
     * with the latest data. Uses ExistingWorkPolicy.REPLACE to
     * avoid duplicate calculations.
     */
    fun triggerImmediateCalculation()

    /**
     * Cancel all scheduled pattern calculation work.
     *
     * Called when the user opts out of pattern learning or
     * during app cleanup.
     */
    fun cancelAll()
}
