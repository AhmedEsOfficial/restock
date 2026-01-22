package com.restock.core.domain.model

import java.time.Instant

/**
 * Domain model representing an item in the current shopping list.
 *
 * Contains both the item details and list-specific properties
 * like quantity, position, and checked state.
 */
data class CurrentListItem(
    val id: String,
    val itemId: String,
    val name: String,
    val normalizedName: String,
    val quantity: Int = 1,
    val unit: String? = null,
    val addedAt: Instant,
    val checkedAt: Instant? = null,
    val position: Int,
    val isChecked: Boolean = false
) {
    /**
     * Returns a display string for the quantity if > 1.
     */
    val quantityDisplay: String?
        get() = if (quantity > 1) {
            if (unit != null) "$quantity $unit" else "×$quantity"
        } else {
            unit
        }
}
