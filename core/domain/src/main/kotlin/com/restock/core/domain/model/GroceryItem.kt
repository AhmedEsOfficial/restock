package com.restock.core.domain.model

import java.time.Instant

/**
 * Domain model representing a grocery item.
 *
 * Items are the core entity in Restock - they represent things users buy.
 * This model is used throughout the domain and presentation layers.
 */
data class GroceryItem(
    val id: String,
    val name: String,
    val normalizedName: String,
    val createdAt: Instant,
    val isActive: Boolean = true
)

/**
 * Extension function to normalize item names for matching and deduplication.
 *
 * Normalization:
 * - Trims whitespace
 * - Converts to lowercase
 * - Collapses multiple spaces to single space
 */
fun String.normalizeItemName(): String =
    this.trim()
        .lowercase()
        .replace(Regex("\\s+"), " ")
