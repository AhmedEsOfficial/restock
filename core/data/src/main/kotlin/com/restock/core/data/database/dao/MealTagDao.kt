package com.restock.core.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.restock.core.data.database.entity.MealTagEntity
import kotlinx.coroutines.flow.Flow

/**
 * Combined view of item with its meal tags.
 */
data class ItemWithMealTags(
    val itemId: String,
    val itemName: String,
    val mealTypes: List<String>
)

/**
 * Data Access Object for meal tag associations.
 */
@Dao
interface MealTagDao {

    /**
     * Observe all meal tags.
     */
    @Query("SELECT * FROM meal_tags ORDER BY frequency_count DESC")
    fun observeAllTags(): Flow<List<MealTagEntity>>

    /**
     * Observe tags for a specific item.
     */
    @Query("SELECT * FROM meal_tags WHERE item_id = :itemId ORDER BY frequency_count DESC")
    fun observeTagsForItem(itemId: String): Flow<List<MealTagEntity>>

    /**
     * Get tags for a specific item.
     */
    @Query("SELECT * FROM meal_tags WHERE item_id = :itemId")
    suspend fun getTagsForItem(itemId: String): List<MealTagEntity>

    /**
     * Get items by meal type.
     */
    @Query("""
        SELECT DISTINCT i.id, i.name
        FROM items i
        INNER JOIN meal_tags mt ON i.id = mt.item_id
        WHERE mt.meal_type = :mealType AND i.is_active = 1
        ORDER BY mt.frequency_count DESC
    """)
    suspend fun getItemsByMealType(mealType: String): List<ItemIdAndName>

    /**
     * Check if a tag exists for an item.
     */
    @Query("SELECT * FROM meal_tags WHERE item_id = :itemId AND meal_type = :mealType LIMIT 1")
    suspend fun findTag(itemId: String, mealType: String): MealTagEntity?

    /**
     * Insert a new tag.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tag: MealTagEntity)

    /**
     * Increment frequency count for an existing tag.
     */
    @Query("UPDATE meal_tags SET frequency_count = frequency_count + 1 WHERE item_id = :itemId AND meal_type = :mealType")
    suspend fun incrementFrequency(itemId: String, mealType: String)

    /**
     * Delete a tag.
     */
    @Query("DELETE FROM meal_tags WHERE item_id = :itemId AND meal_type = :mealType")
    suspend fun delete(itemId: String, mealType: String)

    /**
     * Delete all tags for an item.
     */
    @Query("DELETE FROM meal_tags WHERE item_id = :itemId")
    suspend fun deleteAllForItem(itemId: String)

    /**
     * Get count of tags by meal type.
     */
    @Query("SELECT COUNT(*) FROM meal_tags WHERE meal_type = :mealType")
    suspend fun getCountByMealType(mealType: String): Int
}

/**
 * Simple data class for item ID and name projection.
 */
data class ItemIdAndName(
    val id: String,
    val name: String
)
