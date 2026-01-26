package com.restock.di

import com.restock.core.data.di.DefaultDispatcher
import com.restock.core.data.di.IoDispatcher
import com.restock.core.data.di.MainDispatcher
import com.restock.core.domain.di.ApplicationScope
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * Application-level Hilt module providing common dependencies.
 *
 * This module provides:
 * - Coroutine dispatchers for different use cases
 * - Application-scoped CoroutineScope for long-running operations
 * - Application-wide singleton dependencies
 *
 * Dispatcher qualifiers are defined in [com.restock.core.data.di.DispatcherQualifiers]
 * and [ApplicationScope] is defined in [com.restock.core.domain.di.CoroutineQualifiers]
 * to allow usage across all modules.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    /**
     * Provides the IO dispatcher for disk and network operations.
     */
    @Provides
    @Singleton
    @IoDispatcher
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

    /**
     * Provides the Default dispatcher for CPU-intensive operations.
     */
    @Provides
    @Singleton
    @DefaultDispatcher
    fun provideDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default

    /**
     * Provides the Main dispatcher for UI operations.
     */
    @Provides
    @Singleton
    @MainDispatcher
    fun provideMainDispatcher(): CoroutineDispatcher = Dispatchers.Main

    /**
     * Provides an application-scoped CoroutineScope.
     *
     * Uses SupervisorJob + Default dispatcher for background work
     * that should outlive individual screens/components.
     */
    @Provides
    @Singleton
    @ApplicationScope
    fun provideApplicationScope(
        @DefaultDispatcher dispatcher: CoroutineDispatcher,
    ): CoroutineScope = CoroutineScope(SupervisorJob() + dispatcher)
}
