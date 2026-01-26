package com.restock.core.ml.di

import android.content.Context
import androidx.work.WorkManager
import com.restock.core.domain.scheduler.PatternWorkScheduler
import com.restock.core.ml.worker.PatternWorkSchedulerImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module that provides WorkManager dependencies for background pattern processing.
 *
 * This module provides:
 * 1. [WorkManager] instance - For enqueueing and managing background work
 * 2. [PatternWorkScheduler] binding - Connects the interface to [PatternWorkSchedulerImpl]
 *
 * WorkManager is configured with a custom [androidx.work.Configuration] in
 * [com.restock.RestockApplication] to support Hilt dependency injection in workers.
 */
@Module
@InstallIn(SingletonComponent::class)
object WorkManagerModule {
    /**
     * Provides the WorkManager singleton instance.
     *
     * Note: WorkManager.getInstance(context) returns the same instance every time,
     * but marking this as [Singleton] makes the dependency graph clearer and ensures
     * Hilt doesn't call getInstance repeatedly.
     *
     * @param context The application context for WorkManager initialization.
     * @return The WorkManager instance.
     */
    @Provides
    @Singleton
    fun provideWorkManager(
        @ApplicationContext context: Context,
    ): WorkManager = WorkManager.getInstance(context)
}

/**
 * Hilt module that binds scheduler interface to implementation.
 *
 * Separated from [WorkManagerModule] because abstract binding modules
 * cannot contain @Provides methods in the same class.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class SchedulerBindingModule {
    /**
     * Binds [PatternWorkSchedulerImpl] to [PatternWorkScheduler] interface.
     *
     * This replaces the previous [NoOpPatternWorkScheduler] binding from the
     * :core:data module's SchedulerModule.
     *
     * @param impl The WorkManager-based implementation.
     * @return The interface type for injection.
     */
    @Binds
    @Singleton
    abstract fun bindPatternWorkScheduler(impl: PatternWorkSchedulerImpl): PatternWorkScheduler
}
