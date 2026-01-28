package com.restock.core.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.restock.core.data.database.entity.SuggestionDismissalEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for suggestion dismissal tracking.
 *
 * Tracks when users dismiss suggestions to:
 * - Reduce pattern confidence after single dismissals
 * - Permanently hide items after 3 consecutive dismissals
 * - Reset dismissal state when item is actually purchased
 */
@Dao
interface SuggestionDismissalDao {
    /**
     * Insert or update a dismissal record.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(dismissal: SuggestionDismissalEntity)

    /**
     * Get dismissal record for a specific item.
     */
    @Query("SELECT * FROM suggestion_dismissals WHERE item_id = :itemId")
    suspend fun getDismissal(itemId: String): SuggestionDismissalEntity?

    /**
     * Get all item IDs that are persistently dismissed.
     *
     * These items should not appear in suggestions until purchased.
     */
    @Query("SELECT item_id FROM suggestion_dismissals WHERE is_persistently_dismissed = 1")
    suspend fun getPersistentlyDismissedIds(): List<String>

    /**
     * Observe all dismissal records for reactive updates.
     */
    @Query("SELECT * FROM suggestion_dismissals")
    fun observeDismissals(): Flow<List<SuggestionDismissalEntity>>

    /**
     * Delete dismissal record for an item.
     *
     * Called when an item is deleted from the system.
     */
    @Query("DELETE FROM suggestion_dismissals WHERE item_id = :itemId")
    suspend fun clearDismissal(itemId: String)

    /**
     * Reset dismissal state for an item after purchase.
     *
     * Resets dismiss_count to 0 and is_persistently_dismissed to false,
     * allowing the item to be suggested again.
     */
    @Query(
        """
        UPDATE suggestion_dismissals
        SET dismiss_count = 0, is_persistently_dismissed = 0
        WHERE item_id = :itemId
    """,
    )
    suspend fun resetDismissal(itemId: String)

    /**
     * Get count of all dismissal records.
     */
    @Query("SELECT COUNT(*) FROM suggestion_dismissals")
    suspend fun getCount(): Int

    /**
     * Get count of persistently dismissed items.
     */
    @Query("SELECT COUNT(*) FROM suggestion_dismissals WHERE is_persistently_dismissed = 1")
    suspend fun getPersistentlyDismissedCount(): Int
}
