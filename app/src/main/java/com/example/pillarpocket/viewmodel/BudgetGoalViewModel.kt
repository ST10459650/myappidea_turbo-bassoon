package com.example.pillarpocket.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pillarpocket.data.local.BudgetGoal
import com.example.pillarpocket.data.repository.BudgetGoalRepository
import com.example.pillarpocket.data.repository.ExpenseRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

sealed class BudgetGoalState {
    object Idle : BudgetGoalState()
    object Loading : BudgetGoalState()
    object Success : BudgetGoalState()
    data class Error(val message: String) : BudgetGoalState()
}

data class MonthYear(val month: Int, val year: Int)

@OptIn(ExperimentalCoroutinesApi::class)
class BudgetGoalViewModel(
    private val budgetGoalRepository: BudgetGoalRepository,
    private val expenseRepository: ExpenseRepository,
    private val userId: Int
) : ViewModel() {

    private val calendar = Calendar.getInstance()

    private val _selectedMonthYear = MutableStateFlow(
        MonthYear(
            month = calendar.get(Calendar.MONTH) + 1,
            year = calendar.get(Calendar.YEAR)
        )
    )
    val selectedMonthYear: StateFlow<MonthYear> = _selectedMonthYear.asStateFlow()

    private val _budgetGoalState = MutableStateFlow<BudgetGoalState>(BudgetGoalState.Idle)
    val budgetGoalState: StateFlow<BudgetGoalState> = _budgetGoalState.asStateFlow()

    val currentGoal: StateFlow<BudgetGoal?> = _selectedMonthYear
        .flatMapLatest { (month, year) ->
            budgetGoalRepository.getGoalByMonthYear(userId, month, year)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val monthlyTotal: StateFlow<Double> = _selectedMonthYear
        .flatMapLatest { (month, year) ->
            val yearMonth = "%04d-%02d".format(year, month)
            expenseRepository.getMonthlyTotal(userId, yearMonth)
                .map { it ?: 0.0 }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    fun navigateToPreviousMonth() {
        val current = _selectedMonthYear.value
        if (current.month == 1) {
            _selectedMonthYear.value = MonthYear(12, current.year - 1)
        } else {
            _selectedMonthYear.value = MonthYear(current.month - 1, current.year)
        }
        resetState()
    }

    fun navigateToNextMonth() {
        val current = _selectedMonthYear.value
        if (current.month == 12) {
            _selectedMonthYear.value = MonthYear(1, current.year + 1)
        } else {
            _selectedMonthYear.value = MonthYear(current.month + 1, current.year)
        }
        resetState()
    }

    fun setGoal(minimumGoal: Double, maximumGoal: Double) {
        val (month, year) = _selectedMonthYear.value
        viewModelScope.launch {
            _budgetGoalState.value = BudgetGoalState.Loading
            val result = budgetGoalRepository.setGoal(userId, month, year, minimumGoal, maximumGoal)
            _budgetGoalState.value = result.fold(
                onSuccess = { BudgetGoalState.Success },
                onFailure = { BudgetGoalState.Error(it.message ?: "Failed to save goal") }
            )
        }
    }

    fun resetState() { _budgetGoalState.value = BudgetGoalState.Idle }
}