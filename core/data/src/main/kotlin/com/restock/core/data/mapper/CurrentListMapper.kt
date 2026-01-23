package com.restock.core.data.mapper

import com.restock.core.data.database.dao.CurrentListItemWithDetails
import com.restock.core.data.database.entity.CurrentListEntity
import com.restock.core.domain.model.CurrentListItem
import java.time.Instant

/**
 * Maps CurrentListItemWithDetails (from DAO JOIN) to CurrentListItem domain model.
 */
fun CurrentListItemWithDetails.toDomain(): CurrentListItem =
    CurrentListItem(
        id = currentListId,
        itemId = itemId,
        name = itemName,
        normalizedName = normalizedName,
        quantity = quantity,
        unit = unit,
        addedAt = Instant.ofEpochMilli(addedAt),
        checkedAt = checkedAt?.let { Instant.ofEpochMilli(it) },
        position = position,
        isChecked = isChecked,
    )

/**
 * Maps CurrentListItem domain model to CurrentListEntity.
 */
fun CurrentListItem.toEntity(): CurrentListEntity =
    CurrentListEntity(
        id = id,
        itemId = itemId,
        quantity = quantity,
        unit = unit,
        addedAt = addedAt.toEpochMilli(),
        checkedAt = checkedAt?.toEpochMilli(),
        position = position,
        isChecked = isChecked,
    )

/**
 * Maps a list of CurrentListItemWithDetails to domain models.
 */
fun List<CurrentListItemWithDetails>.toDomain(): List<CurrentListItem> = map { it.toDomain() }
