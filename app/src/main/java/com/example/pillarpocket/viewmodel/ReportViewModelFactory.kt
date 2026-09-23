package com.example.pillarpocket.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.pillarpocket.data.repository.BudgetGoalRepository
import com.example.pillarpocket.data.repository.CategoryRepository
import com.example.pillarpocket.data.repository.ExpenseRepository

class ReportViewModelFactory(
    private val expenseRepository: ExpenseRepository,
    private val categoryRepository: CategoryRepository,
    private val budgetGoalRepository: BudgetGoalRepository,
    private val userId: Int
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ReportViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ReportViewModel(
                expenseRepository,
                categoryRepository,
                budgetGoalRepository,
                userId
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}