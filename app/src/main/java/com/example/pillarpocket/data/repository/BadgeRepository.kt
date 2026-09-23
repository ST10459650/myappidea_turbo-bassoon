package com.example.pillarpocket.data.repository

import com.example.pillarpocket.data.local.BadgeType
import com.example.pillarpocket.data.local.EarnedBadge
import com.example.pillarpocket.data.local.EarnedBadgeDao
import kotlinx.coroutines.flow.Flow

class BadgeRepository(private val earnedBadgeDao: EarnedBadgeDao) {

    /** Returns a live list of all badges earned by the user. */
    fun getEarnedBadges(userId: Int): Flow<List<EarnedBadge>> =
        earnedBadgeDao.getBadgesByUser(userId)

    /** Returns the count of newly earned unseen badges. */
    fun getNewBadgeCount(userId: Int): Flow<Int> =
        earnedBadgeDao.getNewBadgeCount(userId)

    /** Marks all badges as seen when the user visits the Badges screen. */
    suspend fun markAllBadgesSeen(userId: Int) =
        earnedBadgeDao.markAllBadgesSeen(userId)

    /** Awards a badge only if the user hasn't already earned it. */
    private suspend fun awardIfNotEarned(userId: Int, type: BadgeType) {
        val existing = earnedBadgeDao.getBadgeByType(userId, type.name)
        if (existing == null) {
            earnedBadgeDao.insertBadge(
                EarnedBadge(userId = userId, badgeType = type.name)
            )
        }
    }

    /**
     * Checks all badge conditions and awards any newly qualifying badges.
     * This is called reactively whenever the user's data changes.
     */
    suspend fun checkAndAwardBadges(
        userId: Int,
        expenseCount: Int,
        categoryCount: Int,
        budgetGoalCount: Int,
        monthsWithinBudget: Int,
        hasPhotoExpense: Boolean
    ) {
        // Always award welcome badge
        awardIfNotEarned(userId, BadgeType.WELCOME)

        // Expense logging badges
        if (expenseCount >= 1)  awardIfNotEarned(userId, BadgeType.FIRST_EXPENSE)
        if (expenseCount >= 5)  awardIfNotEarned(userId, BadgeType.FIVE_EXPENSES)
        if (expenseCount >= 10) awardIfNotEarned(userId, BadgeType.TEN_EXPENSES)
        if (expenseCount >= 25) awardIfNotEarned(userId, BadgeType.TWENTY_FIVE_EXPENSES)
        if (expenseCount >= 50) awardIfNotEarned(userId, BadgeType.FIFTY_EXPENSES)
        if (hasPhotoExpense)    awardIfNotEarned(userId, BadgeType.RECEIPT_COLLECTOR)

        // Organisation badges
        if (categoryCount >= 1) awardIfNotEarned(userId, BadgeType.FIRST_CATEGORY)
        if (categoryCount >= 5) awardIfNotEarned(userId, BadgeType.FIVE_CATEGORIES)

        // Budget goal badges
        if (budgetGoalCount >= 1)    awardIfNotEarned(userId, BadgeType.FIRST_GOAL)
        if (monthsWithinBudget >= 1) awardIfNotEarned(userId, BadgeType.WITHIN_BUDGET)
        if (monthsWithinBudget >= 3) awardIfNotEarned(userId, BadgeType.THREE_MONTHS_BUDGET)
    }
}