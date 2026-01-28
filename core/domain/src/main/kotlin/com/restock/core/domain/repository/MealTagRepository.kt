package com.restock.core.domain.repository

import com.restock.core.domain.model.GroceryItem
import com.restock.core.domain.model.ItemWithMealTags
import com.restock.core.domain.model.MealTag
import com.restock.core.domain.model.MealType
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for meal tag associations.
 *
 * Manages the associations between items and meal types, enabling users
 * to categorize groceries by meal (breakfast, lunch, dinner, snack).
 * Also tracks frequency to learn user preferences over time.
 */
interface MealTagRepository {
    /**
     * Adds a tag to an item.
     *
     * If the tag already exists, increments the frequency count to track
     * how often this item is associated with the meal type.
     *
     * @param itemId The ID of the item to tag
     * @param mealType The meal type to associate
     * @return Result containing the created or updated MealTag
     */
    suspend fun addTag(
        itemId: String,
        mealType: MealType,
    ): Result<MealTag>

    /**
     * Removes a tag from an item.
     *
     * @param itemId The ID of the item
     * @param mealType The meal type to remove
     * @return Result indicating success or failure
     */
    suspend fun removeTag(
        itemId: String,
        mealType: MealType,
    ): Result<Unit>

    /**
     * Gets all meal types associated with an item.
     *
     * @param itemId The ID of the item
     * @return List of meal types for this item
     */
    suspend fun getTagsForItem(itemId: String): List<MealType>

    /**
     * Gets items by meal type.
     *
     * @param mealType The meal type to query
     * @return List of grocery items tagged with this meal type
     */
    suspend fun getItemsByMeal(mealType: MealType): List<GroceryItem>

    /**
     * Observes items from the current list with their meal tags.
     *
     * Used for the Meals tab to display items grouped by meal type.
     * Items can appear in multiple groups if they have multiple tags.
     *
     * @return Flow emitting the current list items with their tags
     */
    fun observeItemsWithTags(): Flow<List<ItemWithMealTags>>

    /**
     * Suggests a meal tag based on item name.
     *
     * Uses keyword matching for common breakfast/lunch/dinner/snack items,
     * falling back to historical tag patterns for similar items.
     *
     * @param itemName The name of the item to suggest a tag for
     * @return The suggested MealType, or null if no suggestion
     */
    suspend fun suggestTag(itemName: String): MealType?
}
