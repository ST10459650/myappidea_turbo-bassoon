package com.example.pillarpocket.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.pillarpocket.data.repository.CategoryRepository
import com.example.pillarpocket.data.repository.ExpenseRepository

class CategorySpendingViewModelFactory (
    private val expenseRepository: ExpenseRepository,
    private val categoryRepository: CategoryRepository,
    private val userId: Int
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CategorySpendingViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CategorySpendingViewModel(expenseRepository, categoryRepository, userId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}