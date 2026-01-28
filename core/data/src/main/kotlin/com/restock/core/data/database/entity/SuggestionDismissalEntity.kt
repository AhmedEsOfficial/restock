package com.restock.core.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity for tracking suggestion dismissals.
 *
 * When a user dismisses a suggestion, this entity tracks:
 * - How many times the item has been dismissed
 * - When it was last dismissed
 * - Whether it should be permanently hidden from suggestions
 *
 * After 3 consecutive dismissals without a purchase, the item
 * is marked as persistently dismissed and won't appear in suggestions
 * until the user actually purchases that item.
 */
@Entity(
    tableName = "suggestion_dismissals",
    foreignKeys = [
        ForeignKey(
            entity = ItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["item_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["item_id"], unique = true),
        Index(value = ["is_persistently_dismissed"]),
    ],
)
data class SuggestionDismissalEntity(
    @PrimaryKey
    @ColumnInfo(name = "item_id")
    val itemId: String,
    @ColumnInfo(name = "dismiss_count")
    val dismissCount: Int,
    @ColumnInfo(name = "last_dismissed_at")
    val lastDismissedAt: Long,
    @ColumnInfo(name = "is_persistently_dismissed")
    val isPersistentlyDismissed: Boolean,
)
