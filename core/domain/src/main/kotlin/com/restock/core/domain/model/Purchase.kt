package com.restock.core.domain.model

import java.time.Instant

/**
 * Domain model representing a purchase event.
 *
 * Purchases are recorded when items are checked off the list.
 * This data is used for pattern learning.
 */
data class Purchase(
    val id: String,
    val itemId: String,
    val purchasedAt: Instant,
    val quantity: Int = 1,
    val priceCents: Int? = null,
    val storeId: String? = null
)
