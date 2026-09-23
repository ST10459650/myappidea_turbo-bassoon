package com.example.pillarpocket.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pillarpocket.data.local.Category
import com.example.pillarpocket.data.repository.CategoryRepository
import com.example.pillarpocket.data.repository.ExpenseRepository
import kotlinx.coroutines.flow.*
import java.util.Calendar

data class CategorySpendingItem(
    val category: Category,
    val totalAmount: Double,
    val percentage: Float
)

class CategorySpendingViewModel(
    private val expenseRepository: ExpenseRepository,
    private val categoryRepository: CategoryRepository,
    private val userId: Int
) : ViewModel() {

    private val calendar = Calendar.getInstance()

    private val _startDate = MutableStateFlow(
        "%04d-%02d-01".format(
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH) + 1
        )
    )
    private val _endDate = MutableStateFlow(
        "%04d-%02d-%02d".format(
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH) + 1,
            calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        )
    )

    val startDate: StateFlow<String> = _startDate.asStateFlow()
    val endDate: StateFlow<String> = _endDate.asStateFlow()

    private val expenses = expenseRepository
        .getExpensesByUser(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val categories = categoryRepository
        .getCategoriesByUser(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categorySpending: StateFlow<List<CategorySpendingItem>> = combine(
        expenses, categories, _startDate, _endDate
    ) { allExpenses, allCategories, start, end ->
        val filtered = allExpenses.filter { it.date >= start && it.date <= end }
        val totals = filtered
            .groupBy { it.categoryId }
            .mapValues { (_, list) -> list.sumOf { it.amount } }
        val grandTotal = totals.values.sum()
        totals.mapNotNull { (categoryId, total) ->
            val category = allCategories.find { it.id == categoryId } ?: return@mapNotNull null
            CategorySpendingItem(
                category = category,
                totalAmount = total,
                percentage = if (grandTotal > 0) (total / grandTotal * 100).toFloat() else 0f
            )
        }.sortedByDescending { it.totalAmount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalSpending: StateFlow<Double> = categorySpending
        .map { items -> items.sumOf { it.totalAmount } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    fun setDateRange(startDate: String, endDate: String) {
        _startDate.value = startDate
        _endDate.value = endDate
    }

    fun resetToCurrentMonth() {
        val cal = Calendar.getInstance()
        _startDate.value = "%04d-%02d-01".format(
            cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1
        )
        _endDate.value = "%04d-%02d-%02d".format(
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1,
            cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        )
    }
}