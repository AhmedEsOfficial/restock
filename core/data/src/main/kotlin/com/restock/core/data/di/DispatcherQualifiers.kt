package com.restock.core.data.di

import javax.inject.Qualifier

/**
 * Qualifier for the IO dispatcher used for disk and network operations.
 *
 * Use this for:
 * - Room database operations
 * - File I/O
 * - Network requests
 * - DataStore operations
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IoDispatcher

/**
 * Qualifier for the Default dispatcher used for CPU-intensive operations.
 *
 * Use this for:
 * - Heavy computations
 * - Data processing
 * - Pattern calculations
 * - Sorting/filtering large collections
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DefaultDispatcher

/**
 * Qualifier for the Main dispatcher used for UI operations.
 *
 * Use this for:
 * - UI updates
 * - View interactions
 * - Typically avoid using this directly in repositories
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class MainDispatcher

/**
 * Qualifier for the application-scoped CoroutineScope.
 *
 * This scope lives as long as the application and uses a SupervisorJob
 * so that child coroutine failures don't cancel sibling coroutines.
 *
 * Use this for:
 * - Long-running background operations
 * - Work that should outlive individual screens
 * - Pattern calculation workers
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApplicationScope
