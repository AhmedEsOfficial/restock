package com.restock.core.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity representing a learned purchase pattern.
 *
 * Patterns are calculated from purchase history and used
 * to generate smart suggestions.
 */
@Entity(
    tableName = "patterns",
    foreignKeys = [
        ForeignKey(
            entity = ItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["item_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["confidence_score"])
    ]
)
data class PatternEntity(
    @PrimaryKey
    @ColumnInfo(name = "item_id")
    val itemId: String,

    @ColumnInfo(name = "avg_interval_days")
    val avgIntervalDays: Double,

    @ColumnInfo(name = "confidence_score")
    val confidenceScore: Double,

    @ColumnInfo(name = "sample_count")
    val sampleCount: Int,

    @ColumnInfo(name = "last_calculated")
    val lastCalculated: Long,

    @ColumnInfo(name = "last_suggested")
    val lastSuggested: Long? = null
)
