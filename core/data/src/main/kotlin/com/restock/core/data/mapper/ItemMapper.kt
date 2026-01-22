package com.restock.core.data.mapper

import com.restock.core.data.database.entity.ItemEntity
import com.restock.core.domain.model.GroceryItem
import java.time.Instant

/**
 * Maps ItemEntity to GroceryItem domain model.
 */
fun ItemEntity.toDomain(): GroceryItem = GroceryItem(
    id = id,
    name = name,
    normalizedName = normalizedName,
    createdAt = Instant.ofEpochMilli(createdAt),
    isActive = isActive
)

/**
 * Maps GroceryItem domain model to ItemEntity.
 */
fun GroceryItem.toEntity(): ItemEntity = ItemEntity(
    id = id,
    name = name,
    normalizedName = normalizedName,
    createdAt = createdAt.toEpochMilli(),
    isActive = isActive
)

/**
 * Maps a list of ItemEntities to domain models.
 */
fun List<ItemEntity>.toDomain(): List<GroceryItem> = map { it.toDomain() }
