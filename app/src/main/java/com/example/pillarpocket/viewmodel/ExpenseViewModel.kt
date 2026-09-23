package com.example.pillarpocket.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pillarpocket.data.local.Category
import com.example.pillarpocket.data.local.Expense
import com.example.pillarpocket.data.repository.CategoryRepository
import com.example.pillarpocket.data.repository.ExpenseRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class ExpenseState {
    object Idle : ExpenseState()
    object Loading : ExpenseState()
    object Success : ExpenseState()
    data class Error(val message: String) : ExpenseState()
}

class ExpenseViewModel(
    private val expenseRepository: ExpenseRepository,
    private val categoryRepository: CategoryRepository,
    private val userId: Int
) : ViewModel() {

    private val _expenseState = MutableStateFlow<ExpenseState>(ExpenseState.Idle)
    val expenseState: StateFlow<ExpenseState> = _expenseState.asStateFlow()

    private val _filterStartDate = MutableStateFlow<String?>(null)
    private val _filterEndDate = MutableStateFlow<String?>(null)

    val filterStartDate: StateFlow<String?> = _filterStartDate.asStateFlow()
    val filterEndDate: StateFlow<String?> = _filterEndDate.asStateFlow()

    val isFilterActive: StateFlow<Boolean> = combine(
        _filterStartDate, _filterEndDate
    ) { start, end -> start != null && end != null }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val expenses: StateFlow<List<Expense>> = expenseRepository
        .getExpensesByUser(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categories: StateFlow<List<Category>> = categoryRepository
        .getCategoriesByUser(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredExpenses: StateFlow<List<Expense>> = combine(
        expenses, _filterStartDate, _filterEndDate
    ) { allExpenses, startDate, endDate ->
        if (startDate != null && endDate != null) {
            allExpenses.filter { it.date >= startDate && it.date <= endDate }
        } else {
            allExpenses
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredTotal: StateFlow<Double> = filteredExpenses
        .map { list -> list.sumOf { it.amount } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalExpenses: StateFlow<Double> = expenseRepository
        .getTotalExpenses(userId)
        .map { it ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    fun setDateFilter(startDate: String, endDate: String) {
        _filterStartDate.value = startDate
        _filterEndDate.value = endDate
    }

    fun clearFilter() {
        _filterStartDate.value = null
        _filterEndDate.value = null
    }

    fun addExpense(
        categoryId: Int,
        amount: Double,
        date: String,
        startTime: String,
        endTime: String,
        description: String,
        photoUri: String? = null
    ) {
        viewModelScope.launch {
            _expenseState.value = ExpenseState.Loading
            val result = expenseRepository.addExpense(
                userId, categoryId, amount, date, startTime, endTime, description, photoUri
            )
            _expenseState.value = result.fold(
                onSuccess = { ExpenseState.Success },
                onFailure = { ExpenseState.Error(it.message ?: "Failed to add expense") }
            )
        }
    }

    fun resetState() { _expenseState.value = ExpenseState.Idle }
}