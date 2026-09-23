package com.example.pillarpocket.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.pillarpocket.data.repository.*

class BadgeViewModelFactory(
    private val badgeRepository: BadgeRepository,
    private val expenseRepository: ExpenseRepository,
    private val categoryRepository: CategoryRepository,
    private val budgetGoalRepository: BudgetGoalRepository,
    private val userId: Int
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BadgeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BadgeViewModel(
                badgeRepository,
                expenseRepository,
                categoryRepository,
                budgetGoalRepository,
                userId
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}