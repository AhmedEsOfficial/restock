package com.restock.core.domain.model

import java.time.Instant
import java.util.UUID

/**
 * Represents a purchase event when an item is checked off.
 *
 * A purchase event is recorded when:
 * 1. Item transitions from unchecked to checked
 * 2. No purchase exists within last 4 hours for same item (debounce)
 *
 * This is the input model for [com.restock.core.domain.usecase.RecordPurchaseUseCase].
 * It is converted to a [Purchase] when persisted.
 *
 * @property id Unique identifier for this event (auto-generated if not provided).
 * @property itemId The ID of the grocery item being purchased.
 * @property quantity The quantity being purchased.
 * @property purchasedAt The timestamp when the purchase occurred.
 * @property priceCents Optional price in cents.
 * @property storeId Optional store identifier for future use.
 */
data class PurchaseEvent(
    val id: String = UUID.randomUUID().toString(),
    val itemId: String,
    val quantity: Int,
    val purchasedAt: Instant,
    val priceCents: Int? = null,
    val storeId: String? = null,
) {
    /**
     * Converts this event to a [Purchase] domain model.
     */
    fun toPurchase(): Purchase =
        Purchase(
            id = id,
            itemId = itemId,
            purchasedAt = purchasedAt,
            quantity = quantity,
            priceCents = priceCents,
            storeId = storeId,
        )

    companion object {
        /**
         * Default debounce window in hours.
         * Purchases within this window are aggregated.
         */
        const val DEFAULT_DEBOUNCE_HOURS = 4
    }
}
