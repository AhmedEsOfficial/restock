package com.restock.core.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity representing a purchase event.
 *
 * Purchases are recorded when items are checked off the list.
 * This data feeds into the pattern learning engine.
 */
@Entity(
    tableName = "purchases",
    foreignKeys = [
        ForeignKey(
            entity = ItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["item_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        // Composite index for item-specific temporal queries
        Index(value = ["item_id", "purchased_at"]),
        // Performance optimization: Single-column index for date-only queries
        Index(value = ["purchased_at"]),
    ],
)
data class PurchaseEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "item_id")
    val itemId: String,
    @ColumnInfo(name = "purchased_at")
    val purchasedAt: Long,
    @ColumnInfo(name = "quantity")
    val quantity: Int = 1,
    @ColumnInfo(name = "price_cents")
    val priceCents: Int? = null,
    @ColumnInfo(name = "store_id")
    val storeId: String? = null,
)
