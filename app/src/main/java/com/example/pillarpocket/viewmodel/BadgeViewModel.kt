package com.example.pillarpocket.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pillarpocket.data.local.BadgeType
import com.example.pillarpocket.data.local.EarnedBadge
import com.example.pillarpocket.data.repository.BadgeRepository
import com.example.pillarpocket.data.repository.BudgetGoalRepository
import com.example.pillarpocket.data.repository.CategoryRepository
import com.example.pillarpocket.data.repository.ExpenseRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/** Pairs a badge type definition with its earned record (if any). */
data class BadgeDisplayItem(
    val type: BadgeType,
    val earnedBadge: EarnedBadge?
) {
    val isEarned: Boolean get() = earnedBadge != null
    val isNew: Boolean get() = earnedBadge?.isNew ?: false
}

class BadgeViewModel(
    private val badgeRepository: BadgeRepository,
    private val expenseRepository: ExpenseRepository,
    private val categoryRepository: CategoryRepository,
    private val budgetGoalRepository: BudgetGoalRepository,
    private val userId: Int
) : ViewModel() {

    private val earnedBadges: StateFlow<List<EarnedBadge>> = badgeRepository
        .getEarnedBadges(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Count of newly earned unseen badges — drives the notification dot. */
    val newBadgeCount: StateFlow<Int> = badgeRepository
        .getNewBadgeCount(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    /** All badge types paired with their earned status for display. */
    val allBadges: StateFlow<List<BadgeDisplayItem>> = earnedBadges
        .map { earned ->
            BadgeType.values().map { type ->
                BadgeDisplayItem(
                    type        = type,
                    earnedBadge = earned.find { it.badgeType == type.name }
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val earnedCount: StateFlow<Int> = earnedBadges
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    init {
        // Reactively check and award badges whenever user data changes
        viewModelScope.launch {
            combine(
                expenseRepository.getExpensesByUser(userId),
                categoryRepository.getCategoriesByUser(userId),
                budgetGoalRepository.getAllGoals(userId)
            ) { expenses, categories, goals ->
                val monthsWithinBudget = goals.count { goal ->
                    val yearMonth = "%04d-%02d".format(goal.year, goal.month)
                    val total = expenses
                        .filter { it.date.startsWith(yearMonth) }
                        .sumOf { it.amount }
                    total >= goal.minimumGoal && total <= goal.maximumGoal
                }
                badgeRepository.checkAndAwardBadges(
                    userId             = userId,
                    expenseCount       = expenses.size,
                    categoryCount      = categories.size,
                    budgetGoalCount    = goals.size,
                    monthsWithinBudget = monthsWithinBudget,
                    hasPhotoExpense    = expenses.any { it.photoUri != null }
                )
            }.collect()
        }
    }

    /** Marks all badges as seen when the user opens the Badges screen. */
    fun markAllSeen() {
        viewModelScope.launch {
            badgeRepository.markAllBadgesSeen(userId)
        }
    }
}