package com.example.pillarpocket.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pillarpocket.data.local.BudgetGoal
import com.example.pillarpocket.data.local.Expense
import com.example.pillarpocket.data.repository.BudgetGoalRepository
import com.example.pillarpocket.data.repository.CategoryRepository
import com.example.pillarpocket.data.repository.ExpenseRepository
import com.example.pillarpocket.utils.PdfReportGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Calendar


 // Represents the state of a PDF report generation request.

sealed class ReportState {
    object Idle    : ReportState()
    object Loading : ReportState()
    data class Success(val file: File)    : ReportState()
    data class Error(val message: String) : ReportState()
}

/*
  ViewModel for the Monthly Report screen.
  Manages month navigation, collects all data needed for the report,
  and coordinates PDF generation via [PdfReportGenerator].
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ReportViewModel(
    private val expenseRepository: ExpenseRepository,
    private val categoryRepository: CategoryRepository,
    private val budgetGoalRepository: BudgetGoalRepository,
    private val userId: Int
) : ViewModel() {

    private val calendar = Calendar.getInstance()

    private val _selectedMonthYear = MutableStateFlow(
        MonthYear(
            month = calendar.get(Calendar.MONTH) + 1,
            year  = calendar.get(Calendar.YEAR)
        )
    )
    val selectedMonthYear: StateFlow<MonthYear> = _selectedMonthYear.asStateFlow()

    private val _reportState = MutableStateFlow<ReportState>(ReportState.Idle)
    val reportState: StateFlow<ReportState> = _reportState.asStateFlow()

    // Budget goal for the selected month
    val budgetGoal: StateFlow<BudgetGoal?> = _selectedMonthYear
        .flatMapLatest { (month, year) ->
            budgetGoalRepository.getGoalByMonthYear(userId, month, year)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val allExpenses = expenseRepository
        .getExpensesByUser(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val allCategories = categoryRepository
        .getCategoriesByUser(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Expenses filtered to the selected month
    val filteredExpenses: StateFlow<List<Expense>> = combine(
        allExpenses, _selectedMonthYear
    ) { expenses, (month, year) ->
        val yearMonth = "%04d-%02d".format(year, month)
        expenses.filter { it.date.startsWith(yearMonth) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Category spending breakdown for the selected month
    val categorySpending: StateFlow<List<CategorySpendingItem>> = combine(
        filteredExpenses, allCategories
    ) { filtered, categories ->
        val totals     = filtered.groupBy { it.categoryId }
            .mapValues { (_, list) -> list.sumOf { it.amount } }
        val grandTotal = totals.values.sum()
        totals.mapNotNull { (categoryId, total) ->
            val category = categories.find { it.id == categoryId }
                ?: return@mapNotNull null
            CategorySpendingItem(
                category    = category,
                totalAmount = total,
                percentage  = if (grandTotal > 0)
                    (total / grandTotal * 100).toFloat() else 0f
            )
        }.sortedByDescending { it.totalAmount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Total spent in the selected month
    val monthlyTotal: StateFlow<Double> = filteredExpenses
        .map { it.sumOf { e -> e.amount } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    fun navigateToPreviousMonth() {
        val c = _selectedMonthYear.value
        _selectedMonthYear.value =
            if (c.month == 1) MonthYear(12, c.year - 1)
            else MonthYear(c.month - 1, c.year)
        _reportState.value = ReportState.Idle
    }

    fun navigateToNextMonth() {
        val c = _selectedMonthYear.value
        _selectedMonthYear.value =
            if (c.month == 12) MonthYear(1, c.year + 1)
            else MonthYear(c.month + 1, c.year)
        _reportState.value = ReportState.Idle
    }

    /*
      Generates the PDF report on an IO thread and updates [reportState].
      On success, emits [ReportState.Success] with the generated file.
     */
    fun generateReport(context: Context, monthLabel: String) {
        viewModelScope.launch {
            _reportState.value = ReportState.Loading
            try {
                val (month, year) = _selectedMonthYear.value
                val file = withContext(Dispatchers.IO) {
                    PdfReportGenerator.generate(
                        context          = context,
                        month            = month,
                        year             = year,
                        monthLabel       = monthLabel,
                        expenses         = filteredExpenses.value,
                        categories       = allCategories.value,
                        categorySpending = categorySpending.value,
                        budgetGoal       = budgetGoal.value,
                        monthlyTotal     = monthlyTotal.value
                    )
                }
                _reportState.value = ReportState.Success(file)
            } catch (e: Exception) {
                _reportState.value = ReportState.Error(
                    e.message ?: "Failed to generate report"
                )
            }
        }
    }

    fun resetState() { _reportState.value = ReportState.Idle }
}