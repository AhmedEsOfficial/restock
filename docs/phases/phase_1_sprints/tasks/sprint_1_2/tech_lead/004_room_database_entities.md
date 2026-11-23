# Task: Implement Room Database with Core Entities

**Sprint:** 1-2 (Project Setup & Core Infrastructure)
**Owner:** Tech Lead / Senior Developer
**Estimate:** 10 hours (T-2.1 through T-2.9 combined)
**Priority:** P0
**Prerequisites:** 003_hilt_dependency_injection.md

---

## Sprint Context

The Room database is the foundation of Restock's offline-first architecture. All grocery data, purchase history, and learned patterns are stored locally. This task implements all five core entities and their DAOs.

### Database Schema Overview
```
items ─────────┬──── current_list
               ├──── purchases
               ├──── patterns
               └──── meal_tags
```

---

## Task Description

Implement Room database with all core entities, DAOs, type converters, and performance indices.

## Entity Implementations

### 1. ItemEntity.kt

```kotlin
package com.restock.core.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "items",
    indices = [Index(value = ["normalized_name"], unique = true)]
)
data class ItemEntity(
    @PrimaryKey
    val id: String,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "normalized_name")
    val normalizedName: String,

    @ColumnInfo(name = "created_at")
    val createdAt: Long,

    @ColumnInfo(name = "is_active")
    val isActive: Boolean = true,
)
```

### 2. CurrentListEntity.kt

```kotlin
package com.restock.core.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "current_list",
    foreignKeys = [
        ForeignKey(
            entity = ItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["item_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["position"]),
        Index(value = ["is_checked"]),
        Index(value = ["item_id"])
    ]
)
data class CurrentListEntity(
    @PrimaryKey
    val id: String,

    @ColumnInfo(name = "item_id")
    val itemId: String,

    @ColumnInfo(name = "quantity")
    val quantity: Int = 1,

    @ColumnInfo(name = "unit")
    val unit: String? = null,

    @ColumnInfo(name = "added_at")
    val addedAt: Long,

    @ColumnInfo(name = "checked_at")
    val checkedAt: Long? = null,

    @ColumnInfo(name = "position")
    val position: Int,

    @ColumnInfo(name = "is_checked")
    val isChecked: Boolean = false,
)
```

### 3. PurchaseEntity.kt

```kotlin
package com.restock.core.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "purchases",
    foreignKeys = [
        ForeignKey(
            entity = ItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["item_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["item_id", "purchased_at"])]
)
data class PurchaseEntity(
    @PrimaryKey
    val id: String,

    @ColumnInfo(name = "item_id")
    val itemId: String,

    @ColumnInfo(name = "purchased_at")
    val purchasedAt: Long,

    @ColumnInfo(name = "quantity")
    val quantity: Int = 1,

    @ColumnInfo(name = "price_cents")
    val priceCents: Int? = null,

    @ColumnInfo(name = "store_id")
    val storeId: String? = null,
)
```

### 4. PatternEntity.kt

```kotlin
package com.restock.core.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "patterns",
    foreignKeys = [
        ForeignKey(
            entity = ItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["item_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["confidence_score"])]
)
data class PatternEntity(
    @PrimaryKey
    @ColumnInfo(name = "item_id")
    val itemId: String,

    @ColumnInfo(name = "avg_interval_days")
    val avgIntervalDays: Double,

    @ColumnInfo(name = "confidence_score")
    val confidenceScore: Double,

    @ColumnInfo(name = "sample_count")
    val sampleCount: Int,

    @ColumnInfo(name = "last_calculated")
    val lastCalculated: Long,

    @ColumnInfo(name = "last_suggested")
    val lastSuggested: Long? = null,
)
```

### 5. MealTagEntity.kt

```kotlin
package com.restock.core.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "meal_tags",
    foreignKeys = [
        ForeignKey(
            entity = ItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["item_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["item_id", "meal_type"], unique = true)]
)
data class MealTagEntity(
    @PrimaryKey
    val id: String,

    @ColumnInfo(name = "item_id")
    val itemId: String,

    @ColumnInfo(name = "meal_type")
    val mealType: String, // 'breakfast', 'lunch', 'dinner', 'snack'

    @ColumnInfo(name = "frequency_count")
    val frequencyCount: Int = 1,
)
```

## DAO Implementations

### ItemDao.kt

```kotlin
package com.restock.core.data.database.dao

import androidx.room.*
import com.restock.core.data.database.entity.ItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDao {
    @Query("SELECT * FROM items WHERE is_active = 1 ORDER BY name ASC")
    fun observeActiveItems(): Flow<List<ItemEntity>>

    @Query("SELECT * FROM items WHERE id = :id")
    suspend fun getById(id: String): ItemEntity?

    @Query("SELECT * FROM items WHERE normalized_name = :normalizedName LIMIT 1")
    suspend fun findByNormalizedName(normalizedName: String): ItemEntity?

    @Query("SELECT * FROM items WHERE normalized_name LIKE :query || '%' AND is_active = 1 ORDER BY name ASC LIMIT :limit")
    suspend fun searchByName(query: String, limit: Int = 10): List<ItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: ItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<ItemEntity>)

    @Update
    suspend fun update(item: ItemEntity)

    @Query("UPDATE items SET is_active = 0 WHERE id = :id")
    suspend fun softDelete(id: String)

    @Query("DELETE FROM items WHERE id = :id")
    suspend fun hardDelete(id: String)
}
```

### CurrentListDao.kt

```kotlin
package com.restock.core.data.database.dao

import androidx.room.*
import com.restock.core.data.database.entity.CurrentListEntity
import kotlinx.coroutines.flow.Flow

data class CurrentListWithItem(
    val id: String,
    val itemId: String,
    val name: String,
    val normalizedName: String,
    val quantity: Int,
    val unit: String?,
    val addedAt: Long,
    val checkedAt: Long?,
    val position: Int,
    val isChecked: Boolean,
)

@Dao
interface CurrentListDao {
    @Query("""
        SELECT cl.id, cl.item_id as itemId, i.name, i.normalized_name as normalizedName,
               cl.quantity, cl.unit, cl.added_at as addedAt, cl.checked_at as checkedAt,
               cl.position, cl.is_checked as isChecked
        FROM current_list cl
        INNER JOIN items i ON cl.item_id = i.id
        ORDER BY cl.is_checked ASC, cl.position ASC
    """)
    fun observeCurrentListWithItems(): Flow<List<CurrentListWithItem>>

    @Query("SELECT * FROM current_list WHERE id = :id")
    suspend fun getById(id: String): CurrentListEntity?

    @Query("SELECT * FROM current_list WHERE item_id = :itemId AND is_checked = 0 LIMIT 1")
    suspend fun findUncheckedByItemId(itemId: String): CurrentListEntity?

    @Query("SELECT item_id FROM current_list")
    suspend fun getCurrentItemIds(): List<String>

    @Query("SELECT item_id FROM current_list")
    fun observeCurrentItemIds(): Flow<List<String>>

    @Query("SELECT MAX(position) FROM current_list WHERE is_checked = 0")
    suspend fun getMaxPosition(): Int?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: CurrentListEntity)

    @Update
    suspend fun update(entry: CurrentListEntity)

    @Query("UPDATE current_list SET is_checked = :isChecked, checked_at = :checkedAt WHERE id = :id")
    suspend fun updateCheckedStatus(id: String, isChecked: Boolean, checkedAt: Long?)

    @Query("UPDATE current_list SET quantity = :quantity, unit = :unit WHERE id = :id")
    suspend fun updateQuantity(id: String, quantity: Int, unit: String?)

    @Query("UPDATE current_list SET position = :position WHERE id = :id")
    suspend fun updatePosition(id: String, position: Int)

    @Transaction
    suspend fun updatePositions(updates: List<Pair<String, Int>>) {
        updates.forEach { (id, position) ->
            updatePosition(id, position)
        }
    }

    @Query("DELETE FROM current_list WHERE id = :id")
    suspend fun delete(id: String)

    @Query("DELETE FROM current_list WHERE is_checked = 1")
    suspend fun deleteAllChecked()
}
```

### PurchaseDao.kt

```kotlin
package com.restock.core.data.database.dao

import androidx.room.*
import com.restock.core.data.database.entity.PurchaseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PurchaseDao {
    @Query("SELECT * FROM purchases WHERE item_id = :itemId ORDER BY purchased_at DESC")
    suspend fun getPurchasesForItem(itemId: String): List<PurchaseEntity>

    @Query("SELECT * FROM purchases WHERE item_id = :itemId ORDER BY purchased_at DESC")
    fun observePurchasesForItem(itemId: String): Flow<List<PurchaseEntity>>

    @Query("SELECT * FROM purchases WHERE item_id = :itemId ORDER BY purchased_at DESC LIMIT 1")
    suspend fun getLastPurchase(itemId: String): PurchaseEntity?

    @Query("SELECT * FROM purchases WHERE item_id = :itemId AND purchased_at > :afterTimestamp ORDER BY purchased_at DESC LIMIT 1")
    suspend fun getRecentPurchase(itemId: String, afterTimestamp: Long): PurchaseEntity?

    @Query("SELECT DISTINCT item_id FROM purchases")
    suspend fun getItemIdsWithPurchases(): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(purchase: PurchaseEntity)

    @Update
    suspend fun update(purchase: PurchaseEntity)

    @Query("DELETE FROM purchases WHERE id = :id")
    suspend fun delete(id: String)
}
```

### PatternDao.kt

```kotlin
package com.restock.core.data.database.dao

import androidx.room.*
import com.restock.core.data.database.entity.PatternEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PatternDao {
    @Query("SELECT * FROM patterns WHERE item_id = :itemId")
    suspend fun getPattern(itemId: String): PatternEntity?

    @Query("SELECT * FROM patterns ORDER BY confidence_score DESC")
    fun observePatterns(): Flow<List<PatternEntity>>

    @Query("SELECT * FROM patterns WHERE confidence_score >= :minConfidence ORDER BY confidence_score DESC")
    suspend fun getPatternsAboveConfidence(minConfidence: Double): List<PatternEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(pattern: PatternEntity)

    @Query("UPDATE patterns SET confidence_score = MAX(0.3, confidence_score - :by) WHERE item_id = :itemId")
    suspend fun reduceConfidence(itemId: String, by: Double)

    @Query("UPDATE patterns SET last_suggested = :timestamp WHERE item_id = :itemId")
    suspend fun updateLastSuggested(itemId: String, timestamp: Long)

    @Query("DELETE FROM patterns WHERE item_id = :itemId")
    suspend fun delete(itemId: String)
}
```

### MealTagDao.kt

```kotlin
package com.restock.core.data.database.dao

import androidx.room.*
import com.restock.core.data.database.entity.MealTagEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MealTagDao {
    @Query("SELECT * FROM meal_tags WHERE item_id = :itemId")
    suspend fun getTagsForItem(itemId: String): List<MealTagEntity>

    @Query("SELECT * FROM meal_tags WHERE item_id = :itemId AND meal_type = :mealType")
    suspend fun getTag(itemId: String, mealType: String): MealTagEntity?

    @Query("SELECT * FROM meal_tags")
    fun observeAllTags(): Flow<List<MealTagEntity>>

    @Query("""
        SELECT meal_type, SUM(frequency_count) as total
        FROM meal_tags mt
        INNER JOIN items i ON mt.item_id = i.id
        WHERE i.normalized_name LIKE :normalizedName || '%'
        GROUP BY meal_type
        ORDER BY total DESC
        LIMIT 1
    """)
    suspend fun getMostFrequentTagForSimilarItem(normalizedName: String): MealTagEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tag: MealTagEntity)

    @Query("UPDATE meal_tags SET frequency_count = frequency_count + 1 WHERE item_id = :itemId AND meal_type = :mealType")
    suspend fun incrementFrequency(itemId: String, mealType: String)

    @Query("DELETE FROM meal_tags WHERE item_id = :itemId AND meal_type = :mealType")
    suspend fun deleteTag(itemId: String, mealType: String)
}
```

## RestockDatabase.kt

```kotlin
package com.restock.core.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.restock.core.data.database.dao.*
import com.restock.core.data.database.entity.*

@Database(
    entities = [
        ItemEntity::class,
        CurrentListEntity::class,
        PurchaseEntity::class,
        PatternEntity::class,
        MealTagEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class RestockDatabase : RoomDatabase() {
    abstract fun itemDao(): ItemDao
    abstract fun currentListDao(): CurrentListDao
    abstract fun purchaseDao(): PurchaseDao
    abstract fun patternDao(): PatternDao
    abstract fun mealTagDao(): MealTagDao
}
```

## Acceptance Criteria

- [ ] All 5 core tables defined (items, current_list, purchases, patterns, meal_tags)
- [ ] All DAOs with basic CRUD operations
- [ ] Database version 1 with no pending migrations
- [ ] Foreign key constraints in place
- [ ] Performance indices created
- [ ] Flow-based observation methods for reactive UI
- [ ] Unit tests for each DAO

## Testing Requirements

Create tests for each DAO in `:core:data` test source set.

## Definition of Done

- [ ] All entities compile without errors
- [ ] All DAOs compile without errors
- [ ] RestockDatabase builds successfully
- [ ] DAO unit tests pass (minimum 90% coverage)
- [ ] Database inspection works in Android Studio
- [ ] Schema exported to JSON for migration tracking

## Dependent Tasks

This task blocks:
- Repository implementations (Sprint 3-4)
- Pattern learning engine (Sprint 5-6)
- All feature development

## Notes

- Use `exportSchema = true` for migration support
- Keep entity names singular (Item, not Items)
- Use nullable types only where truly optional
- Document complex queries with comments
