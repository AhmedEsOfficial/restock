package com.restock.core.domain.repository

import com.restock.core.domain.model.GroceryItem
import com.restock.core.domain.model.ItemWithMealTags
import com.restock.core.domain.model.MealTag
import com.restock.core.domain.model.MealType
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for meal tag associations.
 *
 * Implementations manage the associations between items and meal types.
 */
interface MealTagRepository {

    /**
     * Observe all meal tags.
     */
    fun observeAllTags(): Flow<List<MealTag>>

    /**
     * Observe tags for a specific item.
     */
    fun observeTagsForItem(itemId: String): Flow<List<MealTag>>

    /**
     * Get tags for a specific item.
     */
    suspend fun getTagsForItem(itemId: String): List<MealTag>

    /**
     * Get items by meal type.
     */
    suspend fun getItemsByMealType(mealType: MealType): List<GroceryItem>

    /**
     * Add a tag to an item.
     *
     * If the tag already exists, increments the frequency count.
     */
    suspend fun addTag(itemId: String, mealType: MealType): Result<Unit>

    /**
     * Remove a tag from an item.
     */
    suspend fun removeTag(itemId: String, mealType: MealType): Result<Unit>

    /**
     * Remove all tags for an item.
     */
    suspend fun removeAllTagsForItem(itemId: String): Result<Unit>
}
