package com.example.pillarpocket.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for earned badge operations.
 */
@Dao
interface EarnedBadgeDao {

    /**
     * Inserts a new earned badge, ignoring duplicates to prevent
     * awarding the same badge twice.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertBadge(badge: EarnedBadge)

    /**
     * Retrieves all badges earned by a specific user, ordered
     * by most recently earned first.
     */
    @Query("SELECT * FROM earned_badges WHERE userId = :userId ORDER BY earnedAt DESC")
    fun getBadgesByUser(userId: Int): Flow<List<EarnedBadge>>

    /**
     * Checks whether a specific badge has already been earned.
     * Returns null if not yet earned.
     */
    @Query("SELECT * FROM earned_badges WHERE userId = :userId AND badgeType = :badgeType LIMIT 1")
    suspend fun getBadgeByType(userId: Int, badgeType: String): EarnedBadge?

    /**
     * Returns the number of newly earned (unseen) badges for a user.
     * Used to display the notification dot on the Home screen.
     */
    @Query("SELECT COUNT(*) FROM earned_badges WHERE userId = :userId AND isNew = 1")
    fun getNewBadgeCount(userId: Int): Flow<Int>

    /**
     * Marks all badges for a user as seen, clearing the notification dot.
     */
    @Query("UPDATE earned_badges SET isNew = 0 WHERE userId = :userId")
    suspend fun markAllBadgesSeen(userId: Int)
}