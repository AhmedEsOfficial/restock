package com.restock.di

import com.restock.core.data.di.ApplicationScope
import com.restock.core.data.di.DefaultDispatcher
import com.restock.core.data.di.IoDispatcher
import com.restock.core.data.di.MainDispatcher
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

/**
 * Application-level Hilt module providing common dependencies.
 *
 * This module provides:
 * - Coroutine dispatchers for different use cases
 * - Application-scoped CoroutineScope for long-running operations
 * - Application-wide singleton dependencies
 *
 * Qualifiers are defined in [com.restock.core.data.di.DispatcherQualifiers]
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
        @DefaultDispatcher dispatcher: CoroutineDispatcher
    ): CoroutineScope = CoroutineScope(SupervisorJob() + dispatcher)
}
