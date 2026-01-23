package com.restock.core.domain.repository

import com.restock.core.domain.model.Pattern
import com.restock.core.domain.model.PatternWithItem
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for learned patterns.
 *
 * Implementations manage pattern storage and retrieval.
 */
interface PatternRepository {
    /**
     * Observe all patterns with item details.
     */
    fun observePatternsWithItems(): Flow<List<PatternWithItem>>

    /**
     * Get high-confidence patterns that are ready for suggestions.
     */
    suspend fun getSuggestionReadyPatterns(): List<PatternWithItem>

    /**
     * Get the pattern for a specific item.
     */
    suspend fun getPatternForItem(itemId: String): Pattern?

    /**
     * Get all patterns.
     */
    suspend fun getAllPatterns(): List<Pattern>

    /**
     * Save or update a pattern.
     */
    suspend fun savePattern(pattern: Pattern): Result<Unit>

    /**
     * Save or update multiple patterns.
     */
    suspend fun savePatterns(patterns: List<Pattern>): Result<Unit>

    /**
     * Update the last suggested timestamp for a pattern.
     */
    suspend fun updateLastSuggested(itemId: String): Result<Unit>

    /**
     * Delete a pattern.
     */
    suspend fun deletePattern(itemId: String): Result<Unit>
}
