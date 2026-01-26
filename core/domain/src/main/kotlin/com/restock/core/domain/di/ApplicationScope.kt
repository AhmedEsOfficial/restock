package com.restock.core.domain.di

import javax.inject.Qualifier

/**
 * Qualifier for the application-scoped CoroutineScope.
 *
 * This scope lives as long as the application and uses a SupervisorJob
 * so that child coroutine failures don't cancel sibling coroutines.
 *
 * Use this for:
 * - Long-running background operations that should outlive ViewModels
 * - Fire-and-forget operations like purchase recording
 * - Work that should continue even if the user navigates away
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApplicationScope
