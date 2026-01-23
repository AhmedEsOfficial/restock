package com.restock.core.data.di

import android.content.Context
import androidx.room.Room
import com.restock.core.data.database.RestockDatabase
import com.restock.core.data.database.dao.CurrentListDao
import com.restock.core.data.database.dao.ItemDao
import com.restock.core.data.database.dao.MealTagDao
import com.restock.core.data.database.dao.PatternDao
import com.restock.core.data.database.dao.PurchaseDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module providing data layer dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
object DataModule {
    /**
     * Provides the Room database instance.
     */
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
    ): RestockDatabase {
        return Room.databaseBuilder(
            context,
            RestockDatabase::class.java,
            RestockDatabase.DATABASE_NAME,
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    /**
     * Provides the ItemDao.
     */
    @Provides
    @Singleton
    fun provideItemDao(database: RestockDatabase): ItemDao {
        return database.itemDao()
    }

    /**
     * Provides the CurrentListDao.
     */
    @Provides
    @Singleton
    fun provideCurrentListDao(database: RestockDatabase): CurrentListDao {
        return database.currentListDao()
    }

    /**
     * Provides the PurchaseDao.
     */
    @Provides
    @Singleton
    fun providePurchaseDao(database: RestockDatabase): PurchaseDao {
        return database.purchaseDao()
    }

    /**
     * Provides the PatternDao.
     */
    @Provides
    @Singleton
    fun providePatternDao(database: RestockDatabase): PatternDao {
        return database.patternDao()
    }

    /**
     * Provides the MealTagDao.
     */
    @Provides
    @Singleton
    fun provideMealTagDao(database: RestockDatabase): MealTagDao {
        return database.mealTagDao()
    }
}
