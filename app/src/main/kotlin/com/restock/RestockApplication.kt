package com.restock

import android.app.Application
import android.content.ComponentCallbacks2
import android.os.Trace
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
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
 * - Performance tracing for startup optimization
 * - Memory management for low-memory situations
 *
 * Implements [Configuration.Provider] to provide a custom WorkManager configuration
 * that enables Hilt dependency injection in Worker classes via [HiltWorkerFactory].
 *
 * Implements [ImageLoaderFactory] to provide optimized Coil configuration.
 *
 * ## Performance Targets
 * - Cold start: < 2 seconds
 * - Memory usage: < 150MB typical
 */
@HiltAndroidApp
class RestockApplication :
    Application(),
    Configuration.Provider,
    ImageLoaderFactory {
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
     * Coil image loader instance for memory management.
     * Lazily initialized to avoid blocking startup.
     */
    private var _imageLoader: ImageLoader? = null

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
        // Start tracing for startup performance profiling
        // View traces with: adb shell perfetto or Android Studio Profiler
        if (BuildConfig.DEBUG) {
            Trace.beginSection("RestockApplication.onCreate")
        }

        super.onCreate()

        if (BuildConfig.DEBUG) {
            Trace.beginSection("Hilt injection complete")
            Trace.endSection()
        }

        // Schedule periodic pattern calculation (daily)
        // This is idempotent - if already scheduled, it will be kept (not replaced)
        if (BuildConfig.DEBUG) {
            Trace.beginSection("PatternWorkScheduler setup")
        }
        patternWorkScheduler.schedulePeriodicCalculation()
        if (BuildConfig.DEBUG) {
            Trace.endSection()
        }

        if (BuildConfig.DEBUG) {
            Trace.endSection() // RestockApplication.onCreate
        }
    }

    /**
     * Creates an optimized Coil [ImageLoader] for efficient image loading.
     *
     * Configuration:
     * - Memory cache: 25% of available memory (Coil default)
     * - Disk cache: 100MB in cache directory
     * - Crossfade enabled for smooth image transitions
     *
     * @return Configured [ImageLoader] instance.
     */
    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(MEMORY_CACHE_PERCENT)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve(DISK_CACHE_DIR))
                    .maxSizeBytes(DISK_CACHE_SIZE_BYTES)
                    .build()
            }
            .crossfade(true)
            .build()
            .also { _imageLoader = it }
    }

    /**
     * Called when the overall system is running low on memory.
     *
     * Clears non-essential caches to free memory and prevent the system
     * from killing our process.
     */
    override fun onLowMemory() {
        super.onLowMemory()
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "onLowMemory: Clearing image caches")
        }
        clearImageCaches()
    }

    /**
     * Called when the operating system determines that it is a good time
     * for a process to trim unneeded memory.
     *
     * Responds to different trim levels appropriately:
     * - [ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN]: App went to background
     * - [ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW]: System is low on memory
     * - [ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL]: System critically low
     *
     * @param level The trim level hint from the system.
     */
    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)

        if (BuildConfig.DEBUG) {
            Log.d(TAG, "onTrimMemory: level=$level (${trimLevelToString(level)})")
        }

        when (level) {
            ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN -> {
                // App went to background - safe to trim some caches
                _imageLoader?.memoryCache?.clear()
            }

            ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW,
            ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL,
            -> {
                // System is low on memory - clear all non-essential caches
                clearImageCaches()
            }

            ComponentCallbacks2.TRIM_MEMORY_BACKGROUND,
            ComponentCallbacks2.TRIM_MEMORY_MODERATE,
            ComponentCallbacks2.TRIM_MEMORY_COMPLETE,
            -> {
                // Process is in background LRU list - clear caches to prevent being killed
                clearImageCaches()
            }
        }
    }

    /**
     * Clears all image caches (memory and disk).
     */
    private fun clearImageCaches() {
        _imageLoader?.memoryCache?.clear()
        // Note: Disk cache clearing is intentionally not done here
        // as it provides value on next app launch
    }

    /**
     * Converts trim level to human-readable string for debugging.
     */
    private fun trimLevelToString(level: Int): String =
        when (level) {
            ComponentCallbacks2.TRIM_MEMORY_COMPLETE -> "COMPLETE"
            ComponentCallbacks2.TRIM_MEMORY_MODERATE -> "MODERATE"
            ComponentCallbacks2.TRIM_MEMORY_BACKGROUND -> "BACKGROUND"
            ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN -> "UI_HIDDEN"
            ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL -> "RUNNING_CRITICAL"
            ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW -> "RUNNING_LOW"
            ComponentCallbacks2.TRIM_MEMORY_RUNNING_MODERATE -> "RUNNING_MODERATE"
            else -> "UNKNOWN($level)"
        }

    companion object {
        private const val TAG = "RestockApplication"

        /** Percentage of available memory for image cache. */
        private const val MEMORY_CACHE_PERCENT = 0.25

        /** Directory name for disk cache. */
        private const val DISK_CACHE_DIR = "image_cache"

        /** Maximum disk cache size: 100MB. */
        private const val DISK_CACHE_SIZE_BYTES = 100L * 1024 * 1024
    }
}
