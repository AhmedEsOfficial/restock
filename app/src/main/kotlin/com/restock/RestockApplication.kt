package com.restock

import android.app.Application
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.restock.core.domain.scheduler.PatternWorkScheduler
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * Main application class for Restock.
 *
 * This class serves as the entry point for:
 * - Hilt dependency injection
 * - WorkManager configuration with Hilt worker factory
 * - Scheduling periodic background pattern calculation
 *
 * Implements [Configuration.Provider] to provide a custom WorkManager configuration
 * that enables Hilt dependency injection in Worker classes via [HiltWorkerFactory].
 */
@HiltAndroidApp
class RestockApplication :
    Application(),
    Configuration.Provider {
    /**
     * Hilt-provided worker factory that enables dependency injection in workers.
     *
     * This factory creates workers with their dependencies injected via Hilt,
     * allowing workers like PatternCalculationWorker to receive repository
     * and use case dependencies.
     */
    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    /**
     * Pattern work scheduler for background pattern calculation.
     *
     * Injected to schedule periodic pattern calculation on app startup.
     */
    @Inject
    lateinit var patternWorkScheduler: PatternWorkScheduler

    /**
     * WorkManager configuration that integrates with Hilt.
     *
     * Key configurations:
     * - Uses [HiltWorkerFactory] for worker creation with DI
     * - Sets minimum logging level based on build type
     *
     * Note: This property is accessed by WorkManager during initialization.
     * WorkManager initialization is disabled in the manifest and performed
     * manually via on-demand initialization to ensure Hilt has completed injection first.
     */
    override val workManagerConfiguration: Configuration
        get() =
            Configuration
                .Builder()
                .setWorkerFactory(workerFactory)
                .setMinimumLoggingLevel(
                    if (BuildConfig.DEBUG) Log.DEBUG else Log.INFO,
                )
                .build()

    override fun onCreate() {
        super.onCreate()

        // Schedule periodic pattern calculation (daily)
        // This is idempotent - if already scheduled, it will be kept (not replaced)
        patternWorkScheduler.schedulePeriodicCalculation()
    }
}
