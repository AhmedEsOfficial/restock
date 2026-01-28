package com.restock.core.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity representing a meal tag association.
 *
 * Links items to meal types (breakfast, lunch, dinner, snack).
 * Tracks frequency to learn user's meal preferences.
 */
@Entity(
    tableName = "meal_tags",
    foreignKeys = [
        ForeignKey(
            entity = ItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["item_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["item_id", "meal_type"], unique = true),
        Index(value = ["meal_type"]),
    ],
)
data class MealTagEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "item_id")
    val itemId: String,
    @ColumnInfo(name = "meal_type")
    val mealType: String,
    @ColumnInfo(name = "frequency_count")
    val frequencyCount: Int = 1,
)

/**
 * Supported meal types.
 */
object MealTypes {
    const val BREAKFAST = "breakfast"
    const val LUNCH = "lunch"
    const val DINNER = "dinner"
    const val SNACK = "snack"

    val ALL = listOf(BREAKFAST, LUNCH, DINNER, SNACK)
}
