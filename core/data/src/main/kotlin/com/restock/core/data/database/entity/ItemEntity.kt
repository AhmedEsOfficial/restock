package com.restock.core.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity representing a grocery item.
 *
 * Items are the core concept in Restock - they represent things users buy.
 * The normalized name allows for fuzzy matching and deduplication.
 */
@Entity(
    tableName = "items",
    indices = [
        Index(value = ["normalized_name"], unique = true)
    ]
)
data class ItemEntity(
    @PrimaryKey
    val id: String,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "normalized_name")
    val normalizedName: String,

    @ColumnInfo(name = "created_at")
    val createdAt: Long,

    @ColumnInfo(name = "is_active")
    val isActive: Boolean = true
)
