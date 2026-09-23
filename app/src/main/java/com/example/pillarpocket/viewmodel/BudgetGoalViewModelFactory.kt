package com.example.pillarpocket.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.pillarpocket.data.repository.BudgetGoalRepository
import com.example.pillarpocket.data.repository.ExpenseRepository

class BudgetGoalViewModelFactory(
    private val budgetGoalRepository: BudgetGoalRepository,
    private val expenseRepository: ExpenseRepository,
    private val userId: Int
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BudgetGoalViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BudgetGoalViewModel(budgetGoalRepository, expenseRepository, userId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}