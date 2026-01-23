package com.restock.core.data.mapper

import com.restock.core.data.database.dao.ItemIdAndName
import com.restock.core.data.database.entity.MealTagEntity
import com.restock.core.domain.model.GroceryItem
import com.restock.core.domain.model.MealTag
import com.restock.core.domain.model.MealType
import java.time.Instant

/**
 * Maps MealTagEntity to MealTag domain model.
 */
fun MealTagEntity.toDomain(): MealTag =
    MealTag(
        id = id,
        itemId = itemId,
        mealType = MealType.fromString(mealType) ?: MealType.SNACK,
        frequencyCount = frequencyCount,
    )

/**
 * Maps MealTag domain model to MealTagEntity.
 */
fun MealTag.toEntity(): MealTagEntity =
    MealTagEntity(
        id = id,
        itemId = itemId,
        mealType = mealType.name.lowercase(),
        frequencyCount = frequencyCount,
    )

/**
 * Maps a list of MealTagEntities to domain models.
 */
fun List<MealTagEntity>.toDomain(): List<MealTag> = map { it.toDomain() }

/**
 * Maps ItemIdAndName to a minimal GroceryItem for meal tag queries.
 *
 * Note: This creates a partial GroceryItem with default values for fields
 * not returned by the meal tag query.
 */
fun ItemIdAndName.toGroceryItem(): GroceryItem =
    GroceryItem(
        id = id,
        name = name,
        normalizedName = name.lowercase().trim(),
        createdAt = Instant.EPOCH,
        isActive = true,
    )

/**
 * Maps a list of ItemIdAndName to GroceryItems.
 */
fun List<ItemIdAndName>.toGroceryItems(): List<GroceryItem> = map { it.toGroceryItem() }
