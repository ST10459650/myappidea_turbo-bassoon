package com.example.pillarpocket.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Represents a badge that has been earned by a user.
 *
 * A unique index on [userId] and [badgeType] ensures that each
 * badge can only be earned once per user. The [isNew] flag is
 * used to show a notification dot until the user views the
 * Badges screen.
 */
@Entity(
    tableName = "earned_badges",
    indices = [Index(value = ["userId", "badgeType"], unique = true)]
)
data class EarnedBadge(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: Int,
    val badgeType: String,
    val earnedAt: Long = System.currentTimeMillis(),
    val isNew: Boolean = true
)