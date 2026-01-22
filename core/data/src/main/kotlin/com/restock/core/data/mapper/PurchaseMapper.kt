package com.restock.core.data.mapper

import com.restock.core.data.database.entity.PurchaseEntity
import com.restock.core.domain.model.Purchase
import java.time.Instant

/**
 * Maps PurchaseEntity to Purchase domain model.
 */
fun PurchaseEntity.toDomain(): Purchase = Purchase(
    id = id,
    itemId = itemId,
    purchasedAt = Instant.ofEpochMilli(purchasedAt),
    quantity = quantity,
    priceCents = priceCents,
    storeId = storeId
)

/**
 * Maps Purchase domain model to PurchaseEntity.
 */
fun Purchase.toEntity(): PurchaseEntity = PurchaseEntity(
    id = id,
    itemId = itemId,
    purchasedAt = purchasedAt.toEpochMilli(),
    quantity = quantity,
    priceCents = priceCents,
    storeId = storeId
)

/**
 * Maps a list of PurchaseEntities to domain models.
 */
fun List<PurchaseEntity>.toDomain(): List<Purchase> = map { it.toDomain() }
