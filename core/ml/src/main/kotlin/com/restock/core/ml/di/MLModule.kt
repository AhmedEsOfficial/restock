package com.restock.core.ml.di

import com.restock.core.domain.port.PatternCalculatorPort
import com.restock.core.ml.pattern.PatternCalculator
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module providing ML-related dependencies.
 *
 * Binds pattern calculation interfaces to their implementations.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class MLModule {
    /**
     * Binds [PatternCalculator] to [PatternCalculatorPort].
     *
     * This allows the domain layer to use pattern calculation
     * without directly depending on the ML module.
     */
    @Binds
    @Singleton
    abstract fun bindPatternCalculatorPort(patternCalculator: PatternCalculator): PatternCalculatorPort
}
