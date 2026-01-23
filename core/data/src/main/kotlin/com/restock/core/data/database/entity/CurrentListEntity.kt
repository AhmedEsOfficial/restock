package com.restock.core.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity representing an item in the current shopping list.
 *
 * This is separate from ItemEntity to allow the same item to appear
 * in multiple lists over time while maintaining history.
 */
@Entity(
    tableName = "current_list",
    foreignKeys = [
        ForeignKey(
            entity = ItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["item_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["item_id"]),
        Index(value = ["position"]),
    ],
)
data class CurrentListEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "item_id")
    val itemId: String,
    @ColumnInfo(name = "quantity")
    val quantity: Int = 1,
    @ColumnInfo(name = "unit")
    val unit: String? = null,
    @ColumnInfo(name = "added_at")
    val addedAt: Long,
    @ColumnInfo(name = "checked_at")
    val checkedAt: Long? = null,
    @ColumnInfo(name = "position")
    val position: Int,
    @ColumnInfo(name = "is_checked")
    val isChecked: Boolean = false,
)
