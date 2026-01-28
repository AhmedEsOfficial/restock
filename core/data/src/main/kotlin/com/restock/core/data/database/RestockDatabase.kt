package com.restock.core.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.restock.core.data.database.converter.Converters
import com.restock.core.data.database.dao.CurrentListDao
import com.restock.core.data.database.dao.ItemDao
import com.restock.core.data.database.dao.MealTagDao
import com.restock.core.data.database.dao.PatternDao
import com.restock.core.data.database.dao.PurchaseDao
import com.restock.core.data.database.dao.SuggestionDismissalDao
import com.restock.core.data.database.entity.CurrentListEntity
import com.restock.core.data.database.entity.ItemEntity
import com.restock.core.data.database.entity.MealTagEntity
import com.restock.core.data.database.entity.PatternEntity
import com.restock.core.data.database.entity.PurchaseEntity
import com.restock.core.data.database.entity.SuggestionDismissalEntity

/**
 * Room database for Restock application.
 *
 * Contains all core tables:
 * - items: Grocery item catalog
 * - current_list: Current shopping list
 * - purchases: Purchase history for pattern learning
 * - patterns: Learned purchase patterns
 * - meal_tags: Item-to-meal associations
 * - suggestion_dismissals: Track dismissed suggestions
 */
@Database(
    entities = [
        ItemEntity::class,
        CurrentListEntity::class,
        PurchaseEntity::class,
        PatternEntity::class,
        MealTagEntity::class,
        SuggestionDismissalEntity::class,
    ],
    version = 2,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class RestockDatabase : RoomDatabase() {
    abstract fun itemDao(): ItemDao

    abstract fun currentListDao(): CurrentListDao

    abstract fun purchaseDao(): PurchaseDao

    abstract fun patternDao(): PatternDao

    abstract fun mealTagDao(): MealTagDao

    abstract fun suggestionDismissalDao(): SuggestionDismissalDao

    companion object {
        const val DATABASE_NAME = "restock_database"
    }
}
