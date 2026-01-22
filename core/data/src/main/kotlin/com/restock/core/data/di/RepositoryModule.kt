package com.restock.core.data.di

import com.restock.core.data.repository.CurrentListRepositoryImpl
import com.restock.core.data.repository.ItemRepositoryImpl
import com.restock.core.data.repository.MealTagRepositoryImpl
import com.restock.core.data.repository.PatternRepositoryImpl
import com.restock.core.data.repository.PurchaseRepositoryImpl
import com.restock.core.domain.repository.CurrentListRepository
import com.restock.core.domain.repository.ItemRepository
import com.restock.core.domain.repository.MealTagRepository
import com.restock.core.domain.repository.PatternRepository
import com.restock.core.domain.repository.PurchaseRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module that binds repository interfaces to their implementations.
 *
 * This module uses @Binds for efficiency since we're mapping interfaces
 * to their implementations without any custom creation logic.
 *
 * All repositories are scoped as Singleton to ensure consistent state
 * throughout the application lifecycle.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    /**
     * Binds [ItemRepositoryImpl] to [ItemRepository] interface.
     */
    @Binds
    @Singleton
    abstract fun bindItemRepository(impl: ItemRepositoryImpl): ItemRepository

    /**
     * Binds [CurrentListRepositoryImpl] to [CurrentListRepository] interface.
     */
    @Binds
    @Singleton
    abstract fun bindCurrentListRepository(impl: CurrentListRepositoryImpl): CurrentListRepository

    /**
     * Binds [PurchaseRepositoryImpl] to [PurchaseRepository] interface.
     */
    @Binds
    @Singleton
    abstract fun bindPurchaseRepository(impl: PurchaseRepositoryImpl): PurchaseRepository

    /**
     * Binds [PatternRepositoryImpl] to [PatternRepository] interface.
     */
    @Binds
    @Singleton
    abstract fun bindPatternRepository(impl: PatternRepositoryImpl): PatternRepository

    /**
     * Binds [MealTagRepositoryImpl] to [MealTagRepository] interface.
     */
    @Binds
    @Singleton
    abstract fun bindMealTagRepository(impl: MealTagRepositoryImpl): MealTagRepository
}
