package com.restock.core.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.restock.core.data.database.entity.PatternEntity
import kotlinx.coroutines.flow.Flow

/**
 * Combined view of pattern with item details.
 */
data class PatternWithItem(
    val itemId: String,
    val itemName: String,
    val avgIntervalDays: Double,
    val confidenceScore: Double,
    val sampleCount: Int,
    val lastCalculated: Long,
    val lastSuggested: Long?,
    val lastPurchasedAt: Long?
)

/**
 * Data Access Object for learned patterns.
 */
@Dao
interface PatternDao {

    /**
     * Observe all patterns with item details, sorted by confidence.
     */
    @Query("""
        SELECT
            p.item_id as itemId,
            i.name as itemName,
            p.avg_interval_days as avgIntervalDays,
            p.confidence_score as confidenceScore,
            p.sample_count as sampleCount,
            p.last_calculated as lastCalculated,
            p.last_suggested as lastSuggested,
            (SELECT MAX(purchased_at) FROM purchases WHERE item_id = p.item_id) as lastPurchasedAt
        FROM patterns p
        INNER JOIN items i ON p.item_id = i.id
        WHERE i.is_active = 1
        ORDER BY p.confidence_score DESC
    """)
    fun observePatternsWithItems(): Flow<List<PatternWithItem>>

    /**
     * Get high-confidence patterns for suggestions.
     */
    @Query("""
        SELECT
            p.item_id as itemId,
            i.name as itemName,
            p.avg_interval_days as avgIntervalDays,
            p.confidence_score as confidenceScore,
            p.sample_count as sampleCount,
            p.last_calculated as lastCalculated,
            p.last_suggested as lastSuggested,
            (SELECT MAX(purchased_at) FROM purchases WHERE item_id = p.item_id) as lastPurchasedAt
        FROM patterns p
        INNER JOIN items i ON p.item_id = i.id
        WHERE i.is_active = 1
          AND p.confidence_score >= :minConfidence
          AND p.sample_count >= :minSamples
        ORDER BY p.confidence_score DESC
    """)
    suspend fun getHighConfidencePatterns(
        minConfidence: Double = 0.6,
        minSamples: Int = 3
    ): List<PatternWithItem>

    /**
     * Get pattern for a specific item.
     */
    @Query("SELECT * FROM patterns WHERE item_id = :itemId")
    suspend fun getPatternForItem(itemId: String): PatternEntity?

    /**
     * Get all patterns.
     */
    @Query("SELECT * FROM patterns ORDER BY confidence_score DESC")
    suspend fun getAllPatterns(): List<PatternEntity>

    /**
     * Insert or update a pattern.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(pattern: PatternEntity)

    /**
     * Insert or update multiple patterns.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(patterns: List<PatternEntity>)

    /**
     * Update last suggested timestamp.
     */
    @Query("UPDATE patterns SET last_suggested = :timestamp WHERE item_id = :itemId")
    suspend fun updateLastSuggested(itemId: String, timestamp: Long)

    /**
     * Delete pattern for an item.
     */
    @Query("DELETE FROM patterns WHERE item_id = :itemId")
    suspend fun delete(itemId: String)

    /**
     * Get pattern count.
     */
    @Query("SELECT COUNT(*) FROM patterns")
    suspend fun getCount(): Int
}
