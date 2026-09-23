package com.example.pillarpocket.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pillarpocket.data.local.BudgetGoal
import com.example.pillarpocket.data.local.Category
import com.example.pillarpocket.data.repository.BudgetGoalRepository
import com.example.pillarpocket.data.repository.CategoryRepository
import com.example.pillarpocket.data.repository.ExpenseRepository
import kotlinx.coroutines.flow.*
import java.util.Calendar

// Represents the overall budget health for the current month
enum class BudgetHealthStatus {
    NO_GOAL,    // No budget goal has been set
    UNDER_MIN,  // Spending is below the minimum goal
    HEALTHY,    // Spending is within min and max goals
    OVER_MAX    // Spending has exceeded the maximum goal
}

// Represents a single category's spending on the dashboard
data class DashboardCategoryItem(
    val category: Category,
    val totalAmount: Double,
    val percentageOfTotal: Float,   // Share of total monthly spending
    val percentageOfMax: Float,     // Share of the maximum budget goal
    val isOverspending: Boolean     // True if this category needs highlighting
)

class DashboardViewModel(
    private val expenseRepository: ExpenseRepository,
    private val categoryRepository: CategoryRepository,
    private val budgetGoalRepository: BudgetGoalRepository,
    private val userId: Int
) : ViewModel() {

    private val calendar = Calendar.getInstance()
    private val currentMonth = calendar.get(Calendar.MONTH) + 1
    private val currentYear  = calendar.get(Calendar.YEAR)

    // Budget goal for the current month
    val budgetGoal: StateFlow<BudgetGoal?> = budgetGoalRepository
        .getGoalByMonthYear(userId, currentMonth, currentYear)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val allExpenses = expenseRepository
        .getExpensesByUser(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val allCategories = categoryRepository
        .getCategoriesByUser(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Per-category spending breakdown for the current month
    val dashboardCategories: StateFlow<List<DashboardCategoryItem>> = combine(
        allExpenses, allCategories, budgetGoal
    ) { expenses, categories, goal ->
        val yearMonth = "%04d-%02d".format(currentYear, currentMonth)
        val filtered  = expenses.filter { it.date.startsWith(yearMonth) }
        val totals    = filtered
            .groupBy { it.categoryId }
            .mapValues { (_, list) -> list.sumOf { it.amount } }

        val grandTotal = totals.values.sum()
        val maxGoal    = goal?.maximumGoal ?: 0.0

        totals.mapNotNull { (categoryId, total) ->
            val category = categories.find { it.id == categoryId }
                ?: return@mapNotNull null

            val percentOfTotal = if (grandTotal > 0)
                (total / grandTotal * 100).toFloat() else 0f
            val percentOfMax   = if (maxGoal > 0)
                (total / maxGoal * 100).toFloat() else 0f

            // A category is flagged as overspending if:
            // 1. Total spending exceeds max goal AND this category is
            //    the highest spender, OR
            // 2. This category alone uses more than 40% of the max goal
            val isOverspending = (grandTotal > maxGoal && maxGoal > 0 &&
                    total == totals.values.max()) ||
                    (maxGoal > 0 && total > maxGoal * 0.40)

            DashboardCategoryItem(
                category          = category,
                totalAmount       = total,
                percentageOfTotal = percentOfTotal,
                percentageOfMax   = percentOfMax,
                isOverspending    = isOverspending
            )
        }.sortedByDescending { it.totalAmount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Total spending for the current month
    val monthlyTotal: StateFlow<Double> = dashboardCategories
        .map { items -> items.sumOf { it.totalAmount } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Overall budget health status
    val budgetStatus: StateFlow<BudgetHealthStatus> = combine(
        monthlyTotal, budgetGoal
    ) { total, goal ->
        when {
            goal == null           -> BudgetHealthStatus.NO_GOAL
            total > goal.maximumGoal -> BudgetHealthStatus.OVER_MAX
            total < goal.minimumGoal -> BudgetHealthStatus.UNDER_MIN
            else                   -> BudgetHealthStatus.HEALTHY
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), BudgetHealthStatus.NO_GOAL)

    // Number of categories flagged as overspending
    val overspendingCount: StateFlow<Int> = dashboardCategories
        .map { items -> items.count { it.isOverspending } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
}