package com.restock.core.data.di

import com.restock.core.data.scheduler.NoOpPatternWorkScheduler
import com.restock.core.domain.scheduler.PatternWorkScheduler
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module that provides scheduler implementations.
 *
 * Currently provides a no-op implementation of [PatternWorkScheduler].
 * This will be replaced with a WorkManager-based implementation in
 * Sprint 5-6, Task 003 (Background Processing).
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class SchedulerModule {
    /**
     * Binds [NoOpPatternWorkScheduler] to [PatternWorkScheduler] interface.
     *
     * TODO: Replace with WorkManagerPatternWorkScheduler in Task 003.
     */
    @Binds
    @Singleton
    abstract fun bindPatternWorkScheduler(impl: NoOpPatternWorkScheduler): PatternWorkScheduler
}
