package com.example.pillarpocket.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pillarpocket.data.local.BudgetGoal
import com.example.pillarpocket.data.repository.BudgetGoalRepository
import com.example.pillarpocket.data.repository.CategoryRepository
import com.example.pillarpocket.data.repository.ExpenseRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import java.util.Calendar

@OptIn(ExperimentalCoroutinesApi::class)
class SpendingGraphViewModel(
    private val expenseRepository: ExpenseRepository,
    private val categoryRepository: CategoryRepository,
    private val budgetGoalRepository: BudgetGoalRepository,
    private val userId: Int
) : ViewModel() {

    private val calendar = Calendar.getInstance()

    // Tracks the currently selected month and year for the graph
    private val _selectedMonthYear = MutableStateFlow(
        MonthYear(
            month = calendar.get(Calendar.MONTH) + 1,
            year = calendar.get(Calendar.YEAR)
        )
    )
    val selectedMonthYear: StateFlow<MonthYear> = _selectedMonthYear.asStateFlow()

    private val expenses = expenseRepository
        .getExpensesByUser(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val categories = categoryRepository
        .getCategoriesByUser(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Budget goal for the currently selected month
    val budgetGoal: StateFlow<BudgetGoal?> = _selectedMonthYear
        .flatMapLatest { (month, year) ->
            budgetGoalRepository.getGoalByMonthYear(userId, month, year)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Category spending breakdown for the selected month
    val categorySpending: StateFlow<List<CategorySpendingItem>> = combine(
        expenses, categories, _selectedMonthYear
    ) { allExpenses, allCategories, monthYear ->
        val yearMonth = "%04d-%02d".format(monthYear.year, monthYear.month)
        val filtered = allExpenses.filter { it.date.startsWith(yearMonth) }
        val totals = filtered
            .groupBy { it.categoryId }
            .mapValues { (_, list) -> list.sumOf { it.amount } }
        val grandTotal = totals.values.sum()
        totals.mapNotNull { (categoryId, total) ->
            val category = allCategories.find { it.id == categoryId }
                ?: return@mapNotNull null
            CategorySpendingItem(
                category = category,
                totalAmount = total,
                percentage = if (grandTotal > 0)
                    (total / grandTotal * 100).toFloat() else 0f
            )
        }.sortedByDescending { it.totalAmount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Total spending for the selected month
    val monthlyTotal: StateFlow<Double> = categorySpending
        .map { items -> items.sumOf { it.totalAmount } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    fun navigateToPreviousMonth() {
        val current = _selectedMonthYear.value
        _selectedMonthYear.value = if (current.month == 1)
            MonthYear(12, current.year - 1)
        else
            MonthYear(current.month - 1, current.year)
    }

    fun navigateToNextMonth() {
        val current = _selectedMonthYear.value
        _selectedMonthYear.value = if (current.month == 12)
            MonthYear(1, current.year + 1)
        else
            MonthYear(current.month + 1, current.year)
    }
}